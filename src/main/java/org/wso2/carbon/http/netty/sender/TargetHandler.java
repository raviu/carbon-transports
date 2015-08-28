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

package org.wso2.carbon.http.netty.sender;


import com.lmax.disruptor.RingBuffer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonCallback;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.disruptor.publisher.CarbonEventPublisher;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.HTTPContentChunk;
import org.wso2.carbon.http.netty.common.Pipe;
import org.wso2.carbon.http.netty.common.Util;

import java.net.InetSocketAddress;

public class TargetHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = Logger.getLogger(TargetHandler.class);

    private CarbonCallback callback;
    private RingBuffer ringBuffer;
    private CarbonMessage cMsg;
    private Pipe pipe;
    private int queuesize;
    private int trgId;

    public TargetHandler(RingBuffer ringBuffer, int trgId , int queuesize) {
        this.ringBuffer = ringBuffer;
        this.trgId = trgId;
        this.queuesize = queuesize;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpResponse) {
            cMsg = new CarbonMessageImpl(Constants.PROTOCOL_NAME);
            cMsg.setPort(((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
            cMsg.setHost(((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
            cMsg.setDirection(CarbonMessage.OUT);
            cMsg.setProperty(Constants.PROTOCOL_NAME, Constants.RESPONSE_CALLBACK, callback);
            HttpResponse httpResponse = (HttpResponse) msg;
            cMsg.setDirection(CarbonMessageImpl.OUT);
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                             Constants.HTTP_STATUS_CODE, httpResponse.getStatus().code());
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                             Constants.TRANSPORT_HEADERS, Util.getHeaders(httpResponse));
            pipe = new Pipe("Target Pipe" , queuesize);
            cMsg.setPipe(pipe);
            ringBuffer.publishEvent(new CarbonEventPublisher(cMsg, trgId));
        } else {
            HTTPContentChunk chunk;
            if (cMsg != null) {
                if (msg instanceof LastHttpContent) {
                    LastHttpContent lastHttpContent = (LastHttpContent) msg;
                    chunk = new HTTPContentChunk(lastHttpContent);
                } else {
                    DefaultHttpContent httpContent = (DefaultHttpContent) msg;
                    chunk = new HTTPContentChunk(httpContent);
                }
                cMsg.getPipe().addContentChunk(chunk);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Target channel closed.");
    }

    public void setCallback(CarbonCallback callback) {
        this.callback = callback;
    }


}
