package org.wso2.carbon.http.netty.common;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerPool {

    private static WorkerPool instance = new WorkerPool();
    private static ExecutorService executorService ;


    private WorkerPool() {
        executorService = Executors.newFixedThreadPool(100);
    }

    public static WorkerPool getInstance() {
        return instance;
    }

    public static void submitJob(Runnable runnable) {
        executorService.submit(runnable);
    }
}
