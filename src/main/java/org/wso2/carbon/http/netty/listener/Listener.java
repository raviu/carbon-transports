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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.http.netty.internal.NettyTransportDataHolder;

import java.util.List;
import java.util.Map;

public class Listener {
    private static Logger log = Logger.getLogger(Listener.class);

    private static String ID = "HTTP-netty";
    private int port;
    private Thread listenerThread;

    private Engine engine;

    private EventLoopGroup bossGroup =
            new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
    private EventLoopGroup workerGroup =
            new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

    public Listener(int port, Engine engine) {
        this.port = port;
        this.engine = engine;
    }

    public void start(final Map<String, ChannelInitializer> defaultInitializers) {
        listenerThread = new Thread(new Runnable() {
            public void run() {

                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.option(ChannelOption.SO_BACKLOG, 10);
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
    }

    private void addChannelInitializers(ServerBootstrap b,
                                             Map<String, ChannelInitializer> defaultInitializers) {
        List<ChannelInitializer> channelInitializers
                = NettyTransportDataHolder.getInstance().getChannelInitializer();
        if (!channelInitializers.isEmpty()) {
            for (ChannelInitializer i : channelInitializers) {
                b.childHandler(i);
            }
        } else {
            for (Map.Entry<String, ChannelInitializer> e : defaultInitializers.entrySet()) {
                b.childHandler(e.getValue());
            }
        }
    }

    public void stop() {
    }

    public void beginMaintenance() {
    }

    public void endMaintenance() {
    }

}
