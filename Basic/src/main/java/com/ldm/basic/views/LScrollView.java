package com.ldm.basic.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.ldm.basic.views.pull.listener.OnPullAbsScrollListener;

/**
 * Created by ldm on 14-9-4.
 * 带滚动事件的ScrollView
 */
public class LScrollView extends ScrollView implements OnPullAbsScrollListener {

    int oldY;
    OnScrollListener onScrollListener;

    public LScrollView(Context context) {
        super(context);
        init();
    }

    public LScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldL, int oldT) {
        super.onScrollChanged(l, t, oldL, oldT);
        if (this.onScrollListener != null) {
            this.onScrollListener.onScroll(this, getScrollY(), l, t, oldL, oldT);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                removeCallbacks(scrollerTask);
                postDelayed(scrollerTask, 25);
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 设置滑动监听事件
     *
     * @param onScrollListener LScrollView.OnScrollListener
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    /**
     * 是否达到底部
     *
     * @return true 在底部
     */
    public boolean isBottom() {
        return getScrollY() + getHeight() >= computeVerticalScrollRange();
    }

    /**
     * 是否在最顶端
     *
     * @return true 在顶部
     */
    public boolean isTop() {
        return getScrollY() <= 0;
    }

    /**
     * 滚动到顶部
     */
    public void scrollToTop() {
        scrollTo(0, 0);
    }

    /**
     * 滚动到低部
     */
    public void scrollToBottom() {
        if (getHeight() <= 1) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollToBottom();
                }
            }, 100);
        } else {
            scrollTo(0, computeVerticalScrollRange() - getHeight());
        }
    }

    @Override
    public boolean isMoveDown() {
        return isTop();
    }

    @Override
    public boolean isMoveUp() {
        return isBottom();
    }

    public interface OnScrollListener {
        void onScroll(final LScrollView v, final int scrollY, final int l, final int t, final int oldL, final int oldT);

        void onScrollFinished(float velocity);
    }


    private Runnable scrollerTask = new Runnable() {
        @Override
        public void run() {
            if (oldY == getScrollY()) {
                if (onScrollListener != null) {
                    onScrollListener.onScrollFinished(0);
                }
            } else {
                oldY = getScrollY();
                postDelayed(this, 25);
            }
        }
    };

}
