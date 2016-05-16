package com.ldm.basic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;

import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.app.Configuration;
import com.ldm.basic.dialog.LToast;
import com.ldm.basic.helper.RightSlidingFinishActivity;
import com.ldm.basic.intent.IntentUtil;
import com.ldm.basic.utils.CPUHelper;
import com.ldm.basic.utils.Log;
import com.ldm.basic.utils.SystemTool;
import com.ldm.basic.utils.image.LazyImageDownloader;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ldm on 11-12-10.
 * BasicActivity中提供了全局的Activity记录、BaseReceiver接收，Activity状态及常用了一些方法
 */
public class BasicActivity extends Activity implements OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    /**
     * 发送一个short toast的标记
     */
    public static final int POST_SHOW_SHORT = 900089;

    /**
     * 发送一个long toast的标记
     */
    public static final int POST_SHOW_LONG = 900090;

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

    private String[] ACTION = null;//

    private BaseReceiver receiver = null;

    /**
     * --页面状态-- 当页面关闭时自动转为false 可用此变量来控制Activity销毁时残留的线程与Handler间的通讯
     */
    public boolean THIS_ACTIVITY_STATE;

    /**
     * true将开启对软盘的监听
     */
    private boolean isSoftInputStateListener;

    /**
     * 简易的异步线程接口，为了脱离asynchronous内部方法影响finish()而设计的
     */
    private static Map<String, Asynchronous> ASYNC_SET;

    /**
     * 界面按钮控制器，可以通过设置间隔时间开启点击事件监听
     */
    private long upClickTime;// 上一次点击的时间
    private long clickSleepTime;

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

    /**
     * 创建一个支持右滑返回的Activity
     * （这个方法将会根据手机的CPU来计算是否开启右滑销毁Activity的功能）
     *
     * @param v View
     */
    public void setContentViewToSlidingFinish(View v) {
        if (CPUHelper.getNumCores() <= 2 && CPUHelper.getCpuMaxFreq() <= 1.2) {
            super.setContentView(v);
        } else {
            super.setContentView(new RightSlidingFinishActivity(this).createContentView(v));
        }
    }

    /**
     * 创建一个支持右滑返回的Activity
     * （这个方法将会根据手机的CPU来计算是否开启右滑销毁Activity的功能）
     *
     * @param layoutResID 布局资源ID
     */
    public void setContentViewToSlidingFinish(int layoutResID) {
        if (CPUHelper.getNumCores() <= 2 && CPUHelper.getCpuMaxFreq() <= 1.2) {
            super.setContentView(layoutResID);
        } else {
            super.setContentView(new RightSlidingFinishActivity(this).createContentView(layoutResID));
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

    /**
     * 在注册Activity的基础上增加本Activity的消息响应能力, 使用receiver()响应消息
     */
    public BasicActivity(String... action) {
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
    public void showShort(final String smg) {
        securityHandler.sendMessage(securityHandler.obtainMessage(POST_SHOW_SHORT, smg));
    }

    /**
     * Long Toast
     *
     * @param smg 提示语
     */
    public void showLong(final String smg) {
        securityHandler.sendMessage(securityHandler.obtainMessage(POST_SHOW_LONG, smg));
    }

    /**
     * 页面跳转
     *
     * @param classes c
     */
    public void intent(final Class<?> classes) {
        IntentUtil.intentDIY(this, classes);
    }

    /**
     * 页面跳转
     *
     * @param classes   目标
     * @param enterAnim 进入动画文件ID
     * @param exitAnim  退出动画文件ID
     */
    public void intent(final Class<?> classes, final int enterAnim, final int exitAnim) {
        IntentUtil.intentDIY(this, classes, enterAnim, exitAnim);
    }

    /**
     * 页面跳转
     *
     * @param classes 目标
     * @param map     参数
     */
    public void intent(final Class<?> classes, final Map<String, Object> map) {
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
    public void intent(final Class<?> classes, final Map<String, Object> map, final int enterAnim, final int exitAnim) {
        IntentUtil.intentDIY(BasicActivity.this, classes, map, enterAnim, exitAnim);
    }

    /**
     * 动画形式关闭页面
     */
    public void finishAnim() {
        IntentUtil.finishDIY(this);
    }

    public LazyImageDownloader getDownloader() {
        return null;
    }

    @Override
    public void finish() {
        if (getDownloader() != null) {
            getDownloader().stopAllTask();
        }
        /**
         * 界面结束时，清除掉记录中对应的KEY
         */
        SystemTool.removeActivity(getActivityKey());
        super.finish();
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
    protected void onSaveInstanceState(Bundle outState) {
        BasicApplication.getBundle(outState);
        if (BasicApplication.globalCacheListener == null) {
            BasicApplication.initGlobalCacheListener(this);// 尝试初始化GlobalCacheListener
        }
        if (BasicApplication.globalCacheListener != null) {
            BasicApplication.globalCacheListener.onSaveInstanceState(this, outState);
        } else {
            Log.e("GlobalCacheListener is null");
        }
        if (outState != null) {
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (BasicApplication.globalCacheListener == null) {
            BasicApplication.initGlobalCacheListener(this);// 尝试初始化GlobalCacheListener
        }
        if (BasicApplication.globalCacheListener != null) {
            BasicApplication.globalCacheListener.onRestoreInstanceState(this, savedInstanceState);
        }
        if (savedInstanceState != null) {
            BasicApplication.setClientCache(savedInstanceState.getSerializable(Configuration.CLIENT_CACHE_KEY));
        }
    }

    @Override
    protected void onUserLeaveHint() {
        if (BasicApplication.globalCacheListener != null) {
            BasicApplication.globalCacheListener.onUserLeaveHint();
        }
        super.onUserLeaveHint();
    }

    @Override
    protected void onDestroy() {
        //移除OnGlobalLayoutListener事件
        removeOnGlobalLayoutListener();
        // 如果设置了异步任务，这里需要清除
        if (ASYNC_SET != null) {
            ASYNC_SET.remove(((Object) this).getClass().getName());
        }
        stopReceiver();
        THIS_ACTIVITY_STATE = false;
        if (securityHandler != null) {
            securityHandler.removeCallbacksAndMessages(null);
        }
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
     * View.OnClickListener事件回调,这个方法将受到睡眠时间的控制， 如果有不需要时间限制的方法可以重写onClick(View)方法，
     * 但如果同时有需要触发onViewClick函数时应该保留super.onClick(v)
     *
     * @param v View
     */
    protected void onViewClick(View v) {
        if (v.getId() == getResources().getIdentifier("back", "id", getPackageName())) {
            finishAnim();
        }
    }

    /**
     * 开启点击事件睡眠时间，设置时间后将无法通过BasicActivity的onViewClick方法进行多次点击，直到超过设置的睡眠时间为止
     *
     * @param time 毫秒
     */
    protected void setClickSleepTime(int time) {
        this.clickSleepTime = time;
    }

    public class BaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                receiver(context, intent);
            }
        }
    }

    /**
     * 异步完成结束后的回调函数，SecurityHandler及Asynchronous接口的任务处理
     *
     * @param msg Message
     */
    protected void handleMessage(Message msg) {
    }

    /**
     * 启动异步回调函数 *使用tag作为标记*
     *
     * @param what 将被分配到handleMessage(...)
     */
    protected void startAsyncTask(final int what) {
        startAsyncTask(what, null);
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

    /**
     * 启动异步回调函数 *使用what作为标记*
     *
     * @param what 将被分配到handleMessage(...)的what
     * @param obj  数据被传送到handleMessage(...)的obj
     */
    protected void startAsyncTask(int what, Object obj) {
        new AsyncThread<SecurityHandler<BasicActivity>>(securityHandler, ((Object) this).getClass().getName(), what, obj) {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                Object object = null;
                if (ASYNC_SET != null) {
                    Asynchronous a = ASYNC_SET.get(key);
                    if (a != null) {
                        object = a.async(what, obj);
                    }
                }
                if (w != null) {
                    SecurityHandler<BasicActivity> t = w.get();
                    if (t != null) {
                        t.sendMessage(t.obtainMessage(what, object));
                    }
                }
            }
        }.start();
    }

    /**
     * 设置异步任务接口，多次调用后面的将冲掉前面的
     *
     * @param asynchronous Asynchronous
     */
    public void setAsynchronous(Asynchronous asynchronous) {
        if (ASYNC_SET == null) {
            ASYNC_SET = new HashMap<>();
        }
        if (ASYNC_SET.containsKey(((Object) this).getClass().getName())) {
            ASYNC_SET.remove(((Object) this).getClass().getName());
        }
        ASYNC_SET.put(((Object) this).getClass().getName(), asynchronous);
    }

    /**
     * 相对安全的Handler，所有请求均由BasicActivity中handleMessage(...)接收(这个handler允许在协议中使用)
     */
    public SecurityHandler<BasicActivity> securityHandler = new SecurityHandler<>(this);

    protected static class SecurityHandler<T extends BasicActivity> extends Handler {
        WeakReference<T> w;

        private SecurityHandler(T t) {
            w = new WeakReference<>(t);
        }

        @Override
        public void handleMessage(Message msg) {
            if (w != null) {
                BasicActivity t = w.get();
                if (t != null && t.THIS_ACTIVITY_STATE) {
                    if (POST_SHOW_SHORT == msg.what) {
                        LToast.showShort(t, String.valueOf(msg.obj));
                    } else if (POST_SHOW_LONG == msg.what) {
                        LToast.showLong(t, String.valueOf(msg.obj));
                    } else {
                        t.handleMessage(msg);
                    }
                }
            }
        }
    }

    /**
     * 异步接口
     */
    public interface Asynchronous {

        Object async(final int tag, Object obj);

    }

    /**
     * 异步线程，与Asynchronous匹配使用
     */
    public class AsyncThread<T> extends Thread {
        WeakReference<T> w;
        String key;
        int what;
        Object obj;

        /**
         * 创建一个简易的异步任务
         *
         * @param w    弱引用
         * @param key  用来查找对应的任务
         * @param what 参数1
         * @param obj  参数2
         */
        public AsyncThread(T w, String key, int what, Object obj) {
            this.w = new WeakReference<>(w);
            this.key = key;
            this.what = what;
            this.obj = obj;
        }
    }
}
