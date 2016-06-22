package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by ldm on 14-7-14.
 * 点击有缩放效果的RelativeLayout，使用这个view后内部控件将无法接收到事件
 */
public class LRelativeLayout extends RelativeLayout {


    public static final int ANIM_ALPHA = 0;//透明度动画
    public static final int ANIM_SCALE = 1;//缩放动画

    private int currentAnimType = ANIM_ALPHA;

    /**
     * 动画时间
     */
    private int startAnimDuration = 80, restoreAnimDuration = 120;

    private GestureDetector gd;
    private boolean isClick = false;
    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

    public LRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public LRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setClickable(true);
        gd = new GestureDetector(context, gestureListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.getVisibility() != VISIBLE) {
            return false;
        }
        boolean bo = gd.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touchCancel();
                break;
            default:
                break;
        }
        return bo;
    }

    /**
     * touch
     * MotionEvent.ACTION_UP
     * MotionEvent.ACTION_CANCEL
     */
    private void touchCancel() {
        switch (currentAnimType) {
            case ANIM_SCALE:
                restoreScale();
                break;
            default:
                restoreAlpha();
                break;
        }
    }


    /**
     * touch
     * MotionEvent.ACTION_DOWN
     */
    private void touchDown() {
        isClick = false;
        switch (currentAnimType) {
            case ANIM_SCALE:
                startScale();
                break;
            default:
                startAlpha();
                break;
        }
    }

    /**
     * *****************************ObjectAnimator******************************
     */
    private void restoreAlpha() {
        ObjectAnimator oa = ObjectAnimator.ofFloat(this, "alpha", 0.6f, 1.0f);
        oa.setDuration(restoreAnimDuration);
        oa.addListener(animatorListener);
        oa.start();
    }

    private void startAlpha() {
        ObjectAnimator oa = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.6f);
        oa.setDuration(startAnimDuration);
        oa.start();
    }

    private void restoreScale() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator oax = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 0.9f);
        ObjectAnimator oay = ObjectAnimator.ofFloat(this, "scaleY", 1.0f, 0.9f);
        set.addListener(animatorListener);
        set.setDuration(restoreAnimDuration);
        set.play(oax).with(oay);
    }

    private void startScale() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator oax = ObjectAnimator.ofFloat(this, "scaleX", 0.9f, 1.0f);
        ObjectAnimator oay = ObjectAnimator.ofFloat(this, "scaleY", 0.9f, 1.0f);
        set.setDuration(startAnimDuration);
        set.play(oax).with(oay);
    }
    /**
     * *****************************ObjectAnimator******************************
     */

    /**
     * 设置点击样式,默认样式{@link #ANIM_ALPHA}
     *
     * @param currentAnimType 取值范围：{@link #ANIM_ALPHA}{@link #ANIM_SCALE}
     */
    public void setCurrentAnimType(int currentAnimType) {
        this.currentAnimType = currentAnimType;
    }

    /**
     * 设置恢复状态时的动画时间
     *
     * @param restoreAnimDuration 单位毫秒
     */
    public void setRestoreAnimDuration(int restoreAnimDuration) {
        this.restoreAnimDuration = restoreAnimDuration;
    }

    /**
     * 设置按下时的动画时间
     *
     * @param startAnimDuration 单位毫秒
     */
    public void setStartAnimDuration(int startAnimDuration) {
        this.startAnimDuration = startAnimDuration;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        this.onClickListener = l;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        this.onLongClickListener = l;
    }


    private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (isClick) {
                if (onClickListener != null) {
                    onClickListener.onClick(LRelativeLayout.this);
                }
                isClick = false;
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            touchDown();
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            isClick = true;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (onLongClickListener != null) {
                onLongClickListener.onLongClick(LRelativeLayout.this);
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }
    };
}
