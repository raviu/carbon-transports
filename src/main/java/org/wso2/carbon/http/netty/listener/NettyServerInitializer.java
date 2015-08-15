/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.http.netty.listener;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.http.netty.internal.NettyTransportDataHolder;

/**
 * TODO: class level comment
 */
public class NettyServerInitializer  extends ChannelInitializer<SocketChannel> {
    private static final Log log = LogFactory.getLog(NettyServerInitializer.class);
    private String transportID;

    public NettyServerInitializer(String transportID) {
        this.transportID = transportID;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //TODO: Add the generic handlers to the pipeline
        // e.g. SSL handler

        // Add the rest of the handlers to the pipeline
        CarbonNettyServerInitializer initializer = NettyTransportDataHolder.getInstance().getChannelInitializer(transportID);
        if (initializer != null) {
            if (log.isDebugEnabled()) {
                log.debug("Calling CarbonNettyServerInitializer OSGi service " + initializer);
            }
            initializer.initChannel(socketChannel);
        }
    }
}
