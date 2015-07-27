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
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.wso2.carbon.api.TransportSenderAPI;
import org.wso2.carbon.context.CommonContext;
import org.wso2.carbon.http.netty.common.Constants;

import java.net.InetSocketAddress;


public class Sender implements TransportSenderAPI {


    public Sender() {

    }

    public boolean init() {
        return true;
    }

    public boolean send(CommonContext ctx) {
        Bootstrap bootstrap = (Bootstrap) ctx.getProperty(Constants.BOOTSTRAP);

        String host = (String) ctx.getProperty(Constants.TO_HOST);
        int port = (Integer) ctx.getProperty(Constants.TO_PORT);

        InetSocketAddress address = new InetSocketAddress(host, port);

        final HttpRequest request = (HttpRequest) ctx.getProperty(Constants.OUTGOING_REQUEST);

        ChannelFuture f = bootstrap.connect(address);
        final Channel ch = f.channel();
        f.addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ch.writeAndFlush(request);
                } else {
                    // Close the connection if the connection attempt has failed.
                    ch.close();
                }
            }
        });

        return false;
    }
}
