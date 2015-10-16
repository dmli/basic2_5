package com.ldm.basic.anim;

import com.ldm.basic.utils.LMath;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.view.View;
import android.view.animation.DecelerateInterpolator;


/**
 * Created by ldm on 15/7/14.
 * 抛物线动画，仅能在API11以上的版本中使用
 */
@SuppressLint("NewApi")
public class ParabolaAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener {

    protected View v;
    protected PointF p0 = new PointF(), p1 = new PointF(), p2 = new PointF();

    /**
     * 给定三个点可以播放一个抛物线动画
     *
     * @param v  需要播放动画的View
     * @param p0 起点
     * @param p1 控制点
     * @param p2 结束点
     */
    public ParabolaAnimator(View v, PointF p0, PointF p1, PointF p2) {
        this.v = v;
        this.p0.set(p0.x, p0.y);
        this.p1.set(p1.x, p1.y);
        this.p2.set(p2.x, p2.y);
    }


    /**
     * 开始动画
     */
    public void startAnim() {
        double d = LMath.getDistance(p0, p2);
        startAnim((int) Math.max(d * 0.80f, 180));
    }

    /**
     * 开始动画
     *
     * @param duration 动画时间
     */
    public void startAnim(int duration) {
        if (isRunning()) {
            cancel();
        }
        setDuration(duration);
        setObjectValues(new PointF(0, 0));
        setInterpolator(new DecelerateInterpolator());
        setEvaluator(new TypeEvaluator<PointF>() {
            @Override
            public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
                long x = calcBezier(fraction, p0.x, p1.x, p2.x);
                long y = calcBezier(fraction, p0.y, p1.y, p2.y);
                return new PointF(x, y);
            }
        });
        v.setVisibility(View.VISIBLE);
        start();
        addUpdateListener(this);
    }

    protected long calcBezier(float interpolatedTime, float p0, float p1, float p2) {
        return Math.round((Math.pow((1 - interpolatedTime), 2) * p0) + (2 * (1 - interpolatedTime) * interpolatedTime * p1) + (Math.pow(interpolatedTime, 2) * p2));
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        PointF point = (PointF) animation.getAnimatedValue();
        v.setX(point.x);
        v.setY(point.y);
    }

}
