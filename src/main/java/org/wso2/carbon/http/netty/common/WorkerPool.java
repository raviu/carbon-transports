package org.wso2.carbon.http.netty.common;


import org.apache.log4j.Logger;
import org.wso2.carbon.controller.POCController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerPool {
    private static Logger log = Logger.getLogger(WorkerPool.class);

    private static WorkerPool instance = new WorkerPool();
    private static ExecutorService executorService ;


    private WorkerPool() {
        log.info("Executor Worker count: " + POCController.props.getProperty("workers", "100"));
        executorService = Executors.newFixedThreadPool(Integer.valueOf(
                POCController.props.getProperty("workers", "100")));
    }

    public static WorkerPool getInstance() {
        return instance;
    }

    public static void submitJob(Runnable runnable) {
        executorService.submit(runnable);
    }
}