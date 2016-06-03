package com.ldm.basic.http;

import com.ldm.basic.bean.BasicInternetRetBean;
import com.ldm.basic.utils.LLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by ldm on 15/10/15.
 * 内部使用HttpURLConnection
 */
public class BasicHttpPost extends HttpRequest {

    private static final String BOUNDARY = java.util.UUID.randomUUID().toString();
    private static final String PREFIX = "--", LINEND = "\r\n";
    private static final String CHARSET = "UTF-8";

    public BasicHttpPost(String url) {
        super(url);
    }

    @Override
    public BasicInternetRetBean execute(String param) {
        return basicHttpPost(url, param);
    }

    @Override
    BasicInternetRetBean execute(Map<String, String> params) {
        return basicHttpPost(url, params);
    }

    /**
     * httpPost请求
     *
     * @param params 参数
     * @return String
     */
    public String httpPost(String params) {
        return httpPost(url, params);
    }

    /**
     * httpPost请求
     *
     * @param serviceUrl 地址
     * @param params     参数
     * @return String
     */
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
                LLog.e(" 出参 " + result.toString());
                return result.toString();
            } else {
                LLog.e("访问[" + serviceUrl + "]失败，错误码 " + responseCode);
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
     *
     * @param params 参数
     * @return BasicInternetRetBean
     */
    public BasicInternetRetBean basicHttpPost(String params) {
        return basicHttpPost(url, params);
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
        BufferedReader bufferedReader = null;
        try {
            if (params == null) {
                params = "";
            }
            byte[] bytes = params.getBytes("utf-8");
            /**
             * 创建连接
             */
            urlConnection = createConnection(serviceUrl, bytes.length);
            //写数据
            OutputStream out = urlConnection.getOutputStream();
            out.write(bytes);
            out.flush();
            out.close();
            //读数据
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
                LLog.e(" 出参 " + result.toString());
            } else {
                resultData.setCode(1);
                resultData.setError("error");
                LLog.e("访问[" + serviceUrl + "]失败，错误码 " + responseCode);
            }
        } catch (IOException e) {
            resultData.setCode(2);
        } catch (Exception e) {
            resultData.setCode(1);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
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

    /**
     * httpPost请求，这个方法的返回结果将被封装到BasicInternetRetBean中，主要提供给InternetEntity使用
     *
     * @param serviceUrl 地址
     * @param params     参数
     * @return BasicInternetRetBean
     */
    public BasicInternetRetBean basicHttpPost(String serviceUrl, Map<String, String> params) {
        BasicInternetRetBean resultData = new BasicInternetRetBean();
        HttpURLConnection urlConnection = null;
        BufferedReader bufferedReader = null;
        try {
            byte[] bytes = buildParamData(params).getBytes("utf-8");
            //创建连接
            urlConnection = createConnection(serviceUrl, bytes.length);
            //写数据
            OutputStream out = urlConnection.getOutputStream();
            out.write(bytes);
            out.flush();
            out.close();
            //读数据
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
                LLog.e(" 出参 " + result.toString());
            } else {
                resultData.setCode(1);
                resultData.setError("error");
                LLog.e("访问[" + serviceUrl + "]失败，错误码 " + responseCode);
            }
        } catch (IOException e) {
            resultData.setCode(2);
        } catch (Exception e) {
            resultData.setCode(1);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
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

    /**
     * 创建HTTP连接
     *
     * @param serviceUrl 网络URL
     * @param length     数据长度
     * @return HttpURLConnection
     * @throws IOException
     */
    private HttpURLConnection createConnection(String serviceUrl, int length) throws IOException {
        URL url = new URL(serviceUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        // 设置通用的请求属性
        if (ACCEPT_TYPE != null) {
            urlConnection.setRequestProperty("accept", ACCEPT_TYPE);
        }
        urlConnection.setUseCaches(false);// 忽略缓存
        urlConnection.setRequestMethod("POST");// 设置URL请求方法
        urlConnection.setRequestProperty("Content-Length", String.valueOf(length));
        urlConnection.setConnectTimeout(TIME_OUT);
        urlConnection.setReadTimeout(SO_TIME_OUT);
        urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE == null ? CONTENT_DEFAULT_TYPE : CONTENT_TYPE);
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        return urlConnection;
    }

    /**
     * 将MAP写入到http request中
     *
     * @param params Map<String, String>
     * @throws IOException
     */
    private String buildParamData(Map<String, String> params) throws IOException {
        // 首先组拼文本类型的参数
        StringBuilder outData = new StringBuilder();
        for (String key : params.keySet()) {
            outData.append(PREFIX);
            outData.append(BOUNDARY);
            outData.append(LINEND);
            outData.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(LINEND);
            outData.append("Content-Type: text/plain; charset=").append(CHARSET).append(LINEND);
            outData.append("Content-Transfer-Encoding: 8bit").append(LINEND).append(LINEND);
            outData.append(params.get(key));
            outData.append(LINEND);
        }
        return outData.toString();
    }

    /**
     * 发送一个POST请求，不等待返回结果
     *
     * @param serviceUrl URL
     * @param params     参数
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
