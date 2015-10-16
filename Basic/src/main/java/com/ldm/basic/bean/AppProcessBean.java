package com.ldm.basic.bean;

import android.graphics.drawable.Drawable;

public class AppProcessBean {

	public String appName;
	public String processName;
	public int pid;
	public int uid;
	public Drawable icon;
	public boolean isSystem;

	public AppProcessBean(String appName, String processName, int pid, int uid, boolean isSystem) {
		this.appName = appName;
		this.processName = processName;
		this.pid = pid;
		this.uid = uid;
		this.isSystem = isSystem;
	}

}
