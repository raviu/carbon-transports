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
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.Pipe;
import org.wso2.carbon.http.netty.common.Request;
import org.wso2.carbon.http.netty.sender.TargetHandler;
import org.wso2.carbon.http.netty.sender.TargetInitializer;


import java.util.ArrayList;
import java.util.List;

public class SourceHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = Logger.getLogger(SourceHandler.class);

    private Bootstrap bootstrap;
    private ChannelFuture channelFuture;
    private Channel channel;
    private TargetHandler outboundHandler;
    private Engine engine;

    private List<Request> requestList = new ArrayList<Request>();

    public SourceHandler(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        outboundHandler = new TargetHandler(engine, ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Request sourceRequest = null;
        if (msg instanceof HttpRequest) {
            sourceRequest = new Request();
            HttpRequest defaultHttpRequest = (HttpRequest) msg;
            HttpHeaders headers = defaultHttpRequest.headers();
            for (String val : headers.names()) {
                sourceRequest.addHttpheaders(val, headers.get(val));
            }
            sourceRequest.setTo(defaultHttpRequest.getUri());
            sourceRequest.setHttpMethod(defaultHttpRequest.getMethod());
            sourceRequest.setHttpVersion(defaultHttpRequest.getProtocolVersion());
            sourceRequest.setUri(defaultHttpRequest.getUri());
            sourceRequest.setInboundChannelHandlerContext(ctx);
            sourceRequest.setBootstrap(bootstrap);
            sourceRequest.setPipe(new Pipe(Constants.SOURCE_PIPE));
            requestList.add(sourceRequest);
            SourceWorkerPool.submitJob(new SourceWorker(engine, sourceRequest, ctx));
        } else if (msg instanceof HttpContent) {
            if (requestList.get(0) != null) {
                if (msg instanceof LastHttpContent) {
                    LastHttpContent defaultLastHttpContent = (LastHttpContent) msg;
                    HttpHeaders trailingHeaders = defaultLastHttpContent.trailingHeaders();
                    for (String val : trailingHeaders.names()) {
                        requestList.get(0).getPipe().addTrailingHeader(val, trailingHeaders.get(val));
                    }
                    requestList.get(0).getPipe().addContent(defaultLastHttpContent);
                    requestList.remove(0);
                } else {
                    HttpContent defaultHttpContent = (HttpContent) msg;
                    requestList.get(0).getPipe().addContent(defaultHttpContent);
                }
            } else {
                log.error("Cannot correlate source request with content");
            }
        }


    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
