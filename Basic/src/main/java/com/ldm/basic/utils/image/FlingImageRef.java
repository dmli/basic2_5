package com.ldm.basic.utils.image;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldm on 16/5/4.
 * 使用列表是处于滑动时的ImageRef缓存列表
 */
class FlingImageRef {

    List<ImageOptions> refs = new ArrayList<>();

    public void put(String key, ImageOptions r) {
        // 如果有重复的任务，先做remove
        int len = refs.size();
        for (int i = len - 1; i >= 0; i--) {
            if (key.equals(refs.get(i).pId)) {
                refs.remove(i);
                break;
            }
        }
        // 新的任务保持加载最后
        refs.add(r);
    }

    void removeFirst() {
        refs.remove(0);
    }

    public void clear() {
        refs.clear();
    }

    public int size() {
        return refs.size();
    }

}
