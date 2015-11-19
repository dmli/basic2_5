package com.ldm.basic.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ldm.basic.db.BasicSQLiteOpenHelper;
import com.ldm.basic.properties.PropertiesHelper;
import com.ldm.basic.shared.SharedPreferencesHelper;
import com.ldm.basic.utils.AES;
import com.ldm.basic.utils.Base64;
import com.ldm.basic.utils.SystemTool;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Properties;

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
     * 全局的缓存状态监听接口，
     * 可以用来监听app内所有activity的onSaveInstanceState及onRestoreInstanceState方法
     */
    public static GlobalCacheListener globalCacheListener;

    /**
     * 客户端启动状态， true客户端启动， false客户端已经退出
     * 改变量的状态由SystemTool控制，如果SystemTool没有被正确使用，改变量将无法保证正确性
     */
    public static boolean CLIENT_START_STATE;

    /**
     * 客户端缓存，通过getClientCache()获取 ，setClientCache（Serializable）设置，
     * BasicActivity包中通过getBundle（Bundle）获取
     */
    public static Serializable CLIENT_CACHE;

    /**
     * ACTIVITY间参数传递统一KEY
     */
    public static final String INTENT_PARAMETER_KEY = "intent_cmd";

    /**
     * ACTIVITY间参数传递DATA数据时使用
     */
    public static final String INTENT_PARAMETER_DATA = "data";

    /**
     * ACTIVITY间数据返回统一KEY
     */
    public static final String INTENT_RESULT_KEY = "intent_result";

    /**
     * ACTIVITY间启动的标准code
     */
    public static final int INTENT_REQUEST_CODE = 100001;

    /**
     * ACTIVITY间返回的标准code
     */
    public static final int INTENT_RESULT_CODE = 100002;

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
        // 尝试检查是否有配置了GlobalCacheListener接口
        initGlobalCacheListener(getApplicationContext());
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
     * 获取一个需要缓存的Bundle对象
     *
     * @param bundle sBundle
     */
    public static void getBundle(Bundle bundle) {
        if (CLIENT_CACHE != null) {
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putSerializable(Configuration.CLIENT_CACHE_KEY, CLIENT_CACHE);
        }
    }

    /**
     * 缓存Serializable对象将通过该方法进行恢复
     *
     * @param obj 需要缓存的对象
     */
    public static void setClientCache(Serializable obj) {
        if (obj != null) {
            CLIENT_CACHE = obj;
        }
    }

    /**
     * 获取客户端缓存
     *
     * @return Serializable
     */
    public static Serializable getClientCache() {
        return CLIENT_CACHE;
    }

    /**
     * 读取AndroidManifest中的meta_data属性
     *
     * @param key AndroidManifest
     * @return value
     */
    public Object getMetaData(String key) {
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
     * 保存用户信息
     *
     * @param context Context
     * @param obj     需要保存的对象
     */
    public static void saveUserInfoToLocal(Context context, Object obj) {
        if (obj != null) {
            SharedPreferencesHelper sph = new SharedPreferencesHelper(context);
            String cache = SystemTool.getGson().toJson(obj);
            String base64Result = null;
            try {
                base64Result = Base64.encodeToString(cache, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (base64Result == null) {
                // base64加密失败，存储明文
                sph.put(Configuration.USER_LOGIN_CACHE_FILE, "type", "0");// 0=明文
                // 1=base64
                // 2=base64+aes
                sph.put(Configuration.USER_LOGIN_CACHE_FILE, "cache1", cache);
            } else {
                String aesResult = AES.encrypt(base64Result, "f9277c7c760b4e91a07e62930b92b71b");
                if (aesResult == null) {
                    // aes加密失败，存储base64密文
                    sph.put(Configuration.USER_LOGIN_CACHE_FILE, "type", "1");// 0明文
                    // 1base64
                    // 2base64+aes
                    sph.put(Configuration.USER_LOGIN_CACHE_FILE, "cache1", base64Result);
                } else {
                    // 存储base64+aes密文
                    sph.put(Configuration.USER_LOGIN_CACHE_FILE, "type", "2");// 0明文
                    // 1base64
                    // 2base64+aes
                    sph.put(Configuration.USER_LOGIN_CACHE_FILE, "cache1", aesResult);
                }
            }
        }
        // 当用户数据发生变化时，尝试更新可变化的表
        BasicSQLiteOpenHelper db = BasicSQLiteOpenHelper.getInstance(context);
        db.createVariableTable(context);
    }

    /**
     * 获取登陆用户信息
     *
     * @param context  Context
     * @param classOfT 类型
     * @return <T>
     */
    public static <T> T getUserInfoFromLocal(Context context, Class<T> classOfT) {
        SharedPreferencesHelper sph = new SharedPreferencesHelper(context);
        String type = sph.query(Configuration.USER_LOGIN_CACHE_FILE, "type");// 加密类型
        String data = sph.query(Configuration.USER_LOGIN_CACHE_FILE, "cache1");// data
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
        SharedPreferencesHelper sph = new SharedPreferencesHelper(context);
        String type = sph.query(Configuration.USER_LOGIN_CACHE_FILE, "type");// 加密类型
        String data = sph.query(Configuration.USER_LOGIN_CACHE_FILE, "cache1");// data
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
        SharedPreferencesHelper sph = new SharedPreferencesHelper(context);
        sph.clear("user_login_cache_file");
    }

    /**
     * 全局的缓存状态监听接口，
     * 设置后将会在app内所有activity的onSaveInstanceState及onRestoreInstanceState方法中进行监听
     * 设置该监听后将会在所有的activity中触发接口内的方法
     *
     * @param globalCacheListener GlobalCacheListener
     */
    public static void setGlobalCacheListener(GlobalCacheListener globalCacheListener) {
        BasicApplication.globalCacheListener = globalCacheListener;
    }

    /**
     * 如果配置了config.properties， 将从GLOBAL_CACHE_LISTENER属性中寻找可用的可用的实体并执行映射
     */
    public static void initGlobalCacheListener(Context context) {
        if (globalCacheListener != null || context == null) {
            return;
        }
        Properties p = PropertiesHelper.loadProperties(context, Configuration.SYS_CONFIG_FILE_NAME, "raw", context.getPackageName());
        if (p != null) {
            String cs = p.getProperty(Configuration.SYS_CONFIG_GLOBAL_CACHE_LISTENER_KEY, null);
            if (cs != null) {
                try {
                    Class<?> c = Class.forName(cs);
                    GlobalCacheListener gcl = (GlobalCacheListener) c.newInstance();
                    if (gcl != null) {
                        setGlobalCacheListener(gcl);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 异步完成结束后的回调函数，SecurityHandler及Asynchronous接口的任务处理
     *
     * @param what 标识，用户可以用该标识来区分任务
     * @param obj  Asynchronous接口中async方法的返回参数
     */
    public void handleMessage(int what, Object obj) {

    }

    /**
     * 相对安全的Handler
     */
    public SecurityHandler<BasicApplication> securityHandler = new SecurityHandler<>(this);

    protected static class SecurityHandler<T extends BasicApplication> extends Handler {
        WeakReference<T> w;

        private SecurityHandler(T t) {
            w = new WeakReference<>(t);
        }

        @Override
        public void handleMessage(Message msg) {
            if (w != null) {
                BasicApplication t = w.get();
                if (t != null) {
                    t.handleMessage(msg.what, msg.obj);
                }
            }
        }
    }
}
