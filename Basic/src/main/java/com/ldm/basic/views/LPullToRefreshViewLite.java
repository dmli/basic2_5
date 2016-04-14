
package com.ldm.basic.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Scroller;

import com.ldm.basic.utils.MeasureHelper;

/**
 * Created by ldm on 12/11/12.
 * 分页内部可以嵌套一个GridView，并为它提供一个分页功能，使用者需要使用setOnLoadingListener方法来设置分页监听
 * <p/>
 * 当用户使用这个ViewGroup时，用户至少需要开启一个功能，如果分页及刷新都没有被开启则PullToRefreshView是不可用的
 * <p/>
 * 注：
 * 这是一个删减版，原版可以支持
 * extends AbsListView
 * RecyclerView
 * PLA_AbsListView
 */
public class LPullToRefreshViewLite extends ViewGroup {

    public static final int RELEASE_TO_REFRESH = 0;// 松开刷新状态
    public static final int PULL_TO_REFRESH = 1;// 下拉刷新状态
    public static final int REFRESHING = 2;// 正在刷新状态
    public static final int DONE = 3;// 完成，表示可以回到下拉刷新状态了
    private static int MAX_MOVE_HEIGHT;//底部进度条的最高移动距离
    private static int PULL_TO_REFRESH_HEIGHT, MAX_TO_REFRESH_HEIGHT;//当滑动距离超出这个范围时已经达到了下啦刷新状态

    private int lState = DONE;
    private AbsListView listView1;
    private View loadingView, headView;
    private int originalTop, scrollOffY;
    private boolean isLoadRunning;// 是否处于刷新中
    private boolean isNext;// 是否有下一页数据
    private boolean state;
    private int currentViewState = 0;
    private float touchMoveX, touchMoveY;
    private boolean lockTouch;//默认false,设置true将锁住刷新全部及分页功能
    private boolean lockTouchRefreshAll;//默认false,设置true将锁住刷新全部功能
    //本次动画将要移动的长度    方向1向上  -1向下 0原地停留
    private static final float REDUCTION_RATIO = 0.35f;//减速比例
    private static final int DURATION_TIME = 650;// 放手时自动移动的持续时间

    private Scroller scroller;
    private AbsListView.OnScrollListener onScrollListener;

    private OnLoadPagingListener onLoadPagingListener;
    private OnRefreshAllListener onRefreshAllListener;
    private BasicHeadAnimation headAnimation;

    public LPullToRefreshViewLite(Context context) {
        super(context);
        init(context);
    }

    public LPullToRefreshViewLite(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LPullToRefreshViewLite(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (scrollOffY != 0) {
            scrollBy(0, -scrollOffY);
            invalidate();
        }
        currentViewState = 1;
    }

    /**
     * 设置一个偏移量
     * 这个方法尽量在onFinishInflate()方法被触发前执行
     *
     * @param scrollOffY 偏移量
     */
    public void setScrollOffY(int scrollOffY) {
        this.scrollOffY = scrollOffY;
        if (scrollOffY != 0) {
            switch (currentViewState) {
                case 2:
                    /**
                     * currentViewState == 2时case需要落到 case1中继续执行
                     */
                    PULL_TO_REFRESH_HEIGHT += scrollOffY;
                    MAX_TO_REFRESH_HEIGHT += scrollOffY;
                case 1:
                    scrollBy(0, -scrollOffY);
                    invalidate();
                    break;
                default:
                    break;
            }
        }
    }

    private void init(Context context) {
        this.lockTouch = false;
        this.lockTouchRefreshAll = false;
        this.currentViewState = 0;
        this.isNext = true;
        this.scrollOffY = 0;
        this.originalTop = 0;
        this.scroller = new Scroller(context);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        MAX_MOVE_HEIGHT = (int) (85 * dm.density);
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
            v1.measure(widthMeasureSpec, MeasureHelper.getHeight(v1, heightMeasureSpec));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        currentViewState = 2;
        if (isOpenRefreshAllListFc()) {
            onLayoutOpenRefreshAllFc(changed);
        } else {
            onLayoutNotOpenRefreshAllFc(changed);
        }
    }

    /**
     * 用户没有开启刷新全部时触发
     *
     * @param changed bool
     */
    private void onLayoutNotOpenRefreshAllFc(boolean changed) {
        View v0 = getChildAt(0), v1 = getChildAt(1);
        v0.layout(0, 0, v0.getMeasuredWidth(), v0.getMeasuredHeight());
        v1.layout(0, v0.getMeasuredHeight(), v1.getMeasuredWidth(), v0.getMeasuredHeight() + v1.getMeasuredHeight());
        if (getWidth() > 0 || changed) {
            originalTop = v1.getTop();
        }
    }

    /**
     * 用户开启刷新全部时触发
     *
     * @param changed bool
     */
    private void onLayoutOpenRefreshAllFc(boolean changed) {
        View v0 = getChildAt(0), v1 = getChildAt(1);
        v0.layout(0, -v0.getMeasuredHeight(), v0.getMeasuredWidth(), 0);
        PULL_TO_REFRESH_HEIGHT = v0.getMeasuredHeight() + scrollOffY;//记录这个头部的高度，后面做动画时作为参数
        MAX_TO_REFRESH_HEIGHT = v0.getMeasuredHeight() + scrollOffY;
        v1.layout(0, 0, v1.getMeasuredWidth(), v1.getMeasuredHeight());
        if (getChildCount() == 3) {
            View v2 = getChildAt(2);
            v2.layout(0, v1.getMeasuredHeight(), v2.getMeasuredWidth(), v1.getMeasuredHeight() + v2.getMeasuredHeight());
            if (getWidth() > 0 || changed) {
                originalTop = v2.getTop();
            }
        }
    }

    public ViewGroup getListView() {
        return listView1;
    }

    /**
     * 锁住Touch操作
     */
    public void lockTouch() {
        this.lockTouch = true;
    }

    /**
     * 解开touch操作
     */
    public void unlockTouch() {
        this.lockTouch = false;
    }

    /**
     * 锁住Touch操作(仅控制刷新全部功能)
     */
    public void lockTouchRefreshAll() {
        this.lockTouchRefreshAll = true;
    }

    /**
     * 解开touch操作(仅控制刷新全部功能)
     */
    public void unlockTouchRefreshAll() {
        this.lockTouchRefreshAll = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (lockTouch || lockTouchRefreshAll || !isOpenRefreshAllListFc() || isRefreshing() || isLoadRunning()) {//如果没有下啦刷新功能或处于刷新中，不允许对其进行操作
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
                            state = isMoveDown();
                        } else {//向上滑动
                            state = isMoveUp();
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
        lState = state ? PULL_TO_REFRESH : DONE;
        return state;
    }

    /**
     * 返回PullToRefreshView是否处于刷新中
     *
     * @return true刷新中
     */
    public boolean isRefreshing() {
        return lState == REFRESHING;
    }

    /**
     * 返回PullToRefreshView是否处于加载分页数据中
     *
     * @return true加载中
     */
    public boolean isLoadRunning() {
        return isLoadRunning;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isOpenRefreshAllListFc() || isRefreshing() || isLoadRunning()) {//如果没有下啦刷新功能或处于刷新中，不允许对其进行操作
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
     * 处理用户手势操作离开屏幕时
     */
    private void touchUp() {
        state = false;
        int d;
        if (lState == RELEASE_TO_REFRESH) {
            //已经达到松开刷新状态，这里需要设置成REFRESHING状态
            d = -getScrollY() - PULL_TO_REFRESH_HEIGHT;
            if (headAnimation != null) {
                headAnimation.refresh(headView);
            }
            lState = REFRESHING;

        } else {
            d = -getScrollY() - scrollOffY;
            lState = DONE;
        }
        if (headAnimation != null) {
            headAnimation.setState(lState);
        }
        if (getScrollY() == d) {
            setChildrenCache(false);
        } else {
            scroller.startScroll(0, getScrollY(), 0, d, (int) (DURATION_TIME + Math.abs(getScrollY() * 0.35)));
            invalidate();
        }
        if (lState == REFRESHING) {
            refreshAll();
        }
    }

    /**
     * 执行刷新全部功能
     */
    private void refreshAll() {
        //触发刷新全部回调功能
        if (onRefreshAllListener != null) {
            onRefreshAllListener.onRefreshComplete();
        }
    }

    /**
     * 处理用户移动手势的事件
     *
     * @param y 新的x坐标
     */
    private void touchMove(float y) {
        if (Math.abs(getScrollY()) > MAX_TO_REFRESH_HEIGHT) {
            lState = RELEASE_TO_REFRESH;
            if (headAnimation != null && headAnimation.getState() != lState) {
                headAnimation.releaseToRefresh(headView);
                headAnimation.setState(lState);
            }
        } else {
            lState = PULL_TO_REFRESH;
            if (headAnimation != null && headAnimation.getState() != lState) {
                headAnimation.pullToRefresh(headView);
                headAnimation.setState(lState);
            }
        }
        if (y != touchMoveY) {
            if (y > touchMoveY) {//向下滑动
                scrollBy(0, getMoveDownDistance(y));
            } else {//向上滑动
                //如果偏移量>0时 菜单属于展开状态，此时优先关闭菜单
                scrollBy(0, getMoveUpDistance(y));
            }
            pullProgress(-getScrollY());
        }
    }

    /**
     * 用户拉动时的进度
     *
     * @param progress 新的位置
     */
    private void pullProgress(int progress) {
        if (headAnimation != null) {
            headAnimation.pullProgress(headView, PULL_TO_REFRESH_HEIGHT, progress);
            headAnimation.setState(lState);
        }
    }

    /**
     * 是否可以向上滑动
     *
     * @return true可以
     */
    private boolean isMoveUp() {
        return getScrollY() != 0;
    }

    /**
     * 是否可以向下滑动 -这里检测list是否滑动到了顶部
     *
     * @return true可以
     */
    private boolean isMoveDown() {
        boolean state = false;
        if (listView1 != null) {
            state = listView1.getChildCount() <= 0 || (listView1.getFirstVisiblePosition() == 0 && listView1.getChildAt(0).getTop() == listView1.getPaddingTop());
        }
        return state;
    }

    /**
     * 返回一个适合的上滑距离
     *
     * @param y 新的Y坐标
     * @return int 距离 可能为0
     */
    private int getMoveUpDistance(float y) {
        int newY = (int) (touchMoveY - y);
        if (getScrollY() + newY > 0) {
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
        return (int) ((touchMoveY - y) * REDUCTION_RATIO);
    }

    /**
     * 处理用户按下时的事件
     *
     * @param event MotionEvent
     */
    private void touchDown(MotionEvent event) {
        lState = PULL_TO_REFRESH;//设置状态
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

    public void setSelection(int position) {
        if (listView1 != null) {
            listView1.setSelection(position);
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            // 产生了动画效果，根据当前值 每次滚动一点
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            if (headAnimation != null) {
                headAnimation.pullProgress(headView, PULL_TO_REFRESH_HEIGHT, -getScrollY());
            }
            postInvalidate();
        } else {
            if (lState == DONE && headAnimation != null) {
                headAnimation.scrollStop();
            }

            setChildrenCache(false);
        }
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

    public AbsListView getAbsListView() {
        return listView1;
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
     * 加载完成，保留分页
     */
    public void loadPagingCompleted() {
        loadPagingCompleted(true);
    }

    /**
     * 刷新完成调用这个方法可以进行状态恢复
     */
    public void refreshAllCompleted() {
        if (lState == REFRESHING) {
            lState = DONE;
            if (headAnimation != null) {
                headAnimation.pullDone();
            }
            scroller.startScroll(0, getScrollY(), 0, -getScrollY() - scrollOffY, (int) (DURATION_TIME + Math.abs(getScrollY() * 0.35)));
            postInvalidate();
        }
    }

    /**
     * 使用loadPagingCompleted(false); 时需要在setAdapter(...)后使用
     * <p/>
     * 加载完成
     *
     * @param keep true保留分页 false去掉分页
     */
    public void loadPagingCompleted(boolean keep) {
        isNext = keep;
        if (isLoadRunning()) {
            isLoadRunning = false;
            onEnd();
        }
        if (loadingView != null && (listView1 instanceof ListView)) {
            ListView lv = ((ListView) listView1);
            if (keep) {
                loadingView.setVisibility(VISIBLE);
                if (lv.getFooterViewsCount() == 0) {
                    lv.addFooterView(loadingView, null, false);
                }
            } else {
                loadingView.setVisibility(INVISIBLE);
                lv.removeFooterView(loadingView);
            }
        }
    }

    /**
     * 开始加载分页
     */
    public void beginLoadPaging() {
        if (loadingView != null) {
            isLoadRunning = true;
            showLoadView();
            if (this.onLoadPagingListener != null) {
                this.onLoadPagingListener.onLoadComplete();
            }
        } else {//这里主要为onCreate中调用onBegin时做的延时处理
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    beginLoadPaging();
                }
            }, 300);
        }
    }

    /**
     * 启动刷新第一页数据
     */
    public void beginRefreshAll() {
        if (headView != null) {
            if (loadingView != null && (listView1 instanceof ListView)) {
                ListView lv = ((ListView) listView1);
                lv.removeFooterView(loadingView);
            }
            lState = REFRESHING;
            if (headAnimation != null && headAnimation.getState() != lState) {
                headAnimation.refresh(headView);
                headAnimation.setState(lState);
            }
            scroller.startScroll(0, getScrollY(), 0, -MAX_TO_REFRESH_HEIGHT + scrollOffY, DURATION_TIME);
            postInvalidate();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (onRefreshAllListener != null) {
                        onRefreshAllListener.onRefreshComplete();
                    }
                }
            }, DURATION_TIME);

        } else {//这里主要为onCreate中调用onBegin时做的延时处理
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    beginRefreshAll();
                }
            }, 300);
        }
    }

    /**
     * 开始显示加载动画
     */
    public void showLoadView() {
        if (!(listView1 instanceof ListView)) {
            if (loadingView.getAnimation() != null) {
                loadingView.clearAnimation();
            }
            loadingView.startAnimation(a1);
        } else {
            loadPagingCompleted(true);
        }
    }

    /**
     * 手动关闭分页加载动画的方法，不建议调用
     */
    public void onEnd() {
        hideLoadView();
    }

    /**
     * 开始隐藏加载动画
     */
    public void hideLoadView() {
        if (!(listView1 instanceof ListView)) {
            if (loadingView.getAnimation() != null) {
                loadingView.clearAnimation();
            }
            loadingView.startAnimation(a2);
        }
    }

    /**
     * 获取分页View
     * 需要在setOnLoadPagingListener(...)方法后才可以拿到返回值
     *
     * @return ListView footer view
     */
    public View getLoadingView() {
        return loadingView;
    }

    public void setOnScrollListener(AbsListView.OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    private void move(int t) {
        if (loadingView != null) {
            loadingView.layout(0, originalTop + t, loadingView.getRight(), originalTop + loadingView.getHeight() + t);
        }
    }


    /**
     * 设置分页监听
     *
     * @param layoutId             moreViewID
     * @param onLoadPagingListener OnLoadPagingListener
     */
    public void setOnLoadPagingListener(int layoutId, OnLoadPagingListener onLoadPagingListener) {
        if (onLoadPagingListener != null) {
            this.onLoadPagingListener = onLoadPagingListener;
            openLoadPagingFunc();
            if (listView1 == null) {
                View v1 = getChildAt(isOpenRefreshAllListFc() ? 1 : 0);
                if (v1 instanceof AbsListView) {
                    listView1 = (AbsListView) v1;
                    listView1.setOnScrollListener(new MyAbsOnScrollListener());
                }
            }

            if (listView1 instanceof ListView) {
                loadingView = View.inflate(getContext(), layoutId, null);
                loadingView.setVisibility(INVISIBLE);
                ((ListView) listView1).addFooterView(loadingView);
            } else {
                /**
                 * 除ListView外都用弹出式动画
                 */
                this.loadingView = getChildAt(isOpenRefreshAllListFc() ? 2 : 1);
                a1.setDuration(DURATION_TIME);
                a1.setInterpolator(new AnticipateOvershootInterpolator());
                a2.setDuration(DURATION_TIME);
                a2.setInterpolator(new AccelerateInterpolator());
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
            headView = getChildAt(0);
            if (headAnimation != null) {
                headAnimation.refresh(headView);
            }
            if (listView1 == null) {
                View v1 = getChildAt(1);
                if (v1 instanceof AbsListView) {
                    listView1 = (AbsListView) v1;
                    listView1.setOnScrollListener(new MyAbsOnScrollListener());
                }
            }
        }
    }

    /**
     * 返回true表示开启了刷新功能
     *
     * @return true / false
     */
    private boolean isOpenRefreshAllListFc() {
        return onRefreshAllListener != null;
    }

    private Animation a1 = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int top = (int) (MAX_MOVE_HEIGHT * interpolatedTime);
            move(-top);
        }
    };
    private Animation a2 = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int top = (int) (MAX_MOVE_HEIGHT * (1 - interpolatedTime));
            move(-top);
        }
    };

    /**
     * 设置分页监听
     */
    public interface OnLoadPagingListener {
        void onLoadComplete();
    }


    public interface OnRefreshAllListener {
        void onRefreshComplete();
    }

    /**
     * 下拉刷新时将会根据当前状态对BasicHeadAnimation进行回调
     *
     * @param headAnimation BasicPullViewAnimation
     */
    public void setHeadAnimation(BasicHeadAnimation headAnimation) {
        this.headAnimation = headAnimation;
    }

    /**
     * 系统控件的监听
     */
    private class MyAbsOnScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (lState == DONE && !lockTouch && isNext && !isLoadRunning() && onLoadPagingListener != null && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                if (view.getLastVisiblePosition() == (view.getCount() - 1)) {// 滚动到底部
                    isLoadRunning = true;
                    beginLoadPaging();
                }
            }
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (onScrollListener != null) {
                onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    }

    public interface BasicHeadAnimation {

        /**
         * 这里可以做拉动时的动画
         *
         * @param headNode View
         * @param count    Head总高度
         * @param p        当前高度
         */
        void pullProgress(View headNode, int count, int p);

        /**
         * 当用户松开开始刷新时这个方法被触发
         *
         * @param headNode View
         */
        void releaseToRefresh(View headNode);

        /**
         * 自动移动停止，触发refresh方法后这个方法被触发
         */
        void scrollStop();

        /**
         * 处于下啦刷新的状态
         *
         * @param headNode View
         */
        void pullToRefresh(View headNode);

        /**
         * 开始刷新
         *
         * @param headNode View
         */
        void refresh(View headNode);

        /**
         * 用户手指操作完成后触发
         */
        void pullDone();

        /**
         * 返回BasicHeadAnimation中当前的状态
         *
         * @return 需要与PullToRefreshView.lState匹配
         */
        int getState();

        /**
         * 设置状态
         *
         * @param state 与PullToRefreshView.lState匹配
         */
        void setState(int state);
    }
}