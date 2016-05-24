package com.ldm.basic.utils.image;

import android.widget.AbsListView;

/**
 * Created by ldm on 16/5/4.
 * 用来控制AbsListView在滑动时不执行任务
 */
public class LazyImageOnScrollListener implements AbsListView.OnScrollListener {

    private LazyImageDownloader lazyImageDownloader;
    private AbsListView.OnScrollListener onScrollListener;

    public LazyImageOnScrollListener(LazyImageDownloader lazyImageDownloader, AbsListView.OnScrollListener onScrollListener) {
        this.lazyImageDownloader = lazyImageDownloader;
        this.onScrollListener = onScrollListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                lazyImageDownloader.scrollState = LazyImageDownloader.SCROLL_STATE_BUSY;
                if (lazyImageDownloader.SCROLL_FLING_P_IDS.size() > 0) {
                    synchronized (lazyImageDownloader.SCROLL_FLING_P_IDS) {
                        lazyImageDownloader.addTaskAll(lazyImageDownloader.SCROLL_FLING_P_IDS.refs);
                        lazyImageDownloader.SCROLL_FLING_P_IDS.clear();
                    }
                }
                lazyImageDownloader.scrollState = LazyImageDownloader.SCROLL_STATE_IDLE;
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                lazyImageDownloader.scrollState = LazyImageDownloader.SCROLL_STATE_FLING;
                break;
            default:
                break;
        }
        if (onScrollListener != null) {
            onScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (onScrollListener != null) {
            onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }
}
