package com.ldm.basic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;

import com.ldm.basic.dialog.LToast;
import com.ldm.basic.intent.IntentUtil;
import com.ldm.basic.utils.SystemTool;
import com.ldm.basic.utils.image.LazyImageDownloader;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ldm on 13-11-10. BasicFragmentActivity中提供了全局的Activity记录、BaseReceiver接收 ，FragmentActivity状态及常用了一些方法
 */
public class BasicFragmentActivity extends FragmentActivity implements OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    private String[] ACTION = null;

    private BaseReceiver receiver = null;

    /**
     * --页面状态-- 当页面关闭时自动转为false 可用此变量来控制Activity销毁时残留的线程与Handler间的通讯
     */
    public boolean THIS_ACTIVITY_STATE;

    /**
     * 窗口集合
     */
    protected BasicFragment[] fragments;

    /**
     * 当前位置
     */
    protected int currentPosition;

    /**
     * 界面按钮控制器，可以通过设置间隔时间开启点击事件监听
     */
    private long upClickTime;// 上一次点击的时间
    private long clickSleepTime;

    /**
     * true将开启对软盘的监听
     */
    private boolean isSoftInputStateListener;

    /**
     * 软键盘关闭
     */
    public static final int SOFT_INPUT_STATE_CLOSE = 0;

    /**
     * 软键盘开启
     */
    public static final int SOFT_INPUT_STATE_OPEN = 1;

    /**
     * 软盘高度
     */
    protected int softInputHeight;

    /**
     * 这里保存了这个activity生成时所带的uuid值
     */
    private String THIS_ACTIVITY_KEY;

    /**
     * 这个标识可以在调用setContentView(View)时 是否使用 『右滑 finish Activity』
     */
    protected boolean ignoreRightSlidingFinishActivity = false;

    /**
     * 忽略右滑finish Activity功能
     * 注：这个方法需要在super.onCreate(...)方法之前调用
     */
    public void ignoreRightSlidingFinishActivity() {
        this.ignoreRightSlidingFinishActivity = true;
    }

    /**
     * 注册每一个启动的Activity, 用于退出时结束程序
     */
    public BasicFragmentActivity() {
        init(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkRightSlidingFinishActivity();
        //super
        super.onCreate(savedInstanceState);

        /** 不重复注册receiver **/
        if (ACTION != null && ACTION.length > 0 && null == receiver) {
            startReceiver();
        }

        getSupportFragmentManager().getFragments();
    }

    /**
     * 检查是否处于透明背景状态
     */
    private void checkRightSlidingFinishActivity() {
        /**
         * 解析是否可以使用右滑 finish activity功能
         */
        if (!this.ignoreRightSlidingFinishActivity) {
            String windowIsTranslucent, windowBackground;
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.windowIsTranslucent, outValue, true);
            windowIsTranslucent = String.valueOf(outValue.coerceToString());
            getTheme().resolveAttribute(android.R.attr.windowBackground, outValue, true);
            windowBackground = String.valueOf(outValue.coerceToString());
            this.ignoreRightSlidingFinishActivity = !("true".equals(windowIsTranslucent) && "#0".equals(windowBackground));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 返回按钮添加事件，如果存在
        View v = findViewById(getResources().getIdentifier("back", "id", getPackageName()));
        if (v != null) {
            v.setOnClickListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fragments != null && currentPosition >= 0 && currentPosition < fragments.length) {
            BasicFragment f = fragments[currentPosition];
        }
    }

    /**
     * 在注册Activity的基础上增加本Activity的消息响应能力, 使用receiver()响应消息
     */
    public BasicFragmentActivity(String... action) {
        init(action);
    }

    private void init(String[] action) {
        ACTION = action;
        THIS_ACTIVITY_STATE = true;
        SystemTool.activitySet.put(getActivityKey(), new WeakReference<Activity>(this));
    }

    /**
     * 返回这个activity在ACTIVITY_SET中对应的KEY
     *
     * @return THIS_ACTIVITY_KEY
     */
    public String getActivityKey() {
        if (THIS_ACTIVITY_KEY == null) {
            THIS_ACTIVITY_KEY = getClass().getName() + UUID.randomUUID().toString();
        }
        return THIS_ACTIVITY_KEY;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    /**
     * 设置所需的所有窗口
     *
     * @param fragments BasicFragment[]
     */
    public void setFragments(BasicFragment[] fragments) {
        this.fragments = fragments;
    }

    /**
     * 向BasicFragment消息
     *
     * @param position BasicFragment的位置
     * @param obj      参数
     * @return Object
     */
    public Object sendMessageToChildren(int position, int state, Object obj) {
        if (fragments != null && position >= 0 && position < fragments.length) {
            BasicFragment f = fragments[currentPosition];
            if (f != null && f.THIS_FRAGMENT_STATE) {
                return f.receiverMessageFromSuper(state, obj);
            }
        }
        return null;
    }

    /**
     * 向BasicFragment消息
     *
     * @param bf  需要接收消息的<? extends BasicFragment>
     * @param obj 参数
     * @return Object
     */
    public <T extends BasicFragment> Object sendMessageToChildren(T bf, int state, Object obj) {
        if (bf != null && bf.THIS_FRAGMENT_STATE) {
            return bf.receiverMessageFromSuper(state, obj);
        }
        return null;
    }

    /**
     * 负责接收子类的指令
     *
     * @param obj 参数
     * @return Object
     */
    public Object receiverMessageFromChildren(int state, Object obj) {
        return null;
    }

    /**
     * 通过Id查询View
     *
     * @param viewId id
     * @return View
     */
    protected View getView(final int viewId) {
        return findViewById(viewId);
    }

    /**
     * Short Toast
     *
     * @param smg 提示语
     */
    protected void showShort(final String smg) {
        LToast.showShort(this, smg);
    }

    /**
     * Long Toast
     *
     * @param smg 提示语
     */
    protected void showLong(final String smg) {
        LToast.showLong(this, smg);
    }

    /**
     * 页面跳转
     *
     * @param classes c
     */
    protected void intent(final Class<?> classes) {
        IntentUtil.intentDIY(this, classes);
    }

    /**
     * 页面跳转
     *
     * @param classes   目标
     * @param enterAnim 进入动画文件ID
     * @param exitAnim  退出动画文件ID
     */
    protected void intent(final Class<?> classes, final int enterAnim, final int exitAnim) {
        IntentUtil.intentDIY(this, classes, enterAnim, exitAnim);
    }

    /**
     * 页面跳转
     *
     * @param classes 目标
     * @param map     参数
     */
    protected void intent(final Class<?> classes, final Map<String, Object> map) {
        IntentUtil.intentDIY(this, classes, map);
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
        IntentUtil.intentDIY(this, classes, map, enterAnim, exitAnim);
    }

    /**
     * 动画形式关闭页面
     */
    public void finishAnim() {
        if (getDownloader() != null) {
            getDownloader().stopAllTask();
        }
        IntentUtil.finishDIY(this);
    }

    @Override
    public void finish() {
        /**
         * 界面结束时，清除掉记录中对应的KEY
         */
        SystemTool.removeActivity(getActivityKey());
        super.finish();
    }

    public LazyImageDownloader getDownloader() {
        return null;
    }

    /**
     * 动画形式关闭页面
     *
     * @param enterAnim 进入动画
     * @param exitAnim  退出动画
     */
    public void finishAnim(final int enterAnim, final int exitAnim) {
        IntentUtil.finishDIY(this, enterAnim, exitAnim);
    }

    /**
     * 切换界面
     *
     * @param oldPosition 旧位置 第一次这里应该时-1
     * @param newPosition 新位置
     * @param isAnim      是否使用预设动画
     */
    protected void switchFragment(int oldPosition, int newPosition, boolean isAnim) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // 切换前检查是否有预制动画
        switchFragmentAnim(oldPosition, newPosition, isAnim, ft);
        BasicFragment newFragment = (BasicFragment) getSupportFragmentManager().findFragmentByTag(newPosition + "");
        BasicFragment oldFragment = (BasicFragment) getSupportFragmentManager().findFragmentByTag(oldPosition + "");
        if (newFragment != null) {
            newFragment.setMenuVisibility(true);
            newFragment.setUserVisibleHint(true);
            ft.show(newFragment);
        } else {
            newFragment = fragments[newPosition];
            ft.add(getResources().getIdentifier("container", "id", getPackageName()), newFragment, newPosition + "");
        }
        if (oldFragment != null) {
            ft.hide(oldFragment);
        }
        // 切换Fragment后触发switchFragmentAfter方法
        switchFragmentAfter(ft, oldFragment, newFragment);
        ft.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        currentPosition = newPosition;
    }

    /**
     * 开发者可以重写这个方法为切换Fragment设置动画
     *
     * @param oldPosition 旧的Fragment所在位置
     * @param newPosition 新的Fragment所在位置
     * @param isAnim      switchFragment时的isAnim参数
     * @param ft          FragmentTransaction
     */
    protected void switchFragmentAnim(int oldPosition, int newPosition, boolean isAnim, FragmentTransaction ft) {
    }

    /**
     * 开发者可以重写这个方法，实现对切换Fragment完成后的监听
     *
     * @param ft          FragmentTransaction
     * @param oldFragment 旧的 Fragment
     * @param newFragment 新的Fragment
     */
    protected void switchFragmentAfter(FragmentTransaction ft, BasicFragment oldFragment, BasicFragment newFragment) {
    }

    /**
     * 启动接收器
     */
    private void startReceiver() {
        IntentFilter localIntentFilter = new IntentFilter();
        final String[] actions = ACTION;
        for (String action : actions) {
            localIntentFilter.addAction(action);
        }
        receiver = new BaseReceiver();
        this.registerReceiver(receiver, localIntentFilter);
    }

    /**
     * 关闭接收器
     */
    private void stopReceiver() {
        if (null != receiver)
            unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        //移除OnGlobalLayoutListener事件
        removeOnGlobalLayoutListener();
        stopReceiver();
        THIS_ACTIVITY_STATE = false;
        super.onDestroy();
    }

    /**
     * 移除OnGlobalLayoutListener事件
     */
    private void removeOnGlobalLayoutListener() {
        if (isSoftInputStateListener) {
            // 注销对ViewTreeObserver的监听
            if (getWindow().getDecorView().getViewTreeObserver().isAlive()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        }
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
        if (v.getId() == getResources().getIdentifier("back", "id", getPackageName())) {
            finishAnim();
        }
    }

    /**
     * 开启点击事件睡眠时间，设置时间后将无法通过BasicFragmentActivity的onViewClick方法进行多次点击， 直到超过设置的睡眠时间为止
     *
     * @param time 毫秒
     */
    protected void setClickSleepTime(int time) {
        this.clickSleepTime = time;
    }

    /**
     * 设置true后将开启对软键盘的监听，通过onSoftInputStateChange(state)方法触发 当activity的windowSoftInputMode属性设置为adjustNothing时，这个方法将无效
     *
     * @param isSoftInputStateListener true开启 默认false
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void setSoftInputStateListener(boolean isSoftInputStateListener) {
        this.isSoftInputStateListener = isSoftInputStateListener;
        if (isSoftInputStateListener) {
            getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(this);
        } else {
            removeOnGlobalLayoutListener();
        }
    }

    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        View rootNode = this.getWindow().getDecorView();
        rootNode.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int state, sih = Math.min(rootNode.getRootView().getHeight(), dm.heightPixels) - r.bottom;
        if (sih <= 200) {
            state = SOFT_INPUT_STATE_CLOSE;
        } else {
            state = SOFT_INPUT_STATE_OPEN;
            softInputHeight = sih;
        }
        onSoftInputState(state);
    }

    /**
     * 软键盘状态监听
     *
     * @param state 取值范围SOFT_INPUT_STATE_CLOSE 、 SOFT_INPUT_STATE_OPEN
     */
    protected void onSoftInputState(int state) {

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
