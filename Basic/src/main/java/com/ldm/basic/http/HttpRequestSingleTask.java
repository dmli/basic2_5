package com.ldm.basic.http;

import com.ldm.basic.bean.BasicInternetRetBean;


/**
 * Created by ldm on 15/10/15.
 * 用来处理单一任务的网络请求
 */
class HttpRequestSingleTask implements HttpThreadTool.AsyncTask {
    private HttpMultiTaskHandler httpMultiTaskHandler;

    private String url;
    private String mode;
    private String param;

    private RequestRetCallBack callBack;

    private boolean isStop;

    /**
     * 创建一个单任务的线程
     *
     * @param httpMultiTaskHandler 更新UI用
     * @param url                  地址
     * @param param                内容
     * @param mode                 类型 - HTTP_MODE_POST --- HTTP_MODE_GET
     * @param callBack             回调接口
     */
    HttpRequestSingleTask(HttpMultiTaskHandler httpMultiTaskHandler, String url, String param, String mode, RequestRetCallBack callBack) {
        this.httpMultiTaskHandler = httpMultiTaskHandler;
        this.url = url;
        this.param = param;
        this.mode = mode;
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
                    httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_SUCCESS, callBack);
                } else {
                    httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_RET_NULL, callBack);// 返回数据为空，默认使用retNull函数
                }
            } else if (bib.getCode() == 1) {
                callBack.data = bib.getError();
                callBack.code = bib.getResponseCode();
                httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_ERROR, callBack);
            } else if (bib.getCode() == 2) {
                httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_IO_ERROR, callBack);
            }
        } else {
            callBack.code = -1;
            httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_ERROR, callBack);
        }
        httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_CHILD_END, callBack);// 接口全部执行完后调用
    }

    @Override
    public void async() {
        if (isStop || httpMultiTaskHandler == null) {
            return;
        }
        httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_CHILD_ENTER, callBack);// 触发任务接口

        BasicInternetRetBean bib;
        if (HttpRequest.HTTP_MODE_GET.equals(mode)) {
            bib = new HttpGet(url).execute(param);
        } else {
            bib = new HttpPost(url).execute(param);
        }
        onResult(bib);
    }
}
