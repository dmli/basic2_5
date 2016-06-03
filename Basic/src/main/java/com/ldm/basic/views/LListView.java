package com.ldm.basic.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.ldm.basic.views.pull.listener.OnPullAbsScrollListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldm on 15/3/6.
 * 增加了一个OnInterceptTouchEventListener监听，仅限拦截，不能够妨碍onInterceptTouchEvent(MotionEvent)的实际处理
 */
public class LListView extends ListView implements OnPullAbsScrollListener {

    private boolean isOnMeasure;
    private OnInterceptTouchEventListener onInterceptTouchEventListener;

    public LListView(Context context) {
        super(context);
    }

    public LListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private int totalHeight;
    private List<Integer> childHeight = new ArrayList<>();

    /**
     * 这个方法仅能获取第一屏的滑动距离
     *
     * @param position item的位置
     * @return 当前移动偏移量
     */
    public int getScrollY(int position) {
        if (position < 0) {
            return 0;
        }
        if (position >= childHeight.size()) {
            return totalHeight + getPaddingTop();
        }
        if (position == 0) {
            return -getChildAt(0).getTop() + getPaddingTop();
        }

        int top = 0;
        for (int i = 0; i < position; i++) {
            top += childHeight.get(i);
        }
        return top - getChildAt(0).getTop() + getPaddingTop();
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
        //记录第一屏View的高度
        if (getFirstVisiblePosition() <= 0) {
            totalHeight = 0;
            childHeight.clear();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                totalHeight += getChildAt(i).getHeight();
                childHeight.add(getChildAt(i).getHeight());
            }
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
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

    public void addPlaceholderView(int height) {
        View view = new View(getContext());
        view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        this.addHeaderView(view, null, false);
    }

    @Override
    public boolean isBottom() {
        return (getChildCount() <= 0) || (getLastVisiblePosition() == getCount() - 1) && (getChildAt(getChildCount() - 1).getBottom() == getBottom() + getPaddingBottom());
    }

    @Override
    public boolean isTop() {
        return getChildCount() <= 0 || (getFirstVisiblePosition() == 0 && getChildAt(0).getTop() == getPaddingTop());
    }

    public interface OnInterceptTouchEventListener {
        void onInterceptTouchEvent(MotionEvent ev);
    }
}
