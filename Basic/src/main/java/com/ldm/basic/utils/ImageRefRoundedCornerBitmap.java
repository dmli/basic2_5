package com.ldm.basic.utils;

import java.util.Locale;

import com.ldm.basic.res.BitmapHelper;

import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import pl.droidsonroids.gif.GifDrawable;

public class ImageRefRoundedCornerBitmap extends LazyImageDownloader.ImageRef {

	private int pixels;

	public ImageRefRoundedCornerBitmap(String pId, String url, View view, String cacheName, int position, int pixels) {
		super(pId, url, view, cacheName, position);
		this.pixels = pixels;
	}

	public ImageRefRoundedCornerBitmap(String pId, String url, View view, int position, int pixels) {
		super(pId, url, view, position);
		this.pixels = pixels;
	}

	public ImageRefRoundedCornerBitmap(String url, View view, int position, int pixels) {
		super(url, view, position);
		this.pixels = pixels;
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
			bit = BitmapHelper.getRoundedCornerBitmap(bit, pixels);
		}
		return bit;
	}

	public GifDrawable onAsynchronousGif(String path) throws Exception {
        return new GifDrawable(path);
    }
}
