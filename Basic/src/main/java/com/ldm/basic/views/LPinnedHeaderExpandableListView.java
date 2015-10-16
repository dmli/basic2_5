/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2014 singwhatiwanna
 * https://github.com/singwhatiwanna
 * http://blog.csdn.net/singwhatiwanna
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ExpandableListView;


public class LPinnedHeaderExpandableListView extends ExpandableListView implements OnScrollListener {

    private View mHeaderView;
    private int mHeaderWidth;
    private int mHeaderHeight;
    private int headerLeft, headerRight;

    private View mTouchTarget;
    private int headerViewNowPosition = -1;//浮动view正在使用的位置
    
    private boolean isOnMeasure;

    private OnScrollListener mScrollListener;

    private boolean mActionDownHappened = false;
    private OnInterceptTouchEventListener onInterceptTouchEventListener;

    public boolean isOnMeasure() {
        return isOnMeasure;
    }

    public LPinnedHeaderExpandableListView(Context context) {
        super(context);
        initView();
    }

    public LPinnedHeaderExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LPinnedHeaderExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        setFadingEdgeLength(0);
        super.setOnScrollListener(this);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mScrollListener = l;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	isOnMeasure = true;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderWidth = mHeaderView.getMeasuredWidth();
            mHeaderHeight = mHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	isOnMeasure = false;
        super.onLayout(changed, l, t, r, b);
        if (mHeaderView != null) {
            int delta = mHeaderView.getTop();
            headerLeft = getPaddingLeft();
            headerRight = Math.min((getWidth() - getPaddingRight()), mHeaderWidth + headerLeft);
            mHeaderView.layout(headerLeft, delta, headerRight, mHeaderHeight + delta);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderView != null) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int x = (int) ev.getX();
        final int y = (int) ev.getY();
        final int pos = pointToPosition(x, y);
        if (mHeaderView != null && y >= mHeaderView.getTop() && y <= mHeaderView.getBottom()) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                mTouchTarget = getTouchTarget(mHeaderView, x, y);
                mActionDownHappened = true;
            } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                View touchTarget = getTouchTarget(mHeaderView, x, y);
                if (touchTarget == mTouchTarget && mTouchTarget.isClickable()) {
                    mTouchTarget.performClick();
                    invalidate(new Rect(0, 0, mHeaderWidth, mHeaderHeight));
                } else {
                    int groupPosition = getPackedPositionGroup(getExpandableListPosition(pos));
                    if (groupPosition != INVALID_POSITION && mActionDownHappened) {
                        if (isGroupExpanded(groupPosition)) {
                            collapseGroup(groupPosition);
                        } else {
                            expandGroup(groupPosition);
                        }
                    }
                }
                mActionDownHappened = false;
            }
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    private View getTouchTarget(View view, int x, int y) {
        if (!(view instanceof ViewGroup)) {
            return view;
        }
        ViewGroup parent = (ViewGroup) view;
        int childrenCount = parent.getChildCount();
        final boolean customOrder = isChildrenDrawingOrderEnabled();
        View target = null;
        for (int i = childrenCount - 1; i >= 0; i--) {
            final int childIndex = customOrder ? getChildDrawingOrder(childrenCount, i) : i;
            final View child = parent.getChildAt(childIndex);
            if (isTouchPointInView(child, x, y)) {
                target = child;
                break;
            }
        }
        if (target == null) {
            target = parent;
        }
        return target;
    }

    private boolean isTouchPointInView(View view, int x, int y) {
        return view.isClickable() && y >= view.getTop() && y <= view.getBottom() && x >= view.getLeft() && x <= view.getRight();
    }

    /**
     * 刷新移动的headView
     */
    public void requestRefreshHeader() {
        refreshHeader();
        invalidate(new Rect(0, 0, mHeaderWidth, mHeaderHeight));
    }

    /**
     * 强制刷新移动的headView
     * 一般在adapter数据更新时调用
     */
    public void enforceRequestRefreshHeader() {
        mHeaderView = null;
        refreshHeader();
        invalidate(new Rect(0, 0, mHeaderWidth, mHeaderHeight));
    }

    protected void refreshHeader() {
        final int headerLeft = this.headerLeft;
        final int headerRight = this.headerRight;
        final int p2 = getFirstVisiblePosition() + 1;
        final int g1 = ExpandableListView.getPackedPositionGroup(this.getExpandableListPosition(getFirstVisiblePosition()));
        final int g2 = ExpandableListView.getPackedPositionGroup(this.getExpandableListPosition(p2));
        if (g1 == AdapterView.INVALID_POSITION) {
            if (mHeaderView != null) {
                enforceRequestRefreshHeader();
            }
        } else {
            if (this.isGroupExpanded(g1)) {
                if (mHeaderView == null || g1 != headerViewNowPosition) {
                    mHeaderView = this.getExpandableListAdapter().getGroupView(g1, true, null, this);
                    mHeaderWidth = mHeaderView.getWidth();
                    mHeaderHeight = mHeaderView.getHeight();
                    headerViewNowPosition = g1;
                    requestLayout();
                }
                if (g2 == g1 + 1) {
                    View view = getChildAt(1);
                    if (view != null) {
                        if (view.getTop() <= mHeaderHeight) {
                            int delta = mHeaderHeight - view.getTop();
                            mHeaderView.layout(headerLeft, -delta, headerRight, mHeaderHeight - delta);
                        } else {
                            mHeaderView.layout(headerLeft, 0, headerRight, mHeaderHeight);
                        }
                    }
                } else {
                    mHeaderView.layout(headerLeft, 0, headerRight, mHeaderHeight);
                }
            }
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mHeaderView != null && scrollState == SCROLL_STATE_IDLE) {
            int firstVisiblePos = getFirstVisiblePosition();
            if (firstVisiblePos == 0) {
                mHeaderView.layout(headerLeft, 0, headerRight, mHeaderHeight);
            }
        }
        if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount > 0) {
            refreshHeader();
        }
        if (mScrollListener != null) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    /**
     * 隐藏驻留的View
     */
    public void hideHeaderView() {
        mHeaderView = null;
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