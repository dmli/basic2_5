package com.ldm.basic.utils;

import com.ldm.basic.res.BitmapHelper;

import android.graphics.Bitmap;
import android.view.View;
import pl.droidsonroids.gif.GifDrawable;

public class ImageRefAsyncBitmap extends LazyImageDownloader.ImageRef{

	public ImageRefAsyncBitmap(String pId, String url, View view, String cacheName, int position) {
		super(pId, url, view, cacheName, position);
	}

	public ImageRefAsyncBitmap(String pId, String url, View view, int position) {
		super(pId, url, view, position);
	}

	public ImageRefAsyncBitmap(String url, View view, int position) {
		super(url, view, position);
	}

    public Bitmap onAsynchronous(String path) {
        return null;
    }

    public GifDrawable onAsynchronousGif(String path) throws Exception {
        return new GifDrawable(path);
    }
}
