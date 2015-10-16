package com.ldm.basic.utils;

import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;

import com.ldm.basic.res.BitmapHelper;

import java.util.Locale;

import pl.droidsonroids.gif.GifDrawable;

public class ImageRefRoundBitmap extends LazyImageDownloader.ImageRef {


	public ImageRefRoundBitmap(String pId, String url, View view, String cacheName, int position) {
		super(pId, url, view, cacheName, position);
	}

	public ImageRefRoundBitmap(String pId, String url, View view, int position) {
		super(pId, url, view, position);
	}

	public ImageRefRoundBitmap(String url, View view, int position) {
		super(url, view, position);
	}

	public Bitmap onAsynchronous(String path, int loadImageWidth, int loadImageHeight) {
		Bitmap bit = null;
		if (SystemTool.SYS_SDK_INT <= Build.VERSION_CODES.JELLY_BEAN && url.toUpperCase(Locale.CHINESE).endsWith(".WEBP")) {
			FileTool ft = new FileTool();
			Bitmap bmp = BitmapHelper.decodeWebp(ft.openFile(path));
			if (bmp != null) {
				bit = BitmapHelper.resetBitmapThrowsOutOfMemoryError(bmp, path, loadImageWidth, loadImageHeight);
			}
		} else {
			bit = BitmapHelper.getBitmapThrowsOutOfMemoryError(path, loadImageWidth, loadImageHeight);
		}
		if (bit != null) {
			bit = BitmapHelper.getRoundBitmap(bit);
		}
		return bit;
	}

	public GifDrawable onAsynchronousGif(String path) throws Exception {
        return new GifDrawable(path);
    }
}
