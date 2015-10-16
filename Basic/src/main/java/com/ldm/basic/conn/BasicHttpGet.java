package com.ldm.basic.conn;

import com.ldm.basic.utils.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by ldm on 15/10/15.
 * 内部使用HttpURLConnection
 */
public class BasicHttpGet {


    /**
     * 连接超时时间
     */
    public static int TIME_OUT = 1000 * 10;

    /**
     * 读取超时时间
     */
    public static int SO_TIME_OUT = 1000 * 25;

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


    public String httpGet(String serviceUrl, String param) {
        try {
            return httpGet(serviceUrl + "?" + URLEncoder.encode(param, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * httpGet请求
     *
     * @param serviceUrl 地址将会使用URLEncoder.encode UTF-8编码
     * @return null接口失败
     */
    public String httpGet(String serviceUrl) {
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(URLEncoder.encode(serviceUrl, "UTF-8"));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIME_OUT);
            urlConnection.setReadTimeout(SO_TIME_OUT);
            urlConnection.setRequestMethod("GET");
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

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
}
