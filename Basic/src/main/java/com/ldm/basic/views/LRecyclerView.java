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
     * 是否可以向下滑动（手指向下滑动）
     *
     * @return true/false
     */
    @Override
    public boolean isMoveDown() {
        boolean state;
        if (getChildCount() <= 0) {
            state = false;
        } else {
            LinearLayoutManager lm = (LinearLayoutManager) getLayoutManager();
            state = lm.findFirstVisibleItemPosition() == 0 && lm.findViewByPosition(lm.findFirstVisibleItemPosition()).getTop() == 0;
        }
        return state;
    }

    /**
     * 是否可以向上滑动（手指向上滑动）
     *
     * @return true/false
     */
    @Override
    public boolean isMoveUp() {
        return false;
    }
}
