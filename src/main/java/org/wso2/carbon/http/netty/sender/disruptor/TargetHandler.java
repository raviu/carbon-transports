/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.http.netty.sender.disruptor;


import com.lmax.disruptor.dsl.Disruptor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpResponse;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.disruptor.DisruptorFactory;
import org.wso2.carbon.disruptor.publisher.CarbonEventPublisher;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.Util;

import java.net.InetSocketAddress;

public class TargetHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = Logger.getLogger(TargetHandler.class);

    private Engine engine;
    private ChannelHandlerContext inboundChannelHandlerContext;
    private Disruptor disruptor;

    public TargetHandler(Engine engine, ChannelHandlerContext inboundChannelHandlerContext , Disruptor disruptor) {
        this.engine = engine;
        this.inboundChannelHandlerContext = inboundChannelHandlerContext;
       // this.disruptor = disruptor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
       disruptor = DisruptorFactory.getDisruptor(20);
      //  disruptor.start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CarbonMessage   cMsg = new CarbonMessageImpl(Constants.PROTOCOL_NAME);
        cMsg.setPort(((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        cMsg.setHost(((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        cMsg.setProperty(Constants.PROTOCOL_NAME,
                         Constants.CHNL_HNDLR_CTX, inboundChannelHandlerContext);
        cMsg.setProperty(Constants.PROTOCOL_NAME,Constants.ENGINE,engine);
        cMsg.setDirection(CarbonMessage.OUT);
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;

            cMsg.setDirection(CarbonMessageImpl.OUT);
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                             Constants.HTTP_STATUS_CODE, httpResponse.getStatus().code());
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                             Constants.TRANSPORT_HEADERS, Util.getHeaders(httpResponse));

            cMsg.setStatus(Constants.HEADERS);
            cMsg.setEvent(msg);
            cMsg.setProperty(Constants.PROTOCOL_NAME,Constants.ENGINE,engine);
            disruptor.publishEvent(new CarbonEventPublisher(cMsg));
        } else {
            cMsg.setEvent(msg);
            cMsg.setStatus(Constants.BODY);
            cMsg.setProperty(Constants.PROTOCOL_NAME,Constants.ENGINE,engine);
            disruptor.publishEvent(new CarbonEventPublisher(cMsg));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Target channel closed.");
     //   disruptor.shutdown();
    }

}
