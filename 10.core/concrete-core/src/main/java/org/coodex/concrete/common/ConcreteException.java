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

package org.coodex.concrete.common;

import org.coodex.util.Common;

/**
 * ConcreteException， Concrete异常.
 * <p>
 * Created by davidoff shen on 2016-08-29.
 */
public class ConcreteException extends RuntimeException {

    private int code;
    private String message;
    private Object[] o;

    /**
     * 根据系统的异常代码构建
     *
     * @param code
     */
    public ConcreteException(int code, Object... objects) {
        this.code = code;
        this.o = objects;
        if (this.o != null && this.o.length > 0) {
            if (this.o[o.length - 1] instanceof Throwable) {
                this.initCause((Throwable) this.o[o.length - 1]);
                if (this.o[o.length - 1] instanceof ConcreteException)
                    throw (ConcreteException) this.o[o.length - 1];
            }
        }

        this.message = buildMessage();
    }

    private String buildMessage() {
        String message = ErrorMessageFacade.getMessage(code, o);
        return Common.isBlank(message) ? String.format("error code: %06d", code) : message;
    }


    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
