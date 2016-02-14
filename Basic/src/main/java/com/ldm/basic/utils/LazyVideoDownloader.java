package com.ldm.basic.utils;

import android.view.SurfaceView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ldm on 15/12/9.
 * 下载视频
 */
public class LazyVideoDownloader {


    private Map<String, VideoRef> map = new HashMap<>();

    public void addTask(VideoRef videoRef) {


        startDownload(videoRef);
    }


    private void startDownload(VideoRef videoRef) {

        MultiThreadDownTool t = new MultiThreadDownTool();
        t.start(videoRef.url, videoRef.localPath, videoRef.fileName, new MultiThreadDownTool.ProgressCallback() {
            @Override
            public void success(String path) {

            }

            @Override
            public void progress(int index, long count, long current) {

            }

            @Override
            public void ioError(String error) {

            }

            @Override
            public void error(String error) {

            }
        }, 1);



    }


    public abstract class VideoRef extends MultiThreadDownTool.ProgressCallback {

        public String url;
        public String localPath;
        public String fileName;
        public SurfaceView view;

        public VideoRef(String url, String localPath, String fileName, SurfaceView view) {
            this.url = url;
            this.localPath = localPath;
            this.fileName = fileName;
            this.view = view;
        }

    }

}
