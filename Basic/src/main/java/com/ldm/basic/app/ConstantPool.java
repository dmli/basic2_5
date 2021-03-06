package com.ldm.basic.app;

import java.io.Serializable;

/**
 * Created by ldm on 12-11-8.
 * 常量
 */
public class ConstantPool implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final String NET_WORK_ERROR = "网络连接失败！";
	public final String CONFIRM = "确认";
	public final String CANCEL = "取消";
	public final String SETTINGS = "设置";
	public final String EXIT_TEXT = "再按一次退出应用";
	public final String NET_WORK_ERROR_TO_SETTINGS = "\t\t您的网络没有开启或接入点错误，请重新设置！";
	
}
