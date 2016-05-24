package com.ldm.basic.bean;

import android.graphics.drawable.Drawable;

/**
 * Created by ldm on 2013-5-22.
 * 通讯录数据Bean, 仅提供了：名称、电话、头像
 */
public class BasicContactBean {

	private String name;
	private String number;
	private Drawable photo;// 联系人头像

	public BasicContactBean(String name, String number) {
		this.name = name;
		this.number = number;
	}

	public BasicContactBean(String name, String number, Drawable photo) {
		this.name = name;
		this.number = number;
		this.photo = photo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Drawable getPhoto() {
		return photo;
	}

	public void setPhoto(Drawable photo) {
		this.photo = photo;
	}

}
