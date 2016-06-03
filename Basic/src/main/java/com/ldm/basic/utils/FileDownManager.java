package com.ldm.basic.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ldm on 16/6/3.
 * 文件下载工具
 */
public class FileDownManager {

    private ExecutorService threadPool;

    private static FileDownManager fileDownManager;

    public FileDownManager() {
        /**
         * FileDownManager中线程数不能同事超过6个
         */
        this.threadPool = Executors.newFixedThreadPool(6);
    }

    public static FileDownManager getInstance() {
        if (fileDownManager == null) {
            fileDownManager = new FileDownManager();
        }
        return fileDownManager;
    }


    public void download(String url, String directory, OnFileDownloadListener listener) {
        FileOptions option = new FileOptions();
        option.httpFileUrl = url;
        option.directory = directory;
        option.listener = listener;
        option.fileName = MD5.md5(url);
        //提交下载任务
        threadPool.submit(option);
    }

    public class FileOptions implements Runnable {

        /**
         * 进入数据
         */
        long totalSize;
        long currentSize;

        /**
         * 网络URL
         */
        String httpFileUrl;

        /**
         * 文件保存到本地时的名字
         */
        String fileName;

        /**
         * 文件路径
         */
        String directory;

        OnFileDownloadListener listener;

        @Override
        public void run() {
            boolean result = false;
            final String filePath = directory + "/" + fileName;
            if (FileTool.createDirectory(directory)) {
                String filePathTemp = directory + "/" + fileName + ".temp";
                try {
                    URL url = new URL(URLEncoder.encode(httpFileUrl, "UTF-8"));
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(7 * 1000);
                    urlConnection.setReadTimeout(120 * 1000);
                    urlConnection.setInstanceFollowRedirects(true);
                    urlConnection.setRequestMethod("GET");
                    //记录文件大小
                    totalSize = urlConnection.getContentLength();
                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                        InputStream is = urlConnection.getInputStream();
                        FileOutputStream out = new FileOutputStream(filePathTemp);
                        // 测试此缓存目录是否存在
                        int len;
                        byte[] bytes = new byte[4096];
                        while ((len = is.read(bytes)) != -1) {
                            out.write(bytes, 0, len);
                            currentSize += len;
                            if (totalSize > 0 && listener != null) {
                                listener.onProgress(totalSize, currentSize);
                            }
                        }
                        out.flush();
                        out.close();
                        is.close();

                        File f = new File(filePathTemp);
                        File newFile = new File(filePath);
                        if (f.exists() && f.renameTo(newFile)) {
                            if (listener != null) {
                                listener.onSuccess(newFile);
                            }
                            result = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (listener != null && !result) {
                listener.onFailure(filePath);
            }
        }
    }

    public interface OnFileDownloadListener {
        void onFailure(String filePath);

        void onSuccess(File file);

        void onProgress(long totalSize, long currentSize);
    }

}
