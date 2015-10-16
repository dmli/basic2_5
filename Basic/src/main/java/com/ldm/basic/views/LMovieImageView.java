package com.ldm.basic.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by ldm on 15/6/3. 
 * 普通的ImageView类，当内部图片使用了GIFDrawable时
 */
@SuppressLint("NewApi")
public class LMovieImageView extends ImageView {

	public LMovieImageView(Context context) {
		super(context);
		initMovie();
	}

	public LMovieImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMovie();
	}

	public LMovieImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMovie();
	}

	private void initMovie() {
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
	}

	@SuppressLint("NewApi")
	@Override
	public void setBackground(Drawable background) {
		super.setBackground(background);
	}

	@Override
	public void setBackgroundResource(int resid) {
		super.setBackgroundResource(resid);
	}

	@Override
	protected void onDetachedFromWindow() {
		recycleGifCache();
		super.onDetachedFromWindow();
	}

	/**
	 * 尝试性的回收GIF内存，如果使用了GIF的话
	 */
	public void recycleGifCache() {
		if (getDrawable() != null) {
			if (getDrawable() instanceof GifDrawable) {
				GifDrawable gif = (GifDrawable) getDrawable();
				setImageDrawable(null);
				if (gif.isPlaying()) {
					gif.stop();
				}
				if (!gif.isRecycled()) {
					gif.recycle();
				}
			}
		}
		if (getBackground() != null) {
			if (getBackground() instanceof GifDrawable) {
				GifDrawable gif = (GifDrawable) getBackground();
				setBackground(null);
				if (gif.isPlaying()) {
					gif.stop();
				}
				if (!gif.isRecycled()) {
					gif.recycle();
				}
			}
		}
	}
}
