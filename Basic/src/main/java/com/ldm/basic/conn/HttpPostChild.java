package com.ldm.basic.conn;

import org.apache.http.HttpEntity;

import com.ldm.basic.bean.BasicInternetCmdBean;
import com.ldm.basic.bean.BasicInternetRetBean;
import com.ldm.basic.utils.SystemTool;

import android.content.Context;

/**
 * 批量网络请求集合的子任务块， 入参可以是String 或 HttpEntity并可以通过BasicPostHelper 设置本次ContentType
 * @author ldm 2012-4-16 
 *
 */
public class HttpPostChild {
	public RequestRetCallBack _c;
	public String _code;
	public String _url;
	public String _p;
	public HttpEntity[] entity;

	/**
	 * 构造一个HttpPostChild节点
	 *
	 * @param callBack c
	 * @param param BasicInternetCmdBean类型的数据对象
	 */
	public HttpPostChild(String param, RequestRetCallBack callBack) {
		this._p = param;
		this._c = callBack;
	}

	/**
	 * 构造一个HttpPostChild节点
	 *
	 * @param callBack c
	 * @param serviceCode 地址Code 如果code不等于NULL，内部将自动封装一层BasicInternetCmdBean | 也可传入http全地址
	 * @param param 字符串类型的参数，内部自动封装BasicInternetCmdBean实体
	 */
	public HttpPostChild(String serviceCode, String param, RequestRetCallBack callBack) {
		if (serviceCode.startsWith("http://") || serviceCode.startsWith("https://")) {
			this._url = serviceCode;
		} else {
			this._code = serviceCode;
		}
		this._p = param;
		this._c = callBack;
	}

	public HttpPostChild(RequestRetCallBack _c, HttpEntity[] entity) {
		this._c = _c;
		this.entity = entity;
	}

	/**
	 * 在UI主线程中执行网络请求,使用BasicPostHelper配置中的地址作为服务器地址
	 *
	 * @param context Context
	 * @return BasicInternetRetBean
	 */
	public BasicInternetRetBean start(final Context context) {
		return start(context, null);
	}

	/**
	 * 在UI主线程中执行网络请求,使用独立的地址
	 *
	 * @param url 地址
	 * @return BasicInternetRetBean
	 */
	public BasicInternetRetBean start(final String url) {
		return start(null, url);
	}

	/**
	 * 在UI主线程中执行网络请求
	 *
	 * @param context c
	 * @return BasicInternetRetBean
	 */
	public BasicInternetRetBean start(final Context context, final String url) {
		BasicInternetRetBean result;
		if (_p == null) {
			if (url == null) {
				result = new BasicPostHelper().http(context, _c.requestType, entity);
			} else {
				result = new BasicPostHelper().http(url, _c.requestType, entity);
			}
		} else {
			String param;
			if (_code != null) {
				BasicInternetCmdBean bcb = new BasicInternetCmdBean();
				bcb.setServiceCode(_code);
				bcb.setData(_p);
				param = SystemTool.getGson().toJson(bcb);
			} else {
				param = _p;
			}
			if (url == null) {
				result = new BasicPostHelper().http(context, _c.requestType, param);
			} else {
				result = new BasicPostHelper().http(url, _c.requestType, param);
			}
		}
		return result;
	}

	/**
	 * 取出回调接口
	 *
	 * @return RequestRetCallBack
	 */
	public RequestRetCallBack getRequestRetCallBack() {
		return _c;
	}
}
