package com.ldm.basic.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by ldm on 13-12-17.
 * 图片平铺展示控件
 */
public class LImageViewGroup extends ViewGroup {

    private int viewMargin = 12;

    public LImageViewGroup(Context context) {
        super(context);
    }

    public LImageViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child != null) {
                child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            }
        }
    }

    protected void onLayout(boolean arg0, int left, int top, int right, int bottom) {
        int count = getChildCount();
        int row = 0;
        int _left = viewMargin;
        for (int i = 0; i < count; i++) {
            View child = this.getChildAt(i);
            if (child == null || child.getVisibility() == GONE) continue;

            int width = 0;
            int height = 0;
            LayoutParams lp = child.getLayoutParams();
            if (lp != null) {
                if (lp.width > 0) {
                    width = child.getLayoutParams().width;
                }
                if (lp.height > 0) {
                    height = child.getLayoutParams().height;
                }
            }
            if (width == 0) {
                width = child.getMeasuredWidth();
            }
            if (height == 0) {
                height = child.getMeasuredHeight();
            }
            if (_left + width + viewMargin > getMeasuredWidth()) {
                row++;//如果宽度不够就换行
                _left = viewMargin;
            }
            int rowMargin = (row + 1) * viewMargin;
            int t = row * height + rowMargin;
            int r = _left + width;
            int b = (row + 1) * height + rowMargin;
            child.layout(_left, t, r, b);
            _left = r + viewMargin;
        }
    }

    /**
     * 设置Child的Visibility属性，使用这个方法显隐Child时将会附带动画效果
     *
     * @param position 位置
     */
    public void setChildVisibility(int position, final int visibility) {
        if (position >= 0 && position < getChildCount()) {
            if (visibility == View.GONE) {
                //将要被隐藏的view
                final View g0 = getChildAt(position);
                ScaleAnimation sa = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, g0.getWidth() / 2, g0.getHeight() / 2);
                sa.setDuration(350);
                sa.setAnimationListener(new BasicAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        g0.setVisibility(visibility);
                    }
                });
                int ol = g0.getLeft();
                int ot = g0.getTop();
                g0.startAnimation(sa);
                do {
                    position++;
                    final View g1 = getChildAt(position);
                    if (g1 != null && g1.getVisibility() == VISIBLE) {
                        int nl = g1.getLeft();
                        int nt = g1.getTop();
                        TranslateAnimation ta = new TranslateAnimation(0, ol - nl, 0, ot - nt);
                        ta.setDuration(350);
                        ta.setFillAfter(false);
                        ta.setFillEnabled(true);
                        ta.setAnimationListener(new BasicAnimationListener(g1, ol, ot) {
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                v.layout(l, t, r, b);
                            }
                        });
                        g1.startAnimation(ta);
                        ol = nl;
                        ot = nt;
                    }

                } while (position < getChildCount() - 1);
            }
        }
    }


    /**
     * 重新初始化控件高度，将根据内容高度自动计算
     * 条件：控件必需设置MATCH_PARENT属性或已经执行完initWidth方法，
     * 该方法执行完成后如果用户没有设置width属性，控件将为其设置MATCH_PARENT属性
     */
    public void initHeight() {
        LayoutParams lp = this.getLayoutParams();
        int h = this.getMinHeight();
        if (lp != null) {
            if (h != lp.height) {
                lp.height = h;
            }
        } else {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, h);
        }
        this.setLayoutParams(lp);
    }

    /**
     * 根据Child计算并重新设置最小所需的宽度
     * 该方法执行完成后如果用户没有设置height, 控件将自动为其设置WRAP_CONTENT属性
     */
    public void initWidth() {
        initWidth(-1);
    }


    /**
     * 根据给定宽度重新初始化控件宽度
     * 该方法执行完成后如果用户没有设置height, 控件将自动为其设置WRAP_CONTENT属性
     */
    public void initWidth(int width) {
        LayoutParams lp = this.getLayoutParams();
        int _width = width == -1 ? getMinWidth() : width;
        if (lp != null) {
            lp.width = _width;
        } else {
            lp = new LayoutParams(_width, LayoutParams.WRAP_CONTENT);
        }
        this.setLayoutParams(lp);
    }

    /**
     * 返回控件所需的最小宽度
     *
     * @return minWidth
     */
    private int getMinWidth() {
        int w = getMaxWidth();
        int count = getChildCount();
        if (w <= 0 && count == 0) {
            return 0;
        }
        int minWidth = 0;//控件所需的最小宽度
        int l = viewMargin;
        for (int i = 0; i < count; i++) {
            View child = this.getChildAt(i);
            if (child == null || child.getVisibility() == GONE) continue;
            int width = 0;
            LayoutParams lp = child.getLayoutParams();
            if (lp != null) {
                if (lp.width > 0) {
                    width = child.getLayoutParams().width;
                }
            }
            if (width == 0) {
                width = child.getMeasuredWidth();
            }
            if (l + width + viewMargin > getMeasuredWidth()) {
                l = viewMargin;
            }
            l = l + width + viewMargin;
            //找到最大宽度的行
            if (minWidth < l) {
                minWidth = l;
            }
        }
        return minWidth + viewMargin;
    }

    /**
     * 获取控件所需的最小高度
     *
     * @return min height
     */
    public int getMinHeight() {
        int w = getMaxWidth();
        int count = getChildCount();
        if (w <= 0 && count == 0) {
            return 0;
        }
        if (count == 1) {
            View v = getChildAt(0);
            if (v != null && v.getVisibility() != GONE) {
                int _h;
                if (v.getLayoutParams() != null && v.getLayoutParams().width > 0) {
                    _h = v.getLayoutParams().height;
                } else {
                    _h = v.getMeasuredHeight();
                }
                return _h + viewMargin * 2;
            }
        }

        int bottom = 0;
        int row = 0;
        int l = viewMargin;
        for (int i = 0; i < count; i++) {
            View child = this.getChildAt(i);
            if (child == null || child.getVisibility() == GONE) continue;

            int width = 0;
            int height = 0;
            LayoutParams lp = child.getLayoutParams();
            if (lp != null) {
                if (lp.width > 0) {
                    width = child.getLayoutParams().width;
                }
                if (lp.height > 0) {
                    height = child.getLayoutParams().height;
                }
            }
            if (width == 0) {
                width = child.getMeasuredWidth();
            }
            if (height == 0) {
                height = child.getMeasuredHeight();
            }

            if (l + width + viewMargin > w) {
                row++;//如果宽度不够就换行
                l = viewMargin;
            }
            l = l + width + viewMargin;
            int rowMargin = (row + 1) * viewMargin;
            bottom = (row + 1) * height + rowMargin;
        }
        return bottom + viewMargin;
    }

    /**
     * 获取控件的最大宽度
     *
     * @return max width
     */
    private int getMaxWidth() {
        int w = 0;
        LayoutParams lp = getLayoutParams();
        if (lp != null) {
            w = lp.width;
        }
        if (w <= 0) {
            if (getMeasuredWidth() <= 0) {
                measure(MeasureSpec.EXACTLY, MeasureSpec.AT_MOST);
            }
            w = getMeasuredWidth();
        }
        return w;
    }

    /**
     * 设置间距
     *
     * @param viewMargin 间距
     */
    public void setViewMargin(int viewMargin) {
        this.viewMargin = viewMargin;
    }

    /**
     * 将child添加到getChildCount() - 1位置
     *
     * @param child View
     */
    public void addItemView(View child) {
        addView(child, getChildCount() - 1);
    }

    private abstract class BasicAnimationListener implements Animation.AnimationListener {

        public View v;
        public int l;
        public int t;
        public int r;
        public int b;

        protected BasicAnimationListener() {
        }

        protected BasicAnimationListener(View v, int l, int t) {
            this.v = v;
            this.l = l;
            this.t = t;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            if (v != null) {
                r = l + v.getWidth();
                b = t + v.getHeight();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
