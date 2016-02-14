package com.ldm.basic.adapter;

import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ldm on 15/12/14.
 * Adapter缓存管理器
 */
public class BasicAdapterCacheManager {


    /**
     * 存储Convert View的类型
     */
    public static final int CACHE_TAG_ID = 0x59999599;

    /**
     * 缓存容器
     */
    Map<String, List<View>> caches = new HashMap<String, List<View>>();

    /**
     * 向容器中添加缓存
     *
     * @param tag  标识
     * @param view cache View
     */
    private void putView(String tag, View view) {
        List<View> vs = caches.get(tag);
        if (vs == null) {
            vs = new ArrayList<View>();
        }
        vs.add(view);
    }

    /**
     * 根据置顶的标识获取一个可用的View,如果没有返回null
     *
     * @param tag 标识
     * @return cache View
     */
    private View getView(String tag) {
        List<View> vs = caches.get(tag);
        if (vs != null && vs.size() > 0) {
            return vs.get(0);
        }
        return null;
    }


    /**
     * 根据tag查询是否有可用的Cache View，如果没有返回null
     *
     * @param convertView adapter中getView(...)中的convertView
     * @param tag         类型
     * @return 如果没有可用的View直接返回
     */
    public View findCacheView(View convertView, String tag) {
        if (convertView == null) {
            return null;
        }
        String viewTag = String.valueOf(convertView.getTag(CACHE_TAG_ID));
        /**
         * 如果类型相同，直接返回
         */
        if (tag.equals(viewTag)) {
            return convertView;
        }

        /**
         * 查询是否有返回
         */
        View cacheView = getView(tag);

        /**
         * 加入到缓存容器中
         */
        convertView.setTag(CACHE_TAG_ID, tag);
        putView(tag, convertView);

        return cacheView;
    }

}
