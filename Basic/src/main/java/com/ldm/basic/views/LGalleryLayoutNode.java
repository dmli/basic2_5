package com.ldm.basic.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ldm.basic.adapter.BasicPagerAdapter;
import com.ldm.basic.utils.MeasureHelper;

/**
 * Created by ldm on 15/11/12.
 * 这个RelativeLayout将会把事件全部移交到child上
 * 使用方法：
 * galleryView = (GalleryLayoutNode) getView(R.id.galleryView);
 * galleryView.setViewPagerSize((int) (SystemTool.getSysScreenWidth(this) / 4.0f), 300);
 * final GalleryViewPager viewPager = galleryView.getViewPager();
 *
 * <com.miyou.myviewpagerproject.views.GalleryLayoutNode
 *    xmlns:android="http://schemas.android.com/apk/res/android"
 *    android:id="@+id/galleryView"
 *    android:layout_width="match_parent"
 *    android:layout_centerInParent="true"
 *    android:layout_height="wrap_content"
 *    android:background="@color/white"
 *    android:clipChildren="false"
 *    android:layerType="software">
 *
 * </com.miyou.myviewpagerproject.views.GalleryLayoutNode>
 *
 *
 */
public class LGalleryLayoutNode extends RelativeLayout implements ViewPager.OnPageChangeListener {


    private LGalleryViewPager viewPager;
    private ImageView floatView;
    private int viewPagerWidth = 0, heightPagerHeight = 0;


    public LGalleryLayoutNode(Context context) {
        super(context);
    }

    public LGalleryLayoutNode(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LGalleryLayoutNode(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LGalleryViewPager getViewPager() {
        return viewPager;
    }

    /**
     * 创建ViewPager并加入到GalleryLayoutNode的第一个child上
     *
     * @param context Context
     */
    private void createViewPager(Context context) {
        viewPager = new LGalleryViewPager(context);
        LayoutParams lp = new LayoutParams(viewPagerWidth, heightPagerHeight);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        viewPager.setLayoutParams(lp);
        viewPager.setClipChildren(false);
        viewPager.setFadingEdgeLength(0);
        viewPager.setOverScrollMode(OVER_SCROLL_NEVER);
        viewPager.setVerticalScrollBarEnabled(false);
        viewPager.setFlingScroll(true);
        //将viewPager加入到第一个View中
        addView(viewPager, 0);
    }

    /**
     * 设置viewPager单个View的宽度
     *
     * @param width  宽度单位PX
     * @param height 高度单位PX
     */
    public void setViewPagerSize(int width, int height) {
        if (viewPager != null) {
            MeasureHelper.resetSize(viewPager, width, height);
        } else {
            viewPagerWidth = width;
            heightPagerHeight = height;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        /**
         * 创建ViewPager
         */
        createViewPager(getContext());

        /**
         * 如果加上ViewPager有两个child的话，创建一个floatView并设置OnPageChangeListener事件
         */
        if (getChildCount() == 2) {
            floatView = (ImageView) getChildAt(1);
            if (floatView != null) {
                viewPager.setOnPageChangeListener(this);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return viewPager.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewPager.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return viewPager.onTouchEvent(event);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffset == 0) {
            if (floatView.getVisibility() != VISIBLE) {
                int i = ((position) % viewPager.getChildCount());
                BasicPagerAdapter adapter = (BasicPagerAdapter) viewPager.getAdapter();
                View v = adapter.getItem(i);
                floatView.setBackgroundDrawable(v.getBackground());
                floatView.setVisibility(VISIBLE);
            }
        } else {
            floatView.setVisibility(GONE);
        }

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
