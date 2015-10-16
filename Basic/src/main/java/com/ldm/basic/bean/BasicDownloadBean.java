package com.ldm.basic.bean;

import java.io.Serializable;

/**
 * Created by ldm on 14-5-9.
 * DownloadService下载实体
 */
public class BasicDownloadBean implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String tag;//标记,可以用来区分下载任务
    private String url;
    private String name;
    private int progress;//百分比
    private boolean isDownload;//true处于下载中
    private int state;//状态0等待中， 1下载中，2出现异常且记录已经保存，3网络异常 4下载成功
    private String error;//异常时的异常信息


    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean isDownload) {
        this.isDownload = isDownload;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
