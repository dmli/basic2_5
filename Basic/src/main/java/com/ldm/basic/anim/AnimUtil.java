package com.ldm.basic.anim;

import java.util.Map;

import com.ldm.basic.intent.IntentUtil;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

/**
 * Created by ldm on 12-12-5. 提供了简单的intent动画及点击事件动画
 */
public class AnimUtil {

	public static long CLICK_ANIM_TIME = 80;

	/**
	 * 跳转页面使用 带缩放特效的点击事件，默认持续时间80毫秒
	 * 
	 * @param activity a
	 * @param classes 跳转目标类
	 * @param v 需要添加事件的View 系统默认为其添加OnClickListener事件
	 */
	public static void intentClick(final Activity activity, final Class<?> classes, final View v) {
		intentClick(activity, classes, v, null);
	}

	/**
	 * 跳转页面使用 带缩放特效的点击事件，默认持续时间80毫秒
	 * 
	 * @param activity a
	 * @param classes 跳转目标类
	 * @param v 需要添加事件的View 系统默认为其添加OnClickListener事件
	 * @param map 参数
	 */
	public static void intentClick(final Activity activity, final Class<?> classes, final View v, final Map<String, Object> map) {
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BasicAnimUtil.animScale(v, 1f, 0.9f, 1f, 0.9f, v.getWidth() / 2, v.getHeight() / 2, new BasicAnimUtil.AnimParams(false, 0, AnimUtil.CLICK_ANIM_TIME, 0) {
					@Override
					public void onAnimationEnd() {
						IntentUtil.intentDIY(activity, classes, map);
					}
				});
			}
		});
	}

	/**
	 * 跳转页面使用 带缩放特效的点击事件，默认持续时间80毫秒 (通过回调在动画结束后执行点击事件)
	 * 
	 * @param v 需要执行动画的View
	 * @param ap 回调接口
	 */
	public static void click(final View v, final BasicAnimUtil.AnimParams ap) {
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BasicAnimUtil.animScale(v, 1f, 0.9f, 1f, 0.9f, v.getWidth() / 2, v.getHeight() / 2, ap);
			}
		});
	}

	/**
	 * 带缩放特效的点击事件，默认持续时间80毫秒 (没有点击事件，通过回调在动画结束后执行点击事件)
	 * 
	 * @param v 需要执行动画的View
	 * @param ap 回调接口
	 */
	public static void click2(final View v, final BasicAnimUtil.AnimParams ap) {
		BasicAnimUtil.animScale(v, 1f, 0.9f, 1f, 0.9f, v.getWidth() / 2, v.getHeight() / 2, ap);
	}

	/**
	 * 返回一个Animation动画，动画为RELATIVE_TO_SELF 1.0f - 0.0f
	 * 
	 * @return AnimationSet
	 */
	public static AnimationSet getAnimationToBottomFadeIn(int durationMillis) {
		AnimationSet set = new AnimationSet(true);
		set.setDuration(durationMillis);
		TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0F, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		set.addAnimation(ta);
		AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
		set.addAnimation(aa);
		return set;
	}

	/**
	 * 返回一个Animation动画，动画为RELATIVE_TO_SELF 0.0f - 1.0f
	 * 
	 * @return AnimationSet
	 */
	public static AnimationSet getAnimationToBottomFadeOut(int durationMillis) {
		AnimationSet set = new AnimationSet(true);
		set.setDuration(durationMillis);
		TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0F, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
		set.addAnimation(ta);
		AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
		set.addAnimation(aa);
		return set;
	}

}
