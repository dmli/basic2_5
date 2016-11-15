package com.ldm.basic.views.ctrl;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by ldm on 16/4/5.
 * <p/>
 * 控制View Visibility状态的控制器
 * <p/>
 * **********************************************************************
 * <p/>
 * *当使用这个控制器设置了View的隐藏状态时就需要使用这个控制器设置View的显示状态*
 * <p/>
 * **********************************************************************
 */
public class LViewVisibilityController {


    /**
     * 设置View的Visibility状态
     *
     * @param view       目标View
     * @param visibility View标准Visibility参数
     */
    public static void setVisibility(View view, int visibility) {
        setVisibility(view, visibility, 200);
    }

    /**
     * 设置View的Visibility状态
     *
     * @param view       目标View
     * @param visibility View标准Visibility参数
     * @param duration   动画时长
     */
    public static void setVisibility(View view, int visibility, int duration) {
        if (view == null || visibility == view.getVisibility()) {
            return;
        }
        if (duration <= 0) {
            ViewHelper.setAlpha(view, visibility == View.VISIBLE ? 1.0f : 0.0f);
            view.setVisibility(visibility);
        } else {
            ObjectAnimator oa;
            if (visibility == View.VISIBLE) {
                view.setVisibility(View.VISIBLE);
                oa = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f);
            } else {
                oa = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f);
            }
            oa.setDuration(duration);
            oa.addListener(new MyAnimatorListener(view, visibility));
            oa.start();
        }
    }

    private static class MyAnimatorListener implements Animator.AnimatorListener {

        View view;
        int visibility;

        public MyAnimatorListener(View view, int visibility) {
            this.view = view;
            this.visibility = visibility;
        }

        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (view != null) {
                view.setVisibility(visibility);
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }
}
