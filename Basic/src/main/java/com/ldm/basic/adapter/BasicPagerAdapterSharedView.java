package com.ldm.basic.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldm on 15/11/16.
 * BasicPagerAdapter的翻版，这个版本将使用View共享机制
 * <p/>
 * 使用方法:
 * 与BaseAdapter相同，利用buildView(...)
 */
public abstract class BasicPagerAdapterSharedView<T> extends PagerAdapter {

    private final List<View> cacheViews = new ArrayList<>();
    private List<T> data;

    /**
     * 无限循环的最大周期数
     */
    private static final int CYCLE_NUMBER = 100;

    private boolean infiniteLoop;

    /**
     * 设置是否开启无限循环功能
     *
     * @param infiniteLoop true/false
     */
    public void setInfiniteLoop(boolean infiniteLoop) {
        this.infiniteLoop = infiniteLoop;
    }

    public BasicPagerAdapterSharedView(List<T> data) {
        this.data = data;
    }

    /**
     * 返回中心位置的第一页数据
     *
     * @param offPosition 偏移量
     * @return position
     */
    public int getFirstPosition(int offPosition) {
        if (getCount() <= 1) {
            return 0;
        }
        int center = getCount() / 2;
        return center - center % data.size() + offPosition;
    }

    @Override
    public int getCount() {
        if (data == null || data.size() < 0) {
            return 0;
        }
        if (data.size() == 1) {
            return 1;
        } else {
            return infiniteLoop ? data.size() * CYCLE_NUMBER : data.size();
        }
    }

    /**
     * 返回真实的索引位置
     *
     * @param position getView(position)
     * @return position
     */
    private int getRealPosition(int position) {
        return position % data.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View v = (View) object;
        container.removeView(v);
        cacheViews.add(v);
    }

    public T getItem(int position) {
        return data == null ? null : data.get(getRealPosition(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View convertView = null;
        if (cacheViews.size() > 0) {
            convertView = cacheViews.remove(0);
        }
        View v = buildView(container, convertView, position);
        container.addView(v);
        return v;
    }

    public abstract View buildView(ViewGroup container, View convertView, int position);


    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

}
