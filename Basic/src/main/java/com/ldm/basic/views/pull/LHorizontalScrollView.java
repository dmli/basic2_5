package com.ldm.basic.views.pull;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Description : 横向滑动控件
 * Created by ldm  on 16/6/1.
 * Job number：143024
 * Phone ：18518677752
 * Email：lidaming@syswin.com
 * Person in charge : 李达明,李亚东
 * Leader：李亚东
 */
public class LHorizontalScrollView extends HorizontalScrollView {

    private OnScrollListener onScrollListener;

    /**
     * 设置一个滑动监听
     *
     * @param onScrollListener OnScrollListener
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    public LHorizontalScrollView(Context context) {
        super(context);
    }

    public LHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldL, int oldT) {
        super.onScrollChanged(l, t, oldL, oldT);
        if (this.onScrollListener != null) {
            this.onScrollListener.onScroll(this, getScrollX(), l, t, oldL, oldT);
        }
    }

    /**
     * 是否到达结尾处
     *
     * @return true/false
     */
    public boolean isEnd() {
        return getScrollX() + getWidth() >= computeHorizontalScrollRange();
    }

    public boolean isStart() {
        return getScrollX() == 0;
    }


    public interface OnScrollListener {
        void onScroll(final HorizontalScrollView v, final int scrollY, final int l, final int t, final int oldL, final int oldT);
    }
}
