package com.ldm.basic.utils.image;

import com.ldm.basic.utils.FileTool;
import com.ldm.basic.utils.TaskThreadToMultiService;

/**
 * Created by ldm on 16/5/16.
 * 加载缓存图像的TaskThreadToMultiService.Task
 */
public class LoaderCacheTask extends TaskThreadToMultiService.Task {

    private ImageOptions ref;
    private String filePath;
    private String cacheName;
    private LazyImageDownloader lazy;

    public LoaderCacheTask(ImageOptions ref, String filePath, String cacheName, LazyImageDownloader lazy) {
        super();
        this.ref = ref;
        this.filePath = filePath;
        this.cacheName = cacheName;
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
        try {
            if (!lazy.checkAvailability(ref)) {
                lazy.removePid(ref);
                return;
            }
            // 创建图像
            lazy.createImage(ref, filePath, cacheName);
            // 任务完成后删除
            lazy.removePid(ref);

            // 检查图片是否可以使用，如果可以发送200通知
            if (ref.loadSuccess) {
                lazy.sendMessage(LazyImageDownloader.LOADER_IMAGE_SUCCESS, ref);
            } else {
                FileTool.delete(filePath);// 删除本地文件
                lazy.sendMessage(LazyImageDownloader.LOADER_IMAGE_ERROR, ref);
            }
        } catch (Exception e) {
            lazy.removePid(ref);
            lazy.sendMessage(LazyImageDownloader.LOADER_IMAGE_ERROR, null);
            e.printStackTrace();
        }
    }
}
