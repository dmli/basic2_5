package com.ldm.basic.base;

/**
 * Created by ldm on 16/6/7.
 * 控制器的生命周期接口
 */
public class OnControllerLifeListener {

    public boolean isFirst = true;

    /**
     * Activity/Fragment onStart()时被触发,
     * 界面第一次被初始化时onStart(first)中的first参数为true
     *
     * @param first true/false
     */
    public void onStart(boolean first) {
        isFirst = false;
    }

    /**
     * Activity/Fragment onResume()时被触发
     */
    public void onResume() {
    }

    /**
     * Activity/Fragment onDestroy()时被触发
     */
    public void onDestroy() {
    }

}
