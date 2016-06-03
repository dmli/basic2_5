package com.ldm.basic.utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ldm on 14-8-13.
 * view测量助手
 */
public class MeasureHelper {

    /**
     * 返回v.measure(int, int)可识别的高度
     *
     * @param v                       测量View
     * @param parentHeightMeasureSpec 最大的高度
     * @return int
     */
    public static int getHeight(View v, int parentHeightMeasureSpec) {
        int lph = 0, h;
        if (v.getLayoutParams() != null) {
            lph = v.getLayoutParams().height;
        }
        if (lph == ViewGroup.LayoutParams.MATCH_PARENT) {
            h = parentHeightMeasureSpec;
        } else if (lph == ViewGroup.LayoutParams.WRAP_CONTENT) {
            if (v.getMeasuredHeight() <= 0) {
                h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            } else {
                h = View.MeasureSpec.makeMeasureSpec(v.getMeasuredHeight(), View.MeasureSpec.EXACTLY);
            }
        } else {
            h = View.MeasureSpec.makeMeasureSpec(lph, View.MeasureSpec.EXACTLY);
        }
        return h;
    }

    /**
     * 返回v.measure(int, int)可识别的高度
     *
     * @param v 测量View
     * @return int
     */
    public static int getHeight(View v) {
        int lph = 0, h;
        if (v.getLayoutParams() != null) {
            lph = v.getLayoutParams().height;
        }
        if (lph == ViewGroup.LayoutParams.MATCH_PARENT) {
            h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST);
        } else if (lph == ViewGroup.LayoutParams.WRAP_CONTENT) {
            if (v.getMeasuredHeight() <= 0) {
                h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            } else {
                h = View.MeasureSpec.makeMeasureSpec(v.getMeasuredHeight(), View.MeasureSpec.EXACTLY);
            }
        } else {
            h = View.MeasureSpec.makeMeasureSpec(lph, View.MeasureSpec.EXACTLY);
        }
        return h;
    }

    /**
     * 返回v.measure(int, int)可识别的宽度
     *
     * @param v                      测量View
     * @param parentWidthMeasureSpec 最大的宽度
     * @return int
     */
    public static int getWidth(View v, int parentWidthMeasureSpec) {
        int lpw = 0, w;
        if (v.getLayoutParams() != null) {
            lpw = v.getLayoutParams().width;
        }
        if (lpw == ViewGroup.LayoutParams.MATCH_PARENT) {
            w = parentWidthMeasureSpec;
        } else if (lpw == ViewGroup.LayoutParams.WRAP_CONTENT) {
            if (v.getMeasuredHeight() <= 0) {
                w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            } else {
                w = View.MeasureSpec.makeMeasureSpec(v.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
            }
        } else {
            w = View.MeasureSpec.makeMeasureSpec(lpw, View.MeasureSpec.EXACTLY);
        }
        return w;
    }

    /**
     * 返回v.measure(int, int)可识别的宽度
     *
     * @param v 测量View
     * @return int
     */
    public static int getWidth(View v) {
        int lpw = 0, w;
        if (v.getLayoutParams() != null) {
            lpw = v.getLayoutParams().width;
        }
        if (lpw == ViewGroup.LayoutParams.MATCH_PARENT) {
            w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST);
        } else if (lpw == ViewGroup.LayoutParams.WRAP_CONTENT) {
            if (v.getMeasuredHeight() <= 0) {
                w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            } else {
                w = View.MeasureSpec.makeMeasureSpec(v.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
            }
        } else {
            w = View.MeasureSpec.makeMeasureSpec(lpw, View.MeasureSpec.EXACTLY);
        }
        return w;
    }


    /**
     * 返回View的宽度，优先使用LayoutParams的参数
     *
     * @param v 测量View
     * @return int
     */
    public static int getWidthToPixel(View v) {
        int w = 0;
        if (v != null) {
            if (v.getLayoutParams() != null) {
                w = v.getLayoutParams().width;
            }
            if (w <= 0) {
                w = v.getMeasuredWidth();
            }
        }
        return w;
    }

    /**
     * 返回View的宽度，优先使用LayoutParams的参数
     *
     * @param v 测量View
     * @return int
     */
    public static int getHeightToPixel(View v) {
        int h = 0;
        if (v != null) {
            if (v.getLayoutParams() != null) {
                h = v.getLayoutParams().height;
            }
            if (h <= 0) {
                h = v.getMeasuredHeight();
            }
        }
        return h;
    }

    /**
     * 对给定的View进行测量
     *
     * @param v 测量View
     */
    public static void measure(View v, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        if (v != null) {
            v.measure(getWidth(v, parentWidthMeasureSpec), getHeight(v, parentHeightMeasureSpec));
        }
    }

    /**
     * 重置view大小
     *
     * @param width  宽度
     * @param height 高度
     */
    public static void resetSize(View v, int width, int height) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp != null) {
            lp.width = width;
            lp.height = height;
        } else {
            lp = new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(width, height));
        }
        v.setLayoutParams(lp);
    }



}
