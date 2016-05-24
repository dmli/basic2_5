package com.ldm.basic.views;

import com.ldm.basic.utils.LLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ldm on 14-2-9.
 * 支持3d运动的View
 */
public class LImageView3d extends View {

    private Camera camera;
    private Matrix matrix;
    private Bitmap bitmap;
    private Paint paint;
    private float centerX, centerY, _translateX, _translateY, depthZ, degreesX, degreesY;
    private int radius;//半径

    public LImageView3d(Context context) {
        super(context);
        init();
    }

    public LImageView3d(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        camera = new Camera();
        matrix = new Matrix();
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null && !bitmap.isRecycled()) {
            camera.save();
            camera.translate(_translateX, _translateY, depthZ);
            camera.rotateY(degreesY);
            camera.rotateX(degreesX);
            camera.getMatrix(matrix);
            camera.restore();

            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);

            canvas.drawBitmap(bitmap, matrix, paint);
        }
    }

    /**
     * 设置图像
     *
     * @param bitmap Bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            LLog.e(" LImageView3D.setBitmap(bit) 中 bit == null ");
            return;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(width, height);
        } else {
            lp.width = width;
            lp.height = height;
        }
        radius = lp.width / 2;//保存半径
        this.setLayoutParams(lp);
        if (this.bitmap != null && !this.bitmap.equals(bitmap) && !this.bitmap.isRecycled()) {
            this.bitmap.recycle();
        }
        this.bitmap = bitmap;
        this.postInvalidate();
    }

    /**
     * 重置中心点、角度及Z轴数据
     */
    public void resetState() {
        this.centerX = 0.0f;
        this.centerY = 0.0f;
        this.depthZ = 0.0f;
        this.degreesX = 0.0f;
        this.degreesY = 0.0f;
    }

    /**
     * 清除Bitmap
     */
    public void clearBitmap() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    /**
     * bitmap是否被回收了
     *
     * @return true回收了  false可用
     */
    public boolean isRecycle() {
        return bitmap == null || bitmap.isRecycled();
    }

    /**
     * 设置中心点
     *
     * @param dx X中心点 0.0 ~ 1.0之间
     * @param dy Y中心点 0.0 ~ 1.0之间
     */
    public void setCenter(float dx, float dy) {
        //优先使用getWidth()
        if (getWidth() != 0) {
            this.centerX = dx * getWidth();
            this.centerY = dy * getHeight();
            return;
        }
        //尝试使用getMeasuredWidth()
        if (getMeasuredWidth() != 0) {
            this.centerX = dx * getMeasuredWidth();
            this.centerY = dy * getMeasuredHeight();
            return;
        }
        //使用原图大小
        if (bitmap != null) {
            this.centerX = dx * bitmap.getWidth();
            this.centerY = dy * bitmap.getHeight();
        }
    }

    /**
     * 获取View与屏幕间的距离（view的中心位置计算）
     *
     * @return int[] 0x  1y
     */
    public int[] getLocationInWindowToCenter() {
        int[] local = new int[2];
        this.getLocationInWindow(local);
        return new int[]{local[0] + radius, local[1] + radius};
    }

    /**
     * 设置Z轴
     *
     * @param z float
     */
    public void setDepthZ(float z) {
        this.depthZ = z;
    }

    /**
     * 当前Z轴
     *
     * @return float
     */
    public float getDepthZ() {
        return depthZ;
    }

    /**
     * 获取当前X轴旋转角度
     *
     * @return degreesX
     */
    public float getDegreesX() {
        return degreesX;
    }

    /**
     * 设置X轴角度
     *
     * @param degreesX 旋转角度
     */
    public void setDegreesX(float degreesX) {
        this.degreesX = degreesX;
    }

    /**
     * 获取当前Y轴旋转角度
     *
     * @return degreesY
     */
    public float getDegreesY() {
        return degreesY;
    }

    /**
     * 设置Y轴角度
     *
     * @param degreesY 旋转角度
     */
    public void setDegreesY(float degreesY) {
        this.degreesY = degreesY;
    }

    /**
     * View宽度的一半大小
     *
     * @return int 半径
     */
    public int getRadius() {
        return radius;
    }

    public void setTranslateX(float translateX) {
        this._translateX = translateX;
    }

    public void setTranslateY(float translateY) {
        this._translateY = translateY;
    }

    /**
     * 返回图像大小
     *
     * @return bitmap.getWidth()
     */
    public int getBitmapWidth() {
        return bitmap == null ? 0 : bitmap.getWidth();
    }
}
