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

import org.wso2.carbon.http.netty.listener.CarbonNettyChannelInitializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO: class level comment
 */
public class NettyTransportDataHolder {

    private static NettyTransportDataHolder instance = new NettyTransportDataHolder();
    private List<CarbonNettyChannelInitializer> channelInitializers = new ArrayList<>();

    private NettyTransportDataHolder(){

    }

    public static NettyTransportDataHolder getInstance() {
        return instance;
    }

    public void addNettyChannelInitializer(CarbonNettyChannelInitializer initializer) {
        channelInitializers.add(initializer);
    }

    public List<CarbonNettyChannelInitializer> getNettyChannelInitializer() {
        return Collections.unmodifiableList(channelInitializers);
    }

    public void removeNettyChannelInitializer(CarbonNettyChannelInitializer initializer) {
        channelInitializers.remove(initializer);
    }
}
