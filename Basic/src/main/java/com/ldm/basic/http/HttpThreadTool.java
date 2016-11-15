package com.ldm.basic.http;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ldm on 16/2/24.
 * <p/>
 * 这是一个简易的Thread工具，它只有一个功能，可以保证带有唯一标记的任务不被重复执行的功能，
 * 当任务tag=1被执行时，在tag=1没有执行完成时，再次触发tag=1的任务时将会被自动过滤掉。
 * 在开发时可以更轻松的使用TNPOnlyAsyncTaskTool来创建任务，而不需要考虑任务的callback被多次触发.
 * 注：AsyncTask中的callback方法均为异步
 */
class HttpThreadTool {

    /**
     * 当前的任务列表
     */
    private final Map<String, AsyncTask> ts = new HashMap<>();

    /**
     * 线程池
     */
    private ExecutorService threadPool;


    private static HttpThreadTool basicHttpThreadTool;

    private HttpThreadTool() {
    }

    public static HttpThreadTool getInstance() {
        if (basicHttpThreadTool == null) {
            basicHttpThreadTool = new HttpThreadTool();
        }
        return basicHttpThreadTool;
    }

    /**
     * 销毁线程池
     */
    public static void shutdown() {
        if (basicHttpThreadTool != null && basicHttpThreadTool.threadPool != null) {
            basicHttpThreadTool.threadPool.shutdownNow();
        }
    }

    /**
     * 创建一个异步任务
     *
     * @param tag  这个任务的标签，用来识别这个任务的唯一性
     * @param task AsyncTask
     */
    void addTask(String tag, AsyncTask task) {
        synchronized (ts) {
            /**
             * 如果任务tag存在，将过滤掉这个任务
             */
            if (!ts.containsKey(tag)) {
                ts.put(tag, task);
                if (threadPool == null || threadPool.isShutdown()) {
                    threadPool = Executors.newFixedThreadPool(5);
                }
                threadPool.submit(new TaskThread(tag));
            }
        }
    }

    /**
     * 异步接口
     */
    interface AsyncTask {
        void async();
    }

    /**
     * Thread
     */
    private class TaskThread implements Runnable {
        String tag;

        TaskThread(String tag) {
            this.tag = tag;
        }

        @Override
        public void run() {
            try {
                AsyncTask a;
                synchronized (ts) {
                    a = ts.get(tag);
                }
                if (a != null) {
                    a.async();
                }
                synchronized (ts) {
                    ts.remove(tag);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
