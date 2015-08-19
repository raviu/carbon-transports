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

import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonCallback;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.TransportSender;
import org.wso2.carbon.common.CarbonMessageImpl;

public class POCMediationEngine implements org.wso2.carbon.api.Engine {
    private static Logger log = Logger.getLogger(POCMediationEngine.class);

    private static String ENGINE_PROTOCOL = "http";
    private TransportSender sender;

    public POCMediationEngine(TransportSender sender) {
        this.sender = sender;
        sender.setEngine(this);
    }

    public boolean init(TransportSender sender) {
        return true;
    }

    public boolean receive(CarbonMessage msg, final CarbonCallback responseCallback) {

        CarbonMessage outMsg = new CarbonMessageImpl(ENGINE_PROTOCOL);
        outMsg.setPipe(msg.getPipe());
        outMsg.setHost(POCController.props.getProperty("proxy_to_host", "localhost"));
        outMsg.setPort(Integer.valueOf(POCController.props.getProperty("proxy_to_port", "8280")));
        outMsg.setURI(POCController.props.getProperty("proxy_to_uri", "/services/echo"));
        outMsg.setProperties(msg.getProperties());
        outMsg.setProperty(ENGINE_PROTOCOL, "Custom-Header", "PerfTest");
        CarbonCallback callbackNew = new CarbonCallback() {
            public void done(CarbonMessage cMsg) {
                log.info("This is a test!");
                responseCallback.done(cMsg);
            }
        };
        sender.send(outMsg, callbackNew);

        return true;
    }

    @Override
    public TransportSender getSender() {
        return sender;
    }
}
