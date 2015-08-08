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

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.wso2.carbon.disruptor.event.CarbonDisruptorEvent;
import org.wso2.carbon.disruptor.exception.GenericExceptionHandler;
import org.wso2.carbon.disruptor.handler.CarbonDisruptorEventHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DisruptorFactory {

    public static Disruptor getDisruptor(int threadPoolSize) {

        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        Disruptor disruptor = new Disruptor<CarbonDisruptorEvent>(
                   CarbonDisruptorEvent.EVENT_FACTORY,
                   1024,
                   executorService,
                   ProducerType.SINGLE,
                   new YieldingWaitStrategy());
        ExceptionHandler exh = new GenericExceptionHandler();
        EventHandler eventHandler = new CarbonDisruptorEventHandler();
        disruptor.handleEventsWith(eventHandler);
        disruptor.handleExceptionsFor(eventHandler).with(exh);
        return disruptor;
    }

}
