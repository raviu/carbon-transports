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
package org.wso2.carbon.http.netty.internal;

import io.netty.channel.ChannelInitializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: class level comment
 */
public class NettyTransportDataHolder {

    private static NettyTransportDataHolder instance = new NettyTransportDataHolder();
    private Map<String, List<ChannelInitializer>> channelInitializers = new HashMap<>();

    private NettyTransportDataHolder(){

    }

    public static NettyTransportDataHolder getInstance() {
        return instance;
    }

    public synchronized void addNettyChannelInitializer(String key, ChannelInitializer initializer) {
        List<ChannelInitializer> chInitializers = this.channelInitializers.get(key);
        if (chInitializers == null) {
            chInitializers = new ArrayList<>();
            this.channelInitializers.put(key, chInitializers);
        }
        chInitializers.add(initializer);
    }

    public List<ChannelInitializer> getChannelInitializers(String key) {
        List<ChannelInitializer> chInitializers = channelInitializers.get(key);
        return chInitializers == null ? new ArrayList<ChannelInitializer>() : chInitializers;
    }

    public void removeNettyChannelInitializer(String key, ChannelInitializer channelInitializer) {
        List<ChannelInitializer> chInitializers = channelInitializers.get(key);
        if (chInitializers != null) {
            chInitializers.remove(channelInitializer);
            if (chInitializers.isEmpty()) {
               channelInitializers.remove(key);
            }
        }
    }
}
