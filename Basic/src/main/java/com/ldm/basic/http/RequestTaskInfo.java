package com.ldm.basic.http;

/**
 * Created by ldm on 16/6/3.
 * 网络请求任务信息
 */
public class RequestTaskInfo {

    String tag;
    long time;

    public RequestTaskInfo(String tag, long time) {
        this.tag = tag;
        this.time = time;
    }
}
