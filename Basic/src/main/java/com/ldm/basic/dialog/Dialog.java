package com.ldm.basic.dialog;

import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.utils.SystemTool;
import com.ldm.basic.utils.TextUtils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * Created by ldm on 11-12-11.
 * 提供了一些简单的Dialog操作，使用系统默认的风格
 */
public class Dialog {

    /**
     * 设置这个不变量可以为下一次设置dialog设置Cancelable属性
     */
    public static boolean CANCELABLE = true;

    /**
     * 提供《确认按钮》，但是不做处理 简单的提示使用
     *
     * @param context Context
     * @param title   标题
     * @param message 提示语
     */
    public static void dialog(Context context, String title, String message) {
        Builder builder = createBuilder(context, title);
        setMessage(message, builder);
        addButton(builder);
        show(builder);
    }

    private static void show(Builder builder) {
        if (builder != null) {
            try {
                builder.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 提供《确认按钮》，但是不做处理 简单的提示使用
     *
     * @param context Context
     * @param title   标题
     * @param message 提示语
     */
    public static void dialog(Context context, int title, String message) {
        Builder builder = createBuilder(context, title);
        setMessage(Html.fromHtml(message), builder);
        addButton(builder);
        show(builder);
    }

    /**
     * 提供 《确认按钮》及《取消按钮》 通过回调函数在确认中做处理
     *
     * @param context  Context
     * @param title    标题
     * @param message  提示语
     * @param callBack c
     */
    public static void dialog(Context context, String title, String message, final CallBack callBack) {
        dialog(context, title, message, BasicApplication.CONSTANTS.CONFIRM, BasicApplication.CONSTANTS.CANCEL, callBack);
    }

    /**
     * 提供 《btn1Text按钮》及《btn2Text按钮》 通过回调函数在确认中做处理
     *
     * @param context  Context
     * @param title    标题
     * @param message  提示语
     * @param btn1Text 按钮1名字
     * @param btn2Text 按钮2名字
     * @param callBack c
     */
    public static void dialog(Context context, String title, String message, String btn1Text, String btn2Text, final CallBack callBack) {
        Builder builder = createBuilder(context, title);
        setMessage(message, builder);
        addButton(builder, callBack, btn1Text);
        addButton2(builder, callBack, btn2Text);
        show(builder);
    }

    /**
     * 提供 《确认按钮》及《取消按钮》 通过回调函数在确认中做处理，并附带复选框
     * 样式均为系统默认
     *
     * @param context  Context
     * @param title    标题
     * @param message  提示语
     * @param boxMsg   复选框提示语
     * @param callBack c
     */
    public static void dialogCheck(Context context, String title, String message, String boxMsg, final CallBack callBack) {
        Builder builder = createBuilder(context, title);
        setMessage(message, builder);
        CheckBox cx = new CheckBox(context);
        cx.setId(1001001);
        cx.setText(boxMsg);
        cx.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                callBack.check(isChecked);
            }
        });
        builder.setView(cx);
        addButton(builder, callBack, BasicApplication.CONSTANTS.CONFIRM);
        addButton2(builder, callBack, BasicApplication.CONSTANTS.CANCEL);
        show(builder);
    }

    /**
     * 网络异常使用
     *
     * @param context Context
     * @param titleId 提示语ID
     */
    public static void netWorkErrDialog(final Context context, int titleId) {
        netWorkErrDialog(context, context.getResources().getString(titleId));
    }

    /**
     * 网络异常使用
     *
     * @param context Context
     * @param title   提示语
     */
    public static void netWorkErrDialog(final Context context, String title) {
        Builder builder = new Builder(context).setTitle(title);
        setMessage(BasicApplication.CONSTANTS.NET_WORKERROR1, builder);

        addButton(context, builder);

        addButton2(builder, null, BasicApplication.CONSTANTS.CANCEL);
        show(builder);
    }

    /**
     * 取消按钮
     *
     * @param builder Builder
     */
    private static void addButton2(Builder builder, final CallBack callBack, final String text) {
        builder.setNegativeButton(text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (callBack != null) {
                    callBack.cancel();
                }
            }
        });
    }

    /**
     * 确认按钮+回调
     *
     * @param callBack c
     * @param builder  Builder
     */
    private static void addButton(Builder builder, final CallBack callBack, final String text) {
        builder.setPositiveButton(text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (callBack != null) {
                    callBack.execution();
                    callBack.cancel();
                }
            }
        });
    }

    /**
     * 网络异常使用的确认按钮
     *
     * @param context Context
     * @param builder Builder
     */
    private static void addButton(final Context context, Builder builder) {
        builder.setPositiveButton(BasicApplication.CONSTANTS.SETTINGS, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                context.startActivity(new Intent(SystemTool.getSettingsAction()));
            }
        });
    }

    /**
     * 确认按钮
     *
     * @param builder Builder
     */
    private static void addButton(final Builder builder) {
        builder.setPositiveButton(BasicApplication.CONSTANTS.CONFIRM, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    private static void setMessage(String message, Builder builder) {
        if (message != null) {
            builder.setMessage(Html.fromHtml(message));
        }
    }

    private static void setMessage(Spanned message, Builder builder) {
        builder.setMessage(message);
    }

    private static Builder createBuilder(Context context, int title) {
        return createBuilder(context, context.getResources().getString(title));
    }

    @SuppressLint("NewApi")
	private static Builder createBuilder(Context context, String title) {
        Builder builder = null;
        if (SystemTool.SYS_SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = new Builder(context, AlertDialog.THEME_HOLO_DARK);
        } else {
            builder = new Builder(context);
        }
        if (!TextUtils.isNull(title)) {
            builder.setTitle(title);
        }
        builder.setCancelable(CANCELABLE);
        return builder;
    }

    /**
     * @author LDM CallBack.java 2011-12-11 上午11:59:40
     */
    public abstract static class CallBack {

        public Object paramObj;

        public CallBack() {
        }

        public CallBack(Object paramObj) {
            this.paramObj = paramObj;
        }

        public abstract void execution();

        public void check(boolean checked) {
        }

        public void cancel() {
        }

    }
}