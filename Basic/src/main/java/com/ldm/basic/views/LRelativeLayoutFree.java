package com.ldm.basic.views;

import com.ldm.basic.utils.SystemTool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by ldm on 14-8-26.
 * 初始化后可以根据API任意移动内部的child，直到调用reset()方法后还原
 */
public class LRelativeLayoutFree extends RelativeLayout {

    private boolean freeMode, firstFinishDraw;
    private Rect[] rect, originalRect;//内部所有的view都将根据编号对应一个Rect
    private OnFirstFinishDraw onFirstFinishDraw;

    public LRelativeLayoutFree(Context context) {
        super(context);
        init();
    }

    public LRelativeLayoutFree(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        freeMode = false;
        firstFinishDraw = true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        rect = new Rect[count];
        originalRect = new Rect[count];
        for (int i = 0; i < count; i++) {
            rect[i] = new Rect();
            originalRect[i] = new Rect();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        freeMode = false;
        firstFinishDraw = true;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int len = getChildCount();
        if (!freeMode) {
            super.onLayout(changed, l, t, r, b);
            for (int i = 0; i < len; i++) {
                View v1 = getChildAt(i);
                if (v1 == null || v1.getVisibility() == GONE) {
                    continue;
                }
                if (t >= 0) {
                    v1.clearAnimation();
                    rect[i].left = v1.getLeft();
                    rect[i].top = v1.getTop();
                    rect[i].right = v1.getRight();
                    rect[i].bottom = v1.getBottom();
                    originalRect[i].set(rect[i].left, rect[i].top, rect[i].right, rect[i].bottom);
                }
            }
        } else {
            for (int i = 0; i < len; i++) {
                View v1 = getChildAt(i);
                if (v1 == null || v1.getVisibility() == GONE) {
                    continue;
                }
                Rect rc;
                if (SystemTool.SYS_SDK_INT > 10) {
                    rc = originalRect[i];
                }else{
                    rc = rect[i];
                }
                v1.layout(rc.left, rc.top, rc.right, rc.bottom);
            }
        }
    }

    //恢复child位置
    public void resetChild() {
        freeMode = false;
        invalidate();
    }

    /**
     * 设置
     *
     * @param i         索引
     * @param fromScale 0-1
     */
    @SuppressLint("NewApi")
    public void setChildScale(int i, float fromScale) {
        View v1 = getChildAt(i);
        if (v1 == null || i < 0 || i >= rect.length) return;
        freeMode = true;
        if (SystemTool.SYS_SDK_INT > 10) {
            v1.setScaleX(fromScale);
            v1.setScaleY(fromScale);
        } else {//低于3.0的手机利用layout实现缩放
            Rect r = rect[i];
            int newWidth = (int) (v1.getMeasuredWidth() * fromScale);//新宽度 根据原始宽度计算
            int newHeight = (int) (v1.getMeasuredHeight() * fromScale);//新高度 根据原始高度计算
            int w = (newWidth - r.width()) / 2;//宽度差
            int h = (newHeight - r.height()) / 2;//高度差
            r.set(r.left + w, r.top + h, r.right - w, r.bottom - h);
            v1.layout(r.left, r.top, r.right, r.bottom);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (firstFinishDraw && onFirstFinishDraw != null) {
            onFirstFinishDraw.onFirstFinishDraw();
            firstFinishDraw = false;
        }
    }

    /**
     * 设置透明度(需要适配3.0以下且布局层次较多时不建议使用)
     *
     * @param i     索引
     * @param alpha 0～1
     */
    public void setChildAlpha(int i, float alpha) {
        setChildAlpha(getChildAt(i), alpha);
    }

    //设置第一次OnDraw的监听
    public void setOnFirstFinishDraw(OnFirstFinishDraw onFirstFinishDraw) {
        this.onFirstFinishDraw = onFirstFinishDraw;
    }

    /**
     * 设置透明度(需要适配3.0以下且布局层次较多时不建议使用)
     *
     * @param v1    View
     * @param alpha 0～1
     */
    @SuppressLint("NewApi")
    public void setChildAlpha(View v1, float alpha) {
        if (v1 == null || v1.getVisibility() != VISIBLE) return;
        freeMode = true;
        if (SystemTool.SYS_SDK_INT > 10) {
            v1.setAlpha(alpha);
        } else {
            final int a = (int) (alpha * 255);
            if (v1.getBackground() != null) {
                v1.getBackground().setAlpha(a);
            }
            if (v1 instanceof ImageView) {//设置src内容的透明度
                ImageView iv = (ImageView) v1;
                if (iv.getDrawable() != null) {
                    iv.getDrawable().setAlpha(a);
                }
            } else if (v1 instanceof TextView) {//设置字体颜色的透明度
                TextView tv = (TextView) v1;
                final int color = tv.getCurrentTextColor();
                final int r = (color >> 16) & 0xFF;
                final int g = (color >> 8) & 0xFF;
                final int b = color & 0xFF;
                tv.setTextColor(Color.argb(a, r, g, b));
            } else if (v1 instanceof ViewGroup) {//递归设置透明度
                ViewGroup vg = (ViewGroup) v1;
                int len = vg.getChildCount();
                for (int i = 0; i < len; i++) {
                    View vv = vg.getChildAt(i);
                    if (vv == null || vv.getVisibility() != VISIBLE) {
                        continue;
                    }
                    setChildAlpha(vv, alpha);
                }
            }
            invalidate();
        }
    }

    /**
     * 移动内部child的x位置，方法兼容值2.1
     *
     * @param i 索引
     * @param x 新位置
     */
    @SuppressLint("NewApi")
    public void setChildTranslationX(int i, int x) {
        View v1 = getChildAt(i);
        if (v1 == null || i < 0 || i >= rect.length) return;
        freeMode = true;
        Rect r = rect[i];
        Rect or = originalRect[i];
        //存储数据
        r.set(or.left + x, r.top, or.left + x + r.width(), r.bottom);
        if (SystemTool.SYS_SDK_INT > 10) {
            v1.setTranslationX(x);
        } else {//低于3.0的手机利用layout实现移动
            v1.layout(r.left, r.top, r.right, r.bottom);
            invalidate();
        }
    }

    /**
     * 移动内部child的y位置，方法兼容值2.1
     *
     * @param i 索引
     * @param y 新位置
     */
    @SuppressLint("NewApi")
    public void setChildTranslationY(int i, int y) {
        View v1 = getChildAt(i);
        if (v1 == null || i < 0 || i >= rect.length) return;
        freeMode = true;
        Rect r = rect[i];
        Rect or = originalRect[i];
        //存储数据
        r.set(r.left, or.top + y, r.right, or.top + y + r.height());
        if (SystemTool.SYS_SDK_INT > 10) {
            v1.setTranslationY(y);
        } else {//低于3.0的手机利用layout实现移动
            v1.layout(r.left, r.top, r.right, r.bottom);
            invalidate();
        }
    }

    /**
     * 移动内部child的y位置，方法兼容值2.1
     *
     * @param i 索引
     * @param x 新位置
     * @param y 新位置
     */
    @SuppressLint("NewApi")
    public void setChildTranslation(int i, int x, int y) {
        View v1 = getChildAt(i);
        if (v1 == null || i < 0 || i >= rect.length) return;
        freeMode = true;
        Rect r = rect[i];
        Rect or = originalRect[i];
        //存储数据
        r.set(or.left + x, or.top + y, or.left + x + r.width(), or.top + y + r.height());
        if (SystemTool.SYS_SDK_INT > 10) {
            v1.setTranslationX(x);
            v1.setTranslationY(y);
        } else {//低于3.0的手机利用layout实现移动
            v1.layout(r.left, r.top, r.right, r.bottom);
            invalidate();
        }
    }

    /**
     * 根据索引获取child的drawRect数据
     *
     * @param i 索引
     * @return Rect 有可能为null
     */
    public Rect getChildRect(int i) {
        if (i >= 0 && i < rect.length) {
            return rect[i];
        }
        return null;
    }

    /**
     * 获取view在内部的Width
     *
     * @param i 索引
     * @return int
     */
    public int getChildWidth(int i) {
        Rect r = null;
        if (i >= 0 && i < rect.length) {
            r = rect[i];
        }
        return r == null ? 0 : r.width();
    }

    /**
     * 获取view在内部的Width
     *
     * @param i 索引
     * @return int
     */
    public int getChildHeight(int i) {
        Rect r = null;
        if (i >= 0 && i < rect.length) {
            r = rect[i];
        }
        return r == null ? 0 : r.height();
    }

    /**
     * 获取view在内部的left边距
     *
     * @param i 索引
     * @return int
     */
    public int getChildLeft(int i) {
        Rect r = null;
        if (i >= 0 && i < rect.length) {
            r = rect[i];
        }
        return r == null ? 0 : r.left;
    }

    /**
     * 获取view在内部的top边距
     *
     * @param i 索引
     * @return int
     */
    public int getChildTop(int i) {
        Rect r = null;
        if (i >= 0 && i < rect.length) {
            r = rect[i];
        }
        return r == null ? 0 : r.top;
    }

    /**
     * 获取view在内部的right边距
     *
     * @param i 索引
     * @return int
     */
    public int getChildRight(int i) {
        Rect r = null;
        if (i >= 0 && i < rect.length) {
            r = rect[i];
        }
        return r == null ? 0 : getWidth() - r.right;
    }

    /**
     * 获取view在内部的bottom边距
     *
     * @param i 索引
     * @return int
     */
    public int getChildBottom(int i) {
        Rect r = null;
        if (i >= 0 && i < rect.length) {
            r = rect[i];
        }
        return r == null ? 0 : getHeight() - r.bottom;
    }

    public interface OnFirstFinishDraw {
        public void onFirstFinishDraw();
    }
}
