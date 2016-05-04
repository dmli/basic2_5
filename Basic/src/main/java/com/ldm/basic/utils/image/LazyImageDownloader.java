package com.ldm.basic.utils.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;

import com.ldm.basic.app.BasicRuntimeCache;
import com.ldm.basic.app.Configuration;
import com.ldm.basic.res.BitmapHelper;
import com.ldm.basic.utils.image.memory.MemoryCache;
import com.ldm.basic.utils.image.memory.UsingFreqLimitedMemoryCache;
import com.ldm.basic.utils.FileDownloadTool;
import com.ldm.basic.utils.FileTool;
import com.ldm.basic.utils.MD5;
import com.ldm.basic.utils.SystemTool;
import com.ldm.basic.utils.TaskThreadToMultiService;
import com.ldm.basic.utils.TextUtils;

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
     * 滑动处于阻尼时的任务列表， 当LazyImageOnScrollListener状态处于SCROLL_STATE_IDLE时
     */
    final FlingImageRef SCROLL_FLING_P_IDS = new FlingImageRef();

    /**
     * 文件下载工具
     */
    private FileDownloadTool fdt;

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
    private boolean isStart;

    private static LazyImageDownloader lazyImageDownloader;

    public static LazyImageDownloader getInstance() {
        if (APPLICATION_CONTENT == null) {
            throw new NullPointerException("使用getInstance()时需要先触发 LazyImageDownloader.init(...)! ");
        }
        if (lazyImageDownloader == null) {
            lazyImageDownloader = new LazyImageDownloader(10, 10, DEFAULT_IMAGE_CACHE_PATH);
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

    private final List<ImageOptions> cacheImage = new ArrayList<>();

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
            /**
             * 过滤掉失效的任务
             */
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
                cacheImage.add(ref);
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
            // 回填任务
            if (cacheImage.size() > 0) {
                cacheImage.add(ref);
                addTask(cacheImage.get(0));
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
        assignThreads.addTask(new TaskThreadToMultiService.Task(ref) {
            @Override
            public void taskStart(Object... obj) {
                if (obj[0] == null) {
                    return;
                }
                ImageOptions ref = (ImageOptions) obj[0];
                if (!isStart) {
                    removePid(ref);
                    return;
                }

                // 验证图像是否已经下载过，如果下载过使用addCacheTask(ImageOptions, String)方法创建任务
                final String cacheName = getCacheName(ref);
                if (!BasicRuntimeCache.IMAGE_PATH_CACHE.containsKey(cacheName)) {// 检查本地是否有文件
                    File f = new File(ref.filePath);
                    if (f.exists()) {
                        BasicRuntimeCache.IMAGE_PATH_CACHE.put(cacheName, ref.filePath);
                    }
                }
                // 检查本地是否有缓存
                String path;
                // 如果URL直接对应本地文件
                if (ref.isLocalImage()) {
                    if (new File(ref.url).exists()) {
                        // 使用缓存任务处理
                        addCacheTask(ref, ref.url, cacheName);
                    }
                    removePid(ref);
                } else {
                    path = BasicRuntimeCache.IMAGE_PATH_CACHE.get(cacheName);
                    if (BasicRuntimeCache.IMAGE_PATH_CACHE.containsKey(cacheName) && path != null && new File(path).exists()) {
                        if (ref.downloadMode) {
                            if (lHandler != null) {
                                lHandler.sendMessage(lHandler.obtainMessage(107, ref));// 本地一存在离线任务
                            }
                            removePid(ref);
                        } else {
                            // 使用缓存任务处理
                            addCacheTask(ref, BasicRuntimeCache.IMAGE_PATH_CACHE.get(cacheName), cacheName);
                        }
                    } else {
                        // 使用下载任务
                        addDownloadTask(ref);
                    }
                }
            }
        });
    }

    /**
     * 根据指定缓存名获取文件缓存全地址
     *
     * @param ref ImageOptions
     * @return path
     */
    public String getFilePath(ImageOptions ref) {
        return IMAGE_CACHE_PATH + "/" + (ref.cacheName + (ref.uSuffix == null ? "" : "." + ref.uSuffix));
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
     * 根据置顶的数据返回这个文件的全路径
     *
     * @param cachePath 缓存目录
     * @param url       URL
     * @param uSuffix   后缀
     * @return true/false 返回是否存在
     */
    public static boolean isFileExist(String cachePath, String url, String uSuffix) {
        File f = new File((cachePath + "/" + MD5.md5(url) + "." + uSuffix));
        return f.exists();
    }

    /**
     * 返回这个任务的cacheName，如果有后缀会自动拼接上
     *
     * @param ref ImageOptions
     * @return 带后缀的cacheName
     */
    private String getCacheName(ImageOptions ref) {
        return ref.cacheName + (ref.uSuffix == null ? "" : "." + ref.uSuffix);
    }

    /**
     * 添加缓存任务
     *
     * @param _ref ImageOptions
     */
    private void addCacheTask(ImageOptions _ref, String _path, String cacheName) {
        if (cacheThreads == null) {
            return;
        }
        cacheThreads.addTask(new TaskThreadToMultiService.Task(_ref, _path, cacheName) {
            @Override
            public void taskStart(Object... obj) {
                if (obj[0] == null) {
                    return;
                }
                ImageOptions _ref = (ImageOptions) obj[0];
                if (!isStart) {
                    removePid(_ref);
                    return;
                }
                try {
                    if (!checkAvailability(_ref)) {
                        removePid(_ref);
                        return;
                    }
                    // 创建图像
                    createImage(_ref, String.valueOf(obj[1]), String.valueOf(obj[2]));
                    // 任务完成后删除
                    removePid(_ref);

                    // 检查图片是否可以使用，如果可以发送200通知
                    if (_ref.loadSuccess) {
                        if (lHandler != null) {
                            lHandler.sendMessage(lHandler.obtainMessage(200, _ref));
                        }
                    } else {
                        FileTool.delete(String.valueOf(obj[1]));// 删除本地文件
                        if (lHandler != null) {
                            lHandler.sendMessage(lHandler.obtainMessage(101, _ref));// 发送重新下载消息
                        }
                    }
                } catch (Exception e) {
                    removePid(_ref);
                    if (lHandler != null) {
                        lHandler.sendMessage(lHandler.obtainMessage(101));
                    }
                    e.printStackTrace();
                }
            }
            // 多线程时任务数量过多会导致UI线程setImageBitmap时出现卡顿，这里对读取SD卡上的资源时增加默认100毫秒的延迟
        });
    }

    /**
     * 这个方法用来清除任务及唤醒处于睡眠中的任务
     *
     * @param _ref ImageOptions
     */
    private void removePid(ImageOptions _ref) {
        if (P_IDS != null && _ref != null && _ref.pId != null) {
            synchronized (P_IDS) {
                if (P_IDS.containsKey(_ref.pId) && _ref.UUID.equals(P_IDS.get(_ref.pId))) {
                    P_IDS.remove(_ref.pId);
                }
            }
        }
    }

    /**
     * 根据给定的信息将图片从本地路径中读取出来并设置的ImageRef中
     *
     * @param _ref      ImageOptions
     * @param path      地址
     * @param cacheName 缓存名称
     */
    private void createImage(ImageOptions _ref, String path, String cacheName) {
        int targetWidth = _ref.width, targetHeight = _ref.height;
        if (_ref.useMinWidth) {
            int w = BitmapHelper.getBitmapSize(path)[0];
            targetWidth = Math.min(w, _ref.width);
            if (_ref.height > 0) {
                targetHeight = (int) (w * 1.0f / _ref.width * _ref.height);
            }
        }
        try {
            _ref.loadSuccess = _ref.onAsynchronous(path, targetWidth, targetHeight);
        } catch (OutOfMemoryError e) {
            if (lHandler != null) {
                lHandler.sendMessage(lHandler.obtainMessage(103));
            }
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_ref.bitmap != null && _ref.view != null) {
            getMemoryCache().put(getMemoryCacheKey(cacheName, targetWidth * targetHeight), _ref.bitmap);
        }
    }

    private String getMemoryCacheKey(String cacheName, int size) {
        return cacheName + "_" + size;
    }

    /**
     * 任务分配器，该方法可以根据各线程的繁忙度动态的分配任务
     */
    private void addDownloadTask(ImageOptions ref) {
        if (services == null) {
            return;
        }
        services.addTask(new TaskThreadToMultiService.Task(ref) {
            @Override
            public void taskStart(Object... obj) {
                if (obj[0] == null) {
                    return;
                }
                ImageOptions _ref = (ImageOptions) obj[0];
                if (!isStart) {
                    removePid(_ref);
                    return;
                }
                try {
                    if (!_ref.downloadMode) {
                        if (!checkAvailability(_ref)) {
                            removePid(_ref);
                            return;
                        }
                    }
                    // 创建下载任务
                    createDownloadTask(_ref);
                } catch (Exception e) {
                    // 任务完成后删除
                    removePid(_ref);
                    if (lHandler != null) {
                        lHandler.sendMessage(lHandler.obtainMessage(101));
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    private synchronized boolean checkAvailability(ImageOptions ref) {
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
     * @param _ref ImageOptions
     */
    private void createDownloadTask(ImageOptions _ref) {
        String path = null;
        final String cacheName = getCacheName(_ref);
        final String filePath = _ref.filePath;
        File f = new File(filePath);
        /**
         * 如果文件存在且任务于maxIgnoreTime内创建，将忽略这个下载任务， 直接返回文件地址，这样可以过滤掉网络不稳定时导致文件重复下载的问题
         */
        if (f.exists() && System.currentTimeMillis() - f.lastModified() < 60000) {
            path = "0::" + filePath;
        } else {
            if (fdt == null) {
                fdt = new FileDownloadTool();
            }
            String url;
            /**
             * 当进行重试时，如果有备用地址将使用备用地址进行下载
             */
            if (_ref.retryCount > 0 && _ref.backupUrl != null && _ref.backupUrl.startsWith("http")) {
                url = _ref.backupUrl;
            } else {
                url = _ref.url;
            }
            if (!TextUtils.isNull(url) && url.startsWith("http")) {
                path = fdt.httpToFile2(url, filePath);
            }
        }
        if (path != null && path.startsWith("0::")) {
            fileDownloadSuccess(_ref, path, cacheName);
            // 任务完成后删除
            removePid(_ref);
        } else {
            fileDownloadError(_ref, path);
        }
    }

    /**
     * 文件下载成功后的逻辑
     *
     * @param _ref      ImageOptions
     * @param path      本地地址
     * @param cacheName 缓存名
     */
    private void fileDownloadSuccess(ImageOptions _ref, String path, String cacheName) {
        String msg = path.substring(3);
        if (TextUtils.isNumber(msg)) {
            _ref.responseCode = TextUtils.parseInt(path, 0);
        }
        if (!_ref.downloadMode) {
            path = path.replace("0::", "");
            // 创建图像
            createImage(_ref, path, cacheName);
            // 检查图片是否可以使用，如果可以发送200通知
            if (_ref.loadSuccess) {
                if (lHandler != null) {
                    lHandler.sendMessage(lHandler.obtainMessage(200, _ref));
                }
                // 下载成功后维护全局缓存
                BasicRuntimeCache.IMAGE_PATH_CACHE.put(cacheName, path);
            } else {
                // 图片不可以使用
                FileTool.delete(path);// 删除本地文件
                if (lHandler != null) {
                    lHandler.sendMessage(lHandler.obtainMessage(101, _ref));// 发送重新下载消息
                }
            }
        } else {
            /**
             * 离线下载的任务会执行一次end()
             */
            if (lHandler != null) {
                lHandler.sendMessage(lHandler.obtainMessage(107, _ref));// 发送重新下载消息
            }
        }
    }

    /**
     * 网络文件下载失败是执行的逻辑
     *
     * @param _ref ImageOptions
     * @param path 本地地址
     */
    private void fileDownloadError(ImageOptions _ref, String path) {
        // 离线任务如果下载失败，将不进行重新下载
        if (!_ref.downloadMode) {
            removePid(_ref);
            if (path == null || path.equals("1::url is null")) {
                /**
                 * 这里放弃任务，不在继续处理
                 */
                lHandler.sendMessage(lHandler.obtainMessage(106, _ref));// 发送重新下载消息
            } else {
                String msg = path.substring(3);
                if (TextUtils.isNumber(msg)) {
                    _ref.responseCode = TextUtils.parseInt(path, 0);
                }
                if (lHandler != null) {
                    if (msg.contains("No space left on device")) {
                        lHandler.sendMessage(lHandler.obtainMessage(105));// 内存不足
                    } else {
                        lHandler.sendMessage(lHandler.obtainMessage(101, _ref));// 发送重新下载消息
                    }
                }
            }
        } else {
            /**
             * 离线下载的任务会执行一次end()
             */
            if (lHandler != null) {
                lHandler.sendMessage(lHandler.obtainMessage(107, _ref));// 发送重新下载消息
            }
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

    // 用来显示图像的Handler
    protected static class SecurityHandler<T extends LazyImageDownloader> extends Handler {
        private static long toastTime;// Toast时间，3秒内不重复弹出
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
                case 200: {// 图标下载成功
                    ImageOptions _ref = (ImageOptions) msg.obj;
                    if (_ref.view != null && (_ref.pId).equals(String.valueOf(_ref.view.getTag(ImageOptions.TAG_ID)))) {
                        imageDownloadSuccess(APPLICATION_CONTENT, _ref);
                    }
                    break;
                }
                case 101: {
                    // 图片下载失败
                    ImageOptions _ref = (ImageOptions) msg.obj;
                    if (_ref != null) {
                        imageDownloadError(_ref);
                    }
                    break;
                }
                case 102:// 通过handler唤醒任务
                    w.get().addTask((ImageOptions) msg.obj);
                    break;
                case 103:// 内存溢出
                    if (System.currentTimeMillis() - toastTime > 3000) {
                        SystemTool.killBackgroundProcesses(APPLICATION_CONTENT.getApplicationContext());
                        try {
                            System.gc();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        toastTime = System.currentTimeMillis();
                    }
                    break;
                case 105:
                    if (System.currentTimeMillis() - toastTime > 3000) {
                        toastTime = System.currentTimeMillis();
                    }
                    break;
                case 106: {
                    ImageOptions _ref = (ImageOptions) msg.obj;
                    if (_ref.view != null && (_ref.pId).equals(String.valueOf(_ref.view.getTag(ImageOptions.TAG_ID)))) {
                        if (w.get().defDrawable != null || w.get().useNullFill) {
                            _ref.setDefaultImage(w.get().defDrawable);
                        }
                        _ref.failed(APPLICATION_CONTENT, -1);
                        _ref.end();
                    }
                }
                break;
                case 107: {
                    ImageOptions _ref = (ImageOptions) msg.obj;
                    if (_ref.view != null && (_ref.pId).equals(String.valueOf(_ref.view.getTag(ImageOptions.TAG_ID)))) {
                        _ref.end();
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
         * @param _ref ImageOptions
         */
        private void imageDownloadSuccess(Context context, ImageOptions _ref) {
            if (_ref.loadSuccess) {
                _ref.onSuccess(context);// 设置图像
                _ref.end();
            }
        }

        /**
         * 图像下载失败
         *
         * @param _ref ImageOptions
         */
        private void imageDownloadError(ImageOptions _ref) {
            BasicRuntimeCache.IMAGE_PATH_CACHE.remove(_ref.cacheName);
            if (_ref.view != null) {// 重置View的PID标记，下载失败时必须重置这个标记
                _ref.view.setTag(ImageOptions.TAG_ID, "");
            }
            if (w != null && w.get() != null) {
                LazyImageDownloader t = w.get();
                if (_ref.retryCount == 0) {// 尝试一次重新下载
                    _ref.retryCount = 1;
                    t.addTask(_ref);
                } else {
                    if (t.defDrawable != null || t.useNullFill) {
                        _ref.setDefaultImage(t.defDrawable);
                    }
                    if (_ref.progressView != null) {
                        _ref.progressView.setVisibility(View.GONE);
                    }
                    _ref.failed(APPLICATION_CONTENT, _ref.responseCode);
                    _ref.end();
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
        List<ImageOptions> ref = new ArrayList<>();

        public void put(String key, ImageOptions r) {
            // 如果有重复的任务，先做remove
            int len = ref.size();
            for (int i = len - 1; i >= 0; i--) {
                if (key.equals(ref.get(i).pId)) {
                    ref.remove(i);
                    break;
                }
            }
            // 新的任务保持加载最后
            ref.add(r);
        }

        public void removeFirst() {
            ref.remove(0);
        }

        public void clear() {
            ref.clear();
        }

        public int size() {
            return ref.size();
        }
    }

}
