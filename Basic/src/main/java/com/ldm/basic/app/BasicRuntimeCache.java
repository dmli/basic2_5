package com.ldm.basic.app;

import com.ldm.basic.utils.Log;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ldm on 14-3-31. Basic框架运行时的缓存及初始化缓存的方法
 */
public class BasicRuntimeCache implements Serializable {

    /**
     * 客户端启动后会根据预设的图片地址初始化图片索引
     */
    public static final Map<String, String> IMAGE_PATH_CACHE = new HashMap<>();

    public static Map<String, String> SCAN_IMAGE_PATH_CACHE = new HashMap<>();

    /**
     * 初始化指定目录下的图片索引，多个目录可以调用对次，如果有同名的图片将会覆盖前一个
     *
     * @param imagePath 图片缓存路径
     */
    public synchronized static void initImageIndex(final String imagePath) {
        // 初始SD卡中缓存图片信息
        File f = new File(imagePath);
        if (!f.isDirectory()) {
            if (f.mkdirs()) {
                Log.e("图像缓存路径初始化完成");
            }
        } else {
            if (IMAGE_PATH_CACHE.size() <= 0) {
                File[] fs = f.listFiles();
                if (fs != null) {
                    for (File file : fs) {
                        if (file.isFile()) {
                            SCAN_IMAGE_PATH_CACHE.put(file.getName(), file.getAbsolutePath());
                        }
                        if (SCAN_IMAGE_PATH_CACHE.size() > 100) {
                            synchronized (IMAGE_PATH_CACHE) {
                                Set<String> set = SCAN_IMAGE_PATH_CACHE.keySet();
                                for (String s : set) {
                                    IMAGE_PATH_CACHE.put(s, SCAN_IMAGE_PATH_CACHE.get(s));
                                }
                            }
                            SCAN_IMAGE_PATH_CACHE.clear();
                        }
                    }
                    synchronized (IMAGE_PATH_CACHE) {
                        Set<String> set = SCAN_IMAGE_PATH_CACHE.keySet();
                        for (String s : set) {
                            IMAGE_PATH_CACHE.put(s, SCAN_IMAGE_PATH_CACHE.get(s));
                        }
                    }
                    SCAN_IMAGE_PATH_CACHE.clear();
                }
            }
        }
    }

    /**
     * 清除已映射的图片索引
     */
    public synchronized static void clearImageIndex() {
        IMAGE_PATH_CACHE.clear();
    }
}
