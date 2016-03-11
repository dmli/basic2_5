package com.ldm.basic.utils.image;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by ldm on 16/1/19.
 * 用来显示GIF图片
 */
public class ImageRefGifDrawable extends LazyImageDownloader.ImageRef {

    private GifDrawable gif;

    public ImageRefGifDrawable(String pId, String url, View view, int position) {
        super(pId, url, view, position);
    }

    public ImageRefGifDrawable(String pId, String url, View view, String cacheName, int position) {
        super(pId, url, view, cacheName, position);
    }

    /**
     * 这个方法会在子线程中运行，通过返回的boolean值来区分图像是否加载成功
     *
     * @param path         本地图片路径
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return true/false 返回当前加载是否成功
     */
    @Override
    public boolean onAsynchronous(String path, int targetWidth, int targetHeight) {
        try {
            gif = new GifDrawable(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gif != null;
    }

    @Override
    public void onSuccess(Context context) {
        if (gif != null) {
            if (!pId.equals(view.getTag(LazyImageDownloader.TAG_ID))) {
                return;
            }
            //设置GIF图片
            ((ImageView) view).setImageDrawable(gif);
            // 这里修复一次Pid缓存
            view.setTag(LazyImageDownloader.TAG_ID, pId);
        }
    }
}
