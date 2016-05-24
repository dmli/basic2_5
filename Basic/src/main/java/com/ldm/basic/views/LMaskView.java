package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.ldm.basic.utils.LMath;


/**
 * Created by ldm on 16/5/18.
 * 用来作为遮罩使用的View
 */
public class LMaskView extends View {

    private Paint paint;

    private RectF rectF;
    private int maskColor = Color.WHITE;
    private float radios;

    float progress = 360;
    float interpolated;

    /**
     * 设置遮罩颜色
     *
     * @param maskColor 遮罩颜色
     */
    public void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
        if (paint != null) {
            paint.setColor(maskColor);
        }
    }

    public LMaskView(Context context) {
        super(context);
        init();
    }

    public LMaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LMaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rectF = new RectF(0, 0, 0, 0);
        paint = new Paint();
        paint.setColor(maskColor);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.FILL);
    }


    public void hide(int duration) {
        if (getAnimation() != null) {
            getAnimation().cancel();
            clearAnimation();
        }
        HideDrawAnimation animation = new HideDrawAnimation(duration, this);
        animation.setAnimationListener(new OnGoneAnimationListener(this));
        startAnimation(animation);
    }

    public void show(int duration) {
        if (getAnimation() != null) {
            getAnimation().cancel();
            clearAnimation();
        }
        HideDrawAnimation animation = new HideDrawAnimation(duration, this);
        setVisibility(VISIBLE);
        startAnimation(animation);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() > 0) {
            int maxDiameter = (int) LMath.getDistance(0, 0, getWidth(), getHeight());
            radios = maxDiameter * 0.5f;
            int top = (int) ((getHeight() - maxDiameter) * 0.50f);
            int left = (int) ((getWidth() - maxDiameter) * 0.50f);
            rectF.set(left, top, maxDiameter + left, maxDiameter + top);
        }

        if (rectF.width() > 0) {
            canvas.drawCircle(getWidth() * 0.5f, getHeight() * 0.5F, radios * (1 - interpolated), paint);
            canvas.drawArc(rectF, 180, progress, true, paint);
        }
    }

    /**
     * @param p 角度
     */
    void progress(float p) {
        this.progress = p;
        invalidate();
    }


    private class HideDrawAnimation extends Animation {
        LMaskView view;

        public HideDrawAnimation(int duration, LMaskView view) {
            this.view = view;
            this.setInterpolator(new LinearInterpolator());
            this.setDuration(duration);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float fl = -360 + 360 * interpolatedTime;
            interpolated = interpolatedTime;
            progress(fl);
        }

    }

    private class RecoveryDrawAnimation extends Animation {
        LMaskView view;

        public RecoveryDrawAnimation(int duration, LMaskView view) {
            this.view = view;
            this.setInterpolator(new LinearInterpolator());
            this.setDuration(duration);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float fl = 360 + 360 * interpolatedTime;
            interpolated = interpolatedTime;
            progress(fl);
        }
    }

    private class OnGoneAnimationListener implements Animation.AnimationListener {

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
                view.setVisibility(GONE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
