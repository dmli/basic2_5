package com.ldm.basic.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ldm.basic.utils.LLog;
import com.ldm.basic.utils.MeasureHelper;

public class LPullToRefreshListView extends ListView implements OnScrollListener {

    public static final int RELEASE_To_REFRESH = 0;// 松开刷新状态
    public static final int PULL_To_REFRESH = 1;// 下拉刷新状态
    public static final int REFRESHING = 2;// 正在刷新状态
    public static final int DONE = 3;// 完成，表示可以回到下拉刷新状态了
    private static final int RATIO = 3;// 实际的padding的距离与界面上偏移距离的比例
    private static final int REFRESH_RECOVERY_DURATION = 600;
    private int lState;
    private int lStartY;// 用于保证lStartY的值在一个完整的touch事件中只被记录一次

    private int lHeadContentHeight;
    private boolean lIsBack;
    private boolean lIsReCored;
    private boolean lRefreshLock; // 刷新锁，全部刷新 和 下拉刷新功能，一个开启另一个需要等待
    private boolean lIsHeadRefreshable; // 全部刷新功能是否开启

    private boolean lIsFooterRefreshable; // 下拉刷新功能是否开启

    private LayoutInflater lInflater;
    private View lFooterView;
    private LinearLayout lHeadView;
    private TextView lPullToRefreshText;
    private ImageView lPullToRefreshImage;
    private View lPullToRefreshProgress;

//        // 刷新箭头的翻转动画
//    private final RotateAnimation lAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
//    // 刷新箭头的翻转动画
//    private final RotateAnimation lReverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF,
//            0.5f);
    // 全部刷新时的插入器
    private final DecelerateInterpolator lDecelerateInterpolator = new DecelerateInterpolator(2f);
    // 全部刷新回弹时的动画
    private final AnimateToStartPosition lAnimateToStartPosition = new AnimateToStartPosition();

    // 全部刷新时的回调接口
    private final Animation.AnimationListener lReturnToStartPositionListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            // 如果运动中出现异常，导致PaddingTop!=0时 执行一次重置
            if (lAnimateToStartPosition.state == RELEASE_To_REFRESH) {
                if (lHeadView.getPaddingTop() != 0) {
                    lHeadView.setPadding(0, 0, 0, 0);
                }
                // 执行全部刷新操作
                refreshAll();
            } else if (lAnimateToStartPosition.state == PULL_To_REFRESH) {
                if (lHeadView.getPaddingTop() != 0) {
                    lHeadView.setPadding(0, -lHeadContentHeight, 0, 0);
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    /**
     * 滚动停止时的监听
     */
    private OnScrollIdleListener onScrollIdleListener;

    private String[] text = new String[]{"下拉刷新", "释放刷新", "更新中..."};

    private RefreshAll lRefreshAll;
    private LoadPaging lLoadPaging;

    public LPullToRefreshListView(Context context) {
        super(context);
        init(context);
    }

    public LPullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LPullToRefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        lInflater = LayoutInflater.from(context);
        setOnScrollListener(this);
    }

    /**
     * 开启刷新全部功能（此方法需要在setAdapter前使用）
     */
    public void openRefreshAllFn(RefreshAll refreshAll, int headLayoutId, int arrowViewId, int progressViewId, int textViewId) {
        lRefreshAll = refreshAll;
        lHeadView = (LinearLayout) lInflater.inflate(headLayoutId, this, false);
        if (lHeadView != null) {
            lPullToRefreshProgress = lHeadView.findViewById(progressViewId);
            lPullToRefreshImage = (ImageView) lHeadView.findViewById(arrowViewId);
            lPullToRefreshText = (TextView) lHeadView.findViewById(textViewId);
            lPullToRefreshText.setMinimumHeight(70);// 设置最小高度
            MeasureHelper.measure(lHeadView, MeasureHelper.getWidth(lHeadView), MeasureHelper.getHeight(lHeadView)); // 测量view大小
            lHeadContentHeight = lHeadView.getMeasuredHeight();
            LLog.e("aaaaaaaa", "lHeadContentHeight = "+ lHeadContentHeight);
            lHeadView.setPadding(0, -lHeadContentHeight, 0, 0);
            addHeaderView(lHeadView, null, false);
        }
        // ----------------------------------------
//        lAnimation.setInterpolator(new LinearInterpolator());
//        lAnimation.setDuration(250);
//        lAnimation.setFillAfter(true);
//        // ----------------------------------------
//        lReverseAnimation.setInterpolator(new LinearInterpolator());
//        lReverseAnimation.setDuration(200);
//        lReverseAnimation.setFillAfter(true);
        // ----------------------------------------
        lState = DONE;
        lIsHeadRefreshable = true;// 开启全部刷新功能
    }

    /**
     * 开启自动加载分页功能（此方法需要在setAdapter前使用）
     */
    public void openLoadPagingFn(LoadPaging loadPaging, int layoutId) {
        lLoadPaging = loadPaging;
        lFooterView = lInflater.inflate(layoutId, this, false);
        addFooterView(lFooterView, null, false);
        lIsFooterRefreshable = true;
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:// 当屏幕滚动停止时
                if (onScrollIdleListener != null) {
                    onScrollIdleListener.onScrollIdleListener();
                }
                // 加载分页数据
                if (lIsFooterRefreshable && !lRefreshLock && getLastVisiblePosition() == (view.getCount() - 1)) {
                    loadPaging();
                }
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 当屏幕滚动且用户使用的触碰或手指还在屏幕上时
                break;
            case OnScrollListener.SCROLL_STATE_FLING:// 屏幕产生惯性滑动时去掉lHeaderStatusView，等待屏幕滚动停止时加入
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (lIsHeadRefreshable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (getFirstVisiblePosition() == 0 && !lIsReCored) {
                        lIsReCored = true;
                        lStartY = (int) event.getY();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    if (lState != REFRESHING && !lRefreshLock) {
                        if (lState == PULL_To_REFRESH) {
                            lState = DONE;
                            changeHeaderViewByState();
                        }
                        if (lState == RELEASE_To_REFRESH) {
                            lState = REFRESHING;
                            changeHeaderViewByState();
                        }
                    }
                    lIsReCored = false;
                    lIsBack = false;
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    int tempY = (int) event.getY();
                    if (!lIsReCored && getFirstVisiblePosition() == 0) {
                        lIsReCored = true;
                        lStartY = tempY;
                    }
                    if (lState != REFRESHING && lIsReCored && !lRefreshLock) {
                        // 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动
                        // 可以松手去刷新了
                        if (lState == RELEASE_To_REFRESH) {
                            setSelection(0);
                            if (((tempY - lStartY) / RATIO < lHeadContentHeight) && (tempY - lStartY) > 0) {// 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
                                lState = PULL_To_REFRESH;
                                changeHeaderViewByState();
                            } else if (tempY - lStartY <= 0) {// 一下子推到顶了
                                lState = DONE;
                                changeHeaderViewByState();
                            }
                        }
                        // 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
                        if (lState == PULL_To_REFRESH) {
                            setSelection(0);
                            if ((tempY - lStartY) / RATIO >= lHeadContentHeight) {// 下拉到可以进入RELEASE_TO_REFRESH的状态
                                lState = RELEASE_To_REFRESH;
                                lIsBack = true;
                                changeHeaderViewByState();
                            } else if (tempY - lStartY <= 0) {// 上推到顶了
                                lState = DONE;
                                changeHeaderViewByState();
                            }
                        }
                        if (lState == DONE && tempY - lStartY > 0) {// done状态下
                            lState = PULL_To_REFRESH;
                            changeHeaderViewByState();
                        }
                        switchHeaderImage(Math.abs(lHeadView.getPaddingTop()) * 1.0f / lHeadContentHeight);// 切换动画
                        if (lState == PULL_To_REFRESH) {// 更新lHeadView的size
                            lHeadView.setPadding(0, -1 * lHeadContentHeight + (tempY - lStartY) / RATIO, 0, 0);
                        }
                        if (lState == RELEASE_To_REFRESH) {// 更新lHeadView的paddingTop
                            lHeadView.setPadding(0, (tempY - lStartY) / RATIO - lHeadContentHeight, 0, 0);
                        }
                    }
                    break;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * header出现时可以在这里进行切换图片
     *
     * @param interpolatedTime 进度
     */
    private void switchHeaderImage(float interpolatedTime) {

    }

    // 当状态改变时候，调用该方法，以更新界面
    private void changeHeaderViewByState() {
        switch (lState) {
            case RELEASE_To_REFRESH: {
                lPullToRefreshImage.setVisibility(View.VISIBLE);
                lPullToRefreshProgress.setVisibility(View.GONE);
                lHeadView.setVisibility(View.VISIBLE);
                lPullToRefreshImage.clearAnimation();
//                lPullToRefreshImage.startAnimation(lAnimation);
                lPullToRefreshText.setText(text[1]);
                break;
            }
            case PULL_To_REFRESH: {
                lPullToRefreshProgress.setVisibility(View.GONE);
                lHeadView.setVisibility(View.VISIBLE);
                if (lPullToRefreshImage != null) {
                    lPullToRefreshImage.clearAnimation();
                    lPullToRefreshImage.setVisibility(View.VISIBLE);
                }
                // 是由RELEASE_To_REFRESH状态转变来的
                if (lIsBack) {
                    lIsBack = false;
                    if (lPullToRefreshImage != null) {
                        lPullToRefreshImage.clearAnimation();
//                        lPullToRefreshImage.startAnimation(lReverseAnimation);
                    }
                }
                lPullToRefreshText.setText(text[0]);
                break;
            }
            case REFRESHING: {
                lPullToRefreshProgress.setVisibility(View.VISIBLE);
                lPullToRefreshImage.clearAnimation();
                lPullToRefreshImage.setVisibility(View.GONE);
                lPullToRefreshText.setText(text[2]);
                if (Math.abs(lHeadView.getPaddingTop()) < 5) {
                    lHeadView.setPadding(0, 0, 0, 0);
                    refreshAll();
                } else {
                    lAnimateToStartPosition.reset();
                    lAnimateToStartPosition.setDuration(REFRESH_RECOVERY_DURATION + lHeadView.getPaddingTop());
                    lAnimateToStartPosition.setAnimationListener(lReturnToStartPositionListener);
                    lAnimateToStartPosition.setInterpolator(lDecelerateInterpolator);
                    lAnimateToStartPosition.state = RELEASE_To_REFRESH;// 设置松手刷新状态
                    lAnimateToStartPosition._top = lHeadView.getPaddingTop();
                    lHeadView.startAnimation(lAnimateToStartPosition);
                }
                break;
            }
            case DONE: {
                // 距离小的时候直接设置Padding，不执行动画
                if (Math.abs(lHeadContentHeight - Math.abs(lHeadView.getPaddingTop())) < 5) {
                    lHeadView.setPadding(0, -lHeadContentHeight, 0, 0);
                } else {
                    lAnimateToStartPosition.reset();
                    lAnimateToStartPosition.setDuration(REFRESH_RECOVERY_DURATION + lHeadView.getPaddingTop());
                    lAnimateToStartPosition.setAnimationListener(lReturnToStartPositionListener);
                    lAnimateToStartPosition.setInterpolator(lDecelerateInterpolator);
                    lAnimateToStartPosition.state = PULL_To_REFRESH;// 设置下拉刷新状态
                    lAnimateToStartPosition._top = lHeadView.getPaddingTop();
                    lHeadView.startAnimation(lAnimateToStartPosition);
                }
                lPullToRefreshProgress.setVisibility(View.GONE);
                if (lPullToRefreshImage != null) {
                    lPullToRefreshImage.clearAnimation();
//                    lPullToRefreshImage.setImageResource(R.drawable.ic_pull_refresh_arrow);
                }
                lPullToRefreshText.setText(text[0]);
                break;
            }
            default:
                break;
        }
    }

    /**
     * 直接执行刷新全部功能
     */
    public void startRefreshAll() {
        lState = REFRESHING;// 设置刷新状态
        lHeadView.setPadding(0, 0, 0, 0);
        lPullToRefreshProgress.setVisibility(View.VISIBLE);
        lPullToRefreshImage.clearAnimation();
        lPullToRefreshImage.setVisibility(View.GONE);
        lPullToRefreshText.setText(text[2]);
        // 执行刷新功能
        refreshAll();
    }

    /**
     * 获取当前状态
     *
     * @return lState
     */
    public int getState() {
        return lState;
    }

    /**
     * 是否属于刷新全部中
     *
     * @return true当前处于刷新全部中
     */
    public boolean isRefreshAll() {
        return lState == REFRESHING;
    }

    private void refreshAll() {
        lRefreshLock = true; // 开启刷新锁
        if (lFooterView != null) {
            // 当全部刷新时隐藏lFooterView
            removeFooterView(lFooterView);
        }
        lRefreshAll.refreshAll();
    }

    private void loadPaging() {
        lRefreshLock = true; // 开启刷新锁
        lLoadPaging.loadPaging();
    }

    /**
     * 设置一个列表滑动停止时的触发接口
     *
     * @param onScrollIdleListener PullToRefreshListView.OnScrollIdleListener
     */
    public void setOnScrollIdleListener(OnScrollIdleListener onScrollIdleListener) {
        this.onScrollIdleListener = onScrollIdleListener;
    }

    /**
     * 为控件重新设置提示语
     *
     * @param pullToRefreshText    下拉刷新
     * @param releaseToRefreshText 松开刷新
     * @param refreshingText       正在刷新
     */
    public void setText(String pullToRefreshText, String releaseToRefreshText, String refreshingText) {
        this.text[0] = pullToRefreshText;
        this.text[1] = releaseToRefreshText;
        this.text[2] = refreshingText;
    }

    /**
     * 全部刷新完毕后需调用该方法通知ListView
     */
    public void refreshAllCompleted() {
        lState = DONE;
        lRefreshLock = false;// 解开刷新锁
        if (lIsFooterRefreshable && lFooterView != null) {
            addFooterView(lFooterView);
        }
        changeHeaderViewByState();
    }

    /**
     * 加载分页完毕后需调用该方法通知ListView
     */
    public void loadPagingCompleted() {
        loadPagingCompleted(true);
    }

    /**
     * 加载分页完毕后需调用该方法通知ListView
     *
     * @param keep 是否保留分页功能 【true表示继续保留】，【false表示关闭，但LoadPaging回调仍保留】
     */
    public void loadPagingCompleted(boolean keep) {
        if (!keep && lIsFooterRefreshable) {
            lIsFooterRefreshable = false;// 关闭分页功能
            try {
                // 防止removeFooterView时出现空指针异常
                removeFooterView(lFooterView);// 清楚lFooterView控件
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (keep && !lIsFooterRefreshable) {
            addFooterView(lFooterView, null, false);
            lIsFooterRefreshable = true;
        }
        lRefreshLock = false;// 解开刷新锁
        lState = DONE;// 避免操作中出现问题，此处将lState设置为完成状态
        if (lIsHeadRefreshable) {// 如果全部刷新功能开启时调用
            changeHeaderViewByState();
        }
    }

    /**
     * 刷新全部回调接口
     *
     * @author LDM
     */
    public interface RefreshAll {
        void refreshAll();
    }

    /**
     * 获取下一页数据回调接口
     *
     * @author LDM
     */
    public interface LoadPaging {
        void loadPaging();
    }

    /**
     * 滚动停止时被出发
     */
    public interface OnScrollIdleListener {

        /**
         * 当滚动停止时被出发
         */
        void onScrollIdleListener();
    }

    private class AnimateToStartPosition extends Animation {
        public int state, _top;

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int top = lHeadView.getPaddingTop();
            int newTop;
            if (state == RELEASE_To_REFRESH) {
                newTop = (int) ((1 - interpolatedTime) * _top);
                if (newTop != top) {// newTop不等于top时进行重置Padding
                    lHeadView.setPadding(0, newTop, 0, 0);
                    switchHeaderImage(Math.abs(lHeadView.getPaddingTop()) * 1.0f / lHeadContentHeight);// 切换动画
                }
            } else if (state == PULL_To_REFRESH) {
                newTop = (int) (_top + interpolatedTime * -(lHeadContentHeight + _top));
                if (newTop != top) {// newTop不等于top时进行重置Padding
                    lHeadView.setPadding(0, newTop, 0, 0);
                }
            }
        }
    }
}
