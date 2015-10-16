package com.ldm.basic;

import java.io.Serializable;
import java.util.Map;

import com.ldm.basic.intent.IntentUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by ldm on 15-3-25. BasicActivity中提供的一组协议，这个协议需要在构造BasicActivity时注入。
 * 协议提供了Activity的一部分回调方法，主要用来做多个界面融合时使用
 */
public abstract class BasicActivityProtocol {

	protected BasicActivity activity;

	public BasicActivityProtocol(BasicActivity activity) {
		this.activity = activity;
	}

	protected void handleMessage(int tag, Object obj) {

	}

	/**
	 * 消息响应方法 当Activity需要响应Broadcast时使用,需要在BasicActivity注册广播
	 */
	protected synchronized void receiver(Context context, Intent intent) {

	}

	protected void onCreate(Bundle savedInstanceState) {
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	}

	protected void onStart() {
	}

	protected void onRestart() {
	}

	protected void onResume() {
	}

	protected void onPause() {
	}

	protected void onStop() {
	}

	protected void onDestroy() {
	}

	protected void onSaveInstanceState(Bundle outState) {
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	}

	/**
	 * 方法返回BasicActivity中为BasicActivityProtocol提供的数据项
	 * （这个方法取出数据后将会在数据清单中永久的删除，如果需要保存原数据，可以使用findProtocolData(key)方法）
	 *
	 * @param key key
	 * @return Serializable
	 */
	protected Serializable getProtocolData(String key) {
		if (activity.protocolData == null) {
			return null;
		}
		return activity.protocolData.remove(key);
	}

	/**
	 * 获取失败时将返回null
	 * （这个方法取出数据后将会在数据清单中永久的删除，如果需要保存原数据，可以使用findProtocolData(key)方法）
	 *
	 * @param key key
	 * @return String
	 */
	protected String getString(String key) {
		if (activity.protocolData == null) {
			return null;
		}
		return (String) activity.protocolData.remove(key);
	}

	/**
	 * 获取失败时将返回-1 （这个方法取出数据后将会在数据清单中永久的删除，如果需要保存原数据，可以使用findProtocolData(key)方法）
	 *
	 * @param key key
	 * @return int
	 */
	protected int getInt(String key) {
		if (activity.protocolData == null) {
			return -1;
		}
		return (Integer) activity.protocolData.remove(key);
	}

	/**
	 * 方法返回BasicActivity中为BasicActivityProtocol提供的数据项
	 *
	 * @param key key
	 * @return Serializable
	 */
	protected Serializable findProtocolData(String key) {
		if (activity.protocolData == null) {
			return null;
		}
		return activity.protocolData.get(key);
	}

	/**
	 * 获取失败时将返回null
	 *
	 * @param key key
	 * @return String
	 */
	protected String findString(String key) {
		if (activity.protocolData == null) {
			return null;
		}
		return (String) activity.protocolData.remove(key);
	}

	/**
	 * 获取失败时将返回-1
	 *
	 * @param key key
	 * @return int
	 */
	protected int findInt(String key) {
		if (activity.protocolData == null) {
			return -1;
		}
		return (Integer) activity.protocolData.remove(key);
	}

	/**
	 * 页面跳转
	 *
	 * @param classes c
	 */
	public void intent(final Class<?> classes) {
		IntentUtil.intentDIY(activity, classes);
	}

	/**
	 * 页面跳转
	 *
	 * @param classes 目标
	 * @param enterAnim 进入动画文件ID
	 * @param exitAnim 退出动画文件ID
	 */
	public void intent(final Class<?> classes, final int enterAnim, final int exitAnim) {
		IntentUtil.intentDIY(activity, classes, enterAnim, exitAnim);
	}

	/**
	 * 页面跳转
	 *
	 * @param classes 目标
	 * @param map 参数
	 */
	public void intent(final Class<?> classes, final Map<String, Object> map) {
		IntentUtil.intentDIY(activity, classes, map);
	}

	/**
	 * 页面跳转
	 *
	 * @param classes 目标
	 * @param map 参数
	 * @param enterAnim 进入动画文件ID
	 * @param exitAnim 退出动画文件ID
	 */
	public void intent(final Class<?> classes, final Map<String, Object> map, final int enterAnim, final int exitAnim) {
		IntentUtil.intentDIY(activity, classes, map, enterAnim, exitAnim);
	}

	/**
	 * 动画形式关闭页面
	 */
	public void finishAnim() {
		activity.finishAnim();
	}

	/**
	 * 动画形式关闭页面
	 *
	 * @param enterAnim 进入动画
	 * @param exitAnim 退出动画
	 */
	public void finishAnim(final int enterAnim, final int exitAnim) {
		activity.finishAnim(enterAnim, exitAnim);
	}

	/**
	 * Short Toast
	 *
	 * @param smg 提示语
	 */
	public void showShort(final String smg) {
		Toast.makeText(activity, smg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Long Toast
	 *
	 * @param smg 提示语
	 */
	public void showLong(final String smg) {
		Toast.makeText(activity, smg, Toast.LENGTH_LONG).show();
	}

	/**
	 * Short Toast
	 *
	 * @param smg 提示语
	 */
	public void postShowShort(final String smg) {
		activity.securityHandler.sendMessage(activity.securityHandler.obtainMessage(BasicActivity.POST_SHOW_SHORT, smg));
	}

	/**
	 * Long Toast
	 *
	 * @param smg 提示语
	 */
	public void postShowLong(final String smg) {
		activity.securityHandler.sendMessage(activity.securityHandler.obtainMessage(BasicActivity.POST_SHOW_LONG, smg));
	}
}
