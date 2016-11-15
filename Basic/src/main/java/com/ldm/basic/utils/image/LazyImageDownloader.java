package com.ldm.basic.utils.image;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;

import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.app.BasicRuntimeCache;
import com.ldm.basic.app.Configuration;
import com.ldm.basic.res.BitmapHelper;
import com.ldm.basic.utils.FileTool;
import com.ldm.basic.utils.MD5;
import com.ldm.basic.utils.SystemTool;
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
 * Created by ldm on 14-1-22.
 * 开启<N>个线程下载图片, 可以通过sizeLimit设置内存大小
 * LazyImageDownloader的简洁版，不含有进度条处理能力，适合列表页使用
 */
public class LazyImageDownloader {

    private static WeakReference<Context> applicationContent;

    private WeakReference<Activity> activityWeakReference;

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
    private String imageCachePath;

    /**
     * 全局默认的图片缓存路径
     */
    private static String defaultImageCachePath;

    /**
     * 任务列表
     */
    private final Map<String, String> P_IDS = new HashMap<>();

    /**
     * 使用了LazyImageOnScrollListener监听时用来做临时缓存使用
     */
    private final List<ImageOptions> cacheImageOptions = new ArrayList<>();

    /**
     * 滑动处于快速滑动时的任务列表， 当LazyImageOnScrollListener状态处于SCROLL_STATE_IDLE时
     */
    final FlingImageRef SCROLL_FLING_P_IDS = new FlingImageRef();

    /**
     * 占位符图片
     */
    private Drawable placeholder;

    /**
     * 当notifyDataSetChanged刷新时可以使用这个变量控制刷新失效的View位置，防止多次刷新
     */
    private int failViewPosition = -1;

    /**
     * 默认的内存大小
     */
    private static int memoryCacheSizeLimit = 16 * 1024 * 1024;
    private static UsingFreqLimitedMemoryCache memoryCache;

    private int downloadTaskNumber;
    private int cacheAsyncTaskNumber;

    /**
     * true表示可用，false需要通过restart()方法重启
     */
    boolean isStart;

    private LazyImageThreadService threadService;

    private static LazyImageDownloader lazyImageDownloader;

    /**
     * 使用这个方法获取default LazyImageDownloader时，需要先触发一次  LazyImageDownloader.init(activity, defaultImageCachePath)
     *
     * @return LazyImageDownloader
     */
    public static LazyImageDownloader getDefaultInstance() {
        if (applicationContent == null) {
            applicationContent = new WeakReference<Context>(BasicApplication.getApp());
        }
        if (lazyImageDownloader == null) {
            lazyImageDownloader = new LazyImageDownloader(10, 6, defaultImageCachePath);
        }
        return lazyImageDownloader;
    }

    /**
     * 初始化LazyImageDownloader，建议在application中调用（这个方法可以多次调用）
     *
     * @param context               Context 建议使用 ApplicationContext作为参数传递
     * @param defaultImageCachePath 默认的图片路径
     */
    public static void initDefaultConfig(Context context, String defaultImageCachePath) {
        if (context instanceof Application) {
            applicationContent = new WeakReference<>(context);
        } else {
            applicationContent = new WeakReference<>(context.getApplicationContext());
        }
        LazyImageDownloader.defaultImageCachePath = defaultImageCachePath;
    }

    public void bindActivity(Activity activity) {
        if (activityWeakReference != null) {
            activityWeakReference.clear();
        }
        if (activityWeakReference == null) {
            activityWeakReference = new WeakReference<>(activity);
        }
    }

    public Context getContent() {
        if (activityWeakReference != null && activityWeakReference.get() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (!activityWeakReference.get().isDestroyed()) {
                    return activityWeakReference.get();
                }
            } else {
                return activityWeakReference.get();
            }
        }
        return applicationContent == null ? null : applicationContent.get();
    }

    /**
     * 是否绑定了Activity
     *
     * @return boolean
     */
    public boolean isBindActivity() {
        return (activityWeakReference != null && activityWeakReference.get() != null);
    }

    /**
     * 返回绑定的Activity是否已经触发了onDestroy();
     *
     * @return true/false activity.isDestroyed();
     */
    public boolean isDestroyed() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activityWeakReference.get().isDestroyed();
    }

    /**
     * 多线程异步下载工具
     *
     * @param downloadTaskNumber   创建处理下载任务的线程数量
     * @param cacheAsyncTaskNumber 创建处理缓存任务的线程数量
     */
    public LazyImageDownloader(int downloadTaskNumber, int cacheAsyncTaskNumber) {
        restart(downloadTaskNumber, cacheAsyncTaskNumber, Configuration.IMAGE_CACHE_PATH);
    }

    /**
     * 多线程异步下载工具
     *
     * @param downloadTaskNumber   创建处理下载任务的线程数量
     * @param cacheAsyncTaskNumber 创建处理缓存任务的线程数量
     * @param cachePath            缓存路径
     */
    public LazyImageDownloader(int downloadTaskNumber, int cacheAsyncTaskNumber, String cachePath) {
        restart(downloadTaskNumber, cacheAsyncTaskNumber, cachePath);
    }

    /**
     * 当调用stopAllTask()后，可以通过这个方法来重启LazyImageDownloader的下载功能
     *
     * @param downloadTaskNumber   创建处理下载任务的线程数量
     * @param cacheAsyncTaskNumber 创建处理缓存任务的线程数量
     * @param cachePath            缓存路径
     */
    public void restart(int downloadTaskNumber, int cacheAsyncTaskNumber, String cachePath) {
        if (cachePath != null) {
            imageCachePath = cachePath;
        }
        /**
         * 检查指定路径是否存在，不存尝试创建
         */
        FileTool.createDirectory(imageCachePath);
        scrollState = SCROLL_STATE_IDLE;
        isStart = true;
        this.downloadTaskNumber = downloadTaskNumber;
        this.cacheAsyncTaskNumber = cacheAsyncTaskNumber;
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
        ref.time1 = System.currentTimeMillis();
        ref.width = imageWidth;
        ref.useMinWidth = true;
        addTask(ref);
    }

    /**
     * 创建一个图像下载任务，图像下载使用原图大小
     *
     * @param url        图像地址
     * @param view       显示图片的View
     * @param imageWidth 目标图片载入内存中宽度
     */
    public synchronized void addTask(String url, View view, int imageWidth) {
        ImageOptions ref = new ImageOptions(url, view, 0);
        ref.width = imageWidth;
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
        if (ref.localDirectory == null) {
            ref.localDirectory = imageCachePath;
        }
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

            //设置默认图片
            ref.setDefaultImage(placeholder);

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
             * 回填处于快速滑动时的任务
             */
            if (cacheImageOptions.size() > 0) {
                cacheImageOptions.add(ref);
                addTask(cacheImageOptions.get(0));
                return;
            }
            genLoaderTask(ref);
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
                if (placeholder == null && ref.isEffectiveTask()) {
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
                genLoaderTask(ref);
            }
        }
    }

    /**
     * 生成加载图片的任务
     *
     * @param ref ImageOptions
     */
    private void genLoaderTask(ImageOptions ref) {
        String cacheName = ref.getCacheName();
        String cacheKey = getMemoryCacheKey(cacheName, ref.width * ref.height);
        ref.bitmap = getMemoryCache().get(cacheKey);
        if (ref.bitmap != null && !ref.bitmap.isRecycled()) {
            ref.loadSuccess = true;
            ref.onSuccess(getContent());// 设置图像
            ref.end();
        } else {
            getMemoryCache().remove(cacheKey);
            if (ref.progressView != null) {
                ref.progressView.setVisibility(View.VISIBLE);
            }
            createAssignTask(ref);
        }
    }

    /**
     * 创建一个异步解析任务
     *
     * @param ref ImageOptions
     */
    private void createAssignTask(ImageOptions ref) {
        synchronized (P_IDS) {
            ref.UUID = UUID.randomUUID().toString();
            P_IDS.put(ref.pId, ref.UUID);
        }
        getTaskThread().createAssignTask(new LoaderAssignTask(ref, this));
    }

    /**
     * 根据指定缓存名获取文件缓存全地址
     *
     * @param ref ImageOptions
     * @return path
     */
    public String getFilePath(ImageOptions ref) {
        String path = TextUtils.isNull(ref.localDirectory) ? imageCachePath : ref.localDirectory;
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
     * 添加缓存任务
     *
     * @param ref ImageOptions
     */
    void addCacheTask(ImageOptions ref, String filePath) {
        getTaskThread().addCacheTask(new LoaderCacheTask(ref, filePath, this));
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
     * @param ref  ImageOptions
     * @param path 地址
     */
    void createImage(ImageOptions ref, String path) {
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
            sendMessage(DisplayUIPresenter.LOADER_IMAGE_ERROR_OOM, null);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ref.bitmap != null && ref.view != null) {
            getMemoryCache().put(getMemoryCacheKey(ref.getCacheName(), targetWidth * targetHeight), ref.bitmap);
        }
    }

    /**
     * 创建一个下载任务
     *
     * @param ref ImageOptions
     */
    void addDownloadTask(ImageOptions ref) {
        getTaskThread().addDownloadTask(new LoaderDownloadTask(ref, this));
    }

    synchronized boolean checkAvailability(ImageOptions ref) {
        boolean result = true;
        if (ref.isEffectiveTask()) {
            synchronized (P_IDS) {
                final String pId = ref.pId;
                if (P_IDS.containsKey(pId)) {
                    // 检查是否需要清除PID
                    String uuid = P_IDS.get(pId);
                    // 如果不相等，表示任务已经失效，被新任务替代
                    if (!uuid.equals(ref.UUID)) {
                        result = false;// 放弃这次任务
                    }
                }
            }
        } else {
            result = false;
            removePid(ref);
        }
        return result;
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

    private String getMemoryCacheKey(String cacheName, int size) {
        return cacheName + "_" + size;
    }

    /**
     * 设置缓存路径
     *
     * @param imageCachePath 缓存路径
     */
    public void setImageCachePath(String imageCachePath) {
        imageCachePath = imageCachePath;
    }


    /**
     * send Message to SecurityHandler
     *
     * @param what msg.what
     * @param ref  msg.obj
     */
    void sendMessage(int what, ImageOptions ref) {
        getTaskThread().sendMessage(what, ref);
    }

    /**
     * 图像下载成功
     *
     * @param ref ImageOptions
     */
    void imageDownloadSuccess(ImageOptions ref) {
        if (ref.isEffectiveTask() && ref.loadSuccess) {
            ref.onSuccess(getContent());// 设置图像
            ref.end();
        }
    }

    /**
     * 加载图片出现OOM时的处理方法
     */
    void loaderImageErrorOom() {
        SystemTool.killBackgroundProcesses(getContent());
    }

    /**
     * 图像url is null 时触发
     *
     * @param ref ImageOptions
     */
    void loaderImageUrlIsNull(ImageOptions ref) {
        if (ref.isEffectiveTask()) {
            ref.failed(getContent(), -1);
            ref.end();
        }
    }

    /**
     * 图像下载失败
     *
     * @param ref ImageOptions
     */
    void imageDownloadError(ImageOptions ref) {
        if (ref == null) return;
        BasicRuntimeCache.IMAGE_PATH_CACHE.remove(ref.cacheName);
        if (ref.view != null) {// 重置View的PID标记，下载失败时必须重置这个标记
            ref.view.setTag(ImageOptions.TAG_ID, "");
        }
        if (ref.retryCount == 0) {// 尝试一次重新下载
            ref.retryCount = 1;
            addTask(ref);
        } else {
            if (placeholder != null) {
                ref.setDefaultImage(placeholder);
            }
            if (ref.progressView != null) {
                ref.progressView.setVisibility(View.GONE);
            }
            ref.failed(getContent(), ref.responseCode);
            ref.end();
        }
    }

    /**
     * 返回一个LazyImageTaskThread工具
     *
     * @return LazyImageTaskThread
     */
    private LazyImageThreadService getTaskThread() {
        if (threadService == null || !threadService.isRunning()) {
            threadService = LazyImageThreadService.create(this, downloadTaskNumber, cacheAsyncTaskNumber);
        }
        return threadService;
    }

    /**
     * 设置默认图片
     *
     * @param placeholder Drawable
     */
    public void setPlaceholder(Drawable placeholder) {
        this.placeholder = placeholder;
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
        clearMemoryCache();
        if (P_IDS != null) {
            synchronized (P_IDS) {
                P_IDS.clear();
            }
        }
        isStart = false;
        if (threadService != null) {
            threadService.release();
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

}
