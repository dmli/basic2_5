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
     * 用户登陆信息缓存文件名
     */
    public static final String USER_LOGIN_CACHE_FILE = "user_login_cache_file";

    /**
     * 客户端共享缓存文件名
     */
    public static final String CLIENT_SHARED_CACHE_FILE = "sys_client_shared_cache_file";

}
