/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.rx.jaxrs;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.client.ClientCommon;
import org.coodex.concrete.rx.RXClientProvider;
import org.coodex.concrete.jaxrs.Client;
import org.coodex.concrete.rx.ReactiveExtensionFor;
import org.coodex.concurrent.ExecutorsHelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public class JaxRS_RXClientProvider implements RXClientProvider {

    private static ExecutorService threadPool = null;

    private static synchronized ExecutorService getThreadPool(){
        if (threadPool == null) {
            threadPool =ExecutorsHelper.newCachedThreadPool();
        }
        return threadPool;
    }

    @Override
    public <T> T getInstance(Class<T> clz, final ClientCommon.Domain domain) {
        final Class<? extends ConcreteService> serviceClass = clz.getAnnotation(ReactiveExtensionFor.class).value();

        final ConcreteService instance = Client.getInstance(serviceClass, domain.getIdentify());

        return (T) Proxy.newProxyInstance(JaxRS_RXClientProvider.class.getClassLoader(), new Class[]{clz}, new InvocationHandler() {

            private Object sync(final Method method, final Object[] args) throws Throwable {
                return Observable.create(new ObservableOnSubscribe() {
                    @Override
                    public void subscribe(ObservableEmitter e) throws Exception {
                        Object result = method.invoke(instance, args);
                        if (result != null)
                            e.onNext(result);
                        e.onComplete();
                    }
                });
            }

            private Object async(final Method method, final Object[] args) throws Throwable {
                return Observable.create(new ObservableOnSubscribe() {
                    @Override
                    public void subscribe(final ObservableEmitter e) throws Exception {
                        getThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Object result = method.invoke(instance, args);
                                    if (result != null)
                                        e.onNext(result);
                                    e.onComplete();
                                } catch (Throwable th) {
                                    e.onError(th);
                                }
                            }
                        });

                    }
                });
            }

            private Method findMethod(Method method) {
                for(Method m: serviceClass.getMethods()){
                    if(m.getName().equals(method.getName()) && Arrays.equals(m.getParameterTypes(), method.getParameterTypes())){
                        return m;
                    }
                }
                throw new RuntimeException("service method not found: " + method.getName());
            }

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                return domain.isAsyncSupport() ? async(findMethod(method), args) : sync(findMethod(method), args);
            }
        });


    }

    @Override
    public boolean accept(ClientCommon.Domain param) {
        String host = param.getIdentify().toLowerCase();
        return host.startsWith("http://") || host.startsWith("https://");
    }
}