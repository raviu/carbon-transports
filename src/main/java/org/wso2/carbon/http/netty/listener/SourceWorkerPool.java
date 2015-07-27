package org.wso2.carbon.http.netty.listener;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SourceWorkerPool {

    private static SourceWorkerPool instance = new SourceWorkerPool();
    private static ExecutorService executorService ;


    private SourceWorkerPool() {
        executorService = Executors.newFixedThreadPool(100);
    }

    public static SourceWorkerPool getInstance() {
        return instance;
    }

    public static void submitJob(Runnable runnable) {
        executorService.submit(runnable);
    }
}
