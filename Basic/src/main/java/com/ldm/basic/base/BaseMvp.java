package com.ldm.basic.base;

import android.content.Context;

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

        /**
         * 设置Presenter
         *
         * @param t T Presenter
         */
        void setPresenter(T t);

        Context getContext();

    }


}
