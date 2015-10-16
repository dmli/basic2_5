package com.ldm.basic.views;

import com.ldm.basic.utils.MeasureHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by ldm on 14/11/12. 
 * 基于SurfaceView实现，在列表使用时可以更加的流程
 */
public class LImageSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private Bitmap bitmap;
    private final SurfaceHolder holder;
    private int backgroundColor;
    private final Paint paint = new Paint();

    public LImageSurfaceView(Context context) {
        super(context);
        holder = getHolder();
        init();
    }

    public LImageSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        init();
    }

    public LImageSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        holder = getHolder();
        init();
    }
    
    private void init() {
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
        backgroundColor = Color.argb(0, 0, 0, 0);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (bitmap != null) {
            canvas.drawColor(backgroundColor);
            canvas.drawBitmap(bitmap, 0, 0, paint);
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * 设置需要绘制的
     *
     * @param b Bitmap
     */
    public void setBitmap(Bitmap b) {
        if (b == null || b.isRecycled()) {
            return;
        }
        if (this.bitmap != null && !this.bitmap.equals(b) && !this.bitmap.isRecycled()) {
            this.bitmap.recycle();
        }
        this.bitmap = b;
        if (holder != null) {
            synchronized (holder) {
                Canvas c = holder.lockCanvas(null);
                if (c != null) {
                    draw(c);
                    holder.unlockCanvasAndPost(c);// 解锁画布，提交画好的图像
                }
            }
        }
    }

    public void restSize() {
        if (bitmap != null) {
            MeasureHelper.resetSize(this, bitmap.getWidth(), bitmap.getHeight());
        }
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

}
