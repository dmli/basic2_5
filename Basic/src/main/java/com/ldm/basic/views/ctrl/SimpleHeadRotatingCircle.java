package com.ldm.basic.views.ctrl;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ldm.basic.views.PullToRefreshView;
import com.miyou.basic.R;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by ldm on 12/11/14.
 * 下拉刷新的动画处理《圆环旋转》
 */
public class SimpleHeadRotatingCircle implements PullToRefreshView.BasicHeadAnimation {

    private int nowState;
    private TextView lPullToRefreshText;
    private ImageView lPullToRefreshImage;
    private View lPullToRefreshProgress;

    public SimpleHeadRotatingCircle() {
        nowState = PullToRefreshView.DONE;
    }

    @Override
    public void pullProgress(View headNode, int count, int p) {
        if (lPullToRefreshText == null) {
            initView(headNode);
        }
        ViewHelper.setRotation(lPullToRefreshImage, -p * 2);
    }

    @Override
    public void releaseToRefresh(View headNode) {
        if (lPullToRefreshText == null) {
            initView(headNode);
        }
        lPullToRefreshText.setVisibility(View.VISIBLE);
        lPullToRefreshText.setText(R.string.unlock_to_refresh);
    }

    @Override
    public void scrollStop() {
    }

    @Override
    public void pullToRefresh(View headNode) {
        if (lPullToRefreshText == null) {
            initView(headNode);
        }
        lPullToRefreshText.setVisibility(View.VISIBLE);
        lPullToRefreshText.setText(R.string.down_push_to_rrefresh_text);
        lPullToRefreshProgress.setVisibility(View.GONE);
        lPullToRefreshImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void refresh(View headNode) {
        if (lPullToRefreshText == null) {
            initView(headNode);
        }
        lPullToRefreshImage.setVisibility(View.GONE);
        lPullToRefreshProgress.setVisibility(View.VISIBLE);
        lPullToRefreshText.setVisibility(View.VISIBLE);
        lPullToRefreshText.setText(R.string.loading_text);
    }

    private void initView(View headNode) {
        lPullToRefreshText = (TextView) headNode.findViewById(R.id.pullToRefreshText);
        lPullToRefreshImage = (ImageView) headNode.findViewById(R.id.pullToRefreshImage);
        lPullToRefreshProgress = headNode.findViewById(R.id.pullToRefreshProgress);
    }

    @Override
    public void pullDone() {
        nowState = PullToRefreshView.DONE;
    }

    @Override
    public int getState() {
        return nowState;
    }

    @Override
    public void setState(int state) {
        nowState = state;
    }

}
