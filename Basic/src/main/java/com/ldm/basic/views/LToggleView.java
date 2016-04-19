package com.ldm.basic.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.ldm.basic.utils.MeasureHelper;

/**
 * Created by ldm on 14-5-19.
 * 触发器布局，该view内部可以放置2个子view,并提供下/上移动触发方法，可以实现两个子view上下切换
 */
public class LToggleView extends ViewGroup {

    private Scroller scroller;//自动滚动工具
    private static final int DURATION_TIME = 350;//动画时间
    private OnToggleViewStateListener onToggleViewStateListener;//状态监听接口
    private boolean isRightSide;//child1是否靠右侧显示

    public LToggleView(Context context) {
        super(context);
        init(context);
    }

    public LToggleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.scroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final View v1 = getChildAt(0);
        final View v2 = getChildAt(1);
        if (v1 != null && v2 != null) {
            int w1 = 0;
            if (v1.getLayoutParams() != null) {
                if (v1.getLayoutParams().width == -1) {
                    w1 = widthMeasureSpec;
                } else {
                    w1 = MeasureHelper.getWidth(v1, widthMeasureSpec);
                }
            }
            v1.measure(w1, heightMeasureSpec);
            int w2 = 0;
            if (v2.getLayoutParams() != null) {
                if (v2.getLayoutParams().width == -1) {
                    w2 = widthMeasureSpec;
                } else {
                    w2 = MeasureHelper.getWidth(v2, widthMeasureSpec);
                }
            }
            v2.measure(w2, heightMeasureSpec);
            setMeasuredDimension(Math.max(v1.getMeasuredWidth(), v2.getMeasuredWidth()), v1.getMeasuredHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final View v1 = getChildAt(0);
        final View v2 = getChildAt(1);
        if (v1 != null && v2 != null) {
            int h = Math.max(v2.getMeasuredHeight(), v1.getMeasuredHeight());
            if (isRightSide) {
                v1.layout(getMeasuredWidth() - v1.getMeasuredWidth(), 0, getMeasuredWidth(), h);
            } else {
                v1.layout(0, 0, v1.getMeasuredWidth(), h);
            }
            v2.layout(0, h, v2.getMeasuredWidth(), h + h);
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        } else {
            clearChildrenCache();
            if (onToggleViewStateListener != null) {
                onToggleViewStateListener.onStateChangeAfterListener(isFirstVisible() ? 0 : 1);
            }
        }
    }

    public void setProgress(float s) {
        final int MAX_Y = getHeight();
        int ny = (int) (s * MAX_Y);
        if (ny < 0) {
            ny = 0;
        } else if (ny > MAX_Y) {
            ny = MAX_Y;
        }
        scrollTo(getScrollX(), ny);
    }

    /**
     * 设置状态监听接口
     *
     * @param onToggleViewStateListener 状态监听接口
     */
    public void setOnToggleViewStateListener(OnToggleViewStateListener onToggleViewStateListener) {
        this.onToggleViewStateListener = onToggleViewStateListener;
    }

    /**
     * 切换状态，
     * 当第一个布局处于显示状态时向上移动
     * 当第二个布局处于显示状态时向下移动
     */
    public void switchState() {
        if (isFirstVisible()) {
            moveUp();
        } else {
            moveDown();
        }
    }

    /**
     * 设置true后child1将靠最右侧
     *
     * @param isRightSide true靠右侧显示  默认靠左
     */
    public void setRightSide(boolean isRightSide) {
        this.isRightSide = isRightSide;
    }

    /**
     * 当前是否是第一个view可见
     *
     * @return true第一个view处于可见状态 ， false第二个view处于可见状态
     */
    public boolean isFirstVisible() {
        return getScrollY() == 0;
    }

    /**
     * 获取动画的播放时间
     *
     * @return DURATION_TIME
     */
    public static int getDurationTime() {
        return DURATION_TIME;
    }

    /**
     * 向上移动
     *
     * @param duration 时间
     */
    public void moveUp(int duration) {
        if (isFirstVisible()) {
            enableChildrenCache();
            if (onToggleViewStateListener != null) {
                onToggleViewStateListener.onStateChangeBeforeListener(1);
            }
            scroller.startScroll(0, getScrollY(), 0, getHeight(), duration == -1 ? DURATION_TIME : duration);
            postInvalidate();
        }
    }

    public void moveUp() {
        moveUp(-1);
    }


    public void moveDown() {
        moveDown(-1);
    }

    /**
     * 向下移动
     *
     * @param duration 时间
     */
    public void moveDown(int duration) {
        if (!isFirstVisible()) {
            enableChildrenCache();
            if (onToggleViewStateListener != null) {
                onToggleViewStateListener.onStateChangeBeforeListener(0);
            }
            scroller.startScroll(0, getScrollY(), 0, -getHeight(), duration == -1 ? DURATION_TIME : duration);
            postInvalidate();
        }
    }

    /**
     * 开启缓存
     */
    private void enableChildrenCache() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            if (v != null) {
                v.setDrawingCacheEnabled(true);
            }
        }
    }

    /**
     * 清除缓存
     */
    private void clearChildrenCache() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            if (v != null) {
                v.setDrawingCacheEnabled(false);
            }
        }
    }

    /**
     * 状态监听接口
     */
    public interface OnToggleViewStateListener {

        /**
         * 当状态发生改变前触发
         *
         * @param visible 0第一个view可见 1第二个view可见
         */
        void onStateChangeBeforeListener(int visible);

        /**
         * 当状态发生改变后触发
         *
         * @param visible 0第一个view可见 1第二个view可见
         */
        void onStateChangeAfterListener(int visible);
    }
}
