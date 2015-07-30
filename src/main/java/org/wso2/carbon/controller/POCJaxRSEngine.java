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

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.TransportSender;
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.controller.builder.XMLBuilder;

public class POCJaxRSEngine implements org.wso2.carbon.api.Engine {
    private static Logger log = Logger.getLogger(POCJaxRSEngine.class);

    private static String ENGINE_PROTOCOL = "http";
    private TransportSender sender;

    public POCJaxRSEngine(TransportSender sender) {
        this.sender = sender;
        sender.setEngine(this);
    }

    public boolean init() {
        return true;
    }

    public boolean receive(CarbonMessage msg) {

        if (msg.getDirection() == CarbonMessageImpl.IN) {
            log.info(msg.getProperty(ENGINE_PROTOCOL, "HTTP_METHOD") + " "
                    + msg.getURI()  + " "
                    + msg.getProperty(ENGINE_PROTOCOL, "HTTP_VERSION"));


            OMElement incoming = XMLBuilder.buildMessage(msg.getPipe());

//            CarbonMessage outMsg = new CarbonMessageImpl(ENGINE_PROTOCOL);
//
//            log.info("Forward Message");
//            outMsg.setHost("localhost");
//            outMsg.setPort(8280);
//            outMsg.setURI("/services/echo");
//            outMsg.setProperties(msg.getProperties());
//            outMsg.setProperty(ENGINE_PROTOCOL, "Custom-Header", "Hello");
//            sender.send(outMsg);
        }

        return true;
    }

}
