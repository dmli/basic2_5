package com.ldm.basic.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.VideoView;

/**
 * Created by ldm on 15/12/9.
 * 这个用来播放视频的SurfaceView
 */
public class LSurfaceView extends VideoView {
    public LSurfaceView(Context context) {
        super(context);
    }

    public LSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(){


    }
}
