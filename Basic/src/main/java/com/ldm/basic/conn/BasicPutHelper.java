package com.ldm.basic.conn;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.bean.BasicInternetRetBean;
import com.ldm.basic.utils.Log;

public class BasicPutHelper {

	/**
	 * 连接超时时间
	 */
	protected static int TIME_OUT = 1000 * 10;

	/**
	 * 读取超时时间
	 */
	protected static int SO_TIME_OUT = 1000 * 25;

	/**
	 * 默认的网络交互数据格式
	 */
	protected static String CONTENT_DEFAULT_TYPE = BasicPostHelper.CONTENT_TYPE_JSON;

	/**
	 * 网络交互数据类型,临时的，默认使用CONTENT_DEFAULT_TYPE
	 */
	protected static String CONTENT_TYPE;

	/**
	 * 调用网络接口，不接收返回数据
	 * 
	 * @param url 地址
	 */
	public void sendPutRequest(final String url, final int type) {
		try {
			HttpClient httpClient = getThreadSafeHttpClient(100, type);
			HttpPut get = new HttpPut(url);
			HttpResponse response = httpClient.execute(get);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				Log.e("sendGetRequest 发送成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建支持多线程的HttpClient
	 * @param maxTotal 最大线程数
	 * @param type 请求类型
	 * @return HttpClient
	 */
	protected static HttpClient getThreadSafeHttpClient(int maxTotal, int type) {
		HttpParams params = new BasicHttpParams();
		// 版本
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		// 编码
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		// Activates 'Expect: 100-continue' handshake for the entity enclosing
		// methods.
		HttpProtocolParams.setUseExpectContinue(params, true);
		// 最大连接数
		ConnManagerParams.setMaxTotalConnections(params, maxTotal);
		// 超时
		HttpConnectionParams.setConnectionTimeout(params, TIME_OUT);
		HttpConnectionParams.setSoTimeout(params, SO_TIME_OUT);
		// 计划注册,可以注册多个计划
		SchemeRegistry schReg = new SchemeRegistry();
		// 请求类型
		if (BasicPostHelper.REQUEST_HTTP == type) {
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		} else {
			/**
			 * 请求类型默认兼容所有证书
			 */
			schReg.register(new Scheme("https", new AllSSLSocketFactory(), 443));
			schReg.register(new Scheme("https", new AllSSLSocketFactory(), 8443));
		}
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
		return new DefaultHttpClient(conMgr, params);
	}

	
	public BasicInternetRetBean http(final String url, HttpEntity... param){
		return http(url, BasicPostHelper.REQUEST_HTTP, param);
	}
	
	/**
	 * 向服务器发送一个put请求
	 * 
	 * @param url 地址
	 * @param param 参数
	 * @return BasicInternetRetBean
	 */
	public BasicInternetRetBean http(final String url, final int type, HttpEntity... param) {
		BasicInternetRetBean result = new BasicInternetRetBean();
		try {
			HttpClient client = getThreadSafeHttpClient(100, type);
			HttpPut httpPut = new HttpPut(url);
			httpPut.addHeader("Content-Type", CONTENT_TYPE == null ? CONTENT_DEFAULT_TYPE : CONTENT_TYPE);
			CONTENT_TYPE = null;
			/************* 向服务器写入数据 **************/
			for (HttpEntity httpEntity : param) {
				httpPut.setEntity(httpEntity);
			}
			/************* 开始执行命令 ******************/
			HttpResponse response = client.execute(httpPut);

			/*************** 接收状态 **************/
			int code = response.getStatusLine().getStatusCode();
			String data;
			if (code == 200) {
				HttpEntity entity = response.getEntity();
				data = EntityUtils.toString(entity, HTTP.UTF_8);
				result.setCode(0);
				result.setSuccess(data);
			} else {
				data = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
				result.setError(data);
			}
			if (BasicApplication.IS_DEBUG) {
				Log.e("PUT", "出参  = " + data);
			}
			result.setResponseCode(code);
		} catch (IOException e) {
			e.printStackTrace();
			result.setCode(2);
			result.setError("网络交互失败，请检查本地网络！");
		} catch (Exception e) {
			e.printStackTrace();
			result.setError("网络交互失败，请检查本地网络！");
		}
		return result;
	}

	public static String getContentType() {
		return CONTENT_TYPE;
	}

	public static void setContentType(String contentType) {
		CONTENT_TYPE = contentType;
	}

	public static String getContentDaultType() {
		return CONTENT_DEFAULT_TYPE;
	}

	public static void setContentDaultType(String contentDaultType) {
		CONTENT_DEFAULT_TYPE = contentDaultType;
	}
}
