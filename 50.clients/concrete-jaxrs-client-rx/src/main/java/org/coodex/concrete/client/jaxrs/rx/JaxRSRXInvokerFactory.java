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

package org.coodex.concrete.client.jaxrs.rx;

import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.Invoker;
import org.coodex.concrete.client.jaxrs.JaxRSInvokerFactory;
import org.coodex.concrete.client.rx.AbstractRXInvokerFactory;
import org.coodex.concrete.client.rx.SyncToRxInvoker;

public class JaxRSRXInvokerFactory extends AbstractRXInvokerFactory {
    @Override
    public Invoker getInvoker(Destination destination) {
        return new SyncToRxInvoker(destination);
    }

    @Override
    public boolean accept(Destination param) {
        return super.accept(param) && JaxRSInvokerFactory.isJaxRS(param.getLocation());
    }
}
