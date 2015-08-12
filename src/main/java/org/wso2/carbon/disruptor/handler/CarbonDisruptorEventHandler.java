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


import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.Engine;
import org.wso2.carbon.disruptor.event.CarbonDisruptorEvent;
import org.wso2.carbon.http.netty.common.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarbonDisruptorEventHandler extends DisruptorEventHandler{

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void onEvent(CarbonDisruptorEvent carbonDisruptorEvent, long l, boolean b) throws Exception {
        CarbonMessage carbonMessage = (CarbonMessage)carbonDisruptorEvent.getEvent();
        Engine engine = (Engine) carbonMessage.getProperty(Constants.PROTOCOL_NAME, Constants.ENGINE);
      engine.receive(carbonMessage);
       // executorService.submit(new Worker(engine,carbonMessage));
    }






}

class Worker implements Runnable {
    private Engine engine;
    private  CarbonMessage carbonMessage;

    public Worker(Engine engine, CarbonMessage carbonMessage){
       this.engine=engine;
        this.carbonMessage=carbonMessage;
    }

    @Override
    public void run() {
        engine.receive(carbonMessage);
    }
}