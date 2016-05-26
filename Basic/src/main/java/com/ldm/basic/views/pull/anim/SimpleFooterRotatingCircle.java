package com.ldm.basic.views.pull.anim;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ldm.basic.R;
import com.ldm.basic.views.pull.LPullToRefreshView;
import com.ldm.basic.views.pull.LPullToRefreshViewLite;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by ldm on 12/11/14.
 * 简单的上拉刷新动画处理《圆环旋转》
 */
public class SimpleFooterRotatingCircle implements BasicPullViewAnimation {

    private int nowState;
    private TextView lPullToRefreshText;
    private ImageView lPullToRefreshImage;
    private View lPullToRefreshProgress;

    public SimpleFooterRotatingCircle() {
        nowState = LPullToRefreshView.DONE;
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
        lPullToRefreshText.setText(R.string.header_unlock_to_refresh_text);
    }

    @Override
    public void scrollStop() {
        if (lPullToRefreshText != null) {
            lPullToRefreshText.setVisibility(View.INVISIBLE);
            lPullToRefreshProgress.setVisibility(View.INVISIBLE);
            lPullToRefreshImage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void pullToRefresh(View headNode) {
        if (lPullToRefreshText == null) {
            initView(headNode);
        }
        lPullToRefreshText.setVisibility(View.VISIBLE);
        lPullToRefreshText.setText(R.string.footer_up_push_to_loading_text);
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
        lPullToRefreshText.setText(R.string.footer_being_loading_text);
    }

    private void initView(View headNode) {
        lPullToRefreshText = (TextView) headNode.findViewById(R.id.footerPullToRefreshText);
        lPullToRefreshImage = (ImageView) headNode.findViewById(R.id.footerPullToRefreshImage);
        lPullToRefreshProgress = headNode.findViewById(R.id.footerPullToRefreshProgress);
    }

    @Override
    public void pullDone() {
        nowState = LPullToRefreshViewLite.DONE;
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
