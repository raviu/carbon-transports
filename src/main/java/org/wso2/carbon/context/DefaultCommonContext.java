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

package org.wso2.carbon.context;

import java.util.HashMap;
import java.util.Map;

public class DefaultCommonContext implements CommonContext {

    private String messageId;

    private Map<String, Object> propertyMap = new HashMap<String, Object>();

    public void init() {
        /*Add default properties */
    }

    public Object getProperty(String key) {

        return propertyMap.get(key);
    }

    public void setProperty(String key, Object val) {
        propertyMap.put(key, val);
    }

    public String getCtxId() {
        return messageId;
    }

    public void setCtxId(String messageId) {
        this.messageId = messageId;
    }

    public void reset() {
        propertyMap.clear();
    }
}