package com.ldm.basic.res;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ldm on 16/2/1.
 * 这里提供了简单的LayoutParams操作
 */
public class LayoutParamsTool {

    public static void setTopMargin(View v, int top) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.topMargin = top;
            v.setLayoutParams(lp);
        }
    }

    public static void moveViewToMargin(View v, int left, int top) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.leftMargin = left;
            lp.topMargin = top;
            v.setLayoutParams(lp);
        }
    }

}
