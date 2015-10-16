package com.ldm.basic.res;

import com.google.webp.libwebp;

public class Libwebp {

	static {
		System.loadLibrary("webp");
	}

	public static byte[] WebPDecodeARGB(byte[] data, int lenght, int[] width, int[] height) {
		return libwebp.WebPDecodeARGB(data, lenght, width, height);
	}
	
	public static byte[] WebPEncodeRGBA(byte[] sourceByteArray, int width, int height, int rows, int quality) {
		return libwebp.WebPEncodeRGBA(sourceByteArray, width, height, rows, quality);
	}
	
	
	
}
