package com.ldm.basic.utils.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;

import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.app.BasicRuntimeCache;
import com.ldm.basic.app.Configuration;
import com.ldm.basic.res.BitmapHelper;
import com.ldm.basic.utils.FileTool;
import com.ldm.basic.utils.HttpFileTool;
import com.ldm.basic.utils.MD5;
import com.ldm.basic.utils.SystemTool;
import com.ldm.basic.utils.TaskThreadToMultiService;
import com.ldm.basic.utils.TextUtils;
import com.ldm.basic.utils.image.memory.MemoryCache;
import com.ldm.basic.utils.image.memory.UsingFreqLimitedMemoryCache;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ldm on 14-1-22. 开启<N>个线程下载图片, 可以通过sizeLimit设置内存大小
 * LazyImageDownloader的简洁版，不含有进度条处理能力，适合列表页使用
 */
public class LazyImageDownloader {

    public static final int LOADER_IMAGE_SUCCESS = 200;
    public static final int LOADER_IMAGE_ERROR = 101;
    public static final int LOADER_IMAGE_WAKE_TASK = 102;
    public static final int LOADER_IMAGE_RECORD_LAST_TIME = 105;
    public static final int LOADER_IMAGE_URL_IS_NULL = 106;
    public static final int LOADER_IMAGE_EXECUTE_END = 107;
    public static final int LOADER_IMAGE_ERROR_OOM = 103;

    private static Context APPLICATION_CONTENT;

    private TaskThreadToMultiService services;
    private TaskThreadToMultiService cacheThreads;
    private TaskThreadToMultiService assignThreads;
    private SecurityHandler<LazyImageDownloader> lHandler;

    /**
     * 如果用户设置了OnScrollListener当滑动状态处于SCROLL_STATE_IDLE/SCROLL_STATE_TOUCH_SCROLL时任务添加正常， 仅当SCROLL_STATE_FLING状态时做任务暂停
     */
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_BUSY = 1;// 当前处于忙碌中，这是进入的任务 全部进入临时任务池
    public static final int SCROLL_STATE_FLING = 2;

    /**
     * 当前滑动状态
     */
    int scrollState = SCROLL_STATE_IDLE;

    /**
     * 阻尼的滑动监听，这里保留引用，方便在销毁任务时释放
     */
    private LazyImageOnScrollListener lazyImageOnScrollListener;

    /**
     * 滑动状态下最大保留任务的数量，默认20
     */
    private int maxTaskNumber = 20;

    // 图片保存路径
    public String IMAGE_CACHE_PATH;

    /**
     * 全局默认的图片缓存路径
     */
    public static String DEFAULT_IMAGE_CACHE_PATH;

    /**
     * 任务列表
     */
    public final Map<String, String> P_IDS = new HashMap<>();

    /**
     * 使用了LazyImageOnScrollListener监听时用来做临时缓存使用
     */
    private final List<ImageOptions> cacheImageOptions = new ArrayList<>();

    /**
     * 滑动处于阻尼时的任务列表， 当LazyImageOnScrollListener状态处于SCROLL_STATE_IDLE时
     */
    final FlingImageRef SCROLL_FLING_P_IDS = new FlingImageRef();

    /**
     * 可以与effectiveViewPosition配合使用， 仅当ImageRef.position > effectiveViewPosition时才会生效
     */
    Drawable defDrawable;

    public boolean useNullFill;

    /**
     * 当notifyDataSetChanged刷新时可以使用这个变量控制刷新失效的View位置，防止多次刷新
     */
    private int failViewPosition = -1;

    /**
     * 默认的内存大小
     */
    private static int memoryCacheSizeLimit = 16 * 1024 * 1024;
    private static UsingFreqLimitedMemoryCache memoryCache;

    /**
     * true表示可用，false需要通过restart()方法重启
     */
    boolean isStart;

    private static LazyImageDownloader lazyImageDownloader;

    public static LazyImageDownloader getInstance() {
        if (APPLICATION_CONTENT == null) {
            APPLICATION_CONTENT = BasicApplication.getApp();
        }
        if (lazyImageDownloader == null) {
            lazyImageDownloader = new LazyImageDownloader(10, 6, DEFAULT_IMAGE_CACHE_PATH);
        }
        return lazyImageDownloader;
    }


    /**
     * 初始化LazyImageDownloader，建议在application中调用（这个方法可以多次调用）
     *
     * @param context               Context 建议使用 ApplicationContext作为参数传递
     * @param defaultImageCachePath 默认的图片路径
     */
    public static void init(Context context, String defaultImageCachePath) {
        APPLICATION_CONTENT = context.getApplicationContext();
        DEFAULT_IMAGE_CACHE_PATH = defaultImageCachePath;
    }


    /**
     * 多线程异步下载工具
     *
     * @param asyncTaskNumber      创建处理下载任务的线程数量
     * @param cacheAsyncTaskNumber 创建处理缓存任务的线程数量
     */
    public LazyImageDownloader(int asyncTaskNumber, int cacheAsyncTaskNumber) {
        restart(asyncTaskNumber, cacheAsyncTaskNumber, Configuration.IMAGE_CACHE_PATH);
    }

    /**
     * 多线程异步下载工具
     *
     * @param asyncTaskNumber      创建处理下载任务的线程数量
     * @param cacheAsyncTaskNumber 创建处理缓存任务的线程数量
     * @param cachePath            缓存路径
     */
    public LazyImageDownloader(int asyncTaskNumber, int cacheAsyncTaskNumber, String cachePath) {
        restart(asyncTaskNumber, cacheAsyncTaskNumber, cachePath);
    }

    /**
     * 当调用stopAllTask()后，可以通过这个方法来重启LazyImageDownloader的下载功能
     *
     * @param asyncTaskNumber      创建处理下载任务的线程数量
     * @param cacheAsyncTaskNumber 创建处理缓存任务的线程数量
     * @param cachePath            缓存路径
     */
    public void restart(int asyncTaskNumber, int cacheAsyncTaskNumber, String cachePath) {
        if (cachePath != null) {
            IMAGE_CACHE_PATH = cachePath;
        }
        /**
         * 检查指定路径是否存在，不存尝试创建
         */
        FileTool.createDirectory(IMAGE_CACHE_PATH);
        scrollState = SCROLL_STATE_IDLE;
        // 用来下载的线程
        if (services != null) {
            services.stopTask();
            services = null;
        }
        services = new TaskThreadToMultiService(asyncTaskNumber);
        // 用来恢复已下载图片时使用
        if (cacheThreads != null) {
            cacheThreads.stopTask();
            cacheThreads = null;
        }
        cacheThreads = new TaskThreadToMultiService(cacheAsyncTaskNumber);
        // 用户处理任务的分配操作的线程，仅开启两个
        if (assignThreads != null) {
            assignThreads.stopTask();
            assignThreads = null;
        }
        assignThreads = new TaskThreadToMultiService(2);
        // 创建一个Handler
        if (lHandler == null) {
            lHandler = new SecurityHandler<>(this);
        }
        isStart = true;
    }

    /**
     * 创建一个图像下载任务，图像下载使用原图大小
     *
     * @param url        图像地址
     * @param view       显示图片的View
     * @param position   如果是在adapter中时，这个可以用getView(...)中的position作为参数，如果没有可以使用0或随机数代替
     * @param imageWidth 目标图片载入内存中宽度
     */
    public synchronized void addTask(String url, View view, int position, int imageWidth) {
        ImageOptions ref = new ImageOptions(url, view, position);
        ref.width = imageWidth;
        ref.useMinWidth = true;
        addTask(ref);
    }

    /**
     * 创建一个图像下载任务，图像下载使用原图大小
     *
     * @param url      图像地址
     * @param view     显示图片的View
     * @param position 如果是在adapter中时，这个可以用getView(...)中的position作为参数，如果没有可以使用0或随机数代替
     */
    public synchronized void addTask(String url, View view, int position) {
        ImageOptions ref = new ImageOptions(url, view, position);
        ref.useMinWidth = true;
        addTask(ref);
    }

    /**
     * 下载单个图片 将ImageRef添加到队列中等待执行
     *
     * @param ref ImageOptions
     */
    public synchronized void addTask(ImageOptions ref) {
        //编译任务
        ref.builder();
        //记录当前任务对应的本地缓存路径
        ref.filePath = getFilePath(ref);
        // 仅下载模式
        if (ref.downloadMode || ref.view == null) {
            /**
             * 下载任务时，当PID存在时将不进行二次任务
             */
            if (!P_IDS.containsKey(ref.pId)) {
                // 同步pid
                ref.syncPid();
                //创建一个解析任务的线程
                createAssignTask(ref);
            }
        } else {

            //过滤掉失效的任务
            if (ref.position <= failViewPosition) {
                return;
            }

            //检查是否设置默认图片
            if (defDrawable != null || useNullFill) {
                ref.setDefaultImage(defDrawable);
            }

            //如果没有URL直接放弃任务
            if (TextUtils.isNull(ref.url)) {
                return;
            }

            // 同步pid
            ref.syncPid();

            /**
             * 如果处于任务导入时这里要将任务存储到缓存中，等待任务导入结束后
             */
            if (scrollState == SCROLL_STATE_BUSY) {
                cacheImageOptions.add(ref);
                return;
            } else if (scrollState == SCROLL_STATE_FLING) {// 当前处于阻尼时，不进行任务处理
                SCROLL_FLING_P_IDS.put(ref.pId, ref);
                synchronized (SCROLL_FLING_P_IDS) {
                    if (SCROLL_FLING_P_IDS.size() > maxTaskNumber) {
                        SCROLL_FLING_P_IDS.removeFirst();
                    }
                }
                return;
            }

            /**
             * 回填处于阻尼时的任务
             */
            if (cacheImageOptions.size() > 0) {
                cacheImageOptions.add(ref);
                addTask(cacheImageOptions.get(0));
                return;
            }
            genTask(ref);
        }
    }

    private void genTask(ImageOptions ref) {
        String cacheName = getCacheName(ref);
        ref.bitmap = getMemoryCache().get(getMemoryCacheKey(cacheName, ref.width * ref.height));
        if (ref.bitmap != null && !ref.bitmap.isRecycled()) {
            ref.loadSuccess = true;
            ref.onSuccess(APPLICATION_CONTENT);// 设置图像
            ref.end();
        } else {
            if (ref.progressView != null) {
                ref.progressView.setVisibility(View.VISIBLE);
            }
            createAssignTask(ref);
        }
    }

    /**
     * 内部使用的批量导入任务的功能
     *
     * @param refs 任务列表
     */
    void addTaskAll(List<ImageOptions> refs) {
        for (ImageOptions ref : refs) {
            if (ref.downloadMode || ref.view == null) {// 仅下载模式
                /**
                 * 下载任务时，当PID存在时将不进行二次任务
                 */
                if (!P_IDS.containsKey(ref.pId)) {
                    createAssignTask(ref);
                }
            } else {
                /**
                 * 过滤掉失效的任务
                 */
                if (ref.position <= failViewPosition) {
                    return;
                }

                /**
                 * 检查任务是否有必要
                 */
                if (ref.position != 0 && defDrawable == null && ref.pId.equals(String.valueOf(ref.view.getTag(ImageOptions.TAG_ID)))) {
                    Drawable d;
                    if (ref.imageToSrc) {
                        ImageView iv = (ImageView) ref.view;
                        d = iv.getDrawable();
                    } else {
                        d = ref.view.getBackground();
                    }
                    if (d != null) {
                        return;// 数据匹配，不需要重建任务
                    }
                }
                // 同步pid
                ref.syncPid();
                // 生成任务
                genTask(ref);
            }
        }
    }

    /**
     * 创建一个异步解析任务
     *
     * @param ref ImageOptions
     */
    private void createAssignTask(ImageOptions ref) {
        if (assignThreads == null) {
            return;
        }
        synchronized (P_IDS) {
            ref.UUID = UUID.randomUUID().toString();
            P_IDS.put(ref.pId, ref.UUID);
        }
        assignThreads.addTask(new LoaderAssignTask(ref, this));
    }

    /**
     * 根据指定缓存名获取文件缓存全地址
     *
     * @param ref ImageOptions
     * @return path
     */
    public String getFilePath(ImageOptions ref) {
        String path = TextUtils.isNull(ref.localDirectory) ? IMAGE_CACHE_PATH : ref.localDirectory;
        return path + "/" + (ref.cacheName + (ref.uSuffix == null ? "" : "." + ref.uSuffix));
    }

    /**
     * 根据指定的数据返回这个文件的全路径
     *
     * @param cachePath 缓存目录
     * @param url       URL
     * @param uSuffix   后缀
     * @return file path
     */
    public static String getFilePath(String cachePath, String url, String uSuffix) {
        return (cachePath + "/" + MD5.md5(url) + (uSuffix == null ? "" : "." + uSuffix));
    }

    /**
     * 返回这个文件是否存在
     *
     * @param cachePath 缓存目录
     * @param url       URL
     * @param uSuffix   后缀
     * @return true/false 返回是否存在
     */
    public static boolean isFileExist(String cachePath, String url, String uSuffix) {
        return new File((cachePath + "/" + MD5.md5(url) + "." + uSuffix)).exists();
    }

    /**
     * 返回这个任务的cacheName，如果有后缀会自动拼接上
     *
     * @param ref ImageOptions
     * @return 带后缀的cacheName
     */
    String getCacheName(ImageOptions ref) {
        return ref.cacheName + (ref.uSuffix == null ? "" : "." + ref.uSuffix);
    }

    /**
     * 添加缓存任务
     *
     * @param ref ImageOptions
     */
    void addCacheTask(ImageOptions ref, String filePath, String cacheName) {
        if (cacheThreads == null) {
            return;
        }
        cacheThreads.addTask(new LoaderCacheTask(ref, filePath, cacheName, this));
    }

    /**
     * 这个方法用来清除任务及唤醒处于睡眠中的任务
     *
     * @param ref ImageOptions
     */
    void removePid(ImageOptions ref) {
        if (P_IDS != null && ref != null && ref.pId != null) {
            synchronized (P_IDS) {
                if (P_IDS.containsKey(ref.pId) && ref.UUID.equals(P_IDS.get(ref.pId))) {
                    P_IDS.remove(ref.pId);
                }
            }
        }
    }

    /**
     * 根据给定的信息将图片从本地路径中读取出来并设置的ImageRef中
     *
     * @param ref      ImageOptions
     * @param path      地址
     * @param cacheName 缓存名称
     */
    void createImage(ImageOptions ref, String path, String cacheName) {
        int targetWidth = ref.width, targetHeight = ref.height;
        if (ref.useMinWidth) {
            int w = BitmapHelper.getBitmapSize(path)[0];
            targetWidth = Math.min(w, ref.width);
            if (ref.height > 0) {
                targetHeight = (int) (w * 1.0f / ref.width * ref.height);
            }
        }
        try {
            ref.loadSuccess = ref.onAsynchronous(path, targetWidth, targetHeight);
        } catch (OutOfMemoryError e) {
            sendMessage(LOADER_IMAGE_ERROR_OOM, null);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ref.bitmap != null && ref.view != null) {
            getMemoryCache().put(getMemoryCacheKey(cacheName, targetWidth * targetHeight), ref.bitmap);
        }
    }

    private String getMemoryCacheKey(String cacheName, int size) {
        return cacheName + "_" + size;
    }

    /**
     * 任务分配器，该方法可以根据各线程的繁忙度动态的分配任务
     */
    void addDownloadTask(ImageOptions ref) {
        if (services == null) {
            return;
        }
        services.addTask(new LoaderDownloadTask(ref, this));
    }

    synchronized boolean checkAvailability(ImageOptions ref) {
        final String pId = ref.pId;
        if (!pId.equals(String.valueOf(ref.view.getTag(ImageOptions.TAG_ID)))) {
            removePid(ref);
            return false;// 如果pid已经无法对应直接放弃本次任务
        }
        synchronized (P_IDS) {
            if (P_IDS.containsKey(pId)) {
                // 检查是否需要清除PID
                String uuid = P_IDS.get(pId);
                // 如果不相等，表示任务已经失效，被新任务替代
                if (!uuid.equals(ref.UUID)) {
                    return false;// 放弃这次任务
                }
            }
        }
        return true;
    }

    /**
     * 创建一个下载任务，如果失败将发送101代码
     *
     * @param ref ImageOptions
     */
    void createDownloadTask(ImageOptions ref) {
        //下载状态
        String state = null;
        //下载后的路径
        String path = null;
        final String cacheName = getCacheName(ref);
        final String filePath = ref.filePath;
        File f = new File(filePath);
        /**
         * 如果文件存在且任务于maxIgnoreTime内创建，将忽略这个下载任务， 直接返回文件地址，这样可以过滤掉网络不稳定时导致文件重复下载的问题
         */
        if (f.exists() && System.currentTimeMillis() - f.lastModified() < 60000) {
            state = "200";
            path = filePath;
        } else {
            String url = ref.getUrl();
            if (!TextUtils.isNull(url) && url.startsWith("http")) {
                String[] info = HttpFileTool.httpToFile2(url, filePath);
                state = info[0];
                path = info[1];
            }
        }
        if ("200".equals(state)) {
            fileDownloadSuccess(ref, 200, path, cacheName);
            // 任务完成后删除
            removePid(ref);
        } else {
            fileDownloadError(ref, state);
        }
    }

    /**
     * 文件下载成功后的逻辑
     *
     * @param ref          ImageOptions
     * @param responseCode 状态
     * @param path         本地地址
     * @param cacheName    缓存名
     */
    private void fileDownloadSuccess(ImageOptions ref, int responseCode, String path, String cacheName) {
        ref.responseCode = responseCode;
        if (!ref.downloadMode) {
            // 创建图像
            createImage(ref, path, cacheName);
            // 检查图片是否可以使用，如果可以发送200通知
            if (ref.loadSuccess) {
                sendMessage(LOADER_IMAGE_SUCCESS, ref);
                // 下载成功后维护全局缓存
                BasicRuntimeCache.IMAGE_PATH_CACHE.put(cacheName, path);
            } else {
                // 图片不可用,删除本地文件
                FileTool.delete(path);
                sendMessage(LOADER_IMAGE_ERROR, ref);
            }
        } else {
            /**
             * 离线下载的任务会执行一次end()
             */
            sendMessage(LOADER_IMAGE_EXECUTE_END, ref);
        }
    }

    /**
     * 网络文件下载失败是执行的逻辑
     *
     * @param ref   ImageOptions
     * @param state 状态
     */
    private void fileDownloadError(ImageOptions ref, String state) {
        // 离线任务如果下载失败，将不进行重新下载
        if (!ref.downloadMode) {
            removePid(ref);
            if ("url is null".equals(state)) {
                sendMessage(LOADER_IMAGE_URL_IS_NULL, ref);//放弃任务，不在继续处理
            } else {
                if (TextUtils.isNumber(state)) {
                    ref.responseCode = TextUtils.parseInt(state, 0);
                }
                if (state != null) {
                    if (state.contains("No space left on device")) {
                        sendMessage(LOADER_IMAGE_RECORD_LAST_TIME, null);// 内存不足
                    } else {
                        sendMessage(101, ref);//发送重新下载消息
                    }
                }
            }
        } else {
            //离线下载的任务会执行一次end()
            sendMessage(LOADER_IMAGE_EXECUTE_END, ref);
        }
    }

    /**
     * 取消对应View下载任务
     *
     * @param v View
     */
    public static void cancelViewTask(View v) {
        if (v != null) {
            v.setTag(ImageOptions.TAG_ID, "");
            v.setTag(ImageOptions.CREATE_TIME_ID, "");
        }
    }

    /**
     * 取消对应View下载任务
     *
     * @param v   View
     * @param pId 唯一标识
     */
    public void cancelViewTask(View v, String pId) {
        synchronized (P_IDS) {
            if (P_IDS.containsKey(pId)) {
                P_IDS.remove(pId);
            }
        }
        if (v != null) {
            v.setTag(ImageOptions.TAG_ID, "");
            v.setTag(ImageOptions.CREATE_TIME_ID, "");
        }
    }

    /**
     * 设置缓存路径
     *
     * @param imageCachePath 缓存路径
     */
    public void setImageCachePath(String imageCachePath) {
        IMAGE_CACHE_PATH = imageCachePath;
    }


    /**
     * send Message to SecurityHandler
     *
     * @param what msg.what
     * @param ref  msg.obj
     */
    void sendMessage(int what, ImageOptions ref) {
        if (lHandler != null) {
            lHandler.sendMessage(lHandler.obtainMessage(what, ref));
        }
    }

    // 用来显示图像的Handler
    protected static class SecurityHandler<T extends LazyImageDownloader> extends Handler {
        private static long lastTime;
        WeakReference<T> w;

        private SecurityHandler(T t) {
            w = new WeakReference<>(t);
        }

        @Override
        public void handleMessage(Message msg) {
            if (w.get() == null || APPLICATION_CONTENT == null) {
                return;
            }
            switch (msg.what) {
                case LOADER_IMAGE_SUCCESS: {// 图标下载成功
                    ImageOptions ref = (ImageOptions) msg.obj;
                    if (ref.view != null && (ref.pId).equals(String.valueOf(ref.view.getTag(ImageOptions.TAG_ID)))) {
                        imageDownloadSuccess(APPLICATION_CONTENT, ref);
                    }
                    break;
                }
                case LOADER_IMAGE_ERROR: {
                    // 图片下载失败
                    ImageOptions ref = (ImageOptions) msg.obj;
                    if (ref != null) {
                        imageDownloadError(ref);
                    }
                    break;
                }
                case LOADER_IMAGE_WAKE_TASK:// 通过handler唤醒任务
                    w.get().addTask((ImageOptions) msg.obj);
                    break;
                case LOADER_IMAGE_ERROR_OOM:// 内存溢出
                    if (System.currentTimeMillis() - lastTime > 3000) {
                        SystemTool.killBackgroundProcesses(APPLICATION_CONTENT.getApplicationContext());
                        lastTime = System.currentTimeMillis();
                    }
                    break;
                case LOADER_IMAGE_RECORD_LAST_TIME:
                    if (System.currentTimeMillis() - lastTime > 3000) {
                        lastTime = System.currentTimeMillis();
                    }
                    break;
                case LOADER_IMAGE_URL_IS_NULL: {//任务下载失败，会触发failed(Context, errorState)方法
                    ImageOptions ref = (ImageOptions) msg.obj;
                    if (ref.view != null && (ref.pId).equals(String.valueOf(ref.view.getTag(ImageOptions.TAG_ID)))) {
                        ref.failed(APPLICATION_CONTENT, -1);
                        ref.end();
                    }
                }
                break;
                case LOADER_IMAGE_EXECUTE_END: {
                    ImageOptions ref = (ImageOptions) msg.obj;
                    if (ref.view != null && (ref.pId).equals(String.valueOf(ref.view.getTag(ImageOptions.TAG_ID)))) {
                        ref.end();
                    }
                }
                break;
                default:
                    break;
            }
        }

        /**
         * 图像下载成功
         *
         * @param ref ImageOptions
         */
        private void imageDownloadSuccess(Context context, ImageOptions ref) {
            if (ref.loadSuccess) {
                ref.onSuccess(context);// 设置图像
                ref.end();
            }
        }

        /**
         * 图像下载失败
         *
         * @param ref ImageOptions
         */
        private void imageDownloadError(ImageOptions ref) {
            BasicRuntimeCache.IMAGE_PATH_CACHE.remove(ref.cacheName);
            if (ref.view != null) {// 重置View的PID标记，下载失败时必须重置这个标记
                ref.view.setTag(ImageOptions.TAG_ID, "");
            }
            if (w != null && w.get() != null) {
                LazyImageDownloader t = w.get();
                if (ref.retryCount == 0) {// 尝试一次重新下载
                    ref.retryCount = 1;
                    t.addTask(ref);
                } else {
                    if (t.defDrawable != null || t.useNullFill) {
                        ref.setDefaultImage(t.defDrawable);
                    }
                    if (ref.progressView != null) {
                        ref.progressView.setVisibility(View.GONE);
                    }
                    ref.failed(APPLICATION_CONTENT, ref.responseCode);
                    ref.end();
                }
            }
        }
    }

    /**
     * 设置默认图片
     *
     * @param defDrawable Drawable
     */
    public void setDefDrawable(Drawable defDrawable) {
        this.defDrawable = defDrawable;
    }

    public void setUseNullFill(boolean useNullFill) {
        this.useNullFill = useNullFill;
    }

    /**
     * 设置失效位置
     *
     * @param position int
     */
    public void setFailViewPosition(int position) {
        this.failViewPosition = position;
    }

    /**
     * 根据指定的URL，返回这个地址是否处于下载中
     *
     * @param pId 创建任务时的PID
     * @return true下载中
     */
    public boolean isDownloading(String pId) {
        synchronized (P_IDS) {
            return P_IDS.containsKey(pId);
        }
    }

    /**
     * 停止所有任务
     */
    public void stopAllTask() {
        lazyImageOnScrollListener = null;
        if (SCROLL_FLING_P_IDS != null) {
            SCROLL_FLING_P_IDS.clear();
        }
        if (services != null) {
            services.stopTask();
            services = null;
        }
        if (cacheThreads != null) {
            cacheThreads.stopTask();
            cacheThreads = null;
        }
        if (assignThreads != null) {
            assignThreads.stopTask();
            assignThreads = null;
        }
        clearMemoryCache();
        if (P_IDS != null) {
            synchronized (P_IDS) {
                P_IDS.clear();
            }
        }
        isStart = false;
        if (lHandler != null) {
            lHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 清理当前的硬缓存
     */
    public void clearMemoryCache() {
        if (memoryCache != null) {
            memoryCache.clear();
        }
    }

    /**
     * LazyImageDownloader是否可用
     *
     * @return true运行，false已停止
     */
    public boolean isStart() {
        return isStart;
    }

    public static MemoryCache getMemoryCache() {
        if (memoryCache == null) {
            memoryCache = new UsingFreqLimitedMemoryCache(memoryCacheSizeLimit);
        }
        return memoryCache;
    }

    public static int getMemoryCacheSizeLimit() {
        return memoryCacheSizeLimit;
    }

    public static void setMemoryCacheSizeLimit(int memoryCacheSizeLimit) {
        LazyImageDownloader.memoryCacheSizeLimit = memoryCacheSizeLimit;
    }

    /**
     * 返回一个滑动监听，设置到列表后可以对列表滑动加载图像有缓冲，可以减轻滑动时的卡顿问题
     *
     * @param maxTaskNumber 阻尼时最大保留的任务数量
     * @return LazyImageOnScrollListener
     */
    public LazyImageOnScrollListener getLazyImageOnScrollListener(int maxTaskNumber) {
        if (lazyImageOnScrollListener == null) {
            lazyImageOnScrollListener = new LazyImageOnScrollListener(this, null);
        }
        this.maxTaskNumber = maxTaskNumber;
        return lazyImageOnScrollListener;
    }

    /**
     * 返回一个滑动监听，设置到列表后可以对列表滑动加载图像有缓冲，可以减轻滑动时的卡顿问题
     *
     * @param onScrollListener OnScrollListener
     * @param maxTaskNumber    阻尼时最大保留的任务数量
     * @return LazyImageOnScrollListener
     */
    public LazyImageOnScrollListener getLazyImageOnScrollListener(OnScrollListener onScrollListener, int maxTaskNumber) {
        if (lazyImageOnScrollListener == null) {
            lazyImageOnScrollListener = new LazyImageOnScrollListener(this, onScrollListener);
        }
        this.maxTaskNumber = maxTaskNumber;
        return lazyImageOnScrollListener;
    }

    /**
     * 阻尼时用来做暂存任务的容器
     *
     * @author ldm
     */
    class FlingImageRef {
        List<ImageOptions> refs = new ArrayList<>();

        public void put(String key, ImageOptions r) {
            // 如果有重复的任务，先做remove
            int len = refs.size();
            for (int i = len - 1; i >= 0; i--) {
                if (key.equals(refs.get(i).pId)) {
                    refs.remove(i);
                    break;
                }
            }
            // 新的任务保持加载最后
            refs.add(r);
        }

        public void removeFirst() {
            refs.remove(0);
        }

        public void clear() {
            refs.clear();
        }

        public int size() {
            return refs.size();
        }
    }

}
