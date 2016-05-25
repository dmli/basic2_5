package com.ldm.basic.views.pull.anim;

import android.view.View;

/**
 * Created by ldm on 16/4/8.
 * HeadView和loadView的动画接口，提供给LPullToRefresh... 控件使用
 */
public interface BasicPullViewAnimation {

    /**
     * 这里可以做拉动时的动画
     *
     * @param viewNode View
     * @param count    Head总高度
     * @param p        当前高度
     */
    void pullProgress(View viewNode, int count, int p);

    /**
     * 当用户松开开始刷新时这个方法被触发
     *
     * @param viewNode View
     */
    void releaseToRefresh(View viewNode);

    /**
     * 自动移动停止，触发refresh方法后这个方法被触发
     */
    void scrollStop();

    /**
     * 处于下啦刷新的状态
     *
     * @param viewNode View
     */
    void pullToRefresh(View viewNode);

    /**
     * 开始刷新
     *
     * @param viewNode View
     */
    void refresh(View viewNode);

    /**
     * 用户手指操作完成后触发
     */
    void pullDone();

    /**
     * 返回BasicHeadAnimation中当前的状态
     *
     * @return 需要与PullToRefreshView.lState匹配
     */
    int getState();

    /**
     * 设置状态
     *
     * @param state 与PullToRefreshView.lState匹配
     */
    void setState(int state);

}
