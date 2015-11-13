package com.ldm.basic.dialog;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ldm on 15/11/13.
 * 单例模式的Toast，当重复调用时，后面的会使用setText
 */
public class LToast {

    private static Toast toast;

    /**
     * 弹出一个Toast ，时间模式Toast.LENGTH_SHORT
     *
     * @param context Context
     * @param string  文本
     */
    public static void showShort(Context context, String string) {
        if (toast == null) {
            toast = Toast.makeText(context, string, Toast.LENGTH_SHORT);
        } else {
            toast.setText(string);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    /**
     * 弹出一个Toast ，时间模式Toast.LENGTH_SHORT
     *
     * @param context  Context
     * @param stringId 文本ID
     */
    public static void showShort(Context context, int stringId) {
        if (toast == null) {
            toast = Toast.makeText(context, stringId, Toast.LENGTH_SHORT);
        } else {
            toast.setText(stringId);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    /**
     * 弹出一个Toast ，时间模式Toast.LENGTH_LONG
     *
     * @param context Context
     * @param string  文本
     */
    public static void showLong(Context context, String string) {
        if (toast == null) {
            toast = Toast.makeText(context, string, Toast.LENGTH_LONG);
        } else {
            toast.setText(string);
            toast.setDuration(Toast.LENGTH_LONG);
        }
        toast.show();
    }

    /**
     * 弹出一个Toast ，时间模式Toast.LENGTH_SHORT
     *
     * @param context  Context
     * @param stringId 文本ID
     */
    public static void showLong(Context context, int stringId) {
        if (toast == null) {
            toast = Toast.makeText(context, stringId, Toast.LENGTH_LONG);
        } else {
            toast.setText(stringId);
            toast.setDuration(Toast.LENGTH_LONG);
        }
        toast.show();
    }

}
