package com.ldm.basic.views.pull.anim;

import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.ldm.basic.R;
import com.ldm.basic.views.pull.LLeftPullToRefreshView;

/**
 * Created by ldm on 16/4/28.
 * 左滑加载更多控件
 */
public class SimpleLeftPullAnimation implements BasicPullViewAnimation {

    private int nowState;
    private TextView lPullToRefreshText;
    private ImageView lPullToRefreshImage;

    // 刷新箭头的翻转动画
    private final RotateAnimation lAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
    // 刷新箭头的翻转动画
    private final RotateAnimation lReverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);

    public SimpleLeftPullAnimation() {
        nowState = LLeftPullToRefreshView.DONE;
        // ----------------------------------------
        lAnimation.setInterpolator(new LinearInterpolator());
        lAnimation.setDuration(150);
        lAnimation.setFillAfter(true);
        // ----------------------------------------
        lReverseAnimation.setInterpolator(new LinearInterpolator());
        lReverseAnimation.setDuration(150);
        lReverseAnimation.setFillAfter(true);
        // ----------------------------------------
    }

    /**
     * 这里可以做拉动时的动画
     *
     * @param viewNode View
     * @param count    Head总高度
     * @param p        当前高度
     */
    @Override
    public void pullProgress(View viewNode, int count, int p) {
        if (lPullToRefreshText == null) {
            initView(viewNode);
        }
//        ViewHelper.setRotation(lPullToRefreshImage, -p * 2);
    }

    /**
     * 当用户松开开始刷新时这个方法被触发
     *
     * @param viewNode View
     */
    @Override
    public void releaseToRefresh(View viewNode) {
        if (lPullToRefreshText == null) {
            initView(viewNode);
        }
        lPullToRefreshImage.startAnimation(lAnimation);
        lPullToRefreshImage.setVisibility(View.VISIBLE);
        lPullToRefreshText.setVisibility(View.VISIBLE);
        lPullToRefreshText.setText(R.string.left_release_refresh_text);
    }

    /**
     * 自动移动停止，触发refresh方法后这个方法被触发
     */
    @Override
    public void scrollStop() {
        if (lPullToRefreshText != null) {
            if (lPullToRefreshImage.getAnimation() != null) {
                lPullToRefreshImage.clearAnimation();
            }
            lPullToRefreshText.setVisibility(View.INVISIBLE);
            lPullToRefreshImage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void pullToRefresh(View headNode) {
        if (lPullToRefreshText == null) {
            initView(headNode);
        }
        lPullToRefreshText.setVisibility(View.VISIBLE);
        lPullToRefreshText.setText(R.string.left_pull_refresh_text);
        if (lPullToRefreshImage.getAnimation() != null) {
            lPullToRefreshImage.clearAnimation();
        }
        lPullToRefreshImage.startAnimation(lReverseAnimation);
        lPullToRefreshImage.setVisibility(View.VISIBLE);
    }

    private void initView(View headNode) {
        lPullToRefreshText = (TextView) headNode.findViewById(R.id.pullToRefreshText);
        lPullToRefreshImage = (ImageView) headNode.findViewById(R.id.pullToRefreshImage);
    }


    /**
     * 开始刷新
     *
     * @param viewNode View
     */
    @Override
    public void refresh(View viewNode) {

    }

    @Override
    public void pullDone() {
        nowState = LLeftPullToRefreshView.DONE;
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
