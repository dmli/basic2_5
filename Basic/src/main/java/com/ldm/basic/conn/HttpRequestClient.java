package com.ldm.basic.conn;

import com.ldm.basic.bean.BasicInternetRetBean;

import java.util.Map;

/**
 * Created by ldm on 16/3/15.
 * 网络请求集合中的Client实体
 */
public abstract class HttpRequestClient extends RequestRetCallBack {

    public String url;
    public String mode;
    private String param;
    private Map<String, String> params;


    public HttpRequestClient(String param, String mode, String[] obj) {
        super(obj);
        this.param = param;
        this.mode = mode;
    }

    public HttpRequestClient(Map<String, String> params, String mode, String[] obj) {
        super(obj);
        this.params = params;
        this.mode = mode;
    }

    /**
     * 使用给定的HttpRequest执行网络请求
     *
     * @param request HttpRequest
     * @return BasicInternetRetBean
     */
    public final BasicInternetRetBean execute(HttpRequest request) {
        BasicInternetRetBean retBean;
        if (param != null) {
            retBean = request.execute(param);
        } else {
            retBean = request.execute(params);
        }
        return retBean;
    }

}
