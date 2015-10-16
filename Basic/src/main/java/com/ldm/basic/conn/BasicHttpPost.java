package com.ldm.basic.conn;

import android.content.Context;

import com.ldm.basic.bean.BasicInternetRetBean;
import com.ldm.basic.properties.PropertiesHelper;
import com.ldm.basic.utils.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * Created by ldm on 15/10/15.
 * 内部使用HttpURLConnection
 */
public class BasicHttpPost {


    // 主机服务地址
    private static String HTTP_URL = "";

    // 临时主机服务地址
    private static String HTTP_URL2 = null;

    /**
     * 连接超时时间
     */
    public static int TIME_OUT = 1000 * 10;

    /**
     * 读取超时时间
     */
    public static int SO_TIME_OUT = 1000 * 25;

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
    protected static String CONTENT_TYPE = null;

    /**
     * 默认的网络交互数据格式
     */
    protected static String CONTENT_DEFAULT_TYPE = CONTENT_TYPE_JSON;

    /**
     * 请求人
     */
    public static String ACCEPT_TYPE = null;


    public String httpPost(String serviceUrl, String params) {
        HttpURLConnection urlConnection = null;
        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            // 设置通用的请求属性
            if (ACCEPT_TYPE != null) {
                urlConnection.setRequestProperty("accept", ACCEPT_TYPE);
            }
            urlConnection.setRequestProperty("connection", "Keep-Alive");
            urlConnection.setUseCaches(false);// 忽略缓存
            urlConnection.setRequestMethod("POST");// 设置URL请求方法
            urlConnection.setConnectTimeout(TIME_OUT);
            urlConnection.setReadTimeout(SO_TIME_OUT);
            urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE == null ? CONTENT_DEFAULT_TYPE : CONTENT_TYPE);

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            if (params != null) {
                printWriter = new PrintWriter(urlConnection.getOutputStream());
                printWriter.write(params);
                printWriter.flush();
            }
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                StringBuilder result = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }
                Log.e(" 出参 " + result.toString());
                return result.toString();
            } else {
                Log.e("访问[" + serviceUrl + "]失败，错误码 " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (printWriter != null) {
                printWriter.close();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * httpPost请求，这个方法的返回结果将被封装到BasicInternetRetBean中，主要提供给InternetEntity使用
     * （这个方法使用全局配置的主URL作为请求地址）
     *
     * @param context Context
     * @param params  参数
     * @return BasicInternetRetBean
     */
    public BasicInternetRetBean basicHttpPost(Context context, String params) {
        return basicHttpPost(getUrl(context), params);
    }

    /**
     * httpPost请求，这个方法的返回结果将被封装到BasicInternetRetBean中，主要提供给InternetEntity使用
     *
     * @param serviceUrl 地址
     * @param params     参数
     * @return BasicInternetRetBean
     */
    public BasicInternetRetBean basicHttpPost(String serviceUrl, String params) {
        BasicInternetRetBean resultData = new BasicInternetRetBean();
        HttpURLConnection urlConnection = null;
        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            // 设置通用的请求属性
            if (ACCEPT_TYPE != null) {
                urlConnection.setRequestProperty("accept", ACCEPT_TYPE);
            }
            urlConnection.setRequestProperty("connection", "Keep-Alive");
            urlConnection.setUseCaches(false);// 忽略缓存
            urlConnection.setRequestMethod("POST");// 设置URL请求方法
            urlConnection.setConnectTimeout(TIME_OUT);
            urlConnection.setReadTimeout(SO_TIME_OUT);
            urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE == null ? CONTENT_DEFAULT_TYPE : CONTENT_TYPE);

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            if (params != null) {
                printWriter = new PrintWriter(urlConnection.getOutputStream());
                printWriter.write(params);
                printWriter.flush();
            }
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                StringBuilder result = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }
                resultData.setCode(0);
                resultData.setSuccess(result.toString());
                Log.e(" 出参 " + result.toString());
            } else {
                resultData.setCode(1);
                resultData.setError("error");
                Log.e("访问[" + serviceUrl + "]失败，错误码 " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            resultData.setCode(2);
            resultData.setError("网络交互失败，请检查本地网络！");
        } catch (Exception e) {
            e.printStackTrace();
            resultData.setError("网络交互失败，请检查本地网络！");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (printWriter != null) {
                printWriter.close();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return resultData;
    }

    public void httpPostNotResult(String serviceUrl, String params) {
        HttpURLConnection urlConnection = null;
        PrintWriter printWriter = null;
        try {
            URL url = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            // 设置通用的请求属性
            if (ACCEPT_TYPE != null) {
                urlConnection.setRequestProperty("accept", ACCEPT_TYPE);
            }
            urlConnection.setRequestProperty("connection", "Keep-Alive");
            urlConnection.setUseCaches(false);// 忽略缓存
            urlConnection.setRequestMethod("POST");// 设置URL请求方法
            urlConnection.setConnectTimeout(TIME_OUT);
            urlConnection.setReadTimeout(SO_TIME_OUT);
            urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE == null ? CONTENT_DEFAULT_TYPE : CONTENT_TYPE);

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            if (params != null) {
                printWriter = new PrintWriter(urlConnection.getOutputStream());
                printWriter.write(params);
                printWriter.flush();
            }
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                Log.e(" send 成功 ");
            } else {
                Log.e(" send 失败，错误码 " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    public String httpsPost(String serviceUrl, Map<String, String> data) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
//            writeStream(out);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//            readStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
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
}
