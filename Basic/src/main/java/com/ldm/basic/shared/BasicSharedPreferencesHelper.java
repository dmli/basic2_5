package com.ldm.basic.shared;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ldm.basic.app.Configuration;
import com.ldm.basic.utils.LLog;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by ldm on 12-10-19.
 * SharedPreferences的一个增强助手，提供了常用的操作
 */
public class BasicSharedPreferencesHelper {


    /**
     * 返回指定key在CLIENT_SHARED_CACHE_FILE中是否存在
     *
     * @param key 名称
     * @return false表示没有找到对应的值
     */
    public static boolean isSharedFileExists(final Context context, final String key) {
        return BasicSharedPreferencesHelper.query(context, Configuration.CLIENT_SHARED_CACHE_FILE, key) != null;
    }

    /**
     * 返回指定key在CLIENT_SHARED_CACHE_FILE中对应的值，没有返回null
     *
     * @param key 名称
     * @return String
     */
    public static String queryCacheInSharedFile(final Context context, final String key) {
        return BasicSharedPreferencesHelper.query(context, Configuration.CLIENT_SHARED_CACHE_FILE, key);
    }

    /**
     * 将给定的key与value存储到CLIENT_SHARED_CACHE_FILE中 *当key存在时执行覆盖操作*
     *
     * @param key   名称
     * @param value 值
     */
    public static void saveCacheToSharedFile(final Context context, final String key, final String value) {
        BasicSharedPreferencesHelper.put(context, Configuration.CLIENT_SHARED_CACHE_FILE, key, value);
    }

    /**
     * 删除指定key在CLIENT_SHARED_CACHE_FILE文件中对应的数据
     *
     * @param key 名称
     */
    public static void removeCacheInSharedFile(final Context context, final String key) {
        BasicSharedPreferencesHelper.remove(context, Configuration.CLIENT_SHARED_CACHE_FILE, key);
    }

    /**
     * 向指定文件内部写入数据
     *
     * @param sysFileName 文件名称
     * @param map         data
     */
    public static void put(Context context, String sysFileName, Map<String, String> map) {
        Editor editor = createSharedPreferences(context, sysFileName).edit();
        if (editor == null) {
            notFindSysFile(sysFileName);
        } else {
            for (Entry<String, String> entry : map.entrySet()) {
                editor.putString(entry.getKey(), entry.getValue());
            }
            editor.apply();
        }
    }

    /**
     * 向指定文件内部写入数据
     *
     * @param sysFileName 文件名称
     * @param key         k
     * @param value       v
     */
    public static void put(Context context, String sysFileName, String key, String value) {
        Editor editor = createSharedPreferences(context, sysFileName).edit();
        if (editor == null) {
            notFindSysFile(sysFileName);
        } else {
            editor.putString(key, value);
            editor.apply();
        }
    }

    /**
     * 删除指定Key的键值对
     *
     * @param sysFileName 文件名称
     * @param key         KEY
     */
    public static void remove(Context context, String sysFileName, String key) {
        Editor editor = createSharedPreferences(context, sysFileName).edit();
        if (editor == null) {
            notFindSysFile(sysFileName);
        } else {
            editor.remove(key);
            editor.apply();
        }
    }

    /**
     * 删除最早存储的一条记录，这个操作是不准确的
     *
     * @param sysFileName 文件名称
     */
    public static void removeFirst(Context context, String sysFileName) {
        SharedPreferences sp = createSharedPreferences(context, sysFileName);
        if (sp != null) {
            Map<String, ?> map = sp.getAll();
            if (map != null && map.size() > 0) {
                Set<String> set = map.keySet();
                String[] s = set.toArray(new String[set.size()]);
                Editor editor = sp.edit();
                editor.remove(s[set.size() - 1]);
                editor.apply();
            }
        }
    }

    /**
     * 删除最后存储的一条记录，这个删除是不准确的
     *
     * @param sysFileName 文件名称
     */
    public static void removeLast(Context context, String sysFileName) {
        SharedPreferences sp = createSharedPreferences(context, sysFileName);
        if (sp != null) {
            Map<String, ?> map = sp.getAll();
            if (map != null && map.size() > 0) {
                Set<String> set = map.keySet();
                String[] s = set.toArray(new String[set.size()]);
                Editor editor = sp.edit();
                editor.remove(s[0]);
                editor.apply();
            }
        }
    }

    /**
     * 根据指定KEY，到指定文件中查询对应的VALUE
     *
     * @param sysFileName 文件名称
     * @param key         k
     * @return 没有返回 null
     */
    public static String query(Context context, String sysFileName, String key) {
        SharedPreferences sp = createSharedPreferences(context, sysFileName);
        if (sp != null) {
            return sp.getString(key, null);
        }
        return null;
    }

    /**
     * 返回这个文件下面有多少对数据
     *
     * @param sysFileName 文件名
     * @return count
     */
    public static int queryCount(Context context, String sysFileName) {
        SharedPreferences sp = createSharedPreferences(context, sysFileName);
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
    public static Map<String, ?> query(Context context, String sysFileName) {
        SharedPreferences sp = createSharedPreferences(context, sysFileName);
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
    public static SharedPreferences createSharedPreferences(Context context, String sysName) {
        return context.getSharedPreferences(sysName, Activity.MODE_PRIVATE);
    }

    /**
     * 清空sysFileName指向的文件中的所有数据
     *
     * @param sysFileName 文件名称
     */
    public static void clear(Context context, String sysFileName) {
        Editor editor = createSharedPreferences(context, sysFileName).edit();
        if (editor == null) {
            notFindSysFile(sysFileName);
        } else {
            editor.clear();
            editor.apply();
        }
    }

    /**
     * 当DEBUG模式开启时进行打印
     *
     * @param sysFileName 文件名称
     */
    private static void notFindSysFile(String sysFileName) {
        LLog.w("没有找到【" + sysFileName + "】指向的数据文件！！！");
    }
}
