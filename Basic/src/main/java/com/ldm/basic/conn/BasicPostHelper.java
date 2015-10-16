package com.ldm.basic.conn;

import android.content.Context;

import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.bean.BasicInternetRetBean;
import com.ldm.basic.properties.PropertiesHelper;
import com.ldm.basic.utils.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Created by ldm on 12-4-11. 基础的网络服务类
 */
public class BasicPostHelper {

    /**
     * 此做法需要保证没有变化 实例化并保证唯一连接的HttpClient， 此链接是不被释放的直到 URL发生变化 或 APP出现异常 或 被强行终止的情况下
     */
    private static HttpClient CLIENT;

    /**
     * 网络交互默认编码
     */
    protected static String CHARSET = HTTP.UTF_8;

    // 主机服务地址
    private static String HTTP_URL = "";

    // 临时主机服务地址
    private static String HTTP_URL2 = null;

    // 代理主机
    public static String HTTP_HOST;

    // 代理端口
    public static int HTTP_PORT;

    /**
     * 连接超时时间
     */
    protected static int TIME_OUT = 1000 * 10;

    /**
     * 读取超时时间
     */
    protected static int SO_TIME_OUT = 1000 * 25;

    /**
     * 全名service.properties 默认的配置文件，当内存出现问题时 尝试读取配置文件中的备用信息
     */
    private static final String DEF_SERVICE_FILE_NAME = "service";

    /**
     * 对应url的KEY
     */
    private static final String DEF_SERVICE_URL_KEY = "SERVICE_URL";

    /**
     * 浏览器默认的Content-Type类型
     */
    public static final String CONTENT_TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded";

    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String CONTENT_TYPE_TEXT_TYPE = "text/html; charset=utf-8";

    public static final String CONTENT_TYPE_TEXT_PLAIN_TYPE = "text/plain; charset=utf-8";

    /**
     * 网络交互数据类型,临时的，默认使用CONTENT_DEFAULT_TYPE
     */
    protected static String CONTENT_TYPE;

    public static final int REQUEST_HTTP = 0;
    public static final int REQUEST_HTTPS = 1;

    /**
     * 默认的网络交互数据格式
     */
    protected static String CONTENT_DEFAULT_TYPE = CONTENT_TYPE_JSON;

    /**
     * HTTP POST方式请求网络，返回值将封装到BasicInternetRetBean中
     *
     * @param context 上下文
     * @param content 数据
     * @return BaseInternetBean实体 详细见 BaseInternetBean中注释
     */
    public BasicInternetRetBean http(Context context, int type, String content) {
        return http(getUrl(context), type, content);
    }

    public BasicInternetRetBean http(String url, String content) {
        return http(url, REQUEST_HTTP, content);
    }


    /**
     * HTTP POST方式请求网络，返回值将封装到BasicInternetRetBean中
     *
     * @param url     服务器地址
     * @param content 数据
     * @return BaseInternetBean实体 详细见 BaseInternetBean中注释
     */
    public BasicInternetRetBean http(String url, int type, String content) {
        BasicInternetRetBean err;
        try {
            return http(url, type, new StringEntity(content, CHARSET));
        } catch (UnsupportedEncodingException e) {
            err = new BasicInternetRetBean();
            err.setError("字符编码转换失败，请与管理人员联系！");
            e.printStackTrace();
        }
        return err;
    }

    /**
     * HTTP POST方式请求网络，返回值将封装到BasicInternetRetBean中
     *
     * @param context 上下文
     * @param entity  HttpEntity集合
     * @return BaseInternetBean实体 详细见 BaseInternetBean中注释
     */
    public BasicInternetRetBean http(Context context, int type, HttpEntity... entity) {
        return http(getUrl(context), type, entity);
    }

    /**
     * HTTP POST方式请求网络，返回值将封装到BasicInternetRetBean中
     *
     * @param url    地址
     * @param entity HttpEntity集合
     * @return BaseInternetBean实体 详细见 BaseInternetBean中注释
     */
    public BasicInternetRetBean http(String url, int type, HttpEntity... entity) {
        BasicInternetRetBean result = new BasicInternetRetBean();
        HttpPost httpPost;
        try {
            /************* 创建HttpClient及HttpPost对象 **************/
            if (CLIENT == null) {
                CLIENT = getThreadSafeHttpClient(100, type);
            }

            httpPost = getHttpPost(url);
            /************* 向服务器写入数据 **************/
            for (HttpEntity httpEntity : entity) {
                httpPost.setEntity(httpEntity);
            }

            /************* 开始执行命令 ******************/
            HttpResponse httpResponse = CLIENT.execute(httpPost);

            /*************** 接收状态 **************/
            int code = httpResponse.getStatusLine().getStatusCode();
            String data;
            if (code == 200) {// 200请求成功
                data = EntityUtils.toString(httpResponse.getEntity(), CHARSET);
                result.setCode(0);
                result.setSuccess(data);
            } else {// 请求失败，code为异常编码
                data = EntityUtils.toString(httpResponse.getEntity(), CHARSET);
                result.setError(data);
            }
            if (BasicApplication.IS_DEBUG) {
                Log.e("出参  = " + data);
            }
            result.setResponseCode(code);
        } catch (ClientProtocolException e) {
            result.setError("网络协议错误，无法向指定主机发送请求！");
            e.printStackTrace();
        } catch (IOException e) {
            result.setCode(2);
            result.setError("网络交互失败，请检查本地网络！");
            e.printStackTrace();
        } catch (Exception e) {
            result.setError("网络交互失败，请检查本地网络！");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * post网络请求，返回数据无任务处理，接口请求失败将返回null值
     *
     * @param context Context
     * @param content String
     * @return String
     */
    public String post(Context context, int type, String content) {
        try {
            return post(context, type, new StringEntity(content, CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * post网络请求，返回数据无任务处理，接口请求失败将返回null值
     *
     * @param context Context
     * @param entity  HttpEntity...
     * @return String
     */
    public String post(Context context, int type, HttpEntity... entity) {
        return post(getUrl(context), type, entity);
    }

    /**
     * post网络请求，返回数据无任务处理，接口请求失败将返回null值
     *
     * @param url    URL
     * @param entity HttpEntity...
     * @return String
     */
    public String post(String url, int type, HttpEntity... entity) {
        HttpPost httpPost;
        try {
            /************* 创建HttpClient及HttpPost对象 **************/
            if (CLIENT == null) {
                CLIENT = getThreadSafeHttpClient(100, type);
            }

            httpPost = getHttpPost(url);
            /************* 向服务器写入数据 **************/
            for (HttpEntity httpEntity : entity) {
                httpPost.setEntity(httpEntity);
            }

            /************* 开始执行命令 ******************/
            HttpResponse httpResponse = CLIENT.execute(httpPost);

            /*************** 接收状态 **************/
            int code = httpResponse.getStatusLine().getStatusCode();
            if (code == 200) {// 200请求成功
                String data = EntityUtils.toString(httpResponse.getEntity(), CHARSET);
                Log.e("出参  = " + data);
                return data;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 向服务端发送一个post请求，无返回 （使用getUrl(Context context) 获取URL策略）
     *
     * @param context 上下文
     * @param content 内容
     */
    public void sendPostRequest(Context context, String content) {
        sendPostRequest(getUrl(context), REQUEST_HTTP, content);
    }

    /**
     * 向服务端发送一个post请求， 无返回 超时时间为3秒
     *
     * @param url     URL
     * @param content 内容
     */
    public void sendPostRequest(String url, String content) {
        sendPostRequest(url, REQUEST_HTTP, content);
    }

    /**
     * 向服务端发送一个post请求，无返回 （使用getUrl(Context context) 获取URL策略）
     *
     * @param context 上下文
     * @param type    类型 REQUERY_HTTPS、REQUERY_HTTP
     * @param content 内容
     */
    public void sendPostRequest(Context context, int type, String content) {
        sendPostRequest(getUrl(context), type, content);
    }

    /**
     * 向服务端发送一个post请求， 无返回 超时时间为3秒
     *
     * @param url     URL
     * @param type    类型 REQUERY_HTTPS、REQUERY_HTTP
     * @param content 内容
     */
    public void sendPostRequest(String url, int type, String content) {
        try {
            HttpClient httpClient = getThreadSafeHttpClient(1, type);
            HttpPost httpPost = getHttpPost(url);
            Log.e("入参  = " + content);
            /************* 向服务器写入数据 **************/

            StringEntity se = new StringEntity(content, CHARSET);
            se.setContentType(CONTENT_TYPE == null ? CONTENT_DEFAULT_TYPE : CONTENT_TYPE);
            CONTENT_TYPE = null;
            httpPost.setEntity(se);

            /************* 开始执行命令 ******************/
            HttpResponse httpResponse = httpClient.execute(httpPost);

            /*************** 接收状态 **************/
            int code = httpResponse.getStatusLine().getStatusCode();
            if (BasicApplication.IS_DEBUG) {
                String result = (EntityUtils.toString(httpResponse.getEntity(), CHARSET));
                if (code == 200) {
                    Log.e("sendPostRequest 发送成功" + result);
                } else {
                    Log.e("sendPostRequest 发送失败" + result);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * getHttpPost
     *
     * @return 返回HttpPost
     */
    private static HttpPost getHttpPost(String url) {
        HttpPost httpPost;
        httpPost = new HttpPost(url);
        httpPost.setHeader("charset", CHARSET);
        httpPost.setHeader("Content-Type", CONTENT_TYPE == null ? CONTENT_DEFAULT_TYPE : CONTENT_TYPE);
        CONTENT_TYPE = null;
        return httpPost;
    }

    /**
     * 获取HTTP_URL地址
     *
     * @return 返回当前可用的url
     */
    private static String getUrl(Context context) {
        String url;
        if (HTTP_URL2 != null) {
            url = HTTP_URL2;
            HTTP_URL2 = null;
        } else {
            url = HTTP_URL;
        }

        if ((url == null || "".equals(url)) && context != null) {
            /**
             * 如果无法在缓存中得到服务器地址尝试从默认的服务器配置文件中读取
             */
            Properties proper = PropertiesHelper.loadProperties(context, DEF_SERVICE_FILE_NAME, "raw", context.getPackageName());
            if (proper != null) {
                url = PropertiesHelper.get(proper, DEF_SERVICE_URL_KEY);
            }
        }

        if ((url == null || "".equals(url))) {
            /**
             * 客户端内存可能因崩溃或第三方程序强行释放，网络模块已无法启动... 悲剧拉
             */
            Log.e("url已经被释放，网络模块已无法启动！！！");
        }
        return url;
    }

    /**
     * 创建支持多线程的HttpClient
     *
     * @param maxTotal 最大线程数
     * @param type     请求类型
     * @return HttpClient
     */
    protected static HttpClient getThreadSafeHttpClient(int maxTotal, int type) {
        HttpParams params = new BasicHttpParams();
        // 尝试添加代理
        setProxy(params);
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
        if (REQUEST_HTTP == type) {
            // 版本
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
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
     * 添加代理
     *
     * @param params 为客户端设置代理信息
     */
    private static void setProxy(HttpParams params) {
        // 如果存在代理就使用
        if (HTTP_HOST != null && !"".equals(HTTP_HOST) && HTTP_PORT != 0) {
            HttpHost proxy = new HttpHost(HTTP_HOST, HTTP_PORT);
            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
    }

    /**
     * 如果CLIENT为活动状态，将结束所有连接
     */
    public static void shutdown() {
        if (CLIENT != null) {
            CLIENT.getConnectionManager().shutdown();
            CLIENT = null;
        }
    }

    public static String getCharset() {
        return CHARSET;
    }

    public static void setCharset(String charset) {
        CHARSET = charset;
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

    /**
     * 获取服务器主机地址
     *
     * @return 主机地址
     */
    public static String getHttpUrl() {
        return HTTP_URL;
    }

    /**
     * 设置服务器地址
     *
     * @param httpUrl 主机地址
     */
    public static void setHttpUrl(String httpUrl) {
        HTTP_URL = httpUrl;
    }

    /**
     * 获取网络连接超时时间与数据读取超时时间
     *
     * @return 超时时间
     */
    public static int getTimeOut() {
        return TIME_OUT;
    }

    /**
     * 设置网络连接超时时间与数据读取超时时间
     */
    public static void setTimeOut(int timeOut) {
        TIME_OUT = timeOut;
    }

    /**
     * 设置网络数据读取超时时间
     *
     * @param timeOut 毫秒单位
     */
    public static void setReadTimeOut(int timeOut) {
        SO_TIME_OUT = timeOut;
    }

    /**
     * 设置网络临时地址，仅本次设置有效
     *
     * @param httpUrl2 临时URL
     */
    public static void setHttpUrl2(String httpUrl2) {
        HTTP_URL2 = httpUrl2;
    }

    /**
     * 设置代理
     *
     * @param host 主机
     * @param port 端口
     */
    public static void addProxy(String host, int port) {
        HTTP_HOST = host;
        HTTP_PORT = port;
    }

    /**
     * 删除代理
     */
    public static void removeProxy() {
        HTTP_HOST = null;
        HTTP_PORT = 0;
    }

}
