package com.ldm.basic.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

/**
 * Created by ldm on 15/7/14.
 * 给定一个View，指定一个起始点及终点图标将根据贝塞尔曲线做一个移动
 */
public class FlyingView extends ImageView {


    private Point startPoint, endPoint;
    public boolean isRuning;

    public FlyingView(Context context) {
        super(context);
        init();
    }

    public FlyingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlyingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {


    }

    public void play() {
    	if (isRuning || getAnimation() != null) {
    		return;
		}
        startPoint = new Point();
        endPoint = new Point();

        startPoint.set(100, 100);
        endPoint.set(360, 1280);

        Point controlPoint = new Point();
        controlPoint.set(360, 200);
        MyAnimation a = new MyAnimation(startPoint, endPoint, controlPoint);
        a.setAnimationListener(animationListener);
        startAnimation(a);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


    }

    public void setPoint(Point startPoint, Point endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }




    private long calcBezier(float interpolatedTime, float p0, float p1, float p2) {
        return Math.round((Math.pow((1 - interpolatedTime), 2) * p0)
                + (2 * (1 - interpolatedTime) * interpolatedTime * p1)
                + (Math.pow(interpolatedTime, 2) * p2));
    }

    private class MyAnimation extends Animation{

        private Point startPoint, endPoint, controlPoint;

        public MyAnimation(Point startPoint, Point endPoint, Point controlPoint) {
            this.startPoint = startPoint;
            this.endPoint = endPoint;
            this.controlPoint = controlPoint;
            this.setDuration(1000);
        }

        @SuppressLint("NewApi")
		@Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            long x = calcBezier(interpolatedTime, startPoint.x, controlPoint.x, endPoint.x);
            long y = calcBezier(interpolatedTime, startPoint.y, controlPoint.y, endPoint.y);

            FlyingView.this.setTranslationX(x);
            FlyingView.this.setTranslationY(y);

        }
    }

    public boolean isRuning(){
    	return isRuning;
    }
    

    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        	isRuning = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        	isRuning = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

}
