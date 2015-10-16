package com.ldm.basic.utils;

import android.graphics.Color;

public class LColor {

	/**
	 * 将10进制颜色转换成16进制的Color颜色
	 * 
	 * @param p int
	 * @return Color.argb
	 */
	public static int intToColor(int p) {
		return Color.argb((p >> 24) & 0xFF, (p >> 16) & 0xFF, (p >> 8) & 0xFF, p & 0xFF);
	}
}
