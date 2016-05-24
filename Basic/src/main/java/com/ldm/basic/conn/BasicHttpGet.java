package com.ldm.basic.conn;

import com.ldm.basic.bean.BasicInternetRetBean;
import com.ldm.basic.utils.LLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class BasicHttpGet extends HttpRequest {


    public BasicHttpGet(String url) {
        super(url);
    }

    @Override
    public BasicInternetRetBean execute(String param) {
        return basicHttpGet(url, param);
    }

    @Override
    BasicInternetRetBean execute(Map<String, String> params) {
        return null;
    }

    /**
     * httpGet请求
     *
     * @param serviceUrl 地址
     * @param param      附加在地址上的数据
     * @return data
     */
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
                LLog.e(" 出参 " + result.toString());
                return result.toString();
            } else {
                LLog.e("访问[" + serviceUrl + "]失败，错误码 " + responseCode);
            }

        } catch (IOException e) {
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


    public BasicInternetRetBean basicHttpGet(String serviceUrl, String param) {
        BasicInternetRetBean retBean = new BasicInternetRetBean();
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
                LLog.e(" 出参 " + result.toString());
                retBean.setCode(0);
                retBean.setSuccess(result.toString());
            } else {
                retBean.setCode(1);
                LLog.e("访问[" + serviceUrl + "]失败，错误码 " + responseCode);
            }
        } catch (IOException e) {
            retBean.setCode(2);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return retBean;
    }

    /**
     * get请求，没有返回结果
     *
     * @param serviceUrl 地址
     * @param params     附加在地址后面的参数
     */
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
                LLog.e(" send 成功 ");
            } else {
                LLog.e(" send 失败，错误码 " + responseCode);
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
