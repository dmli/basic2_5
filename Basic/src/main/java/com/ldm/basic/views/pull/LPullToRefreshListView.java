package com.ldm.basic.views.pull;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.ldm.basic.views.pull.listener.OnPullAbsScrollListener;
import com.ldm.basic.utils.MeasureHelper;
import com.ldm.basic.views.pull.anim.BasicPullViewAnimation;

/**
 * Created by ldm on 16/4/7.
 * 分页支持所有实现了OnAbsScrollListener接口的View类
 */
public class LPullToRefreshListView extends ViewGroup {

    public static final int RELEASE_TO_REFRESH = 0;// 松开刷新状态
    public static final int RELEASE_PULL_TO_REFRESH = 1;// 下拉刷新状态
    public static final int RELEASE_REFRESHING = 2;// 正在刷新数据的状态
    public static final int DONE = 3;// 完成，表示可以回到下拉刷新状态了

    public static final int LOAD_PAGER_REFRESH = 10;// 松开加载分页状态
    public static final int LOAD_PULL_TO_REFRESH = 11;// 上拉加载状态
    public static final int LOAD_REFRESHING = 12;// 正在加载分页数据的状态

    public int currentState = DONE;

    /**
     * 本次被触发的功能
     * 取值RELEASE_REFRESHING | LOAD_REFRESHING
     * 默认0
     */
    private int currentAction;

    private boolean state;
    private Scroller scroller;
    private boolean isNext;// 是否有下一页数据
    private float touchMoveX, touchMoveY;
    private OnPullAbsScrollListener lAbsScrollState;
    private boolean lockTouch;//设置true后将被锁住touch操作

    private static final float REDUCTION_RATIO = 0.35f;//减速比例
    private static final int DURATION_TIME = 650;// 放手时自动移动的持续时间

    private int loadViewHeight;//显示加载中状态的View高度
    private int headViewHeight;//下拉刷新的View高度
    private View headView, loadView;

    /**
     * 本次执行的模式，当用户先触发了RELEASE_PULL_TO_REFRESH操作时，本次touch将不允许执行相反的LOAD_PULL_TO_REFRESH操作
     */
    private int currentMode;

    private BasicPullViewAnimation headAnimation;
    private BasicPullViewAnimation loadAnimation;

    /**
     * 监听
     */
    private OnLoadPagingListener onLoadPagingListener;
    private OnRefreshAllListener onRefreshAllListener;


    public LPullToRefreshListView(Context context) {
        super(context);
        init(context);
    }

    public LPullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LPullToRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        scroller = new Scroller(context);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        /**
         * 滑动的控件需要实现LAbsScrollState接口
         */
        View c1 = null;
        if (getChildCount() == 2) {
            c1 = getChildAt(0);
        } else if (getChildCount() == 3) {
            c1 = getChildAt(1);
        }
        if (c1 == null) {
            throw new NullPointerException("not find class impl OnPullAbsScrollListener! ");
        }
        lAbsScrollState = (OnPullAbsScrollListener) c1;
        LayoutParams lp = c1.getLayoutParams();
        if (lp.width != LayoutParams.MATCH_PARENT) {
            lp.width = LayoutParams.MATCH_PARENT;
            c1.setLayoutParams(lp);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isOpenRefreshAllListFc()) {
            View v0 = getChildAt(0), v1 = getChildAt(1);
            v0.measure(widthMeasureSpec, MeasureHelper.getHeight(v0, heightMeasureSpec));
            v1.measure(widthMeasureSpec, heightMeasureSpec);
            /**
             * 这个给GridView类型的控件使用的
             */
            if (getChildCount() == 3) {
                View v2 = getChildAt(2);
                v2.measure(widthMeasureSpec, MeasureHelper.getHeight(v2, heightMeasureSpec));
            }
        } else {
            View v0 = getChildAt(0), v1 = getChildAt(1);
            v0.measure(widthMeasureSpec, heightMeasureSpec);
            v1.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isOpenRefreshAllListFc()) {
            onLayoutOpenRefreshAllFc();
        } else {
            onLayoutNotOpenRefreshAllFc();
        }
        /**
         * child至少有两个，如果不足会抛出异常
         */
        if (isOpenRefreshAllListFc()) {
            headViewHeight = getChildAt(0).getMeasuredHeight();//记录这个头部的高度，后面做动画时作为参数
            if (getChildCount() == 3) {
                loadViewHeight = getChildAt(2).getMeasuredHeight();
            }
        } else {
            loadViewHeight = getChildAt(1).getMeasuredHeight();
        }
    }


    /**
     * 用户没有开启刷新全部时触发
     */
    private void onLayoutNotOpenRefreshAllFc() {
        View v0 = getChildAt(0), v1 = getChildAt(1);
        loadView = v1;
        v0.layout(0, 0, v0.getMeasuredWidth(), v0.getMeasuredHeight());
        v1.layout(0, v0.getMeasuredHeight(), v1.getMeasuredWidth(), v0.getMeasuredHeight() + v1.getMeasuredHeight());
    }

    /**
     * 用户开启刷新全部时触发
     */
    private void onLayoutOpenRefreshAllFc() {
        View v0 = getChildAt(0), v1 = getChildAt(1);
        v0.layout(0, -v0.getMeasuredHeight(), v0.getMeasuredWidth(), 0);
        v1.layout(0, 0, v1.getMeasuredWidth(), v1.getMeasuredHeight());
        if (getChildCount() == 3) {
            View v2 = getChildAt(2);
            loadView = v2;
            v2.layout(0, v1.getMeasuredHeight(), v2.getMeasuredWidth(), v1.getMeasuredHeight() + v2.getMeasuredHeight());
        }
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

                if (yDiff * 0.5f > xDiff) {
                    if (y != touchMoveY) {
                        if (y > touchMoveY) {//向下滑动
                            state = lAbsScrollState.isMoveDown();
                        } else {//向上滑动
                            state = isNext() && lAbsScrollState.isMoveUp();
                        }
                    }
                }
                touchMoveX = ev.getX();
                touchMoveY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touchUp();
                break;
            default:
                break;
        }
        return state;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (lockTouch) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event.getY());
                touchMoveX = event.getX();
                touchMoveY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touchUp();
                break;
            default:
                break;
        }
        return true;
    }


    /**
     * 处理用户移动手势的事件
     *
     * @param y 新的x坐标
     */
    private void touchMove(float y) {
        if (getScrollY() > 0) {
            currentState = LOAD_PULL_TO_REFRESH;
            if (loadAnimation != null) {
                if (loadAnimation.getState() != currentState) {
                    loadAnimation.pullToRefresh(headView);
                    loadAnimation.setState(currentState);
                }
                loadAnimation.pullProgress(loadView, loadViewHeight, getScrollY());

            }
            if (getScrollY() > loadViewHeight) {
                currentState = LOAD_PAGER_REFRESH;
                if (loadAnimation != null && loadAnimation.getState() != currentState) {
                    loadAnimation.releaseToRefresh(loadView);
                    loadAnimation.setState(currentState);
                }
            }
        } else {
            currentState = RELEASE_PULL_TO_REFRESH;
            if (headAnimation != null) {
                if (headAnimation.getState() != currentState) {
                    headAnimation.pullToRefresh(headView);
                    headAnimation.setState(currentState);
                }
                headAnimation.pullProgress(headView, headViewHeight, -getScrollY());
            }

            if (getScrollY() < -headViewHeight) {
                currentState = RELEASE_TO_REFRESH;
                if (headAnimation != null && headAnimation.getState() != currentState) {
                    headAnimation.releaseToRefresh(headView);
                    headAnimation.setState(currentState);
                }
            }
        }

        if (y != touchMoveY) {
            if (y > touchMoveY) {//向下滑动
                scrollBy(0, getMoveDownDistance(y));
            } else {//向上滑动
                //如果偏移量>0时 菜单属于展开状态，此时优先关闭菜单
                scrollBy(0, getMoveUpDistance(y));
            }
        }
        if (currentMode == 0 && getScrollY() != 0) {
            currentMode = getScrollY() > 0 ? LOAD_PULL_TO_REFRESH : RELEASE_PULL_TO_REFRESH;
        }
    }

    /**
     * 返回一个适合的上滑距离
     *
     * @param y 新的Y坐标
     * @return int 距离 可能为0
     */
    private int getMoveUpDistance(float y) {
        int newY;
        if (getScrollY() > 0) {
            newY = (int) ((touchMoveY - y) * REDUCTION_RATIO);
        } else {
            newY = (int) (touchMoveY - y);
        }

        if (currentMode == RELEASE_PULL_TO_REFRESH && getScrollY() + newY > 0) {
            newY = -getScrollY();
        }
        return newY;
    }

    /**
     * 返回一个适合的下滑距离
     *
     * @param y 新的Y坐标
     * @return int 距离 可能为0
     */
    private int getMoveDownDistance(float y) {
        int newY = (int) ((touchMoveY - y) * REDUCTION_RATIO);
        if (currentMode == LOAD_PULL_TO_REFRESH && getScrollY() + newY < 0) {
            newY = -getScrollY();
        }
        return newY;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            // 产生了动画效果，根据当前值 每次滚动一点
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
            if (headAnimation != null) {
                headAnimation.pullProgress(headView, headViewHeight, -getScrollY());
            }
        } else {
            if (currentState == DONE) {
                if (currentAction == RELEASE_REFRESHING) {
                    if (headAnimation != null) {
                        headAnimation.scrollStop();
                    }
                } else if (currentAction == LOAD_REFRESHING) {
                    if (loadAnimation != null) {
                        loadAnimation.scrollStop();
                    }
                }
            }
            setChildrenCache(false);
        }
    }

    /**
     * 处理用户手势操作离开屏幕时
     */
    private void touchUp() {
        currentMode = 0;
        int off;
        switch (currentState) {
            case RELEASE_TO_REFRESH: {
                off = -loadViewHeight;
                currentState = RELEASE_REFRESHING;
                currentAction = RELEASE_REFRESHING;
                if (headAnimation != null) {
                    headAnimation.refresh(headView);
                    headAnimation.setState(currentState);
                }
                refreshAll();
            }
            break;
            case LOAD_PAGER_REFRESH: {
                off = loadViewHeight;
                currentState = LOAD_REFRESHING;
                currentAction = LOAD_PAGER_REFRESH;
                if (loadAnimation != null) {
                    loadAnimation.refresh(loadView);
                    loadAnimation.setState(currentState);
                }
                loadPager();
            }
            break;
            default:
                off = 0;
                currentAction = 0;//0表示本次没有执行任何功能
                if (headAnimation != null) {
                    headAnimation.setState(currentState);
                }
                if (loadAnimation != null) {
                    loadAnimation.setState(currentState);
                }
                break;
        }
        int distance = -getScrollY() + off;
        int duration = (int) (DURATION_TIME + Math.abs(distance * 0.35));
        if (getScrollY() != 0) {
            scroller.startScroll(0, getScrollY(), 0, distance, duration);
            invalidate();
        } else {
            setChildrenCache(false);
        }
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
        if (getAnimation() != null) {
            clearAnimation();
        }
        touchMoveX = event.getX();
        touchMoveY = event.getY();
        setChildrenCache(true);
    }

    /**
     * 执行刷新全部功能
     */
    private void refreshAll() {
        //锁住Touch操作
        lockTouch = true;
        //触发刷新全部回调功能
        if (onRefreshAllListener != null) {
            onRefreshAllListener.onRefreshComplete();
        }
    }

    /**
     * 加载分页
     */
    private void loadPager() {
        //锁住Touch操作
        lockTouch = true;
        //触发刷新分页
        if (onLoadPagingListener != null) {
            onLoadPagingListener.onLoadComplete();
        }
    }

    /**
     * 刷新完成调用这个方法可以进行状态恢复
     */
    public void refreshAllCompleted() {
        if (currentState == RELEASE_REFRESHING) {
            currentState = DONE;
            if (headAnimation != null) {
                headAnimation.pullDone();
            }
            scroller.startScroll(0, getScrollY(), 0, -getScrollY(), (int) (DURATION_TIME + Math.abs(getScrollY() * 0.35)));
            invalidate();
        }
        lockTouch = false;
    }


    /**
     * 加载完成,保留分页功能
     */
    public void loadPagingCompleted() {
        loadPagingCompleted(true);
    }

    /**
     * 加载完成
     *
     * @param keep true保留分页 false去掉分页
     */
    public void loadPagingCompleted(boolean keep) {
        if (currentState == LOAD_REFRESHING) {
            scroller.startScroll(0, getScrollY(), 0, -getScrollY(), (int) (DURATION_TIME + Math.abs(getScrollY() * 0.35)));
            invalidate();
        }
        isNext = keep;
        lockTouch = false;
        currentState = DONE;
    }

    /**
     * 开启分页功能
     */
    public void openLoadPagingFunc() {
        isNext = true;
    }

    /**
     * true表示有下一页数据
     *
     * @return true/false
     */
    public boolean isNext() {
        return isNext;
    }

    /**
     * 下拉刷新时将会根据当前状态对BasicPullViewAnimation进行回调
     *
     * @param headAnimation BasicPullViewAnimation
     */
    public void setHeadAnimation(BasicPullViewAnimation headAnimation) {
        this.headAnimation = headAnimation;
    }

    /**
     * 上拉刷新时将会根据当前状态对BasicPullViewAnimation进行回调
     *
     * @param loadAnimation BasicPullViewAnimation
     */
    public void setLoadAnimation(BasicPullViewAnimation loadAnimation) {
        this.loadAnimation = loadAnimation;
    }

    /**
     * 设置缓存是否开启，true开启
     */
    void setChildrenCache(boolean bool) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            if (v != null) {
                v.setDrawingCacheEnabled(bool);
            }
        }
    }

    /**
     * 返回是否开启了刷新全部功能
     *
     * @return true/false
     */
    private boolean isOpenRefreshAllListFc() {
        return onRefreshAllListener != null || getChildCount() == 3;
    }

    /**
     * 设置分页监听
     *
     * @param onLoadPagingListener OnLoadPagingListener
     */
    public void setOnLoadPagingListener(OnLoadPagingListener onLoadPagingListener) {
        if (onLoadPagingListener != null) {
            this.onLoadPagingListener = onLoadPagingListener;
            openLoadPagingFunc();
            if (loadView == null) {
                loadView = getChildAt(isOpenRefreshAllListFc() ? 2 : 1);
            }
            if (loadAnimation != null) {
                loadAnimation.refresh(loadView);
            }
        }
    }

    /**
     * 设置刷新全部监听
     *
     * @param onRefreshAllListener OnRefreshAllListener
     */
    public void setOnRefreshAllListener(OnRefreshAllListener onRefreshAllListener) {
        if (onRefreshAllListener != null) {
            this.onRefreshAllListener = onRefreshAllListener;
            if (headView == null) {
                headView = getChildAt(0);
            }
            if (headAnimation != null) {
                headAnimation.refresh(headView);
            }
        }
    }


    /**
     * 设置分页监听
     */
    public interface OnLoadPagingListener {
        void onLoadComplete();
    }


    public interface OnRefreshAllListener {
        void onRefreshComplete();
    }

}
