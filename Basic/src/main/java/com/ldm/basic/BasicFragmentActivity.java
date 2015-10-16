package com.ldm.basic;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;

import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.app.Configuration;
import com.ldm.basic.intent.IntentUtil;
import com.ldm.basic.shared.SharedPreferencesHelper;
import com.ldm.basic.utils.LazyImageDownloader;
import com.ldm.basic.utils.Log;
import com.ldm.basic.utils.SystemTool;

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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ldm on 13-11-10. BasicFragmentActivity中提供了全局的Activity记录、BaseReceiver接收 ，FragmentActivity状态及常用了一些方法
 */
public class BasicFragmentActivity extends FragmentActivity implements OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

	/**
	 * 使用startAsyncTask方法时默认的tag值
	 */
	public static final int ASYNC_TASK_DEFAULT_TAG = 10001;

	private String[] ACTION = null;

	private BaseReceiver receiver = null;

	/**
	 * --页面状态-- 当页面关闭时自动转为false 可用此变量来控制Activity销毁时残留的线程与Handler间的通讯
	 */
	public boolean THIS_ACTIVITY_STATE;

	/**
	 * true繁忙状态
	 */
	protected boolean isBusy;

	/**
	 * 窗口集合
	 */
	protected BasicFragment[] fragments;

	/**
	 * 当前位置
	 */
	protected int currentPosition;

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
	 * 附加的协议接口，使用时需要在构造方法中调用
	 */
	private List<BasicFragmentActivityProtocol> protocol;

	/**
	 * 协议用来去参数的map值
	 */
	Map<String, Serializable> protocolData;

	/**
	 * 这里保存了这个activity生成时所带的uuid值
	 */
	private String THIS_ACTIVITY_KEY;

	/**
	 * 注册每一个启动的Activity, 用于退出时结束程序
	 */
	public BasicFragmentActivity() {
		init(null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/** 不重复注册receiver **/
		if (ACTION != null && ACTION.length > 0 && null == receiver) {
			startReceiver();
		}

		// 触发协议的相关函数
		if (protocol != null) {
			synchronized (protocol) {
				for (BasicFragmentActivityProtocol p : protocol) {
					if (p != null) {
						p.onCreate(savedInstanceState);
					}
				}
			}
		}

		getSupportFragmentManager().getFragments();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 返回按钮添加事件，如果存在
		setOnClickListener(getResources().getIdentifier("back", "id", getPackageName()));

		// 触发协议的相关函数
		if (protocol != null) {
			synchronized (protocol) {
				for (BasicFragmentActivityProtocol p : protocol) {
					if (p != null) {
						p.onStart();
					}
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (fragments != null && currentPosition >= 0 && currentPosition < fragments.length) {
			BasicFragment f = fragments[currentPosition];
			if (f != null && f.THIS_FRAGMENT_STATE) {
				f.show();
			}
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
		if (null == SystemTool.activitySet) {
			showLong(BasicApplication.CONSTANTS.APP_ERROR);
			SystemTool.exit(false);// 保留已启动的service
		}
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
		// 触发协议的相关函数
		if (protocol != null) {
			synchronized (protocol) {
				for (BasicFragmentActivityProtocol p : protocol) {
					if (p != null) {
						p.onActivityResult(requestCode, resultCode, data);
					}
				}
			}
		}
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
	 * @param obj 参数
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
	 * @param bf 需要接收消息的<? extends BasicFragment>
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
	 * 获取Intent中额外附加的参数
	 *
	 * @param key 附加参数KEY
	 * @return 附加的字符串，null代表没有找到该附加值
	 */
	protected String getIntentToString(final String key) {
		String result = null;
		if (getIntent() != null) {
			result = getIntent().getStringExtra(key);
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
		if (getIntent() != null) {
			result = getIntent().getIntExtra(key, -1);
		}
		return result;
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
	 * 通过Id给对应的TextView或其所有子类设置text
	 *
	 * @param viewId id
	 * @param str s
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
	 * @param html s
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
		Toast.makeText(this, smg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Long Toast
	 *
	 * @param smg 提示语
	 */
	protected void showLong(final String smg) {
		Toast.makeText(this, smg, Toast.LENGTH_LONG).show();
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
	 * 添加一个协议
	 *
	 * @param p BasicFragmentActivityProtocol
	 */
	protected void addProtocol(BasicFragmentActivityProtocol p) {
		if (protocol == null) {
			protocol = new ArrayList<BasicFragmentActivityProtocol>();
			if (protocolData == null) {
				protocolData = new HashMap<String, Serializable>();
			}
		}
		protocol.add(p);
	}

	/**
	 * 可以通过这个方法调用协议中对应的任务
	 *
	 * @param task 任务ID
	 */
	protected void startProtocolTask(int task) {
		if (protocol != null || securityHandler != null) {
			securityHandler.sendMessage(securityHandler.obtainMessage(task));
		}
	}

	/**
	 * 向protocolData中存储数据
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	protected Serializable saveProtocolData(String key, Serializable value) {
		if (protocolData == null) {
			throw new NullPointerException("protocolData没有被初始化，不能使用这个方法...");
		}
		return protocolData.put(key, value);
	}

	/**
	 * 释放掉所有协议，释放时将会触发协议的onDestroy()方法
	 */
	public void clearProtocol() {
		if (protocol == null) {
			return;
		}
		synchronized (protocol) {
			for (BasicFragmentActivityProtocol p : protocol) {
				if (p != null) {
					p.onDestroy();
				}
			}
			protocol.clear();
		}
		if (protocolData != null) {
			protocolData.clear();
		}
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
	 * @param classes 目标
	 * @param enterAnim 进入动画文件ID
	 * @param exitAnim 退出动画文件ID
	 */
	protected void intent(final Class<?> classes, final int enterAnim, final int exitAnim) {
		IntentUtil.intentDIY(this, classes, enterAnim, exitAnim);
	}

	/**
	 * 页面跳转
	 *
	 * @param classes 目标
	 * @param map 参数
	 */
	protected void intent(final Class<?> classes, final Map<String, Object> map) {
		IntentUtil.intentDIY(this, classes, map);
	}

	/**
	 * 页面跳转
	 *
	 * @param classes 目标
	 * @param map 参数
	 * @param enterAnim 进入动画文件ID
	 * @param exitAnim 退出动画文件ID
	 */
	protected void intent(final Class<?> classes, final Map<String, Object> map, final int enterAnim, final int exitAnim) {
		IntentUtil.intentDIY(this, classes, map, enterAnim, exitAnim);
	}

	/**
	 * 动画形式关闭页面
	 */
	public void finishAnim() {
		if (getDownloader() != null) {
			getDownloader().clearCache();
			getDownloader().stopAllTask();
		}
		IntentUtil.finishDIY(this);
	}

	@Override
	public void finish() {
		/**
		 * 界面结束时，清除掉记录中对应的KEY
		 */
		SystemTool.remoreActivity(getActivityKey());
		super.finish();
	}

	public LazyImageDownloader getDownloader() {
		return null;
	}

	/**
	 * 动画形式关闭页面
	 *
	 * @param enterAnim 进入动画
	 * @param exitAnim 退出动画
	 */
	public void finishAnim(final int enterAnim, final int exitAnim) {
		IntentUtil.finishDIY(this, enterAnim, exitAnim);
	}

	/**
	 * 查询并添加OnClickListener事件 如果View不存在则抛出NullPointerException
	 *
	 * @param id ViewId
	 * @return View
	 */
	protected View setOnClickListener(final int id) {
		View v = getView(id);
		if (v != null) {
			v.setOnClickListener(this);
		}
		return v;
	}

	/**
	 * 返回指定key在CLIENT_INFO_CACHE_FILE中是否存在
	 *
	 * @param key 名称
	 * @return false表示没有找到对应的值
	 */
	protected boolean isExists(final String key) {
		SharedPreferencesHelper sph = new SharedPreferencesHelper(this);
		return sph.query(Configuration.CLIENT_INFO_CACHE_FILE, key) != null;
	}

	/**
	 * 返回指定key在CLIENT_INFO_CACHE_FILE中对应的值，没有返回null
	 *
	 * @param key 名称
	 * @return String
	 */
	protected String queryCache(final String key) {
		SharedPreferencesHelper sph = new SharedPreferencesHelper(this);
		return sph.query(Configuration.CLIENT_INFO_CACHE_FILE, key);
	}

	/**
	 * 将给定的key与value存储到CLIENT_INFO_CACHE_FILE中 *当key存在时执行覆盖操作*
	 *
	 * @param key 名称
	 * @param value 值
	 */
	protected void saveCache(final String key, final String value) {
		SharedPreferencesHelper sph = new SharedPreferencesHelper(this);
		sph.put(Configuration.CLIENT_INFO_CACHE_FILE, key, value);
	}

	/**
	 * 删除指定key在CLIENT_INFO_CACHE_FILE文件中对应的数据
	 *
	 * @param key 名称
	 */
	protected void removeCache(final String key) {
		SharedPreferencesHelper sph = new SharedPreferencesHelper(this);
		sph.remove(Configuration.CLIENT_INFO_CACHE_FILE, key);
	}

	/**
	 * 根据fileName及key获取对应文件中的value值
	 *
	 * @param fileName 文件名
	 * @param key key
	 * @return value 没有返回null
	 */
	protected String queryFromSharedPreferences(final String fileName, final String key) {
		SharedPreferencesHelper sph = new SharedPreferencesHelper(this);
		return sph.query(fileName, key);
	}

	/**
	 * 将给定的key与value存储到fileName指定的文件中 *当key存在时执行覆盖操作*
	 *
	 * @param fileName 文件名
	 * @param key key
	 * @param value 值
	 */
	protected void saveToSharedPreferences(final String fileName, final String key, final String value) {
		SharedPreferencesHelper sph = new SharedPreferencesHelper(this);
		sph.put(fileName, key, value);
	}

	/**
	 * 删除指定key在CLIENT_INFO_CACHE_FILE文件中对应的数据
	 *
	 * @param fileName 文件名
	 * @param key 名称
	 */
	protected void removeToSharedPreferences(final String fileName, final String key) {
		SharedPreferencesHelper sph = new SharedPreferencesHelper(this);
		sph.remove(fileName, key);
	}

	/**
	 * 读取AndroidManifest中Activity的meta_data属性
	 *
	 * @param componentName Activity.getComponentName()
	 * @param key key
	 * @return Object
	 */
	protected Object getMetaData(ComponentName componentName, String key) {
		Object result = null;
		try {
			ActivityInfo ai = this.getPackageManager().getActivityInfo(componentName == null ? this.getComponentName() : componentName, PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
			if (ai != null && ai.metaData != null && ai.metaData.containsKey(key)) {
				result = ai.metaData.get(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 切换界面
	 *
	 * @param oldPosition 旧位置 第一次这里应该时-1
	 * @param newPosition 新位置
	 * @param isAnim 是否使用预设动画
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
			newFragment.show();
			ft.show(newFragment);
		} else {
			newFragment = fragments[newPosition];
			ft.add(getResources().getIdentifier("container", "id", getPackageName()), newFragment, newPosition + "");
		}
		if (oldFragment != null) {
			oldFragment.hide();
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
	 * @param isAnim switchFragment时的isAnim参数
	 * @param ft FragmentTransaction
	 */
	protected void switchFragmentAnim(int oldPosition, int newPosition, boolean isAnim, FragmentTransaction ft) {
	}

	/**
	 * 开发者可以重写这个方法，实现对切换Fragment完成后的监听
	 *
	 * @param ft FragmentTransaction
	 * @param oldFragment 旧的 Fragment
	 * @param newFragment 新的Fragment
	 */
	protected void switchFragmentAfter(FragmentTransaction ft, BasicFragment oldFragment, BasicFragment newFragment) {
	}

	/**
	 * 开启一个定时器，无限循环（*所有的定时器将会在activity暂停时自动取消*）
	 *
	 * @param task BasicTimerTask
	 * @param delay 延时
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
		// 触发协议的相关函数
		if (protocol != null) {
			synchronized (protocol) {
				for (BasicFragmentActivityProtocol p : protocol) {
					if (p != null) {
						p.onSaveInstanceState(outState);
					}
				}
			}
		}
		BasicApplication.getBundle(outState);
		BasicApplication.initGlobalCacheListener(this);// 尝试初始化GlobalCacheListener
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
		// 触发协议的相关函数
		if (protocol != null) {
			synchronized (protocol) {
				for (BasicFragmentActivityProtocol p : protocol) {
					if (p != null) {
						p.onRestoreInstanceState(savedInstanceState);
					}
				}
			}
		}
		BasicApplication.initGlobalCacheListener(this);// 尝试初始化GlobalCacheListener
		if (BasicApplication.globalCacheListener != null) {
			BasicApplication.globalCacheListener.onRestoreInstanceState(this, savedInstanceState);
		} else {
			Log.e("GlobalCacheListener is null");
		}
		if (savedInstanceState != null) {
			BasicApplication.setClientCache(savedInstanceState.getSerializable(Configuration.CLIENT_CACHE_KEY));
		}
	}

	@Override
	public void onPause() {
		// 触发协议的相关函数
		if (protocol != null) {
			synchronized (protocol) {
				for (BasicFragmentActivityProtocol p : protocol) {
					if (p != null) {
						p.onPause();
					}
				}
			}
		}
		// 界面暂停时将取消所有的定时任务
		cancelAllSchedule();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// 触发协议的相关函数
		clearProtocol();
		if (isSoftInputStateListener) {
			// 注销对ViewTreeObserver的监听
			this.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(null);
		}
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
	 * 消息响应方法 当Activity需要响应Broadcast时使用
	 */
	protected synchronized void receiver(Context context, Intent intent) {
		// 触发协议的相关函数
		if (protocol != null) {
			synchronized (protocol) {
				for (BasicFragmentActivityProtocol p : protocol) {
					if (p != null) {
						p.receiver(context, intent);
					}
				}
			}
		}
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
	protected void startClickSleepTime(int time) {
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
			if (getWindow().getDecorView().getViewTreeObserver().isAlive()) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		}
	}

	@Override
	public void onGlobalLayout() {
		Rect r = new Rect();
		View rootNode = this.getWindow().getDecorView();
		rootNode.getWindowVisibleDisplayFrame(r);
		softInputHeight = rootNode.getRootView().getHeight() - r.bottom;
		onSoftInputState(softInputHeight == 0 ? SOFT_INPUT_STATE_CLOSE : SOFT_INPUT_STATE_OPEN);
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

	/**
	 * 异步完成结束后的回调函数，SecurityHandler及Asynchronous接口的任务处理
	 *
	 * @param tag 标识，用户可以用该标识来区分任务
	 * @param obj Asynchronous接口中async方法的返回参数
	 */
	protected void handleMessage(int tag, Object obj) {
		// 触发协议的相关函数
		if (protocol != null) {
			synchronized (protocol) {
				for (BasicFragmentActivityProtocol p : protocol) {
					if (p != null) {
						p.handleMessage(tag, obj);
					}
				}
			}
		}
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
	 * 使用securityHandler发送一条消息
	 *
	 * @param what what
	 * @param obj Object
	 */
	protected void sendHandleMessage(int what, Object obj) {
		if (THIS_ACTIVITY_STATE) {
			securityHandler.sendMessage(securityHandler.obtainMessage(what, obj));
		}
	}

	/**
	 * 使用securityHandler发送一条延时消息
	 *
	 * @param what what
	 * @param obj Object
	 * @param delayMillis what
	 */
	protected void sendHandleMessageDelayed(int what, Object obj, int delayMillis) {
		if (THIS_ACTIVITY_STATE) {
			securityHandler.sendMessageDelayed(securityHandler.obtainMessage(what, obj), delayMillis);
		}
	}

	/**
	 * 启动异步回调函数 *使用tag作为标记*
	 *
	 * @param tag 将被分配到handleMessage(tag, obj)的第一个参数中
	 * @param obj 数据被传送到handleMessage(tag, Object)中的第二个参数
	 */
	protected void startAsyncTask(int tag, Object obj) {
		new AsyncThread<SecurityHandler<BasicFragmentActivity>>(securityHandler, ((Object) this).getClass().getName(), tag, obj) {
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
					SecurityHandler<BasicFragmentActivity> t = w.get();
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
	 * 相对安全的Handler，所有请求均由BasicFragmentActivity中handleMessage(int, Object)接收
	 */
	protected SecurityHandler<BasicFragmentActivity> securityHandler = new SecurityHandler<BasicFragmentActivity>(this);

	protected static class SecurityHandler<T extends BasicFragmentActivity> extends Handler {
		WeakReference<T> w;

		private SecurityHandler(T t) {
			w = new WeakReference<T>(t);
		}

		@Override
		public void handleMessage(Message msg) {
			if (w != null) {
				BasicFragmentActivity t = w.get();
				if (t != null && t.THIS_ACTIVITY_STATE) {
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
	 * 异步接口，用来取替asynchronous方法
	 */
	public static interface Asynchronous {
		/**
		 * 任务结束后通过handleMessage(tag, Object)接收返回值
		 *
		 * @param tag 参数1
		 * @param obj 参数2
		 * @return Object将由handleMessage(tag, Object)接收
		 */
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
		 * @param w 弱引用
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
