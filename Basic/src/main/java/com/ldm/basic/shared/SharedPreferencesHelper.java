package com.ldm.basic.shared;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ldm.basic.utils.Log;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by ldm on 12-10-19. SharedPreferences的一个增强助手，提供了常用的操作
 */
public class SharedPreferencesHelper {

	private Context lContext;

	public SharedPreferencesHelper(Context context) {
		lContext = context;
	}

	/**
	 * 向指定文件内部写入数据
	 *
	 * @param sysFileName 文件名称
	 * @param map data
	 */
	public void put(String sysFileName, Map<String, String> map) {
		Editor editor = getEditor(createSharedPreferences(sysFileName));
		if (editor == null) {
			notFindSysFile(sysFileName);
		} else {
			for (Entry<String, String> entry : map.entrySet()) {
				editor.putString(entry.getKey(), entry.getValue());
			}
			editor.commit();
		}
	}

	/**
	 * 向指定文件内部写入数据
	 *
	 * @param sysFileName 文件名称
	 * @param key k
	 * @param value v
	 */
	public void put(String sysFileName, String key, String value) {
		Editor editor = getEditor(createSharedPreferences(sysFileName));
		if (editor == null) {
			notFindSysFile(sysFileName);
		} else {
			editor.putString(key, value);
			editor.commit();
		}
	}

	/**
	 * 删除指定Key的键值对
	 *
	 * @param sysFileName 文件名称
	 * @param key KEY
	 */
	public void remove(String sysFileName, String key) {
		Editor editor = getEditor(createSharedPreferences(sysFileName));
		if (editor == null) {
			notFindSysFile(sysFileName);
		} else {
			editor.remove(key);
			editor.commit();
		}
	}

	/**
	 * 删除最早存储的一条记录，这个操作是不准确的
	 *
	 * @param sysFileName 文件名称
	 */
	public void removeFirst(String sysFileName) {
		SharedPreferences sp = createSharedPreferences(sysFileName);
		if (sp != null) {
			Map<String, ?> map = sp.getAll();
			if (map != null && map.size() > 0) {
				Set<String> set = map.keySet();
				int len = map.size() - 1;
				int index = 0;
				for (String key : set) {
					if (index == len) {
						Editor editor = getEditor(sp);
						editor.remove(key);
						editor.commit();
					}
					index++;
				}
			}
		}
	}

	/**
	 * 删除最后存储的一条记录，这个删除是不准确的
	 *
	 * @param sysFileName 文件名称
	 */
	public void removeLast(String sysFileName) {
		SharedPreferences sp = createSharedPreferences(sysFileName);
		if (sp != null) {
			Map<String, ?> map = sp.getAll();
			if (map != null && map.size() > 0) {
				Set<String> set = map.keySet();
				for (String key : set) {
					Editor editor = getEditor(sp);
					editor.remove(key);
					editor.commit();
					return;
				}
			}
		}
	}
	
	/**
	 * 根据指定KEY，到指定文件中查询对应的VALUE
	 *
	 * @param sysFileName 文件名称
	 * @param key k
	 * @return 没有返回 null
	 */
	public String query(String sysFileName, String key) {
		SharedPreferences sp = createSharedPreferences(sysFileName);
		if (sp != null) {
			return sp.getString(key, null);
		}
		return null;
	}

	/**
	 * 返回这个文件下面有多少对数据
	 * 
	 * @param sysFileName
	 * @return count
	 */
	public int queryCount(String sysFileName) {
		SharedPreferences sp = createSharedPreferences(sysFileName);
		if (sp != null && sp.getAll() != null) {
			return sp.getAll().size();
		}
		return 0;
	}

	/**
	 * 返回整个数据文件的Map形式
	 *
	 * @param sysFileName 文件名称
	 * @return map
	 */
	public Map<String, ?> query(String sysFileName) {
		SharedPreferences sp = createSharedPreferences(sysFileName);
		if (sp != null) {
			return sp.getAll();
		}
		return null;
	}

	/**
	 * 创建一个SharedPreferences对象
	 *
	 * @param sysName 文件名称
	 * @return SharedPreferences
	 */
	public SharedPreferences createSharedPreferences(String sysName) {
		return lContext == null ? null : lContext.getSharedPreferences(sysName, Activity.MODE_PRIVATE);
	}

	/**
	 * 获取Editor实体
	 *
	 * @param sp SharedPreferences
	 * @return Editor
	 */
	public Editor getEditor(SharedPreferences sp) {
		return sp == null ? null : sp.edit();
	}

	/**
	 * 清空sysFileName指向的文件中的所有数据
	 *
	 * @param sysFileName 文件名称
	 */
	public void clear(String sysFileName) {
		Editor editor = getEditor(createSharedPreferences(sysFileName));
		if (editor == null) {
			notFindSysFile(sysFileName);
		} else {
			editor.clear();
			editor.commit();
		}
	}

	/**
	 * 当DEBUG模式开启时进行打印
	 *
	 * @param sysFileName 文件名称
	 */
	private void notFindSysFile(String sysFileName) {
		Log.w("没有找到【" + sysFileName + "】指向的数据文件！！！");
	}
}
