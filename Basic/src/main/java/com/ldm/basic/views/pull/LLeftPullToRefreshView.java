package com.ldm.basic.views.pull;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.ldm.basic.utils.MeasureHelper;
import com.ldm.basic.views.pull.anim.BasicPullViewAnimation;

/**
 * Created by ldm  on 16/6/1.
 * 左滑刷新控件
 * <p/>
 * 使用方法：
 * <p/>
 * <LLeftPullToRefreshView>
 * <LHorizontalScrollView>
 * <LinearLayout android:id="@+id/rootView" android:orientation="horizontal">
 * 这里可以动态添加ChildView
 * <p>
 * </LinearLayout>
 * </LHorizontalScrollView>
 * <LLeftPullToRefreshView/>
 */
public class LLeftPullToRefreshView extends ViewGroup {

    public static final int RELEASE_TO_REFRESH = 0;// 松开刷新状态
    public static final int PULL_TO_REFRESH = 1;// 左拉刷新状态
    public static final int RELEASE_REFRESHING = 2;// 正在刷新数据的状态
    public static final int DONE = 3;// 完成，表示可以回到下拉刷新状态了
    public int currentState = DONE;
    private boolean lockTouch;//设置true后将被锁住touch操作
    private Scroller scroller;
    private LHorizontalScrollView scrollView;
    private View pullView;
    private static final float REDUCTION_RATIO = 0.35f;//减速比例
    private static final int DURATION_TIME = 650;// 放手时自动移动的持续时间
    private int MAX_PULL_DISTANCE = 200;//达到这个距离后会触发回掉监听
    private float touchMoveX, touchMoveY;//存储TOUCH坐标
    private OnStateListener onStateListener;
    private BasicPullViewAnimation basicPullViewAnimation;
    private boolean state;

    public LLeftPullToRefreshView(Context context) {
        super(context);
        init(context);
    }

    public LLeftPullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LLeftPullToRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        scroller = new Scroller(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        scrollView = (LHorizontalScrollView) getChildAt(0);
        pullView = getChildAt(1);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (lockTouch) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                state = false;
                touchDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                final float x = ev.getX();
                final float y = ev.getY();
                final float xDiff = Math.abs(x - touchMoveX);
                final float yDiff = Math.abs(y - touchMoveY);
                if (xDiff * 0.5f > yDiff) {
                    if (x != touchMoveX) {
                        if (x > touchMoveX) {//向右滑动
                            state = isRightScroll();
                        } else {//向左滑动
                            state = isLeftScroll();
                        }
                    }
                }
                touchMoveX = ev.getX();
                touchMoveY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                touchUp();
                break;
            default:
                break;
        }
        if (state) {
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
        return state;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                touchMove(event.getX());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                touchUp();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View v1 = getChildAt(0);
        MeasureHelper.measure(v1, widthMeasureSpec, heightMeasureSpec);
        View v2 = getChildAt(1);
        MeasureHelper.measure(v2, widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View v1 = getChildAt(0);
        v1.layout(0, 0, v1.getMeasuredWidth(), v1.getMeasuredHeight());
        View v2 = getChildAt(1);
        v2.layout(v1.getMeasuredWidth(), 0, v1.getMeasuredWidth() + v2.getMeasuredWidth(), v2.getMeasuredHeight());
        //使用v2.getMeasuredWidth()作为达到刷新数据的距离
        MAX_PULL_DISTANCE = v2.getMeasuredWidth();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            if (basicPullViewAnimation != null) {
                basicPullViewAnimation.pullProgress(pullView, MAX_PULL_DISTANCE, getScrollX());
            }
            invalidate();
        } else {
            if (currentState == DONE) {
                if (basicPullViewAnimation != null) {
                    basicPullViewAnimation.scrollStop();
                }
            }
        }
    }

    /**
     * 是否可以向右滑动
     *
     * @return true/false
     */
    private boolean isRightScroll() {
        return getScrollX() > 0 || (getScrollX() < 0 && scrollView.isStart());
    }

    /**
     * 是否可以向左拉动
     *
     * @return true/false
     */
    private boolean isLeftScroll() {
        return scrollView.isEnd() && getScrollX() >= 0;
    }


    /**
     * Touch Up
     */
    private void touchUp() {
        state = false;
        if (getScrollX() > MAX_PULL_DISTANCE) {
            lockTouch = true;
            currentState = RELEASE_REFRESHING;
            if (onStateListener != null) {
                onStateListener.onRefresh();
            }
            if (basicPullViewAnimation != null) {
                basicPullViewAnimation.refresh(pullView);
                basicPullViewAnimation.setState(RELEASE_REFRESHING);
            }
        } else {
            currentState = RELEASE_REFRESHING;
            if (basicPullViewAnimation != null) {
                basicPullViewAnimation.setState(RELEASE_REFRESHING);
            }
        }
        if (getScrollX() != 0) {
            scroller.startScroll(getScrollX(), 0, -getScrollX(), 0, (int) (DURATION_TIME + Math.abs(getScrollX() * 0.35)));
            invalidate();
        }
    }

    /**
     * Touch move
     *
     * @param x event.getX()
     */
    private void touchMove(final float x) {
        boolean selfTouch;
        float nx = 0;
        if (x != touchMoveX) {
            if (x > touchMoveX) {//向右滑动
                selfTouch = isRightScroll();
                if (selfTouch) {
                    nx = touchMoveX - x;
                    if (getScrollX() + nx < 0) {
                        nx = 0 - getScrollX();
                    }
                }
            } else {//向左滑动
                selfTouch = isLeftScroll();
                if (selfTouch) {
                    nx = getOffDistance(x);
                }
            }
            if (selfTouch) {
                scrollBy((int) nx, 0);
            }
            if (basicPullViewAnimation != null) {
                basicPullViewAnimation.pullProgress(pullView, MAX_PULL_DISTANCE, getScrollX());
            }
            touchMoveX = x;
            if (getScrollX() >= MAX_PULL_DISTANCE) {
                currentState = RELEASE_TO_REFRESH;
                if (basicPullViewAnimation != null && basicPullViewAnimation.getState() != currentState) {
                    basicPullViewAnimation.releaseToRefresh(pullView);
                    basicPullViewAnimation.setState(currentState);
                }
            } else {
                currentState = PULL_TO_REFRESH;
                if (basicPullViewAnimation != null && basicPullViewAnimation.getState() != currentState) {
                    basicPullViewAnimation.pullToRefresh(pullView);
                    basicPullViewAnimation.setState(currentState);
                }
            }

        }
    }

    /**
     * 返回一个有效的拉动距离
     *
     * @param x touchEvent X
     * @return off distance
     */
    private float getOffDistance(float x) {
        return (touchMoveX - x) * REDUCTION_RATIO;
    }

    /**
     * 处理用户按下时的事件
     *
     * @param event MotionEvent
     */
    private void touchDown(MotionEvent event) {
        // 结束动画
        if (scroller != null) {
            if (!scroller.isFinished()) {
                scroller.abortAnimation();
            }
        }
        touchMoveX = event.getX();
        touchMoveY = event.getY();
    }

    /**
     * 刷新完成调用这个方法可以进行状态恢复
     */
    public void refreshAllCompleted() {
        if (currentState == RELEASE_REFRESHING) {
            currentState = DONE;
            if (basicPullViewAnimation != null) {
                basicPullViewAnimation.pullDone();
            }
            scroller.startScroll(0, getScrollY(), 0, -getScrollY(), (int) (DURATION_TIME + Math.abs(getScrollY() * 0.35)));
            invalidate();
        }
        lockTouch = false;
    }

    /**
     * 设置最大的拉动触发距离
     *
     * @param maxPullDistance 距离
     */
    public void setMaxPullDistance(int maxPullDistance) {
        this.MAX_PULL_DISTANCE = maxPullDistance;
    }

    /**
     * 设置状态监听
     *
     * @param onStateListener OnStateListener
     */
    public void setOnStateListener(OnStateListener onStateListener) {
        this.onStateListener = onStateListener;
    }

    /**
     * 设置一个拉动动画
     *
     * @param basicPullViewAnimation BasicPullViewAnimation
     */
    public void setBasicPullViewAnimation(BasicPullViewAnimation basicPullViewAnimation) {
        this.basicPullViewAnimation = basicPullViewAnimation;
    }

    /**
     * 状态接口，onRefresh()触发时表示达到了最大滑动距离
     */
    public interface OnStateListener {
        void onRefresh();
    }

}
