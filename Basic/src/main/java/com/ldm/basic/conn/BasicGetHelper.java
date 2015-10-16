package com.ldm.basic.conn;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.bean.BasicInternetRetBean;
import com.ldm.basic.utils.Log;
import com.ldm.basic.utils.SystemTool;

/**
 * Created by ldm on 13-6-6.
 * GET请求助手，一个简单的get操作类
 */
public class BasicGetHelper {

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
        return http(url, BasicPostHelper.REQUEST_HTTP);
    }

    /**
     * GET方式访问接口
     *
     * @param url  地址
     * @param type 请求类型
     * @return BasicInternetRetBean
     */
    public BasicInternetRetBean http(final String url, final int type) {
        BasicInternetRetBean result = new BasicInternetRetBean();
        try {
            HttpClient httpClient = getThreadSafeHttpClient(100, type);
            UrlData ud = analysisHeaderUrl(url);
            HttpGet get = new HttpGet(ud.url);
            /************* 写入Header数据 **************/
            if (ud.headers != null && ud.headers.size() > 0) {
                for (BasicGetHelper.Header header : ud.headers) {
                    get.setHeader(header.getKey(), header.getValue());
                }
            }
            HttpResponse response = httpClient.execute(get);
            int code = response.getStatusLine().getStatusCode();
            String data = null;
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
                Log.e("GET", "出参  = " + data);
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

    /**
     * 生成一个支持传输Header的url
     *
     * @param orgUrl  原始地址
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
        return http2(url, BasicPostHelper.REQUEST_HTTP);
    }

    /**
     * GET方式访问接口,如果接口请求失败，将返回null
     *
     * @param type 请求类型
     * @param url  地址
     * @return 接口内容
     */
    public String http2(final String url, final int type) {
        try {
            HttpClient httpClient = getThreadSafeHttpClient(100, type);
            UrlData ud = analysisHeaderUrl(url);
            HttpGet get = new HttpGet(ud.url);
            /************* 写入Header数据 **************/
            if (ud.headers != null && ud.headers.size() > 0) {
                for (BasicGetHelper.Header header : ud.headers) {
                    get.setHeader(header.getKey(), header.getValue());
                }
            }
            HttpResponse response = httpClient.execute(get);
            int code = response.getStatusLine().getStatusCode();
            String data;
            if (code == 200) {
                HttpEntity entity = response.getEntity();
                data = EntityUtils.toString(entity, HTTP.UTF_8);
                if (BasicApplication.IS_DEBUG) {
                    Log.e("GET", "出参  = " + data);
                }
                return data;
            } else {
                HttpEntity entity = response.getEntity();
                data = EntityUtils.toString(entity, HTTP.UTF_8);
                if (BasicApplication.IS_DEBUG) {
                    Log.e("GET", "出参  = " + data);
                }
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
            HttpGet get = new HttpGet(url);
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
            HttpGet get = new HttpGet(url);
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
