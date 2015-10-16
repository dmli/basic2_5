package com.ldm.basic.views;

import com.ldm.basic.utils.MeasureHelper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;

/**
 * Created by ldm on 14/11/17. 
 * 状态按钮 
 * 1、这个View内部可以容纳2个相同大小的View, 当用户触发时将会执行切换操作
 * 2、点击之后将会内部将会计时，直到用户触发重新计时或时间超出预设范围，用户才可以再次点击 当使用时仅存放一个View时仅有第2项功能
 */
public class LStateViewGroup extends ViewGroup implements View.OnClickListener {

	private long time = -1;
	private long delayTime = 500;
	private View.OnClickListener onClickListener;
	private int duration = 120;
	private int count;
	private OnStateSwitchListener onStateSwitchListener;
	private int maxWidth, maxHeight;

	public LStateViewGroup(Context context) {
		super(context);
		init();
	}

	public LStateViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LStateViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		count = getChildCount();
		if (count == 2) {
			getChildAt(1).setVisibility(INVISIBLE);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int count = getChildCount();
		for (int j = 0; j < count; j++) {
			View v = getChildAt(j);
			v.measure(MeasureHelper.getWidth(v, widthMeasureSpec), MeasureHelper.getHeight(v, heightMeasureSpec));
			if (v.getMeasuredWidth() > maxWidth) {
				maxWidth = v.getMeasuredWidth();
			}
			if (v.getMeasuredHeight() > maxHeight) {
				maxHeight = v.getMeasuredHeight();
			}
		}
		setMeasuredDimension( getPaddingLeft() + maxWidth + getPaddingRight(), getPaddingTop() + maxHeight + getPaddingBottom());
	}

	@Override
	protected void onLayout(boolean b, int i, int i2, int i3, int i4) {
		int count = getChildCount();
		if (count > 0) {
			int left = getPaddingLeft();
			int top = getPaddingTop();
			for (int j = 0; j < count; j++) {
				View v = getChildAt(j);
				v.layout(left, top, left + v.getMeasuredWidth(), top + v.getMeasuredHeight());
			}
		}
	}

	private void init() {
		super.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (onClickListener != null && (time == -1 || System.currentTimeMillis() - time > delayTime)) {
			time = System.currentTimeMillis();
			if (count == 2) {
				switchViewToRotate(getChildAt(0), getChildAt(1), getChildAt(0).getVisibility() == VISIBLE);
			}
			onClickListener.onClick(v);
		}
	}

	public void resume() {
		time = -1;
	}

	public void setDelayTime(long time) {
		this.delayTime = time;
	}

	@Override
	public void setOnClickListener(View.OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	public void setOnStateSwitchListener(OnStateSwitchListener onStateSwitchListener) {
		this.onStateSwitchListener = onStateSwitchListener;
	}

	/**
	 * 对给定的View进行切换并执行RotateAnimation动画
	 * 
	 * @param v1 View
	 * @param v2 View
	 * @param reverse true逆向翻转
	 */
	public void switchViewToRotate(final View v1, final View v2, boolean reverse) {
		if (onStateSwitchListener != null) {
			onStateSwitchListener.onStateSwitchBeginListener(reverse ? 1 : 0);
		}
		if (reverse) {
			Animation rotateAnimation0 = new RotateAnimation(0, 90, Animation.RELATIVE_TO_SELF, 0.50f, Animation.RELATIVE_TO_SELF, 0.50f);
			rotateAnimation0.setDuration(duration);
			Animation rotateAnimation1 = new RotateAnimation(-90, 0, Animation.RELATIVE_TO_SELF, 0.50f, Animation.RELATIVE_TO_SELF, 0.50f);
			rotateAnimation1.setStartOffset(duration);
			rotateAnimation1.setDuration(duration);
			rotateAnimation1.setAnimationListener(new AnimListener(v1, v2, reverse));
			v1.setVisibility(VISIBLE);
			v1.startAnimation(rotateAnimation0);
			v2.startAnimation(rotateAnimation1);
		} else {
			Animation rotateAnimation0 = new RotateAnimation(0, -90, Animation.RELATIVE_TO_SELF, 0.50f, Animation.RELATIVE_TO_SELF, 0.50f);
			rotateAnimation0.setDuration(duration);
			Animation rotateAnimation1 = new RotateAnimation(90, 0, Animation.RELATIVE_TO_SELF, 0.50f, Animation.RELATIVE_TO_SELF, 0.50f);
			rotateAnimation1.setStartOffset(duration);
			rotateAnimation1.setDuration(duration);
			rotateAnimation1.setAnimationListener(new AnimListener(v2, v1, reverse));
			v2.setVisibility(VISIBLE);
			v2.startAnimation(rotateAnimation0);
			v1.startAnimation(rotateAnimation1);
		}
	}

	/**
	 * 设置动画播放时间
	 * 
	 * @param duration 默认120
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public class AnimListener implements AnimationListener {

		private View hide, show;
		private boolean reverse;

		/**
		 * 动画结束时将自动隐藏v1现实v2
		 * 
		 * @param v1 将被隐藏
		 * @param v2 将被显示
		 */
		public AnimListener(View hide, View show, boolean reverse) {
			this.hide = hide;
			this.show = show;
			this.reverse = reverse;
		}

		@Override
		public void onAnimationStart(Animation animation) {
			hide.setVisibility(View.INVISIBLE);
			show.setVisibility(View.VISIBLE);
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (onStateSwitchListener != null) {
				onStateSwitchListener.onStateSwitchBeginListener(reverse ? 1 : 0);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}
	}

	/**
	 * 设置这个监听事件后将会对内部View进行切换
	 * 
	 * @author ldm
	 * 
	 */
	public interface OnStateSwitchListener {

		/**
		 * 状态切换开始时触发这个方法
		 * 
		 * @param visibilityPosition 位置
		 */
		public void onStateSwitchBeginListener(int visibilityPosition);

		/**
		 * 状态切换结束时触发这个方法
		 * 
		 * @param visibilityPosition 位置
		 */
		public void onStateSwitchEndListener(int visibilityPosition);
	}
}
