package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;


/**
 * Created by ldm on 13-6-5.
 * 使用AnimationDrawable做动画，使用的AnimationDrawable可以更好的兼容listView的headView及footerView
 */
public class LProgressBar extends ImageView {


    public LProgressBar(Context context) {
        super(context);
    }

    public LProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getDrawable() != null) {
            AnimationDrawable ad = (AnimationDrawable) this.getDrawable();
            if (ad != null && !ad.isRunning()) {
                ad.start();
            }
        } else {
            if (getBackground() != null) {
                AnimationDrawable ad = (AnimationDrawable) this.getBackground();
                if (ad != null && !ad.isRunning()) {
                    ad.start();
                }
            }
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }
}
