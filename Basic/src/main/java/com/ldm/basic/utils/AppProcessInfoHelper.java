package com.ldm.basic.utils;

import java.util.ArrayList;
import java.util.List;

import com.ldm.basic.bean.AppProcessBean;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;

public class AppProcessInfoHelper {

	/**
	 * 获取当前系统处于运行中的APP信息（不返回图片）
	 * 
	 * @param ac Activity
	 * @return List<AppProcessBean>
	 */
	public static List<AppProcessBean> getRunningAppProcesses(Activity ac) {
		List<AppProcessBean> aps = new ArrayList<AppProcessBean>();
		ActivityManager activityManger = (ActivityManager) ac.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
		if (list != null) {
			for (int i = 0; i < list.size(); ++i) {
				ActivityManager.RunningAppProcessInfo amInfo = (ActivityManager.RunningAppProcessInfo) list.get(i);
				if (amInfo.pkgList != null && amInfo.pkgList.length > 0) {
					String[] pkgList = new String[amInfo.pkgList.length];
					System.arraycopy(amInfo.pkgList, 0, pkgList, 0, amInfo.pkgList.length);
					for (String s : pkgList) {
						try {
							ApplicationInfo appInfo = ac.getPackageManager().getApplicationInfo(s, 0);
							boolean isSystem = false;
							if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
								isSystem = true;
							}
							aps.add(new AppProcessBean(s, amInfo.processName, amInfo.pid, amInfo.uid, isSystem));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return aps;
	}

	/**
	 * 返回当前处于运行中的APP（不包含系统进程）
	 * 
	 * @param ac Activity
	 * @return List<AppProcessBean>可以循环做kill操作
	 */
	public static List<AppProcessBean> getRunningAppProcessesNotSystem(Activity ac) {
		List<AppProcessBean> aps = new ArrayList<AppProcessBean>();
		ActivityManager activityManger = (ActivityManager) ac.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
		if (list != null) {
			for (int i = 0; i < list.size(); ++i) {
				ActivityManager.RunningAppProcessInfo amInfo = (ActivityManager.RunningAppProcessInfo) list.get(i);
				if (amInfo.pkgList != null && amInfo.pkgList.length > 0) {
					String[] pkgList = new String[amInfo.pkgList.length];
					System.arraycopy(amInfo.pkgList, 0, pkgList, 0, amInfo.pkgList.length);
					for (String s : pkgList) {
						try {
							ApplicationInfo appInfo = ac.getPackageManager().getApplicationInfo(s, 0);
							if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
								aps.add(new AppProcessBean(s, amInfo.processName, amInfo.pid, amInfo.uid, true));
							}
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return aps;
	}

}
