package com.ldm.basic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ldm.basic.dialog.LToast;
import com.ldm.basic.intent.IntentUtil;
import com.ldm.basic.utils.BasicSimpleHandler;
import com.ldm.basic.utils.LLog;

import java.util.Map;

/**
 * Created by ldm on 14-2-21. 基于Fragment增加了一些常用的功能, 用户调用initView(LayoutInflater,
 * ViewGroup, int)后，将可以使用BasicFragment的一些基础属性及方法 如：rootView =（布局）、inflater
 * =（LayoutInflater）、inflate(resource, ViewGroup,
 * attachToRoot)及getView(resource)等方法
 * <p/>
 * *仅限于配合BasicFragmentActivity使用*
 */
public class BasicFragment extends Fragment implements View.OnClickListener, BasicSimpleHandler.OnSimpleHandlerInterface {

    protected View rootView;
    protected LayoutInflater inflater;
    protected BasicFragmentActivity activity;

    private BaseReceiver receiver = null;

    /**
     * --页面状态-- 当页面关闭时自动转为false 可用此变量来控制Activity销毁时残留的线程与Handler间的通讯
     */
    public boolean THIS_FRAGMENT_STATE;

    /**
     * 界面按钮控制器，可以通过设置间隔时间开启点击事件监听
     */
    private long upClickTime;// 上一次点击的时间
    private long clickSleepTime;

    protected BasicSimpleHandler<BasicFragment> handler = new BasicSimpleHandler<>(this);

    /**
     * 返回resource指向的View布局，并初始化相关功能及方法， 建议使用方法： public View
     * onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
     * savedInstanceState) { return initView(LayoutInflater, ViewGroup,
     * R.layout.*); }
     *
     * @param inflater  LayoutInflater
     * @param container ViewGroup根节点
     * @param resource  布局源
     * @return View
     */
    protected View initView(LayoutInflater inflater, ViewGroup container, int resource) {
        this.inflater = inflater;
        return rootView = inflater.inflate(resource, container, false);
    }

    /**
     * 界面布局初始化方法，如果用户在onCreateView中成功为rootView初始化或使用了BasicFragment提供的initView(
     * LayoutInflater, ViewGroup, int)方法后 将可以通过init()方法进行布局初始化，
     * 该方法的好处在于开发者可以不用考虑onCreateView与onActivityCreated的关系和界面隐藏后恢复时activity ==
     * null的问题
     */
    protected void init() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.activity = (BasicFragmentActivity) getActivity();
        THIS_FRAGMENT_STATE = true;
        if (rootView != null) {
            init();
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
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroyView();
    }

    /**
     * 内部调用inflater.inflate(int resource, ViewGroup root, boolean attachToRoot)
     *
     * @param resource     源ID
     * @param root         根节点
     * @param attachToRoot 是否附加到到根
     * @return View 有可能为null
     */
    protected View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        if (inflater == null) {
            LLog.e("inflater 没有初始化，可以考虑在onCreateView中使用initView(LayoutInflater, ViewGroup, int)方法或自行设置inflater");
        }
        return inflater == null ? null : inflater.inflate(resource, root, attachToRoot);
    }

    /**
     * 预留回调方法，这个方法会在FragmentTransaction.hide(Fragment)时调用
     */
    public void hide() {
    }

    /**
     * 预留回调方法,这个方法会被BasicFragmentActivity的onResume调用及FragmentTransaction.show(
     * Fragment)方法调用时触发，与activity的onResume方法类似
     */
    public void show() {
    }

    /**
     * 向父类发送一个指令
     *
     * @param obj 参数
     * @return Object
     */
    public Object sendMessageToSuper(int state, Object obj) {
        if (activity != null) {
            return activity.receiverMessageFromChildren(state, obj);
        }
        return null;
    }

    /**
     * 接收由BasicFragmentActivity发来的消息
     *
     * @param state 状态
     * @param obj   Object
     * @return Object
     */
    public Object receiverMessageFromSuper(int state, Object obj) {

        return null;
    }

    /**
     * 通过Id查询View
     *
     * @param viewId id
     * @return View
     */
    protected View getView(final int viewId) {
        return rootView.findViewById(viewId);
    }

    /**
     * 通过Id给对应的TextView或其所有子类设置text
     *
     * @param viewId id
     * @param str    s
     */
    protected void setText(final int viewId, final String str) {
        TextView tv = (TextView) getView(viewId);
        if (tv != null) {
            tv.setText(str);
        } else {
            LLog.e("没有找到 id" + viewId + "所对应的View");
        }
    }

    /**
     * 通过Id给对应的TextView或其所有子类设置text
     *
     * @param viewId id
     * @param html   s
     */
    protected void setText(final int viewId, final Spanned html) {
        ((TextView) getView(viewId)).setText(html);
        TextView tv = (TextView) getView(viewId);
        if (tv != null) {
            tv.setText(html);
        } else {
            LLog.e("没有找到 id" + viewId + "所对应的View");
        }
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
     * 页面跳转
     *
     * @param classes c
     */
    protected void intent(final Class<?> classes) {
        IntentUtil.intentDIY(activity, classes);
    }

    /**
     * 页面跳转
     *
     * @param classes   目标
     * @param enterAnim 进入动画文件ID
     * @param exitAnim  退出动画文件ID
     */
    protected void intent(final Class<?> classes, final int enterAnim, final int exitAnim) {
        IntentUtil.intentDIY(activity, classes, enterAnim, exitAnim);
    }

    /**
     * 页面跳转
     *
     * @param classes 目标
     * @param map     参数
     */
    protected void intent(final Class<?> classes, final Map<String, Object> map) {
        IntentUtil.intentDIY(activity, classes, map);
    }

    /**
     * 页面跳转
     *
     * @param classes   目标
     * @param map       参数
     * @param enterAnim 进入动画文件ID
     * @param exitAnim  退出动画文件ID
     */
    protected void intent(final Class<?> classes, final Map<String, Object> map, final int enterAnim, final int exitAnim) {
        IntentUtil.intentDIY(activity, classes, map, enterAnim, exitAnim);
    }

    /**
     * 查询 VIEW 并执行页面跳转
     *
     * @param id      ViewId
     * @param classes 目标
     */
    protected void intent(final int id, final Class<?> classes) {
        intent(id, classes, null, IntentUtil.INTENT_DEFAULT_ENTER_ANIM, IntentUtil.INTENT_DEFAULT_EXIT_ANIM);
    }

    /**
     * 查询 VIEW 并执行页面跳转
     *
     * @param id        ViewId
     * @param classes   目标
     * @param enterAnim 进入动画文件ID
     * @param exitAnim  退出动画文件ID
     */
    protected void intent(final int id, final Class<?> classes, final int enterAnim, final int exitAnim) {
        intent(id, classes, null, enterAnim, exitAnim);
    }

    /**
     * 查询 VIEW 并执行页面跳转
     *
     * @param id      ViewId
     * @param classes 目标
     * @param map     参数
     */
    protected void intent(final int id, final Class<?> classes, final Map<String, Object> map) {
        intent(id, classes, map, IntentUtil.INTENT_DEFAULT_ENTER_ANIM, IntentUtil.INTENT_DEFAULT_EXIT_ANIM);
    }

    /**
     * 查询 VIEW 并执行页面跳转
     *
     * @param id        ViewId
     * @param classes   目标
     * @param map       参数
     * @param enterAnim 进入动画文件ID
     * @param exitAnim  退出动画文件ID
     */
    protected void intent(final int id, final Class<?> classes, final Map<String, Object> map, final int enterAnim, final int exitAnim) {
        View v = getView(id);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtil.intentDIY(activity, classes, map, enterAnim, exitAnim);
            }
        });
    }

    /**
     * 动画形式关闭页面
     */
    public void finishAnim() {
        IntentUtil.finishDIY(activity);
    }

    /**
     * 动画形式关闭页面
     *
     * @param enterAnim 进入动画
     * @param exitAnim  退出动画
     */
    public void finishAnim(final int enterAnim, final int exitAnim) {
        IntentUtil.finishDIY(activity, enterAnim, exitAnim);
    }

    /**
     * 查询并添加OnClickListener事件 如果View不存在则抛出NullPointerException
     *
     * @param id ViewId
     * @return View
     */
    protected View setOnClickListener(final int id) {
        View v = getView(id);
        v.setOnClickListener(this);
        return v;
    }

    /**
     * 启动接收器
     *
     * @param actions 动作
     */
    protected void startReceiver(String... actions) {
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
        if (null != receiver && activity != null)
            this.activity.unregisterReceiver(receiver);
    }

    /**
     * 消息响应方法 当Activity需要响应Broadcast时使用
     */
    protected synchronized void receiver(Context context, Intent intent) {

    }

    @Override
    public void onClick(View v) {
        boolean bool = true;
        if (clickSleepTime > 0) {
            if (System.currentTimeMillis() - upClickTime < clickSleepTime) {
                bool = false;// 阻止这次点击事件的发生
            }
        }
        if (bool) {
            onViewClick(v);
        }
        upClickTime = System.currentTimeMillis();
    }

    /**
     * View.OnClickListener事件回调
     *
     * @param v View
     */
    protected void onViewClick(View v) {
    }

    /**
     * 开启点击事件睡眠时间，设置时间后将无法通过BasicFragment的onViewClick方法进行多次点击，直到超过设置的睡眠时间为止
     *
     * @param time 毫秒
     */
    protected void setClickSleepTime(int time) {
        this.clickSleepTime = time;
    }

    @Override
    public void handleMessage(Message msg) {

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
