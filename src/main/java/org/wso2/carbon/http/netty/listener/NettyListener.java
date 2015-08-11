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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import org.wso2.carbon.controller.POCController;
import org.wso2.carbon.http.netty.internal.NettyTransportDataHolder;
import org.wso2.carbon.CarbonTransport;

import java.util.List;
import java.util.Map;

public class NettyListener extends CarbonTransport {
    private static Logger log = Logger.getLogger(NettyListener.class);

    private static String id = "HTTP-netty";
    private int port;
    private Thread listenerThread;

    private EventLoopGroup bossGroup =
            new NioEventLoopGroup(Integer.valueOf(POCController.props.getProperty(
                    "netty_boss", String.valueOf(Runtime.getRuntime().availableProcessors()))));
    private EventLoopGroup workerGroup =
            new NioEventLoopGroup(Integer.valueOf(POCController.props.getProperty(
                    "netty_worker", String.valueOf(Runtime.getRuntime().availableProcessors() * 2))));

    public NettyListener(String id, int port) {
        super(id);
        this.port = port;
    }

    public void start(final Map<String, ChannelInitializer> defaultInitializers) {
        log.info("### Netty Boss Count: " + Integer.valueOf(POCController.props.getProperty(
                "netty_boss", String.valueOf(Runtime.getRuntime().availableProcessors()))));
        log.info("### Netty Worker Count: " + Integer.valueOf(POCController.props.getProperty(
                "netty_worker", String.valueOf(Runtime.getRuntime().availableProcessors()))));

        listenerThread = new Thread(new Runnable() {
            public void run() {

                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.option(ChannelOption.SO_BACKLOG, 100);
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class);
                    addChannelInitializers(b, defaultInitializers);
                    b.childOption(ChannelOption.TCP_NODELAY, true);
                    b.option(ChannelOption.SO_KEEPALIVE, true);
                    b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);

                    b.option(ChannelOption.SO_SNDBUF, 1048576);
                    b.option(ChannelOption.SO_RCVBUF, 1048576);
                    b.childOption(ChannelOption.SO_RCVBUF, 1048576);
                    b.childOption(ChannelOption.SO_SNDBUF, 1048576);
                    b.childOption(ChannelOption.AUTO_READ,false);
                    Channel ch = null;
                    try {
                        ch = b.bind(port).sync().channel();
                        ch.closeFuture().sync();
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                } finally {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            }
        },
                "Inbound Listener"
        );
        listenerThread.start();
        log.info("Listener started on port " + port);
	    super.start();
    }

    private void addChannelInitializers(ServerBootstrap bootstrap,
                                        Map<String, ChannelInitializer> defaultInitializers) {
        List<ChannelInitializer> channelInitializers
                = NettyTransportDataHolder.getInstance().getChannelInitializers(id);
        if (!channelInitializers.isEmpty()) {
            for (ChannelInitializer channelInitializer : channelInitializers) {
                bootstrap.childHandler(channelInitializer);
            }
        } else {
            for (Map.Entry<String, ChannelInitializer> channelInitializer :
                    defaultInitializers.entrySet()) {
                bootstrap.childHandler(channelInitializer.getValue());
            }
        }
    }

    @Override
    public void stop() {
        //TODO: implement
    }

    @Override
    public void beginMaintenance() {
        //TODO: implement
    }

    @Override
    public void endMaintenance() {
        //TODO: implement
    }

}
