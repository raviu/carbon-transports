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
package org.wso2.carbon.http.netty.common;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import org.wso2.carbon.api.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

public class Util {

    public static Map<String, String> getHeaders(HttpMessage message) {
        Map<String, String> headers = new HashMap<String, String>();
        if (message.headers() != null) {
            for (String k: message.headers().names()) {
                headers.put(k, message.headers().get(k));
            }
        }

        return headers;
    }

    public static void setHeaders(HttpMessage message, Map<String, String> headers) {
        HttpHeaders httpHeaders = message.headers();
        for (Map.Entry<String, String> e: headers.entrySet()) {
            httpHeaders.add(e.getKey(), e.getValue());
        }
    }

    public static String getStringValue(CarbonMessage msg, String key, String defaultValue) {
        String value = (String) msg.getProperty(Constants.PROTOCOL_NAME, key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public static int getIntValue(CarbonMessage msg, String key, int defaultValue) {
        Integer value = (Integer) msg.getProperty(Constants.PROTOCOL_NAME, key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }
}
