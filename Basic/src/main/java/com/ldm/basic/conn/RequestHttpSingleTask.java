package com.ldm.basic.conn;

import com.ldm.basic.bean.BasicInternetRetBean;


/**
 * Created by ldm on 15/10/15.
 * 用来处理单一任务的网络请求
 */
public class RequestHttpSingleTask implements BasicHttpThreadTool.AsyncTask {
    private MultiSecurityHandler handler;

    private String url;
    private String mode;
    private String param;

    private int index;
    private RequestRetCallBack callBack;

    private boolean isStop;

    /**
     * 创建一个单任务的线程
     *
     * @param handler  更新UI用
     * @param url      地址
     * @param param    内容
     * @param mode     类型 - HTTP_MODE_POST --- HTTP_MODE_GET
     * @param index    伪随机数
     * @param callBack 回调接口
     */
    public RequestHttpSingleTask(MultiSecurityHandler handler, String url, String param, String mode, int index, RequestRetCallBack callBack) {
        this.handler = handler;
        this.url = url;
        this.param = param;
        this.mode = mode;
        this.index = index;
        this.callBack = callBack;
        this.isStop = false;
    }

    /**
     * 网络请求处理后的处理流程
     *
     * @param bib BasicInternetRetBean
     */
    private void onResult(BasicInternetRetBean bib) {
        if (bib != null) {// 此处数据返回
            if (bib.getCode() == 0) {
                if (bib.getSuccess() != null && !"".equals(bib.getSuccess()) && !"[]".equals(bib.getSuccess())) {
                    callBack.data = callBack.asynchronous(bib.getSuccess());
                    handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_SUCCESS, callBack));// 数据处理成功
                } else {
                    handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_RET_NULL, callBack));// 返回数据为空，默认使用retNull函数
                }
            } else if (bib.getCode() == 1) {
                callBack.data = bib.getError();
                callBack.code = bib.getResponseCode();
                handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_ERROR, callBack));
            } else if (bib.getCode() == 2) {
                handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_IS_NETWORK_STATE, callBack));
                handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_IO_ERROR, callBack));
            }
        } else {
            callBack.code = -1;
            handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_ERROR, callBack));
        }
        handler.sendMessage(handler.obtainMessage(index, callBack));// 接口全部执行完后调用
    }

    @Override
    public void async() {
        if (isStop || handler == null) {
            return;
        }

        handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_CHILD_ENTER, callBack));// 触发任务接口

        BasicInternetRetBean bib;
        if (HttpRequest.HTTP_MODE_GET.equals(mode)) {
            bib = new BasicHttpGet(url).execute(param);
        } else {
            bib = new BasicHttpPost(url).execute(param);
        }
        onResult(bib);
    }
}
