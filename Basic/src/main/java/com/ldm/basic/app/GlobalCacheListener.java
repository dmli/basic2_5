package com.ldm.basic.app;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by ldm on 14-6-9.
 * 全局的缓存状态监听接口
 * 使用时需要通过BasicApplication.setGlobalCacheListener(gcl)方法设置，
 * 设置后将会在app内所有activity的onSaveInstanceState及onRestoreInstanceState方法中进行监听
 * 设置该监听后将会在所有的activity中触发接口内的方法
 */
public interface GlobalCacheListener {

    /**
     * activity的onSaveInstanceState方法
     *
     * @param activity Activity
     * @param outState Bundle
     */
    public abstract void onSaveInstanceState(Activity activity, Bundle outState);

    /**
     * activity的onRestoreInstanceState方法
     *
     * @param activity           Activity
     * @param savedInstanceState Bundle
     */
    public abstract void onRestoreInstanceState(Activity activity, Bundle savedInstanceState);
}
