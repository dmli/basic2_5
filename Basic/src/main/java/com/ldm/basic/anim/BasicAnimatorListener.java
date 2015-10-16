package com.ldm.basic.anim;

import android.animation.Animator;
import android.annotation.SuppressLint;

@SuppressLint("NewApi")
public abstract class BasicAnimatorListener implements Animator.AnimatorListener{

	public Object obj;

	public BasicAnimatorListener(Object obj) {
		this.obj = obj;
	}

	public BasicAnimatorListener() {
	}

	@Override
	public void onAnimationStart(Animator animation) {
		
	}

	@Override
	public void onAnimationCancel(Animator animation) {
		
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
		
	}

}
