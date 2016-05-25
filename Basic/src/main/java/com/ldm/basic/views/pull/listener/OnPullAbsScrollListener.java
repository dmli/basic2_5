package com.ldm.basic.views.pull.listener;

/**
 * Created by ldm on 16/4/7.
 * 提供支持Touch操作的滑动状态
 */
public interface OnPullAbsScrollListener {

    /**
     * 是否可以向下滑动（手指向下滑动）
     *
     * @return true/false
     */
    boolean isMoveDown();

    /**
     * 是否可以向上滑动（手指向上滑动）
     *
     * @return true/false
     */
    boolean isMoveUp();

}
