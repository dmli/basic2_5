package com.ldm.basic.utils.image;

import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;

import com.ldm.basic.res.BitmapHelper;
import com.ldm.basic.utils.FileTool;
import com.ldm.basic.utils.SystemTool;

import java.util.Locale;

public class ImageRefRoundBitmap extends LazyImageDownloader.ImageRef {


    public ImageRefRoundBitmap(String pId, String url, View view, String cacheName, int position) {
        super(pId, url, view, cacheName, position);
    }

    public ImageRefRoundBitmap(String pId, String url, View view, int position) {
        super(pId, url, view, position);
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
        Bitmap bit = null;
        if (SystemTool.SYS_SDK_INT <= Build.VERSION_CODES.JELLY_BEAN && url.toUpperCase(Locale.CHINESE).endsWith(".WEBP")) {
            Bitmap bmp = BitmapHelper.decodeWebp(FileTool.openFile(path));
            if (bmp != null) {
                bit = BitmapHelper.resetBitmapThrowsOutOfMemoryError(bmp, path, targetWidth, targetHeight);
            }
        } else {
            bit = BitmapHelper.getBitmapThrowsOutOfMemoryError(path, targetWidth, targetHeight);
        }
        if (bit != null) {
            bitmap = BitmapHelper.getRoundBitmap(bit);
        }
        return bitmap != null && !bitmap.isRecycled();
    }

}
