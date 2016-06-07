package com.ldm.basic.base;

import android.os.Message;

import com.ldm.basic.utils.BasicSimpleHandler;

/**
 * Created by ldm on 16/5/24.
 * 基础Mvp接口
 */
public interface BaseMvp {

    /**
     * 基础实际接口
     *
     * @param <T>
     */
    interface BaseView<T> {

        void setPresenter(T t);
    }

    /**
     * 数据提供商
     */
    interface BaseProvider {
    }

    /**
     * 主持人/控制器
     */
    abstract class BasePresenter extends OnControllerLifeListener implements BasicSimpleHandler.OnSimpleHandlerInterface {

        /**
         * 所有请求均由BasicActivity中handleMessage(...)接收(这个handler允许在协议中使用)
         */
        public BasicSimpleHandler<BasePresenter> handler = new BasicSimpleHandler<>(this);

        @Override
        public void handleMessage(Message msg) {
        }

    }

}
