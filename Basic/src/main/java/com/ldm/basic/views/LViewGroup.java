package com.ldm.basic.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.AbsListView;

/**
 * Created by ldm on 15-5-27. 
 * 继承这个ViewGroup的View，表示可以在LNotBoringActionBarView下使用
 * （未完成）
 */
public abstract class LViewGroup extends ViewGroup {

	public LViewGroup(Context context) {
		super(context);
	}

	public LViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

	}

	/**
	 * 是否达到底部
	 *
	 * @return true 在底部
	 */
	public abstract boolean isBottom();

	/**
	 * 是否在最顶端
	 *
	 * @return true 在顶部
	 */
	public abstract boolean isTop();

	/**
	 * 设置一个OnScrollListener监听
	 */
	public abstract void setOnScrollListener(AbsListView.OnScrollListener onScrollListener);

}
