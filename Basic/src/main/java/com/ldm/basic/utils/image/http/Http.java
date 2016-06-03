package com.ldm.basic.utils.image.http;

import com.ldm.basic.utils.FileTool;
import com.ldm.basic.utils.TextUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ldm on 16/5/31.
 * 网络请求工具
 */
public class Http {

    private String url;
    private String filePath, fileName;

    public Http(String url, String filePath, String fileName) {
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    /**
     * 连接超时时间
     */
    public static int TIME_OUT = 1000 * 7;

    /**
     * 读取超时时间
     */
    public static int SO_TIME_OUT = 1000 * 120;

    /**
     * 执行网络请求
     *
     * @return HttpResult
     */
    public HttpResult request() {
        if (TextUtils.isNull(url)) {
            return HttpResult.buildUrlIsNull();
        }
        HttpResult result = new HttpResult();
        if (!FileTool.createDirectory(filePath)) {
            return result;
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = createHttpURLConnection();
            urlConnection.connect();
            int responseCode = result.responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                result.code = 1;
                result.localFilePath = FileTool.save(urlConnection.getInputStream(), filePath, fileName);
            } else {
                //文件没有下载完成，执行一次删除
                FileTool.delete(filePath);
            }
        } catch (Exception e) {
            result.error = e.getMessage();
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
        return result;
    }

    /**
     * 创建一个HttpURLConnection
     *
     * @return HttpURLConnection
     * @throws IOException
     */
    private HttpURLConnection createHttpURLConnection() throws IOException {
        HttpURLConnection urlConnection;
        URL httpUrl = new URL(url);
        urlConnection = (HttpURLConnection) httpUrl.openConnection();
        urlConnection.setConnectTimeout(TIME_OUT);
        urlConnection.setReadTimeout(SO_TIME_OUT);
        urlConnection.setInstanceFollowRedirects(true);
        urlConnection.setRequestMethod("GET");
        return urlConnection;
    }

}
