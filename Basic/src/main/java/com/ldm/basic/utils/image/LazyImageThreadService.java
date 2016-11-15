package com.ldm.basic.utils.image;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ldm on 16/5/31.
 * 线程池
 */
class LazyImageThreadService {

    private ExecutorService cacheThreadPool;
    private ExecutorService assignThreadPool;
    private ExecutorService downloadThreadPool;

    private int downloadTaskNumber;
    private int cacheAsyncTaskNumber;

    private boolean isRunning;

    private DisplayUIPresenter handler;

    private LazyImageThreadService(LazyImageDownloader lazy, int downloadTaskNumber, int cacheAsyncTaskNumber) {
        this.downloadTaskNumber = downloadTaskNumber;
        this.cacheAsyncTaskNumber = cacheAsyncTaskNumber;
        if (handler == null) {
            handler = new DisplayUIPresenter(lazy);
        }
        isRunning = true;
    }

    /**
     * 创建一个LazyImageThreadService
     *
     * @param lazy                 LazyImageDownloader
     * @param downloadTaskNumber   用来解析任务的最大线程数
     * @param cacheAsyncTaskNumber 下载图片时的最大线程数
     * @return LazyImageThreadService
     */
    public static LazyImageThreadService create(LazyImageDownloader lazy, int downloadTaskNumber, int cacheAsyncTaskNumber) {
        return new LazyImageThreadService(lazy, downloadTaskNumber, cacheAsyncTaskNumber);
    }


    private ExecutorService getAssignThreads() {
        if (assignThreadPool == null || assignThreadPool.isShutdown()) {
            assignThreadPool = Executors.newFixedThreadPool(2);
        }
        return assignThreadPool;
    }

    private ExecutorService getCacheThreads() {
        if (cacheThreadPool == null || cacheThreadPool.isShutdown()) {
            cacheThreadPool = Executors.newFixedThreadPool(cacheAsyncTaskNumber);
        }
        return cacheThreadPool;
    }

    private ExecutorService getDownloadThreads() {
        if (downloadThreadPool == null || downloadThreadPool.isShutdown()) {
            downloadThreadPool = Executors.newFixedThreadPool(downloadTaskNumber);
        }
        return downloadThreadPool;
    }


    /**
     * 释放线程及handler任务
     */
    void release() {
        if (downloadThreadPool != null) {
            downloadThreadPool.shutdownNow();
        }
        if (cacheThreadPool != null) {
            cacheThreadPool.shutdownNow();
        }
        if (assignThreadPool != null) {
            assignThreadPool.shutdownNow();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        isRunning = false;
    }

    /**
     * 是否处于运行中
     *
     * @return true/false
     */
    boolean isRunning() {
        return isRunning;
    }

    /**
     * 创建一个异步解析任务
     *
     * @param task Runnable
     */
    void createAssignTask(Runnable task) {
        if (isRunning){
            getAssignThreads().submit(task);
        }
    }

    /**
     * 创建一个加载缓存的任务
     *
     * @param task Runnable
     */
    void addCacheTask(Runnable task) {
        if (isRunning){
            getCacheThreads().submit(task);
        }
    }

    /**
     * 创建一个下载任务
     *
     * @param task Runnable
     */
    void addDownloadTask(Runnable task) {
        if (isRunning){
            getDownloadThreads().submit(task);
        }
    }

    void sendMessage(int what, ImageOptions ref) {
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(what, ref));
        }
    }

}
