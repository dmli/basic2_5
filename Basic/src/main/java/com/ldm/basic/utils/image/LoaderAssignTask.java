package com.ldm.basic.utils.image;

import com.ldm.basic.app.BasicRuntimeCache;
import com.ldm.basic.utils.TaskThreadToMultiService;

import java.io.File;

/**
 * Created by ldm on 16/5/16.
 * 分配任务的TaskThreadToMultiService.Task
 */
public class LoaderAssignTask extends TaskThreadToMultiService.Task{

    private ImageOptions ref;
    private LazyImageDownloader lazy;

    public LoaderAssignTask(ImageOptions ref, LazyImageDownloader lazy) {
        super();
        this.ref = ref;
        this.lazy = lazy;
    }

    @Override
    public void taskStart(Object... obj) {
        if (ref == null) {
            return;
        }
        if (!lazy.isStart) {
            lazy.removePid(ref);
            return;
        }

        // 验证图像是否已经下载过，如果下载过使用addCacheTask(ImageOptions, String)方法创建任务
        final String cacheName = lazy.getCacheName(ref);
        if (!BasicRuntimeCache.IMAGE_PATH_CACHE.containsKey(cacheName)) {// 检查本地是否有文件
            File f = new File(ref.filePath);
            if (f.exists()) {
                BasicRuntimeCache.IMAGE_PATH_CACHE.put(cacheName, ref.filePath);
            }
        }
        // 如果URL直接对应本地文件
        if (ref.isLocalImage()) {
            if (new File(ref.url).exists()) {
                // 使用缓存任务处理
                lazy.addCacheTask(ref, ref.url, cacheName);
            }
            lazy.removePid(ref);
        } else {
            String path = BasicRuntimeCache.IMAGE_PATH_CACHE.get(cacheName);
            if (BasicRuntimeCache.IMAGE_PATH_CACHE.containsKey(cacheName) && path != null && new File(path).exists()) {
                if (ref.downloadMode) {
                    lazy.sendMessage(LazyImageDownloader.LOADER_IMAGE_EXECUTE_END, ref);
                    lazy.removePid(ref);
                } else {
                    // 使用缓存任务处理
                    lazy.addCacheTask(ref, BasicRuntimeCache.IMAGE_PATH_CACHE.get(cacheName), cacheName);
                }
            } else {
                // 使用下载任务
                lazy.addDownloadTask(ref);
            }
        }
    }
}
