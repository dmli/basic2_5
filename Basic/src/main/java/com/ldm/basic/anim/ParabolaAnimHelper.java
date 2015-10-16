package com.ldm.basic.anim;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by ldm on 15/7/14.
 * 抛物线动画，仅能在API11以上的版本中使用
 */
public class ParabolaAnimHelper extends Animation {

	private View v;
	private Point p0 = new Point(), p1 = new Point(), p2 = new Point();
	
	/**
	 * 给定三个点可以播放一个抛物线动画
	 * @param v 需要播放动画的View
	 * @param p0 起点
	 * @param p1 控制点
	 * @param p2 结束点
	 */
	public ParabolaAnimHelper(View v, Point p0, Point p1, Point p2) {
		this.v = v;
		this.p0.set(p0.x, p0.y);
		this.p1.set(p1.x, p1.y);
		this.p2.set(p2.x, p2.y);
	}

	/**
	 * 开始动画
	 */
	public void startAnim() {
		if (v.getAnimation() != null) {
			v.getAnimation().cancel();
		}
		v.startAnimation(this);
	}

	private long calcBezier(float interpolatedTime, float p0, float p1, float p2) {
		return Math.round((Math.pow((1 - interpolatedTime), 2) * p0) + (2 * (1 - interpolatedTime) * interpolatedTime * p1) + (Math.pow(interpolatedTime, 2) * p2));
	}

	@SuppressLint("NewApi")
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		long x = calcBezier(interpolatedTime, p0.x, p1.x, p2.x);
		long y = calcBezier(interpolatedTime, p0.y, p1.y, p2.y);

		v.setTranslationX(x);
		v.setTranslationY(y);
	}

}
