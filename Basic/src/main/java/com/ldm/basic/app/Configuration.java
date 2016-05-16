package com.ldm.basic.app;

import android.os.Environment;

import java.io.Serializable;

/**
 * Created by ldm on 12-11-8.
 * 基础的配置信息
 */
public class Configuration implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String IMAGE_CACHE_PATH = Environment.getExternalStorageDirectory().getPath() + "/basic/cache/images";
    public static final String FILE_CACHE_PATH = Environment.getExternalStorageDirectory().getPath() + "/basic/cache/file";

    /**
     * 数据库配置文件名称，全名db.properties 默认的配置文件raw下
     */
    public static final String DB_CONFIG_FILE_NAME = "db";

    /**
     * 数据库名称对应的KEY
     */
    public static final String DB_NAME_KEY = "DB_NAME";

    /**
     * 数据库版本对应的KEY
     */
    public static final String DB_VERSION_KEY = "DB_VERSION";

    /**
     * 数据库创建和修改时的回调接口
     */
    public static final String DB_CALLBACK_KEY = "DB_CALLBACK";

    /**
     * 客户端的全局配置文件名称
     */
    public static final String SYS_CONFIG_FILE_NAME = "config";

    /**
     * GlobalCacheListener 实现类的KEY键，用来读取config文件中的值
     */
    public static final String SYS_CONFIG_GLOBAL_CACHE_LISTENER_KEY = "GLOBAL_CACHE_LISTENER";

    /**
     * 用户登陆信息缓存文件名
     */
    public static final String USER_LOGIN_CACHE_FILE = "user_login_cache_file";

    /**
     * 客户端共享缓存文件名
     */
    public static final String CLIENT_SHARED_CACHE_FILE = "sys_client_shared_cache_file";

    /**
     * 客户端用来存储临时Bundle的 KEY
     */
    public static final String CLIENT_CACHE_KEY = "_BASIC_CLIENT_CACHE_KEY_";

}
