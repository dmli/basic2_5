package com.ldm.basic.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;

import com.ldm.basic.app.BasicRuntimeCache;
import com.ldm.basic.app.Configuration;
import com.ldm.basic.dialog.LToast;
import com.ldm.basic.res.BitmapHelper;
import com.ldm.basic.res.memory.MemoryCache;
import com.ldm.basic.res.memory.impl.UsingFreqLimitedMemoryCache;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by ldm on 14-1-22.
 * 开启<N>个线程下载图片, 可以通过sizeLimit设置内存大小 LazyImageDownloader的简洁版，不含有进度条处理能力，适合列表页使用
 */
public class LazyImageDownloader {

    private WeakReference<Context> context;

    private TaskThreadToMultiService services;
    private TaskThreadToMultiService cacheThreads;
    private TaskThreadToMultiService assignThreads;// 用来分配任务的线程
    private SecurityHandler<LazyImageDownloader> lHandler;

    /**
     * 如果用户设置了OnScrollListener当滑动状态处于SCROLL_STATE_IDLE/SCROLL_STATE_TOUCH_SCROLL时任务添加正常， 仅当SCROLL_STATE_FLING状态时做任务暂停
     */
    public static int SCROLL_STATE_IDLE = 0;
    public static int SCROLL_STATE_TOUCH_SCROLL = 1;
    public static int SCROLL_STATE_BUSY = 2;// 当前处于忙碌中，这是进入的任务 全部进入临时任务池
    public static int SCROLL_STATE_FLING = 3;

    /**
     * 当前滑动状态
     */
    private int scrollState = SCROLL_STATE_IDLE;

    /**
     * 阻尼的滑动监听，这里保留引用，方便在销毁任务时释放
     */
    private LazyImageOnScrollListener lazyImageOnScrollListener;

    /**
     * 滑动阻尼状态下最大保留任务的数量，默认20
     */
    private int maxTaskNumber = 20;

    /**
     * 下载过程中用来与PID匹配的value值存储的KEY
     */
    public static final int TAG_ID = 0x59999999;

    public static final int CREATE_TIME_ID = 0x59999998;

    /**
     * 设置后所有下载的图片缓存都将被设置该后缀
     */
    private static String unifiedSuffix;

    // 图片保存路径
    public String IMAGE_CACHE_PATH;
    public static String DEFAULT_IMAGE_CACHE_PATH;

    public final Map<String, String> P_IDS = new HashMap<>();

    /**
     * 滑动处于阻尼时的任务列表， 当LazyImageOnScrollListener状态处于SCROLL_STATE_IDLE时
     */
    private final FlingImageRef SCROLL_FLING_P_IDS = new FlingImageRef();

    /**
     * 阻尼时用来做暂存任务的容器
     *
     * @author ldm
     */
    private class FlingImageRef {
        List<ImageRef> ref = new ArrayList<>();

        public void put(String key, ImageRef r) {
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

    /**
     * 文件下载工具
     */
    private FileDownloadTool fdt;

    /**
     * 可以与effectiveViewPosition配合使用， 仅当ImageRef.position > effectiveViewPosition时才会生效
     */
    Drawable defDrawable;

    /**
     * 自动隐藏，设置true后将在任务成功添加之后自动设置隐藏状态
     */
    private boolean autoInvisible;
    private boolean useNullFill;

    /**
     * 当notifyDataSetChanged刷新时可以使用这个变量控制刷新失效的View位置，防止多次刷新
     */
    private int failViewPosition = -1;

    /**
     * 这个属性需要与ImageRef中的position配合使用， 仅当ImageRef.position > invisiblePosition时autoInvisible才会生效
     */
    private int invisiblePosition;

    /**
     * 当ImageRef开启了动画动能时，可以通过这个属性及ImageRef中的position属性进行过滤，仅当ImageRef.position
     */
    private int animPosition;

    /**
     * 默认的内存大小
     */
    private static int sizeLimit = 24 * 1024 * 1024;
    private static UsingFreqLimitedMemoryCache memoryCache;

    /**
     * true表示可用，false需要通过restart()方法重启
     */
    private boolean isStart;

    /**
     * 多线程异步下载工具（使用默认的缓存路径）
     *
     * @param context         Context
     * @param asyncTaskNumber 创建线程数量，该现成集合仅处理需要下载的任务
     */
    public LazyImageDownloader(Context context, int asyncTaskNumber) {
        this(context, asyncTaskNumber, asyncTaskNumber, TextUtils.isNull(DEFAULT_IMAGE_CACHE_PATH) ? Configuration.IMAGE_CACHE_PATH : DEFAULT_IMAGE_CACHE_PATH);
    }

    /**
     * 多线程异步下载工具
     *
     * @param context         Context
     * @param asyncTaskNumber 创建线程数量，该现成集合仅处理需要下载的任务
     * @param cachePath       缓存路径
     */
    public LazyImageDownloader(Context context, int asyncTaskNumber, String cachePath) {
        this(context, asyncTaskNumber, 1, cachePath);
    }

    /**
     * 多线程异步下载工具
     *
     * @param context              Context
     * @param asyncTaskNumber      创建线程数量，该线程集合仅处理需要下载的任务
     * @param cacheAsyncTaskNumber 创建线程数量，该线程集合仅处理需要下载的任务
     */
    public LazyImageDownloader(Context context, int asyncTaskNumber, int cacheAsyncTaskNumber) {
        restart(context, asyncTaskNumber, cacheAsyncTaskNumber, TextUtils.isNull(DEFAULT_IMAGE_CACHE_PATH) ? Configuration.IMAGE_CACHE_PATH : DEFAULT_IMAGE_CACHE_PATH);
    }

    /**
     * 多线程异步下载工具
     *
     * @param context              Context
     * @param asyncTaskNumber      创建线程数量，该线程集合仅处理需要下载的任务
     * @param cacheAsyncTaskNumber 创建线程数量，该线程集合仅处理需要下载的任务
     * @param cachePath            缓存路径
     */
    public LazyImageDownloader(Context context, int asyncTaskNumber, int cacheAsyncTaskNumber, String cachePath) {
        restart(context, asyncTaskNumber, cacheAsyncTaskNumber, cachePath);
    }

    /**
     * 当调用stopAllTask()后，可以通过这个方法来重启LazyImageDownloader2的下载功能
     *
     * @param asyncTaskNumber      创建线程数量，该线程集合仅处理需要下载的任务
     * @param cacheAsyncTaskNumber 创建线程数量，该线程集合仅处理需要下载的任务
     */
    public void restart(Context context, int asyncTaskNumber, int cacheAsyncTaskNumber) {
        restart(context, asyncTaskNumber, cacheAsyncTaskNumber, TextUtils.isNull(DEFAULT_IMAGE_CACHE_PATH) ? Configuration.IMAGE_CACHE_PATH : DEFAULT_IMAGE_CACHE_PATH);
    }

    /**
     * 当调用stopAllTask()后，可以通过这个方法来重启LazyImageDownloader2的下载功能
     *
     * @param asyncTaskNumber      创建线程数量，该线程集合仅处理需要下载的任务
     * @param cacheAsyncTaskNumber 创建线程数量，该线程集合仅处理需要下载的任务
     * @param cachePath            缓存路径
     */
    public void restart(Context context, int asyncTaskNumber, int cacheAsyncTaskNumber, String cachePath) {
        this.context = new WeakReference<>(context);
        if (cachePath != null) {
            IMAGE_CACHE_PATH = cachePath;
        }
        scrollState = SCROLL_STATE_IDLE;
        animPosition = -1;// 希望出现动画的位置
        invisiblePosition = -1;// 默认-1
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

    private final List<ImageRef> cacheImage = new ArrayList<ImageRef>();

    /**
     * 下载单个图片 将ImageRef添加到队列中等待执行
     *
     * @param ref ImageRef
     */
    public synchronized void addTask(ImageRef ref) {
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
            // if (defDrawable == null && ref.pId.equals(String.valueOf(ref.view.getTag(TAG_ID)))) {
            // Drawable d;
            // if (ref.imageToSrc) {
            // ImageView iv = (ImageView) ref.view;
            // d = iv.getDrawable();
            // } else {
            // d = ref.view.getBackground();
            // }
            // if (d != null) {
            // return;// 数据匹配，不需要重建任务
            // }
            // }

            if (!ref.ignoreDefaultImage && (defDrawable != null || useNullFill)) {
                ref.setDefaultImage(defDrawable);
            }

            if (TextUtils.isNull(ref.url)) {
                return;
            }

            // 同步pid
            ref.syncPid();
            //记录当前任务对应的本地缓存路径
            ref.filePath = getFilePath(ref);
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

    private void genTask(ImageRef ref) {
        String cacheName = getCacheName(ref);
        if (autoInvisible && ref.position > invisiblePosition) {
            ref.view.setVisibility(View.INVISIBLE);
        }
        ref.bitmap = getMemoryCache().get(getMemoryCacheKey(cacheName, ref.width * ref.height));
        if (ref.bitmap != null && !ref.bitmap.isRecycled()) {
            ref.onDrawBitmap(context.get());// 设置图像
            ref.isAnim(false);
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
    private void addTaskAll(List<ImageRef> refs) {
        for (ImageRef ref : refs) {
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
                if (ref.position != 0 && defDrawable == null && ref.pId.equals(String.valueOf(ref.view.getTag(TAG_ID)))) {
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

                genTask(ref);
            }
        }
    }

    /**
     * 创建一个异步解析任务
     *
     * @param ref ImageRef
     */
    private void createAssignTask(ImageRef ref) {
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
                ImageRef ref = (ImageRef) obj[0];
                if (!isStart) {
                    removePid(ref);
                    return;
                }

                // 验证图像是否已经下载过，如果下载过使用addCacheTask(ImageRef, String)方法创建任务
                String cacheName = getCacheName(ref);
                if (!BasicRuntimeCache.IMAGE_PATH_CACHE.containsKey(cacheName)) {// 检查本地是否有文件
                    File f = new File(ref.filePath);
                    if (f.exists()) {
                        BasicRuntimeCache.IMAGE_PATH_CACHE.put(cacheName, ref.filePath);
                    }
                }
                // 检查本地是否有缓存
                String path;
                // 如果URL直接对应本地文件
                if (ref.isLocalImage() && new File(ref.url).exists()) {
                    // 使用缓存任务处理
                    addCacheTask(ref, ref.url, cacheName);
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
                        if (ref.isLocalImage()) {
                            removePid(ref);
                        } else {
                            // 使用下载任务
                            addDownloadTask(ref);
                        }
                    }
                }
            }
        });
    }

    /**
     * 根据指定缓存名获取文件缓存全地址
     *
     * @param ref ImageRef
     * @return path
     */
    public String getFilePath(ImageRef ref) {
        if (ref.localDirectory == null) {
            ref.localDirectory = IMAGE_CACHE_PATH;
        }
        return ref.localDirectory + "/" + (ref.cacheName + (ref.isUnifiedSuffix ? unifiedSuffix : ""));
    }

    /**
     * 返回这个任务的cacheName，如果有后缀会自动拼接上
     *
     * @param _ref ImageRef
     * @return 带后缀的cacheName
     */
    private String getCacheName(ImageRef _ref) {
        return _ref.cacheName + (_ref.isUnifiedSuffix ? unifiedSuffix : "");
    }

    /**
     * 添加缓存任务
     *
     * @param _ref ImageRef
     */
    private void addCacheTask(ImageRef _ref, String _path, String cacheName) {
        cacheThreads.addTask(new TaskThreadToMultiService.Task(_ref, _path, cacheName) {
            @Override
            public void taskStart(Object... obj) {
                if (obj[0] == null) {
                    return;
                }
                ImageRef _ref = (ImageRef) obj[0];
                if (!isStart) {
                    removePid(_ref);
                    return;
                }
                try {
                    if (!checkAvailability(_ref)) {
                        removePid(_ref);
                        return;
                    }
                    if (_ref.isMovie) {
                        createMovie(_ref, String.valueOf(obj[1]));
                    } else {
                        // 创建图像
                        createImage(_ref, String.valueOf(obj[1]), String.valueOf(obj[2]));
                    }
                    // 任务完成后删除
                    removePid(_ref);

                    // 检查图片是否可以使用，如果可以发送200通知
                    if (!censorResult(_ref)) {
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
     * @param _ref ImageRef
     */
    private void removePid(ImageRef _ref) {
        if (P_IDS != null && _ref != null && _ref.pId != null) {
            synchronized (P_IDS) {
                if (P_IDS.containsKey(_ref.pId) && _ref.UUID.equals(P_IDS.get(_ref.pId))) {
                    P_IDS.remove(_ref.pId);
                }
            }
        }
    }

    /**
     * 检查图片是否可以使用，如果可以发送200通知， 不可以返回false
     *
     * @param _ref ImageRef
     * @return true成功
     */
    private synchronized boolean censorResult(ImageRef _ref) {
        if (_ref.isMovie) {
            if (_ref.gif != null) {
                // 检查是否需要取消动画
                if (_ref.isAnim && _ref.position <= animPosition) {
                    _ref.isAnim(false);// 取消本次动画
                }
                if (lHandler != null) {
                    lHandler.sendMessage(lHandler.obtainMessage(200, _ref));
                }
                return true;
            }
        } else {
            if (_ref.bitmap != null) {
                // 检查是否需要取消动画
                if (_ref.isAnim && _ref.position <= animPosition) {
                    _ref.isAnim(false);// 取消本次动画
                }
                if (lHandler != null) {
                    lHandler.sendMessage(lHandler.obtainMessage(200, _ref));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 根据给定的信息将图片从本地路径中读取出来并设置的ImageRef中
     *
     * @param _ref      ImageRef
     * @param path      地址
     * @param cacheName 缓存名称
     */
    private void createImage(ImageRef _ref, String path, String cacheName) {
        int loadImageWidth = _ref.width, loadImageHeight = _ref.height;
        if (_ref.useMinWidth) {
            int w = BitmapHelper.getBitmapSize(path)[0];
            loadImageWidth = Math.min(w, _ref.width);
            if (_ref.height > 0) {
                loadImageHeight = (int) (w * 1.0f / _ref.width * _ref.height);
            }
        }
        try {
            if (_ref instanceof ImageRefAsyncBitmap) {
                _ref.bitmap = ((ImageRefAsyncBitmap) _ref).onAsynchronous(path);
            } else if (_ref instanceof ImageRefRoundedCornerBitmap) {
                _ref.bitmap = ((ImageRefRoundedCornerBitmap) _ref).onAsynchronous(path, loadImageWidth, loadImageHeight);
            } else if (_ref instanceof ImageRefRoundBitmap) {
                _ref.bitmap = ((ImageRefRoundBitmap) _ref).onAsynchronous(path, loadImageWidth, loadImageHeight);
            } else {
                if (SystemTool.SYS_SDK_INT <= Build.VERSION_CODES.JELLY_BEAN && _ref.url != null && _ref.url.toUpperCase(Locale.CHINESE).endsWith(".WEBP")) {
                    FileTool ft = new FileTool();
                    Bitmap bmp = BitmapHelper.decodeWebp(ft.openFile(path));
                    if (bmp != null) {
                        _ref.bitmap = BitmapHelper.resetBitmapThrowsOutOfMemoryError(bmp, path, loadImageWidth, loadImageHeight);
                    }
                } else {
                    _ref.bitmap = BitmapHelper.getBitmapThrowsOutOfMemoryError(path, loadImageWidth, loadImageHeight);
                }
            }
        } catch (OutOfMemoryError e) {
            if (lHandler != null) {
                lHandler.sendMessage(lHandler.obtainMessage(103));
            }
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_ref.bitmap != null && _ref.view != null) {
            getMemoryCache().put(getMemoryCacheKey(cacheName, loadImageWidth * loadImageHeight), _ref.bitmap);
        }
    }

    /**
     * 根据给定的信息将图片从本地路径中读取出来并设置的ImageRef中
     *
     * @param _ref ImageRef
     * @param path 地址
     */
    private synchronized void createMovie(ImageRef _ref, String path) {
        try {
            if (_ref instanceof ImageRefAsyncBitmap) {
                _ref.gif = ((ImageRefAsyncBitmap) _ref).onAsynchronousGif(path);
            } else if (_ref instanceof ImageRefRoundedCornerBitmap) {
                _ref.gif = ((ImageRefRoundedCornerBitmap) _ref).onAsynchronousGif(path);
            } else if (_ref instanceof ImageRefRoundBitmap) {
                _ref.gif = ((ImageRefRoundBitmap) _ref).onAsynchronousGif(path);
            } else {
                _ref.gif = new GifDrawable(path);
            }
        } catch (OutOfMemoryError e) {
            if (lHandler != null) {
                lHandler.sendMessage(lHandler.obtainMessage(103));
            }
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMemoryCacheKey(String cacheName, int size) {
        return cacheName + "_" + size;
    }

    /**
     * 任务分配器，该方法可以根据各线程的繁忙度动态的分配任务
     */
    private void addDownloadTask(ImageRef ref) {
        services.addTask(new TaskThreadToMultiService.Task(ref) {
            @Override
            public void taskStart(Object... obj) {
                if (obj[0] == null) {
                    return;
                }
                ImageRef _ref = (ImageRef) obj[0];
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

    private synchronized boolean checkAvailability(ImageRef ref) {
        final String pId = ref.pId;
        if (!pId.equals(String.valueOf(ref.view.getTag(TAG_ID)))) {
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
     * @param _ref ImageRef
     */
    private void createDownloadTask(ImageRef _ref) {
        String path = null;
        String cacheName = getCacheName(_ref);
        String filePath = _ref.filePath;
        File f = new File(filePath);
        /**
         * 如果文件存在切任务于1分钟内创建，将忽略这个下载任务， 直接返回文件地址，这样可以过滤掉网络不稳定时导致文件重复下载的问题
         */
        if (f.exists() && System.currentTimeMillis() - f.lastModified() < _ref.maxIgnoreTime) {
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
            if (!_ref.downloadMode) {
                path = path.replace("0::", "");
                if (_ref.autoVerifyFileType) {
                    if ("gif".equals(FileType.getFileType(path))) {
                        _ref.isMovie = true;
                    }
                }
                if (_ref.isMovie) {
                    createMovie(_ref, path);
                } else {
                    // 创建图像
                    createImage(_ref, path, cacheName);
                }
                // 检查图片是否可以使用，如果可以发送200通知
                if (censorResult(_ref)) {
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
            // 任务完成后删除
            removePid(_ref);
        } else {
            // 离线任务如果下载失败，将不进行重新下载
            if (!_ref.downloadMode) {
                removePid(_ref);
                if (path == null || path.equals("1::url is null")) {
                    /**
                     * 这里放弃任务，不在继续处理
                     */
                    if (lHandler != null) {
                        lHandler.sendMessage(lHandler.obtainMessage(106, _ref));// 发送重新下载消息
                    }
                } else {
                    String msg = path.substring(3);
                    if (lHandler != null) {
                        if (msg.contains("No space left on device")) {
                            lHandler.sendMessage(lHandler.obtainMessage(105));// 内存不足
                        } else {
                            if (TextUtils.isNumber(msg)) {
                                _ref.responseCode = TextUtils.parseInt(path, 0);
                            }
                            lHandler.sendMessage(lHandler.obtainMessage(101, _ref));// 发送重新下载消息
                        }
                    }
                }
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
            v.setTag(TAG_ID, "");
            v.setTag(CREATE_TIME_ID, "");
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
            v.setTag(TAG_ID, "");
            v.setTag(CREATE_TIME_ID, "");
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
        WeakReference<T> w;

        private SecurityHandler(T t) {
            w = new WeakReference<>(t);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200: {// 图标下载成功
                    ImageRef _ref = (ImageRef) msg.obj;
                    if (_ref.view != null && (_ref.pId).equals(String.valueOf(_ref.view.getTag(TAG_ID)))) {
                        if (w.get() != null) {
                            imageDownloadSuccess(w.get().getContext(), _ref);
                        }
                    }
                    break;
                }
                case 101: {
                    // 图片下载失败
                    ImageRef _ref = (ImageRef) msg.obj;
                    if (_ref != null) {
                        imageDownloadError(_ref);
                    }
                    break;
                }
                case 102:// 通过handler唤醒任务
                    if (w.get() != null && w.get().getContext() != null) {
                        w.get().addTask((ImageRef) msg.obj);
                    }
                    break;
                case 103:// 内存溢出
                    if (w.get() != null && w.get().getContext() != null) {
                        LToast.showShort(w.get().getContext(), "可用内存不足，请释放内存后重试！");
                        SystemTool.killBackgroundProcesses(w.get().getContext().getApplicationContext());
                        try {
                            System.gc();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 105:
                    if (w.get() != null && w.get().getContext() != null) {
                        LToast.showShort(w.get().getContext(), "手机磁盘空间不足！");
                    }
                    break;
                case 106: {
                    ImageRef _ref = (ImageRef) msg.obj;
                    if (_ref.view != null && (_ref.pId).equals(String.valueOf(_ref.view.getTag(TAG_ID)))) {
                        if (w.get() != null) {
                            _ref.setDefaultImage(w.get().defDrawable);
                            _ref.error(w.get().getContext(), -1);
                            _ref.end();
                        }
                    }
                }
                break;
                case 107: {
                    ImageRef _ref = (ImageRef) msg.obj;
                    if (_ref.view != null && (_ref.pId).equals(String.valueOf(_ref.view.getTag(TAG_ID)))) {
                        if (w.get() != null) {
                            _ref.responseCode = 200;
                            _ref.end();
                        }
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
         * @param _ref ImageRef
         */
        private void imageDownloadSuccess(Context context, ImageRef _ref) {
            if (_ref.isMovie) {
                if (_ref.gif != null) {
                    _ref.onDrawMovie(_ref.gif);// 设置图像
                    _ref.end();
                }
            } else {
                if (_ref.bitmap != null && !_ref.bitmap.isRecycled()) {
                    _ref.onDrawBitmap(context);// 设置图像
                    _ref.end();
                }
            }
        }

        /**
         * 图像下载失败
         *
         * @param _ref ImageRef
         */
        private void imageDownloadError(ImageRef _ref) {
            BasicRuntimeCache.IMAGE_PATH_CACHE.remove(_ref.cacheName);
            if (_ref.view != null) {// 重置View的PID标记，下载失败时必须重置这个标记
                _ref.view.setTag(TAG_ID, "");
            }
            if (w != null && w.get() != null) {
                LazyImageDownloader t = w.get();
                if (_ref.retryCount == 0) {// 尝试一次重新下载
                    _ref.retryCount = 1;
                    t.addTask(_ref);
                } else {
                    _ref.setDefaultImage(t.defDrawable);
                    _ref.isAnim(false);
                    if (_ref.progressView != null) {
                        _ref.progressView.setVisibility(View.GONE);
                    }
                    _ref.error(t.getContext(), _ref.responseCode);
                    _ref.end();
                }
            }
        }
    }

    public Context getContext() {
        return context == null ? null : context.get();
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
     * 可以通过这个变量及ImageRef.position属性对setAutoInvisible(true)进行过滤 （这个属性可能仅在listView中可以使用到）
     *
     * @param invisiblePosition 需要隐藏的view的最小position
     */
    public void setInvisiblePosition(int invisiblePosition) {
        this.invisiblePosition = invisiblePosition;
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
     * 当ImageRef开启了动画动能时，可以通过这个属性及ImageRef中的position属性进行过滤，仅当ImageRef.position > animPosition时才会播放动画
     *
     * @param animPosition 需要执行动画的view的最小position
     */
    public void setAnimPosition(int animPosition) {
        this.animPosition = animPosition;
    }

    /**
     * 自动隐藏，设置true后将在任务成功添加之后自动设置隐藏状态
     *
     * @param autoInvisible boolean
     */
    public void setAutoInvisible(boolean autoInvisible) {
        this.autoInvisible = autoInvisible;
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
        if (memoryCache != null) {
            memoryCache.clear();
        }
        if (P_IDS != null) {
            synchronized (P_IDS) {
                P_IDS.clear();
            }
        }
        isStart = false;
        if (lHandler != null) {
            lHandler.removeCallbacksAndMessages(null);
            lHandler = null;
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
     * LazyImageDownloader2是否可用
     *
     * @return true运行，false已停止
     */
    public boolean isStart() {
        return isStart;
    }

    public static MemoryCache getMemoryCache() {
        if (memoryCache == null) {
            memoryCache = new UsingFreqLimitedMemoryCache(sizeLimit);
        }
        return memoryCache;
    }

    /**
     * 设置统一的缓存后缀
     *
     * @param unifiedSuffix 后缀名如：.cache
     */
    public static void setUnifiedSuffix(String unifiedSuffix) {
        LazyImageDownloader.unifiedSuffix = unifiedSuffix;
    }

    public int getSizeLimit() {
        return sizeLimit;
    }

    public void setSizeLimit(int sizeLimit) {
        LazyImageDownloader.sizeLimit = sizeLimit;
    }

    /**
     * 可以通过这个方法在创建LazyImageDownloader2后进行清理上一次遗留的图片缓存
     */
    public void clearCache() {
        if (memoryCache != null) {
            memoryCache.clearToRecycle();
        }
    }

    /**
     * 返回一个滑动监听，设置到列表后可以对列表滑动加载图像有缓冲，可以减轻滑动时的卡顿问题
     *
     * @param maxTaskNumber 阻尼时最大保留的任务数量
     * @return LazyImageOnScrollListener
     */
    public LazyImageOnScrollListener getLazyImageOnScrollListener(int maxTaskNumber) {
        if (lazyImageOnScrollListener == null) {
            lazyImageOnScrollListener = new LazyImageOnScrollListener(null);
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
            lazyImageOnScrollListener = new LazyImageOnScrollListener(onScrollListener);
        }
        this.maxTaskNumber = maxTaskNumber;
        return lazyImageOnScrollListener;
    }

    @SuppressLint("NewApi")
    public static class ImageRef {
        public String strObj;
        public Object obj;
        public String pId = "-1";// 队列ID 模块内将用此变量来控制ImageRef的重复
        public String url, backupUrl;
        public View view;
        public View progressView;
        public Bitmap bitmap;
        public GifDrawable gif;// GIF图片格式时使用
        public boolean autoVerifyFileType;// 设置true后将自动校验文件类型，如果是GIF将设置isMovie为true状态
        public boolean isMovie;// 设置true后将会使用出发数据流方式返回
        public int duration = 50;
        public int width;
        public int height;
        public boolean ignoreDefaultImage;// 设置true时将忽略全局的default功能
        public boolean isUnifiedSuffix = true;// 统一后缀，默认true
        public String cacheName;// 缓存名字
        public int retryCount;// 重试次数，大于等于1时将不继续重试
        public int responseCode;
        public int position;// 仅当这个控件需要控制动画时才会使用这个属性
        public boolean isAnim;
        public boolean localImage;
        public boolean imageToSrc;
        public boolean downloadMode;// 设置true后这个任务将近做下载，不做显示使用
        public boolean useMinWidth;// 使用较小的宽度，设置true后将会使用原图的宽度及给定的width做比较，使用较小的宽度作为读取图片的标准
        public int maxIgnoreTime = 60000;// 最大的忽略时间
        public String localDirectory;// 如果这个值不等于null将表示强制将图片下载到某路径下面
        public String filePath;//下载完成后的文件路径
        public String UUID;

        /**
         * 创建ImageRef
         *
         * @param pId  唯一标识(如果界面中没有相同的url同时出现时可以不传)
         * @param url  地址
         * @param view View, 这个ImageView的tag会在创建ImageRef时被赋予新的数值，请不要把重要数据放到tag中
         */
        public ImageRef(String pId, String url, View view, int position) {
            init(pId, url, view, null, position);
        }

        /**
         * 创建ImageRef
         *
         * @param url      URL
         * @param view     View 这个ImageView的tag会在创建ImageRef时被赋予新的数值，请不要把重要数据放到tag中
         * @param position adapter中的位置
         */
        public ImageRef(String url, View view, int position) {
            String cacheName = TextUtils.getCacheNameForUrl(url, unifiedSuffix);
            init(cacheName + "_" + position, url, view, cacheName, position);
        }

        /**
         * 设置后可以强制这张图片的缓存路径
         *
         * @param directory 本地目录
         * @return ImageRef
         */
        public ImageRef setLocalPath(String directory) {
            this.localDirectory = directory;
            return this;
        }

        public Animation getAnim() {
            AlphaAnimation anim = new AlphaAnimation(0.3f, 1.0f);
            anim.setDuration(duration);
            return anim;
        }

        /**
         * 创建ImageRef
         *
         * @param pId       唯一标识
         * @param url       地址
         * @param view      View, 这个ImageView的tag会在创建ImageRef时被赋予新的数值，请不要把重要数据放到tag中
         * @param cacheName 缓存后的名称
         */
        public ImageRef(String pId, String url, View view, String cacheName, int position) {
            init(pId, url, view, cacheName, position);
        }

        // 初始化
        private void init(String id, String url, View view, String cn, int position) {
            this.cacheName = cn == null ? TextUtils.getCacheNameForUrl(url, unifiedSuffix) : cn;
            this.position = position;
            this.pId = id == null ? this.cacheName : id;
            this.url = url;
            this.imageToSrc = true;
            this.view = view;
            this.retryCount = 0;
            this.width = Math.max(view.getWidth(), view.getMeasuredWidth());
            this.height = -1;// -1使用宽度的缩放比
            if (url != null) {
                this.isMovie = url.toUpperCase(Locale.CHINESE).endsWith(".GIF");
            }
        }

        /**
         * 当任务被加入到队列中之后，将会进行pid的同步 仅当两次值相同的情况下显示图片（主要过滤Adapter中复用View的问题）
         */
        public void syncPid() {
            this.view.setTag(TAG_ID, pId + "");
        }


        /**
         * 设置true后将会在结束时执行一个特定的动画
         *
         * @param bool true/false
         * @return ImageRef
         */
        public ImageRef isAnim(boolean bool) {
            this.isAnim = bool;
            return this;
        }

        /**
         * 设置true后将会在结束时执行一个特定的动画
         *
         * @param bool     true/false
         * @param duration 动画时长
         * @return ImageRef
         */
        public ImageRef isAnim(boolean bool, int duration) {
            this.isAnim = bool;
            this.duration = duration;
            return this;
        }

        /**
         * 是否是本地任务，如果这个变量被设置true，控件会先检查本地文件，如果不存在执行网络下载
         *
         * @return true先检查本地
         */
        public boolean isLocalImage() {
            return localImage;
        }


        @SuppressWarnings("deprecation")
        public void onDrawBitmap(Context context) {
            if (!pId.equals(view.getTag(TAG_ID))) {
                return;
            }
            if (imageToSrc) {
                ((ImageView) view).setImageBitmap(bitmap);
                // 这里修复一次Pid缓存
                view.setTag(TAG_ID, pId);
            } else {
                if (context != null) {
                    BitmapDrawable bd = null;
                    try {
                        bd = new BitmapDrawable(context.getResources(), bitmap);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                    }
                    if (SystemTool.DENSITY_DPI >= Build.VERSION_CODES.JELLY_BEAN) {
                        view.setBackground(bd);
                    } else {
                        view.setBackgroundDrawable(bd);
                    }
                    // 这里修复一次Pid缓存
                    view.setTag(TAG_ID, pId);
                }
            }
        }

        /**
         * 这个方法需要开发者自己实现
         */
        public void onDrawMovie(GifDrawable gif) {

        }

        public void error(Context context, int stateCode) {
        }

        @SuppressWarnings("deprecation")
        public void setDefaultImage(Drawable defDrawable) {
            if (imageToSrc) {
                ImageView iv = ((ImageView) view);
                if (iv.getDrawable() != null) {
                    if (iv.getDrawable() instanceof GifDrawable) {
                        GifDrawable gif = (GifDrawable) iv.getDrawable();
                        if (gif.isPlaying()) {
                            gif.stop();
                        }
                        if (!gif.isRecycled()) {
                            gif.recycle();
                        }
                    }
                }
                ((ImageView) view).setImageDrawable(defDrawable);
            } else {
                if (SystemTool.DENSITY_DPI >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.setBackground(defDrawable);
                } else {
                    view.setBackgroundDrawable(defDrawable);
                }
            }
        }

        /**
         * 任务结束时被调用
         */
        public void end() {
            if (progressView != null) {
                progressView.setVisibility(View.GONE);
            }
            view.setVisibility(View.VISIBLE);
            if (isAnim && view.getAnimation() == null) {
                view.startAnimation(getAnim());
            }
            // 放弃Bitmap的引用
            bitmap = null;
            gif = null;
        }

        // 图片是否存在，true存在 false不存在
        public boolean isDrawable() {
            return (imageToSrc ? ((ImageView) view).getDrawable() : view.getBackground()) != null;
        }

        // 删除图片占用的内存
        public void recycle() {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }

        public ImageRef setObj(Object obj) {
            this.obj = obj;
            return this;
        }
    }

    public class SimpleImageRef extends ImageRef {

        /**
         * 创建ImageRef
         *
         * @param pId       唯一标识
         * @param url       地址
         * @param view      View, 该ImageView的tag会在创建ImageRef时被赋予新的数值，请不要把重要数据放到tag中
         * @param cacheName 缓存后的名称
         */
        public SimpleImageRef(String pId, String url, View view, String cacheName, int position) {
            super(pId, url, view, cacheName, position);
        }
    }

    public class LazyImageOnScrollListener implements OnScrollListener {

        OnScrollListener onScrollListener;

        public LazyImageOnScrollListener(OnScrollListener onScrollListener) {
            this.onScrollListener = onScrollListener;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    LazyImageDownloader.this.scrollState = LazyImageDownloader.SCROLL_STATE_BUSY;
                    if (SCROLL_FLING_P_IDS.size() > 0) {
                        synchronized (SCROLL_FLING_P_IDS) {
                            addTaskAll(SCROLL_FLING_P_IDS.ref);
                            SCROLL_FLING_P_IDS.clear();
                        }
                    }
                    LazyImageDownloader.this.scrollState = LazyImageDownloader.SCROLL_STATE_IDLE;
                    break;
                case OnScrollListener.SCROLL_STATE_FLING:
                    LazyImageDownloader.this.scrollState = LazyImageDownloader.SCROLL_STATE_FLING;
                    break;
                default:
                    break;
            }
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (onScrollListener != null) {
                onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    }
}
