package com.ldm.basic.conn;

import android.content.Context;

import com.ldm.basic.utils.SystemTool;

/**
 * Created by ldm on 15-9-25.
 * 网络请求的回调函数 RetCallBack接口中除asynchronous回调外都是同步的，
 * 如果有大数据量的处理且不需要更新UI时， 请尽量使用asynchronous方法
 * asynchronous在接口返回值处做了过滤操作，最终把自定义的返回值传递给success中的obj参数
 */
public abstract class RequestRetCallBack {
	
	public String[] _obj;// 提前存储的数据，可以在各个方法中使用的参数，通过构造方法传入
	public Object data;
	public int code;
	public int requestType = BasicHttpPost.REQUEST_HTTP;

	/**
	 * 创建回掉
	 * 
	 * @param requestType 请求类型
	 * @param obj 参数
	 */
	public RequestRetCallBack(int requestType, String[] obj) {
		this._obj = obj;
		this.requestType = requestType;
	}

	public RequestRetCallBack(String[] obj) {
		this._obj = obj;
	}

	/**
	 * 数据处理成功，该回调的参数会被asynchronous过滤，使用前请查看代码的注释
	 *
	 * @param obj Object
	 */
	public abstract void success(final Object obj);

	/**
	 * 当该任务开始处理时调用，该方法优先于start (主线程中被调用)
	 */
	public void enter() {
	}

	/**
	 * 当该任务处理完成后被调用(主线程中被调用)
	 */
	public void exit() {
	}

	/**
	 * 该方法为异步操作且仅当responseCode == 200时被触发，start后将会把数据传到该方法中，用户可以定义自己的asynchronous流程
	 *
	 * @param data 网络返回数据
	 * @return Object 将要传递给success的参数
	 */
	public Object asynchronous(String data) {
		return data;
	}

	/**
	 * 异常
	 *
	 * @param context c
	 * @param responseCode 网络返回code值（非200之外所有值）
	 * @param error String
	 */
	public void error(final Context context, final int responseCode, final String error) {
	}

	/**
	 * 接口返回空值
	 *
	 * @param context c
	 */
	public void retNull(final Context context) {
	}

	/**
	 * 网络异常
	 *
	 * @param context c
	 */
	public void ioError(final Context context) {
	}

	/**
	 * 网络是否可用 context等于null时返回false
	 *
	 * @param context c
	 * @return true可用
	 */
	public boolean isNetWorkState(final Context context) {
		boolean bool = false;
		if (context != null) {
			bool = SystemTool.getNetworkStatus(context);
		}
		return bool;
	}
}
