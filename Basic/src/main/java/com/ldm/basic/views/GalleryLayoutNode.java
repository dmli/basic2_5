package com.ldm.basic.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by ldm on 15/11/12.
 * 这个RelativeLayout将会把事件全部移交到child上，
 * 例：
 * <com.ldm.basic.views.GalleryLayoutNode
     xmlns:android="http://schemas.android.com/apk/res/android"
     android:layout_width="match_parent"
     android:layout_height="match_parent"
     android:clipChildren="false">

     <com.ldm.basic.views.GalleryViewPager
     android:id="@+id/viewPager"
     android:layout_width="150dp"
     android:layout_height="220dp"
     android:layout_centerInParent="true"
     android:clipChildren="false"
     android:fadingEdgeLength="0dp"
     android:overScrollMode="never"
     android:scrollbars="none"/>

 </com.ldm.basic.views.GalleryLayoutNode>

 viewPager.setOffscreenPageLimit(5);
 viewPager.setAdapter(new BasicPagerAdapter(vs));
 viewPager.setCurrentItem(1);

 *
 *
 */
public class GalleryLayoutNode extends RelativeLayout {


    private LViewPager childView;


    public GalleryLayoutNode(Context context) {
        super(context);
    }

    public GalleryLayoutNode(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GalleryLayoutNode(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        childView = (LViewPager) getChildAt(0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return childView.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return childView.onTouchEvent(event);
    }
}
