/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.api;

import java.util.HashMap;
import java.util.Map;

public abstract class TransportSender<T> {

    private Map<T, CarbonCallback> callbackMap = new HashMap<T, CarbonCallback>();

    private String protocol;
    private Engine engine;

    public TransportSender(String protocol) {
        this.protocol = protocol;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public String getProtocol() { return protocol; }

    public void setProtocol(String protocol) { this.protocol = protocol; }

    public abstract boolean init();

    public abstract boolean send(CarbonMessage msg, CarbonCallback callback);

    public CarbonCallback getCallback(T obj) {
        return callbackMap.get(obj);
    }

    public CarbonCallback consumeCallback(T obj) {
        CarbonCallback c = getCallback(obj);
        removeCallback(obj);
        return c;
    }

    public void putCallback(T obj, CarbonCallback callback) {
        callbackMap.put(obj, callback);
    }

    public void removeCallback(T obj) {
        callbackMap.remove(obj);
    }

}
