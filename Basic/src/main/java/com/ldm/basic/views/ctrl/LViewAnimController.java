package com.ldm.basic.views.ctrl;

import com.ldm.basic.anim.BasicAnimationListener;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by ldm on 14/10/22.
 * 设置view后，使用这个控制器的setVisibility对view进行隐藏或现实时将会自动播放动画
 */
public class LViewAnimController {

    private Animation anim;
    private long durationMillis = 200;
    public static final int ALPHA_ANIM = 0;//  淡入淡出
    public static final int ROTATE_ANIM = 1;// 旋转+淡入淡出
    public static final int TRANSLATE_DOWN_TO_DOWN_VISIBLE_ANIM = 2;// 上移显示 下落隐藏
    public static final int TRANSLATE_UP_TO_DOWN_VISIBLE_ANIM = 3;// 下落显示 上移隐藏
    private int USE_ANIM = ALPHA_ANIM;
    private BasicAnimationListener animationListener;

    /**
     * 设置显示隐藏
     *
     * @param visibility 取值范围 View.VISIBLE 、 GONE 、INVISIBLE
     */
    public void setVisibility(View v, int visibility) {
        setVisibility(v, visibility, -1);
    }

    /**
     * 动画监听
     *
     * @param animationListener BasicAnimationListener
     */
    public void setAnimationListener(BasicAnimationListener animationListener) {
        this.animationListener = animationListener;
    }

    /**
     * 显示/隐藏 时对指定的View做预设动画
     *
     * @param v          View
     * @param visibility 取值范围View.VISIBLE 、View.GONE
     * @param anim       动画类型 取值范围
     *                   ALPHA_ANIM、ROTATE_ANIM、TRANSLATE_DOWN_ANIM、TRANSLATE_UP_ANIM
     */
    public void setVisibility(View v, int visibility, int anim) {
        if (v != null && v.getVisibility() != visibility) {
            Animation a;
            int type = anim == -1 ? USE_ANIM : anim;
            if (type == ROTATE_ANIM) {
                a = getRotateAnim(visibility);
            } else if (type == TRANSLATE_DOWN_TO_DOWN_VISIBLE_ANIM) {
                a = getTranslateToDownAnim(visibility);
            } else if (type == TRANSLATE_UP_TO_DOWN_VISIBLE_ANIM) {
                a = getTranslateToUpAnim(visibility);
            } else {
                a = getAlphaAnim(visibility);// 没有找到对应属性，使用
            }
            a.setAnimationListener(new MyAnimationListener(v, visibility));
            if (visibility == View.VISIBLE) {
                v.setVisibility(visibility);
            }
            v.startAnimation(a);
        }
    }

    public Animation getAlphaAnim(int visibility) {
        if (anim == null) {
            AlphaAnimation a;
            if (visibility == View.VISIBLE) {
                a = new AlphaAnimation(0.0f, 1.0f);
            } else {
                a = new AlphaAnimation(1.0f, 0.0f);
            }
            a.setDuration(durationMillis);
            return a;
        }
        return anim;
    }

    public Animation getRotateAnim(int visibility) {
        if (anim == null) {
            AnimationSet set = new AnimationSet(true);
            if (visibility == View.VISIBLE) {
                set.addAnimation(new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.50f, Animation.RELATIVE_TO_SELF, 0.50f));
                set.addAnimation(new AlphaAnimation(0.0f, 1.0f));
            } else {
                set.addAnimation(new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.50f, Animation.RELATIVE_TO_SELF, 0.50f));
                set.addAnimation(new AlphaAnimation(1.0f, 0.0f));
            }
            set.setDuration(durationMillis);
            return set;
        }
        return anim;
    }

    public Animation getTranslateToUpAnim(int visibility) {
        if (anim == null) {
            AnimationSet set = new AnimationSet(true);
            if (visibility == View.VISIBLE) {
                set.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f));
                set.addAnimation(new AlphaAnimation(0.0f, 1.0f));
            } else {
                set.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f));
                set.addAnimation(new AlphaAnimation(1.0f, 0.0f));
            }
            set.setDuration(durationMillis);
            return set;
        }
        return anim;
    }

    public Animation getTranslateToDownAnim(int visibility) {
        if (anim == null) {
            AnimationSet set = new AnimationSet(true);
            if (visibility == View.VISIBLE) {
                set.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f));
                set.addAnimation(new AlphaAnimation(0.0f, 1.0f));
            } else {
                set.addAnimation(new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f));
                set.addAnimation(new AlphaAnimation(1.0f, 0.0f));
            }
            set.setDuration(durationMillis);
            return set;
        }
        return anim;
    }

    /**
     * 设置需要播放的动画
     *
     * @param anim Animation
     */
    public void setAnim(Animation anim) {
        this.anim = anim;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    private class MyAnimationListener implements Animation.AnimationListener {
        View view;
        int visibility;

        private MyAnimationListener(View v, int visibility) {
            this.visibility = visibility;
            this.view = v;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            if (animationListener != null) {
                animationListener.onAnimationStart(animation);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (view != null) {
                view.setVisibility(visibility);
            }
            if (animationListener != null) {
                animationListener.onAnimationEnd(animation);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            if (animationListener != null) {
                animationListener.onAnimationRepeat(animation);
            }
        }
    }
}
