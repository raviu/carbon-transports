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

import java.util.Map;

public interface CarbonMessage {

    String getHost();
    void setHost(String host);

    int getPort();
    void setPort(int port);

    String getURI();
    void setURI(String to);

    String replyTo();
    void setReplyTo(String replyTo);

    int getDirection();
    void setDirection(int direction);

    String getProtocol();
    void setProtocol(String protocol);

    Object getProperty(String protocol, String key);
    void setProperty(String protocol, String key, Object value);

    Map<String, Map<String, Object>> getProperties();
    void setProperties(Map<String, Map<String, Object>> properties);
}
