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
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.HTTPContentChunk;
import org.wso2.carbon.http.netty.common.Pipe;
import org.wso2.carbon.http.netty.common.Util;
import org.wso2.carbon.http.netty.common.Worker;
import org.wso2.carbon.http.netty.common.WorkerPool;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class SourceHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = Logger.getLogger(SourceHandler.class);

    private Engine engine;
    private CarbonMessage cMsg;
    private List<CarbonMessage> requestList = new ArrayList<CarbonMessage>();


    public SourceHandler(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            cMsg = new CarbonMessageImpl(Constants.PROTOCOL_NAME);
            cMsg.setPort(((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
            cMsg.setHost(((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
            cMsg.setURI(httpRequest.getUri());
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                    Constants.HTTP_VERSION, httpRequest.getProtocolVersion().text());
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                    Constants.HTTP_METHOD, httpRequest.getMethod().name());
            cMsg.setProperty(Constants.PROTOCOL_NAME,
                    Constants.TRANSPORT_HEADERS, Util.getHeaders(httpRequest));
            cMsg.setProperty(Constants.PROTOCOL_NAME, Constants.CHNL_HNDLR_CTX, ctx);

            cMsg.setPipe(new Pipe(Constants.SOURCE_PIPE));
            requestList.add(cMsg);
            WorkerPool.submitJob(new Worker(engine, cMsg));
        } else if (msg instanceof HttpContent) {
            HTTPContentChunk chunk;
            if (cMsg != null) {
                if (msg instanceof LastHttpContent) {
                    LastHttpContent lastHttpContent = (LastHttpContent) msg;
                    HttpHeaders trailingHeaders = lastHttpContent.trailingHeaders();
                    for (String val : trailingHeaders.names()) {
                        ((Pipe) cMsg.getPipe()).
                                addTrailingHeader(val, trailingHeaders.get(val));
                    }
                    chunk =  new HTTPContentChunk(lastHttpContent);
                    cMsg.getPipe().addContentChunk(chunk);
                } else {
                    HttpContent httpContent = (HttpContent) msg;
                    chunk = new HTTPContentChunk(httpContent);
                    cMsg.getPipe().addContentChunk(chunk);
                }
            }
        }
    }
}
