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
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.Logger;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.transports.CarbonTransport;

import java.io.File;

public class NettyListener extends CarbonTransport {
    private static Logger log = Logger.getLogger(NettyListener.class);

    private String SERVER_STATE = Constants.STATE_STOPPED;

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private static ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private ChannelInitializer defaultInitializer;
    private Config nettyConfig;

    public NettyListener(Config nettyConfig) {
        super(nettyConfig.getId());
        this.nettyConfig = nettyConfig;
        bossGroup = new NioEventLoopGroup(nettyConfig.getBossThreads());
        workerGroup = new NioEventLoopGroup(nettyConfig.getWorkerThreads());

        //TODO: setup SSL
    }

    public void setDefaultInitializer(ChannelInitializer defaultInitializer) {
        this.defaultInitializer = defaultInitializer;
    }

    public void start() {
        /*log.info("### Netty Boss Count: " + Integer.valueOf(POCController.props.getProperty(
                "netty_boss", String.valueOf(Runtime.getRuntime().availableProcessors()))));
        log.info("### Netty Worker Count: " + Integer.valueOf(POCController.props.getProperty(
                "netty_worker", String.valueOf(Runtime.getRuntime().availableProcessors()))));*/

        startTransport();
    }

    private void startTransport() {
        try {
            bootstrap = new ServerBootstrap();
            bootstrap.option(ChannelOption.SO_BACKLOG, 100);
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class);
            addChannelInitializer();
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);

            bootstrap.option(ChannelOption.SO_SNDBUF, 1048576);
            bootstrap.option(ChannelOption.SO_RCVBUF, 1048576);
            bootstrap.childOption(ChannelOption.SO_RCVBUF, 1048576);
            bootstrap.childOption(ChannelOption.SO_SNDBUF, 1048576);
            Channel ch = null;
            try {
                ch = bootstrap.bind(nettyConfig.getPort()).sync().channel();
                allChannels.add(ch);
                SERVER_STATE = Constants.STATE_STARTED;
                log.info("Netty Listener starting on port " + nettyConfig.getPort());
                ch.closeFuture().sync();
                allChannels.close().awaitUninterruptibly();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void addChannelInitializer() {
        if (defaultInitializer != null) {
            bootstrap.childHandler(defaultInitializer);
        } else {
            bootstrap.childHandler(new NettyServerInitializer(id));
        }
    }

    @Override
    public void stop() {
        SERVER_STATE = Constants.STATE_TRANSITION;
        log.info("Stopping Netty transport " + id + " on port " + nettyConfig.getPort());
        shutdownEventLoops();
    }

    @Override
    public void beginMaintenance() {
        SERVER_STATE = Constants.STATE_TRANSITION;
        log.info("Putting Netty transport " + id + " on port " + nettyConfig.getPort() + " into maintenance mode");
        shutdownEventLoops();
    }

    @Override
    public void endMaintenance() {
        SERVER_STATE = Constants.STATE_TRANSITION;
        log.info("Ending maintenance mode for Netty transport " + id + " running on port " + nettyConfig.getPort());
        bossGroup = new NioEventLoopGroup(nettyConfig.getBossThreads());
        workerGroup = new NioEventLoopGroup(nettyConfig.getWorkerThreads());
        startTransport();
    }

    public String getState() {
        return SERVER_STATE;
    }

    private void shutdownEventLoops() {
        Future<?> f = workerGroup.shutdownGracefully();
        f.addListener(new GenericFutureListener<Future<Object>>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                Future f = bossGroup.shutdownGracefully();
                f.addListener(new GenericFutureListener<Future<Object>>() {
                    @Override
                    public void operationComplete(Future<Object> future) throws Exception {
                        log.info("Netty transport " + id + " on port " + nettyConfig.getPort() + " stopped successfully");
                        SERVER_STATE = Constants.STATE_STOPPED;
                    }
                });
            }
        });
    }

    public static ChannelGroup getListenerChannelGroup() {
        return allChannels;
    }

    public static class Config {

        private String id;
        private String host = "127.0.0.1";
        private int port = 8080;
        private int bossThreads = Runtime.getRuntime().availableProcessors();
        private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
        private int execThreads = 50;
        private SslConfig sslConfig;

        public Config(String id) {
            if(id == null) {
                throw new IllegalArgumentException("Netty transport ID is null");
            }
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public int getBossThreads() {
            return bossThreads;
        }

        public Config setBossThreads(int bossThreads) {
            this.bossThreads = bossThreads;
            return this;
        }

        public int getExecThreads() {
            return execThreads;
        }

        public Config setExecThreads(int execThreads) {
            this.execThreads = execThreads;
            return this;
        }

        public String getHost() {
            return host;
        }

        public Config setHost(String host) {
            this.host = host;
            return this;
        }

        public int getPort() {
            return port;
        }

        public Config setPort(int port) {
            this.port = port;
            return this;
        }

        public int getWorkerThreads() {
            return workerThreads;
        }

        public Config setWorkerThreads(int workerThreads) {
            this.workerThreads = workerThreads;
            return this;
        }

        public Config enableSsl(SslConfig sslConfig) {
            this.sslConfig = sslConfig;
            return this;
        }

        public SslConfig getSslConfig() {
            return sslConfig;
        }

        public static class SslConfig {
            private File keyStore;
            private String keyStorePass;
            private String certPass;
            private File trustStore;
            private String trustStorePass;

            public SslConfig(File keyStore, String keyStorePass) {
                this.keyStore = keyStore;
                this.keyStorePass = keyStorePass;
            }

            public String getCertPass() {
                return certPass;
            }

            public SslConfig setCertPass(String certPass) {
                this.certPass = certPass;
                return this;
            }

            public File getTrustStore() {
                return trustStore;
            }

            public SslConfig setTrustStore(File trustStore) {
                this.trustStore = trustStore;
                return this;
            }

            public String getTrustStorePass() {
                return trustStorePass;
            }

            public SslConfig setTrustStorePass(String trustStorePass) {
                this.trustStorePass = trustStorePass;
                return this;
            }

            public File getKeyStore() {
                return keyStore;
            }

            public String getKeyStorePass() {
                return keyStorePass;
            }
        }
    }

}
