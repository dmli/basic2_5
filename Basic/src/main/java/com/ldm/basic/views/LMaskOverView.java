package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

/**
 * Created by ldm on 15/6/16.
 * 增加了简单的过度动画的ImageView， 但需要给定一个Bitmap
 */
public class LMaskOverView extends ImageView {

    public static final int RANDOM_ANIM = 0;//随机选择预设的动画进行过度
    public static final int RANDOM_ANIM_12 = -1;//随机播放对图像处理的渐变动画 ANIM_TYPE_1 ANIM_TYPE_1_REVERSE ANIM_TYPE_2
    public static final int ANIM_TYPE_1 = 1;//
    public static final int ANIM_TYPE_1_REVERSE = 5;//ANIM_TYPE_1的逆向动画
    public static final int ANIM_TYPE_2 = 2;
    public static final int ANIM_TYPE_13 = 3;//单纯的淡出动画   当这个动画不需要自己过度时可以使用这个动画
    public static final int ANIM_TYPE_14 = 4;//随机一个方向移出界面   当这个动画不需要自己过度时可以使用这个动画

    private static final int ANIM_COUNT = 5;

    private int currentAnimType = ANIM_TYPE_1;

    private int bitWidth, bitHeight;
    private float interpolated;
    private Paint paint;
    private RectF rectF;
    private Rect rect;
    private boolean isAnimFinished;//动画是否结束 true结束状态
    private boolean animFinishedAutoHide;//动画结束完成自动隐藏View
    private Bitmap bitmap;
    private Animation.AnimationListener animationListener;
    private boolean useImageSrcBitmap;//默认false， 设置true后将使用ImageView的src图像做渐变
    private boolean openAnim;//默认true 设置false后 这个View与ImageView没有任何区别


    public LMaskOverView(Context context) {
        super(context);
        init();
    }

    public LMaskOverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LMaskOverView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    /**
     * 如果有动画正在运行的时候
     */
    public void stopAnimation() {
        if (getAnimation() != null) {
            getAnimation().cancel();
            isAnimFinished = true;
        }
    }

    private void init() {
        paint = new Paint();
        rectF = new RectF();
        rect = new Rect();
        openAnim = true;
        isAnimFinished = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitWidth <= 0 || bitHeight <= 0) {
            bitWidth = getWidth();
            bitHeight = getHeight();
        }
        if (openAnim && !isAnimFinished) {
            if (bitWidth > 0 && bitHeight > 0) {
                switch (currentAnimType) {
                    case ANIM_TYPE_1:
                        drawAnimType1(canvas, bitWidth, bitHeight);
                        break;
                    case ANIM_TYPE_1_REVERSE:
                        drawAnimType1Reverse(canvas, bitWidth, bitHeight);
                        break;
                    case ANIM_TYPE_2:
                        drawAnimType2(canvas, bitWidth, bitHeight);
                        break;
                    case ANIM_TYPE_13://动画三使用了淡出动画
                    case ANIM_TYPE_14://动画四使用了位移动画
                        if (bitmap != null && !bitmap.isRecycled()) {
                            canvas.drawBitmap(bitmap, 0, 0, paint);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 过度动画1的逆向动画
     *
     * @param canvas 画布
     * @param width  宽
     * @param height 高
     */
    private void drawAnimType1Reverse(Canvas canvas, int width, int height) {
        final int row = 5;
        final float cw = width * 1.0f / row;
        if (bitmap != null && !bitmap.isRecycled()) {
            for (int i = 0; i < 5; i++) {
                rectF.set(i * cw + (cw * interpolated), 0, (i * cw + cw), height);
                rect.set((int) rectF.left, 0, (int) rectF.right, height);
                canvas.drawBitmap(bitmap, rect, rectF, paint);
            }
        }
    }

    /**
     * 过度动画1
     *
     * @param canvas 画布
     * @param width  宽
     * @param height 高
     */
    private void drawAnimType1(final Canvas canvas, final float width, final float height) {
        final int row = 5;
        final float cw = width * 1.0f / row;
        if (bitmap != null && !bitmap.isRecycled()) {
            for (int i = 0; i < 5; i++) {
                rectF.set(i * cw, 0, (i * cw) + (cw * (1 - interpolated)), height);
                rect.set((int) rectF.left, 0, (int) rectF.right, (int) (height));
                canvas.drawBitmap(bitmap, rect, rectF, paint);
            }
        }
    }

    /**
     * 过度动画2
     *
     * @param canvas 画布
     * @param width  宽
     * @param height 高
     */
    private void drawAnimType2(final Canvas canvas, final float width, final float height) {
        final int row = 5;
        final int col = 5;
        final float cw = width * 1.0f / row;
        final float ch = height * 1.0f / col;
        if (bitmap != null && !bitmap.isRecycled()) {
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    float x_interpolated = (cw / 2 * interpolated);
                    float y_interpolated = (ch / 2 * interpolated);
                    float l = i * cw;
                    float t = j * ch;
                    rectF.set(l + x_interpolated, t + y_interpolated, l + cw - x_interpolated, t + ch - y_interpolated);
                    rect.set((int) l, (int) t, (int) rectF.right, (int) rectF.bottom);
                    canvas.drawBitmap(bitmap, rect, rectF, paint);
                }
            }
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setAnimationListener(Animation.AnimationListener animationListener) {
        this.animationListener = animationListener;
    }

    public void setAnimFinishedAutoHide(boolean animFinishedAutoHide) {
        this.animFinishedAutoHide = animFinishedAutoHide;
    }

    public void startAnim(final int durationMillis) {
        if (!isAnimFinished) return;//当动画处于播放中时不接受第二次动画
        if (useImageSrcBitmap && getDrawable() != null) {
            bitmap = ((BitmapDrawable) getDrawable()).getBitmap().copy(Bitmap.Config.RGB_565, true);
            if (bitmap == null) {//如果获取到图像失败，将放弃这次过度动画，直接调用animListener的end
                if (animListener != null) {
                    animListener.onAnimationEnd(null);
                }
                return;
            }
        }
        Animation animation;
        if (currentAnimType == ANIM_TYPE_13) {
            animation = new AlphaAnimation(1.0f, 0.0f);
        } else if (currentAnimType == ANIM_TYPE_14) {
            animation = createTranslateAnimation();
        } else {
            animation = new MyAnimation();
        }
        isAnimFinished = false;
        animation.setAnimationListener(animListener);
        animation.setDuration(durationMillis);
        this.startAnimation(animation);
    }

    /**
     * 使用ImageView的src中图像初始化动画图像
     */
    public boolean initBitmapInImageSrc() {
        if (getDrawable() != null) {
            try {
                bitmap = ((BitmapDrawable) getDrawable()).getBitmap().copy(Bitmap.Config.RGB_565, true);
                return true;
            } catch (Exception e) {
                Bitmap bit = getDrawingCache();
                if (bit != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private Animation createTranslateAnimation() {
        int s = (int) (Math.random() * 4);
        TranslateAnimation tr;
        switch (s) {
            case 0:
                tr = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                break;
            case 1:
                tr = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                break;
            case 2:
                tr = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
                break;
            default:
                tr = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
                break;
        }
        return tr;
    }

    /**
     * 设置过度动画类型 （）
     *
     * @param currentAnimType 取值范围 ANIM_TYPE_1、ANIM_TYPE_2、ANIM_TYPE_3、ANIM_TYPE_4
     */
    public void setCurrentAnimType(int currentAnimType) {
        if (currentAnimType == RANDOM_ANIM) {
            int s = (int) (Math.random() * ANIM_COUNT);
            switch (s) {
                case 0:
                    this.currentAnimType = ANIM_TYPE_14;
                    break;
                case 1:
                    this.currentAnimType = ANIM_TYPE_13;
                    break;
                case 2:
                    this.currentAnimType = ANIM_TYPE_2;
                    break;
                case 3:
                    this.currentAnimType = ANIM_TYPE_1_REVERSE;
                    break;
                default:
                    this.currentAnimType = ANIM_TYPE_1;
                    break;
            }
        } else if (currentAnimType == RANDOM_ANIM_12) {
            int s = (int) (Math.random() * 3);
            switch (s) {
                case 0:
                    this.currentAnimType = ANIM_TYPE_1_REVERSE;
                    break;
                case 1:
                    this.currentAnimType = ANIM_TYPE_2;
                    break;
                default:
                    this.currentAnimType = ANIM_TYPE_1;
                    break;
            }
        } else {
            this.currentAnimType = currentAnimType;
        }
    }

    /**
     * 动画是否完成
     *
     * @return true动画运行中  false动画停止
     */
    public boolean isAnimFinished() {
        return isAnimFinished;
    }

    public void setUseImageSrcBitmap(boolean useImageSrcBitmap) {
        this.useImageSrcBitmap = useImageSrcBitmap;
    }

    private class MyAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            interpolated = interpolatedTime;
            postInvalidate();
        }
    }

    public void setOpenAnim(boolean openAnim) {
        this.openAnim = openAnim;
    }

    private Animation.AnimationListener animListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            if (animationListener != null) {
                animationListener.onAnimationStart(animation);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (animFinishedAutoHide) {
                setVisibility(GONE);
            }
            isAnimFinished = true;
            if (animationListener != null) {
                animationListener.onAnimationEnd(animation);
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            if (animationListener != null) {
                animationListener.onAnimationRepeat(animation);
            }
        }
    };
}
