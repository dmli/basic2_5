package com.ldm.basic.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.text.format.Formatter;

public class MemoryHelper {

	/**
	 * 应用程序最大可用内存 
	 * 
	 * @return float MB
	 */
	public static float getMaxMemory() {
		return Runtime.getRuntime().maxMemory() / 1024.0f / 1024.0f;
	}

	/**
	 * 应用程序已获得内存 
	 * 
	 * @return float MB
	 */
	public static float getTotalMemory() {
		return Runtime.getRuntime().totalMemory() / 1024.0f / 1024.0f;
	}

	/**
	 * 应用程序已获得内存中未使用内存 
	 * 
	 * @return float MB
	 */
	public static float getFreeMemory() {
		return Runtime.getRuntime().freeMemory() / 1024.0f / 1024.0f;
	}

	/**
	 * 应用程序剩余的最大可申请内存
	 * 
	 * @return float MB
	 */
	public static float getMaxFreeMemory() {
		return (Runtime.getRuntime().maxMemory() - getTotalMemory() + Runtime.getRuntime().freeMemory()) / 1024.0f / 1024.0f;
	}

	/**
	 * 返回系统可用内存，通过ActivityManager获取
	 * 
	 * @param context Context
	 * @return Formatter.formatFileSize格式化后的字符串
	 */
	public String getAvailMemoryFormat(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(outInfo);
		return Formatter.formatFileSize(context, outInfo.availMem);
	}

	/**
	 * 返回系统可用内存，通过ActivityManager获取
	 * 
	 * @param context Context
	 * @return availMem值
	 */
	public long getAvailMemory(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(outInfo);
		return outInfo.availMem;
	}

}
