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


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.Engine;

/**
 * A class that responsible for create server side channels.
 */
public class SourceInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger log = Logger.getLogger(SourceInitializer.class);

    private Engine engine;
    private int noOfChannels;
    private int queueSize;
    private Object lock = new Object();

    public SourceInitializer(Engine engine , int queueSize) {
        this.engine = engine;
        this.queueSize = queueSize;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        if (log.isDebugEnabled()) {
            log.info("Initializing source channel pipeline");
        }
     //   Disruptor disruptor = DisruptorFactory.getDisruptorFromMap();
        ChannelPipeline p = ch.pipeline();
        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("encoder", new HttpResponseEncoder());

    //    p.addLast("handler", new SourceHandler(engine,null));

        //TODO Test adding event executor group as below
//        p.addLast(new DefaultEventExecutorGroup(10), "handler", new SourceHandler(engine));
        synchronized (lock){
            noOfChannels++;
            p.addLast("handler", new SourceHandler(engine,noOfChannels ,queueSize));
        }


    }

}
