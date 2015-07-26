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
package org.wso2.carbon.controller;

import org.apache.log4j.Logger;
import org.wso2.carbon.api.EngineAPI;
import org.wso2.carbon.context.CommonContext;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.Request;

public class Engine implements EngineAPI {
    private static Logger log = Logger.getLogger(Engine.class);

    public boolean init() {
        return true;
    }

    public boolean receive(CommonContext ctx) {
        log.info("Engine receive");
        Request request = (Request) ctx.getProperty(Constants.MSG_OBJ);
        log.info(request.getHttpMethod() + " "  + request.getUri() + " " + request.getHttpVersion());

        return true;
    }
}
