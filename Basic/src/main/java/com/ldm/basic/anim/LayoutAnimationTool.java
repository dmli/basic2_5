package com.ldm.basic.anim;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;

/**
 * Created by ldm on 12-8-17. Layout动画工具
 */
public class LayoutAnimationTool {

	/**
	 * 通用列表加载特效
	 * 
	 * @return LayoutAnimationController
	 */
	public static LayoutAnimationController getLayoutAnimationController() {
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(100);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(120);
		set.addAnimation(animation);

		return new LayoutAnimationController(set, 0.5f);
	}

	/**
	 * 通用列表加载特效
	 * 
	 * @return LayoutAnimationController
	 */
	public static LayoutAnimationController getLayoutAnimationController2Alpha() {
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(120);
		set.addAnimation(animation);

		set.addAnimation(animation);
		return new LayoutAnimationController(set, 0.5f);
	}

}
