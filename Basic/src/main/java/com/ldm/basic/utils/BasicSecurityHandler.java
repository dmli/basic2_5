package com.ldm.basic.utils;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

/**
 * Created by ldm on 12/9/8.
 * 相对安全的Handler，使用者需要实现SecurityHandlerInterface接口
 */
public class BasicSecurityHandler<T extends BasicSecurityHandler.SecurityHandlerInterface> extends Handler {

    WeakReference<T> w;

    public BasicSecurityHandler(T t) {
        w = new WeakReference<>(t);
    }

    @Override
    public void handleMessage(Message msg) {
        if (w != null) {
            SecurityHandlerInterface t = w.get();
            if (t != null) {
                t.handleMessage(msg.what, msg.obj);
            }
        }
    }

    public interface SecurityHandlerInterface {

        void handleMessage(int what, Object obj);
    }

}
