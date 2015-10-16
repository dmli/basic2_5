package com.ldm.basic.utils;

import java.security.MessageDigest;

public class MD5 {

	private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', };

	/**
	 * 将给定的字符串使用md5进行加密
	 * 
	 * @param s
	 * @return
	 */
	public static String md5(String s) {
		if (TextUtils.isNull(s)) {
			return null;
		}
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(s.getBytes());
			byte[] m = md5.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0, n = m.length; i < n; i++) {
				sb.append(HEX[(m[i] >> 4) & 0xF]);
				sb.append(HEX[m[i] & 0xF]);
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
}
