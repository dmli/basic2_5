package com.ldm.basic.utils.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import com.ldm.basic.res.BitmapHelper;
import com.ldm.basic.utils.MD5;
import com.ldm.basic.utils.TextUtils;

/**
 * Created by ldm on 16/5/4.
 * LazyImageDownloader 下载文件时的数据
 */
public class ImageOptions {

    /**
     * 下载过程中用来与PID匹配的value值存储的KEY
     */
    public static final int TAG_ID = 0x59999999;
    public static final int CREATE_TIME_ID = 0x59999998;

    /**
     * 这个对象可以存储一个临时的数据，在处理是使用
     */
    public Object obj;

    /**
     * 队列ID 模块内将用此变量来控制ImageRef的重复
     */
    public String pId = "-1";

    /**
     * url 网络文件的地址
     * backupUrl 网络文件的备用地址
     */
    public String url, backupUrl;
    public View view;

    /**
     * 设置这个View后当任务开始/结束时会被设置VISIBLE / GONE属性
     */
    public View progressView;

    /**
     * 如果是ListView及类似控件中使用时，可以设置这个参数，可以与LazyImageDownloader.failViewPosition属性做比较，可以过滤掉失效的任务
     */
    public int position;
    /**
     * 当下载图片时，且重写onAsynchronous方法时 需要给这个bitmap赋值
     */
    public Bitmap bitmap;
    public int width;
    public int height;
    //设置true时这个任务将不会被设置默认图片
    public boolean ignoreDefaultImage;
    // 设置后将忽略全局的default功能
    public String uSuffix = null;
    // 缓存名字
    public String cacheName;
    // 重试次数，大于等于1时将不继续重试
    public int retryCount;
    //0默认的
    public int responseCode = 0;
    //是否是本地任务，如果这个变量被设置true，控件会先检查本地文件，如果不存在执行网络下载
    public boolean localImage;
    public boolean imageToSrc;
    // 设置true后这个任务将近做下载，不做显示使用
    public boolean downloadMode;
    // 使用较小的宽度，设置true后将会使用原图的宽度及给定的width做比较，使用较小的宽度作为读取图片的标准
    public boolean useMinWidth;
    //下载完成后的文件路径
    public String filePath;
    //这个属性用来接收onAsynchronous方法的返回值
    protected boolean loadSuccess;
    public String UUID;

    /**
     * 创建ImageRef
     *
     * @param url  地址
     * @param view View, 这个ImageView的tag会在创建ImageRef时被赋予新的数值，请不要把重要数据放到tag中
     */
    public ImageOptions(String url, View view, int position) {
        init(url, view, MD5.md5(url), position);
    }

    /**
     * 创建ImageRef
     *
     * @param url       地址
     * @param view      View, 这个ImageView的tag会在创建ImageRef时被赋予新的数值，请不要把重要数据放到tag中
     * @param cacheName 缓存后的名称
     * @param position  位置索引
     */
    public ImageOptions(String url, View view, String cacheName, int position) {
        init(url, view, cacheName, position);
    }

    /**
     * 初始化
     *
     * @param url      文件地址
     * @param view     将要显示图片的载体
     * @param cn       cacheName
     * @param position 位置索引
     */
    private void init(String url, View view, String cn, int position) {
        this.pId = MD5.md5(url) + "_" + position;
        this.cacheName = cn;
        this.url = url;
        this.imageToSrc = true;
        this.view = view;
        this.retryCount = 0;
        this.width = Math.max(view.getWidth(), view.getMeasuredWidth());
        this.height = -1;// -1使用宽度的缩放比
        this.position = position;
    }

    /**
     * 编译一次给定的参数
     * 这个方法可以配置一些对参数的处理
     */
    public final void builder() {
        if (cacheName == null) {
            cacheName = TextUtils.getCacheNameForUrl(url, uSuffix);
        }
        if (pId == null) {
            pId = this.cacheName + "_" + position;
        }
    }

    /**
     * 当任务被加入到队列中之后，将会进行pid的同步 仅当两次值相同的情况下显示图片（主要过滤Adapter中复用View的问题）
     */
    public void syncPid() {
        this.view.setTag(TAG_ID, pId + "");
    }

    /**
     * 这个方法会在子线程中运行，通过返回的boolean值来区分图像是否加载成功
     *
     * @param path         本地图片路径
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return true/false 返回当前加载是否成功
     */
    public boolean onAsynchronous(String path, int targetWidth, int targetHeight) {
        bitmap = BitmapHelper.getBitmapThrowsOutOfMemoryError(path, targetWidth, targetHeight);
        return bitmap != null && !bitmap.isRecycled();
    }

    public void setUnifiedSuffix(String us) {
        this.uSuffix = us;
    }

    /**
     * @return true先检查本地
     */
    public boolean isLocalImage() {
        return localImage;
    }

    public ImageOptions setWidth(int width) {
        this.width = width;
        return this;
    }

    public ImageOptions setHeight(int height) {
        this.height = height;
        return this;
    }

    /**
     * 图像加载成功时这个方法被触发
     *
     * @param context Context
     */
    public void onSuccess(Context context) {
        if (!pId.equals(view.getTag(TAG_ID))) {
            return;
        }
        if (imageToSrc) {
            ((ImageView) view).setImageBitmap(bitmap);
        } else {
            if (context != null) {
                Drawable drawable = null;
                try {
                    drawable = new BitmapDrawable(context.getResources(), bitmap);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.setBackground(drawable);
                } else {
                    view.setBackgroundDrawable(drawable);
                }
            }
        }
        // 这里修复一次Pid缓存
        view.setTag(TAG_ID, pId);
    }

    /**
     * 用来设置默认图片，可以重写这个方法改变设置规则
     *
     * @param defDrawable Drawable
     */
    public void setDefaultImage(Drawable defDrawable) {
        if (!ignoreDefaultImage) {
            if (imageToSrc) {
                ((ImageView) view).setImageDrawable(defDrawable);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.setBackground(defDrawable);
                } else {
                    view.setBackgroundDrawable(defDrawable);
                }
            }
        }
    }

    /**
     * 当任务失败时会触发这个方法
     *
     * @param context   Context
     * @param stateCode 网络请求的 responseCode
     */
    public void failed(Context context, int stateCode) {
    }


    /**
     * 任务结束时被调用
     */
    public void end() {
        if (progressView != null) {
            progressView.setVisibility(View.GONE);
        }
        view.setVisibility(View.VISIBLE);
        // 放弃Bitmap的引用
        bitmap = null;
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
}
