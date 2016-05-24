package com.ldm.basic.views;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.ldm.basic.filter.FrameRenderer;
import com.ldm.basic.filter.FrameRendererWave;
import com.ldm.basic.utils.LLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.AttributeSet;

/**
 * Created by ldm on 15/9/24. copy汪洋的代码实现
 */
public class LFilterGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

	private int width = 0;
	private int height = 0;
	public ClearColor clearColor;
	private FrameRenderer myRenderer;
	private Bitmap bitmap;

	private int mTextureID;

	public class ClearColor {
		public float r, g, b, a = 1.0f;
	}

	public void setClearColor(float r, float g, float b, float a) {
		clearColor.r = r;
		clearColor.g = g;
		clearColor.b = b;
		clearColor.a = a;
		queueEvent(new Runnable() {
			@Override
			public void run() {
				GLES20.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
			}
		});
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public LFilterGLSurfaceView(Context context) {
		super(context);
		init(context);
	}

	public LFilterGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		clearColor = new ClearColor();

		setEGLContextClientVersion(2);
		setZOrderOnTop(true);
		setEGLConfigChooser(8, 8, 8, 8, 8, 0);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		setRenderer(this);
		setRenderMode(RENDERMODE_CONTINUOUSLY);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		LLog.e("onDetachedFromWindow 1 ");
		if (myRenderer != null) {
			myRenderer.release();
			myRenderer = null;
		}
		LLog.e("onDetachedFromWindow 2 ");
	}

	public int initTexture(GL10 gl, Bitmap bitmap) {
		// 生成纹理ID
		if (bitmap != null && !bitmap.isRecycled()) {
			int[] textures = new int[1];
			gl.glGenTextures(1, textures, 0);
			int currTextureId = textures[0];
			gl.glBindTexture(GL10.GL_TEXTURE_2D, currTextureId);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			return textures[0];
		} else {
			return -1;
		}
	}

	FrameRenderer.Viewport drawViewport;

	private void calcViewport() {
		drawViewport = new FrameRenderer.Viewport();
		drawViewport.width = LFilterGLSurfaceView.this.width;
		drawViewport.height = LFilterGLSurfaceView.this.height;
		drawViewport.x = 0;
		drawViewport.y = 0;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_STENCIL_TEST);

		mTextureID = initTexture(gl, bitmap);

		FrameRendererWave rendererWave = FrameRendererWave.create(false);
		myRenderer = rendererWave;
		// rendererWave.setRotation((float) Math.PI / 2.0f);//90度
		rendererWave.setRotation(0);
		rendererWave.setAutoMotion(0.1f);
	}

	public int getTextureWidth() {
		if (width <= 0 && bitmap != null && !bitmap.isRecycled()) {
			return bitmap.getWidth();
		}
		return width;
	}

	public int getTextureHeight() {
		if (height <= 0 && bitmap != null && !bitmap.isRecycled()) {
			return bitmap.getHeight();
		}
		return height;
	}

	/**
	 * 设置一个新的FrameRendererWave
	 * 
	 * @param fr
	 */
	public void setFrameRenderer(FrameRenderer fr) {
		if (myRenderer != null) {
			myRenderer.release();
			myRenderer = null;
		}
		myRenderer = fr;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (mTextureID == -1) {
			mTextureID = initTexture(gl, bitmap);
		}
		GLES20.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);

		LFilterGLSurfaceView.this.width = width;
		LFilterGLSurfaceView.this.height = height;

		calcViewport();
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		if (mTextureID == -1) {

			mTextureID = initTexture(gl, bitmap);
		} else {
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			if (myRenderer != null) {
				myRenderer.renderTexture(mTextureID, drawViewport);
			}
		}
	}
}
