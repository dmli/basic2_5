package com.ldm.basic.properties;

import android.content.Context;

import java.util.Properties;


/**
 * Created by ldm on 12-8-11.
 * 简单的Properties文件操作助手
 */
public class BasicPropertiesHelper {
	
	/**
	 * 获取Properties对象
	 * @param context Context
	 * @param name 文件名（不需要后缀）
	 * @param defType 类型（raw）
	 * @param defPackage 报名
	 * @return Properties
	 */
	public static Properties loadProperties(Context context, String name, String defType, String defPackage){
		Properties props = new Properties();
        try {
			/* 得到属性文件在资源文件的ID */
            int id = context.getResources().getIdentifier(name, defType, defPackage);
            props.load(context.getResources().openRawResource(id));
        } catch (Exception e) {
            e.printStackTrace();
            props = null;
        }
		return props;
	}
	
	/**
	 * 根据Properties获取对应的值，没有找到返回null
	 * @param props p
	 * @param key k
	 * @return String
	 */
	public static String get(Properties props, String key){
		if (props == null) {
			return null;
		}
		if(key == null){
			return null;
		}
		return props.getProperty(key, null);
	}

}
