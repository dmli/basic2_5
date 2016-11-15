package com.ldm.basic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ldm.basic.dialog.LToast;

/**
 * Created by ldm on 14-2-21.
 * 基于Fragment增加了一些常用的功能, 用户调用initView(LayoutInflater, ViewGroup, int)
 */
public abstract class BasicFragment extends Fragment {

    protected View rootView;
    protected LayoutInflater inflater;
    protected BasicFragmentActivity activity;

    private BaseReceiver receiver = null;

    /**
     * --页面状态-- 当页面关闭时自动转为false 可用此变量来控制Activity销毁时残留的线程与Handler间的通讯
     */
    public boolean THIS_FRAGMENT_STATE;

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p/>
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        return rootView = inflater.inflate(getContentViewId(), container, false);
    }


    /**
     * 返回用来创建ContentView布局的ViewId
     *
     * @return R.layout.***
     */
    protected abstract int getContentViewId();

    /**
     * 使用buildContentView()方法可以不用考虑onCreateView与onActivityCreated的关系和界面隐藏后恢复时activity == null的问题
     */
    protected abstract void buildContentView();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.activity = (BasicFragmentActivity) getActivity();
        THIS_FRAGMENT_STATE = true;
        if (rootView != null) {
            buildContentView();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        stopReceiver();
        THIS_FRAGMENT_STATE = false;
        super.onDestroyView();
    }

    /**
     * Short Toast
     *
     * @param smg 提示语
     */
    protected void showShort(final String smg) {
        LToast.showShort(getContext(), smg);
    }

    /**
     * Long Toast
     *
     * @param smg 提示语
     */
    protected void showLong(final String smg) {
        LToast.showLong(getContext(), smg);
    }

    /**
     * 对应findViewById方法
     *
     * @param viewId View Id
     * @return View
     */
    protected View getView(int viewId) {
        return rootView.findViewById(viewId);
    }

    /**
     * 启动接收器
     *
     * @param actions 动作
     */
    protected void registerReceiver(String... actions) {
        IntentFilter localIntentFilter = new IntentFilter();
        if (actions != null && actions.length > 0) {
            for (String a : actions) {
                localIntentFilter.addAction(a);
            }
            receiver = new BaseReceiver();
            this.activity.registerReceiver(receiver, localIntentFilter);
        }
    }

    /**
     * 关闭接收器
     */
    private void stopReceiver() {
        if (null != receiver && activity != null){
            this.activity.unregisterReceiver(receiver);
        }
    }

    /**
     * 消息响应方法 当Activity需要响应Broadcast时使用
     */
    protected synchronized void receiver(Context context, Intent intent) {

    }

    public class BaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                receiver(context, intent);
            }
        }
    }

}
