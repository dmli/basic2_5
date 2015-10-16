package com.ldm.basic.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

public class LGridView extends GridView {
	
	private boolean isOnMeasure;
	private OnInterceptTouchEventListener onInterceptTouchEventListener;

	public LGridView(Context context) {
	        super(context);
	    }

	public LGridView(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }

	public LGridView(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
	    }

	public boolean isOnMeasure() {
		return isOnMeasure;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		isOnMeasure = true;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		isOnMeasure = false;
		super.onLayout(changed, l, t, r, b);
	}
	
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (onInterceptTouchEventListener != null) {
            onInterceptTouchEventListener.onInterceptTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setOnInterceptTouchEventListener(OnInterceptTouchEventListener onInterceptTouchEventListener) {
        this.onInterceptTouchEventListener = onInterceptTouchEventListener;
    }

    public interface OnInterceptTouchEventListener {
        void onInterceptTouchEvent(MotionEvent ev);
    }
}
