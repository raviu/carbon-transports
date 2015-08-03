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

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.http.netty.listener.CarbonNettyChannelInitializer;

@Component(
        name = "org.wso2.carbon.http.netty.internal.NettyTransportServiceComponent",
        immediate = true
)
@SuppressWarnings("unused")
public class NettyTransportServiceComponent {

    private BundleContext bundleContext;
    NettyTransportDataHolder dataHolder = NettyTransportDataHolder.getInstance();

    @Activate
    protected void start(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Reference(
            name = "netty-channel.initializer",
            service = CarbonNettyChannelInitializer.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeNettyChannelInitializer"
    )
    protected void addNettyChannelInitializer(CarbonNettyChannelInitializer channelInitializer) {
        try {
            dataHolder.addChannelInitializer(channelInitializer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    protected void removeNettyChannelInitializer(CarbonNettyChannelInitializer channelInitializer) {
        dataHolder.removeChannelInitializer(channelInitializer);
    }
}
