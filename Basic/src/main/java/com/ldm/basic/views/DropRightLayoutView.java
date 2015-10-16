package com.ldm.basic.views;

import com.nineoldandroids.view.ViewHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by ldm on 15/5/29. 支持右滑的布局，滑动时仅滑动控件内部的最后一个child（最上层的）
 */
public class DropRightLayoutView extends ViewGroup {

	private int LEFT_VALUE = 30;// 底部忽略距离
	public static final int STATE_DEFAULT = 0;
	public static final int STATE_OPEN = 1;
	private VelocityTracker vTracker;
	private float velocity;
	private float touchX, touchY, firstTouchX;
	private boolean isOpenScroll = true;
	public boolean isEnableChildrenCache = false;
	View lastView;// 最后
	int state = STATE_DEFAULT, oldState = STATE_DEFAULT;
	OnStateListener onStateListener;

	public DropRightLayoutView(Context context) {
		super(context);
	}

	public DropRightLayoutView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DropRightLayoutView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		LEFT_VALUE = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (vTracker != null) {
			try {
				vTracker.recycle();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		lastView = getChildAt(count - 1);
		for (int i = 0; i < count; i++) {
			View v0 = getChildAt(i);
			v0.layout(0, 0, v0.getMeasuredWidth(), v0.getMeasuredHeight());
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int count = getChildCount();
		lastView = getChildAt(count - 1);
		for (int i = 0; i < count; i++) {
			View v0 = getChildAt(i);
			v0.measure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean bool = super.onInterceptTouchEvent(ev);
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			oldState = state;
			firstTouchX = touchX = ev.getX();
			if (firstTouchX > LEFT_VALUE) {
				return bool;
			}
			touchY = ev.getY();
			if (vTracker == null) {
				vTracker = VelocityTracker.obtain();
				if (vTracker != null) {
					vTracker.addMovement(ev);
				}
			} else {
				vTracker.clear();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (isOpenScroll && Math.abs(touchX - ev.getX()) - Math.abs(touchY - ev.getY()) > 5) {
				bool = true;
				touchX = ev.getX();
				touchY = ev.getY();
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			break;
		default:
			break;
		}
		return bool;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (firstTouchX > LEFT_VALUE) {
			return false;// 本次不接收事件
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			oldState = state;
			touchX = event.getX();
			touchY = event.getY();
			if (vTracker == null) {
				vTracker = VelocityTracker.obtain();
				if (vTracker != null) {
					vTracker.addMovement(event);
				}
			} else {
				vTracker.clear();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			move((event.getX() - touchX));
			touchMove(event);
			touchX = event.getX();
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			touchUp();
		default:
			break;
		}
		return true;
	}

	private void touchMove(MotionEvent event) {
		vTracker.addMovement(event);
		vTracker.computeCurrentVelocity(1000);
		velocity = vTracker.getXVelocity();
	}

	private void touchUp() {
		final float x = ViewHelper.getTranslationX(lastView);
		if (velocity > 0) {// 向右
			if ((Math.abs(x) > lastView.getWidth() * 0.28f) || Math.abs(velocity) > 350) {
				moveToRight(x);
			} else {
				moveToLeft(x);
			}
		} else if (velocity < 0) {// 向左
			if ((Math.abs(x) < lastView.getWidth() * 0.72f) || Math.abs(velocity) > 350) {
				moveToLeft(x);
			} else {
				moveToRight(x);
			}
		} else {// velocity == 0
			if ((Math.abs(x) > lastView.getWidth() * 0.28f)) {
				moveToRight(x);
			} else {
				moveToLeft(x);
			}
		}
		if (vTracker != null) {
			try {
				vTracker.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 移动到右边
	 */
	private void moveToRight(float x) {
		state = STATE_OPEN;
		if (x == 0) {
			if (onStateListener != null && oldState != state) {
				onStateListener.onStateListener(state);
			}
		} else {
			lastView.startAnimation(new MyAnimation(x, lastView.getWidth()));
		}
	}

	/**
	 * 恢复到左边
	 */
	private void moveToLeft(float x) {
		state = STATE_DEFAULT;
		if (x == 0) {
			if (onStateListener != null && oldState != state) {
				onStateListener.onStateListener(state);
			}
		} else {
			lastView.startAnimation(new MyAnimation(x, 0));
		}
	}

	/**
	 * 移动lastView View
	 *
	 * @param d 移动的距离
	 */
	private void move(float d) {
		if (!isEnableChildrenCache) {
			enableChildrenCache();
		}
		float nx = ViewHelper.getTranslationX(lastView) + d;
		if (nx < -lastView.getWidth()) {
			nx = -lastView.getWidth();
		} else if (nx < 0) {
			nx = 0;
		}
		if (onStateListener != null) {
			onStateListener.onScroll(lastView.getWidth(), (int) nx);
		}
		final float n1 = lastView.getWidth();
		ViewHelper.setTranslationX(lastView, nx);
		final float s = Math.max(0.92f, (n1 - nx) / n1);
		if (ViewHelper.getAlpha(lastView) != s) {
			ViewHelper.setAlpha(lastView, s);
		}
	}

	/**
	 * 返回当前控件是否open状态
	 *
	 * @return true 当前open状态
	 */
	public boolean isOpen() {
		return state == STATE_OPEN;
	}

	/**
	 * 设置是否开启滑动
	 *
	 * @param isOpenScroll true开启 false关闭
	 */
	public void setIsOpenScroll(boolean isOpenScroll) {
		this.isOpenScroll = isOpenScroll;
	}

	/**
	 * 恢复到初始化时的位置
	 */
	public void restore() {
		state = STATE_DEFAULT;
		ViewHelper.setTranslationX(lastView, 0);
	}

	/**
	 * 恢复到初始化时的位置
	 */
	public void restoreToAnim() {
		state = STATE_DEFAULT;
		moveToLeft(ViewHelper.getTranslationX(lastView));
	}

	/**
	 * 开启缓存
	 */
	void enableChildrenCache() {
		isEnableChildrenCache = true;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View v = getChildAt(i);
			if (v != null) {
				v.setDrawingCacheEnabled(true);
			}
		}
	}

	/**
	 * 清除缓存
	 */
	void clearChildrenCache() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View v = getChildAt(i);
			if (v != null) {
				v.setDrawingCacheEnabled(false);
			}
		}
		isEnableChildrenCache = false;
	}

	public void setOnStateListener(OnStateListener onStateListener) {
		this.onStateListener = onStateListener;
	}

	private class MyAnimation extends Animation {

		final float start, d;
		final int width;

		public MyAnimation(float start, float end) {
			this.width = lastView.getWidth();
			this.start = start;
			this.d = end - start;
			this.setDuration((long) Math.max(Math.min(d * 0.35f, 350), 180));
			this.setAnimationListener(animationListener);
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			final float x = start + d * interpolatedTime;
			ViewHelper.setTranslationX(lastView, x);
			final float s = Math.max(0.92f, (width - x) / width);
			if (ViewHelper.getAlpha(lastView) != s) {
				ViewHelper.setAlpha(lastView, s);
			}
			if (onStateListener != null) {
				onStateListener.onScroll(lastView.getWidth(), (int) x);
			}
		}
	}

	private Animation.AnimationListener animationListener = new Animation.AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			clearChildrenCache();
			if (onStateListener != null && oldState != state) {
				onStateListener.onStateListener(state);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}
	};

	public interface OnStateListener {
		void onStateListener(int state);

		void onScroll(int count, int position);
	}
}
