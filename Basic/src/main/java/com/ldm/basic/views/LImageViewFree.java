package com.ldm.basic.views;

import java.io.Serializable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by ldm on 14-10-15. 这个ImageView将图片的宽度拉伸，高度过超出范围时隐藏底部
 */
public class LImageViewFree extends ImageView {

    private Context context;
    private Bitmap bitmap, drawBitmap;
    private Paint paint;
    private int w;
    private Serializable serializable;
    private Object obj;
    private OnDrawListener onDrawListener;
    private boolean first;
    private int MAX_HEIGHT;

    public LImageViewFree(Context context) {
        super(context);
        init(context);
    }

    public LImageViewFree(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LImageViewFree(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        this.paint = new Paint();
        this.first = true;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        MAX_HEIGHT = MeasureSpec.getSize(heightMeasureSpec);
        if (ScaleType.CENTER_CROP == getScaleType()) {
            if (MAX_HEIGHT > 0) {
                initBitmap(false);
            }
        } else {
            initBitmap(false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.w = 0;
        this.bitmap = null;
        this.drawBitmap = null;
    }

    private void initBitmap(boolean bool) {
        if (bool || (getWidth() > 0 && getWidth() != w)) {
            if (bitmap != null && !bitmap.isRecycled()) {
                float s;
                if (bool && getWidth() == 0) {
                    s = 1.0f;
                } else {
                    s = getWidth() * 1.0f / bitmap.getWidth();
                }
                if (ScaleType.CENTER_CROP == getScaleType()) {
                    if (bitmap.getHeight() * s > MAX_HEIGHT) {
                        s = MAX_HEIGHT * 1.0f / bitmap.getHeight();
                    }
                }
                if (s > 0) {
                    if (s == 1.0f) {
                        drawBitmap = bitmap;
                    } else {
                        Matrix m = new Matrix();
                        m.reset();
                        m.postScale(s, s);
                        drawBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                        w = getWidth();
                    }
                }
            }
            if (bool) {
                postInvalidate();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
			super.onDraw(canvas);
		} catch (Exception e) {
			e.printStackTrace();
		}
        initBitmap(false);
        if (drawBitmap != null) {
            int l = 0, t = 0;
            if (ScaleType.CENTER == getScaleType() || ScaleType.CENTER_CROP == getScaleType()) {
                if (getHeight() > drawBitmap.getHeight()) {
                    t = (getHeight() - drawBitmap.getHeight()) / 2;
                }
                if (getWidth() > drawBitmap.getWidth()) {
                    l = (getWidth() - drawBitmap.getWidth()) / 2;
                }
            }
            canvas.drawBitmap(drawBitmap, l, t, paint);
        }
        if (first && onDrawListener != null) {
            onDrawListener.onFirstDraw();
            first = false;
        }
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public Serializable getSerializable() {
        return serializable;
    }

    public void setSerializable(Serializable serializable) {
        this.serializable = serializable;
    }

    /**
     * 使用这个方法设置Bitmap,将根据图片宽度进行缩放，并将底部超出的范围进行忽略
     *
     * @param drawableId getResource(id)
     */
    public void setBitmap(int drawableId) {
        Drawable drawable = context.getResources().getDrawable(drawableId);
        if (drawable != null) {
            this.bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        initBitmap(true);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        if (bitmap == null) {
            this.drawBitmap = null;
        }
        initBitmap(true);
    }

    public int getBitmapWidth() {
        return bitmap == null ? 0 : bitmap.getWidth();
    }

    public int getBitmapHeight() {
        return bitmap == null ? 0 : bitmap.getHeight();
    }

    public int getDrawBitmapWidth() {
        return drawBitmap == null ? 0 : drawBitmap.getWidth();
    }

    public int getDrawBitmapHeight() {
        return drawBitmap == null ? 0 : drawBitmap.getHeight();
    }

    public void setOnDrawListener(OnDrawListener onDrawListener) {
        this.onDrawListener = onDrawListener;
    }

    public interface OnDrawListener {
        public void onFirstDraw();
    }

}
