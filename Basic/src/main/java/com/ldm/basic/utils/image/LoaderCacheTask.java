package com.ldm.basic.utils.image;

import com.ldm.basic.utils.FileTool;

/**
 * Created by ldm on 16/5/16.
 * 用来loader本地图像使用的
 */
public class LoaderCacheTask implements Runnable {

    private ImageOptions ref;
    private String filePath;
    private LazyImageDownloader lazy;

    public LoaderCacheTask(ImageOptions ref, String filePath, String cacheName, LazyImageDownloader lazy) {
        super();
        this.ref = ref;
        this.filePath = filePath;
        this.lazy = lazy;
    }

    @Override
    public void run() {
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
            lazy.createImage(ref, filePath);
            // 任务完成后删除
            lazy.removePid(ref);

            // 检查图片是否可以使用，如果可以发送200通知
            if (ref.loadSuccess) {
                lazy.sendMessage(DisplayUIPresenter.LOADER_IMAGE_SUCCESS, ref);
            } else {
                FileTool.delete(filePath);// 删除本地文件
                lazy.sendMessage(DisplayUIPresenter.LOADER_IMAGE_ERROR, ref);
            }
        } catch (Exception e) {
            lazy.removePid(ref);
            lazy.sendMessage(DisplayUIPresenter.LOADER_IMAGE_ERROR, null);
            e.printStackTrace();
        }
    }
}
