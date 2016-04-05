package com.ldm.basic.utils;

import java.security.Provider;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ldm on 12-6-11.
 * AES加密工具
 */
public class AES {

    /**
     * 加密
     *
     * @param encryptString 需要加密的字符串
     * @param encryptKey    密钥
     * @return String
     */
    public static String encrypt(String encryptString, String encryptKey) {
        byte[] result = encrypt(encryptString.getBytes(), encryptKey);
        if (result != null) return byte2HexString(result);
        return null;
    }

    /**
     * 返回一个可用的Provider
     *
     * @return Provider
     */
    private static Provider getProvider() {
        String[] ps = new String[]{"BC", "SRP", "Crypto", "HarmonyJSSE"};
        Provider provider = null;
        for (String name : ps) {
            provider = Security.getProvider(name);
            if (provider != null) {
                break;
            }
        }
        return provider;
    }

    /**
     * 加密
     *
     * @param encryptByte 需要加密的字节数组
     * @return byte[]
     */
    public static byte[] encrypt(byte[] encryptByte, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES", getProvider());
            SecretKeySpec securekey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, securekey);// 设置密钥和加密形式
            return cipher.doFinal(encryptByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     *
     * @param encryptString 需要解密的字符串
     * @param encryptKey    密钥
     * @return String
     */
    public static String decrypt(String encryptString, String encryptKey) {
        byte[] result = decrypt(string2Byte(encryptString), encryptKey);
        if (result != null) return new String(result);
        return null;
    }

    /**
     * 解密
     *
     * @param encryptByte 需要解密的字节数组
     * @param key         密钥
     * @return byte[]
     */
    public static byte[] decrypt(byte[] encryptByte, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES", getProvider());
            SecretKeySpec secureKey = new SecretKeySpec(key.getBytes(), "AES");// 设置加密Key
            cipher.init(Cipher.DECRYPT_MODE, secureKey);// 设置密钥和解密形式
            return cipher.doFinal(encryptByte);
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

    public static void main(String[] args) {
        String text = "你要加密的内容";

        String idEncrypt = encrypt(text, "f9277c7c760b4e91a07e62930b92b71b");
        System.out.println(idEncrypt);

        String idDecrypt = decrypt(idEncrypt, "f9277c7c760b4e91a07e62930b92b71b");
        System.out.println(idDecrypt);

    }

}
