package com.ldm.basic.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.ldm.basic.anim.BasicAnimationListener;

public class LZoomImageView extends ImageView {

	private ScaleAnimation animation1;
	private ScaleAnimation animation2;
	private OnClickListener clickListener;
	private OnLongClickListener longClickListener;
	private float scale = 0.9f;
	private GestureDetector gd;
	private boolean isClick = false;

	public LZoomImageView(Context context) {
		super(context);
		init(context);
	}

	public LZoomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LZoomImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		setClickable(true);
		gd = new GestureDetector(context, gestureListener);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			super.onDraw(canvas);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		if (getAnimation() != null) {
			this.clearAnimation();
		}
		this.startAnimation(animation2);
	}

	private void touchDown(MotionEvent event) {
		isClick = false;
		if (animation1 == null) {
			animation1 = new ScaleAnimation(1.0f, scale, 1.0f, scale, Animation.RELATIVE_TO_SELF, 0.50f, Animation.RELATIVE_TO_SELF, 0.50f);
			animation1.setDuration(80);
			animation1.setFillAfter(true);
		}
		if (animation2 == null) {
			animation2 = new ScaleAnimation(scale, 1.0f, scale, 1.0f, Animation.RELATIVE_TO_SELF, 0.50f, Animation.RELATIVE_TO_SELF, 0.50f);
			animation2.setDuration(80);
			animation2.setAnimationListener(animationListener);
			animation2.setFillAfter(false);
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
		this.clickListener = click;
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		this.longClickListener = l;
	}

	/**
	 * 设置缩放尺度
	 *
	 * @param scale 0.0f - 1.0f
	 */
	public void setScale(float scale) {
		this.scale = scale;
	}

	private BasicAnimationListener animationListener = new BasicAnimationListener() {
		@Override
		public void onAnimationEnd(Animation animation) {
			if (isClick) {
				if (clickListener != null) {
					clickListener.onClick(LZoomImageView.this);
				}
				isClick = false;
			}
		}
	};
	
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
			if (longClickListener != null) {
				longClickListener.onLongClick(LZoomImageView.this);
			}
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return true;
		}
	};
}
