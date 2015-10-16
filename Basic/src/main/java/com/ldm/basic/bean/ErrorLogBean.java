package com.ldm.basic.bean;

/**
 * Created by ldm on 14-6-20.
 * 异常日志bean,客户端存储异常日志的默认格式
 */
public class ErrorLogBean {

    private String appVersion;//客户端版本
    private String userId;//用户ID
    private String deviceOS = "Android";//设备系统
    private String deviceVersion;//设备版本
    private String errorLog;//错误日志

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceOS() {
        return deviceOS;
    }

    public void setDeviceOS(String deviceOS) {
        this.deviceOS = deviceOS;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public String getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog;
    }
}
