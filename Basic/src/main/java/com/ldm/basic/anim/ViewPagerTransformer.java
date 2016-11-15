package com.ldm.basic.anim;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by ldm on 16/11/15.
 * ViewPager动画实体
 */

public class ViewPagerTransformer {

    /**
     * 滑动时 第二页有缩放效果
     */
    public static class DepthPageTransformer implements ViewPager.PageTransformer {

        private float scale = 0.75f;

        public DepthPageTransformer() {
        }

        public DepthPageTransformer(float scale) {
            this.scale = scale;
        }

        /**
         * Apply a property transformation to the given page.
         *
         * @param page     Apply the transformation to this page
         * @param position Position of page relative to the current front-and-center
         *                 position of the pager. 0 is front and center. 1 is one full
         */
        @Override
        public void transformPage(View page, float position) {
            int pageWidth = page.getWidth();
            if (position < -1) {//左边View将被隐藏
                ViewHelper.setAlpha(page, 0);
            } else if (position <= 0) {//右边View开始准备进场
                ViewHelper.setAlpha(page, 1 + position);
                ViewHelper.setTranslationX(page, 0);
                ViewHelper.setScaleX(page, 1);
                ViewHelper.setScaleY(page, 1);
            } else if (position <= 4) {
                ViewHelper.setAlpha(page, 1 - position);
                ViewHelper.setTranslationX(page, pageWidth * -position);
                float scaleFactor = scale + (1 - scale) * (1 - position);
                ViewHelper.setScaleX(page, scaleFactor);
                ViewHelper.setScaleY(page, scaleFactor);
            } else {
                ViewHelper.setAlpha(page, 1);
            }
        }
    }

    /**
     * 缩放效果，从小到大
     */
    public static class ScaleInTransformer implements ViewPager.PageTransformer {

        private static final float DEFAULT_CENTER = 0.5f;
        private float mMinScale = 0.85f;

        public ScaleInTransformer() {

        }

        public ScaleInTransformer(float minScale) {
            mMinScale = minScale;
        }

        /**
         * Apply a property transformation to the given page.
         *
         * @param page     Apply the transformation to this page
         * @param position Position of page relative to the current front-and-center
         *                 position of the pager. 0 is front and center. 1 is one full
         */
        @Override
        public void transformPage(View page, float position) {
            //复原一次
            page.setScaleX(1);
            int pageWidth = page.getWidth();
            int pageHeight = page.getHeight();

            //设置中心点
            page.setPivotY(pageHeight / 2);
            page.setPivotX(pageWidth / 2);
            if (position < -1) {
                page.setScaleX(mMinScale);
                page.setScaleY(mMinScale);
                page.setPivotX(pageWidth);
            } else if (position <= 1) {
                if (position < 0) {
                    float scaleFactor = (1 + position) * (1 - mMinScale) + mMinScale;
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                    page.setPivotX(pageWidth * (DEFAULT_CENTER + (DEFAULT_CENTER * -position)));
                } else {
                    float scaleFactor = (1 - position) * (1 - mMinScale) + mMinScale;
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                    page.setPivotX(pageWidth * ((1 - position) * DEFAULT_CENTER));
                }
            } else {
                page.setPivotX(0);
                page.setScaleX(mMinScale);
                page.setScaleY(mMinScale);
            }
        }
    }

    /**
     * 根据Y轴旋转
     */
    public static class RotateYTransformer implements ViewPager.PageTransformer {
        private static final float DEFAULT_CENTER = 0.5f;
        private float mMaxRotate = 35f;

        public RotateYTransformer() {
        }

        public RotateYTransformer(float maxRotate) {
            mMaxRotate = maxRotate;
        }

        /**
         * Apply a property transformation to the given page.
         *
         * @param page     Apply the transformation to this page
         * @param position Position of page relative to the current front-and-center
         *                 position of the pager. 0 is front and center. 1 is one full
         */
        @Override
        public void transformPage(View page, float position) {
            //复原一次
            page.setScaleX(1);

            page.setPivotY(page.getHeight() / 2);
            if (position < -1) {
                page.setRotationY(-1 * mMaxRotate);
                page.setPivotX(page.getWidth());
            } else if (position <= 1) {
                page.setRotationY(position * mMaxRotate);
                if (position < 0) {
                    page.setPivotX(page.getWidth() * (DEFAULT_CENTER + DEFAULT_CENTER * (-position)));
                    page.setPivotX(page.getWidth());
                } else {
                    page.setPivotX(page.getWidth() * DEFAULT_CENTER * (1 - position));
                    page.setPivotX(0);
                }
            } else {
                page.setRotationY(1 * mMaxRotate);
                page.setPivotX(0);
            }
        }
    }
}
