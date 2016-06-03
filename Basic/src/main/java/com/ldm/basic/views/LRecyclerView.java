package com.ldm.basic.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.ldm.basic.views.pull.listener.OnPullAbsScrollListener;

/**
 * Description :
 * Created by ldm on 16/5/25.
 * Job number：143024
 * Phone ：18518677752
 * Email：lidaming@syswin.com
 * Person in charge : 李达明,李亚东
 * Leader：李亚东
 */
public class LRecyclerView extends RecyclerView implements OnPullAbsScrollListener {

    public LRecyclerView(Context context) {
        super(context);
    }

    public LRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 当前控件是否在最底部
     *
     * @return true/false
     */
    @Override
    public boolean isBottom() {
        LinearLayoutManager manager = (LinearLayoutManager) getLayoutManager();
        int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
        return lastVisibleItem == (manager.getItemCount() - 1);
    }

    /**
     * 当前控件是否在最顶部
     *
     * @return true/false
     */
    @Override
    public boolean isTop() {
        boolean state;
        if (getChildCount() <= 0) {
            state = false;
        } else {
            LinearLayoutManager manager = (LinearLayoutManager) getLayoutManager();
            state = manager.findFirstCompletelyVisibleItemPosition() == 0;
        }
        return state;
    }
}
