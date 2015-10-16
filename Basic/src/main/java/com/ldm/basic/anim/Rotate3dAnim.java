package com.ldm.basic.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by ldm on 12-10-20.
 * 摘取官方源码，实现View间3D的切换效果
 */
public class Rotate3dAnim extends Animation{

	private float lFromDegrees;
	private float lToDegrees;
	private float lCenterX;
	private float lCenterY;
	private float lDepthZ;
	private boolean lReverse;
	private Camera lCamera;
	
	/**
	 * 初始化Rotate3dAnim
	 * @param formDegrees 开始角度
	 * @param toDegrees   结束角度
	 * @param centerX 中心X
	 * @param centerY 中心Y
	 * @param depthZ Z轴
	 * @param reverse true表示正向
	 */
	public Rotate3dAnim(float formDegrees, float toDegrees, float centerX, float centerY, float depthZ, boolean reverse) {
		lFromDegrees = formDegrees;
		lToDegrees = toDegrees;
		lCenterX = centerX;
		lCenterY = centerY;
		lReverse = reverse;
		lDepthZ = depthZ;
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		lCamera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		float fromDegrees = lFromDegrees;
		float degrees = fromDegrees + ((lToDegrees - fromDegrees) * interpolatedTime);

		float centerX = lCenterX;
		float centerY = lCenterY;
		Camera camera = lCamera;

		Matrix matrix = t.getMatrix();

		camera.save();
		if (lReverse) {
			camera.translate(0.0f, 0.0f, lDepthZ * interpolatedTime);
		} else {
			camera.translate(0.0f, 0.0f, lDepthZ * (1.0f - interpolatedTime));
		}

		camera.rotateY(degrees);
		camera.getMatrix(matrix);
		camera.restore();

		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);
	}
}
