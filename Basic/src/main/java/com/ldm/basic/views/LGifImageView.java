
package com.ldm.basic.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

/**
 * 带有内存缓存机制的GIF加载器
 *
 * @author Created by ldm on 16/1/4. 支持GIF图片的ImageView Changed by 138710
 */
public class LGifImageView extends ImageView {

    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    private GifDrawable gif;

    public LGifImageView(Context context) {
        super(context);
        init(null);
    }

    public LGifImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LGifImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LGifImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    /**
     * 初始化
     *
     * @param attrs AttributeSet
     */
    private void init(AttributeSet attrs) {
        if (attrs != null) {
            Drawable src = getGifOrDefaultDrawable(
                    attrs.getAttributeResourceValue(ANDROID_NS, "src", 0));
            Drawable background = getGifOrDefaultDrawable(
                    attrs.getAttributeResourceValue(ANDROID_NS, "background", 0));

            /**
             * 设置默认的src图像
             */
            if (src != null) {
                setImageDrawable(src);
            }

            /**
             * 设置默认的background图像
             */
            if (background != null) {
                setBackground(background);
            }
        }
    }

    /**
     * 暂停GIF
     */
    public void pause() {
        if (gif != null && gif.isPlaying()) {
            gif.pause();
        }
    }

    /**
     * 播放gif
     */
    public void start() {
        if (gif != null && !gif.isPlaying()) {
            gif.start();
        }
    }

    /**
     * 停止gif播放状态
     */
    public void stop() {
        if (gif != null && gif.isRunning()) {
            gif.stop();
        }
    }

    /**
     * 释放GIF资源
     */
    public void recycle() {
        if (gif != null) {
            if (gif.isRunning()) {
                gif.stop();
            }
            if (!gif.isRecycled()) {
                gif.recycle();
            }
        }
    }

    /**
     * 根据给定的gif图片地址加载图像
     *
     * @param gifPath gif local path
     */
    public void setImageDrawablePath(String gifPath) {
        recycle();
        try {
            gif = new GifDrawable(gifPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.setImageDrawable(gif);
    }

    /**
     * 根据给定的gif图片地址加载图像
     *
     * @param gifPath gif local path
     */
    public void setBackgroundDrawablePath(String gifPath) {
        recycle();
        try {
            gif = new GifDrawable(gifPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            super.setBackground(gif);
        } else {
            super.setBackgroundDrawable(gif);
        }
    }

    /**
     * setImageDrawable(Drawable) 这个方法被setImageDrawable(GifDrawable)方法代替
     *
     * @param drawable Drawable
     */
    public void setImageDrawable(Drawable drawable) {
        if (drawable instanceof GifDrawable) {
            this.gif = (GifDrawable) drawable;
        }
        super.setImageDrawable(drawable);
    }

    /**
     * setBackground(Drawable) 使用setBackground(GifDrawable)方法代替
     *
     * @param background Drawable
     */
    @Override
    public void setBackground(Drawable background) {
        if (background instanceof GifDrawable) {
            this.gif = (GifDrawable) background;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            super.setBackground(background);
        } else {
            super.setBackgroundDrawable(background);
        }
    }

    /**
     * setBackground(Drawable) 使用setBackground(GifDrawable)方法代替
     *
     * @param resid Drawable id
     */
    @Deprecated
    @Override
    public void setBackgroundResource(int resid) {
        Drawable dr = getGifOrDefaultDrawable(resid);
        if (dr != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                super.setBackground(dr);
            } else {
                super.setBackgroundDrawable(dr);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        recycleGifCache();
        super.onDetachedFromWindow();
    }

    @SuppressWarnings("deprecation") // Resources#getDrawable(int)
    private Drawable getGifOrDefaultDrawable(int resId) {
        if (resId == 0) {
            return null;
        }
        final Resources resources = getResources();
        if (!isInEditMode() && "drawable".equals(resources.getResourceTypeName(resId))) {
            try {
                gif = new GifDrawable(resources, resId);
                return gif;
            } catch (IOException | Resources.NotFoundException ignored) {
                // ignored
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return resources.getDrawable(resId, getContext().getTheme());
        } else {
            return resources.getDrawable(resId);
        }
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
