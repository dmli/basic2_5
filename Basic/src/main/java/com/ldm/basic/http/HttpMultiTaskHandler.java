package com.ldm.basic.http;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ldm on 15/10/15.
 * 支持HttpEntity创建单线程多任务使用的Handler
 */
public class HttpMultiTaskHandler extends Handler {

    /**
     * 成功
     */
    public static final int RESULT_SUCCESS = 200;

    /**
     * 失败error 附带错误信息
     */
    public static final int RESULT_ERROR = 100;

    /**
     * 返回数据为空
     */
    public static final int RESULT_RET_NULL = 101;

    /**
     * IO error 网络连接异常
     */
    public static final int RESULT_IO_ERROR = 102;

    /**
     * 任务进入
     */
    public static final int RESULT_CHILD_ENTER = 110;

    /**
     * 任务退出
     */
    public static final int RESULT_CHILD_END = 111;

    /**
     * 线程集合请求完成后执行
     */
    public static final int RESULT_REQUEST_SET_EXIT = 112;


    /**
     * 存储任务
     */
    private final Map<String, Long> taskList = new HashMap<>();

    private static HttpMultiTaskHandler handler;

    public static HttpMultiTaskHandler getInstance() {
        if (handler != null) {
            handler = new HttpMultiTaskHandler();
        }
        return handler;
    }

    public HttpMultiTaskHandler build(RequestTaskInfo taskInfo) {
        taskList.put(taskInfo.tag, taskInfo.time);
        return handler;
    }

    private HttpMultiTaskHandler() {
        super(Looper.getMainLooper());
    }

    /**
     * 发送网络请求状态
     *
     * @param what int what
     * @param obj  Object obj
     */
    public void sendResponseState(int what, Object obj) {
        sendMessage(obtainMessage(what, obj));
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.obj == null) {
            return;
        }

        /**
         * HttpMultiTaskHandler.RESULT_REQUEST_SET_EXIT 网络集合结束时发送的消息
         */
        if (msg.what == HttpMultiTaskHandler.RESULT_REQUEST_SET_EXIT) {
            RequestSet set = ((RequestSet) msg.obj);
            if (isVerify(set.taskInfo)) {
                set.end();
            }
        } else {
            RequestRetCallBack callBack = (RequestRetCallBack) msg.obj;
            if (isVerify(callBack.taskInfo)) {
                handle(msg, callBack);
            }
        }
    }

    private boolean isVerify(RequestTaskInfo taskInfo) {
        return taskList.containsKey(taskInfo.tag) && taskList.get(taskInfo.tag) == taskInfo.time;
    }

    /**
     * HttpMultiTaskHandler 的handleMessage处理流程
     *
     * @param msg      Message
     * @param callBack RequestRetCallBack
     */
    private void handle(Message msg, RequestRetCallBack callBack) {
        if (msg.what == HttpMultiTaskHandler.RESULT_SUCCESS) {
            callBack.success(callBack.data);
        } else if (msg.what == HttpMultiTaskHandler.RESULT_RET_NULL) {
            callBack.retNull();
        } else if (msg.what == HttpMultiTaskHandler.RESULT_ERROR) {
            callBack.error(callBack.code, String.valueOf(callBack.data));
        } else if (msg.what == HttpMultiTaskHandler.RESULT_IO_ERROR) {
            callBack.ioError();
        } else if (msg.what == HttpMultiTaskHandler.RESULT_CHILD_ENTER) {
            callBack.enter();
        } else if (msg.what == HttpMultiTaskHandler.RESULT_CHILD_END) {
            synchronized (taskList) {
                taskList.remove(callBack.taskInfo.tag);
            }
            callBack.end();
        }
    }
}
