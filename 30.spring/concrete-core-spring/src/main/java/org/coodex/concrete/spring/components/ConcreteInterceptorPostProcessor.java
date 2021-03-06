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

package org.coodex.concrete.spring.components;

import org.coodex.concrete.core.intercept.ConcreteInterceptor;
import org.coodex.concrete.core.intercept.InterceptorChain;
import org.coodex.concrete.spring.aspects.ConcreteAOPChain;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ConcreteInterceptorPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

    @Inject
    private ConcreteAOPChain concreteAOPChain;

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        if (bean instanceof ConcreteInterceptor && !(bean instanceof InterceptorChain)) {
            concreteAOPChain.add((ConcreteInterceptor) bean);
        }
        return super.postProcessAfterInstantiation(bean, beanName);
    }
}
