package com.ldm.basic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ldm.basic.app.Configuration;
import com.ldm.basic.intent.IntentUtil;
import com.ldm.basic.shared.SharedPreferencesHelper;
import com.ldm.basic.utils.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

/**
 * Created by ldm on 14-2-21. 基于Fragment增加了一些常用的功能, 用户调用initView(LayoutInflater,
 * ViewGroup, int)后，将可以使用BasicFragment的一些基础属性及方法 如：rootView =（布局）、inflater
 * =（LayoutInflater）、inflate(resource, ViewGroup,
 * attachToRoot)及getView(resource)等方法
 * <p/>
 * *仅限于配合BasicFragmentActivity使用*
 */
public class BasicFragment extends Fragment implements View.OnClickListener {

    /**
     * 使用startAsyncTask方法时默认的tag值
     */
    public static final int ASYNC_TASK_DEFAULT_TAG = 10001;
    protected View rootView;
    protected LayoutInflater inflater;
    protected BasicFragmentActivity activity;

    private BaseReceiver receiver = null;

    /**
     * --页面状态-- 当页面关闭时自动转为false 可用此变量来控制Activity销毁时残留的线程与Handler间的通讯
     */
    public boolean THIS_FRAGMENT_STATE;

    /**
     * true繁忙状态
     */
    protected boolean isBusy;

    /**
     * 网络监听器的唯一标识
     */
    protected String networkListenerTag;

    /**
     * 简易的异步线程接口，为了脱离asynchronous内部方法影响finish()而设计的
     */
    private static Map<String, Asynchronous> ASYNC_SET;

    /**
     * 用来获取当前播放进度的定时器
     */
    private Timer timer;

    private Map<String, BasicTimerTask> timerTasks;

    /**
     * 界面按钮控制器，可以通过设置间隔时间开启点击事件监听
     */
    private long upClickTime;// 上一次点击的时间
    private long clickSleepTime;

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
        activity = null;
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        // 界面暂停时将取消所有的定时任务
        cancelAllSchedule();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        // 如果设置了异步任务，这里需要清除
        if (ASYNC_SET != null) {
            ASYNC_SET.remove(((Object) this).getClass().getName());
        }
        stopReceiver();
        THIS_FRAGMENT_STATE = false;
        if (securityHandler != null) {
            securityHandler.removeCallbacksAndMessages(null);
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
            Log.e("inflater 没有初始化，可以考虑在onCreateView中使用initView(LayoutInflater, ViewGroup, int)方法或自行设置inflater");
        }
        return inflater == null ? null : inflater.inflate(resource, root, attachToRoot);
    }

    /**
     * 获取Intent中额外附加的参数
     *
     * @param key 附加参数KEY
     * @return 附加的字符串，null代表没有找到该附加值
     */
    protected String getIntentToString(final String key) {
        String result = null;
        if (activity != null && activity.getIntent() != null) {
            result = activity.getIntent().getStringExtra(key);
        }
        return result;
    }

    /**
     * 返回Intent中额外附加的参数
     *
     * @param key 附加参数KEY
     * @return 附加的整形值，-1代表没有找到该附加值
     */
    protected int getIntentToInt(final String key) {
        int result = -1;
        if (activity != null && activity.getIntent() != null) {
            result = activity.getIntent().getIntExtra(key, -1);
        }
        return result;
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
     * @param objs Object[]
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
            Log.e("没有找到 id" + viewId + "所对应的View");
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
            Log.e("没有找到 id" + viewId + "所对应的View");
        }
    }

    /**
     * 获取TextView的text属性, TextView == null时 返回null
     *
     * @param viewId TextView ID
     * @return text
     */
    protected String getViewText(final int viewId) {
        return getViewText((TextView) getView(viewId));
    }

    /**
     * 获取TextView的text属性, TextView == null时 返回null
     *
     * @param tv TextView
     * @return text
     */
    protected String getViewText(final TextView tv) {
        return tv == null ? null : tv.getText() == null ? null : tv.getText().toString();
    }

    /**
     * Short Toast
     *
     * @param smg 提示语
     */
    protected void showShort(final String smg) {
        Toast.makeText(activity, smg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Long Toast
     *
     * @param smg 提示语
     */
    protected void showLong(final String smg) {
        Toast.makeText(activity, smg, Toast.LENGTH_LONG).show();
    }

    /**
     * Short Toast
     *
     * @param smg 提示语
     */
    protected void postShowShort(final String smg) {
        securityHandler.sendMessage(securityHandler.obtainMessage(BasicActivity.POST_SHOW_SHORT, smg));
    }

    /**
     * Long Toast
     *
     * @param smg 提示语
     */
    protected void postShowLong(final String smg) {
        securityHandler.sendMessage(securityHandler.obtainMessage(BasicActivity.POST_SHOW_LONG, smg));
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
     * 返回指定key在CLIENT_INFO_CACHE_FILE中是否存在
     *
     * @param key 名称
     * @return false表示没有找到对应的值
     */
    protected boolean isExists(final String key) {
        SharedPreferencesHelper sph = new SharedPreferencesHelper(activity);
        return sph.query(Configuration.CLIENT_INFO_CACHE_FILE, key) != null;
    }

    /**
     * 返回指定key在CLIENT_INFO_CACHE_FILE中对应的值，没有返回null
     *
     * @param key 名称
     * @return String
     */
    protected String queryCache(final String key) {
        SharedPreferencesHelper sph = new SharedPreferencesHelper(activity);
        return sph.query(Configuration.CLIENT_INFO_CACHE_FILE, key);
    }

    /**
     * 将给定的key与value存储到CLIENT_INFO_CACHE_FILE中 *当key存在时执行覆盖操作*
     *
     * @param key   名称
     * @param value 值
     */
    protected void saveCache(final String key, final String value) {
        SharedPreferencesHelper sph = new SharedPreferencesHelper(activity);
        sph.put(Configuration.CLIENT_INFO_CACHE_FILE, key, value);
    }

    /**
     * 删除指定key在CLIENT_INFO_CACHE_FILE文件中对应的数据
     *
     * @param key 名称
     */
    protected void removeCache(final String key) {
        SharedPreferencesHelper sph = new SharedPreferencesHelper(activity);
        sph.remove(Configuration.CLIENT_INFO_CACHE_FILE, key);
    }

    /**
     * 根据fileName及key获取对应文件中的value值
     *
     * @param fileName 文件名
     * @param key      key
     * @return value 没有返回null
     */
    protected String queryFromSharedPreferences(final String fileName, final String key) {
        SharedPreferencesHelper sph = new SharedPreferencesHelper(activity);
        return sph.query(fileName, key);
    }

    /**
     * 将给定的key与value存储到fileName指定的文件中 *当key存在时执行覆盖操作*
     *
     * @param fileName 文件名
     * @param key      key
     * @param value    值
     */
    protected void saveToSharedPreferences(final String fileName, final String key, final String value) {
        SharedPreferencesHelper sph = new SharedPreferencesHelper(activity);
        sph.put(fileName, key, value);
    }

    /**
     * 删除指定key在CLIENT_INFO_CACHE_FILE文件中对应的数据
     *
     * @param fileName 文件名
     * @param key      名称
     */
    protected void removeToSharedPreferences(final String fileName, final String key) {
        SharedPreferencesHelper sph = new SharedPreferencesHelper(activity);
        sph.remove(fileName, key);
    }

    /**
     * 开启一个定时器，无限循环（*所有的定时器将会在activity暂停时自动取消*）
     *
     * @param task   BasicTimerTask
     * @param delay  延时
     * @param period 周期的间隔时间
     */
    protected void scheduleAtFixedRate(final BasicTimerTask task, long delay, long period) {
        if (timer == null) {
            timer = new Timer();
        }
        if (timerTasks == null) {
            timerTasks = new HashMap<String, BasicTimerTask>();
        }
        if (timerTasks.containsKey(task.getTag())) {
            BasicTimerTask t;
            if ((t = timerTasks.remove(task.getTag())) != null) {
                // 如果有相同的新的将替换旧的
                t.cancel();
            }
        }
        // 启动一个新的循环定时任务
        timer.scheduleAtFixedRate(task, delay, period);
    }

    /**
     * 根据标识取消定时任务
     *
     * @param tag 创建BasicTimerTask时的唯一标识
     */
    protected void cancelSchedule(final String tag) {
        if (timerTasks != null) {
            if (timerTasks.containsKey(tag)) {
                BasicTimerTask t;
                if ((t = timerTasks.remove(tag)) != null) {
                    // 如果有相同的新的将替换旧的
                    t.cancel();
                }
            }
            if (timerTasks.size() <= 0) {
                timerTasks = null;
                timer.cancel();
                timer = null;
            }
        }
    }

    /**
     * 取消掉所有任务
     */
    protected void cancelAllSchedule() {
        if (timerTasks != null) {
            for (String s : timerTasks.keySet()) {
                BasicTimerTask t;
                if ((t = timerTasks.remove(s)) != null) {
                    // 如果有相同的新的将替换旧的
                    t.cancel();
                }
            }
            timerTasks.clear();
            timerTasks = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * 启动接收器
     *
     * @param action 多个用 , 号分割
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
     * 使用securityHandler发送一条消息
     *
     * @param what what
     * @param obj  Object
     */
    protected void sendHandleMessage(int what, Object obj) {
        if (THIS_FRAGMENT_STATE) {
            securityHandler.sendMessage(securityHandler.obtainMessage(what, obj));
        }
    }

    /**
     * 使用securityHandler发送一条延时消息
     *
     * @param what        what
     * @param obj         Object
     * @param delayMillis what
     */
    protected void sendHandleMessageDelayed(int what, Object obj, int delayMillis) {
        if (THIS_FRAGMENT_STATE) {
            securityHandler.sendMessageDelayed(securityHandler.obtainMessage(what, obj), delayMillis);
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
    }

    /**
     * 开启点击事件睡眠时间，设置时间后将无法通过BasicFragment的onViewClick方法进行多次点击，直到超过设置的睡眠时间为止
     *
     * @param time 毫秒
     */
    protected void startClickSleepTime(int time) {
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
     * @param tag 标识，用户可以用该标识来区分任务
     * @param obj Asynchronous接口中async方法的返回参数
     */
    protected void handleMessage(int tag, Object obj) {
    }

    /**
     * 启动异步回调函数 *使用默认标识 ASYNC_TASK_DEFAULT_TAG *
     */
    protected void startAsyncTask() {
        startAsyncTask(ASYNC_TASK_DEFAULT_TAG, null);
    }

    /**
     * 启动异步回调函数 *使用tag作为标记*
     *
     * @param tag 将被分配到handleMessage(tag, obj)的第一个参数中
     */
    protected void startAsyncTask(final int tag) {
        startAsyncTask(tag, null);
    }

    /**
     * 启动异步回调函数 *使用默认标识ASYNC_TASK_DEFAULT_TAG，并传递一个参数*
     *
     * @param obj 数据被传送到handleMessage(tag, Object)中的第二个参数
     */
    protected void startAsyncTask(final Object obj) {
        startAsyncTask(ASYNC_TASK_DEFAULT_TAG, obj);
    }

    /**
     * 启动异步回调函数 *使用tag作为标记*
     *
     * @param tag 将被分配到handleMessage(tag, obj)的第一个参数中
     * @param obj 数据被传送到handleMessage(tag, Object)中的第二个参数
     */
    protected void startAsyncTask(int tag, Object obj) {
        new AsyncThread<SecurityHandler<BasicFragment>>(securityHandler, ((Object) this).getClass().getName(), tag, obj) {
            @Override
            public void run() {
            	android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                Object object = null;
                if (ASYNC_SET != null) {
                    Asynchronous a = ASYNC_SET.get(key);
                    if (a != null) {
                        object = a.async(tag, obj);
                    }
                }
                if (w != null) {
                    SecurityHandler<BasicFragment> t = w.get();
                    if (t != null) {
                        t.sendMessage(t.obtainMessage(tag, object));
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
            ASYNC_SET = new HashMap<String, Asynchronous>();
        }
        if (ASYNC_SET.containsKey(((Object) this).getClass().getName())) {
            ASYNC_SET.remove(((Object) this).getClass().getName());
        }
        ASYNC_SET.put(((Object) this).getClass().getName(), asynchronous);
    }

    /**
     * 相对安全的Handler，所有请求均由BasicFragment中handleMessage(int, Object)接收
     */
    protected SecurityHandler<BasicFragment> securityHandler = new SecurityHandler<BasicFragment>(this);

    protected static class SecurityHandler<T extends BasicFragment> extends Handler {
        WeakReference<T> w;

        private SecurityHandler(T t) {
            w = new WeakReference<T>(t);
        }

        @Override
        public void handleMessage(Message msg) {
            if (w != null) {
                BasicFragment t = w.get();
                if (t != null && t.THIS_FRAGMENT_STATE) {
                    if (BasicActivity.POST_SHOW_SHORT == msg.what) {
                        t.showShort(String.valueOf(msg.obj));
                    } else if (BasicActivity.POST_SHOW_LONG == msg.what) {
                        t.showLong(String.valueOf(msg.obj));
                    } else {
                        t.handleMessage(msg.what, msg.obj);
                    }
                }
            }
        }
    }

    /**
     * 异步接口
     */
    public static interface Asynchronous {

        public Object async(final int tag, Object obj);

    }

    /**
     * 异步线程，与Asynchronous匹配使用
     */
    public class AsyncThread<T> extends Thread {
        WeakReference<T> w;
        String key;
        int tag;
        Object obj;

        /**
         * 创建一个简易的异步任务
         *
         * @param w   弱引用
         * @param key 用来查找对应的任务
         * @param tag 参数1
         * @param obj 参数2
         */
        public AsyncThread(T w, String key, int tag, Object obj) {
            this.w = new WeakReference<T>(w);
            this.key = key;
            this.tag = tag;
            this.obj = obj;
        }
    }
}