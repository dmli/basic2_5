package com.ldm.basic.conn;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.bean.BasicInternetRetBean;
import com.ldm.basic.utils.Log;
import com.ldm.basic.utils.SystemTool;

public class BasicDeleteHelper {
	
	/**
	 * 连接超时时间
	 */
	protected static int TIME_OUT = 1000 * 10;

	/**
	 * 读取超时时间
	 */
	protected static int SO_TIME_OUT = 1000 * 25;

	/**
	 * GET方式访问接口
	 *
	 * @param url 地址
	 * @return BasicInternetRetBean
	 */
	public BasicInternetRetBean http(final String url) {
		BasicInternetRetBean result = new BasicInternetRetBean();
		try {
			HttpClient httpClient = getHttpClient(TIME_OUT, SO_TIME_OUT);
			UrlData ud = analysisHeaderUrl(url);
			HttpDelete delete = new HttpDelete(ud.url);
			/************* 写入Header数据**************/
			if (ud.headers != null && ud.headers.size() > 0) {
				for (BasicDeleteHelper.Header header : ud.headers) {
					delete.setHeader(header.getKey(), header.getValue());
				}
			}
			HttpResponse response = httpClient.execute(delete);
			int code = response.getStatusLine().getStatusCode();
			String data = null;
			if (code == 200) {
				HttpEntity resultEntity = response.getEntity();
				data = EntityUtils.toString(resultEntity, HTTP.UTF_8);
				result.setCode(0);
				result.setSuccess(data);
			} else {
				data = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
				result.setError(data);
			}
			if (BasicApplication.IS_DEBUG) {
				Log.e("DELETE", "出参  = " + data);
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

	/**
	 * 创建一个HttpClient 使用给定的超时时间
	 *
	 * @param connection_time_out 连接超时
	 * @param so_time_out 读取超时
	 * @return HttpClient
	 */
	protected static HttpClient getHttpClient(int connection_time_out, int so_time_out) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connection_time_out);
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, so_time_out);
		return httpClient;
	}

	/**
	 * 生成一个支持传输Header的url
	 *
	 * @param orgUrl 原始地址
	 * @param headers Header
	 * @return new url
	 */
	public static String genHeaderUrl(String orgUrl, Header... headers) {
		if (orgUrl == null || headers == null || headers.length <= 0) {
			return orgUrl;
		}
		String ss = "<<-header{";
		ss += SystemTool.getGson().toJson(headers);
		ss += "}header->>";
		return orgUrl + ss;
	}

	/**
	 * 将一个支持Header协议的url反解析
	 *
	 * @param url 地址
	 * @return UrlData
	 */
	public static UrlData analysisHeaderUrl(String url) {
		UrlData ud = new UrlData();
		if (url == null || !url.contains("<<-header{")) {
			ud.url = url;
			return ud;
		}
		String _url = url.substring(0, url.indexOf("<<-header{"));
		ud.url = _url;
		// ---解析headers---
		Type retList = new TypeToken<List<Header>>() {
		}.getType();
		try {
			ud.headers = SystemTool.getGson().fromJson(url.substring(url.indexOf("<<-header{") + 10, url.indexOf("}header->>")), retList);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		}
		return ud;
	}

	/**
	 * GET方式访问接口,如果接口请求失败，将返回null
	 *
	 * @param url 地址
	 * @return 接口内容
	 */
	public String http2(final String url) {
		try {
			HttpClient httpClient = getHttpClient(TIME_OUT, SO_TIME_OUT);
			HttpDelete get = new HttpDelete(url);
			HttpResponse response = httpClient.execute(get);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				HttpEntity entity = response.getEntity();
				String data = EntityUtils.toString(entity, HTTP.UTF_8);
				if (BasicApplication.IS_DEBUG) {
					Log.e("DELETE", "出参  = " + data);
				}
				return data;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 调用网络接口，不接收返回数据
	 *
	 * @param url 地址
	 */
	public void sendGetRequest(final String url) {
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpDelete get = new HttpDelete(url);
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
	 * 验证网络文件是否有效， 当返回状态200/206时表示有效
	 *
	 * @param url 地址
	 * @return true有效
	 */
	public boolean isValidity(final String url) {
		boolean bool = false;
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpDelete get = new HttpDelete(url);
			HttpResponse response = httpClient.execute(get);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				bool = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bool;
	}

	public static class Header {
		private String key;
		private String value;

		public Header() {
		}

		public Header(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class UrlData {
		public String url;
		public List<Header> headers;

	}

}
