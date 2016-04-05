package com.ldm.basic.views.ctrl;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.ldm.basic.adapter.BasicMultiTypeAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldm on 14-8-7.
 * 给listView删除时增加动画
 */
public class LListViewRemoveHelper {

    private AbsListView absListView;
    private BasicMultiTypeAdapter<?> adapter;
    private Callback callback;// 完成反馈
    private final List<View> animView;//被执行过动画的View
    private boolean isBusy;// 是否处于繁忙状态

    public LListViewRemoveHelper(AbsListView absListView, BasicMultiTypeAdapter<?> adapter, Callback callback) {
        this.absListView = absListView;
        this.adapter = adapter;
        this.callback = callback;
        this.animView = new ArrayList<>();
    }

    /**
     * 设置新的适配器
     *
     * @param adapter BasicAdapter<?>
     */
    public void setAdapter(BasicMultiTypeAdapter<?> adapter) {
        this.adapter = adapter;
    }

    /**
     * 内容自由下落
     *
     * @return true动画开始执行
     */
    public boolean removeToPlummet(int duration) {
        if (isBusy) {
            return false;
        }
        isBusy = true;
        final int len = absListView.getChildCount();
        int offset = 0;
        if (absListView instanceof ListView && absListView.getFirstVisiblePosition() == 0) {
            offset = ((ListView) absListView).getHeaderViewsCount();
        }
        animView.clear();
        for (int i = len - 1; i >= offset; i--) {
            View v = absListView.getChildAt(i);
            if (v != null) {
                animView.add(v);
                AnimatorSet a = getPlummetAnimator(v, duration, i);
                a.addListener(new MyAnimatorListener(v, i == len - 1));
                a.start();
            }
        }
        return true;
    }

    /**
     * 返回一个自由下落的动画
     *
     * @return Animation
     */
    private AnimatorSet getPlummetAnimator(View v, int duration, int index) {
        AnimatorSet set1 = new AnimatorSet();
        float scale = (float) (0.95f - index * 0.016);
        PropertyValuesHolder valuesHolder1 = PropertyValuesHolder.ofFloat("scaleX", 1.0f, scale);
        PropertyValuesHolder valuesHolder2 = PropertyValuesHolder.ofFloat("scaleY", 1.0f, scale);
        ObjectAnimator sAnimator = ObjectAnimator.ofPropertyValuesHolder(v, valuesHolder1, valuesHolder2);
        sAnimator.setDuration(120);
        set1.play(sAnimator);

        PropertyValuesHolder valuesHolder3 = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
        PropertyValuesHolder valuesHolder4 = PropertyValuesHolder.ofFloat("translationY", 0.0f, v.getMeasuredHeight());
        ObjectAnimator aAnimator = ObjectAnimator.ofPropertyValuesHolder(v, valuesHolder3, valuesHolder4);
        aAnimator.setDuration(duration);
        aAnimator.setStartDelay(100);
        set1.play(aAnimator);

        set1.setStartDelay(35 * index);
        return set1;
    }

    /**
     * 使用removeToPlummet()动画结束后，可以调用这个方法恢复动到动画开始效果
     *
     * @param v View
     */
    public static void resetPlummetAnimator(View v) {
        ViewHelper.setTranslationY(v, 0);
        ViewHelper.setAlpha(v, 1);
        ViewHelper.setScaleX(v, 1);
        ViewHelper.setScaleY(v, 1);
    }

    private class MyAnimatorListener implements Animator.AnimatorListener {

        private View v;
        private boolean isEnd;

        public MyAnimatorListener(View v, boolean isEnd) {
            this.v = v;
            this.isEnd = isEnd;
        }

        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            v.setVisibility(View.INVISIBLE);
            resetPlummetAnimator(v);
            if (isEnd) {
                if (adapter != null) {
                    adapter.removeAll();
                    adapter.notifyDataSetChanged();

                    /**
                     * 恢复View的显示状态
                     */
                    for (View view : animView) {
                        if (view != null) {
                            view.setVisibility(View.VISIBLE);
                        }
                    }
                    animView.clear();
                    isBusy = false;
                }
                if (callback != null) {
                    callback.fulfil();
                }
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }


    public interface Callback {
        void fulfil();
    }

}
