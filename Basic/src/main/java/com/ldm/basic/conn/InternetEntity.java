package com.ldm.basic.conn;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.dialog.Dialog;
import com.ldm.basic.utils.SystemTool;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

/**
 * Created by ldm on 12-4-11. 基于BasicPostHelper与Thread封装的一个网络工具，使用时请详细阅读代码注释
 */
public class InternetEntity {

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
	 * POST模式访问接口
	 */
	public static final String HTTP_MODE_POST = "POST";

	/**
	 * GET模式访问接口
	 */
	public static final String HTTP_MODE_GET = "GET";

	/**
	 * DELETE模式访问接口
	 */
	public static final String HTTP_MODE_DELETE = "DELETE";

	/**
	 * PUT模式访问接口 （仅提供单一的调用方法，这个模式不太常用）
	 */
	public static final String HTTP_MODE_PUT = "PUT";

	/**
	 * 网络提示
	 */
	static ProgressDialog dialog0;

	/**
	 * 在独立的线程中运行网络请求，有提示框
	 *
	 * @param context Context
	 * @param callBack RequestRetCallBack
	 * @param param HttpEntity[]
	 */
	public static void conn(final Context context, String msg, final RequestRetCallBack callBack, final String param) {
		if (isNetWorkState(context)) {
			try {
				httpPost(context, msg, callBack, new StringEntity(param, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				callBack.error(context, -1, "数据编码转换失败，无法上传数据！");
				callBack.exit();
			}
		} else {
			callBack.ioError(context);
			callBack.exit();
		}
	}

	/**
	 * 在独立的线程中运行网络请求，有提示框
	 *
	 * @param context Context
	 * @param callBack RequestRetCallBack
	 * @param param HttpEntity[]
	 */
	public static void conn(final Context context, String msg, final RequestRetCallBack callBack, final HttpEntity... param) {
		if (isNetWorkState(context)) {
			httpPost(context, msg, callBack, param);
		} else {
			callBack.ioError(context);
			callBack.exit();
		}
	}

	/**
	 * 在独立的线程中运行网络请求，没有提示框
	 *
	 * @param context Context
	 * @param callBack RequestRetCallBack
	 * @param param HttpEntity[]
	 */
	public static void conn(final Context context, final RequestRetCallBack callBack, final HttpEntity... param) {
		if (isNetWorkState(context)) {
			httpPost(context, null, callBack, param);
		} else {
			callBack.ioError(context);
			callBack.exit();
		}
	}

	/**
	 * 在独立的线程中运行网络请求，有提示框
	 *
	 * @param context Context
	 * @param strParam 入参字符串
	 * @param callBack RequestRetCallBack
	 */
	public static void conn(final Context context, String msgs, final String strParam, final RequestRetCallBack callBack) {
		if (isNetWorkState(context)) {
			try {
				httpPost(context, msgs, callBack, new StringEntity(strParam, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				callBack.error(context, -1, "字符编码转换失败，请与管理人员联系！");
				callBack.exit();
				e.printStackTrace();
			}
		} else {
			callBack.ioError(context);
			callBack.exit();
		}
	}

	/**
	 * 在独立的线程中运行网络请求，没有提示框
	 *
	 * @param context Context
	 * @param strParam 入参字符串
	 * @param callBack RequestRetCallBack
	 */
	public static void conn(final Context context, final String strParam, final RequestRetCallBack callBack) {
		if (isNetWorkState(context)) {
			try {
				httpPost(context, null, callBack, new StringEntity(strParam, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				callBack.error(context, -1, "字符编码转换失败，请与管理人员联系！");
				callBack.exit();
				e.printStackTrace();
			}
		} else {
			callBack.ioError(context);
			callBack.exit();
		}
	}

	/**
	 * 在独立的线程中运行网络请求，有提示框
	 *
	 * @param url 地址
	 * @param context Context
	 * @param callBack RequestRetCallBack
	 * @param param HttpEntity[]
	 */
	public static void conn(final String url, final Context context, final String msg, final RequestRetCallBack callBack, final String param) {
		if (isNetWorkState(context)) {
			try {
				httpPost(context, url, msg, callBack, new StringEntity(param, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				callBack.exit();
				callBack.error(context, -1, "数据编码转换失败，无法上传数据！");
			}
		} else {
			callBack.ioError(context);
			callBack.exit();
		}
	}

	/**
	 * 在独立的线程中运行网络请求，有提示框
	 *
	 * @param url 地址
	 * @param context Context
	 * @param callBack RequestRetCallBack
	 * @param param HttpEntity[]
	 */
	public static void conn(final String url, final Context context, String msg, final RequestRetCallBack callBack, final HttpEntity... param) {
		if (isNetWorkState(context)) {
			httpPost(context, url, msg, callBack, param);
		} else {
			callBack.ioError(context);
			callBack.exit();
		}
	}

	/**
	 * 在独立的线程中运行网络请求，没有提示框
	 *
	 * @param url 地址
	 * @param context Context
	 * @param callBack RequestRetCallBack
	 * @param param HttpEntity[]
	 */
	public static void conn(final String url, final Context context, final RequestRetCallBack callBack, final HttpEntity... param) {
		if (isNetWorkState(context)) {
			httpPost(context, url, null, callBack, param);
		} else {
			callBack.ioError(context);
			callBack.exit();
		}
	}

	/**
	 * 在独立的线程中运行网络请求，有提示框
	 *
	 * @param url 地址
	 * @param context Context
	 * @param strParam 入参字符串
	 * @param callBack RequestRetCallBack
	 */
	public static void conn(final String url, final Context context, String msgs, final String strParam, final RequestRetCallBack callBack) {
		if (isNetWorkState(context)) {
			try {
				httpPost(context, msgs, url, callBack, new StringEntity(strParam, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				callBack.error(context, -1, "字符编码转换失败！");
				callBack.exit();
				e.printStackTrace();
			}
		} else {
			callBack.ioError(context);
			callBack.exit();
		}
	}

	/**
	 * 在独立的线程中运行网络请求，没有提示框
	 *
	 * @param context Context
	 * @param strParam 入参字符串
	 * @param callBack RequestRetCallBack
	 */
	public static void conn(final String url, final Context context, final String strParam, final RequestRetCallBack callBack) {
		if (isNetWorkState(context)) {
			try {
				httpPost(context, url, null, callBack, new StringEntity(strParam, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				callBack.error(context, -1, "字符编码转换失败！");
				callBack.exit();
				e.printStackTrace();
			}
		} else {
			callBack.ioError(context);
			callBack.exit();
		}
	}

	/**
	 * 检查网络连接状况
	 *
	 * @param context Context
	 * @return true可用
	 */
	private static boolean isNetWorkState(final Context context) {
		return SystemTool.getNetworkStatus(context);
	}

	/**
	 * 在独立的线程中运行网络请求，没有提示框
	 *
	 * @param context 上下文
	 * @param url URL地址
	 * @param callBack 回调
	 */
	public static void httpGet(final Context context, final String url, final RequestRetCallBack callBack) {
		httpGet(context, null, url, callBack);
	}

	/**
	 * 在独立的线程中运行网络请求，当msg不等于null时附带提示框
	 *
	 * @param context 上下文
	 * @param msg 操作提示，当msg不等于null进行提示
	 * @param url URL地址
	 * @param callBack 回调
	 */
	public static void httpGet(final Context context, String msg, final String url, final RequestRetCallBack callBack) {
		// 利用伪随机数控制多线程时 Handler接收错误的数据使用
		int index = (int) (Math.random() * 1000000);

		// 初始Thread
		RequestSingleThread thread = new RequestSingleThread(context, new MultiSecurityHandler<Context>(context, index), url, null, HTTP_MODE_GET, index, callBack);

		if (msg != null) {
			dialogShow(context, msg, thread);
		}

		thread.start();
	}

	/**
	 * 在独立的线程中运行网络请求，没有提示框
	 *
	 * @param context 上下文
	 * @param url URL地址
	 * @param callBack 回调
	 */
	public static void httpDelete(final Context context, final String url, final RequestRetCallBack callBack) {
		httpDelete(context, null, url, callBack);
	}

	/**
	 * 在独立的线程中运行网络请求，当msg不等于null时附带提示框
	 *
	 * @param context 上下文
	 * @param msg 操作提示，当msg不等于null进行提示
	 * @param url URL地址
	 * @param callBack 回调
	 */
	public static void httpDelete(final Context context, String msg, final String url, final RequestRetCallBack callBack) {
		// 利用伪随机数控制多线程时 Handler接收错误的数据使用
		int index = (int) (Math.random() * 1000000);

		// 初始Thread
		RequestSingleThread thread = new RequestSingleThread(context, new MultiSecurityHandler<Context>(context, index), url, null, HTTP_MODE_DELETE, index, callBack);

		if (msg != null) {
			dialogShow(context, msg, thread);
		}

		thread.start();
	}

	/**
	 * 在独立的线程中运行网络请求，没有提示框
	 *
	 * @param context Context
	 * @param url 地址
	 * @param param 参数
	 * @param callBack RequestRetCallBack
	 */
	public static void httpPut(final Context context, final String url, final String param, final RequestRetCallBack callBack) {
		StringEntity se;
		try {
			se = new StringEntity(param, HTTP.UTF_8);
			httpPut(context, null, url, callBack, se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			callBack.error(context, -1, "字符编码失败！");
		}
	}

	/**
	 * 在独立的线程中运行网络请求，没有提示框
	 *
	 * @param context Context
	 * @param url 地址
	 * @param callBack RequestRetCallBack
	 * @param param Map<String, String>
	 */
	public static void httpPut(final Context context, final String url, final RequestRetCallBack callBack, final Map<String, String> param) {
		StringEntity se;
		try {
			se = new StringEntity(SystemTool.getGson().toJson(param), HTTP.UTF_8);
			httpPut(context, null, url, callBack, se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			callBack.error(context, -1, "字符编码失败！");
		}
	}

	/**
	 * 在独立的线程中运行网络请求，没有提示框
	 *
	 * @param context 上下文
	 * @param url URL地址
	 * @param callBack 回调
	 * @param param HttpEntity...
	 */
	public static void httpPut(final Context context, final String url, final RequestRetCallBack callBack, final HttpEntity... param) {
		httpPut(context, null, url, callBack, param);
	}

	/**
	 * 在独立的线程中运行网络请求，当msg不等于null时附带提示框
	 *
	 * @param context 上下文
	 * @param msg 操作提示，当msg不等于null进行提示
	 * @param url URL地址
	 * @param callBack 回调
	 */
	public static void httpPut(final Context context, String msg, final String url, final RequestRetCallBack callBack, final HttpEntity... param) {
		// 利用伪随机数控制多线程时 Handler接收错误的数据使用
		int index = (int) (Math.random() * 1000000);

		// 初始Thread
		RequestSingleThread thread = new RequestSingleThread(context, new MultiSecurityHandler<Context>(context, index), url, param, HTTP_MODE_PUT, index, callBack);

		if (msg != null) {
			dialogShow(context, msg, thread);
		}

		thread.start();
	}

	/**
	 * 在独立的线程中运行网络请求，当msg不等于null时附带提示框
	 *
	 * @param context Context
	 * @param msg 提示语
	 * @param param HttpEntity[]
	 * @param callBack RequestRetCallBack
	 */
	private static void httpPost(final Context context, final String url, String msg, final RequestRetCallBack callBack, final HttpEntity... param) {
		// 利用伪随机数控制多线程时 Handler接收错误的数据使用
		int index = (int) (Math.random() * 1000000);

		RequestSingleThread thread = new RequestSingleThread(context, new MultiSecurityHandler<Context>(context, index), url, param, HTTP_MODE_POST, index, callBack);

		if (msg != null) {
			dialogShow(context, msg, thread);
		}

		thread.start();
	}

	/**
	 * 在独立的线程中运行网络请求，当msg不等于null时附带提示框
	 *
	 * @param context Context
	 * @param msg 提示语
	 * @param param HttpEntity[]
	 * @param callBack RequestRetCallBack
	 */
	private static void httpPost(final Context context, String msg, final RequestRetCallBack callBack, final HttpEntity... param) {
		// 利用伪随机数控制多线程时 Handler接收错误的数据使用
		int index = (int) (Math.random() * 1000000);

		RequestSingleThread thread = new RequestSingleThread(context, new MultiSecurityHandler<Context>(context, index), null, param, HTTP_MODE_POST, index, callBack);

		if (msg != null) {
			dialogShow(context, msg, thread);
		}

		thread.start();
	}

	/**
	 * 网络集合使用（该方法内部所有子节点（RequestSet）的回调方法除asynchronous外皆为同步操作， 如果有大数据量操作切不需要更新UI时尽量用asynchronous处理）
	 *
	 * @param context c
	 * @param _msg 提示语
	 * @param requestSet 网络集合
	 */
	public static void httpPostSet(final Context context, String _msg, final RequestSet requestSet) {
		if (!SystemTool.getNetworkStatus(context)) {
			Dialog.netWorkErrDialog(context, BasicApplication.CONSTANTS.NET_WORKERROR0);
			requestSet.ioError();// 网络异常
		} else {
			requestSet.enter();// 本次网络请求进入初始化状态
			// 利用伪随机数控制多线程时 Handler接收错误的数据使用
			int index = (int) (Math.random() * 1000000);
			RequestMultiThread thread = new RequestMultiThread(context, requestSet, new MultiSecurityHandler<Context>(context, index), index);
			if (_msg != null) {
				dialogShow(context, _msg, thread);
			}
			thread.start();
		}
	}

	/**
	 * 创建一个ProgressDialog并显示
	 *
	 * @param context c
	 * @param msg 提示语
	 * @param listener OnClickListener
	 */
	private static void dialogShow(final Context context, String msg, OnClickListener listener) {
		dialog0 = new ProgressDialog(context);
		dialog0.setMessage(msg);
		dialog0.setIndeterminate(true);
		dialog0.setButton(ProgressDialog.BUTTON_NEUTRAL, "取消", listener);
		dialog0.setCanceledOnTouchOutside(false);
		dialog0.show();
	}

}
