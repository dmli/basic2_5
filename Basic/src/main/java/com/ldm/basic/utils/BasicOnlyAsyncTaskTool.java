package com.ldm.basic.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ldm on 16/2/24.
 * 这是一个简易的Thread工具(后期可以使用线程池来替换Thread)，它只有一个功能，可以保证带有唯一标记的任务不被重复执行的功能，
 * 当任务tag=1被执行时，在tag=1没有执行完成时，再次触发tag=1的任务时将会被自动过滤掉。
 * 在开发时可以更轻松的使用TNPOnlyAsyncTaskTool来创建任务，而不需要考虑任务的callback被多次触发.
 * 注：AsyncTask中的callback方法均为异步
 */
public class BasicOnlyAsyncTaskTool {

    private final Map<String, AsyncTask> ts = new HashMap<>();

    public void addTask(String tag, AsyncTask task) {
        synchronized (ts) {
            /**
             * 如果任务tag存在，将过滤掉这个任务
             */
            if (!ts.containsKey(tag)) {
                ts.put(tag, task);
                //启动任务
                new TaskThread(tag) {}.start();
            }
        }
    }

    /**
     * 异步接口
     */
    public interface AsyncTask {
        void async();
    }

    /**
     * Thread
     */
    private class TaskThread extends Thread {
        String tag;

        public TaskThread(String tag) {
            this.tag = tag;
        }

        @Override
        public void run() {
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
        }
    }
}
