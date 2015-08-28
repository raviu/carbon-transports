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

package org.wso2.carbon.http.netty.listener;

import com.lmax.disruptor.RingBuffer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.disruptor.DisruptorFactory;
import org.wso2.carbon.disruptor.publisher.CarbonEventPublisher;
import org.wso2.carbon.http.netty.common.*;
import org.wso2.carbon.http.netty.sender.TargetChanel;
import org.wso2.carbon.http.netty.sender.TargetHandler;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * A Class responsible for handle handle incoming message through netty inbound pipeline
 */
public class SourceHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = Logger.getLogger(SourceHandler.class);

    private Engine engine;
    private RingBuffer disruptor;
    private ResponseCallback responseCallback;
    private ChannelHandlerContext ctx;
    private CarbonMessage cMsg;
    private Pipe pipe;
    private Map<String, TargetChanel> channelFutureMap = new HashMap<>();
    private TargetHandler targetHandler;
    private int srcId;
    private int queueSize;
    private Object lock = new Object();

    public SourceHandler(Engine engine, int srcId, int queueSize) {
        this.engine = engine;
        this.srcId = srcId;
        this.queueSize = queueSize;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        disruptor = DisruptorFactory.getDisruptorFromMap();
        this.ctx=ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            cMsg = new CarbonMessageImpl(Constants.PROTOCOL_NAME);
            cMsg.setPort(((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
            cMsg.setHost(((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
            cMsg.setProperty(Constants.PROTOCOL_NAME, Constants.CHNL_HNDLR_CTX, this.ctx);
            cMsg.setProperty(Constants.PROTOCOL_NAME, Constants.SRC_HNDLR, this);
            cMsg.setProperty(Constants.PROTOCOL_NAME,Constants.ENGINE,engine);
            cMsg.setProperty(Constants.PROTOCOL_NAME,Constants.SRC_HNDLR,this);
            responseCallback = new ResponseCallback(this.ctx);
            cMsg.setProperty(Constants.PROTOCOL_NAME,Constants.RESPONSE_CALLBACK ,responseCallback);
            HttpRequest httpRequest = (HttpRequest) msg;
            cMsg.setURI(httpRequest.getUri());
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                             Constants.HTTP_VERSION, httpRequest.getProtocolVersion().text());
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                             Constants.HTTP_METHOD, httpRequest.getMethod().name());
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                             Constants.TRANSPORT_HEADERS, Util.getHeaders(httpRequest));;
            pipe = new Pipe("Source Pipe" , queueSize);
            cMsg.setPipe(pipe);
            cMsg.setProperty(Constants.PROTOCOL_NAME,Constants.DISRUPTOR,disruptor);
            disruptor.publishEvent(new CarbonEventPublisher(cMsg,srcId));
        } else {
            HTTPContentChunk chunk;
            if (cMsg != null) {
                if (msg instanceof LastHttpContent) {
                    LastHttpContent lastHttpContent = (LastHttpContent) msg;
                    HttpHeaders trailingHeaders = lastHttpContent.trailingHeaders();
                    for (String val : trailingHeaders.names()) {
                        ((Pipe) cMsg.getPipe()).
                                   addTrailingHeader(val, trailingHeaders.get(val));
                    }
                    chunk = new HTTPContentChunk(lastHttpContent);
                } else {
                    HttpContent httpContent = (HttpContent) msg;
                    chunk = new HTTPContentChunk(httpContent);
                }
                cMsg.getPipe().addContentChunk(chunk);
            }
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
    }

    public void addChannelFuture(HttpRoute route, TargetChanel targetChanel) {
        channelFutureMap.put(route.toString(), targetChanel);
    }
    public void removeChannelFuture(HttpRoute route) {
        log.debug("Removing channel future from map");
        channelFutureMap.remove(route.toString());
    }
    public TargetChanel getChannelFuture(HttpRoute route) {
        return channelFutureMap.get(route.toString());
    }

    public void setTargetHandler(TargetHandler targetHandler){
        this.targetHandler=targetHandler;
    }

    public TargetHandler getTargetHandler() {
        return targetHandler;
    }

    public Object getLock(){
        return lock;
    }
}