package com.ldm.basic.views.ctrl;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ListView;

import com.ldm.basic.adapter.BasicAdapter;

/**
 * Created by ldm on 14-8-7.
 * 给listView删除时增加动画
 */
public class LListViewRemoveHelper {

    private AbsListView absListView;
    private BasicAdapter<?> adapter;
    private Callback callback;// 完成反馈
    private boolean isBusy;// 是否处于繁忙状态

    public LListViewRemoveHelper(AbsListView absListView, BasicAdapter<?> adapter, Callback callback) {
        this.absListView = absListView;
        this.adapter = adapter;
        this.callback = callback;
    }

    /**
     * 设置新的适配器
     *
     * @param adapter BasicAdapter<?>
     */
    public void setAdapter(BasicAdapter<?> adapter) {
        this.adapter = adapter;
    }

    /**
     * 左侧移除动画
     *
     * @return true动画开始执行
     */
    public boolean removeToLeftOut() {
        if (isBusy) {
            return false;
        }
        isBusy = true;
        final int len = absListView.getChildCount();
        int offset = 0;
        if (absListView instanceof ListView) {
            offset = ((ListView) absListView).getHeaderViewsCount();
        }
        for (int i = offset; i < len; i++) {
            View v = absListView.getChildAt(i);
            if (v != null) {
                TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
                ta.setDuration(120);
                ta.setAnimationListener(new MyAnimationListener(v, i == len - 1));
                ta.setStartOffset(20 * i);
                v.startAnimation(ta);
            }
        }
        return true;
    }

    /**
     * 淡出动画
     *
     * @return true动画开始执行
     */
    public boolean removeToFadeOut() {
        if (isBusy) {
            return false;
        }
        isBusy = true;
        final int len = absListView.getChildCount();
        int offset = 0;
        if (absListView instanceof ListView) {
            offset = ((ListView) absListView).getHeaderViewsCount();
        }
        for (int i = offset; i < len; i++) {
            View v = absListView.getChildAt(i);
            if (v != null) {
                Animation ta = new AlphaAnimation(1.0f, 0.0f);
                ta.setDuration(120);
                ta.setAnimationListener(new MyAnimationListener(v, i == len - 1));
                ta.setStartOffset(20 * i);
                v.startAnimation(ta);
            }
        }
        return true;
    }

    /**
     * 内容自由下落
     *
     * @return true动画开始执行
     */
    public boolean removeToPlummet() {
        if (isBusy) {
            return false;
        }
        isBusy = true;
        final int len = absListView.getChildCount();
        int offset = 0;
        if (absListView instanceof ListView) {
            offset = ((ListView) absListView).getHeaderViewsCount();
        }
        for (int i = len - 1; i >= offset; i--) {
            View v = absListView.getChildAt(i);
            if (v != null) {
                Animation a = getPlummetAnimation();
                a.setAnimationListener(new MyAnimationListener(v, i == offset));
                a.setStartOffset(20 * i);
                v.startAnimation(a);
            }
        }
        return true;
    }

    /**
     * 返回一个自由下落的动画
     *
     * @return Animation
     */
    private Animation getPlummetAnimation() {
        AnimationSet set = new AnimationSet(false);
        Animation sa = new ScaleAnimation(1.0f, 0.95f, 1.0f, 0.95f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(120);
        set.addAnimation(sa);
        Animation aa = new AlphaAnimation(1.0f, 0.0f);
        aa.setDuration(280);
        aa.setStartOffset(100);
        set.addAnimation(aa);
        Animation ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 2.0f);
        ta.setDuration(280);
        ta.setStartOffset(100);
        set.addAnimation(ta);
        return set;
    }

    private class MyAnimationListener implements Animation.AnimationListener {
        private View v;
        private boolean isEnd;

        public MyAnimationListener(View v, boolean isEnd) {
            this.v = v;
            this.isEnd = isEnd;
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            v.setVisibility(View.INVISIBLE);
            if (isEnd) {
                if (adapter != null) {
                    adapter.removeAll();
                    adapter.notifyDataSetChanged();
                    isBusy = false;
                }
                if (callback != null) {
                    callback.fulfil();
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    public interface Callback {
        void fulfil();
    }

}
