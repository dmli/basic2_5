package com.ldm.basic.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by ldm on 14-5-12.
 * 滑动菜单,与LSlipSwitch的区别在于内部需要放2个child，第一个是底部默认隐藏部分，第二个默认显示的View
 */
public class LSwipeMenuView extends RelativeLayout {

    /**
     * 状态码
     */
    private static final int STATE_OPEN = 0;
    private static final int STATE_CLOSE = 1;

    /**
     *
     */
    private View upperView, innerView;
    private int currentState = STATE_CLOSE;
    private float lastMoveVelocityX;

    /**
     * 做边距位置
     */
    private int leftBound = 0;
    private ViewDragHelper viewDragHelper;

    /**
     * 上一次touch的位置
     */
    private float ox, oy;
    private boolean isMove;
    private boolean ignoreTouch;

    /**
     * 设置true后点击upperView及任何click事件时后自动弹回
     */
    private boolean isAutoRebound = true;

    public LSwipeMenuView(Context context) {
        super(context);
        init();
    }

    public LSwipeMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LSwipeMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        viewDragHelper = ViewDragHelper.create(this, 2.0f, callback);
        viewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (viewDragHelper != null) {
            viewDragHelper.cancel();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 外层
        upperView = getChildAt(1);
        // 下层
        innerView = getChildAt(0);
        innerView.setEnabled(false);
        LayoutParams lp = (LayoutParams) innerView.getLayoutParams();
        if (lp != null) {
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, TRUE);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        leftBound = -innerView.getMeasuredWidth();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        if (isAutoRebound &&
                action == MotionEvent.ACTION_DOWN &&
                currentState == STATE_OPEN &&
                viewDragHelper.isViewUnder(upperView, (int) ev.getX(), (int) ev.getY())) {
            setState(0);
            ignoreTouch = true;
            return false;
        }
        if (ignoreTouch) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                ignoreTouch = false;
            }
            return false;
        }
        boolean ite = super.onInterceptTouchEvent(ev);
        return !ite && viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (ignoreTouch) {
            return false;
        }
        viewDragHelper.processTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ox = event.getX();
                oy = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - ox) >= 5 || Math.abs(event.getY() - oy) >= 5) {
                    isMove = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                ignoreTouch = false;
                if (isMove) {
                    resetPosition();
                }
                isMove = false;
                break;
            default:
                break;
        }
        return true;
    }


    @Override
    public void computeScroll() {
        viewDragHelper.continueSettling(true);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * 设置自动复位
     *
     * @param autoRebound true/false
     */
    public void setAutoRebound(boolean autoRebound) {
        isAutoRebound = autoRebound;
    }

    /**
     * 手指离开屏幕时用来做还原位置的方法
     */
    private void resetPosition() {
        int targetV;
        final int w = innerView.getWidth();
        if (upperView.getLeft() != 0 && upperView.getLeft() != w) {
            int nx = Math.abs(upperView.getLeft());
            if (lastMoveVelocityX > 0) {//向右
                if (nx > w * 0.33f) {
                    targetV = 0;
                } else {
                    targetV = -w;
                }
            } else if (lastMoveVelocityX < 0) {//向左
                if (nx > w * 0.33f) {
                    targetV = -w;
                } else {
                    targetV = 0;
                }
            } else {//没有速度
                if (currentState == STATE_OPEN) {
                    if (nx < w * 0.67f) {
                        targetV = 0;
                    } else {
                        targetV = -w;
                    }
                } else {
                    if (nx > w * 0.33f) {
                        targetV = -w;
                    } else {
                        targetV = 0;
                    }
                }
            }
            setState(targetV);
        }
    }

    /**
     * 指定目标值
     *
     * @param targetV 目标位置
     */
    private void setState(int targetV) {
        if (targetV == 0) {
            innerView.setEnabled(false);
            currentState = STATE_CLOSE;
        } else {
            innerView.setEnabled(true);
            currentState = STATE_OPEN;
        }
        viewDragHelper.smoothSlideViewTo(upperView, targetV, 0);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * ViewDragHelper.Callback
     */
    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View view, int i) {
            return true;
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            super.onEdgeTouched(edgeFlags, pointerId);
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            viewDragHelper.captureChildView(upperView, pointerId);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//            requestLayout();
            invalidate();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            lastMoveVelocityX = xvel;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return Math.min(0, Math.max(leftBound, left));
        }
    };

}
