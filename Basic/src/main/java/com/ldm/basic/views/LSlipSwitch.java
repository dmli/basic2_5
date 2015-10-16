package com.ldm.basic.views;

import com.ldm.basic.utils.MeasureHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;


/**
 * Created by ldm on 14-5-12.
 * 滑动开关
 */
public class LSlipSwitch extends RelativeLayout {

    private View dragView;
    private ViewDragHelper viewDragHelper;
    private OnStateChangeListener onStateChangeListener;
    private OnPositionChangeListener onPositionChangeListener;
    private OnViewClickListener onViewClickListener;
    private boolean isTriggerCallback;
    private boolean isMove, updatePosition = true;
    private boolean isLeftEdge;
    private float ox, oy;
    private int switchWidth, childHeight;

    public LSlipSwitch(Context context) {
        super(context);
        init(context);
    }

    public LSlipSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        isLeftEdge = true;
        viewDragHelper = ViewDragHelper.create(this, 2.0f, callback);
        viewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        dragView = getChildAt(0);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final View child0 = getChildAt(0);
        if (child0 != null && updatePosition) {
            switchWidth = MeasureSpec.getSize(widthMeasureSpec);
            //优先使用用户设置的高度
            child0.measure(MeasureHelper.getWidth(child0, widthMeasureSpec), MeasureHelper.getHeight(child0, heightMeasureSpec));
            childHeight = child0.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final View child0 = getChildAt(0);
        if (child0 != null && updatePosition) {
            child0.layout(isLeftEdge ? 0 : switchWidth - child0.getMeasuredWidth(), 0, isLeftEdge ? child0.getMeasuredWidth() : switchWidth - child0.getMeasuredWidth() + child0.getMeasuredWidth(), childHeight);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        updatePosition = true;
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            viewDragHelper.cancel();
            return false;
        }
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ox = event.getX();
                oy = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (onPositionChangeListener != null) {
                    onPositionChangeListener.onPositionChangeListener(dragView, (this.getWidth() - dragView.getWidth()) / ((float) dragView.getLeft()));
                }
                if (Math.abs(event.getX() - ox) >= 5 || Math.abs(event.getY() - oy) >= 5) {
                    isMove = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isMove) {
                    isTriggerCallback = true;
                    reductionPosition();
                } else {
                    if (onViewClickListener != null) {
                        if (event.getX() < this.getWidth() - dragView.getWidth() - 15) {// -15盲区
                            onViewClickListener.onViewClickListener(0.0f);
                            isLeftEdge = true;
                        }
                        if (event.getX() > this.getWidth() - dragView.getWidth() + 15) {// +15盲区
                            onViewClickListener.onViewClickListener(1.0f);
                            isLeftEdge = false;
                        }
                    }
                }
                isMove = false;
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * dragView是否停靠在左边缘
     *
     * @return true在左边
     */
    public boolean isLeftEdge() {
        return dragView.getLeft() == 0;
    }

    /**
     * 还原位置
     */
    private void reductionPosition() {
        if (dragView.getLeft() > (this.getWidth() - dragView.getWidth()) / 2) {
            if (dragView.getLeft() != this.getWidth() - dragView.getWidth()) {
                scrollToRight();
            } else {
                if (isTriggerCallback && onStateChangeListener != null) {
                    onStateChangeListener.onStateChangeListener(dragView, 1.0f);
                    isTriggerCallback = false;
                }
            }
            isLeftEdge = false;
        } else {
            if (dragView.getLeft() != 0) {
                scrollToLeft();
            } else {
                if (isTriggerCallback && onStateChangeListener != null) {
                    onStateChangeListener.onStateChangeListener(dragView, 0.0f);
                    isTriggerCallback = false;
                }
            }
            isLeftEdge = true;
        }
    }

    /**
     * 滚动到左边
     */
    public void scrollToLeft() {
        TranslateAnimation ta;
        ta = getTranslateAnimation(0, -dragView.getLeft());
        ta.setDuration(100);
        ta.setAnimationListener(new MyAnimationListener(dragView) {
            @Override
            public void onAnimationEnd(Animation animation) {
                //同步位置
                updatePosition = true;
                v.layout(0, v.getTop(), v.getWidth(), v.getHeight());
                if (onStateChangeListener != null) {
                    onStateChangeListener.onStateChangeListener(dragView, 0.0f);
                    isTriggerCallback = false;
                }
            }
        });
        ta.setFillAfter(false);
        ta.setFillEnabled(true);
        updatePosition = false;
        dragView.startAnimation(ta);
        isLeftEdge = true;
    }

    /**
     * 滚动到右边
     */
    public void scrollToRight() {
        TranslateAnimation ta;
        ta = getTranslateAnimation(0, this.getWidth() - dragView.getWidth() - dragView.getLeft());
        ta.setDuration(100);
        ta.setAnimationListener(new MyAnimationListener(dragView) {
            @Override
            public void onAnimationEnd(Animation animation) {
                //同步位置
                updatePosition = true;
                v.layout(getWidth() - v.getWidth(), v.getTop(), getWidth() - v.getWidth() + v.getWidth(), v.getHeight());
                if (onStateChangeListener != null) {
                    onStateChangeListener.onStateChangeListener(dragView, 1.0f);
                    isTriggerCallback = false;
                }
            }
        });
        ta.setFillAfter(false);
        ta.setFillEnabled(true);
        updatePosition = false;
        dragView.startAnimation(ta);
        isLeftEdge = false;
    }

    /**
     * 设置状态监听
     *
     * @param onStateChangeListener LSlipSwitch.OnStateChangeListener
     */
    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    /**
     * 设置位置监听
     *
     * @param onPositionChangeListener LSlipSwitch.OnPositionChangeListener
     */
    public void setOnPositionChangeListener(OnPositionChangeListener onPositionChangeListener) {
        this.onPositionChangeListener = onPositionChangeListener;
    }

    /**
     * 设置点击事件
     *
     * @param onViewClickListener LSlipSwitch.OnViewClickListener
     */
    public void setOnViewClickListener(OnViewClickListener onViewClickListener) {
        this.onViewClickListener = onViewClickListener;
    }

    /**
     * 获取一个移动动画
     *
     * @param formX 从
     * @param toX   到
     * @return TranslateAnimation
     */
    private TranslateAnimation getTranslateAnimation(float formX, float toX) {
        return new TranslateAnimation(Animation.ABSOLUTE, formX, Animation.ABSOLUTE, toX, Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0);
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View view, int i) {
            return true;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            viewDragHelper.captureChildView(getChildAt(0), pointerId);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int leftBound = getPaddingLeft();
            final int rightBound = getWidth() - dragView.getWidth();
            return Math.min(Math.max(left, leftBound), rightBound);
        }

    };

    /**
     * 动画的监听，可以用index在动画结束了做位置计算
     */
    private abstract class MyAnimationListener implements Animation.AnimationListener {
        public View v;

        private MyAnimationListener(View v) {
            this.v = v;
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    /**
     * 状态改变时触发
     */
    public interface OnStateChangeListener {
        /**
         * 状态改变时触发
         *
         * @param dragView 被移动的view
         * @param position 位置 0.0左边   1.0右边
         */
        public void onStateChangeListener(final View dragView, final float position);
    }

    /**
     * 拖拽view位置改变时触发
     */
    public interface OnPositionChangeListener {

        /**
         * 拖拽view位置改变时调用
         *
         * @param dragView 被移动的view
         * @param position 位置 0.0 ～ 1.0之间
         */
        public void onPositionChangeListener(final View dragView, final float position);
    }

    /**
     * 点击事件，只区分左右，中间部分有30像素的盲区
     */
    public interface OnViewClickListener {

        /**
         * 点击事件，只区分左右，中间部分有30像素的盲区
         *
         * @param position 位置 0.0左边   1.0右边
         */
        public void onViewClickListener(final float position);
    }
}
