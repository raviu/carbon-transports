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

package org.wso2.carbon.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.wso2.carbon.disruptor.event.CarbonDisruptorEvent;
import org.wso2.carbon.disruptor.exception.GenericExceptionHandler;
import org.wso2.carbon.disruptor.handler.CarbonDisruptorEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DisruptorFactory {

    private static  Disruptor disruptor;

    private static List<Disruptor> disruptorMap = new ArrayList<Disruptor>();

    public static Disruptor getDisruptor(int threadPoolSize) {
        return disruptor;
    }
    public static Disruptor createDisruptor(int threadPoolSize) {

        ExecutorService executorService = Executors.newFixedThreadPool(1);
       disruptor = new Disruptor<CarbonDisruptorEvent>(
                   CarbonDisruptorEvent.EVENT_FACTORY,
                   1024,
                   executorService,
                   ProducerType.MULTI,
                   PhasedBackoffWaitStrategy.withLiteLock(1, 4, TimeUnit.SECONDS));
        ExceptionHandler exh = new GenericExceptionHandler();
        EventHandler eventHandler = new CarbonDisruptorEventHandler();
        disruptor.handleEventsWith(eventHandler);
        disruptor.handleExceptionsFor(eventHandler).with(exh);
        disruptor.start();
        return disruptor;
    }

    public static void populateDisruptors(int concurrency) {

       for(int i=0;i<2*concurrency;i++) {
           ExecutorService executorService = Executors.newFixedThreadPool(1);
           disruptor = new Disruptor<CarbonDisruptorEvent>(
                      CarbonDisruptorEvent.EVENT_FACTORY,
                      4096,
                      executorService,
                      ProducerType.SINGLE,
                      PhasedBackoffWaitStrategy.withLock(1, 4, TimeUnit.SECONDS));
           ExceptionHandler exh = new GenericExceptionHandler();
           EventHandler eventHandler = new CarbonDisruptorEventHandler();
           disruptor.handleEventsWith(eventHandler);
           disruptor.handleExceptionsFor(eventHandler).with(exh);
           disruptor.start();
           disruptorMap.add(disruptor);
       }

    }

    public static Disruptor  getDisruptorFromMap(){
        return disruptorMap.remove(0);
    }

}
