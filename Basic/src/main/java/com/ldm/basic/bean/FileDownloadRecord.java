package com.ldm.basic.bean;

import java.util.List;

/**
 * Created by ldm on 14-5-6.
 * 文件下载记录
 */
public class FileDownloadRecord {

    private String url;//用来比较任务是否已经发生变化
    private String filePath;//文件的完成路径，包含名称
    private String fileName;//文件名
    private int threadTotal;//使用的总线程数
    private long fileSize;//文件大小
    private long currentSize;//当前下载大小
    private boolean isComplete;//文件是否完全下载成功
    private int current;//当前完成下载数量（线程数）

    public FileDownloadRecord() {
    }

    private List<MultiThreadTaskRef> multiThreadTaskRef;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getThreadTotal() {
        return threadTotal;
    }

    public void setThreadTotal(int threadTotal) {
        this.threadTotal = threadTotal;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public List<MultiThreadTaskRef> getMultiThreadTaskRef() {
        return multiThreadTaskRef;
    }

    public void setMultiThreadTaskRef(List<MultiThreadTaskRef> multiThreadTaskRef) {
        this.multiThreadTaskRef = multiThreadTaskRef;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }
}
