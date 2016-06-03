package com.ldm.basic.views.pull.listener;

/**
 * Created by ldm on 16/4/7.
 * 提供支持Touch操作的滑动状态
 */
public interface OnPullAbsScrollListener {

    /**
     * 当前控件是否在最底部
     *
     * @return true/false
     */
    boolean isBottom();

    /**
     * 当前控件是否在最顶部
     *
     * @return true/false
     */
    boolean isTop();

}
