package com.ldm.basic.bean;

import java.io.Serializable;


/**
 * Created by ldm on 12-4-11.
 * 网络请求的传输类
 */
public class BasicInternetCmdBean implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 服务代码
	 */
	private String serviceCode;
	
	/**
	 * 请求数据
	 */
	private String data;
	
	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
