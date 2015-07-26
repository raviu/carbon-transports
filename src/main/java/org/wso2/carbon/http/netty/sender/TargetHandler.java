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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.apache.log4j.Logger;
import org.wso2.carbon.http.netty.common.Pipe;
import org.wso2.carbon.http.netty.common.Response;

import java.util.ArrayList;
import java.util.List;

public class TargetHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = Logger.getLogger(TargetHandler.class);
    private Channel inboundChannel;

    private List<Response> responseList = new ArrayList<Response>();

    int count = 0;

    public TargetHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {
            Response response = new Response();
            HttpResponse defaultHttpResponse = (HttpResponse) msg;
            HttpHeaders headers = defaultHttpResponse.headers();
            for (String val : headers.names()) {
                response.addHttpheaders(val, headers.get(val));
            }
            response.setStatus(defaultHttpResponse.getStatus());
            response.setStatusLine(defaultHttpResponse.getStatus().toString());
            response.setPipe(new Pipe("TargetPipe"));
            responseList.add(response);
//            sourceConfiguration.getWorkerPool().execute(new ResponseWorker(messageContext, response, sourceConfiguration));
        } else if (msg instanceof HttpContent) {
            if (responseList.get(0) != null) {
                if (msg instanceof LastHttpContent) {
                    LastHttpContent defaultLastHttpContent = (LastHttpContent) msg;
                    responseList.get(0).getPipe().addContent(defaultLastHttpContent);
                    responseList.remove(0);
                } else {
                    DefaultHttpContent defaultHttpContent = (DefaultHttpContent) msg;
                    //  responseList.get(0).getPipe().writeContent(defaultHttpContent);
                    responseList.get(0).getPipe().addContent(defaultHttpContent);
                }

            }
        }
        //    sourceConfiguration.getWorkerPool().execute(new ResponseWorker(messageContext,response,sourceConfiguration));
    }


}
