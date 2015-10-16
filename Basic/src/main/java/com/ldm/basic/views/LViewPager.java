package com.ldm.basic.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Hacky fix for Issue #4 and http://code.google.com/p/android/issues/detail?id=18990
 * <p/>
 * ScaleGestureDetector seems to mess up the touch events, which means that ViewGroups which make use of
 * onInterceptTouchEvent throw a lot of IllegalArgumentException: pointerIndex out of range.
 * <p/>
 * There's not much I can do in my code for now, but we can mask the result by just catching the problem and ignoring
 * it.
 *
 * @author Chris Banes
 */
public class LViewPager extends ViewPager {

	/**
	 * 忽略的view，当用户touch在这个View区域内部时事件传递给child处理
	 */
	private View[] ignoreViews = null;
	private int ignoreCurrentIndex;

	public LViewPager(Context context) {
		super(context);
	}

	public LViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	/**
	 * 设置忽略的当前View
	 * 
	 * @param index 当前索引
	 */
	public void setIgnoreCurrentIndex(int index) {
		ignoreCurrentIndex = index;
	}

	/**
	 * 使用忽略View功能时需要调用这个方法设置最大忽略View的数量
	 * 
	 * @param maxLenght 最大数量
	 */
	public void openTouchIgnoreViewFunc(int maxLenght) {
		this.ignoreViews = new View[maxLenght];
	}

	/**
	 * 设置忽略View
	 * 
	 * @param index 索引
	 * @param ignoreView View
	 */
	public void setIgnoreView(int index, View ignoreView) {
		if (this.ignoreViews == null) {
			throw new NullPointerException("这个方法需要先使用openIgnoreViewFunc(N)激活后才能使用！");
		}
		this.ignoreViews[index] = ignoreView;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (ignoreViews != null && ignoreViews[ignoreCurrentIndex] != null && isViewUnder(ignoreViews[ignoreCurrentIndex], (int) ev.getX(), (int) ev.getY())) {
			/**
			 * 本次事件交给child处理
			 */
			return false;
		}
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (IllegalArgumentException e) {
			return false;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	@Override
	public void setCurrentItem(int item, int duration) {
		if (getCurrentItem() != item) {
			super.setCurrentItem(item, Math.abs(getCurrentItem() - item) * duration);
		}
	}

	/**
	 * setCurrentItem(int item, int duration)的扩展
	 *
	 * @param item 新位置
	 * @param duration 速度
	 * @param singleSpeed 是否使用单一item速度 （设置true，将不会计算距离的位置）
	 */
	public void setCurrentItem(int item, int duration, boolean singleSpeed) {
		if (getCurrentItem() != item) {
			super.setCurrentItem(item, singleSpeed ? duration : Math.abs(getCurrentItem() - item) * duration);
		}
	}

	public boolean isViewUnder(View view, int x, int y) {
		return x >= view.getLeft() && x < view.getRight() && y >= view.getTop() && y < view.getBottom();
	}

}
