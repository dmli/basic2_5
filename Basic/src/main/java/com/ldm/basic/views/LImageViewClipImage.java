package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by ldm on 14-10-15.
 * 这个ImageView将图片的宽度拉伸，高度过超出范围时隐藏底部(当图片高度不足时 将出现默认背景色区域)
 */
public class LImageViewClipImage extends ImageView {

    private Context context;
    private Object obj;
    public static final int CLIP_IMAGE_BOTTOM = 0;// default
    public static final int CLIP_IMAGE_TOP = 0;// 当图片过大时 基于宽度 裁剪到图片的顶部
    private int clipImageMode = CLIP_IMAGE_BOTTOM;// 裁剪图片的模式
    private int targetWidth;
    private int targetHeight;
    private int drawableId = 0;

    public LImageViewClipImage(Context context) {
        super(context);
        init(context);
    }

    public LImageViewClipImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LImageViewClipImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    private void init(Context context) {
        this.context = context;
        this.setScaleType(ScaleType.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getWidth() > 0 && drawableId > 0) {
            setBitmap(drawableId, getWidth(), getHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() > 0 && drawableId > 0) {
            setBitmap(drawableId, getWidth(), getHeight());
        }
    }

    /**
     * 设置裁剪模式
     *
     * @param mode 取值范围 CLIP_IMAGE_BOTTOM / CLIP_IMAGE_TOP
     */
    public void setClipImageMode(int mode) {
        this.clipImageMode = mode;
    }

    private void initBitmap(Bitmap bitmap) {
        Bitmap drawBitmap = null;
        if (bitmap != null && !bitmap.isRecycled()) {
            float s = targetWidth * 1.0f / bitmap.getWidth();
            if (s != 1) {
                Matrix m = new Matrix();
                m.reset();
                m.postScale(s, s);
                drawBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
            } else {
                drawBitmap = bitmap;
            }
            if (drawBitmap.getHeight() > targetHeight) {
                if (clipImageMode == CLIP_IMAGE_BOTTOM) {
                    drawBitmap = Bitmap.createBitmap(drawBitmap, 0, 0, targetWidth, targetHeight);
                } else {
                    drawBitmap = Bitmap.createBitmap(drawBitmap, 0, drawBitmap.getHeight() - targetHeight, targetWidth, targetHeight);
                }
            }
            setImageBitmap(drawBitmap);
        }
        drawableId = 0;//这里需要将全局的状态设置为0，否则将会导致图片不停的被创建
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }


    /**
     * 使用这个方法设置Bitmap,将根据图片宽度进行缩放，并将底部超出的范围进行忽略
     * 这个方法将在第一次onMeasure或onDraw中被初始化initBitmap
     *
     * @param drawableId getResource(id)
     */
    public void setBitmap(int drawableId) {
        this.drawableId = drawableId;
    }

    /**
     * 使用这个方法设置Bitmap,将根据图片宽度进行缩放，并将底部超出的范围进行忽略
     *
     * @param drawableId getResource(id)
     */
    public void setBitmap(int drawableId, int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        Drawable drawable = context.getResources().getDrawable(drawableId);
        if (drawable != null) {
            initBitmap(((BitmapDrawable) drawable).getBitmap());
        }
    }

    public void setBitmap(Bitmap bitmap, int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        initBitmap(bitmap);
    }

    public interface OnDrawListener {
        void onFirstDraw();
    }


}
