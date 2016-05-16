package com.ldm.basic.app;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ldm on 14-3-31. Basic框架运行时的缓存及初始化缓存的方法
 */
public class BasicRuntimeCache implements Serializable {

    /**
     * 客户端启动后会根据预设的图片地址初始化图片索引
     */
    public static final Map<String, String> IMAGE_PATH_CACHE = new HashMap<>();

    /**
     * 清除已映射的图片索引
     */
    public synchronized static void clearImageIndex() {
        IMAGE_PATH_CACHE.clear();
    }
}
