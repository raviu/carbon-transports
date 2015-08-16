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

package org.wso2.carbon.http.netty.listener.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.controller.POCController;
import org.wso2.carbon.disruptor.DisruptorFactory;
import org.wso2.carbon.disruptor.publisher.CarbonEventPublisher;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.Util;
import org.wso2.carbon.http.netty.sender.TargetInitializer;

import java.net.InetSocketAddress;

public class SourceHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = Logger.getLogger(SourceHandler.class);

    private Engine engine;
    private Bootstrap bootstrap;
    private Channel channel;
    private RingBuffer disruptor;

    private int count=0;


    public SourceHandler(Engine engine , Disruptor disruptor) {
        this.engine = engine;
      //  this.disruptor = disruptor;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        disruptor = DisruptorFactory.getDisruptorFromMap();
       final Channel inboundChannel =  ctx.channel();
        bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop())
                   .channel(ctx.channel().getClass())
                   .handler(new TargetInitializer(engine, ctx,disruptor));
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);
        bootstrap.option(ChannelOption.SO_SNDBUF, 1048576);
        bootstrap.option(ChannelOption.SO_RCVBUF, 1048576);
        String host = POCController.props.getProperty("proxy_to_host", "localhost");
        int port =  Integer.valueOf(POCController.props.getProperty("proxy_to_port", "8280"));
        InetSocketAddress address = new InetSocketAddress(host, port);
        ChannelFuture future = bootstrap.connect(address);
        final Channel outboundChannel = future.channel();
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    channel= outboundChannel;
                   // disruptor=DisruptorFactory.getDisruptorFromMap();
                    ctx.read();
                } else {
                    outboundChannel.close();
                }
            }
        });
    //    disruptor = DisruptorFactory.getDisruptor();
      //  disruptor.start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      CarbonMessage   cMsg = new CarbonMessageImpl(Constants.PROTOCOL_NAME);
        cMsg.setPort(((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        cMsg.setHost(((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        cMsg.setProperty(Constants.PROTOCOL_NAME, Constants.CHNL_HNDLR_CTX, ctx);
        cMsg.setProperty(Constants.PROTOCOL_NAME, Constants.SRC_HNDLR, this);
        cMsg.setProperty(Constants.PROTOCOL_NAME,Constants.ENGINE,engine);
        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            cMsg.setURI(httpRequest.getUri());
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                             Constants.HTTP_VERSION, httpRequest.getProtocolVersion().text());
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                             Constants.HTTP_METHOD, httpRequest.getMethod().name());
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                             Constants.TRANSPORT_HEADERS, Util.getHeaders(httpRequest));
            cMsg.setStatus(Constants.HEADERS);
            cMsg.setEvent(msg);
//            long   sequence =  disruptor.next();
//             CarbonDisruptorEvent carbonDisruptorEvent = (CarbonDisruptorEvent)disruptor.get(sequence);
//            carbonDisruptorEvent.setEvent(cMsg);
//            disruptor.publish(sequence);
            disruptor.publishEvent(new CarbonEventPublisher(cMsg));
        } else {
            cMsg.setEvent(msg);
            cMsg.setStatus(Constants.BODY);
           disruptor.publishEvent(new CarbonEventPublisher(cMsg));
//            long   sequence =  disruptor.next();
//            CarbonDisruptorEvent carbonDisruptorEvent = (CarbonDisruptorEvent)disruptor.get(sequence);
//            carbonDisruptorEvent.setEvent(cMsg);
//            disruptor.publish(sequence);

        }
        ctx.channel().read();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Source channel closed.");
    //   disruptor.shutdown();
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public RingBuffer getDisruptor(){
        return  disruptor;
    }
}