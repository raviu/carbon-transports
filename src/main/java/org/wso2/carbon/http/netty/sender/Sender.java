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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.Pipe;
import org.wso2.carbon.api.TransportSender;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.HTTPContentChunk;
import org.wso2.carbon.http.netty.common.Util;

import java.net.InetSocketAddress;
import java.util.Map;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class Sender extends TransportSender {
    private static Logger log = Logger.getLogger(Sender.class);

    public Sender() {
        super(Constants.PROTOCOL_NAME);
    }

    public boolean init() {
        return true;
    }

    public boolean send(CarbonMessage msg) {
        Bootstrap bootstrap = getBootstrap(msg);

        InetSocketAddress address = new InetSocketAddress(msg.getHost(), msg.getPort());

        final HttpRequest httpRequest = createHttpRequest(msg);
        final Pipe pipe = msg.getPipe();

        ChannelFuture f = bootstrap.connect(address);
        final Channel ch = f.channel();
        f.addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ch.write(httpRequest);
                    while (true) {
                        HTTPContentChunk chunk = (HTTPContentChunk) pipe.getContent();
                        HttpContent content = chunk.getHttpContent();
                        if (content instanceof LastHttpContent) {
                            ch.writeAndFlush(content);
                            break;
                        }
                        if (content != null) {
                            ch.write(content);
                        }
                    }
                } else {
                    // Close the connection if the connection attempt has failed.
                    ch.close();
                }
            }
        });

        return false;
    }

    public boolean sendBack(CarbonMessage msg) {
        ChannelHandlerContext inboundCtx = (ChannelHandlerContext)
                msg.getProperty(Constants.PROTOCOL_NAME, Constants.CHNL_HNDLR_CTX);
        Pipe pipe = msg.getPipe();
        HttpResponse response = createHttpResponse(msg);
        inboundCtx.write(response);
        while (true) {
            HTTPContentChunk chunk = (HTTPContentChunk) pipe.getContent();
            HttpContent content = chunk.getHttpContent();
            if (content instanceof LastHttpContent) {
                inboundCtx.writeAndFlush(content);
                break;
            }
            inboundCtx.write(content);
        }
        return false;
    }

    private Bootstrap getBootstrap(CarbonMessage msg) {
        ChannelHandlerContext ctx = (ChannelHandlerContext)
                msg.getProperty(Constants.PROTOCOL_NAME, Constants.CHNL_HNDLR_CTX);

        final Channel inboundChannel = ctx.channel();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new TargetInitializer(getEngine(), ctx));
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);;
        bootstrap.option(ChannelOption.SO_SNDBUF, 1048576);
        bootstrap.option(ChannelOption.SO_RCVBUF, 1048576);

        return bootstrap;
    }

    private HttpRequest createHttpRequest(CarbonMessage msg) {

        HttpMethod httpMethod = new HttpMethod((String) msg.getProperty(Constants.PROTOCOL_NAME,
                Constants.HTTP_METHOD));

        HttpVersion httpVersion = new HttpVersion((String) msg.getProperty(Constants.PROTOCOL_NAME,
                Constants.HTTP_VERSION), true);

        HttpRequest outgoingRequest = new DefaultHttpRequest(httpVersion, httpMethod, msg.getURI());

        Map headers = (Map) msg.getProperty(Constants.PROTOCOL_NAME,
                Constants.TRANSPORT_HEADERS);

        Util.setHeaders(outgoingRequest, headers);

        return outgoingRequest;
    }

    private HttpResponse createHttpResponse(CarbonMessage msg) {
        HttpVersion httpVersion = new HttpVersion(Util.getStringValue(msg,
                Constants.HTTP_VERSION, HTTP_1_1.text()), true);

        int statusCode = (Integer) Util.getIntValue(msg, Constants.HTTP_STATUS_CODE, 200);

        HttpResponseStatus httpResponseStatus = new HttpResponseStatus(statusCode,
                HttpResponseStatus.valueOf(statusCode).reasonPhrase());

        DefaultHttpResponse outgoingResponse = new DefaultHttpResponse(httpVersion,
                httpResponseStatus, true);

        Map<String, String> headerMap = (Map<String, String>) msg.getProperty(
                Constants.PROTOCOL_NAME, Constants.TRANSPORT_HEADERS);

        Util.setHeaders(outgoingResponse, headerMap);

        return outgoingResponse;
    }

}
