package com.ldm.basic.utils.image;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by ldm on 16/5/16.
 * LazyImageDownloader使用的Handler
 */
public class DisplayUIPresenter extends Handler {

    public static final int LOADER_IMAGE_SUCCESS = 200;
    public static final int LOADER_IMAGE_ERROR = 101;
    public static final int LOADER_IMAGE_WAKE_TASK = 102;
    public static final int LOADER_IMAGE_ERROR_OOM = 103;
    public static final int LOADER_IMAGE_RECORD_LAST_TIME = 105;
    public static final int LOADER_IMAGE_URL_IS_NULL = 106;
    public static final int LOADER_IMAGE_EXECUTE_END = 107;

    private static long lastTime;
    private WeakReference<LazyImageDownloader> imageDownloader;

    public DisplayUIPresenter(LazyImageDownloader imageDownloader) {
        super(Looper.getMainLooper());
        this.imageDownloader = new WeakReference<>(imageDownloader);
    }

    @Override
    public void handleMessage(Message msg) {
        if (imageDownloader == null || imageDownloader.get() == null) {
            return;
        }
        LazyImageDownloader lazy = imageDownloader.get();
        if (lazy.isBindActivity() && lazy.isDestroyed()) {
            return;
        }
        switch (msg.what) {
            case LOADER_IMAGE_SUCCESS: {// 图标下载成功
                lazy.imageDownloadSuccess((ImageOptions) msg.obj);
                break;
            }
            case LOADER_IMAGE_ERROR: {
                // 图片下载失败
                lazy.imageDownloadError((ImageOptions) msg.obj);
                break;
            }
            case LOADER_IMAGE_WAKE_TASK:// 通过handler唤醒任务
                lazy.addTask((ImageOptions) msg.obj);
                break;
            case LOADER_IMAGE_ERROR_OOM:// 内存溢出
                if (System.currentTimeMillis() - lastTime > 3000) {
                    lazy.loaderImageErrorOom();
                    lastTime = System.currentTimeMillis();
                }
                break;
            case LOADER_IMAGE_RECORD_LAST_TIME:
                if (System.currentTimeMillis() - lastTime > 3000) {
                    lastTime = System.currentTimeMillis();
                }
                break;
            case LOADER_IMAGE_URL_IS_NULL: {//任务下载失败，会触发failed(Context, errorState)方法
                lazy.loaderImageUrlIsNull((ImageOptions) msg.obj);
            }
            break;
            case LOADER_IMAGE_EXECUTE_END: {
                ImageOptions ref = (ImageOptions) msg.obj;
                if (ref.isEffectiveTask()) {
                    ref.end();
                }
            }
            break;
            default:
                break;
        }
    }

}
