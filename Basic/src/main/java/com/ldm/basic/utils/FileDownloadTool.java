package com.ldm.basic.utils;

import android.content.Context;
import android.content.Intent;

import com.ldm.basic.app.Configuration;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

/**
 * Created by ldm on 13-12-14.
 * 文件下载工具，提供了常用的Http操作及一个单线程的异步任务
 * 异步任务下载成功后可以通过注册广播，拦截发往com.ldm.basic.utils.FileDownloadTool的广播得到下载完成后的文件名称
 */
public class FileDownloadTool {

    // 广播地址，可以通过该广播地址接收下载完成进度
    public static final String DOWNLOAD_COMPLETE = "com.ldm.basic.utils.FileDownloadTool";

    // 图片保存路径
    public static String CACHE_IMAGES_PATH = Configuration.IMAGE_CACHE_PATH;

    public static final String FILE_ID = "fId";// 在广播中通告该KEY获取fId
    public static final String FILE_PATH = "filePath";// 在广播中通告该KEY获取绝对路径

    /**
     * 连接超时时间
     */
    public static int TIME_OUT = 1000 * 7;

    /**
     * 读取超时时间
     */
    public static int SO_TIME_OUT = 1000 * 80;

    private WeakReference<Context> context;
    private TaskThreadService lService;

    private boolean async;// 异步任务是否开启


    public FileDownloadTool() {
    }

    /**
     * 可以使用addTask方法
     *
     * @param context      Context
     * @param isStartAsync 是否开启异步任务（开启后将可以使用addTask等方法）
     */
    public FileDownloadTool(Context context, boolean isStartAsync) {
        this.context = new WeakReference<>(context);
        if (async = isStartAsync) {
            this.lService = new TaskThreadService(true);
        }
    }

    /**
     * 向列表中添加任务
     *
     * @param res 文件属性
     */
    public void addTask(FileRes res) {
        if (async) {
            lService.addTask(getTask(res));
        } else {
            Log.e("您还没有开启异步任务，暂时不能使用该方法");
        }
    }

    /**
     * 向任务列表添加一个根据url及cacheName构造的任务
     *
     * @param url       HTTP_URL
     * @param cacheName 本地缓存时的名称
     * @return fid 一个由UUID生成的唯一标识，用户可以通过监听FileDownloadTool.
     * DOWNLOAD_COMPLETE广播并可以通过intent拿到fId与filePath
     */
    public String addTask(String url, String cacheName) {
        String fid = null;
        if (async) {
            fid = String.valueOf(UUID.randomUUID());
            lService.addTask(getTask(new FileRes(fid, url, cacheName)));
        } else {
            Log.e("您还没有开启异步任务，暂时不能使用该方法");
        }
        return fid;
    }

    /**
     * 向列表中添加多个任务
     *
     * @param s 文件属性列表
     */
    public void addTask(List<FileRes> s) {
        if (async) {
            for (FileRes fileRes : s) {
                lService.addTask(getTask(fileRes));
            }
        } else {
            Log.e("您还没有开启异步任务，暂时不能使用该方法");
        }
    }

    /**
     * 通过给定的FileRes 返回一个Task任务
     *
     * @param param 入参
     * @return TaskThreadService.Task
     */
    private TaskThreadService.Task getTask(Object... param) {
        return new TaskThreadService.Task(param) {
            @Override
            public void taskStart(Object... obj) {
                FileRes res = (FileRes) obj[0];

                String path = httpToFile(res.url, res.cachePath == null ? CACHE_IMAGES_PATH : res.cachePath, res.cacheName);
                sendBroadcast(res.fId, path);
            }
        };
    }

    /**
     * 通过HttpClient下载文件并保存在 默认的缓存路径下（CACHE_IMAGES_PATH）
     *
     * @param url       文件属性
     * @param cacheName 缓存名
     * @return 返回保存后的路径
     */
    public String httpToFile(final String url, final String cacheName) {
        return httpToFile(url, CACHE_IMAGES_PATH, cacheName);
    }

    /**
     * 通过HttpClient下载文件并保存在 默认的缓存路径下（CACHE_IMAGES_PATH）
     *
     * @param url       文件属性
     * @param cachePath 缓存路径
     * @param cacheName 缓存名
     * @return 返回保存后的路径
     */
    public String httpToFile(final String url, final String cachePath, final String cacheName) {
        if (TextUtils.isNull(url)) {
            return null;
        }
        FileTool.createDirectory(cachePath);
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
                return FileTool.save(urlConnection.getInputStream(), cachePath, cacheName);
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

    /**
     * 发送下载成功广播
     *
     * @param fId  fId
     * @param path 绝对路径
     */
    private void sendBroadcast(String fId, String path) {
        Intent broadcast = new Intent(DOWNLOAD_COMPLETE);
        broadcast.putExtra(FILE_ID, fId);
        broadcast.putExtra(FILE_PATH, path);
        if (context != null) {
            Context _context = context.get();
            if (_context != null) {
                _context.sendBroadcast(broadcast);
            }
        }
    }

    /**
     * 设置缓存目录
     *
     * @param path 路径
     */
    public static void setCacheImagesPath(final String path) {
        CACHE_IMAGES_PATH = path;
    }

    /**
     * 关闭异步任务
     */
    public void stopAllTask() {
        if (lService != null) {
            lService.stopTask();
        }
    }

    /**
     * 文件属性
     */
    public static class FileRes {
        public String fId;
        public String url;
        public String cacheName;
        public String cachePath;

        /**
         * 构造FileRes
         *
         * @param fId       文件任务ID， 如果该ID与已有队列中的任务冲突，将不进行添加
         * @param url       HTTP_URL
         * @param cacheName 保存本地时的名称
         */
        public FileRes(String fId, String url, String cacheName) {
            this.fId = fId;
            this.url = url;
            this.cacheName = cacheName;
        }

        /**
         * 构造FileRes
         *
         * @param fId       文件任务ID， 如果该ID与已有队列中的任务冲突，将不进行添加
         * @param url       HTTP_URL
         * @param cacheName 保存本地时的名称
         */
        public FileRes(String fId, String url, String cacheName, String cachePath) {
            this.fId = fId;
            this.url = url;
            this.cacheName = cacheName;
            this.cachePath = cachePath;
        }

    }
}
