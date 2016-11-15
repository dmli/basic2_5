package com.ldm.basic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;

import com.ldm.basic.app.Configuration;
import com.ldm.basic.dialog.LToast;
import com.ldm.basic.helper.RightSlidingFinishActivity;
import com.ldm.basic.utils.CPUHelper;
import com.ldm.basic.utils.SystemTool;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Created by ldm on 11-12-10.
 * <p/>
 * BasicActivity中提供了全局的Activity记录、BaseReceiver接收，Activity状态及常用了一些方法
 */
public class BasicActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener {

    /**
     * 软盘高度
     */
    private int softInputHeight;

    private BaseReceiver receiver = null;

    /**
     * --页面状态-- 当页面关闭时自动转为false 可用此变量来控制Activity销毁时残留的线程与Handler间的通讯
     */
    public boolean THIS_ACTIVITY_STATE;

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
    public BasicActivity() {
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkRightSlidingFinishActivity();
        //super
        super.onCreate(savedInstanceState);
    }

    /**
     * 检查是否处于透明背景状态
     */
    protected void checkRightSlidingFinishActivity() {
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
    public void setContentView(View view) {
        if (ignoreRightSlidingFinishActivity && CPUHelper.getNumCores() > 2 && CPUHelper.getCpuMaxFreq() > 1.2f) {
            super.setContentView(new RightSlidingFinishActivity(this, view).build());
        } else {
            super.setContentView(view);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        if (ignoreRightSlidingFinishActivity && CPUHelper.getNumCores() > 2 && CPUHelper.getCpuMaxFreq() > 1.2f) {
            super.setContentView(new RightSlidingFinishActivity(this, layoutResID).build());
        } else {
            super.setContentView(layoutResID);
        }
    }

    @Override
    protected void onDestroy() {
        stopReceiver();
        THIS_ACTIVITY_STATE = false;
        super.onDestroy();
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

    /**
     * Short Toast
     *
     * @param smg 提示语
     */
    public void showShort(final String smg) {
        LToast.showShort(this, smg);
    }

    /**
     * Long Toast
     *
     * @param smg 提示语
     */
    public void showLong(final String smg) {
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
     * 读取AndroidManifest中Activity的meta_data属性
     *
     * @param componentName Activity.getComponentName()
     * @param key           key
     * @return Object
     */
    protected Object getActivityMetaData(ComponentName componentName, String key) {
        Object result = null;
        try {
            ComponentName cn = componentName == null ? this.getComponentName() : componentName;
            ActivityInfo ai = this.getPackageManager().getActivityInfo(cn, PackageManager.GET_META_DATA);
            if (ai != null && ai.metaData != null && ai.metaData.containsKey(key)) {
                result = ai.metaData.get(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 注册广播接收器
     *
     * @param actions 需要绑定的广播地址
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
        if (null != receiver) {
            try {
                unregisterReceiver(receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
}
