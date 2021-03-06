package com.ldm.basic.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by ldm on 15/12/4.
 * 键盘
 */
public class SoftInputTool {


    /**
     * 打开软键盘
     *
     * @param context Context
     * @param view    要使用软盘的view
     */
    public static void showSoftInput(final Context context, final View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        view.setFocusable(true);
        view.requestFocus();
        boolean bool = imm.showSoftInput(view, 0);
        if (!bool) {
            bool = imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
        if (!bool) {
            imm.showSoftInputFromInputMethod(view.getWindowToken(), InputMethodManager.SHOW_FORCED);
        }
    }

    /**
     * 弹出软盘
     *
     * @param context Context
     * @param view    要使用软盘的view
     * @link 请使用showSoftInput(Context, View)
     * @deprecated
     */
    public static void showSoftInputFromInputMethod(final Context context, final View view) {
        showSoftInput(context, view);
    }

    /**
     * 关闭软盘
     *
     * @param context Context
     * @param binder  IBinder
     */
    public static boolean hideSoftInput(final Context context, final IBinder binder) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean bool = false;
        try {
            if (imm.isActive()) {
                if (binder != null) {
                    bool = imm.hideSoftInputFromWindow(binder, 0);
                }
                if (!bool && context instanceof Activity) {
                    Activity a = (Activity) context;
                    if (a.getCurrentFocus() != null) {
                        bool = imm.hideSoftInputFromWindow(a.getCurrentFocus().getWindowToken(), 0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    /**
     * 关闭软盘，这个方法没有返回值
     *
     * @param context   Context
     * @param focusView View
     */
    public static void hideSoftInput(final Context context, final View focusView) {
        boolean boo = hideSoftInput(context, focusView.getWindowToken());
        if (!boo) {
            focusView.clearFocus();
        }
    }

    /**
     * 获取软键盘高度(这个方法需要在软键盘完全出现后调用有效,建议在OnGlobalLayoutListener监听中使用)
     * *rootNode.getViewTreeObserver().addOnGlobalLayoutListener*
     *
     * @return int
     */
    public static int getSoftInputHeight(Activity activity) {
        final Rect r = new Rect();
        View rootNode = activity.getWindow().getDecorView();
        rootNode.getWindowVisibleDisplayFrame(r);
        final int sih = Math.min(rootNode.getRootView().getHeight(), activity.getResources().getDisplayMetrics().heightPixels);
        return sih - r.bottom;
    }

    /**
     * 返回软盘是否开启状态
     *
     * @return true开启 false没有开启
     */
    public static boolean isSoftInputOpen(Activity activity) {
        final Rect r = new Rect();
        View rootNode = activity.getWindow().getDecorView();
        rootNode.getWindowVisibleDisplayFrame(r);
        final int sih = Math.min(rootNode.getRootView().getHeight(), activity.getResources().getDisplayMetrics().heightPixels);
        return sih != r.bottom;
    }


    /**
     * 设置true后将开启对软键盘的监听, 当activity的windowSoftInputMode属性设置为adjustNothing时，这个方法将无效
     *
     * @param activity               Activity
     * @param onGlobalLayoutListener ViewTreeObserver.OnGlobalLayoutListener
     */
    public static void addSoftInputStateListener(Activity activity, ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        activity.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    /**
     * 移除OnGlobalLayoutListener事件
     *
     * @param activity               Activity
     * @param onGlobalLayoutListener ViewTreeObserver.OnGlobalLayoutListener
     */
    public static void removeGlobalLayoutListener(Activity activity, ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        // 注销对ViewTreeObserver的监听
        if (activity.getWindow().getDecorView().getViewTreeObserver().isAlive()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                activity.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
            } else {
                activity.getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
            }
        }
    }
}
