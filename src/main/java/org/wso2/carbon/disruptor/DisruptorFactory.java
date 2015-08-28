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

    private static Disruptor disruptor;

    private static List<RingBuffer> disruptorMap = new ArrayList<RingBuffer>();
    private static int noOfDisruptors;
    private static int index = 1;
    private static int noOfEventHandlersPerDisruptor = 1;

    public static Disruptor getDisruptor() {
        return disruptor;
    }

    public static void createDisruptor(int bufferSize, int noDisruptors, int noOfEventHandlersPerDisruptor,
                                       WAITSTRATEGY waitstrategy) {
        noOfDisruptors = noDisruptors;
        DisruptorFactory.noOfEventHandlersPerDisruptor = noOfEventHandlersPerDisruptor;
        WaitStrategy waitStrategy;

        switch (waitstrategy) {
            case BLOCKING_WAIT:
                waitStrategy = new BlockingWaitStrategy();
                break;
            case BUSY_SPIN:
                waitStrategy = new BusySpinWaitStrategy();
                break;
            case LITE_BLOCKING:
                waitStrategy = new LiteBlockingWaitStrategy();
                break;
            case PHASED_BACKOFF:
                waitStrategy = PhasedBackoffWaitStrategy.withLiteLock(1, 4, TimeUnit.SECONDS);
                break;
            case SLEEPING_WAIT:
                waitStrategy = new SleepingWaitStrategy();
                break;
            case TIME_BLOCKING:
                waitStrategy = new TimeoutBlockingWaitStrategy(1, TimeUnit.SECONDS);
                break;
            default:
                waitStrategy = PhasedBackoffWaitStrategy.withLiteLock(1, 4, TimeUnit.SECONDS);

        }
        for (int i = 0; i < noDisruptors; i++) {
            ExecutorService executorService = Executors.newFixedThreadPool(noOfEventHandlersPerDisruptor);
            disruptor = new Disruptor<CarbonDisruptorEvent>(
                       CarbonDisruptorEvent.EVENT_FACTORY,
                       bufferSize,
                       executorService,
                       ProducerType.MULTI,
                       waitStrategy);
            ExceptionHandler exh = new GenericExceptionHandler();
            EventHandler[] eventHandlers = new EventHandler[noOfEventHandlersPerDisruptor];
            for (int j = 0; j < noOfEventHandlersPerDisruptor; j++) {
                EventHandler eventHandler = new CarbonDisruptorEventHandler(j);
                eventHandlers[j] = eventHandler;
            }
            disruptor.handleEventsWith(eventHandlers);
            for (EventHandler eventHandler : eventHandlers) {
                disruptor.handleExceptionsFor(eventHandler).with(exh);
            }
            disruptorMap.add(disruptor.start());

        }
    }


    public synchronized static RingBuffer getDisruptorFromMap() {
        int ind = index % noOfDisruptors;
        index++;
        return disruptorMap.get(ind);
    }

    public static int noOfEventHandlersPerDisruptor() {
        return noOfEventHandlersPerDisruptor;
    }

    public enum WAITSTRATEGY {
        BLOCKING_WAIT, BUSY_SPIN, LITE_BLOCKING, PHASED_BACKOFF, SLEEPING_WAIT, TIME_BLOCKING
    }
}