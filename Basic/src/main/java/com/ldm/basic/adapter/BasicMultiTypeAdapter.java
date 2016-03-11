package com.ldm.basic.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;

import com.ldm.basic.utils.image.LazyImageDownloader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldm on 14-3-5.
 * 基础的适配器模版BasicAdapter的共享版，使用外界的List<T>结果集地址，可以达到多适配器共享结果集的效果
 */
public abstract class BasicMultiTypeAdapter<T extends BasicMultiTypeAdapter.BasicMultiTypeBean> extends BaseAdapter {

    protected List<T> data;
    protected LayoutInflater layoutInflater;
    private int animPosition;// 大于等于该位置的view需要增加动画
    private int oldPosition, oldCount;// 上一次初始化VIEW的位置，用来控制动画
    private boolean startTwoWayAnimation;
    private Handler handler;
    private int failCount;
    private BasicAdapterCacheManager cacheViewManager;

    public BasicMultiTypeAdapter(Context context, List<T> data) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
        notPlayAnimation();// 第一次初始化不开始动画
        this.oldCount = data.size();
        this.cacheViewManager = new BasicAdapterCacheManager();
    }

    /**
     * 使用者需要实现这个方法来返回LazyImageDownloader，默认null
     *
     * @return LazyImageDownloader
     */
    public LazyImageDownloader getLazyImageDownloader() {
        return null;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * 使用者可以重写这个方法来使用现有的Handler
     *
     * @return Handler
     */
    public Handler getHandler() {
        if (handler == null) {
            setHandler(new Handler());
        }
        return handler;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /**
         * 查询是否有缓存View可用
         */
        T f = getItem(position);
        View v = cacheViewManager.findCacheView(convertView, String.valueOf(f.getViewType()));
        /**
         * 编译View
         */
        return buildView(position, v, parent);
    }

    /**
     * 这个View代替了getView(...)方法
     *
     * @param position 位置
     * @param v        Convert View
     * @param parent   ViewGroup
     */
    protected abstract View buildView(int position, View v, ViewGroup parent);


    @Override
    public void notifyDataSetChanged() {
        notifyDataSetChanged(false);
    }

    public void notifyDataSetChanged(boolean closeAnim) {
        LazyImageDownloader d = getLazyImageDownloader();
        if (d != null) {
            d.setFailViewPosition((failCount <= 0 ? -1 : failCount));
        }
        super.notifyDataSetChanged();
        if (d != null || closeAnim) {
            getHandler().postDelayed(new RefreshRunnable(closeAnim), getDelayedTime());
        }
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
            this.data = new ArrayList<T>();
        }
        this.data.add(data);
    }

    /**
     * 向adapter中增加数据
     *
     * @param data List<T>
     */
    public void addAll(List<? extends T> data) {
        if (data == null) {
            return;
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

    protected View startAnim(View convertView, int position) {
        if (position >= animPosition) {
            Animation a;
            if (oldCount > getCount()) {// 数据集被增加
                setAnimPosition(oldPosition);
                a = getAnimationUp(convertView.getWidth(), getConvertViewHeight(convertView));
            } else if (oldCount < getCount()) {// 数据集减少，使用向上动画，但需要开发者设置AnimPosition
                a = getAnimationUp(convertView.getWidth(), getConvertViewHeight(convertView));
            } else {// 普通滑动，这里检查是否开了双向动画
                if (startTwoWayAnimation) {
                    if (oldPosition <= position) {// 向上
                        a = getAnimationUp(convertView.getWidth(), getConvertViewHeight(convertView));
                    } else {// 向下
                        a = getAnimationDown(convertView.getWidth(), getConvertViewHeight(convertView));
                    }
                } else {
                    a = getAnimationUp(convertView.getWidth(), getConvertViewHeight(convertView));
                }
            }
            if (a != null) {
                a.setAnimationListener(new MyAnimationListener(convertView));
                convertView.startAnimation(a);
            }
        }
        oldCount = getCount();
        oldPosition = position;
        return convertView;
    }

    private int getConvertViewHeight(View convertView) {
        int height;
        if ((height = convertView.getHeight()) <= 0) {
            height = convertView.getMeasuredHeight();
        }
        return height;
    }

    /**
     * 设置是否支持双向动画，默认不支持
     *
     * @param startTwoWayAnimation true/false
     */
    public void setStartTwoWayAnimation(boolean startTwoWayAnimation) {
        this.startTwoWayAnimation = startTwoWayAnimation;
    }

    public void stop() {

    }

    /**
     * 开发者可以重写这个方法实现自定义动画
     *
     * @param width  view宽度
     * @param height view高度
     * @return Animation
     */
    protected Animation getAnimationUp(int width, int height) {
        if (height > 0) {
            TranslateAnimation ta = new TranslateAnimation(0, 0, height, 0);
            ta.setDuration(300);
            return ta;
        }
        return null;
    }

    protected Animation getAnimationDown(int width, int height) {
        if (height > 0) {
            TranslateAnimation ta = new TranslateAnimation(0, 0, -height, 0);
            ta.setDuration(300);
            return ta;
        }
        return null;
    }

    /**
     * 设置本次刷新不播放动画
     */
    public void notPlayAnimation() {
        setAnimPosition(getCount());
    }

    public void setAnimPosition(int position) {
        animPosition = position;
    }

    public int getAnimPosition() {
        return animPosition;
    }

    /**
     * 动画
     */
    public class MyAnimationListener implements Animation.AnimationListener {

        View v;

        public MyAnimationListener(View v) {
            this.v = v;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (v != null) {
                v.clearAnimation();
            }
        }
    }

    /**
     * ViewHolder
     */
    public abstract class BasicViewHolder {

        public abstract View buildView(int position, T t, ViewGroup parent, View convertView);
    }

    /**
     * 使用这个适配器
     */
    public abstract static class BasicMultiTypeBean {

        public String viewType;

        public abstract String getViewType();
    }

    /**
     * 刷新数据是用来控制动画的Runnable
     */
    public class RefreshRunnable implements Runnable {
        boolean close;

        public RefreshRunnable(boolean close) {
            this.close = close;
        }

        @Override
        public void run() {
            if (getLazyImageDownloader() != null) {
                getLazyImageDownloader().setFailViewPosition(-1);
            }
            if (close) {
                notPlayAnimation();
            }
        }
    }
}
