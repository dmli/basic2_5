package com.ldm.basic.bean;

/**
 * Created by ldm on 12-4-11.
 * 网络请求使用的传输类
 */
public class BasicInternetRetBean {

    /**
     * 0成功
     * 1无法处理
     * 2网络异常，可先测试本地网络情况后在确定错误信息
     * 默认为1
     */
    private int code = 1;

    /**
     * 失败的错误信息
     */
    private String error;

    /**
     * 成功返回数据
     */
    private Object success;

    /**
     * 网络返回的code值
     */
    private int responseCode = 200;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getSuccess() {
        return String.valueOf(success);
    }

    public Object getSuccessToObject() {
        return success;
    }

    public void setSuccess(Object success) {
        this.success = success;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
