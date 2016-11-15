package com.ldm.basic.http;

import com.ldm.basic.bean.BasicInternetRetBean;

import java.util.List;

/**
 * Created by ldm on 15/10/15.
 * 用来处理多任务的网络请求
 */
class HttpRequestMultiTask implements HttpThreadTool.AsyncTask {
    private RequestSet requestSet;
    private HttpMultiTaskHandler httpMultiTaskHandler;
    private boolean isStop;

    /**
     * 创建一个多任务使用的Thread
     *
     * @param requestSet 请求集合
     * @param httpMultiTaskHandler    HttpMultiTaskHandler 更新UI
     */
    HttpRequestMultiTask(RequestSet requestSet, HttpMultiTaskHandler httpMultiTaskHandler) {
        this.requestSet = requestSet;
        this.httpMultiTaskHandler = httpMultiTaskHandler;
        this.isStop = false;
    }

    @Override
    public void async() {
        if (isStop || httpMultiTaskHandler == null || requestSet.getChild() == null || requestSet.getChild().size() <= 0) {
            return;
        }
        List<HttpRequestClient> cs = requestSet.getChild();
        int len = cs.size();
        for (int i = 0; i < len; i++) {
            HttpRequestClient client = cs.get(i);
            httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_CHILD_ENTER, client);
            BasicInternetRetBean bib;
            HttpRequest request;
            if (HttpRequest.HTTP_MODE_GET.equals(client.mode)) {
                request = new HttpGet(client.url);
            } else {
                request = new HttpPost(client.url);
            }
            //执行网络请求
            bib = client.execute(request);
            if (bib == null) {
                client.code = -1;
                httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_ERROR, client);
            } else {
                onResult(client, bib);
            }
            httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_CHILD_END, client);
        }
        // 接口全部执行完成后该方法被执行，为带有对话框的请求关闭对话框
        httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_REQUEST_SET_EXIT, requestSet);
    }

    /**
     * 网络请求处理后的处理流程
     *
     * @param bib BasicInternetRetBean
     */
    private void onResult(HttpRequestClient client, BasicInternetRetBean bib) {
        if (bib.getCode() == 0) {
            if (bib.getSuccess() != null && !"".equals(bib.getSuccess()) && !"[]".equals(bib.getSuccess())) {
                client.data = client.asynchronous(bib.getSuccess());
                /**
                 * 数据处理成功
                 */
                httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_SUCCESS, client);
            } else {
                /**
                 * 返回数据为空，默认使用retNull函数
                 */
                httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_RET_NULL, client);
            }
        } else if (bib.getCode() == 1) {
            client.data = bib.getError();
            client.code = bib.getResponseCode();
            httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_ERROR, client);
        } else if (bib.getCode() == 2) {
            httpMultiTaskHandler.sendResponseState(HttpMultiTaskHandler.RESULT_IO_ERROR, client);
        }
    }
}
