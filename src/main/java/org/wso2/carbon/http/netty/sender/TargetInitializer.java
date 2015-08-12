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

import com.lmax.disruptor.dsl.Disruptor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.disruptor.DisruptorFactory;
import org.wso2.carbon.http.netty.sender.disruptor.TargetHandler;

public class TargetInitializer extends ChannelInitializer<SocketChannel> {
    private static Logger log = Logger.getLogger(TargetInitializer.class);

    protected static final String HANDLER = "handler";

    private Engine engine;
    private volatile ChannelHandlerContext ctx;

    public TargetInitializer(Engine engine, ChannelHandlerContext ctx) {
        this.engine = engine;
        this.ctx = ctx;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
     //  Disruptor disruptor = DisruptorFactory.getDisruptorFromMap();
        ChannelPipeline p = ch.pipeline();
        p.addLast("decoder", new HttpResponseDecoder());
        p.addLast("encoder", new HttpRequestEncoder());
//        p.addLast("aggegator", new HttpObjectAggregator(512 * 1024));
        p.addLast(HANDLER, new TargetHandler(engine, ctx, null ));
    }
}
