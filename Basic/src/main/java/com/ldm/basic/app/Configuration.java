package com.ldm.basic.app;

import java.io.Serializable;

import android.os.Environment;

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
     * 全名service.properties 默认的配置文件，当内存出现问题时 尝试读取配置文件中的备用信息
     */
    public static final String DEF_SERVICE_FILE_NAME = "service";

    /**
     * 对应url的KEY
     */
    public static final String DEF_SERVICE_URL_KEY = "SERVICE_URL";

    /**
     * 用户登陆信息缓存文件名
     */
    public static final String USER_LOGIN_CACHE_FILE = "user_login_cache_file";

    public static final String SYS_APP_CONFIG_CACHE_FILE = "SYS_APP_CONFIG_CACHE_FILE";

    /**
     * 客户端缓存文件名-BasicActivity中的saveCache方法使用的就是CLIENT_INFO_CACHE_FILE
     */
    public static final String CLIENT_INFO_CACHE_FILE = "client_info_cache_file";

    /**
     * 客户端用来存储临时Bundle的 KEY
     */
    public static final String CLIENT_CACHE_KEY = "_BASIC_CLIENT_CACHE_KEY_";

}
