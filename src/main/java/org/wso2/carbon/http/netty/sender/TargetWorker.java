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
package org.wso2.carbon.http.netty.sender;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.Response;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class TargetWorker implements Runnable {
    private static Logger log = Logger.getLogger(TargetWorker.class);

    private Engine engine;
    private Response targetResponse;
    private ChannelHandlerContext channelHandlerContext;

    public TargetWorker(Engine engine, ChannelHandlerContext channelHandlerContext,
                        Response targetResponse) {
        this.engine = engine;
        this.targetResponse = targetResponse;
        this.channelHandlerContext = channelHandlerContext;
    }

    public void run() {
        //TODO call Engine.receive();
        CarbonMessage msg = new CarbonMessageImpl(Constants.PROTOCOL_NAME);
        msg.setDirection(CarbonMessageImpl.OUT);
        msg.setProperty(Constants.PROTOCOL_NAME, Constants.RESPONSE, targetResponse);
        msg.setProperty(Constants.PROTOCOL_NAME, Constants.CHNL_HNDLR_CTX, channelHandlerContext);
        msg.setProperty(Constants.PROTOCOL_NAME, Constants.PIPE, targetResponse.getPipe());

        engine.receive(msg);
    }

    public ChannelHandlerContext getInboundChannelHandlerContext() {
        return channelHandlerContext;
    }


}