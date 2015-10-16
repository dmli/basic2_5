package com.ldm.basic.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.gson.Gson;
import com.ldm.basic.BasicService;
import com.ldm.basic.bean.ErrorLogBean;
import com.ldm.basic.utils.FileTool;
import com.ldm.basic.utils.Log;
import com.ldm.basic.utils.SystemTool;

import android.content.Context;

/**
 * Created by ldm on 14-6-20.
 * 客户端异常捕捉类，默认将错误信息存储到指定缓存目录中，等待上传
 */
public class BasicUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    //客户端版本
    private String appVersion;

    //客户端需要捕捉的包路径数组
    private String[] caughtPackages;

    //手机SDK版本号
    private String deviceVersion;

    //错误日志本地缓存路径
    private String userErrorLogCachePath;

    //错误日志本地缓存文件名
    private String userErrorLogCacheFileName;

    //开发者可以根据这个回调接口的反馈进行日志上传等操作
    private ErrorCallback asyncCallback;

    /**
     * @param appVersion                客户端版本
     * @param caughtPackages            客户端需要捕捉的包路径数组 如：com.ldm./ com.basic.
     * @param deviceVersion             手机SDK版本号 可以传入SystemTool.SYS_SDK_INT
     * @param userErrorLogCachePath     错误日志本地缓存路径
     * @param userErrorLogCacheFileName 错误日志本地缓存文件名
     * @param asyncCallback             开发者可以根据这个回调接口的反馈进行日志上传等操作,这个接口内的方法将会被转到BasicService中进行异步处理
     */
    public BasicUncaughtExceptionHandler(String appVersion, String[] caughtPackages, String deviceVersion, String userErrorLogCachePath, String userErrorLogCacheFileName, ErrorCallback asyncCallback) {
        this.appVersion = appVersion;
        this.caughtPackages = caughtPackages;
        this.deviceVersion = deviceVersion;
        this.userErrorLogCachePath = userErrorLogCachePath;
        this.userErrorLogCacheFileName = userErrorLogCacheFileName;
        this.asyncCallback = asyncCallback;
        if (asyncCallback != null) {
            uploadErrorLog();
        }
    }

    /**
     * 如果存在错误日志，执行上传操作
     */
    private void uploadErrorLog() {
        File f = new File(userErrorLogCachePath + "/" + userErrorLogCacheFileName);
        if (f.exists()) {
            //如果错误日志存在就执行上传操作，如果BasicService没有启动的话将无法上传，需等待BasicService启动后执行上传
            FileTool ft = new FileTool();
            String err = ft.inputStreamToString(ft.openFile(f));
            if (err != null) {
                BasicService.createAsyncTask(null, "upload_error_log", new BasicService.AsyncTaskCallback(err) {
                    @Override
                    public int asynchronous(Context context) {
                        try {
                            asyncCallback.error(String.valueOf(_obj[0]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });
            }
            if (!f.delete()) {
                Log.e("error.log 缓存文件删除失败！");
            }
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        //分析并存储异常日志，等待下一次重启时自动上传
        if (asyncCallback != null) {
        	saveErrorLog(analysisErrorInfo(ex));
		}
        //销毁所有缓存信息并结束整个进程
        SystemTool.exit(true);

    }

    /**
     * 分析错误日志
     *
     * @param ex Throwable
     * @return 返回ErrorLogBean封装好的日志信息
     */
    private String analysisErrorInfo(Throwable ex) {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append(ex.toString());
        if (ex.getStackTrace() != null) {
            sBuilder.append("  *");
            StackTraceElement[] st = ex.getStackTrace();
            for (StackTraceElement se : st) {
                boolean bool = false;
                if (se.getClassName().contains("com.ldm.basic")) {
                    bool = true;
                } else {
                    for (String s : caughtPackages) {
                        if (se.getClassName().contains(s)) {
                            bool = true;
                        }
                    }
                }
                if (bool) {
                    sBuilder.append(se.getFileName());
                    sBuilder.append(" To line ");
                    sBuilder.append(se.getLineNumber());
                    sBuilder.append(" | ");
                    sBuilder.append(se.getClassName());
                    sBuilder.append(" | Method Name ");
                    sBuilder.append(se.getMethodName());
                    sBuilder.append("()");
                }
            }
            sBuilder.append("*");
        }
        /************************************整理异常数据****************************************/
        ErrorLogBean el = new ErrorLogBean();
        el.setUserId("");
        el.setAppVersion(appVersion);
        el.setDeviceVersion(deviceVersion);
        el.setErrorLog(sBuilder.toString().length() >= 1900 ? sBuilder.toString().substring(0, 1900) : sBuilder.toString());
        return new Gson().toJson(el);
    }

    /**
     * @param log 错误日志
     */
    private void saveErrorLog(String log) {
        File f = new File(userErrorLogCachePath);
        boolean re = true;
        if (f.exists() || f.mkdirs()) {
            f = new File(userErrorLogCachePath + "/" + userErrorLogCacheFileName);
            if (!f.exists()) {
                try {
                    re = f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (re) {
                try {
                    FileOutputStream os = new FileOutputStream(f);
                    os.write(log.getBytes());
                    os.close();
                    Log.e("日志记录成功");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 当有异常信息时BasicUncaughtExceptionHandler将会通过ErrorCallback接口返回给开发者错误信息，
     * 开发者可以通过ErrorCallback中的upload方法进行日志上传
     */
    public interface ErrorCallback {

        /**
         * 开发者可以通过这个方法进行日志上传操作
         *
         * @param err 封装好的ErrorLogBean json格式
         */
        public void error(String err);
    }
}
