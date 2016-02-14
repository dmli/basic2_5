/*
 * Copyright (C) 2015 tyrantgit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ldm.basic.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.ldm.basic.anim.ExplosionAnimator;
import com.ldm.basic.utils.SystemTool;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * 代码来源于http://www.itlanbao.com网友提供
 */
public class LExplosionFieldView extends View {

	private List<ExplosionAnimator> mExplosions = new ArrayList<>();
	private int[] mExpandInset = new int[2];
	private Animator.AnimatorListener animatorListener;

	public LExplosionFieldView(Context context) {
		super(context);
		init();
	}

	public void setAnimatorListener(Animator.AnimatorListener animatorListener) {
		this.animatorListener = animatorListener;
	}

	public LExplosionFieldView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LExplosionFieldView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		Arrays.fill(mExpandInset, (int) SystemTool.dipToPx(32));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (ExplosionAnimator explosion : mExplosions) {
			explosion.draw(canvas);
		}
	}

	public void expandExplosionBound(int dx, int dy) {
		mExpandInset[0] = dx;
		mExpandInset[1] = dy;
	}

	public void explode(Bitmap bitmap, Rect bound, long startDelay, long duration) {
		final ExplosionAnimator explosion = new ExplosionAnimator(this, bitmap, bound);
		explosion.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mExplosions.remove(animation);
				if (animatorListener != null) {
					animatorListener.onAnimationEnd(animation);
				}
			}

			@Override
			public void onAnimationStart(Animator animation) {
				if (animatorListener != null) {
					animatorListener.onAnimationStart(animation);
				}
			}
		});
		explosion.setStartDelay(startDelay);
		explosion.setDuration(duration);
		mExplosions.add(explosion);
		explosion.start();
	}

	public void explode(final View view) {
		Rect r = new Rect();
		view.getGlobalVisibleRect(r);
		int[] location = new int[2];
		getLocationOnScreen(location);
		r.offset(location[0], -location[1]);
		r.inset(-mExpandInset[0], -mExpandInset[1]);
		int startDelay = 100;
		ValueAnimator animator = ValueAnimator.ofFloat(new float[] { 0f, 1f }).setDuration(150);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			Random random = new Random();

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				ViewHelper.setTranslationX(LExplosionFieldView.this, (random.nextFloat() - 0.5f) * view.getWidth() * 0.05f);
				ViewHelper.setTranslationY(LExplosionFieldView.this, (random.nextFloat() - 0.5f) * view.getHeight() * 0.05f);
			}
		});
		animator.start();
		animator.setDuration(150);
		animator.setStartDelay(startDelay);
		ViewHelper.setScaleX(this, 0f);
		ViewHelper.setScaleY(this, 0f);
		ViewHelper.setAlpha(this, 0f);
		animator.start();
		explode(createBitmapFromView(view), r, startDelay, ExplosionAnimator.DEFAULT_DURATION);
	}
	
	public void explode(final View view, int startDelay) {
		Rect r = new Rect();
		view.getGlobalVisibleRect(r);
		int[] location = new int[2];
		getLocationOnScreen(location);
		r.offset(location[0], -location[1]);
		r.inset(-mExpandInset[0], -mExpandInset[1]);
		explode(createBitmapFromView(view), r, startDelay, ExplosionAnimator.DEFAULT_DURATION);
	}

	public void clear() {
		mExplosions.clear();
		invalidate();
	}

	public Bitmap createBitmapFromView(View view) {
		final Canvas canvas = new Canvas();
		if (view instanceof ImageView) {
			Drawable drawable = ((ImageView) view).getDrawable();
			if (drawable != null && drawable instanceof BitmapDrawable) {
				return ((BitmapDrawable) drawable).getBitmap();
			}
		}
		view.clearFocus();
		Bitmap bitmap = createBitmapSafely(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888, 1);
		if (bitmap != null) {
			canvas.setBitmap(bitmap);
			view.draw(canvas);
			canvas.setBitmap(null);
		}
		return bitmap;
	}

	public Bitmap createBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
		try {
			return Bitmap.createBitmap(width, height, config);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			if (retryCount > 0) {
				System.gc();
				return createBitmapSafely(width, height, config, retryCount - 1);
			}
			return null;
		}
	}

}
