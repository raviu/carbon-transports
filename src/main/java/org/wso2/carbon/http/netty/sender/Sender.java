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
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
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
import org.wso2.carbon.http.netty.listener.SourceHandler;

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
        final ChannelHandlerContext inboundCtx = (ChannelHandlerContext)
                msg.getProperty(Constants.PROTOCOL_NAME, Constants.CHNL_HNDLR_CTX);

        final HttpRequest httpRequest = createHttpRequest(msg);
        final Pipe pipe = msg.getPipe();

        final SourceHandler srcHandler = (SourceHandler) msg.getProperty(
                Constants.PROTOCOL_NAME, Constants.SRC_HNDLR);

        Bootstrap bootstrap = srcHandler.getBootstrap();

        InetSocketAddress address = new InetSocketAddress(msg.getHost(), msg.getPort());

        if (srcHandler.getChannelFuture() == null) {
            ChannelFuture future = bootstrap.connect(address);
            final Channel outboundChannel = future.channel();

            future.addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        outboundChannel.write(httpRequest);
                        while (true) {
                            HTTPContentChunk chunk = (HTTPContentChunk) pipe.getContent();
                            HttpContent httpContent = chunk.getHttpContent();
                            if (httpContent instanceof LastHttpContent) {
                                outboundChannel.writeAndFlush(httpContent);
                                break;
                            }
                            if (httpContent != null) {
                                outboundChannel.write(httpContent);
                            }
                        }
                        srcHandler.setChannelFuture(future);
                        srcHandler.setChannel(outboundChannel);
                    } else {
                        // Close the connection if the connection attempt has failed.
                        outboundChannel.close();
                    }
                }
            });

        } else {
            ChannelFuture future = srcHandler.getChannelFuture();
            if (future.isSuccess() && srcHandler.getChannel().isActive()) {
                srcHandler.getChannel().write(httpRequest);
                while (true) {
                    HTTPContentChunk chunk = (HTTPContentChunk) pipe.getContent();
                    HttpContent httpContent = chunk.getHttpContent();
                    if (httpContent instanceof LastHttpContent) {
                        srcHandler.getChannel().writeAndFlush(httpContent);
                        break;
                    }
                    srcHandler.getChannel().write(httpContent);
                }

            } else {
                final ChannelFuture futuretwo = bootstrap.connect(address);
                final Channel outboundChannel = futuretwo.channel();
                futuretwo.addListener(new ChannelFutureListener() {

                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (futuretwo.isSuccess()) {
                            outboundChannel.write(httpRequest);
                            while (true) {
                                HTTPContentChunk chunk = (HTTPContentChunk) pipe.getContent();
                                HttpContent httpContent = chunk.getHttpContent();
                                if (httpContent instanceof LastHttpContent) {
                                    outboundChannel.writeAndFlush(httpContent);
                                    break;
                                }
                                outboundChannel.write(httpContent);
                            }
                            srcHandler.setChannelFuture(future);

                        } else {
                            // Close the connection if the connection attempt has failed.
                            outboundChannel.close();
                        }
                    }
                });
            }
        }

        return false;
    }

    public boolean sendBack(CarbonMessage msg) {
        final ChannelHandlerContext inboundChCtx = (ChannelHandlerContext)
                msg.getProperty(Constants.PROTOCOL_NAME, Constants.CHNL_HNDLR_CTX);
        final Pipe pipe = msg.getPipe();
        final HttpResponse response = createHttpResponse(msg);

        inboundChCtx.write(response);
        while (true) {
            HTTPContentChunk chunk = (HTTPContentChunk) pipe.getContent();
            HttpContent httpContent = chunk.getHttpContent();
            if (httpContent != null) {
                if (httpContent instanceof LastHttpContent ||
                        httpContent instanceof DefaultLastHttpContent) {
                    inboundChCtx.writeAndFlush(httpContent);
                    break;
                }
                inboundChCtx.write(httpContent);
            }
        }
        return false;
    }

    private HttpRequest createHttpRequest(CarbonMessage msg) {

        HttpMethod httpMethod = new HttpMethod((String) msg.getProperty(Constants.PROTOCOL_NAME,
                Constants.HTTP_METHOD));

        HttpVersion httpVersion = new HttpVersion((String) msg.getProperty(Constants.PROTOCOL_NAME,
                Constants.HTTP_VERSION), true);

        HttpRequest outgoingRequest =
                new DefaultHttpRequest(httpVersion, httpMethod, msg.getURI(), false);

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
                httpResponseStatus, false);

        Map<String, String> headerMap = (Map<String, String>) msg.getProperty(
                Constants.PROTOCOL_NAME, Constants.TRANSPORT_HEADERS);

        Util.setHeaders(outgoingResponse, headerMap);

        return outgoingResponse;
    }

}
