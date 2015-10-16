package com.ldm.basic.views;

import com.ldm.basic.anim.Mesh;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by ldm on 14-8-22.
 * 指定Mesh后，可以将内部的布局映射成图片进行Mesh动画回执
 */
public class RelativeLayoutMesh extends RelativeLayout {

    private Mesh mesh;
    public boolean isStartImage;//是否开启影像功能
    private Bitmap bitmapCache;//当前使用的镜像
    private Bitmap _bitmapCache;//缓存备份
    private long destroyTime;//记录时间戳，如果销毁创建时间与销毁时间相差在1秒内时 不重新创建bitmap（异步创建时无效）

    public RelativeLayoutMesh(Context context) {
        super(context);
    }

    public RelativeLayoutMesh(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutMesh(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (bitmapCache != null && !bitmapCache.isRecycled()) {
            bitmapCache.recycle();
        }
        if (_bitmapCache != null && !_bitmapCache.isRecycled()) {
            _bitmapCache.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    /**
     * 创建影像并显示
     */
    public void createImage() {
        if (mesh == null) {
            return;
        }
        isStartImage = true;
        //如果操作时间过快使用上一次的镜像
        if (_bitmapCache != null && !_bitmapCache.isRecycled() && System.currentTimeMillis() - destroyTime <= 500) {
            mesh.setBitmapSize(_bitmapCache.getWidth(), _bitmapCache.getHeight());
            setChildVisibility(INVISIBLE);//隐藏控件内所有的子控件不可见
        } else {
            setDrawingCacheEnabled(true);
            bitmapCache = getDrawingCache();
            if (bitmapCache == null) {
                setDrawingCacheEnabled(false);
                isStartImage = false;
            } else {
                //这里可以释放备份了
                if (_bitmapCache != null && !_bitmapCache.isRecycled()) {
                    _bitmapCache.recycle();
                }
                mesh.setBitmapSize(bitmapCache.getWidth(), bitmapCache.getHeight());
                setChildVisibility(INVISIBLE);//隐藏控件内所有的子控件不可见
            }
        }
    }

    /**
     * 销毁影像并恢复子控件状态
     */
    public void destroyImage() {
        //缓存一份图像
        if (bitmapCache != null && !bitmapCache.isRecycled()) {
            _bitmapCache = Bitmap.createBitmap(bitmapCache);
        }
        setDrawingCacheEnabled(false);
        setChildVisibility(VISIBLE);//显示子控件
        isStartImage = false;
        destroyTime = System.currentTimeMillis();//记录时间戳
    }

    /**
     * 设置网格实现类
     *
     * @param t extends Mesh
     */
    public <T extends Mesh> void setMesh(T t) {
        this.mesh = t;
    }

    /**
     * 设置控件内所有子控件的显示/隐藏
     *
     * @param state GONE隐藏/VISIBLE显示
     */
    private void setChildVisibility(int state) {
        int len = getChildCount();
        for (int i = 0; i < len; i++) {
            View c = getChildAt(i);
            if (c != null) {
                c.setVisibility(state);
            }
        }
    }
}
