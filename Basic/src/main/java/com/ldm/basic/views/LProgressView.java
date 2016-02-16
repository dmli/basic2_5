package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * Created by ldm on 16/2/16.
 * 旋转的进度条，使用RotateAnimation动画
 */
public class LProgressView extends ImageView {

    private RotateAnimation ra;

    public LProgressView(Context context) {
        super(context);
        init();
    }

    public LProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ra = new RotateAnimation(0, 3600, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setInterpolator(new LinearInterpolator());
        ra.setRepeatCount(-1);
        ra.setDuration(8000);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (getAnimation() != null) {
            getAnimation().cancel();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getAnimation() == null) {
            startAnimation(ra);
        }
    }


}
