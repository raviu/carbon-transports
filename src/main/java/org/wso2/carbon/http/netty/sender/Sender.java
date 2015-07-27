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
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.TransportSender;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.Request;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;


public class Sender extends TransportSender {


    public Sender() {
        super(Constants.PROTOCOL_NAME);
    }

    public boolean init() {
        return true;
    }

    public boolean send(CarbonMessage msg) {
        Bootstrap bootstrap = (Bootstrap) msg.getProperty(Constants.PROTOCOL_NAME, Constants.BOOTSTRAP);

        InetSocketAddress address = new InetSocketAddress(msg.getHost(), msg.getPort());

        final HttpRequest request = createHttpRequest(msg, msg.getURI());

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


    private HttpRequest createHttpRequest(CarbonMessage msg, String uri) {

        Request incomingRequest = (Request) msg.getProperty(Constants.PROTOCOL_NAME,
                Constants.REQUEST);

        HttpMethod httpMethod = new HttpMethod(incomingRequest.getHttpMethod().name());
        HttpVersion httpVersion = new HttpVersion(incomingRequest.getHttpVersion().text(), true);
        Map headers = (Map) incomingRequest.getHttpHeaders();

        HttpRequest outgoinRequest = new DefaultHttpRequest(httpVersion, httpMethod, uri);

        if (headers != null) {
            Iterator iterator = headers.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                outgoinRequest.headers().add(key, headers.get(key));
            }
        }

        Map<String, Object> map = msg.getProperties().get(Constants.PROTOCOL_NAME);
        if (map != null) {
            for (String k: map.keySet()) {
                if (map.get(k) instanceof String)
                outgoinRequest.headers().add(k, (String) map.get(k));
            }
        }

        return outgoinRequest;
    }
}
