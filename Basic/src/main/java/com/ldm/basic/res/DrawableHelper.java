package com.ldm.basic.res;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

/**
 * Created by ldm on 14-5-8.
 * 图像助手，提供了常用的操作Drawable的方法
 */
public class DrawableHelper {


    /**
     * 创建一个支持点击更换背景的selector 类型的 Drawable
     *
     * @param normalColor  默认颜色
     * @param pressedColor 点击后的颜色
     * @return Drawable
     */
    public static Drawable getDrawableToPressed(final int normalColor, final int pressedColor) {
        return getDrawableToPressed(new ColorDrawable(normalColor), new ColorDrawable(pressedColor));
    }

    /**
     * 创建一个支持点击更换背景的selector 类型的 Drawable
     *
     * @param normalColor  默认颜色
     * @param pressedColor 点击后的颜色
     * @return Drawable
     */
    public static Drawable getDrawableToPressed(final String normalColor, final String pressedColor) {
        ColorDrawable normal;
        ColorDrawable pressed;
        if (!normalColor.startsWith("#")) {
            normal = new ColorDrawable(Color.parseColor("#" + normalColor));
            pressed = new ColorDrawable(Color.parseColor("#" + pressedColor));
        } else {
            normal = new ColorDrawable(Color.parseColor(normalColor));
            pressed = new ColorDrawable(Color.parseColor(pressedColor));
        }
        return getDrawableToPressed(normal, pressed);
    }

    /**
     * 创建一个支持点击更换背景的selector 类型的 Drawable
     *
     * @param normal  默认背景
     * @param pressed 点击后的背景
     * @return Drawable
     */
    public static Drawable getDrawableToPressed(final Drawable normal, final Drawable pressed) {
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[-android.R.attr.state_pressed], normal);
        sld.addState(new int[android.R.attr.state_pressed], pressed);
        return sld;
    }


    /**
     * 创建一个支持选中后更换背景的selector 类型的 Drawable
     *
     * @param normalColor  默认颜色
     * @param pressedColor 点击后的颜色
     * @return Drawable
     */
    public static Drawable getDrawableToChecked(final int normalColor, final int pressedColor) {
        return getDrawableToChecked(new ColorDrawable(normalColor), new ColorDrawable(pressedColor));
    }

    /**
     * 创建一个支持选中后更换背景的selector 类型的 Drawable
     *
     * @param normalColor  默认颜色
     * @param pressedColor 点击后的颜色
     * @return Drawable
     */
    public static Drawable getDrawableToChecked(final String normalColor, final String pressedColor) {
        ColorDrawable normal;
        ColorDrawable pressed;
        if (!normalColor.startsWith("#")) {
            normal = new ColorDrawable(Color.parseColor("#" + normalColor));
            pressed = new ColorDrawable(Color.parseColor("#" + pressedColor));
        } else {
            normal = new ColorDrawable(Color.parseColor(normalColor));
            pressed = new ColorDrawable(Color.parseColor(pressedColor));
        }
        return getDrawableToChecked(normal, pressed);
    }

    /**
     * 创建一个支持选中后更换背景的selector 类型的 Drawable
     *
     * @param normal  默认背景
     * @param pressed 选中后的背景
     * @return Drawable
     */
    public static Drawable getDrawableToChecked(final Drawable normal, final Drawable pressed) {
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[-android.R.attr.state_checked], normal);
        sld.addState(new int[android.R.attr.state_checked], pressed);
        return sld;
    }

    /**
     * 创建一个Selector类型的Drawable
     *
     * @param stateSet  状态集合
     * @param drawables 图像集合
     * @return Drawable
     */
    public static Drawable getDrawableToSelector(final int[][] stateSet, final Drawable[] drawables) {
        StateListDrawable sld = new StateListDrawable();
        int len = drawables.length;
        for (int i = 0; i < len; i++) {
            sld.addState(stateSet[i], drawables[i]);
        }
        return sld;
    }
}
