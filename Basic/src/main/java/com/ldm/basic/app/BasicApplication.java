package com.ldm.basic.app;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.ldm.basic.shared.BasicSharedPreferencesHelper;
import com.ldm.basic.utils.AES;
import com.ldm.basic.utils.Base64;
import com.ldm.basic.utils.SystemTool;

import java.io.Serializable;

/**
 * Created by ldm on 12-11-8. 基础的全局变量且包含了一个可实时缓存的Serializable（CLIENT_CACHE）对象，
 * 设置后将配合BasicActivity与BasicFragmentActivity自动缓存，通过getClientCache()获得缓存的对象
 * <p/>
 * -------------------
 * 如果不能在AndroidManifest.xml中设置这个BasicApplication时，可以直接使用BasicApplication.initGlobalCacheListener(Context)方法初始化
 */
public abstract class BasicApplication extends Application implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static BasicApplication app;

    /**
     * 客户端启动状态， true客户端启动， false客户端已经退出
     * 改变量的状态由SystemTool控制，如果SystemTool没有被正确使用，改变量将无法保证正确性
     */
    public static boolean CLIENT_START_STATE;

    /**
     * 常量字符串
     */
    public static final ConstantPool CONSTANTS = new ConstantPool();

    /**
     * DEBUG调试模式将会输出客户端的网络请求数据 默认不开启
     */
    public static boolean IS_DEBUG;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    /**
     * 返回全局的Application对象
     *
     * @return BasicApplication
     */
    public static BasicApplication getApp() {
        return app;
    }


    /**
     * 设置是否开启DEBUG模式
     *
     * @param isDebug true开启
     */
    public static void setModeToDebug(boolean isDebug) {
        IS_DEBUG = isDebug;
    }


    /**
     * 读取AndroidManifest中的meta_data属性
     *
     * @param key AndroidManifest
     * @return value
     */
    public Object getApplicationMetaData(String key) {
        Object result = null;
        try {
            ApplicationInfo ai = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
            if (ai != null && ai.metaData != null && ai.metaData.containsKey(key)) {
                result = ai.metaData.get(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 读取AndroidManifest中Activity的meta_data属性
     *
     * @param activity           key
     * @param componentName Activity.getComponentName()
     * @param key           key
     * @return Object
     */
    public Object getActivityMetaData(Activity activity, ComponentName componentName, String key) {
        Object result = null;
        try {
            ComponentName cn = componentName == null ? activity.getComponentName() : componentName;
            ActivityInfo ai = this.getPackageManager().getActivityInfo(cn, PackageManager.GET_META_DATA);
            if (ai != null && ai.metaData != null && ai.metaData.containsKey(key)) {
                result = ai.metaData.get(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 保存用户信息
     *
     * @param context Context
     * @param obj     需要保存的对象
     */
    public static void saveUserInfoToLocal(Context context, Object obj) {
        if (obj != null) {
            String cache = SystemTool.getGson().toJson(obj);
            String base64Result = null;
            try {
                base64Result = Base64.encodeToString(cache, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (base64Result == null) {
                // base64加密失败，存储明文
                BasicSharedPreferencesHelper.put(context, Configuration.USER_LOGIN_CACHE_FILE, "type", "0");// 0=明文
                // 1=base64
                // 2=base64+aes
                BasicSharedPreferencesHelper.put(context, Configuration.USER_LOGIN_CACHE_FILE, "cache1", cache);
            } else {
                String aesResult = AES.encrypt(base64Result, "f9277c7c760b4e91a07e62930b92b71b");
                if (aesResult == null) {
                    // aes加密失败，存储base64密文
                    BasicSharedPreferencesHelper.put(context, Configuration.USER_LOGIN_CACHE_FILE, "type", "1");// 0明文
                    // 1base64
                    // 2base64+aes
                    BasicSharedPreferencesHelper.put(context, Configuration.USER_LOGIN_CACHE_FILE, "cache1", base64Result);
                } else {
                    // 存储base64+aes密文
                    BasicSharedPreferencesHelper.put(context, Configuration.USER_LOGIN_CACHE_FILE, "type", "2");// 0明文
                    // 1base64
                    // 2base64+aes
                    BasicSharedPreferencesHelper.put(context, Configuration.USER_LOGIN_CACHE_FILE, "cache1", aesResult);
                }
            }
        }
    }

    /**
     * 获取登陆用户信息
     *
     * @param context  Context
     * @param classOfT 类型
     * @return <T>
     */
    public static <T> T getUserInfoFromLocal(Context context, Class<T> classOfT) {
        String type = BasicSharedPreferencesHelper.query(context, Configuration.USER_LOGIN_CACHE_FILE, "type");// 加密类型
        String data = BasicSharedPreferencesHelper.query(context, Configuration.USER_LOGIN_CACHE_FILE, "cache1");// data
        if (type == null || data == null) {
            return null;// 没有用户登陆信息
        }
        String result = null;
        if ("0".equals(type)) {// 明文
            result = data;
        } else if ("1".equals(type)) {// base64
            result = Base64.decodeToString(data, Base64.DEFAULT);
        } else if ("2".equals(type)) {// base64+ase
            try {
                result = AES.decrypt(data, "f9277c7c760b4e91a07e62930b92b71b");
                if (result != null) {
                    result = Base64.decodeToString(result, Base64.DEFAULT);
                }
            } catch (Exception e) {
                result = null;
                e.printStackTrace();
            }
        }
        if (result == null) {
            return null;// 没有用户登陆信息
        } else {
            return SystemTool.getGson().fromJson(result, classOfT);
        }
    }

    /**
     * 获取登陆用户信息
     *
     * @param context Context
     * @return json串
     */
    public static String getUserInfoFromLocal(Context context) {
        String type = BasicSharedPreferencesHelper.query(context, Configuration.USER_LOGIN_CACHE_FILE, "type");// 加密类型
        String data = BasicSharedPreferencesHelper.query(context, Configuration.USER_LOGIN_CACHE_FILE, "cache1");// data
        if (type == null || data == null) {
            return null;// 没有用户登陆信息
        }
        String result = null;
        if ("0".equals(type)) {// 明文
            result = data;
        } else if ("1".equals(type)) {// base64
            result = Base64.decodeToString(data, Base64.DEFAULT);
        } else if ("2".equals(type)) {// base64+ase
            try {
                result = AES.decrypt(data, "f9277c7c760b4e91a07e62930b92b71b");
                if (result != null) {
                    result = Base64.decodeToString(result, Base64.DEFAULT);
                }
            } catch (Exception e) {
                result = null;
                e.printStackTrace();
            }
        }
        if (result == null) {
            return null;// 没有用户登陆信息
        } else {
            return result;
        }
    }

    /**
     * 清除登陆缓存信息
     *
     * @param context Context
     */
    public static void clearUserInfo(Context context) {
        BasicSharedPreferencesHelper.clear(context, "user_login_cache_file");
    }

    /**
     * 异步完成结束后的回调函数，SecurityHandler及Asynchronous接口的任务处理
     *
     * @param what 标识，用户可以用该标识来区分任务
     * @param obj  Asynchronous接口中async方法的返回参数
     */
    public void handleMessage(int what, Object obj) {

    }
}
