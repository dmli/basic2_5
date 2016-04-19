package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by ldm on 16/2/16.
 * 旋转的进度条，使用ObjectAnimator.ofFloat(..."rotation"......)动画
 * 使用ObjectAnimator动画在ListView的HeadView及footerView中使用可能刷新不及时
 */
public class LProgressView extends ImageView {

    public LProgressView(Context context) {
        super(context);
        init();
    }

    public LProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    ObjectAnimator animator;

    private void init() {
        animator = ObjectAnimator.ofFloat(this, "rotation", 0, 360);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(700);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
    }


    @Override
    protected void onDetachedFromWindow() {
        if (getAnimation() != null) {
            getAnimation().cancel();
        }
        clearAnimation();
        if (animator != null && animator.isStarted()) {
            animator.cancel();
        }
        super.onDetachedFromWindow();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (animator != null) {
            if (!animator.isStarted()) {
                animator.start();
            }
        }

    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility != getVisibility()) {
            if (visibility != VISIBLE) {
                if (animator != null && (animator.isStarted() || animator.isRunning())) {
                    animator.cancel();
                }
            }
            super.setVisibility(visibility);
        }
    }

}
