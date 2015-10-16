
package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Scroller;

import com.ldm.basic.adapter.BasicAdapter;
import com.ldm.basic.utils.MeasureHelper;
import com.ldm.basic.utils.SystemTool;

/**
 * Created by ldm on 12/11/12.
 * 分页内部可以嵌套一个GridView，并为它提供一个分页功能，使用者需要使用setOnLoadingListener方法来设置分页监听
 * <p/>
 * 当用户使用这个ViewGroup时，用户至少需要开启一个功能，如果分页及刷新都没有被开启则PullToRefreshView是不可用的
 */
public class PullToRefreshView extends ViewGroup {

    public static final int RELEASE_TO_REFRESH = 0;// 松开刷新状态
    public static final int PULL_TO_REFRESH = 1;// 下拉刷新状态
    public static final int REFRESHING = 2;// 正在刷新状态
    public static final int DONE = 3;// 完成，表示可以回到下拉刷新状态了
    private static int MAX_MOVE_HEIGHT;//底部进度条的最高移动距离
    private static int PULL_TO_REFRESH_HEIGHT, MAX_TO_REFRESH_HEIGHT;//当滑动距离超出这个范围时已经达到了下啦刷新状态

    private int lState = DONE;
    private Rect rect;
    private AbsListView listView1;
    private RecyclerView recyclerView;
    private View loadingView, headView, listViewHeadView;
    private int originalTop;
    private BasicAdapter<?> adapter;
    private RecyclerView.Adapter<?> recyclerAdapter;
    private BaseExpandableListAdapter baseExpandableListAdapter;
    private boolean effective;
    private boolean loadPaging;// 是否处于刷新中
    private boolean isNext;// 是否有下一页数据
    private boolean state;
    private float touchMoveX, touchMoveY;
    //    private float scaledTouchSlop;//最大忽略范围
    //本次动画将要移动的长度    方向1向上  -1向下 0原地停留
    private static final float REDUCTION_RATIO = 0.35f;//减速比例
    private static final int DURATION_TIME = 350;// 放手时自动移动的持续时间

    private Scroller scroller;
    private AbsListView.OnScrollListener onScrollListener;

    private OnLoadPagingListener onLoadPagingListener;
    private OnRefreshAllListener onRefreshAllListener;
    private BasicHeadAnimation headAnimation;

    /**
     * 当设置了回掉之后这两个属性将跟随变成false
     */
    private boolean openLoadPagingFc, openRefreshAllListFc, isRefreshAllTouchPause;//isRefreshAllTouchPause true将暂停下拉手势


    public PullToRefreshView(Context context) {
        super(context);
        init(context);
    }

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullToRefreshView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isRefreshAllTouchPause = false;
        if (openRefreshAllListFc) {
            headView = getChildAt(0);
        }
        View v1 = getChildAt(openRefreshAllListFc ? 1 : 0);
        if (v1 instanceof AbsListView) {
            listView1 = (AbsListView) v1;
            listView1.setOnScrollListener(new MyAbsOnScrollListener());
        } else if (v1 instanceof RecyclerView) {
            recyclerView = (RecyclerView) v1;
            recyclerView.setOnScrollListener(new RecyclerViewOnScrollListener());
        }
        if (openLoadPagingFc) {//用户开启了分页功能
            this.loadingView = getChildAt(openRefreshAllListFc ? 2 : 1);
        } else {
            openLoadPagingFunc();
        }
        a1.setDuration(DURATION_TIME);
        a1.setInterpolator(new AnticipateOvershootInterpolator());
        a2.setDuration(DURATION_TIME);
        a2.setInterpolator(new AccelerateInterpolator());

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.effective = false;
    }

    private void init(Context context) {
        this.isNext = true;
        this.effective = true;
        this.originalTop = 0;
        this.scroller = new Scroller(context);
        MAX_MOVE_HEIGHT = (int) (85 * SystemTool.DENSITY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (openRefreshAllListFc) {
            View v0 = getChildAt(0), v1 = getChildAt(1);
            int height = MeasureHelper.getHeight(v0, heightMeasureSpec);
            v0.measure(widthMeasureSpec, height);
            v1.measure(widthMeasureSpec, heightMeasureSpec);
            if (openLoadPagingFc) {
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
        if (openRefreshAllListFc) {
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
        if (rect == null || rect.top <= 0 || changed) {
            v1.layout(0, v0.getMeasuredHeight(), v1.getMeasuredWidth(), v0.getMeasuredHeight() + v1.getMeasuredHeight());
            rect = new Rect();
            rect.set(0, v0.getMeasuredHeight(), v1.getMeasuredWidth(), v0.getMeasuredHeight() + v1.getMeasuredHeight());
        } else {
            v1.layout(rect.left, rect.top, rect.right, rect.bottom);
        }
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
        PULL_TO_REFRESH_HEIGHT = (int) (v0.getMeasuredHeight() * 1.20f);//记录这个头部的高度，后面做动画时作为参数
        MAX_TO_REFRESH_HEIGHT = (int) (v0.getMeasuredHeight() * 1.15f);
        v1.layout(0, 0, v1.getMeasuredWidth(), v1.getMeasuredHeight());
        if (openLoadPagingFc) {
            View v2 = getChildAt(2);
            if (rect == null || rect.top <= 0 || changed) {
                v2.layout(0, v1.getMeasuredHeight(), v2.getMeasuredWidth(), v1.getMeasuredHeight() + v2.getMeasuredHeight());
                rect = new Rect();
                rect.set(0, v1.getMeasuredHeight(), v2.getMeasuredWidth(), v1.getMeasuredHeight() + v2.getMeasuredHeight());
            } else {
                v2.layout(rect.left, rect.top, rect.right, rect.bottom);
            }
            if (getWidth() > 0 || changed) {
                originalTop = v2.getTop();
            }
        }
    }

    /**
     * 为ListView设置适配器
     * (如果可以的话建议在activity中进行对listView设置适配器, 因为在这里将会有一定的延时问题)
     *
     * @param adapter BasicAdapter<?>
     */
    public void setAdapter(BasicAdapter<?> adapter) {
        if (listView1 != null) {
            if (listView1 instanceof ListView) {
                ListView lv = (ListView) listView1;
                lv.setAdapter(adapter);
            } else if (listView1 instanceof GridView) {
                GridView lv = (GridView) listView1;
                lv.setAdapter(adapter);
            }
        } else {
            this.adapter = adapter;
            if (effective) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setAdapter(PullToRefreshView.this.adapter);
                    }
                }, 300);
            }
        }
    }

    /**
     * 为RecyclerView设置适配器
     * (如果可以的话建议在activity中进行对listView设置适配器, 因为在这里将会有一定的延时问题)
     *
     * @param adapter RecyclerView.Adapter<?>
     */
    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        if (recyclerView != null) {
            recyclerView.setAdapter(adapter);
        } else {
            this.recyclerAdapter = adapter;
            if (effective) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setAdapter(PullToRefreshView.this.recyclerAdapter);
                    }
                }, 300);
            }
        }
    }

    /**
     * 添加headView,仅使用ExpandableListView、ListView及PLA_ListView时 才能使用这个方法
     * (如果可以的话建议在activity中进行对listView设置headView, 因为在这里将会有一定的延时问题)
     *
     * @param v headView
     */
    public void addHeaderView(View v) {
        if (listView1 != null) {
            if (listView1 instanceof ExpandableListView) {
                ExpandableListView lv = (ExpandableListView) listView1;
                lv.addHeaderView(v, null, false);
            } else if (listView1 instanceof ListView) {
                ListView lv = (ListView) listView1;
                lv.addHeaderView(v, null, false);
            }
        } else {
            listViewHeadView = v;
            if (effective) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addHeaderView(PullToRefreshView.this.listViewHeadView);
                    }
                }, 300);
            }
        }
    }

    public ViewGroup getListView() {
        return listView1 == null ? recyclerView : listView1;
    }

    public void setAdapter(BaseExpandableListAdapter adapter) {
        if (listView1 != null) {
            ExpandableListView el = ((ExpandableListView) listView1);
            el.setAdapter(adapter);
        } else {
            this.baseExpandableListAdapter = adapter;
            if (effective) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setAdapter(baseExpandableListAdapter);
                    }
                }, 500);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isRefreshAllTouchPause || !openRefreshAllListFc || isRefreshing() || loadPaging) {//如果没有下啦刷新功能或处于刷新中，不允许对其进行操作
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!openRefreshAllListFc || isRefreshing() || loadPaging) {//如果没有下啦刷新功能或处于刷新中，不允许对其进行操作
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event.getX(), event.getY());
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
            d = -getScrollY();
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
            onRefreshAllListener.onBeginLoadPagingListener();
        }
    }

    /**
     * 处理用户移动手势的事件
     *
     * @param x 新的y坐标
     * @param y 新的x坐标
     */
    private void touchMove(float x, float y) {
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
        } else if (recyclerView != null) {
            if (recyclerView.getChildCount() <= 0) {
                state = true;
            } else {
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                state = (lm.findFirstCompletelyVisibleItemPosition() == 0);
            }
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
        if (recyclerView != null) {
            recyclerView.getLayoutManager().scrollToPosition(position);
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
            if (lState == REFRESHING && headAnimation != null) {
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

    public RecyclerView getRecyclerView() {
        return recyclerView;
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

    public void pauseRefreshAllFunc() {
        isRefreshAllTouchPause = true;
    }

    public void resumeRefreshAllFunc() {
        isRefreshAllTouchPause = false;
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
            scroller.startScroll(0, getScrollY(), 0, -getScrollY(), (int) (DURATION_TIME + Math.abs(getScrollY() * 0.35)));
            postInvalidate();
        }
    }

    /**
     * 加载完成
     *
     * @param keep true保留分页 false去掉分页
     */
    public void loadPagingCompleted(boolean keep) {
        isNext = keep;
        if (loadPaging) {
            loadPaging = false;
            onEnd();
        }
    }

    /**
     * 开始加载分页
     */
    public void beginLoadPaging() {
        if (loadingView != null) {
            loadPaging = true;
            showLoadView();
            if (this.onLoadPagingListener != null) {
                this.onLoadPagingListener.onBeginLoadPagingListener();
            }
        } else {//这里主要为onCreate中调用onBegin时做的延时处理
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    beginLoadPaging();
                }
            }, 500);
        }
    }

    /**
     * 启动刷新第一页数据
     */
    public void beginRefreshAll() {
        if (headView != null) {
            lState = REFRESHING;
            if (headAnimation != null && headAnimation.getState() != lState) {
                headAnimation.refresh(headView);
                headAnimation.scrollStop();
                headAnimation.setState(lState);
            }
            scroller.startScroll(0, getScrollY(), 0, -MAX_TO_REFRESH_HEIGHT, DURATION_TIME);
            if (this.onRefreshAllListener != null) {
                this.onRefreshAllListener.onBeginLoadPagingListener();
            }
        } else {//这里主要为onCreate中调用onBegin时做的延时处理
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    beginRefreshAll();
                }
            }, 500);
        }
    }

    /**
     * 开始显示加载动画
     */
    public void showLoadView() {
        if (loadingView.getAnimation() != null) {
            loadingView.clearAnimation();
        }
        loadingView.startAnimation(a1);
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
        if (loadingView.getAnimation() != null) {
            loadingView.clearAnimation();
        }
        loadingView.startAnimation(a2);
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
     * @param onLoadPagingListener OnLoadPagingListener
     */
    public void setOnLoadPagingListener(OnLoadPagingListener onLoadPagingListener) {
        if (onLoadPagingListener != null) {
            this.onLoadPagingListener = onLoadPagingListener;
            openLoadPagingFc = true;
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
            openRefreshAllListFc = true;
        }
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
        void onBeginLoadPagingListener();
    }


    public interface OnRefreshAllListener {
        void onBeginLoadPagingListener();
    }

    /**
     * 下拉刷新时将会根据当前状态对BasicHeadAnimation进行回调
     *
     * @param headAnimation BasicHeadAnimation
     */
    public void setHeadAnimation(BasicHeadAnimation headAnimation) {
        this.headAnimation = headAnimation;
    }

    /**
     * PLA控件的滑动监听
     */
    private class RecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (lState == DONE && isNext && !loadPaging && onLoadPagingListener != null && newState == RecyclerView.SCROLL_STATE_IDLE) {
                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (llm.findLastCompletelyVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1) {// 滚动到底部
                    loadPaging = true;
                    beginLoadPaging();
                }
            }
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChanged(null, newState);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        }
    }

    /**
     * 系统控件的监听
     */
    private class MyAbsOnScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (lState == DONE && isNext && !loadPaging && onLoadPagingListener != null && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                if (view.getLastVisiblePosition() == (view.getCount() - 1)) {// 滚动到底部
                    loadPaging = true;
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