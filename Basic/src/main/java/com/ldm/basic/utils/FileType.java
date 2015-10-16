package com.ldm.basic.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

public class FileType {

	/**
	 * 返回文件类型
	 *
	 * @param filePath 文件路径
	 * @return 字符串（ jpg/png/gif）
	 */
	public static String getFileType(String filePath) {
		String header = getFileHeader(filePath);
		if (header == null || "".equals(header)) {
			return header;
		}
		String value;
		switch (header) {
		case "FFD8FF":
			value = "jpg";
			break;
		case "524946":
			value = "webp";
			break;
		case "89504E47":
			value = "png";
			break;
		case "47494638":
			value = "gif";
			break;
		case "49492A00":
			value = "tif";
			break;
		case "424D":
			value = "bmp";
			break;
		case "41433130":
			value = "dwg";
			break;
		case "38425053":
			value = "psd";
			break;
		case "7B5C727466":
			value = "rtf";
			break;
		case "3C3F786D6C":
			value = "xml";
			break;
		case "68746D6C3E":
			value = "html";
			break;
		case "44656C69766572792D646174653A":
			value = "eml";
			break;
		case "D0CF11E0":
			value = "doc";
			break;
		case "5374616E64617264204A":
			value = "mdb";
			break;
		case "252150532D41646F6265":
			value = "ps";
			break;
		case "255044462D312E":
			value = "pdf";
			break;
		case "504B0304":
			value = "zip";
			break;
		case "52617221":
			value = "rar";
			break;
		case "57415645":
			value = "wav";
			break;
		case "41564920":
			value = "avi";
			break;
		case "2E524D46":
			value = "rm";
			break;
		case "000001BA":
			value = "mpg";
			break;
		case "000001B3":
			value = "mpg";
			break;
		case "6D6F6F76":
			value = "mov";
			break;
		case "3026B2758E66CF11":
			value = "asf";
			break;
		case "4D546864":
			value = "mid";
			break;
		case "1F8B08":
			value = "gz";
			break;
		default:
			value = null;
			break;
		}
		return value;
	}

	/**
	 * 获取文件头信息
	 *
	 * @param filePath 文件地址
	 * @return 文件的头文件信息
	 */
	public static String getFileHeader(String filePath) {
		FileInputStream is = null;
		String value = null;
		try {
			is = new FileInputStream(filePath);
			byte[] b = new byte[3];
			is.read(b, 0, b.length);
			value = bytesToHexString(b);
		} catch (Exception e) {
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return value;
	}

	private static String bytesToHexString(byte[] src) {
		StringBuilder builder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		String hv;
		for (int i = 0; i < src.length; i++) {
			hv = Integer.toHexString(src[i] & 0xFF).toUpperCase(Locale.CHINESE);
			if (hv.length() < 2) {
				builder.append(0);
			}
			builder.append(hv);
		}
		return builder.toString();
	}
}
