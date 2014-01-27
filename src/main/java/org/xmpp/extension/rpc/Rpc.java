/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.xmpp.extension.rpc;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
public final class Rpc {

    @XmlElement(name = "methodCall")
    private MethodCall methodCall;

    @XmlElement(name = "methodResponse")
    private MethodResponse methodResponse;

    private Rpc() {

    }

    public Rpc(String methodName, Value... parameters) {
        this.methodCall = new MethodCall(methodName, parameters);
    }

    public MethodCall getMethodCall() {
        return methodCall;
    }

    public MethodResponse getMethodResponse() {
        return methodResponse;
    }

    static final class MethodCall {
        @XmlElement(name = "methodName")
        private String methodName;

        @XmlElementWrapper(name = "params")
        @XmlElement(name = "param")
        private List<Parameter> parameters = new ArrayList<>();

        private MethodCall() {

        }

        public MethodCall(String methodName, Value... parameters) {
            this.methodName = methodName;
            for (Value value : parameters) {
                this.parameters.add(new Parameter(value));
            }
        }

        public String getMethodName() {
            return methodName;
        }

        public List<Parameter> getParameters() {
            return parameters;
        }


    }

    static final class MethodResponse {
        private MethodResponse() {
        }

        MethodResponse(Value value) {
            this.parameters.add(new Parameter(value));
        }

        MethodResponse(Fault fault) {
            this.fault = fault;
        }

        @XmlElementWrapper(name = "params")
        @XmlElement(name = "param")
        private List<Parameter> parameters = new ArrayList<>();

        @XmlElement(name = "fault")
        private Fault fault;

        public Value getResponse() {
            if (parameters != null && !parameters.isEmpty()) {
                return parameters.get(0).getValue();
            }
            return null;
        }

        public Fault getFault() {
            return fault;
        }

        public static final class Fault {

            @XmlElement(name = "value")
            private Value value;

            public int getFaultCode() {
                if (value != null) {
                    Map<String, Value> map = value.getAsMap();
                    Value faultCode = map.get("faultCode");
                    if (faultCode != null) {
                        Integer value = faultCode.getAsInteger();
                        if (value != null) {
                            return value.intValue();
                        }
                    }
                }
                return 0;
            }

            public String getFaultString() {
                if (value != null) {
                    Map<String, Value> map = value.getAsMap();
                    Value faultCode = map.get("faultCode");
                    if (faultCode != null) {
                        return faultCode.getAsString();
                    }
                }
                return null;
            }
        }
    }
}
