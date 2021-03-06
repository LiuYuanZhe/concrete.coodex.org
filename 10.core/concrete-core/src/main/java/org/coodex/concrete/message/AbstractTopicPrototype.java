/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.message;

import org.coodex.concrete.common.IF;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public abstract class AbstractTopicPrototype<M extends Serializable> implements AbstractTopic<M> {

    private final static Logger log = LoggerFactory.getLogger(AbstractTopicPrototype.class);

    private static Singleton<Executor> pool = new Singleton<Executor>(
            new Singleton.Builder<Executor>() {
                @Override
                public Executor build() {
                    // todo 根据配置获取
                    return ExecutorsHelper.newFixedThreadPool(10);
                }
            }
    );
    private final Courier courier;
    private Map<Observer<M>, SubscriptionImpl> subscriptions = new ConcurrentHashMap<Observer<M>, SubscriptionImpl>();

    public AbstractTopicPrototype(Courier<M> courier) {
        this.courier = courier;
        IF.isNull(courier, "courier MUST NOT null.")
                .associate(this);
    }

    protected Set<Observer<M>> getObservers() {
        return Collections.unmodifiableSet(subscriptions.keySet());
    }

    private void remove(Observer<M> observer) {
        if (subscriptions.containsKey(observer)) {
            synchronized (subscriptions) {
                if (subscriptions.containsKey(observer)) {
                    subscriptions.remove(observer);
                }
            }
        }
    }

    protected String getQueue() {
        return courier instanceof CourierPrototype ?
                ((CourierPrototype) courier).getQueue() : null;
    }

    protected Courier getCourier() {
        return courier;
    }

    protected Executor getExecutor() {
        return pool.getInstance();
    }

    @Override
    public Subscription subscribe(Observer<M> observer) {
        if (!subscriptions.containsKey(observer)) {
            synchronized (subscriptions) {
                if (!subscriptions.containsKey(observer)) {
                    subscriptions.put(observer, new SubscriptionImpl(observer));
                }
            }
        }
        return subscriptions.get(observer);
    }


    public void notify(final M message) {
        Set<Observer<M>> observers = getObservers();
        for (final Observer<M> observer : observers) {

            final MessageFilter<M> finalFilter = (observer instanceof MessageFilter) ? (MessageFilter<M>) observer : null;
            getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (finalFilter == null || finalFilter.handle(message)) {
                            observer.update(message);
                        }
                    } catch (Throwable throwable) {
                        log.warn("message update failed.", throwable);
                    }
                }
            });

        }
    }

    private class SubscriptionImpl implements Subscription {

        private final Observer<M> observer;

        private SubscriptionImpl(Observer<M> observer) {
            this.observer = observer;
        }

        @Override
        public void cancel() {
            remove(observer);
        }
    }
}

