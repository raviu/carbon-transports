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
package org.wso2.carbon.controller;

import io.netty.channel.ChannelInitializer;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.api.TransportSender;
import org.wso2.carbon.disruptor.DisruptorFactory;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.listener.NettyListener;
import org.wso2.carbon.http.netty.listener.SourceInitializer;
import org.wso2.carbon.http.netty.sender.NettySender;


import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class POCController {

    public static Properties props = new Properties();
    private static String ID = "HTTP-netty";
    public static NettyListener.Config nettyConfig;
    public static Engine engine;


    public static void main(String[] args) throws Exception {
   //     Engine engine = null;

        if (args.length == 2) {


            File propFile = new File(args[1]);
            try {
                FileInputStream fis = new FileInputStream(propFile);
                props.load(fis);
            } catch (Exception e) {
                showUsage();
                e.printStackTrace();
                System.exit(0);
            }
            String waitstrategy = props.getProperty("wait_strategy", Constants.PHASED_BACKOFF);
            DisruptorFactory.WAITSTRATEGY waitstrategy1 = null;
            if (waitstrategy.equals(Constants.BLOCKING_WAIT)) {
                waitstrategy1 = DisruptorFactory.WAITSTRATEGY.BLOCKING_WAIT;
            } else if (waitstrategy.equals(Constants.BUSY_SPIN)) {
                waitstrategy1 = DisruptorFactory.WAITSTRATEGY.BUSY_SPIN;
            } else if (waitstrategy.equals(Constants.TIME_BLOCKING)) {
                waitstrategy1 = DisruptorFactory.WAITSTRATEGY.TIME_BLOCKING;
            } else if (waitstrategy.equals(Constants.PHASED_BACKOFF)) {
                waitstrategy1 = DisruptorFactory.WAITSTRATEGY.PHASED_BACKOFF;
            } else if (waitstrategy.equals(Constants.LITE_BLOCKING)) {
                waitstrategy1 = DisruptorFactory.WAITSTRATEGY.LITE_BLOCKING;
            }
            DisruptorFactory.createDisruptor(Integer.valueOf(props.getProperty("disruptor_buffer_Size", "1024")),
                                             Integer.valueOf(props.getProperty("no_of_disurptors", "1")),
                                             Integer.valueOf(props.getProperty("no_of_eventHandlers_per_disruptor", "1")),
                                             waitstrategy1);
             nettyConfig = new NettyListener.Config("netty-gw").setQueuSize(Integer.valueOf(props.getProperty("queue_size", "1024"))).
                       setPort(Integer.valueOf(props.getProperty("uport", "8585")));
            NettyListener nettyListener = new NettyListener(nettyConfig);
            ArrayList<InetSocketAddress> inetSocketAddresses = new ArrayList<>();
            inetSocketAddresses.add(new InetSocketAddress(props.getProperty("proxy_to_host", "localhost"),
                                                          Integer.parseInt(props.getProperty("proxy_to_port", "8280"))));
            NettySender.Config config = new NettySender.Config("netty-gw-sender").setQueueSize(Integer.parseInt(props.getProperty("queue_size","1024")))
                       .setWorkerGroup(nettyConfig.getWorkerGroup());
            config.setQueueSize(nettyConfig.getQueueSize());
            TransportSender sender = new NettySender(config);
            if (args[0].equals("jaxrs")) {
                engine = new POCJaxRSEngine(sender);
                nettyListener.setDefaultInitializer(new SourceInitializer(engine, config.getQueueSize()));

                nettyListener.start();

            } else if ((args[0].equals("camel"))) {
                CamelContext context = new DefaultCamelContext();
                context.disableJMX();

                context.addRoutes(new RouteBuilder() {
                    public void configure() {

                        from("wso2-gw:http://204.13.85.2:9090/service")
                                   .choice()
                                   .when(header("routeId").regex("r1"))
                                   .to("wso2-gw:http://204.13.85.5:5050/services/echo")
                                   .when(header("routeId").regex("r2"))
                                   .to("wso2-gw:http://204.13.85.5:6060/services/echo")
                                   .otherwise()
                                   .to("wso2-gw:http://204.13.85.5:7070/services/echo");

                        from("wso2-gw:http://localhost:8585/service")
                                   .choice()
                                   .when(header("routeId").regex("r1"))
                                   .to("wso2-gw:http://localhost:8280/services/echo")
                                   .when(header("routeId").regex("r2"))
                                   .to("wso2-gw:http://localhost:6060/services/echo")
                                   .otherwise()
                                   .to("wso2-gw:http://localhost:8280/services/echo");

                    }
                });
                context.start();
            } else {
                engine = new DefaultMediationEngine(sender);
                nettyListener.setDefaultInitializer(new SourceInitializer(engine, config.getQueueSize()));

                nettyListener.start();

            }


            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } else {
            showUsage();
        }
    }

    private static void showUsage() {
        System.out.println("\n\n");
        System.out.println("Usage: java -jar server.jar <default |  " +
                           "jaxrs> /path/to/properties.prop");
        System.out.println("\n");
    }

    public Engine startPOCController() {
        NettySender.Config config = new NettySender.Config("netty-gw-sender").setQueueSize(Integer.parseInt(props.getProperty("queue_size","1024")))
                   .setWorkerGroup(nettyConfig.getWorkerGroup());
        NettySender sender = new NettySender(config);
       engine = new CamelMediationEngine(sender);

        Map<String, ChannelInitializer> channelInitializers = new HashMap<String, ChannelInitializer>();
        channelInitializers.put("SourceInitializer", new SourceInitializer(engine,config.getQueueSize()));
        NettyListener nettyListener = new NettyListener(nettyConfig);
        nettyListener.setDefaultInitializer(new SourceInitializer(engine,nettyConfig.getQueueSize()));
        nettyListener.start();

        return engine;
    }

}
