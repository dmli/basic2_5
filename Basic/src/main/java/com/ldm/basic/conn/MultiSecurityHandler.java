package com.ldm.basic.conn;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by ldm on 15/10/15.
 * 支持HttpEntity创建单线程多任务使用的Handler
 */
public class MultiSecurityHandler extends Handler {

    protected int index;
    protected WeakReference<Context> t = null;

    public MultiSecurityHandler(Context context, int index) {
        this.t = new WeakReference<>(context);
        this.index = index;
    }

    @Override
    public void handleMessage(Message msg) {
        if (t == null || t.get() == null) {
            return;
        }

        Context context = t.get();
        if (context == null){
            return;
        }

        RequestSet requestSet = null;
        RequestRetCallBack callBack = null;
        if (msg.arg1 == 2 && msg.what == index) {
            requestSet = (RequestSet) msg.obj;
        } else {
            callBack = (RequestRetCallBack) msg.obj;
        }

        if (callBack == null && requestSet == null) {
            return;
        }

        handleMessage(msg, context, requestSet, callBack);
    }

    /**
     * MultiSecurityHandler 的handleMessage处理流程
     *
     * @param msg        Message
     * @param context    Context
     * @param requestSet RequestSet
     * @param callBack   RequestRetCallBack
     */
    private void handleMessage(Message msg, Context context, RequestSet requestSet, RequestRetCallBack callBack) {
        // SET结合的处理 msg.what == index）本次请求结束标识
        if (msg.what == index) {
            // 在本次网络集合请求结束时执行退出方法
            if (msg.arg1 == 2) {
                requestSet.exit();
            } else {
                callBack.exit();// 单任务使用
            }
        } else if (msg.what == HttpEntity.RESULT_SUCCESS) {// 子线程“success”的回调
            callBack.success(callBack.data);
        } else if (msg.what == HttpEntity.RESULT_RET_NULL) {// 子线程“retNull”的回调
            callBack.retNull(context);
        } else if (msg.what == HttpEntity.RESULT_ERROR) {// 子线程“error”的回调
            callBack.error(context, callBack.code, String.valueOf(callBack.data));
        } else if (msg.what == HttpEntity.RESULT_IS_NETWORK_STATE) {// 子线程“isNetWorkState”的回调
            callBack.isNetWorkState(context);
        } else if (msg.what == HttpEntity.RESULT_IO_ERROR) {// 子线程“ioError”的回调
            callBack.ioError(context);
        } else if (msg.what == HttpEntity.RESULT_CHILD_ENTER) {// 子线程开始
            callBack.enter();
        } else if (msg.what == HttpEntity.RESULT_CHILD_EXIT) {// 多任务使用的子线程结束
            callBack.exit();
        }
    }
}
