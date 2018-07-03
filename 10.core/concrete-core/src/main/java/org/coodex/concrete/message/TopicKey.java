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

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.util.Common;

import java.lang.reflect.Type;

public class TopicKey {
    String queue;
    Type topicType;

    public TopicKey(String queue, Type topicType) {
        this.queue = Common.isBlank(queue) ? ConcreteHelper.getProfile().getString("queue.default") : queue;
        this.topicType = topicType;
    }

    static TopicKey copy(TopicKey topicKey) {
        return new TopicKey(topicKey.queue, topicKey.topicType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopicKey topicKey = (TopicKey) o;

        if (queue != null ? !queue.equals(topicKey.queue) : topicKey.queue != null) return false;
        return topicType != null ? topicType.equals(topicKey.topicType) : topicKey.topicType == null;
    }

    @Override
    public int hashCode() {
        int result = queue != null ? queue.hashCode() : 0;
        result = 31 * result + (topicType != null ? topicType.hashCode() : 0);
        return result;
    }
}