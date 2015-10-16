package com.ldm.basic.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by ldm on 13-1-15. 常用的字符串处理工具
 */
public class TextUtils {

	/**
	 * 在源字符串中根据标签提取内容，不建议数据量大时使用
	 *
	 * @param text 源
	 * @param tag 标签 <tag></tag> 仅需要传入 tag
	 * @return <tag></tag>标签内的值， 没有返回null
	 */
	public static String extract(final String text, final String tag) {
		if (text == null || tag == null)
			return null;
		int start = text.indexOf("<" + tag + ">");
		int end = text.indexOf("</" + tag + ">");
		return (start != -1 && end != -1) ? text.substring(start + tag.length() + 2, text.indexOf("</" + tag + ">")) : null;
	}

	/**
	 * UUID生成器
	 *
	 * @return UUID
	 */
	public static String getUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 判断两个字符串是否相同 当两个值都为null时返回true
	 *
	 * @param s1 字符串1
	 * @param s2 字符串2
	 * @return true相同
	 */
	public static boolean equals(final String s1, final String s2) {
		return (s1 == null && s2 == null) || (s1 != null && s1.equals(s2));
	}

	/**
	 * equals的翻版，当两个值都等于null时返回false
	 *
	 * @param s1 字符串1
	 * @param s2 字符串2
	 * @return 当两个值都等于null时返回false
	 */
	public static boolean equals2(final String s1, final String s2) {
		return (s1 != null || s2 != null) && (s1 != null && s1.equals(s2));
	}

	/**
	 * 是否为空
	 *
	 * @param str 字符串
	 * @return true == null
	 */
	public static boolean isNull(final Object str) {
		return str == null || "".equals(str) || "null".equals(str);
	}

	/**
	 * 将字符串首字母转换为大写
	 *
	 * @param text String
	 * @return 转换后的字符串
	 */
	public static String upperFirst(String text) {
		return text.substring(0, 1).toUpperCase(Locale.CHINA) + text.replaceFirst("\\w", "");
	}

	/**
	 * 获取URL指向文件的名称
	 *
	 * @param url 地址
	 * @return name
	 */
	public static String getFileName(String url) {
		return url.substring(url.lastIndexOf("/") + 1, url.length());
	}

	/**
	 * 将url转换为可用的名称，带后缀名
	 *
	 * @param url 地址
	 * @return newName
	 */
	public static String urlToName(String url) {
		if (url == null || url.length() <= 0) {
			return null;
		}
		if (url.contains("http://")) {
			url = (url.substring(7));
			url = url.substring(url.indexOf("/") + 1).replace("/", "-");
		} else {
			if (url.indexOf("/") == 0) {
				url = url.substring(url.indexOf("/") + 1).replace("/", "-");
			} else {
				url = url.replace("/", "-");
			}
		}
		if (url.contains(".") && url.contains("?")) {
			String suffix = url.substring(url.lastIndexOf("."), url.length());
			url = url.substring(0, url.lastIndexOf(".")) + suffix.substring(0, suffix.indexOf("?"));
		}
		return url.replace("&", "_");
	}

	/**
	 * 将url转换为可用的名称，不带后缀名
	 *
	 * @param url 地址
	 * @return newName
	 */
	public static String urlToNameNoSuffix(String url) {
		if (url == null || url.length() <= 0) {
			return null;
		}
		if (url.contains("http://")) {
			url = (url.substring(7));
			url = url.substring(url.indexOf("/") + 1).replace("/", "-");
		} else {
			if (url.indexOf("/") == 0) {
				url = url.substring(url.indexOf("/") + 1).replace("/", "-");
			} else {
				url = url.replace("/", "-");
			}
		}
		if (url.contains("&")) {
			url = url.replace("&", "-");
		}
		if (url.contains("?")) {
			url = url.replace("?", "-");
		}
		if (url.contains(".")) {
			url = url.substring(0, url.lastIndexOf("."));
		}
		return url;
	}

	/**
	 * 根据给定的URL，使用MD5加密后拼接后缀名返回
	 * 
	 * @param url 地址
	 * @param unifiedSuffix 后缀
	 * @return
	 */
	public static String getCacheNameForUrl(String url, String unifiedSuffix) {
		if (url == null || url.length() <= 0) {
			return null;
		}
		if (unifiedSuffix == null) {
			return MD5.md5(url);
		}
		return MD5.md5(url) + "." + unifiedSuffix;
	}

	/**
	 * 是否是数字
	 *
	 * @param str 字符串
	 * @return true 是数字
	 */
	public static boolean isNumber(final String str) {
		return str != null && str.matches("[-]{0,1}[0-9]+");
	}

	/**
	 * 字符串转换int , 转换失败后返回def值
	 *
	 * @param number 需要转型的字符串
	 * @param def 转换失败的值
	 * @return int
	 */
	public static int parseInt(String number, int def) {
		if (!isNumber(number))
			return def;
		int result;
		try {
			result = Integer.parseInt(number);
		} catch (Exception e) {
			result = def;
		}
		return result;
	}

	/**
	 * 字符串转换float , 转换失败后返回def值
	 *
	 * @param number 需要转型的字符串
	 * @param def 转换失败的值
	 * @return float
	 */
	public static float parseFloat(String number, float def) {
		float result;
		try {
			result = Float.parseFloat(number);
		} catch (Exception e) {
			result = def;
		}
		return result;
	}

	/**
	 * 字符串转换long , 转换失败后返回def值
	 *
	 * @param number 需要转型的字符串
	 * @param def 转换失败的值
	 * @return long
	 */
	public static long parseLong(String number, long def) {
		if (!isNumber(number))
			return def;
		long result;
		try {
			result = Long.parseLong(number);
		} catch (Exception e) {
			result = def;
		}
		return result;
	}

	/**
	 * 字符串转换double , 转换失败后返回def值
	 *
	 * @param number 需要转型的字符串
	 * @param def 转换失败的值
	 * @return double
	 */
	public static double parseDouble(String number, double def) {
		double result;
		try {
			result = Double.parseDouble(number);
		} catch (Exception e) {
			result = def;
		}
		return result;
	}

	/**
	 * 从0开始 截取到第一个firstStr位置 (如果没有找到firstStr 默认返回source)
	 *
	 * @param source source == null return null
	 * @param toStr firstStr == null return source
	 * @return source.substring(0, source.indexOf(firstStr))
	 */
	public static String substring(String source, String toStr) {
		if (source == null)
			return null;
		if (toStr == null || "".equals(toStr) || !source.contains(toStr))
			return source;
		return source.substring(0, source.indexOf(toStr));
	}

	/**
	 * 从fromStr开始 截取到toStr结束 (如果没有找到toStr， 默认等于source.length())
	 *
	 * @param source source == null return null
	 * @param fromStr firstStr == null return substring(source, toStr)
	 * @param toStr toStr == null source.substring(source.indexOf(fromStr),
	 *            source.length())
	 * @return source.substring(source.indexOf(fromStr), source.indexOf(toStr))
	 */
	public static String substring(String source, String fromStr, String toStr) {
		if (source == null)
			return null;
		if (fromStr == null || "".equals(fromStr))
			return substring(source, toStr);
		if (toStr == null || "".equals(toStr) || !source.contains(toStr))
			return source.substring(source.indexOf(fromStr) + 1, source.length());
		return source.substring(source.indexOf(fromStr) + 1, source.indexOf(toStr));
	}

	/**
	 * 返回text对应的MD5值
	 *
	 * @param input 源文本
	 * @return null获取失败
	 */
	public static String textToMD5(byte[] input) {
		String re_md5 = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input);
			byte b[] = md.digest();

			StringBuilder buf = new StringBuilder("");
			for (int i : b) {
				if (i < 0) {
					i += 256;
				}
				if (i < 16) {
					buf.append("0");
				}
				buf.append(Integer.toHexString(i));
			}
			re_md5 = buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return re_md5;
	}
}
