package com.ldm.basic.utils.image;

import com.ldm.basic.utils.TaskThreadToMultiService;

/**
 * Created by ldm on 16-5-31.
 * 加载图片的的线程工具类
 */
public class LazyImageTaskThread {

    private TaskThreadToMultiService cacheThreads;
    private TaskThreadToMultiService assignThreads;
    private TaskThreadToMultiService downloadThreads;

    private int asyncTaskNumber;
    private int cacheAsyncTaskNumber;
    private LazyImageHandler lHandler;
    private boolean isRunning;

    public LazyImageTaskThread(LazyImageDownloader lazy, int asyncTaskNumber, int cacheAsyncTaskNumber) {
        this.asyncTaskNumber = asyncTaskNumber;
        this.cacheAsyncTaskNumber = cacheAsyncTaskNumber;
        if (lHandler == null) {
            lHandler = new LazyImageHandler(lazy);
        }
        isRunning = true;
    }

    private TaskThreadToMultiService getAssignThreads() {
        if (assignThreads == null) {
            assignThreads = new TaskThreadToMultiService(2);
        }
        return assignThreads;
    }

    private TaskThreadToMultiService getCacheThreads() {
        if (cacheThreads == null) {
            cacheThreads = new TaskThreadToMultiService(cacheAsyncTaskNumber);
        }
        return cacheThreads;
    }

    private TaskThreadToMultiService getDownloadThreads() {
        if (downloadThreads == null) {
            downloadThreads = new TaskThreadToMultiService(asyncTaskNumber);
        }
        return downloadThreads;
    }

    /**
     * 释放线程及handler任务
     */
    void release() {
        if (downloadThreads != null) {
            downloadThreads.stopTask();
            downloadThreads = null;
        }
        if (cacheThreads != null) {
            cacheThreads.stopTask();
            cacheThreads = null;
        }
        if (assignThreads != null) {
            assignThreads.stopTask();
            assignThreads = null;
        }
        if (lHandler != null) {
            lHandler.removeCallbacksAndMessages(null);
        }
        isRunning = false;
    }

    /**
     * 是否处于运行中
     *
     * @return true/false
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 创建一个异步解析任务
     *
     * @param ref LoaderAssignTask
     */
    void createAssignTask(LoaderAssignTask ref) {
        getAssignThreads().addTask(ref);
    }

    /**
     * 创建一个加载缓存的任务
     *
     * @param loaderCacheTask LoaderCacheTask
     */
    void addCacheTask(LoaderCacheTask loaderCacheTask) {
        getCacheThreads().addTask(loaderCacheTask);
    }

    /**
     * 创建一个下载任务
     *
     * @param ref LoaderDownloadTask
     */
    void addDownloadTask(LoaderDownloadTask ref) {
        getDownloadThreads().addTask(ref);
    }

    void sendMessage(int what, ImageOptions ref) {
        if (lHandler != null) {
            lHandler.sendMessage(lHandler.obtainMessage(what, ref));
        }
    }
}
