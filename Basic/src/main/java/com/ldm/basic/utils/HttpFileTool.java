package com.ldm.basic.utils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by ldm on 16/3/11.
 * 使用HttpURLConnection下载文件
 */
public class HttpFileTool {

    /**
     * 连接超时时间
     */
    public static int TIME_OUT = 1000 * 7;

    /**
     * 读取超时时间
     */
    public static int SO_TIME_OUT = 1000 * 120;

    /**
     * 根据URL将文件转存到本地
     *
     * @param cachePath  缓存目录
     * @param fileName   本地文件名
     * @param serviceUrl 网络URL
     * @return file path
     */
    public static String httpToFile(String cachePath, String fileName, String serviceUrl) {
        FileTool.createDirectory(cachePath);
        try {
            URL url = new URL(URLEncoder.encode(serviceUrl, "UTF-8"));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIME_OUT);
            urlConnection.setReadTimeout(SO_TIME_OUT);
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setRequestMethod("GET");
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return FileTool.save(urlConnection.getInputStream(), cachePath, fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用HttpURLConnection下载网络文件并转成List
     *
     * @param serviceUrl 网络URL
     * @return data
     */
    public static List<String> httpToLines(String serviceUrl) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(URLEncoder.encode(serviceUrl, "UTF-8"));
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIME_OUT);
            urlConnection.setReadTimeout(SO_TIME_OUT);
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setRequestMethod("GET");
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                List<String> result = FileTool.toLines(urlConnection.getInputStream());
                if (result != null && result.size() > 0) {
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                try {
                    urlConnection.disconnect();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 使用HttpURLConnection下载网络文件并转成String
     *
     * @param serviceUrl 网络URL
     * @return data
     */
    public static String httpToString(String serviceUrl) {
        String resultPath = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(URLEncoder.encode(serviceUrl, "UTF-8"));
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIME_OUT);
            urlConnection.setReadTimeout(SO_TIME_OUT);
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setRequestMethod("GET");
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                List<String> result = FileTool.toLines(urlConnection.getInputStream());
                if (result != null && result.size() > 0) {
                    resultPath = result.get(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                try {
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return resultPath;
    }

}
