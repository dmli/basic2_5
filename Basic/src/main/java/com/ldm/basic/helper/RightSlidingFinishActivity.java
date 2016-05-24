package com.ldm.basic.helper;

import android.content.Intent;
import android.view.View;

import com.ldm.basic.BasicActivity;
import com.ldm.basic.views.LDropRightLayoutView;

/**
 * Created by ldm on 15/9/30. 右滑删除功能
 */
public class RightSlidingFinishActivity implements LDropRightLayoutView.OnStateListener {

    public LDropRightLayoutView rootView;
    BasicActivity activity;

    public RightSlidingFinishActivity(BasicActivity activity, View v) {
        this.activity = activity;
        rootView = new LDropRightLayoutView(activity);
        rootView.setOnStateListener(this);
        rootView.addView(v);
    }

    public RightSlidingFinishActivity(BasicActivity activity, int layoutId) {
        this.activity = activity;
        rootView = new LDropRightLayoutView(activity);
        rootView.setOnStateListener(this);
        rootView.addView(activity.getLayoutInflater().inflate(layoutId, rootView, false));
    }

    public View build() {
        return rootView;
    }

    @Override
    public void onStateListener(int state) {
        if (activity != null && activity.THIS_ACTIVITY_STATE && state == LDropRightLayoutView.STATE_OPEN) {
            Intent intent = activity.getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.overridePendingTransition(0, 0);
            activity.finish();
        }
    }

    @Override
    public void onScroll(int count, int position) {

    }
}
