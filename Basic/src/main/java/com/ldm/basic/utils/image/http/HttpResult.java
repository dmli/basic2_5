package com.ldm.basic.utils.image.http;

/**
 * Created by ldm on 16/5/31.
 * 网络请求结果
 */
public class HttpResult {

    /**
     * http请求状态
     */
    public int responseCode;

    /**
     * 0 请求失败
     * 1 请求成功
     */
    public int code = 0;

    /**
     * 下载成功时返回本地文件路径
     */
    public String localFilePath;

    /**
     * code=0时error中存储错误信息
     */
    public String error;

    /**
     * url 是否为空
     *
     * @return true/false
     */
    public boolean isUrlNull() {
        return "url is null".equals(error);
    }

    public static HttpResult buildUrlIsNull() {
        HttpResult result = new HttpResult();
        result.code = -1;
        result.error = "url is null";
        return result;
    }

    public void addHttpResult(HttpResult result) {
        this.code = result.code;
        this.error = result.error;
        this.responseCode = result.responseCode;
        this.localFilePath = result.localFilePath;
    }

}
