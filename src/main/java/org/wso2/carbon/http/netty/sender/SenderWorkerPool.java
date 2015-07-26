package org.wso2.carbon.http.netty.sender;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SenderWorkerPool {

    private static SenderWorkerPool instance = new SenderWorkerPool();
    private static ExecutorService executorService ;


    private SenderWorkerPool() {
        executorService = Executors.newFixedThreadPool(100);
    }

    public static SenderWorkerPool getInstance() {
        return instance;
    }

    public static void submitJob(Runnable runnable) {
        executorService.submit(runnable);
    }
}
