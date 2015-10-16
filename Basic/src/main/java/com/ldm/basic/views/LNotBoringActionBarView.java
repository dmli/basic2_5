package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ListView;

import com.ldm.basic.utils.MeasureHelper;
import com.ldm.basic.utils.SystemTool;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by ldm on 14-8-26. 下拉收缩的view，根据设定的保留高度进行收缩动画，
 * 且将进度通过OnStateListener监听的onScrollListener
 * (float)方法进行返回，这个方法将在onInterceptTouchEvent触发后执行， 也可以通过
 * (仅支持ListView及LScrollView作为滑动载体)
 */
public class LNotBoringActionBarView extends ViewGroup {

    public final int DEFAULT_HEADER_HEIGHT = (int) SystemTool.dipToPx(50);
    private int headerHeight, maxScrollDistance;
    private ListView cList;// child1如果是listView时，这个变量将被赋值
    private LScrollView cScroll;// child1如果是ScrollView时，这个变量将被赋值
    private View _headerView;
    private View headerView;// 布局1
    private boolean isScroll;
    private float oldScale;
    private Rect headRect;
    private boolean fixedHeadView;// 固定HeadView
    private boolean openAdsorptionFunc;//是否开启吸附功能，开启后headerView将只有展开和最小化两个状态
    private OnInterceptTouchEventListener onInterceptTouchEventListener;

    private OnStateListener onStateListener;

    public LNotBoringActionBarView(Context context) {
        super(context);
        init();
    }

    public LNotBoringActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        fixedHeadView = false;
        // 根据屏幕密度设置高度
        headerHeight = (int) (DEFAULT_HEADER_HEIGHT * (SystemTool.DENSITY <= 0 ? 1 : SystemTool.DENSITY));
    }

    /**
     * 设置true后 将开启自动吸附功能，此时headerView只有展开和最小化的状态
     *
     * @param bool true开启
     */
    public void setOpenAdsorptionFunc(boolean bool) {
        openAdsorptionFunc = true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        headerView = getChildAt(1);
        View v0 = getChildAt(0);
        if (v0 instanceof ListView) {
            cList = (ListView) v0;
            cList.setOnScrollListener(onScrollListener);
            _headerView = new View(getContext());
            initHeaderView();
            cList.addHeaderView(_headerView, null, false);
        } else if (v0 instanceof LScrollView) {
            cScroll = (LScrollView) v0;
            cScroll.setOnScrollListener(lScrollListener);
            _headerView = new View(getContext());
            initHeaderView();
            ViewGroup node = (ViewGroup) cScroll.getChildAt(0);
            if (node != null) {
                node.addView(_headerView, 0);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (onStateListener != null) {
            onStateListener.onFinishInflate();
        }

    }

    private void initHeaderView() {
        if (_headerView == null) {
            throw new NullPointerException("请检查LNotBoringActionBarView的child 0是ListView或ScrollView ！");
        }

        LayoutParams lp = _headerView.getLayoutParams();
        final int height = MeasureHelper.getHeightToPixel(headerView);
        if (lp == null) {
            lp = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, height);
        } else {
            lp.height = height;
        }
        _headerView.setLayoutParams(lp);

        maxScrollDistance = height - headerHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View v0 = getChildAt(0), v1 = getChildAt(1);
        if (v0 != null && v1 != null) {
            v0.measure(widthMeasureSpec, heightMeasureSpec);
            v1.measure(widthMeasureSpec, MeasureHelper.getHeight(v1, heightMeasureSpec));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View v0 = getChildAt(0), v1 = getChildAt(1);
        if (v0 != null && v1 != null) {
            if (headRect == null || headRect.top <= 0 || changed) {
                v1.layout(0, 0, v1.getMeasuredWidth(), v1.getMeasuredHeight());
                headRect = new Rect();
                headRect.set(0, 0, v1.getMeasuredWidth(), v1.getMeasuredHeight());
            } else {
                v1.layout(headRect.left, headRect.top, headRect.right, headRect.bottom);
            }
            if (cList != null || cScroll != null) {
                v0.layout(0, 0, v0.getMeasuredWidth(), v0.getMeasuredHeight());
            } else {
                v0.layout(0, v1.getMeasuredHeight(), v0.getMeasuredWidth(), v1.getMeasuredHeight() + v0.getMeasuredHeight());
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (onInterceptTouchEventListener != null) {
            onInterceptTouchEventListener.onInterceptTouchEvent(ev);
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN && openAdsorptionFunc) {
            if (getAnimation() != null) {
                getAnimation().cancel();
            }
        }
        isScroll = true;
        return (cList == null && cScroll == null) && super.onInterceptTouchEvent(ev);
    }

    // ListView滑动监听
    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0 || firstVisibleItem == 1) {
                int scrollY;
                if (firstVisibleItem == 0) {
                    scrollY = getScrollY(view);
                } else {// 这里防止Item高度过低，导致的无法全部隐藏HeadView问题
                    scrollY = maxScrollDistance;
                }
                final int s = -Math.min(maxScrollDistance, scrollY);
                if (headRect != null && isScroll) {
                    resetHeaderPosition(s);
                    if (onStateListener != null) {
                        final float scale = 1 - (Math.abs(s) * 1.0f / maxScrollDistance);

                        if (scale != oldScale) {
                            onStateListener.onScrollListener(scale);
                            oldScale = scale;
                        }
                    }
                }
            }
        }
    };

    private LScrollView.OnScrollListener lScrollListener = new LScrollView.OnScrollListener() {
        @Override
        public void onScroll(LScrollView v, int scrollY, int l, int t, int oldL, int oldT) {
            final int s = -Math.min(maxScrollDistance, scrollY);
            if (headRect != null && isScroll) {
                resetHeaderPosition(s);
                if (onStateListener != null) {
                    float scale = 1 - (Math.abs(s) * 1.0f / maxScrollDistance);
                    scale = scale > 1 ? 1 : scale;
                    if (scale != oldScale) {
                        onStateListener.onScrollListener(scale);
                        oldScale = scale;
                    }
                }
            }
        }

        @Override
        public void onScrollFinished(float velocity) {
            if (openAdsorptionFunc) {
                if (cScroll.getScrollY() < (maxScrollDistance) && cScroll.getScrollY() > 0) {
                    int end = cScroll.getScrollY() <= maxScrollDistance * 0.50f ? 0 : maxScrollDistance;
                    LNotBoringActionBarView.this.startAnimation(new MyAnimation(cScroll.getScrollY(), end));
                }
            }
        }
    };

    /**
     * 重置headerView位置
     *
     * @param y y坐标
     */
    private void resetHeaderPosition(int y) {
        if (fixedHeadView)
            return;
        if (headRect.top != y) {
            headRect.set(headRect.left, y, headRect.right, headerView.getMeasuredHeight() + y);
            ViewHelper.setTranslationY(headerView, y);
        }
    }

    /**
     * 开启
     */
    public void open() {
        if (cList != null) {
            resetHeaderPosition(0);
            cList.setSelectionFromTop(0, 0);
        } else if (cScroll != null) {
            resetHeaderPosition(0);
            cScroll.scrollToTop();
        }
    }

    /**
     * 获取HeadRect 值，当界面切换时
     *
     * @param rect Rect
     */
    public void getHeadRect(Rect rect) {
        if (rect != null && headRect != null) {
            rect.set(headRect.left, headRect.top, headRect.right, headRect.bottom);
        }
    }

    /**
     * 关闭
     */
    public void close() {
        if (cList != null) {
            resetHeaderPosition(-getScrollY(cList));
            cList.setSelectionFromTop(0, maxScrollDistance);
        } else if (cScroll != null) {
            resetHeaderPosition(-cScroll.getScrollY());
            cScroll.scrollTo(0, maxScrollDistance);
        }
    }

    /**
     * 获取AbsListView的getScrollY值
     *
     * @param v AbsListView
     * @return scrollY
     */
    public int getScrollY(AbsListView v) {
        View c = v.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = v.getFirstVisiblePosition();
        int top = c.getTop();
        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = this.headerHeight;
        }
        return -top + firstVisiblePosition * c.getHeight() + headerHeight;
    }

    // 返回最大的可移动距离
    public int getMaxScrollDistance() {
        return maxScrollDistance;
    }

    /**
     * 设置列表滚动
     *
     * @param y 当前所需要的位置
     */
    void setChildScrollY(int y) {
        if (cList != null) {
            cList.scrollTo(0, y);
        } else if (cScroll != null) {
            cScroll.scrollTo(0, y);
        }
    }

    /**
     * 设置移动后保留部分的高度 (这个方法需要在findViewById抓到view后立即设置才会有效)
     *
     * @param headerHeight 默认45*DENSITY
     */
    public void setHeaderHeight(int headerHeight) {
        this.headerHeight = headerHeight;

        final int height = MeasureHelper.getHeightToPixel(headerView);

        maxScrollDistance = height - headerHeight;
    }

    // 设置回调接口
    public void setOnStateListener(OnStateListener onStateListener) {
        this.onStateListener = onStateListener;
    }

    /**
     * 固定HeadView,滑动时将执行覆盖HeadView
     */
    public void fixedHeadView() {
        fixedHeadView = true;
    }

    /**
     * 创建时设置true后将会在初始化时进行回调OnStateListener.onScrollListener方法
     * （这时的onScrollListener(float scale)中的scale将会时0）
     *
     * @param isScroll true初始化时将会调用OnStateListener.onScrollListener方法
     *                 false则需要用户第一次触摸屏幕时执行
     */
    public void setScroll(boolean isScroll) {
        this.isScroll = isScroll;
    }

    // 状态变更接口
    public interface OnStateListener {
        /**
         * 如果界面布局中使用了listView,需要在这个接口中调用setAdapter
         */
        public void onFinishInflate();

        /**
         * 1展开 0达到最小状态
         *
         * @param scale 1～0
         */
        public void onScrollListener(float scale);

    }

    public void setOnInterceptTouchEventListener(OnInterceptTouchEventListener onInterceptTouchEventListener) {
        this.onInterceptTouchEventListener = onInterceptTouchEventListener;
    }

    public interface OnInterceptTouchEventListener {
        void onInterceptTouchEvent(MotionEvent ev);
    }

    private class MyAnimation extends Animation {

        final float start, d;

        public MyAnimation(float start, float end) {
            this.start = start;
            this.d = end - start;
            this.setDuration((long) Math.max(Math.min(d * 0.35f, 350), 180));
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            setChildScrollY((int) (start + d * interpolatedTime));
        }
    }
}
