package com.ldm.basic;

import java.util.TimerTask;

/**
 * Created by ldm on 14-6-23.
 * BasicActivity/BasicFragmentActivity/BasicFragment中将使用它来开启定时器
 */
public abstract class BasicTimerTask extends TimerTask {

    //定时器的唯一标识
    private String tag;

    protected BasicTimerTask(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
