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
package org.wso2.carbon.http.netty.listener;

import io.netty.bootstrap.Bootstrap;
import org.apache.log4j.Logger;
import org.wso2.carbon.controller.Engine;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.Request;
import org.wso2.carbon.http.netty.sender.TargetHandler;
import org.wso2.carbon.context.CommonContext;
import org.wso2.carbon.context.DefaultCommonContext;

public class SourceWorker implements Runnable {
    private static Logger log = Logger.getLogger(SourceWorker.class);

    private Engine engine;
    private Request sourceRequest;
    private SourceHandler inboudnHandler;
    private CommonContext commonContext;
    private TargetHandler outboundHandler;
    private Bootstrap bootstrap;

    public SourceWorker(TargetHandler outboundHandler, Request sourceRequest,
                        SourceHandler inboudnHandler, Engine engine, Bootstrap bootstrap) {
        this.engine = engine;
        this.sourceRequest = sourceRequest;
        this.inboudnHandler = inboudnHandler;
        this.commonContext = new DefaultCommonContext();
        this.commonContext.setProperty("REQUEST", sourceRequest);
        this.outboundHandler = outboundHandler;
        this.bootstrap = bootstrap;
    }

    public void run() {
        DefaultCommonContext ctx = new DefaultCommonContext();
        ctx.setProperty(Constants.INCOMING_REQUEST, sourceRequest);
        ctx.setProperty(Constants.BOOTSTRAP, bootstrap);
        engine.receive(ctx);
    }


    public TargetHandler getOutboundHandler() {
        return outboundHandler;
    }

}
