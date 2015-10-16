package com.ldm.basic.views;

import com.ldm.basic.utils.TaskThreadService;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by ldm on 14-2-9.
 * 支持3D动画的RelativeLayout， 可以将内部的控件以图片的形式进行3D动画（动画需要手动控制）
 */
public class LRelativeLayout3D extends RelativeLayout {

    private LImageView3d view3d;//处理3D效果的VIEW
    public boolean isStartImage;//是否开启影像功能
    private TaskThreadService taskThread;
    private Bitmap bitmapCache;//当前使用的镜像
    private Bitmap _bitmapCache;//缓存备份
    private long destroyTime;//记录时间戳，如果销毁创建时间与销毁时间相差在500毫秒内时 不重新创建bitmap（异步创建时无效）

    public LRelativeLayout3D(Context context) {
        super(context);
        init(context);
    }

    public LRelativeLayout3D(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        taskThread = new TaskThreadService(false);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (taskThread != null) {
            taskThread.stopTask();
        }
        //尝试清楚图片
        if (view3d != null) {
            view3d.clearBitmap();
        }
        if (bitmapCache != null && !bitmapCache.isRecycled()) {
            bitmapCache.recycle();
        }
        if (_bitmapCache != null && !_bitmapCache.isRecycled()) {
            _bitmapCache.recycle();
        }
    }

    public void init(Context context) {
        view3d = new LImageView3d(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    /**
     * 创建影像并显示
     */
    public void createImage() {
        isStartImage = true;
        //如果操作时间过快使用上一次的镜像
        if (_bitmapCache != null && !_bitmapCache.isRecycled() && System.currentTimeMillis() - destroyTime <= 500) {
            view3d.resetState();//每次设置时初始化数据
            view3d.setBitmap(_bitmapCache);
            setChildVisibility(INVISIBLE);//隐藏控件内所有的子控件不可见
            addView(view3d);//添加支持3D状态的控件
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
                view3d.resetState();//每次设置时初始化数据
                view3d.setBitmap(bitmapCache);
                setChildVisibility(INVISIBLE);//隐藏控件内所有的子控件不可见
                addView(view3d);//添加支持3D状态的控件
            }
        }
    }

    /**
     * 销毁影像并恢复子控件状态
     */
    public void destroyImage() {
        //缓存一份图像
        if (bitmapCache != null && !bitmapCache.isRecycled()){
            _bitmapCache = Bitmap.createBitmap(bitmapCache);
        }
        setDrawingCacheEnabled(false);
        setChildVisibility(VISIBLE);//显示子控件
        if (view3d != null) {
            this.removeView(view3d);
        }
        isStartImage = false;
        destroyTime = System.currentTimeMillis();//记录时间戳
    }

    /**
     * 设置3DView的旋转中心点
     *
     * @param dx X中心点 0.0 ~ 1.0之间
     * @param dy Y中心点 0.0 ~ 1.0之间
     */
    public void setView3DCenter(float dx, float dy) {
        if (view3d != null) {
            view3d.setCenter(dx, dy);
        }
    }

    /**
     * 获取3DView实体
     *
     * @return ImageView3d
     */
    public LImageView3d get3DView() {
        return view3d;
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
