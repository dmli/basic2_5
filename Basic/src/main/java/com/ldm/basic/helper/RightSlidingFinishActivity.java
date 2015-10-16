package com.ldm.basic.helper;

import com.ldm.basic.BasicActivity;
import com.ldm.basic.views.DropRightLayoutView;

import android.content.Intent;
import android.view.View;

/**
 * Created by ldm on 15/9/30. 右滑删除功能
 */
public class RightSlidingFinishActivity implements DropRightLayoutView.OnStateListener {

	public DropRightLayoutView rootView;
    BasicActivity activity;

    public RightSlidingFinishActivity(BasicActivity activity) {
        this.activity = activity;
        rootView = new DropRightLayoutView(activity);
        rootView.setOnStateListener(this);
    }

    public View createContentView(int layoutId) {
        View v = activity.getLayoutInflater().inflate(layoutId, rootView, false);
        rootView.addView(v);
        return rootView;
    }

    public View createContentView(View v) {
    	rootView.addView(v);
        return rootView;
    }

    @Override
    public void onStateListener(int state) {
        if (activity != null && activity.THIS_ACTIVITY_STATE && state == DropRightLayoutView.STATE_OPEN) {
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
