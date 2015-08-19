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
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.http.netty.listener.NettyListener;
import org.wso2.carbon.http.netty.listener.SourceInitializer;
import org.wso2.carbon.http.netty.sender.Sender;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class POCController {

    public static Properties props = new Properties();
    private static String ID = "HTTP-netty";

    public static void main(String[] args) {
        Sender sender = new Sender();
        Engine engine = new POCMediationEngine(sender);

        if (args.length == 2) {
            if (args[0].equals("jaxrs")) {
                engine = new POCJaxRSEngine(sender);
            } else if (args[0].equals("camel")) {
                engine = new CamelMediationEngine(sender);
                engine.init();
            }

            File propFile = new File(args[1]);
            try {
                FileInputStream fis = new FileInputStream(propFile);
                props.load(fis);
            } catch (Exception e) {
                showUsage();
                e.printStackTrace();
                System.exit(0);
            }

            Map<String, ChannelInitializer> channelInitializers = new HashMap<String, ChannelInitializer>();
            channelInitializers.put("SourceInitializer", new SourceInitializer(engine));

            NettyListener nettyListener =
                    new NettyListener(ID, Integer.valueOf(props.getProperty("port", "9090")));
            nettyListener.start(channelInitializers);
        } else {
            showUsage();
        }
    }

    public Engine startPOCController(){
        Sender sender = new Sender();
        Engine engine = new CamelMediationEngine(sender);

            File propFile = new File("sample.properties");
            try {
                FileInputStream fis = new FileInputStream(propFile);
                props.load(fis);
            } catch (Exception e) {
                showUsage();
                e.printStackTrace();
                System.exit(0);
            }

            Map<String, ChannelInitializer> channelInitializers = new HashMap<String, ChannelInitializer>();
            channelInitializers.put("SourceInitializer", new SourceInitializer(engine));

            NettyListener nettyListener =
                    new NettyListener(ID, Integer.valueOf(props.getProperty("port", "9090")));
            nettyListener.start(channelInitializers);
        return engine;
    }

    private static void showUsage() {
        System.out.println("\n\n");
        System.out.println("Usage: java -jar server.jar <default |  " +
                "jaxrs> /path/to/properties.prop");
        System.out.println("\n");
    }

}
