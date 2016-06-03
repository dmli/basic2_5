package com.ldm.basic.http;

import android.content.Context;

import com.ldm.basic.utils.MD5;
import com.ldm.basic.utils.SystemTool;

/**
 * Created by ldm on 12-4-11.
 * 基于BasicPostHelper与Thread封装的一个网络工具，使用时请详细阅读代码注释
 */
public class HttpEntity {

    /**
     * 在独立的线程中运行网络请求，当msg不等于null时附带提示框
     *
     * @param context  上下文
     * @param url      URL地址
     * @param callBack 回调
     */
    public static void httpGet(final Context context, final String url, final RequestRetCallBack callBack) {
        /**
         * 使用MD5根据URL创建一个标记，用来判断任务的有效性
         */
        httpGet(context, url, MD5.md5(url), callBack);
    }

    /**
     * 在独立的线程中运行网络请求，当msg不等于null时附带提示框
     *
     * @param context  上下文
     * @param url      URL地址
     * @param tag      tag 网络请求的标记，用来判断任务的有效性
     * @param callBack 回调
     */
    public static void httpGet(final Context context, final String url, final String tag, final RequestRetCallBack callBack) {
        if (!SystemTool.isNetworkAvailable(context)) {
            callBack.ioError();// 网络异常
        } else {
            RequestTaskInfo taskInfo = new RequestTaskInfo(tag, System.currentTimeMillis());
            callBack.setTaskInfo(taskInfo);
            // 创建Thread
            BasicHttpThreadTool.getInstance().addTask(tag, new HttpRequestSingleTask(HttpMultiTaskHandler.getInstance().build(taskInfo), url, null, HttpRequest.HTTP_MODE_GET, callBack));
        }
    }

    /**
     * 在独立的线程中运行网络请求，当msg不等于null时附带提示框
     *
     * @param context  Context
     * @param param    HttpEntity[]
     * @param callBack RequestRetCallBack
     */
    private static void httpPost(final Context context, final String url, final String param, final RequestRetCallBack callBack) {
        //使用MD5根据URL创建一个标记，用来判断任务的有效性
        httpPost(context, url, param, MD5.md5(url), callBack);
    }

    /**
     * 在独立的线程中运行网络请求，当msg不等于null时附带提示框
     *
     * @param context  Context
     * @param param    HttpEntity[]
     * @param tag      tag 网络请求的标记，用来判断任务的有效性
     * @param callBack RequestRetCallBack
     */
    private static void httpPost(final Context context, final String url, final String param, final String tag, final RequestRetCallBack callBack) {
        if (!SystemTool.isNetworkAvailable(context)) {
            callBack.ioError();// 网络异常
        } else {
            RequestTaskInfo taskInfo = new RequestTaskInfo(tag, System.currentTimeMillis());
            callBack.setTaskInfo(taskInfo);
            // 创建Thread
            BasicHttpThreadTool.getInstance().addTask(tag, new HttpRequestSingleTask(HttpMultiTaskHandler.getInstance().build(taskInfo), url, param, HttpRequest.HTTP_MODE_POST, callBack));
        }
    }

    /**
     * 网络集合使用（该方法内部所有子节点（RequestSet）的回调方法除asynchronous外皆为同步操作， 如果有大数据量操作切不需要更新UI时尽量用asynchronous处理）
     *
     * @param context    c
     * @param tag        tag 网络请求的标记，用来判断任务的有效性
     * @param requestSet 网络集合
     */
    public static void httpPostSet(final Context context, final String tag, final RequestSet requestSet) {
        if (!SystemTool.isNetworkAvailable(context)) {
            requestSet.ioError();// 网络异常
        } else {
            requestSet.enter();// 本次网络请求进入初始化状态
            RequestTaskInfo taskInfo = new RequestTaskInfo(tag, System.currentTimeMillis());
            requestSet.setTaskInfo(taskInfo);
            BasicHttpThreadTool.getInstance().addTask(tag, new HttpRequestMultiTask(requestSet, HttpMultiTaskHandler.getInstance().build(taskInfo)));
        }
    }

}
