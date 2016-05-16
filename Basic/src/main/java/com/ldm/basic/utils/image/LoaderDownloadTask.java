package com.ldm.basic.utils.image;

import com.ldm.basic.utils.TaskThreadToMultiService;

/**
 * Created by ldm on 16/5/16.
 * 处理下载任务的TaskThreadToMultiService.Task
 */
public class LoaderDownloadTask extends TaskThreadToMultiService.Task {

    private ImageOptions ref;
    private LazyImageDownloader lazy;

    public LoaderDownloadTask(ImageOptions ref, LazyImageDownloader lazy) {
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
        try {
            if (!ref.downloadMode) {
                if (!lazy.checkAvailability(ref)) {
                    lazy.removePid(ref);
                    return;
                }
            }
            // 创建下载任务
            lazy.createDownloadTask(ref);
        } catch (Exception e) {
            // 任务完成后删除
            lazy.removePid(ref);
            lazy.sendMessage(LazyImageDownloader.LOADER_IMAGE_ERROR, null);
            e.printStackTrace();
        }
    }
}
