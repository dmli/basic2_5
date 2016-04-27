package com.ldm.basic.utils;

import android.view.View;

/**
 * Created by ldm on 16/4/21.
 * 点击事件接口，这个View.OnClickListener可以设置一个时间戳，阻隔按钮点击事件的间隔时间
 */
public abstract class BasicOnClickListener implements View.OnClickListener {

    /**
     * 界面按钮控制器，可以通过设置间隔时间开启点击事件监听
     */
    private long upClickTime;// 上一次点击的时间
    private long clickSleepTime;//毫秒

    public BasicOnClickListener() {
        /**
         * 默认点击延时时间500毫秒
         */
        this(500);
    }

    public BasicOnClickListener(long clickSleepTime) {
        this.clickSleepTime = clickSleepTime;
    }

    public void setClickSleepTime(long clickSleepTime) {
        this.clickSleepTime = clickSleepTime;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        // 两次点击时间间隔 < clickSleepTime时放弃本次点击
        if (clickSleepTime <= 0 || System.currentTimeMillis() - upClickTime > clickSleepTime) {
            onViewClick(v);
            upClickTime = System.currentTimeMillis();
        }
    }

    /**
     * View.OnClickListener事件回调，用来代替onClick(View v)
     *
     * @param v View
     */
    public abstract void onViewClick(View v);
}
