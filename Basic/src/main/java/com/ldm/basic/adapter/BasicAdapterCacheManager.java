package com.ldm.basic.adapter;

import android.text.TextUtils;
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
    public static final int CACHE_TAG_ID = 0x59959599;

    /**
     * 缓存容器
     */
    final Map<String, List<View>> caches = new HashMap<>();

    /**
     * 向容器中添加缓存
     *
     * @param tag  标识
     * @param view cache View
     */
    private void putView(String tag, View view) {
        List<View> vs = caches.get(tag);
        if (vs == null) {
            vs = new ArrayList<>();
        }
        vs.add(view);
        caches.put(tag, vs);
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
            return vs.remove(0);
        }
        return null;
    }

    /**
     * 同步标记
     *
     * @param v   View
     * @param tag 标识，标识View类型的字符串
     */
    public static void syncTag(View v, String tag) {
        if (v != null && !TextUtils.isEmpty(tag)) {
            v.setTag(CACHE_TAG_ID, tag);
        }
    }

    /**
     * 根据tag查询是否有可用的Cache View，如果没有返回null
     *
     * @param view 需要cache的View
     * @param tag  类型
     * @return 如果没有可用的View直接返回
     */
    public View findCacheView(View view, String tag) {
        if (view != null && !TextUtils.isEmpty(String.valueOf(view.getTag(CACHE_TAG_ID)))) {
            String cacheViewTag = String.valueOf(view.getTag(CACHE_TAG_ID));
            if (tag.equals(cacheViewTag)) {
                //如果类型相同，直接返回
                return view;
            } else {
                //加入到缓存容器中
                putView(cacheViewTag, view);
            }
        }
        //查询是否有缓存可用
        return getView(tag);
    }

}
