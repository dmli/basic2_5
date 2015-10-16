package com.ldm.basic.anim;

import java.lang.ref.WeakReference;

import com.ldm.basic.utils.SystemTool;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by ldm on 14-8-1. 移动缩放动画
 */
public class AnimTranslateScale extends AnimationSet {

	private int translateType = Animation.ABSOLUTE;// 类型，默认绝对值
	private float pivotX, pivotY;// 默认中心点位置
	private WeakReference<Activity> a;
	private boolean notScale, scaleZoomHeight, scaleZoomWidth;// 不执行放大
	private View v;
	private int targetOffX = 0, targetOffY = 0, originOffX = 0, originOffY = 0;// 目标的偏移量

	/**
	 * @param activity Activity
	 * @param view 动画将由这个view开始
	 */
	public AnimTranslateScale(Activity activity, View view) {
		super(false);
		a = new WeakReference<Activity>(activity);
		this.v = view;
		this.setInterpolator(new DecelerateInterpolator());
	}

	/**
	 * 设置一个将要被移动的View
	 * 
	 * @param v View
	 */
	public void setView(View v) {
		this.v = v;
	}

	/**
	 * 设置动画参数
	 * 
	 * @param tfx 移动动画 从x
	 * @param ttx 移动动画 到x
	 * @param tfy 移动动画 从y
	 * @param tty 移动动画 到y
	 * @param sfx 缩放动画 从x （数据是倍数关系 >1.0f放大 <1.0f缩小）
	 * @param stx 缩放动画 到x （数据是倍数关系 >1.0f放大 <1.0f缩小）
	 * @param sfy 缩放动画 从y （数据是倍数关系 >1.0f放大 <1.0f缩小）
	 * @param sty 缩放动画 到y （数据是倍数关系 >1.0f放大 <1.0f缩小）
	 */
	public void setParams(float tfx, float ttx, float tfy, float tty, float sfx, float stx, float sfy, float sty) {
		if (this.getAnimations().size() > 0) {
			this.getAnimations().clear();
		}
		this.reset();
		if (!notScale) {
			ScaleAnimation s = new ScaleAnimation(sfx, stx, sfy, sty, pivotX, pivotY);
			this.addAnimation(s);
		}
		TranslateAnimation t = new TranslateAnimation(translateType, tfx + originOffX, translateType, ttx + targetOffX, translateType, tfy + originOffY,
				translateType, tty + targetOffY);
		this.addAnimation(t);
	}

	/**
	 * 设置动画参数， 假设动画有view作为参考 可以直接传入
	 * 
	 * @param start 动画基于这个view开始
	 * @param end 动画达到这个view的时结束
	 */
	public void setParams(final View start, final View end) {
		Activity activity = a.get();
		if (activity != null) {
			// 给如view需要使用绝对位置制作动画，这里需要计算状态栏的高度
			final int statusBarHeight = SystemTool.getStatusBarHeight(activity);
			// -----------移动动画所需数据-------------
			int[] s = new int[2];
			start.getLocationInWindow(s);
			int[] e = new int[2];
			end.getLocationInWindow(e);
			if (!notScale) {
				// -----------缩放动画所需数据-------------
				// 目标
				int sw, sh, ew, eh;
				float sfx, stx, sfy, sty;
				if (scaleZoomHeight) {// 根据高度自动缩放
					sh = start.getHeight();
					eh = end.getHeight();
					sfy = 1.0f;
					sty = (eh * 1.0f / sh);
					sfx = 1.0f;
					stx = sty;
				} else if (scaleZoomWidth) {// 根据宽度自动缩放
					sw = start.getWidth();
					ew = end.getWidth();
					sfx = 1.0f;
					stx = (ew * 1.0f / sw);
					sfy = 1.0f;
					sty = stx;
				} else {// 根据目标宽高度自动缩放
					sw = start.getWidth();
					sh = start.getHeight();
					ew = end.getWidth();
					eh = end.getHeight();
					sfx = 1.0f;
					stx = (ew * 1.0f / sw);
					sfy = 1.0f;
					sty = (eh * 1.0f / sh);
				}
				// -----------------------
				setTranslateType(Animation.ABSOLUTE);// 设置绝对位置
				setPivot(0, 0);// 设置中心点
				setParams(s[0], e[0], s[1] - statusBarHeight, e[1] - statusBarHeight, sfx, stx, sfy, sty);
			} else {
				setParams(s[0], e[0], s[1] - statusBarHeight, e[1] - statusBarHeight, 0, 0, 0, 0);
			}
		}
	}

	/**
	 * 启动动画
	 * 
	 * @param durationMillis 动画时间
	 * @param callback 回调
	 */
	public void start(long durationMillis, Callback callback) {
		this.setDuration(durationMillis);// 设置动画时间
		this.setAnimationListener(callback);// 设置回调接口
		callback.setVew(v);//设置View后可以自动清除动画状态
		v.setVisibility(View.VISIBLE);
		v.startAnimation(this);
	}

	/**
	 * 设置移动参数类型 默认绝对位置
	 * 
	 * @param translateType
	 *        Animation.ABSOLUTE、Animation.RELATIVE_TO_PARENT、Animation.
	 *        RELATIVE_TO_SELF
	 */
	public void setTranslateType(int translateType) {
		this.translateType = translateType;
	}

	/**
	 * 设置缩放中心点 （中心点需要给绝对位置）
	 * 
	 * @param pivotX x中心点
	 * @param pivotY y中心点
	 */
	public void setPivot(float pivotX, float pivotY) {
		this.pivotX = pivotX;
		this.pivotY = pivotY;
	}

	/**
	 * 设置偏移量
	 * 
	 * @param offX x坐标
	 * @param offY y坐标
	 */
	public void setTargetOff(int offX, int offY) {
		this.targetOffX = offX;
		this.targetOffY = offY;
	}

	/**
	 * 起点的偏移量
	 * 
	 * @param offX x坐标
	 * @param offY y坐标
	 */
	public void setOriginOff(int offX, int offY) {
		this.originOffX = offX;
		this.originOffY = offY;
	}

	// 设置后将不执行放大
	public void setNotScale(boolean notScale) {
		this.notScale = notScale;
	}

	// 根据高度按比例缩放
	public void setScaleZoomHeight(boolean scaleZoomHeight) {
		this.scaleZoomHeight = scaleZoomHeight;
	}

	// 根据宽度按比例缩放
	public void setScaleZoomWidth(boolean scaleZoomWidth) {
		this.scaleZoomWidth = scaleZoomWidth;
	}

	/**
	 * 动画回调
	 */
	public static abstract class Callback implements AnimationListener {

		private View v;

		protected Callback() {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (v != null) {
				v.clearAnimation();
			}
		}

		public void setVew(View v) {
			this.v = v;
		}

	}
}
