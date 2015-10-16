package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ldm on 13-5-22. 快速滑动view(eoe中网友编写)
 */
public class LFastSlideView extends View {

	private String[] datas = { "#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

	private int choose = -1;
	private Paint paint = new Paint();
	private boolean showBkg = false;
	private int fontColor;
	private int fontSize;
	private int effectiveHeight;
	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;

	private Bitmap firstCharBitmap;// 占位符，所有字符的顶部图片，参加索引

	private int normalBackground;
	private int pressedBackground;

	public void setData(String[] datas) {
		this.datas = datas;
	}

	public LFastSlideView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LFastSlideView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LFastSlideView(Context context) {
		super(context);
	}

	/**
	 * 这个方法需要在setTheme之后调用
	 * 
	 * @param bit Bitmap
	 */
	public void setFirstCharBitmap(Bitmap bit) {
		firstCharBitmap = bit;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (showBkg) {
			canvas.drawColor(pressedBackground);
		} else {
			canvas.drawColor(normalBackground);
		}
		effectiveHeight = getHeight() - getPaddingTop() - getPaddingBottom();
		int singleHeight;
		int width = getWidth();
		int offLen = 0;
		int offTop = getPaddingTop();
		if (firstCharBitmap != null) {
			offLen = 1;
			float left = (width - firstCharBitmap.getWidth()) / 2;
			float top = offTop - firstCharBitmap.getHeight();// Top是底部的位置
			canvas.drawBitmap(firstCharBitmap, left, top, null);
		}
		singleHeight = effectiveHeight / (datas.length + offLen);
		for (int i = 0; i < datas.length; i++) {
			float xPos = width / 2 - paint.measureText(datas[i]) / 2;
			float yPos = singleHeight * (i + offLen) + offTop;
			canvas.drawText(datas[i], xPos, yPos, paint);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		if (listener != null) {
			final int action = event.getAction();
			final float y = event.getY() - getPaddingTop();
			final int oldChoose = choose;
			final int c = (int) (y / effectiveHeight * (datas.length + (firstCharBitmap == null ? 0 : 1)));
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				showBkg = true;
				if (oldChoose != c) {
					if (c < 0) {
						listener.onTouchingDown();
						listener.onTouchingLetterChanged("-1");
						choose = -1;
					} else {
						if (c < datas.length) {
							listener.onTouchingDown();
							listener.onTouchingLetterChanged(datas[c]);
							choose = c;
						}
					}
					invalidate();
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (oldChoose != c) {
					if (c < 0) {
						listener.onTouchingLetterChanged("-1");
						choose = -1;
					} else {
						if (c < datas.length) {
							listener.onTouchingLetterChanged(datas[c]);
							choose = c;
						}
					}
					invalidate();
				}
				break;
			case MotionEvent.ACTION_UP:
				showBkg = false;
				choose = -1;
				invalidate();
				listener.onTouchingCenter();
				break;
			case MotionEvent.ACTION_CANCEL:// 监听ACTION_CANCEL事件，防止用户手指移动到其它view上时ACTION_UP被跳过
				showBkg = false;
				choose = -1;
				invalidate();
				listener.onTouchingCenter();
				break;
			}
		}
		return true;
	}

	/**
	 * 设置该控件的样式
	 *
	 * @param textColor
	 * @param textSize
	 * @param normalBackground
	 * @param pressedBackground
	 */
	public void setTheme(int textColor, int textSize, int normalBackground, int pressedBackground) {
		this.fontColor = textColor;
		this.fontSize = textSize;
		this.normalBackground = normalBackground;
		this.pressedBackground = pressedBackground;
		paint.setColor(fontColor);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		paint.setAntiAlias(true);
		paint.setTextSize(fontSize);
	}

	public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	public interface OnTouchingLetterChangedListener {
		public void onTouchingLetterChanged(String s);

		public void onTouchingDown();

		public void onTouchingCenter();
	}

}
