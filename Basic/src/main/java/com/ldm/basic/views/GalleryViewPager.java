package com.ldm.basic.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by ldm on 15/11/12.
 * 画册
 */
public class GalleryViewPager extends LViewPager {


    public GalleryViewPager(Context context) {
        super(context);
        init();
    }

    public GalleryViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public void init() {
        this.setPageTransformer(true, new DepthPageTransformer());
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        /**
         * 这个值用来控制倾斜角度
         */
        final float ROTATION_OFF_VALUE = -30f;

        public void transformPage(View view, float position) {
            ViewHelper.setRotationY(view, position * ROTATION_OFF_VALUE);
            final float tran = position * ROTATION_OFF_VALUE;
            ViewHelper.setTranslationX(view, tran);
        }
    }

}
