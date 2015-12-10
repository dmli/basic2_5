package com.ldm.basic.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.ldm.basic.dialog.LToast;
import com.ldm.basic.utils.LazyImageDownloader.ImageRef;
import com.ldm.basic.utils.TaskThreadToMultiService.Task;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by ldm on 15-1-17. 下载文件，提供的下载进度
 */
public class LazyFileDownloader {

    public static final int DOWNLOAD_STATE_URL_INVALID = -1;// 下载失败，URL无效
    public static final int DOWNLOAD_STATE_READ_ERROR = -2;// 下载失败，READ失败

    WeakReference<Context> context;

    private TaskThreadToMultiService services;

    private SecurityHandler<LazyFileDownloader> lHandler;

    // 图片保存路径
    public String FILE_CACHE_PATH;

    public Map<String, FileRef> P_IDS = new HashMap<>();

    /**
     * 下载过程中用来与PID匹配的value值存储的KEY
     */
    public static final int TAG_ID = 0x59999999;

    public LazyFileDownloader(Context context, int maxAsyncTaskNumber) {
        this.context = new WeakReference<Context>(context);
        services = new TaskThreadToMultiService(maxAsyncTaskNumber);
    }

    public LazyFileDownloader(Context context, int maxAsyncTaskNumber, String cachePath) {
        this.context = new WeakReference<Context>(context);
        this.services = new TaskThreadToMultiService(maxAsyncTaskNumber);
        this.FILE_CACHE_PATH = cachePath;
    }


    /**
     * 创建一个一步任务
     *
     * @param ref FileRef
     */
    public void addTask(FileRef ref) {
        if (ref == null || ref.url == null) {
            return;
        }

        if (!ref.url.startsWith("http")) {
            ref.error(DOWNLOAD_STATE_URL_INVALID);
            return;
        }

        /**
         * 同步PID，如果view被重用，这样可以有效的切断与原任务的绑定关系
         */
        ref.syncPid();

        /**
         * 任务存在时，尝试重新绑定任务
         */
        if (P_IDS.containsKey(ref.pId)) {
            FileRef ir = P_IDS.get(ref.pId);
            if (ir != null) {
                ir.progressBar = ref.progressBar;
                ir.progressListener = ref.progressListener;
                return;
            }
        }

        /**
         * 将本次任务注册到P_IDS列表中
         */
        P_IDS.put(ref.pId, ref);

        /**
         * 创建一个异步任务
         */
        services.addTask(new Task(ref) {
            @Override
            public void taskStart(Object... obj) {
                if (obj != null && obj[0] != null) {
                    FileRef fr = (FileRef) obj[0];
                    File f = new File(FILE_CACHE_PATH + "/" + fr.cacheName);
                    if (f.exists()) {
                        lHandler.sendMessage(lHandler.obtainMessage(200, fr));// 使用缓存
                    } else {
                        downloadFile(fr);
                    }
                }
            }
        });
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
        // Activates 'Expect: 100-continue' handshake for the entity enclosing methods.
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
     * 下载文件
     *
     * @param ref FileRef
     */
    private void downloadFile(final FileRef ref) {
        // 测试此缓存路径是否存在
        File f = new File(FILE_CACHE_PATH);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        HttpClient client = null;
        HttpEntity entity = null;
        HttpGet request = null;
        InputStream is = null;
        try {
            String url = ref.retryCount <= 0 ? ref.url : ref.backupUrl;
            request = new HttpGet(url);
            client = getThreadSafeHttpClient();
            final HttpResponse response = client.execute(request);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                entity = response.getEntity();
                if (entity != null) {
                    ref.len = entity.getContentLength();
                    is = entity.getContent();
                    boolean show = false;
                    if (ref.len != -1 && ref.progressListener != null) {
                        show = true;
                    }
                    FileOutputStream fos = new FileOutputStream(FILE_CACHE_PATH + "/" + ref.cacheName);
                    byte[] buf = new byte[2048];
                    int readNum = -1; // 每次网络读取数
                    long nowCount = 0;
                    while ((readNum = is.read(buf)) != -1) {
                        if (show) {
                            ref.currentProgress = (nowCount += readNum);
                            lHandler.sendMessage(lHandler.obtainMessage(101, ref));// 图标获取成功
                        }
                        fos.write(buf, 0, readNum);
                    }
                    lHandler.sendMessage(lHandler.obtainMessage(200, ref));// 下载完成
                    fos.flush();
                    fos.close();
                    is.close();
                }
            } else {
                lHandler.sendMessage(lHandler.obtainMessage(statusCode, ref));// 下载失败
            }
        } catch (Exception e) {
            e.printStackTrace();
            lHandler.sendMessage(lHandler.obtainMessage(101, ref));// 图标获取成功
        } finally {
            removePid(ref.pId);
            if (entity != null) {
                try {
                    entity.consumeContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (request != null) {
                request.abort();
            }
            if (client != null && client.getConnectionManager() != null) {
                client.getConnectionManager().shutdown();
            }
        }
    }

    private void removePid(String pId) {
        /**
         * 释放任务列表
         */
        synchronized (P_IDS) {
            if (P_IDS.containsKey(pId)) {
                P_IDS.remove(pId);
            }
        }
    }

    /**
     * @author LDM
     * @DOC 数据队列
     */
    public class QueueImage {
        public final Queue<ImageRef> queue = new LinkedList<ImageRef>();
    }

    // 用来显示图像的Handler
    protected static class SecurityHandler<T extends LazyFileDownloader> extends Handler {
        WeakReference<T> w;

        private SecurityHandler(T t) {
            w = new WeakReference<T>(t);
        }

        @Override
        public void handleMessage(Message msg) {
            FileRef _ref = (FileRef) msg.obj;
            if (_ref == null) {
                return;
            }
            switch (msg.what) {
                case 200: {// 下载完成
                    _ref.completion(_ref.path);
                    break;
                }
                case 101: {// 下载进度
                    if (_ref.progressListener != null && _ref.pId.equals(_ref.progressBar.getTag(TAG_ID))) {
                        _ref.progressListener.progress(_ref.currentProgress, _ref.len);
                    }
                    break;
                }
                case 102: {// 下载失败
                    if (_ref.retryCount <= 0 && _ref.backupUrl != null && w.get() != null) {
                        _ref.retryCount = 1;
                        w.get().addTask(_ref);
                    } else {
                        _ref.error(_ref.errorCode);
                    }
                    break;
                }
                case 103:// 内存溢出
                    if (w.get() != null && w.get().context.get() != null) {
                        LToast.showShort(w.get().context.get(), "可用内存不足，请释放内存后重试！");
                        SystemTool.killBackgroundProcesses(w.get().context.get().getApplicationContext());
                        try {
                            System.gc();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 105:// 磁盘空间
                    if (w.get() != null && w.get().context.get() != null) {
                        LToast.showShort(w.get().context.get(), "手机磁盘空间不足！");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void stop() {
        if (services != null) {
            services.stopTask();
        }
    }

    /**
     * @author ldm 下载文件任务，如果需要进度提醒，传入的progressBar必须实现了OnDownloadProgressListener监听
     */
    public static class FileRef {

        public String pId;// 队列ID 用来控制任务的绑定与解绑关系使用的唯一标识
        public String url, backupUrl;
        public String path;
        public long len = 0;// 大小
        public long currentProgress = 0;
        public int errorCode;
        public int retryCount = 0;// 重试次数，大于等于1时将不继续重试
        public OnDownloadProgressListener progressListener;// 进度接口
        private View progressBar;// 进度View
        public String cacheName;// 缓存名字

        /**
         * 创建一个最简任务
         *
         * @param url url
         */
        public FileRef(String url) {
            this.url = url;
            this.pId = MD5.md5(url);
            this.cacheName = pId;
        }

        public FileRef(String url, String pId) {
            this.url = url;
            this.pId = pId;
            this.cacheName = MD5.md5(url);
        }

        public FileRef(String url, View progressBar) {
            this.url = url;
            this.pId = MD5.md5(url);
            this.cacheName = pId;
            this.progressBar = progressBar;
            if (progressBar instanceof OnDownloadProgressListener) {
                this.progressListener = (OnDownloadProgressListener) progressBar;
            }
        }

        public FileRef(String url, String pId, View progressBar) {
            this.url = url;
            this.pId = pId;
            this.cacheName = MD5.md5(url);
            this.progressBar = progressBar;
            if (progressBar instanceof OnDownloadProgressListener) {
                this.progressListener = (OnDownloadProgressListener) progressBar;
            }
        }

        public FileRef(String url, String pId, String cacheName, View progressBar) {
            this.url = url;
            this.pId = pId;
            this.cacheName = cacheName;
            this.progressBar = progressBar;
            if (progressBar instanceof OnDownloadProgressListener) {
                this.progressListener = (OnDownloadProgressListener) progressBar;
            }
        }

        /**
         * 设置备用URL
         *
         * @param backupUrl 备用地址
         * @return FileRef
         */
        public FileRef setBackupUrl(String backupUrl) {
            this.backupUrl = backupUrl;
            return this;
        }

        /**
         * 当任务被加入到队列中之后，将会进行pid的同步
         */
        public void syncPid() {
            if (this.progressBar != null) {
                this.progressBar.setTag(TAG_ID, pId + "");
            }
        }

        public void completion(String path) {

        }

        public void error(int errorCode) {

        }
    }

    /**
     * 当需要下载进度提醒时，开发者需要在提示进度的View
     *
     * @author ldm
     */
    interface OnDownloadProgressListener {

        void progress(long progress, long count);
    }

}
