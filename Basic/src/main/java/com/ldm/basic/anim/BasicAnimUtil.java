package com.ldm.basic.anim;


import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by ldm on 12-12-5.
 * 提供了简单的动画封装
 */
public class BasicAnimUtil {


    /**
     * 旋转控制动画
     *
     * @param view        动画view
     * @param fromDegrees 动画开始角度
     * @param toDegrees   动画结束角度
     * @param animParams  动画参数及回调
     */
    public static void animRotate(View view, float fromDegrees, float toDegrees, AnimParams animParams) {
        animRotate(view, fromDegrees, toDegrees, 0, 0, animParams);
    }

    /**
     * 旋转控制动画
     *
     * @param view        动画view
     * @param fromDegrees 动画开始角度
     * @param toDegrees   动画结束角度
     * @param pivotX      动画旋转时X坐标开始位置
     * @param pivotY      动画旋转时Y坐标开始位置
     * @param animParams  动画参数及回调
     */
    public static void animRotate(View view, float fromDegrees, float toDegrees, float pivotX, float pivotY, AnimParams animParams) {
        animRotate(view, fromDegrees, toDegrees, Animation.ABSOLUTE, pivotX, Animation.ABSOLUTE, pivotY, animParams);
    }

    /**
     * 旋转控制动画
     *
     * @param view        动画view
     * @param fromDegrees 动画开始角度
     * @param toDegrees   动画结束角度
     * @param pivotXType  中心点类型 取值范围：Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_PARENT
     * @param pivotXValue 中心点位置 0.0f - 1.0f
     * @param pivotYType  取值范围：Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_PARENT
     * @param pivotYValue 中心点位置 0.0f - 1.0f
     * @param animParams  动画参数及回调
     */
    public static void animRotate(View view, float fromDegrees, float toDegrees, int pivotXType, float pivotXValue, int pivotYType, float pivotYValue, AnimParams animParams) {
        RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees, pivotXType, pivotXValue, pivotYType, pivotYValue);
        startAnimation(view, rotate);
        setParams(rotate, animParams);
    }

    /**
     * 移动控制动画
     *
     * @param view       动画view
     * @param fromXDelta 动画开始X坐标
     * @param toXDelta   动画结束X坐标
     * @param fromYDelta 动画开始Y坐标
     * @param toYDelta   动画结束Y坐标
     * @param animParams 动画参数及回调
     */
    public static void animTranslate(View view, float fromXDelta, float toXDelta, float fromYDelta, float toYDelta, AnimParams animParams) {
        TranslateAnimation translate = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
        startAnimation(view, translate);
        setParams(translate, animParams);
    }

    /**
     * 移动控制动画
     *
     * @param view       动画view
     * @param fromXType  取值范围：Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_PARENT
     * @param fromXValue 动画开始X坐标
     * @param toXType    取值范围：Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_PARENT
     * @param toXValue   动画结束X坐标
     * @param fromYType  取值范围：Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_PARENT
     * @param fromYValue 动画开始Y坐标
     * @param toYType    取值范围：Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_PARENT
     * @param toYValue   动画结束Y坐标
     * @param animParams 动画参数及回调
     */
    public static void animTranslate(View view, int fromXType, float fromXValue, int toXType, float toXValue, int fromYType, float fromYValue, int toYType, float toYValue, AnimParams animParams) {
        TranslateAnimation translate = new TranslateAnimation(fromXType, fromXValue, toXType, toXValue, fromYType, fromYValue, toYType, toYValue);
        startAnimation(view, translate);
        setParams(translate, animParams);
    }

    /**
     * 缩放控制动画
     *
     * @param view       动画view
     * @param fromX      动画开始X坐标的伸缩尺寸
     * @param toX        动画结束X坐标的伸缩尺寸
     * @param fromY      动画开始Y坐标的伸缩尺寸
     * @param toY        动画结束Y坐标的伸缩尺寸
     * @param animParams 动画参数及回调
     */
    public static void animScale(View view, float fromX, float toX, float fromY, float toY, AnimParams animParams) {
        animScale(view, fromX, toX, fromY, toY, 0, 0, animParams);
    }

    /**
     * 缩放控制动画
     *
     * @param view       动画view
     * @param fromX      动画开始X坐标的伸缩尺寸
     * @param toX        动画结束X坐标的伸缩尺寸
     * @param fromY      动画开始Y坐标的伸缩尺寸
     * @param toY        动画结束Y坐标的伸缩尺寸
     * @param pivotX     动画缩放时X坐标开始位置  取值 0 -- 100
     * @param pivotY     动画缩放时X坐标开始位置  取值 0 -- 100
     * @param animParams 动画参数及回调
     */
    public static void animScale(View view, float fromX, float toX, float fromY, float toY, float pivotX, float pivotY, AnimParams animParams) {
        animScale(view, fromX, toX, fromY, toY, Animation.ABSOLUTE, pivotX, Animation.ABSOLUTE, pivotY, animParams);
    }

    /**
     * 缩放控制动画
     *
     * @param view        动画view
     * @param fromX       动画开始X坐标的伸缩尺寸
     * @param toX         动画结束X坐标的伸缩尺寸
     * @param fromY       动画开始Y坐标的伸缩尺寸
     * @param toY         动画结束Y坐标的伸缩尺寸
     * @param pivotXType  取值范围：Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_PARENT
     * @param pivotXValue 动画缩放时X坐标开始位置
     * @param pivotYType  取值范围：Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_PARENT
     * @param pivotYValue 动画缩放时X坐标开始位置
     * @param animParams  动画参数及回调
     */
    public static void animScale(View view, float fromX, float toX, float fromY, float toY, int pivotXType, float pivotXValue, int pivotYType, float pivotYValue, AnimParams animParams) {
        ScaleAnimation scale = new ScaleAnimation(fromX, toX, fromY, toY, pivotXType, pivotXValue, pivotYType, pivotYValue);
        startAnimation(view, scale);
        setParams(scale, animParams);
    }

    /**
     * 透明度控制动画
     *
     * @param view       动画view
     * @param fromAlpha  动画开始透明度
     * @param toAlpha    动画结束透明度
     * @param animParams 动画参数及回调
     */
    public static void animAlpha(View view, float fromAlpha, float toAlpha, AnimParams animParams) {
        AlphaAnimation alpha = new AlphaAnimation(fromAlpha, toAlpha);
        startAnimation(view, alpha);
        setParams(alpha, animParams);
    }

    /**
     * ***********************AnimParams设置********************************
     */
    private static void setParams(Animation animation, final AnimParams animParams) {
        if (animParams == null) {
            return;
        }
        setFillAfter(animation, animParams.fillAfter);
        if (animParams.durationMillis != 0L && animParams.durationMillis != 0) {
            setDuration(animation, animParams.durationMillis);
        }
        if (animParams.startOffset != 0L && animParams.startOffset != 0) {
            setStartOffset(animation, animParams.startOffset);
        }
        if (animParams.interpolator != null) {
            animation.setInterpolator(animParams.interpolator);
        }

        //监听动画结束
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animParams.onAnimationStart();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                animParams.onAnimationRepeat();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animParams.onAnimationEnd();
            }
        });
    }

    private static void setFillAfter(Animation animation, boolean fillAfter) {
        animation.setFillAfter(fillAfter);
    }

    private static void setDuration(Animation animation, long durationMillis) {
        animation.setDuration(durationMillis);
    }

    private static void setStartOffset(Animation animation, long startOffset) {
        animation.setStartOffset(startOffset);
    }

    private static void startAnimation(View view, Animation animation) {
        view.startAnimation(animation);
    }

    /**
     * *********************************************************************
     */

    public static class AnimParams {

        /**
         * 动画结束后是否保留当前状态
         */
        public boolean fillAfter;

        /**
         * 推迟动画开始时间
         */
        public long startOffset;

        /**
         * 动画持续时长
         */
        public long durationMillis;

        /**
         * 动画开始时长
         */
        public long startTimeMillis;

        /**
         * 插补，动画的控制器
         */
        public Interpolator interpolator;

        /**
         * @param fillAfter       动画结束后是否保留当前状态
         * @param startOffset     推迟动画开始时间
         * @param durationMillis  动画持续时长
         * @param startTimeMillis 动画开始时长
         */
        public AnimParams(final boolean fillAfter, final long startOffset, final long durationMillis, final long startTimeMillis) {
            this.fillAfter = fillAfter;
            this.startOffset = startOffset;
            this.durationMillis = durationMillis;
            this.startTimeMillis = startTimeMillis;
        }

        /**
         * @param fillAfter       动画结束后是否保留当前状态
         * @param startOffset     推迟动画开始时间
         * @param durationMillis  动画持续时长
         * @param startTimeMillis 动画开始时长
         * @param i               动画插补（控制器）
         */
        public AnimParams(final boolean fillAfter, final long startOffset, final long durationMillis, final long startTimeMillis, Interpolator i) {
            this.fillAfter = fillAfter;
            this.startOffset = startOffset;
            this.durationMillis = durationMillis;
            this.startTimeMillis = startTimeMillis;
            interpolator = i;
        }

        /**
         * @param durationMillis 动画持续时长
         *                       fillAfter = false
         *                       startOffset = 0
         *                       startTimeMillis = 0;
         */
        public AnimParams(final long durationMillis) {
            this.durationMillis = durationMillis;
        }

        /**
         * @param durationMillis
         * @param i              fillAfter = false
         *                       startOffset = 0
         *                       startTimeMillis = 0;
         */
        public AnimParams(final long durationMillis, Interpolator i) {
            this.durationMillis = durationMillis;
            interpolator = i;
        }

        public void onAnimationStart() {
        }

        public void onAnimationRepeat() {
        }

        public void onAnimationEnd() {
        }

    }
}
