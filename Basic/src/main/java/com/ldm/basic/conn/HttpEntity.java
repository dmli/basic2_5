package com.ldm.basic.conn;

import android.content.Context;

import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.dialog.LDialog;
import com.ldm.basic.utils.MD5;
import com.ldm.basic.utils.SystemTool;

/**
 * Created by ldm on 12-4-11.
 * 基于BasicPostHelper与Thread封装的一个网络工具，使用时请详细阅读代码注释
 */
public class HttpEntity {

    /**
     * 成功
     */
    public static final int RESULT_SUCCESS = 200;

    /**
     * 失败error 附带错误信息
     */
    public static final int RESULT_ERROR = 100;

    /**
     * 返回数据为空
     */
    public static final int RESULT_RET_NULL = 101;

    /**
     * IO error 网络连接异常
     */
    public static final int RESULT_IO_ERROR = 102;

    /**
     * IS NETWORK STATE
     */
    public static final int RESULT_IS_NETWORK_STATE = 103;

    /**
     * 任务进入
     */
    public static final int RESULT_CHILD_ENTER = 110;

    /**
     * 任务退出
     */
    public static final int RESULT_CHILD_EXIT = 111;

    /**
     * 在独立的线程中运行网络请求，当msg不等于null时附带提示框
     *
     * @param context  上下文
     * @param url      URL地址
     * @param callBack 回调
     */
    public static void httpGet(final Context context, final String url, final RequestRetCallBack callBack) {
        // 利用伪随机数控制多线程时 Handler接收错误的数据使用
        int index = (int) (Math.random() * 1000000);
        // 创建Thread
        RequestHttpSingleTask task = new RequestHttpSingleTask(new MultiSecurityHandler(context, index), url, null, HttpRequest.HTTP_MODE_GET, index, callBack);
        //使用MD5根据URL创建一个标记，用来判断任务的有效性
        String tag = MD5.md5(url);
        BasicHttpThreadTool.addTask(tag, task);
    }

    /**
     * 在独立的线程中运行网络请求，当msg不等于null时附带提示框
     *
     * @param context  Context
     * @param param    HttpEntity[]
     * @param callBack RequestRetCallBack
     */
    private static void httpPost(final Context context, final String url, final String param, final RequestRetCallBack callBack) {
        // 利用伪随机数控制多线程时 Handler接收错误的数据使用
        int index = (int) (Math.random() * 1000000);
        // 创建Thread
        RequestHttpSingleTask task = new RequestHttpSingleTask(new MultiSecurityHandler(context, index), url, param, HttpRequest.HTTP_MODE_POST, index, callBack);
        //使用MD5根据URL创建一个标记，用来判断任务的有效性
        String tag = MD5.md5(url);
        BasicHttpThreadTool.addTask(tag, task);
    }

    /**
     * 网络集合使用（该方法内部所有子节点（RequestSet）的回调方法除asynchronous外皆为同步操作， 如果有大数据量操作切不需要更新UI时尽量用asynchronous处理）
     *
     * @param context    c
     * @param tag        tag 网络请求的标记，用来判断任务的有效性
     * @param requestSet 网络集合
     */
    public static void httpPostSet(final Context context, final String tag, final RequestSet requestSet) {
        if (!SystemTool.isNetworkAvailable(context)) {
            LDialog.netWorkErrDialog(context, BasicApplication.CONSTANTS.NET_WORKERROR0);
            requestSet.ioError();// 网络异常
        } else {
            requestSet.enter();// 本次网络请求进入初始化状态
            // 利用伪随机数控制多线程时 Handler接收错误的数据使用
            int index = (int) (Math.random() * 1000000);
            RequestHttpMultiTask task = new RequestHttpMultiTask(requestSet, new MultiSecurityHandler(context, index), index);
            BasicHttpThreadTool.addTask(tag, task);
        }
    }

}
