package com.ldm.basic.conn;

import com.ldm.basic.bean.BasicInternetRetBean;

import java.util.Map;

/**
 * Created by ldm on 16/3/15.
 * 网络请求接口
 */
public abstract class HttpRequest {

    /**
     * POST模式访问接口
     */
    public static final String HTTP_MODE_POST = "POST";

    /**
     * GET模式访问接口
     */
    public static final String HTTP_MODE_GET = "GET";

    /**
     * 网络请求类型 HTTP
     */
    public static final int REQUEST_HTTP = 0;

    /**
     * 网络请求类型 HTTPS
     */
    public static final int REQUEST_HTTPS = 1;

    /**
     * 连接超时时间
     */
    public static final int TIME_OUT = 1000 * 10;

    /**
     * 读取超时时间
     */
    public static final int SO_TIME_OUT = 1000 * 60;

    /**
     * 浏览器默认的Content-Type类型
     */
    public static final String CONTENT_TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded";

    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String CONTENT_TYPE_TEXT_TYPE = "text/html; charset=utf-8";

    public static final String CONTENT_TYPE_TEXT_PLAIN_TYPE = "text/plain; charset=utf-8";

    /**
     * 网络交互数据类型,临时的，默认使用CONTENT_DEFAULT_TYPE
     */
    protected static String CONTENT_TYPE = null;

    /**
     * 默认的网络交互数据格式
     */
    protected static String CONTENT_DEFAULT_TYPE = CONTENT_TYPE_JSON;

    /**
     * 请求人
     */
    public static String ACCEPT_TYPE = null;

    /**
     * 网络请求对应的URL
     */
    protected String url;

    /**
     * 创建一个HttpRequest实体
     *
     * @param url 网络请求对应的URL
     */
    public HttpRequest(String url) {
        this.url = url;
    }

    abstract BasicInternetRetBean execute(String param);

    abstract BasicInternetRetBean execute(Map<String, String> params);

    /**
     * 返回这个实体使用的URL
     *
     * @return URL
     */
    public String getUrl() {
        return url;
    }
}
