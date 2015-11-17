package com.ldm.basic.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by ldm on 15/11/12.
 * 画廊，这里默认使用了Gallery3DPageTransformer动画控制器
 */
public class GalleryViewPager extends LViewPager {

    /**
     * 这个值用来控制倾斜角度
     */
    private static final float ROTATION_OFF_VALUE = -30f;

    public GalleryViewPager(Context context) {
        super(context);
        init();
    }

    public GalleryViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 初始化，这里默认设置了一个Gallery3DPageTransformer动画控制器，
     * 如果需要自定义动画可以调用setPageTransformer(...)
     */
    public void init() {
        this.setPageTransformer(true, new Gallery3DPageTransformer());
    }

    /**
     * 动画控制器，这里做了一个3D相册的动画
     */
    public class Gallery3DPageTransformer implements ViewPager.PageTransformer {

        public void transformPage(View view, float position) {

            /**
             * 计算缩放
             */

            final float rot = position * ROTATION_OFF_VALUE;
            ViewHelper.setRotationY(view, rot > 0 ? Math.min(rot, 70) : Math.max(rot, -70));

            /**
             * 计算位移距离
             */
            final int translationOffValue = (int) (-getWidth() * 0.21f);
            final float tran = position * translationOffValue;
            ViewHelper.setTranslationX(view, tran);

            /**
             * 计算缩放比例
             */
            final float s = Math.max(0.75f, 0.75f + (1 - Math.abs(position)) * 0.25f);
            ViewHelper.setScaleX(view, s);
            ViewHelper.setScaleY(view, s);

        }
    }

}
