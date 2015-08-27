/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.disruptor.handler;


import org.wso2.carbon.api.CarbonCallback;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.disruptor.DisruptorFactory;
import org.wso2.carbon.disruptor.event.CarbonDisruptorEvent;
import org.wso2.carbon.http.netty.common.Constants;

public class CarbonDisruptorEventHandler extends DisruptorEventHandler {

   private int eventHandlerid;

    public CarbonDisruptorEventHandler(int eventHandlerid){
        this.eventHandlerid = eventHandlerid;
    }



    @Override
    public void onEvent(CarbonDisruptorEvent carbonDisruptorEvent, long l, boolean b) throws Exception {
        CarbonMessage carbonMessage = (CarbonMessage) carbonDisruptorEvent.getEvent();
        int messageID = carbonDisruptorEvent.getEventId();
        if (canProcess(DisruptorFactory.noOfEventHandlersPerDisruptor(), eventHandlerid, messageID)) {
            Engine engine = (Engine) carbonMessage.getProperty(Constants.PROTOCOL_NAME, Constants.ENGINE);
            if (carbonMessage.getDirection() == CarbonMessage.IN) {
                CarbonCallback carbonCallback = (CarbonCallback) carbonMessage.getProperty(Constants.PROTOCOL_NAME, Constants.RESPONSE_CALLBACK);
                engine.receive(carbonMessage, carbonCallback);
            } else {
                CarbonCallback carbonCallback = (CarbonCallback) carbonMessage.getProperty(Constants.PROTOCOL_NAME, Constants.RESPONSE_CALLBACK);
                carbonCallback.done(carbonMessage);
            }

        }
    }
}

