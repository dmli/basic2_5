package com.ldm.basic.bean;

/**
 * Created by ldm on 14-5-6.
 * 文件下载任务Bean
 */
public class MultiThreadTaskRef {

    private String url;//URL
    private int index;//任务索引（线程索引）
    private long startPosition;//开始位置
    private long endPosition;//结束位置
    private int completeSize;//当前完成数
    private boolean isComplete;//当前线程是否处于完成状态

    public MultiThreadTaskRef() {
    }

    public MultiThreadTaskRef(String url, int index, long startPosition, long endPosition) {
        this.url = url;
        this.index = index;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public MultiThreadTaskRef(MultiThreadTaskRef ref) {
        this.url = ref.url;
        this.index = ref.index;
        this.startPosition = ref.startPosition;
        this.endPosition = ref.endPosition;
    }

    public int getCompleteSize() {
        return completeSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public void setCompleteSize(int completeSize) {
        this.completeSize = completeSize;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }
}
