package com.ldm.basic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;

import com.ldm.basic.app.Configuration;
import com.ldm.basic.dialog.LToast;
import com.ldm.basic.utils.SystemTool;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Created by ldm on 13-11-10.
 * <p/>
 * BasicFragmentActivity中提供了全局的Activity记录、BaseReceiver接收 ，
 * <p/>
 * FragmentActivity状态及常用了一些方法
 */
public abstract class BasicFragmentActivity extends FragmentActivity implements ViewTreeObserver.OnGlobalLayoutListener {

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
     * 软盘高度
     */
    private int softInputHeight;

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
        init();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkRightSlidingFinishActivity();
        //super
        super.onCreate(savedInstanceState);

        fragments = getFragments();
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

    private void init() {
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
     * 使用BasicFragmentActivity需要实现这个方法,将创建的Fragment数组返回
     *
     * @return BasicFragment[]
     */
    protected abstract BasicFragment[] getFragments();

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

    @Override
    public void finish() {
        /**
         * 界面结束时，清除掉记录中对应的KEY
         */
        SystemTool.removeActivity(getActivityKey());
        super.finish();
    }

    /**
     * 切换界面
     *
     * @param oldPosition 旧位置 第一次这里应该时-1
     * @param newPosition 新位置
     * @param isAnim      是否使用预设动画
     */
    protected void switchFragment(int oldPosition, int newPosition, boolean isAnim) {
        if (oldPosition == newPosition) return;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // 切换前检查是否有预制动画
        onSwitchFragmentAnim(oldPosition, newPosition, isAnim, ft);
        BasicFragment newFragment = (BasicFragment) getSupportFragmentManager().findFragmentByTag(String.valueOf(newPosition));
        BasicFragment oldFragment = (BasicFragment) getSupportFragmentManager().findFragmentByTag(String.valueOf(oldPosition));
        if (newFragment != null) {
            newFragment.setMenuVisibility(true);
            newFragment.setUserVisibleHint(true);
            ft.show(newFragment);
        } else {
            newFragment = fragments[newPosition];
            ft.add(getResources().getIdentifier("container", "id", getPackageName()), newFragment, String.valueOf(newPosition));
        }
        if (oldFragment != null) {
            ft.hide(oldFragment);
        }
        // 切换Fragment后触发switchFragmentAfter方法
        onSwitchFragmentAfter(ft, oldFragment, newFragment);
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
    protected void onSwitchFragmentAnim(int oldPosition, int newPosition, boolean isAnim, FragmentTransaction ft) {
    }

    /**
     * 开发者可以重写这个方法，实现对切换Fragment完成后的监听
     *
     * @param ft          FragmentTransaction
     * @param oldFragment 旧的 Fragment
     * @param newFragment 新的Fragment
     */
    protected void onSwitchFragmentAfter(FragmentTransaction ft, BasicFragment oldFragment, BasicFragment newFragment) {
    }

    /**
     * 启动接收器
     */
    private void registerReceiver(String[] actions) {
        IntentFilter localIntentFilter = new IntentFilter();
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
        stopReceiver();
        THIS_ACTIVITY_STATE = false;
        super.onDestroy();
    }

    /**
     * 消息响应方法 当Activity需要响应Broadcast时使用
     */
    protected synchronized void receiver(Context context, Intent intent) {
    }

    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        View rootNode = this.getWindow().getDecorView();
        rootNode.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int state, sih = Math.min(rootNode.getRootView().getHeight(), dm.heightPixels) - r.bottom;
        if (sih <= 240) {
            state = Configuration.SOFT_INPUT_STATE_CLOSE;
        } else {
            state = Configuration.SOFT_INPUT_STATE_OPEN;
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

    /**
     * 返回键盘高度
     *
     * @return px
     */
    protected int getSoftInputHeight() {
        return softInputHeight;
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
