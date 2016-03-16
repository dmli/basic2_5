package com.ldm.basic.conn;

import com.ldm.basic.bean.BasicInternetRetBean;

import java.util.List;

/**
 * Created by ldm on 15/10/15.
 * 用来处理多任务的网络请求
 */
public class RequestHttpMultiTask implements BasicHttpThreadTool.AsyncTask {
    private RequestSet requestSet;
    private MultiSecurityHandler handler;
    private int index;
    private boolean isStop;

    /**
     * 创建一个多任务使用的Thread
     *
     * @param requestSet 请求集合
     * @param handler    MultiSecurityHandler 更新UI
     * @param index      伪随机数
     */
    public RequestHttpMultiTask(RequestSet requestSet, MultiSecurityHandler handler, int index) {
        this.requestSet = requestSet;
        this.handler = handler;
        this.index = index;
        this.isStop = false;
    }

    @Override
    public void async() {
        if (isStop || handler == null || requestSet.getChild() == null || requestSet.getChild().size() <= 0) {
            return;
        }
        List<HttpRequestClient> cs = requestSet.getChild();
        int len = cs.size();
        for (int i = 0; i < len; i++) {
            HttpRequestClient client = cs.get(i);
            handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_CHILD_ENTER, i, 0, client));
            BasicInternetRetBean bib;
            HttpRequest request;
            if (HttpRequest.HTTP_MODE_GET.equals(client.mode)) {
                request = new BasicHttpGet(client.url);
            } else {
                request = new BasicHttpPost(client.url);
            }
            //执行网络请求
            bib = client.execute(request);
            if (bib == null) {
                client.code = -1;
                handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_ERROR, i, 0, client));
            } else {
                onResult(client, bib);
            }
            handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_CHILD_EXIT, client));
        }
        // 接口全部执行完成后该方法被执行，为带有对话框的请求关闭对话框
        handler.sendMessage(handler.obtainMessage(index, 2, 0, requestSet));
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
                handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_SUCCESS, client));// 数据处理成功
            } else {
                handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_RET_NULL, client));// 返回数据为空，默认使用retNull函数
            }
        } else if (bib.getCode() == 1) {
            client.data = bib.getError();
            client.code = bib.getResponseCode();
            handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_ERROR, client));
        } else if (bib.getCode() == 2) {
            handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_IS_NETWORK_STATE, client));
            handler.sendMessage(handler.obtainMessage(HttpEntity.RESULT_IO_ERROR, client));
        }
    }
}
