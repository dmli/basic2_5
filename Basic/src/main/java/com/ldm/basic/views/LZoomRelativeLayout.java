package com.ldm.basic.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import com.ldm.basic.anim.BasicAnimationListener;

/**
 * Created by ldm on 14-7-14. 
 * 点击有缩放效果的RelativeLayout，使用这个view后内部控件将无法接收到事件
 */
public class LZoomRelativeLayout extends RelativeLayout {

	private ScaleAnimation animation1;
	private ScaleAnimation animation2;
	private OnClickListener click;
	private float scale = 0.9f;
	private Object obj;
	private GestureDetector gd;
	private boolean isClick = false;

	public LZoomRelativeLayout(Context context) {
		super(context);
		init(context);
	}

	public LZoomRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LZoomRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		gd = new GestureDetector(context, gestureListener);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (this.getVisibility() != VISIBLE) {
			return false;
		}
		boolean bo = gd.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			touchCancel();
			break;
		default:
			break;
		}
		return bo;
	}

	private void touchCancel() {
		setPressed(false);
		if (getAnimation() != null) {
			this.clearAnimation();
		}
		this.startAnimation(animation2);
	}

	private void touchDown(MotionEvent event) {
		setPressed(true);
		isClick = false;
		if (animation1 == null) {
			animation1 = new ScaleAnimation(1.0f, scale, 1.0f, scale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			animation1.setDuration(80);
			animation1.setFillAfter(true);
		}
		if (animation2 == null) {
			animation2 = new ScaleAnimation(scale, 1.0f, scale, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			animation2.setDuration(80);
			animation2.setFillAfter(false);
			animation2.setAnimationListener(animationListener);
		}
		this.startAnimation(animation1);
	}

	/**
	 * 设置点击事件
	 *
	 * @param click View.OnClickListener
	 */
	@Override
	public void setOnClickListener(OnClickListener click) {
		this.click = click;
	}

	private BasicAnimationListener animationListener = new BasicAnimationListener() {
		@Override
		public void onAnimationEnd(Animation animation) {
			if (isClick) {
				if (click != null) {
					click.onClick(LZoomRelativeLayout.this);
				}
				isClick = false;
			}
		}
	};
	
	/**
	 * 设置缩放尺度
	 *
	 * @param scale 0.0f - 1.0f
	 */
	public void setScale(float scale) {
		this.scale = scale;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
		@Override
		public boolean onDown(MotionEvent e) {
			touchDown(e);
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			isClick = true;
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {

		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return true;
		}
	};
}
