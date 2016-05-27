package com.ldm.basic.utils.image;

import com.ldm.basic.app.BasicRuntimeCache;
import com.ldm.basic.utils.FileTool;
import com.ldm.basic.utils.HttpFileTool;
import com.ldm.basic.utils.TaskThreadToMultiService;
import com.ldm.basic.utils.TextUtils;

import java.io.File;

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
            //下载状态
            String state = null;
            //下载后的路径
            String path = null;
            final String cacheName = lazy.getCacheName(ref);
            final String filePath = ref.filePath;
            File f = new File(filePath);
            /**
             * 如果文件存在且任务于maxIgnoreTime内创建，将忽略这个下载任务， 直接返回文件地址，这样可以过滤掉网络不稳定时导致文件重复下载的问题
             */
            if (f.exists() && System.currentTimeMillis() - f.lastModified() < 60000) {
                state = "200";
                path = filePath;
            } else {
                String url = ref.getUrl();
                if (!TextUtils.isNull(url) && url.startsWith("http")) {
                    String[] info = HttpFileTool.httpToFile2(url, filePath);
                    state = info[0];
                    path = info[1];
                }
            }
            if ("200".equals(state)) {
                fileDownloadSuccess(ref, 200, path, cacheName);
                // 任务完成后删除
                lazy.removePid(ref);
            } else {
                fileDownloadError(ref, state);
            }

        } catch (Exception e) {
            // 任务完成后删除
            lazy.removePid(ref);
            lazy.sendMessage(LazyImageDownloader.LOADER_IMAGE_ERROR, null);
            e.printStackTrace();
        }
    }

    /**
     * 文件下载成功后的逻辑
     *
     * @param ref          ImageOptions
     * @param responseCode 状态
     * @param path         本地地址
     * @param cacheName    缓存名
     */
    void fileDownloadSuccess(ImageOptions ref, int responseCode, String path, String cacheName) {
        ref.responseCode = responseCode;
        if (!ref.downloadMode) {
            // 创建图像
            lazy.createImage(ref, path, cacheName);
            // 检查图片是否可以使用，如果可以发送200通知
            if (ref.loadSuccess) {
                lazy.sendMessage(LazyImageDownloader.LOADER_IMAGE_SUCCESS, ref);
                // 下载成功后维护全局缓存
                BasicRuntimeCache.IMAGE_PATH_CACHE.put(cacheName, path);
            } else {
                // 图片不可用,删除本地文件
                FileTool.delete(path);
                lazy.sendMessage(LazyImageDownloader.LOADER_IMAGE_ERROR, ref);
            }
        } else {
            /**
             * 离线下载的任务会执行一次end()
             */
            lazy.sendMessage(LazyImageDownloader.LOADER_IMAGE_EXECUTE_END, ref);
        }
    }

    /**
     * 网络文件下载失败是执行的逻辑
     *
     * @param ref   ImageOptions
     * @param state 状态
     */
    void fileDownloadError(ImageOptions ref, String state) {
        // 离线任务如果下载失败，将不进行重新下载
        if (!ref.downloadMode) {
            lazy.removePid(ref);
            if ("url is null".equals(state)) {
                lazy.sendMessage(LazyImageDownloader.LOADER_IMAGE_URL_IS_NULL, ref);//放弃任务，不在继续处理
            } else {
                ref.responseCode = TextUtils.parseInt(state, 0);
                if (state != null) {
                    if (state.contains("No space left on device")) {
                        lazy.sendMessage(LazyImageDownloader.LOADER_IMAGE_RECORD_LAST_TIME, null);// 内存不足
                    } else {
                        lazy.sendMessage(101, ref);//发送重新下载消息
                    }
                }
            }
        } else {
            //离线下载的任务会执行一次end()
            lazy.sendMessage(LazyImageDownloader.LOADER_IMAGE_EXECUTE_END, ref);
        }
    }
}
