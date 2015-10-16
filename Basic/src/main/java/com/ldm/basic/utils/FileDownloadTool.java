package com.ldm.basic.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.ldm.basic.app.Configuration;

import android.content.Context;
import android.content.Intent;

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

    private WeakReference<Context> context;
    private FileTool fileTool;
    private TaskThreadService lService;

    private boolean async;// 异步任务是否开启

    /**
     * 可以使用addTask方法
     *
     * @param context      Context
     * @param isStartAsync 是否开启异步任务（开启后将可以使用addTask等方法）
     */
    public FileDownloadTool(Context context, boolean isStartAsync) {
        this.context = new WeakReference<Context>(context);
        this.fileTool = new FileTool();
        if (async = isStartAsync) {
            this.lService = new TaskThreadService(true);
        }
    }

    /**
     * 这个构造函数将不会启用异步操作
     */
    public FileDownloadTool() {
        this.fileTool = new FileTool();
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
        HttpClient client = getThreadSafeHttpClient();
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse response = client.execute(get);
            int code = response.getStatusLine().getStatusCode();
            if (code == HttpStatus.SC_OK && response.getEntity() != null) {
                return fileTool.save(response.getEntity().getContent(), cachePath, cacheName);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null && client.getConnectionManager() != null) {
                client.getConnectionManager().shutdown();
            }
        }
        return null;
    }

    /**
     * 通过HttpClient下载文件并保存在 默认的缓存路径下（CACHE_IMAGES_PATH）
     * 处理失败时将返回异常代码，成功时增加标识【0::】成功 【1::】失败
     *
     * @param url       文件属性
     * @param filePath 文件路径
     * @return 保存后返回 状态::路径
     */
    public String httpToFile2(final String url, final String filePath) {
    	 if (TextUtils.isNull(url)) {
             return "1::url is null";
         }
         String result = "";
         HttpClient client = getThreadSafeHttpClient();
         HttpGet get = new HttpGet(url);
         try {
             HttpResponse response = client.execute(get);
             int code = response.getStatusLine().getStatusCode();
             Log.e("code = " + code);
             if (code == HttpStatus.SC_OK && response.getEntity() != null) {
                 return "0::" + fileTool.save(response.getEntity().getContent(), filePath);
             } else {
                 //文件没有下载完成，执行一次删除
                 FileTool.delete(filePath);
                 return "2::" + HttpStatus.SC_NOT_FOUND;
             }
         } catch (Exception e) {
             e.printStackTrace();
             //文件没有下载完成，执行一次删除
             FileTool.delete(filePath);
             result = "1::" + e.getMessage();
         } finally {
             if (client != null && client.getConnectionManager() != null) {
                 client.getConnectionManager().shutdown();
             }
         }
         return result;
    }

    /**
     * 通过HttpClient下载文件并保存在 默认的缓存路径下（CACHE_IMAGES_PATH）
     * 处理失败时将返回异常代码，成功时增加标识【0::】成功 【1::】失败
     *
     * @param url       文件属性
     * @param cachePath 缓存路径
     * @param cacheName 缓存名
     * @return 保存后返回 状态::路径
     */
    public String httpToFile2(final String url, final String cachePath, final String cacheName) {
        return httpToFile2(url, cachePath + "/" + cacheName);
    }

    /**
     * 返回URL的数据流
     *
     * @param url 地址
     * @return InputStream
     */
    public InputStream httpToInputStream(final String url) {
        HttpClient client = getThreadSafeHttpClient();
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse response = client.execute(get);
            int code = response.getStatusLine().getStatusCode();
            if (code == HttpStatus.SC_OK && response.getEntity() != null) {
                return response.getEntity().getContent();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null && client.getConnectionManager() != null) {
                client.getConnectionManager().shutdown();
            }
        }
        return null;
    }

    /**
     * 根据指定的URL，将内容对应行数转换成List<String>
     *
     * @param url URL
     * @return List<String>
     */
    public static List<String> httpToLines(String url) {
        HttpClient client = getThreadSafeHttpClient();
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse response = client.execute(get);
            int code = response.getStatusLine().getStatusCode();
            if (code == HttpStatus.SC_OK) {
                InputStream is = response.getEntity().getContent();
                if (is != null) {
                    List<String> result = new ArrayList<String>();
                    InputStreamReader inReader = new InputStreamReader(is);
                    BufferedReader buffReader = new BufferedReader(inReader);
                    String data;
                    try {
                        while ((data = buffReader.readLine()) != null) {
                            if (!"".equals(data.trim())) {
                                result.add(data.trim());
                            }
                        }
                        is.close();
                        inReader.close();
                        buffReader.close();
                        return result;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null && client.getConnectionManager() != null) {
                client.getConnectionManager().shutdown();
            }
        }
        return null;
    }

    /**
     * 将给定的地址对应的文件中的内容以字符串的形式返回
     *
     * @param url 地址
     * @return String
     */
    public static String httpToString(String url) {
        HttpClient client = getThreadSafeHttpClient();
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse response = client.execute(get);
            int code = response.getStatusLine().getStatusCode();
            if (code == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null && client.getConnectionManager() != null) {
                client.getConnectionManager().shutdown();
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
     * 创建支持多线程的HttpClient
     *
     * @return HttpClient
     */
    protected static HttpClient getThreadSafeHttpClient() {
        HttpParams params = new BasicHttpParams();
        // 版本
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        // 编码
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        // Activates 'Expect: 100-continue' handshake for the entity enclosing
        // methods.
        HttpProtocolParams.setUseExpectContinue(params, true);
        // 最大连接数
        ConnManagerParams.setMaxTotalConnections(params, 100);
        // 超时
        HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
        HttpConnectionParams.setSoTimeout(params, 20 * 1000);
        // 计划注册,可以注册多个计划
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
        return new DefaultHttpClient(conMgr, params);
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
