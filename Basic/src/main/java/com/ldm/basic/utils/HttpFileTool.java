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
     * 通过HttpClient下载文件并保存在 默认的缓存路径下（CACHE_IMAGES_PATH）
     * 处理失败时将返回异常代码，成功时增加标识【0】成功 【other】失败
     *
     * @param url      文件属性
     * @param filePath 文件路径
     * @return 保存后返回 String[]{"状态", "路径"}
     */
    public static String[] httpToFile2(final String url, final String filePath) {
        if (TextUtils.isNull(url)) {
            return new String[]{"url is null", ""};
        }
        String[] result = new String[2];
        result[0] = "err";
        if (FileTool.createDirectory(filePath)) {
            HttpURLConnection urlConnection = null;
            try {
                URL httpUrl = new URL(URLEncoder.encode(url, "UTF-8"));
                urlConnection = (HttpURLConnection) httpUrl.openConnection();
                urlConnection.setConnectTimeout(TIME_OUT);
                urlConnection.setReadTimeout(SO_TIME_OUT);
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.setRequestMethod("GET");
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    result[0] = "200";
                    result[1] = FileTool.save(urlConnection.getInputStream(), filePath);
                    return result;
                } else {
                    //文件没有下载完成，执行一次删除
                    FileTool.delete(filePath);
                    result[0] = String.valueOf(responseCode);
                    result[1] = null;
                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
                result[0] = e.getMessage();
            } finally {
                if (urlConnection != null) {
                    try {
                        urlConnection.disconnect();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    /**
     * 通过HttpClient下载文件并保存在 默认的缓存路径下（CACHE_IMAGES_PATH）
     * 处理失败时将返回异常代码，成功时增加标识【0】成功 【other】失败
     *
     * @param url       文件属性
     * @param cachePath 缓存路径
     * @param cacheName 缓存名
     * @return 保存后返回 String[]{"状态", "路径"}
     */
    public static String[] httpToFile2(final String url, final String cachePath, final String cacheName) {
        return httpToFile2(url, cachePath + "/" + cacheName);
    }

    /**
     * 使用HttpURLConnection下载网络文件并转成List<String>
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
                    return result.get(0);
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
        return null;
    }

}
