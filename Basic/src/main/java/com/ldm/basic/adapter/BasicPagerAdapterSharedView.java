package com.ldm.basic.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldm on 15/11/16.
 * BasicPagerAdapter的翻版，这个版本将使用View共享机制
 *
 * 使用方法:
 * 与BaseAdapter相同，利用buildView(...)
 *
 */
public abstract class BasicPagerAdapterSharedView<T> extends PagerAdapter {

    private final List<View> cacheViews = new ArrayList<>();
    private List<T> data;


    public BasicPagerAdapterSharedView(List<T> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View v = (View) object;
        container.removeView(v);
        cacheViews.add(v);
    }

    public T getItem(int position) {
        return data == null ? null : data.get(position);
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
