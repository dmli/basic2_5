package com.ldm.basic.utils;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by ldm on 12/9/8.
 * 使用者需要实现SecurityHandlerInterface接口
 */
public class BasicSimpleHandler<T extends BasicSimpleHandler.OnSimpleHandlerInterface> extends Handler {

    WeakReference<T> w;

    public BasicSimpleHandler(T t) {
        w = new WeakReference<>(t);
    }

    @Override
    public void handleMessage(Message msg) {
        if (w != null) {
            OnSimpleHandlerInterface t = w.get();
            if (t != null) {
                t.handleMessage(msg);
            }
        }
    }

    public interface OnSimpleHandlerInterface {

        void handleMessage(Message msg);
    }

}
