package org.wso2.carbon.http.netty.listener;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListenerWorkerPool {

    private static ListenerWorkerPool instance = new ListenerWorkerPool();
    private static ExecutorService executorService ;


    private ListenerWorkerPool() {
        executorService = Executors.newFixedThreadPool(100);
    }

    public static ListenerWorkerPool getInstance() {
        return instance;
    }

    public static void submitJob(Runnable runnable) {
        executorService.submit(runnable);
    }
}
