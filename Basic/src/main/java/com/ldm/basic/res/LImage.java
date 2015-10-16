package com.ldm.basic.res;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * Created by ldm on 14-8-4.
 * 图像控制器与LImageView3d使用相同
 */
public class LImage {

    private Camera camera;
    private Matrix matrix;
    private Bitmap bitmap;
    private Paint paint;
    private float centerX, centerY, translateX, translateY, depthZ, degreesX, degreesY, scaleX, scaleY;
    private int radius;// 半径
    private boolean update;// 数据是否发生了变化

    public LImage() {
        init();
    }

    private void init() {
        camera = new Camera();
        matrix = new Matrix();
        paint = new Paint();
    }

    public void onDraw(Canvas canvas) {
        if (bitmap != null && !bitmap.isRecycled()) {
            camera.save();
            camera.translate(translateX, translateY, depthZ);
            camera.rotateY(degreesY);
            camera.rotateX(degreesX);
            camera.getMatrix(matrix);
            camera.restore();
            matrix.postScale(scaleX, scaleY);
            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);
            canvas.drawBitmap(bitmap, matrix, paint);
        }
        update = false;
    }

    /**
     * 设置图像
     *
     * @param bitmap Bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        radius = bitmap.getWidth() / 2;// 保存半径
        this.bitmap = bitmap;
        update = true;
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
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        update = true;
    }

    /**
     * 清除Bitmap
     */
    public void clearBitmap() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
            update = true;
        }
    }

    /**
     * 设置中心点
     *
     * @param dx X中心点 0.0 ~ 1.0之间
     * @param dy Y中心点 0.0 ~ 1.0之间
     */
    public void setCenter(float dx, float dy) {
        // 使用原图大小
        if (bitmap != null) {
            this.centerX = dx * bitmap.getWidth();
            this.centerY = dy * bitmap.getHeight();
            update = true;
        }
    }

    /**
     * 设置Z轴
     *
     * @param z float
     */
    public void setDepthZ(float z) {
        if (this.depthZ != z) {
            this.depthZ = z;
            update = true;
        }
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
     * camera.translate 的X偏移量
     *
     * @return translateX
     */
    public float getTranslateX() {
        return translateX;
    }

    /**
     * 设置camera.translate 的X偏移量
     *
     * @param translateX translateX
     */
    public void setTranslateX(float translateX) {
        if (this.translateX != translateX) {
            this.translateX = translateX;
            update = true;
        }
    }

    /**
     * camera.translate 的Y偏移量
     *
     * @return translateY
     */
    public float getTranslateY() {
        return translateY;
    }

    /**
     * 设置camera.translate 的Y偏移量
     *
     * @param translateY translateY
     */
    public void setTranslateY(float translateY) {
        if (this.translateY != translateY) {
            this.translateY = translateY;
            update = true;
        }
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
        if (this.degreesX != degreesX) {
            this.degreesX = degreesX;
            update = true;
        }
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
        if (this.degreesY != degreesY) {
            this.degreesY = degreesY;
            update = true;
        }
    }

    /**
     * View宽度的一半大小
     *
     * @return int 半径
     */
    public int getRadius() {
        return radius;
    }

    /**
     * 当前X轴的缩放比例
     *
     * @return float
     */
    public float getScaleX() {
        return scaleX;
    }

    /**
     * 设置x轴的缩放比例
     *
     * @param scaleX float
     */
    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    /**
     * 获取Y轴的缩放比例
     *
     * @return float
     */
    public float getScaleY() {
        return scaleY;
    }

    /**
     * 设置Y周的缩放比例
     *
     * @param scaleY float
     */
    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    /**
     * 返回图像大小
     *
     * @return bitmap.getWidth()
     */
    public int getBitmapWidth() {
        return bitmap == null ? 0 : bitmap.getWidth();
    }

    /**
     * 返回这个image是否需要更新
     *
     * @return true需要更新
     */
    public boolean isUpdate() {
        return update;
    }

}
