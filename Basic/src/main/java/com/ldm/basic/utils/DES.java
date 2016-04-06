package com.ldm.basic.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ldm on 12-6-11.
 * DES加密解密
 */
public class DES {

    private static byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8};

    /**
     * 加密方法
     * @param encryptString 需要加密的字符串
     * @param encryptKey 密钥
     * @return String
     */
    public static String encrypt(String encryptString, String encryptKey){
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "DES");
		try {
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
			return byte2HexString(cipher.doFinal(encryptString.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
    }

    /**
     * 解密方法
     * @param decryptString 需要解密的字符串
     * @param decryptKey 密钥
     * @return String
     */
    public static String decrypt(String decryptString, String decryptKey) {
        byte[] byteMi = string2Byte(decryptString);
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "DES");
		try {
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
			return new String(cipher.doFinal(byteMi));
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
    }

    /**
     * byte[]转换成字符串
     *
     * @param b byte[]
     * @return String
     */
    private static String byte2HexString(byte[] b) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b1 : b) {
            String sTmp = Integer.toHexString(b1 & 0xff);
            if (sTmp.length() == 1) {
                stringBuilder.append("0");
            }
            stringBuilder.append(sTmp);
        }
        return stringBuilder.toString();
    }

    /**
     * 16进制转换成byte[]
     *
     * @param hexString String
     * @return byte[]
     */
    private static byte[] string2Byte(String hexString) {
        if (hexString.length() % 2 == 1)
            return null;
        byte[] ret = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            ret[i / 2] = Integer.decode("0x" + hexString.substring(i, i + 2)).byteValue();
        }
        return ret;
    }
}