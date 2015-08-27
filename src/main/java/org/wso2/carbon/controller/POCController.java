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

import org.wso2.carbon.api.Engine;
import org.wso2.carbon.api.TransportSender;
import org.wso2.carbon.disruptor.DisruptorFactory;
import org.wso2.carbon.http.netty.listener.NettyListener;
import org.wso2.carbon.http.netty.listener.SourceInitializer;
import org.wso2.carbon.http.netty.sender.NettySender;



import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Properties;

public class POCController {

    public static Properties props = new Properties();
    private static String ID = "HTTP-netty";

    public static void main(String[] args) {




        if (args.length == 2) {
//            if (args[0].equals("jaxrs")) {
//                engine = new POCJaxRSEngine(sender);
//            }

            File propFile = new File(args[1]);
            try {
                FileInputStream fis = new FileInputStream(propFile);
                props.load(fis);
            } catch (Exception e) {
                showUsage();
                e.printStackTrace();
                System.exit(0);
            }
            DisruptorFactory.createDisruptor(Integer.valueOf(props.getProperty("disruptorbufferSize", "1024")),
                                             Integer.valueOf(props.getProperty("noOfDisurptors", "1")),Integer.valueOf(props.getProperty("noOfEventHandlersPerDisruptor", "2")));
            NettyListener.Config nettyConfig = new NettyListener.Config("netty-gw").setPort(Integer.valueOf(props.getProperty("uport", "8585")));
            NettyListener nettyListener = new NettyListener(nettyConfig);
            ArrayList<InetSocketAddress> inetSocketAddresses = new ArrayList<>();
            inetSocketAddresses.add(new InetSocketAddress(props.getProperty("proxy_to_host", "localhost"), Integer.parseInt(props.getProperty("proxy_to_port", "8280"))));
            NettySender.Config config = new NettySender.Config("netty-gw-sender").setConcurrency(Integer.valueOf(props.getProperty("concurrency", "2500"))).setWorkerGroup(nettyConfig.getWorkerGroup());
            config.setEndpointList(inetSocketAddresses);
//            try {
//                //OutboundChannelFactory.OutboundChannelFactory(nettyConfig.getWorkerGroup(), Integer.valueOf(props.getProperty("concurrency", "2500")), inetSocketAddresses, config);
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
        TransportSender sender = new NettySender(config);
        Engine engine = new POCMediationEngine(sender);
            nettyListener.setDefaultInitializer(new SourceInitializer(engine, config));

            nettyListener.start();

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

}
