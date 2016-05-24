package com.ldm.basic.anim;


import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * Created by ldm on 12-12-5.
 * 提供了简单的动画封装
 */
public class BasicAnimUtil {

    /**
     * 从底部弹出View
     *
     * @param view           View
     * @param maskView       View 背景遮罩，没有可以传入null
     * @param durationMillis anim duration
     */
    public static void bottomPopUp(View view, View maskView, long durationMillis) {
        clearAnimation(view);
        clearAnimation(maskView);

        AnimationSet set = new AnimationSet(false);
        set.addAnimation(createTranslateAnimation(0.0f, 0.0f, 1.0f, 0.0f, durationMillis));
        set.addAnimation(createAlphaAnimation(0.5f, 1.0f, (long) (durationMillis * 0.85f)));

        /**
         * 遮罩动画
         */
        Animation maskAnimation = createAlphaAnimation(0.0f, 1.0f, durationMillis);
        maskView.setVisibility(View.VISIBLE);
        maskView.startAnimation(maskAnimation);

        view.setVisibility(View.VISIBLE);
        view.startAnimation(set);
    }

    /**
     * 恢复到底部 与 bottomPopUp(view, durationMillis)相反
     *
     * @param view           View
     * @param maskView       View 背景遮罩，没有可以传入null
     * @param durationMillis anim duration
     */
    public static void recoveryBottom(View view, View maskView, long durationMillis) {
        clearAnimation(view);
        clearAnimation(maskView);

        AnimationSet set = new AnimationSet(false);
        set.addAnimation(createTranslateAnimation(0.0f, 0.0f, 0.0f, 1.0f, durationMillis));
        set.addAnimation(createAlphaAnimation(1.0f, 0.0f, durationMillis));

        /**
         * 遮罩动画
         */
        Animation maskAnimation = createAlphaAnimation(1.0f, 0.0f, durationMillis);
        maskAnimation.setAnimationListener(new OnGoneAnimationListener(maskView));
        maskView.startAnimation(maskAnimation);

        set.setAnimationListener(new OnGoneAnimationListener(view));
        view.startAnimation(set);
    }

    /**
     * 从上面弹出
     *
     * @param view           View
     * @param maskView       View 背景遮罩，没有可以传入null
     * @param durationMillis anim duration
     */
    public static void topPopUp(View view, View maskView, long durationMillis) {
        clearAnimation(view);
        clearAnimation(maskView);

        AnimationSet set = new AnimationSet(false);
        set.addAnimation(createTranslateAnimation(0.0f, 0.0f, -1.0f, 0.0f, durationMillis));
        set.addAnimation(createAlphaAnimation(0.5f, 1.0f, (long) (durationMillis * 0.85f)));

        /**
         * 遮罩动画
         */
        Animation maskAnimation = createAlphaAnimation(0.0f, 1.0f, durationMillis);
        maskView.setVisibility(View.VISIBLE);
        maskView.startAnimation(maskAnimation);

        view.setVisibility(View.VISIBLE);
        view.startAnimation(set);
    }

    /**
     * 恢复到上部 与 topPopUp(view, durationMillis)相反
     *
     * @param view           View
     * @param maskView       View 背景遮罩，没有可以传入null
     * @param durationMillis anim duration
     */
    public static void recoveryTop(View view, View maskView, long durationMillis) {
        clearAnimation(view);
        clearAnimation(maskView);

        AnimationSet set = new AnimationSet(false);
        set.addAnimation(createTranslateAnimation(0.0f, 0.0f, 0.0f, -1.0f, durationMillis));
        set.addAnimation(createAlphaAnimation(1.0f, 0.0f, durationMillis));

        /**
         * 遮罩动画
         */
        Animation maskAnimation = createAlphaAnimation(1.0f, 0.0f, durationMillis);
        maskAnimation.setAnimationListener(new OnGoneAnimationListener(maskView));
        maskView.startAnimation(maskAnimation);

        set.setAnimationListener(new OnGoneAnimationListener(view));
        view.startAnimation(set);
    }


    /**
     * 创建一个TranslateAnimation
     *
     * @param fromXValue     从N X
     * @param toXValue       到N X
     * @param fromYValue     从N Y
     * @param toYValue       到N Y
     * @param durationMillis anim duration
     * @return Animation
     */
    private static Animation createTranslateAnimation(float fromXValue, float toXValue, float fromYValue, float toYValue, long durationMillis) {
        Animation translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromXValue, Animation.RELATIVE_TO_SELF, toXValue, Animation.RELATIVE_TO_SELF, fromYValue, Animation.RELATIVE_TO_SELF, toYValue);
        translate.setDuration(durationMillis);
        translate.setInterpolator(new DecelerateInterpolator());
        return translate;
    }


    /**
     * 创建一个透明度动画
     *
     * @param fromAlpha      从
     * @param toAlpha        到
     * @param durationMillis anim duration
     * @return AlphaAnimation
     */
    private static Animation createAlphaAnimation(float fromAlpha, float toAlpha, long durationMillis) {
        Animation alpha = new AlphaAnimation(fromAlpha, toAlpha);
        alpha.setDuration(durationMillis);
        alpha.setInterpolator(new LinearInterpolator());
        return alpha;
    }

    /**
     * 清除动画
     *
     * @param view playing animation View
     */
    private static void clearAnimation(View view) {
        if (view != null && view.getAnimation() != null) {
            view.getAnimation().cancel();
            view.clearAnimation();
        }
    }


    static class OnGoneAnimationListener implements Animation.AnimationListener {

        View view;

        public OnGoneAnimationListener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

}
