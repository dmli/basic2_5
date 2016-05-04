package com.ldm.basic.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.ldm.basic.utils.image.LazyImageDownloader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldm on 14-3-5.
 * 基础的适配器模版BasicAdapter<T> 标准版，将不使用外界的List<? extends
 * T>地址,每次传入将从新创建一个结果集作为数据存储容器
 */
public abstract class BasicAdapterUnsharedMode<T> extends BaseAdapter {

    protected Context context;
    protected List<T> data;
    protected LayoutInflater layoutInflater;
    private int failCount;
    private Handler handler;

    public BasicAdapterUnsharedMode(Context context, List<? extends T> data) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.data = new ArrayList<>();
        this.data.addAll(data);
    }

    /**
     * 使用者需要实现这个方法来返回LazyImageDownloader，默认null
     *
     * @return LazyImageDownloader
     */
    public LazyImageDownloader getLazyImageDownloader() {
        return null;
    }

    /**
     * 使用者可以重写这个方法来使用现有的Handler
     *
     * @return Handler
     */
    public Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }


    @Override
    public void notifyDataSetChanged() {
        LazyImageDownloader d = getLazyImageDownloader();
        if (d != null) {
            d.setFailViewPosition((failCount <= 0 ? -1 : failCount));
            getHandler().postDelayed(new RefreshRunnable(), getDelayedTime());
        }
        super.notifyDataSetChanged();
    }

    /**
     * 返回延时时间，默认300
     *
     * @return time
     */
    public int getDelayedTime() {
        return 300;
    }

    @Override
    public int getCount() {
        return this.data == null ? 0 : this.data.size();
    }

    /**
     * 获取数据集合
     *
     * @return List<T>
     */
    public List<T> getData() {
        return data;
    }

    /**
     * 删除全部数据
     */
    public void removeAll() {
        if (this.data != null) {
            this.data.clear();
        }
    }

    /**
     * 根据位置删除数据中的数据项
     *
     * @param position 位置
     */
    public T remove(int position) {
        if (this.data != null && position >= 0 && position < this.data.size()) {
            return this.data.remove(position);
        }
        return null;
    }

    /**
     * 向adapter中增加数据
     *
     * @param data T
     */
    public void add(T data) {
        if (this.data == null) {
            this.data = new ArrayList<>();
        }
        this.data.add(data);
    }

    /**
     * 向adapter中增加数据
     *
     * @param data List<T>
     */
    public void addAll(List<? extends T> data) {
        if (data == null)
            return;
        if (this.data == null) {
            this.data = new ArrayList<>();
        }
        failCount = getCount();
        this.data.addAll(data);
    }

    @Override
    public T getItem(int position) {
        return (this.data == null || position < 0 || position >= this.data.size()) ? null : this.data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class RefreshRunnable implements Runnable {
        public RefreshRunnable() {
        }

        @Override
        public void run() {
            if (getLazyImageDownloader() != null) {
                getLazyImageDownloader().setFailViewPosition(-1);
            }
        }
    }
}
