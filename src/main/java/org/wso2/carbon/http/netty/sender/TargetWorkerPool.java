package org.wso2.carbon.http.netty.sender;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TargetWorkerPool {

    private static TargetWorkerPool instance = new TargetWorkerPool();
    private static ExecutorService executorService ;


    private TargetWorkerPool() {
        executorService = Executors.newFixedThreadPool(100);
    }

    public static TargetWorkerPool getInstance() {
        return instance;
    }

    public static void submitJob(Runnable runnable) {
        executorService.submit(runnable);
    }
}
