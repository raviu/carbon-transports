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

import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.Request;

import java.util.UUID;

public class SourceWorker implements Runnable {
    private static Logger log = Logger.getLogger(SourceWorker.class);

    private Engine engine;
    private Request sourceRequest;
    private ChannelHandlerContext sourceRequestCtx;

    public SourceWorker(Engine engine, Request sourceRequest,
                        ChannelHandlerContext sourceRequestCtx) {
        this.engine = engine;
        this.sourceRequest = sourceRequest;
        this.sourceRequestCtx = sourceRequestCtx;
    }

    public void run() {
        CarbonMessage msg = new CarbonMessageImpl(Constants.PROTOCOL_NAME);
        msg.setId(UUID.randomUUID());
        msg.setProperty(Constants.PROTOCOL_NAME, Constants.REQUEST, sourceRequest);
        msg.setProperty(Constants.PROTOCOL_NAME, Constants.CHNL_HNDLR_CTX, sourceRequestCtx);
        msg.setProperty(Constants.PROTOCOL_NAME, Constants.PIPE, sourceRequest.getPipe());
        engine.receive(msg);
    }

}
