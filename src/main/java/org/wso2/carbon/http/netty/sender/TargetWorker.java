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
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.Response;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class TargetWorker implements Runnable {
    private static Logger log = Logger.getLogger(TargetWorker.class);

    private Response targetResponse;
    private ChannelHandlerContext channelHandlerContext;

    public TargetWorker(ChannelHandlerContext channelHandlerContext,
                        Response targetResponse) {
        this.targetResponse = targetResponse;
        this.channelHandlerContext = channelHandlerContext;
    }

    public void run() {
        writeResponse(channelHandlerContext);
    }

    public ChannelHandlerContext getInboundChannelHandlerContext() {
        return channelHandlerContext;
    }

    private void writeResponse(ChannelHandlerContext channelHandlerContext) {
        ByteBuf content = Unpooled.unreleasableBuffer(Unpooled.buffer());

        DefaultHttpResponse defaultHttpResponse = new DefaultHttpResponse(HTTP_1_1, OK);

        if (targetResponse.getHttpheaders() != null) {
            for (String k : targetResponse.getHttpheaders().keySet()) {
                defaultHttpResponse.headers().add(k, targetResponse.getHeader(k));
            }
        }
        channelHandlerContext.write(defaultHttpResponse);
        while (true) {
            HttpContent httpContent = targetResponse.getPipe().getContent();
            if (httpContent instanceof LastHttpContent || httpContent instanceof DefaultLastHttpContent) {
                //    trailingHeadrs = pipe.getTrailingheaderMap();
                channelHandlerContext.writeAndFlush(httpContent);
                break;
            }
            channelHandlerContext.write(httpContent);
        }

    }

}