package com.ldm.basic.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.Window;

import com.ldm.basic.R;
import com.ldm.basic.app.BasicApplication;
import com.ldm.basic.utils.SystemTool;
import com.ldm.basic.utils.TextUtils;

/**
 * Created by ldm on 11-12-11.
 * 提供了一些简单的Dialog操作，使用系统默认的风格
 */
public class LDialog {

    /**
     * 设置这个不变量可以为下一次设置dialog设置Cancelable属性
     */
    public static boolean CANCELABLE = true;
    private int themeResId;

    public LDialog(int themeResId) {
        this.themeResId = themeResId;
    }

    private static LDialog lightDialog, darkDialog;

    public static LDialog getLightInstance() {
        if (lightDialog == null) {
            lightDialog = new LDialog(AlertDialog.THEME_HOLO_LIGHT);
        }
        return lightDialog;
    }

    public static LDialog getDarkInstance() {
        if (darkDialog == null) {
            darkDialog = new LDialog(AlertDialog.THEME_HOLO_DARK);
        }
        return darkDialog;
    }

    /**
     * 提供《确认按钮》，但是不做处理 简单的提示使用
     *
     * @param context Context
     * @param title   标题
     * @param message 提示语
     */
    public void dialog(Context context, String title, String message) {
        Builder builder = createBuilder(context, title);
        setMessage(message, builder);
        addButton(builder);
        show(builder);
    }

    private Dialog show(Builder builder) {
        if (builder != null) {
            try {
                return builder.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 提供《确认按钮》，但是不做处理 简单的提示使用
     *
     * @param context Context
     * @param title   标题
     * @param message 提示语
     */
    public void dialog(Context context, int title, String message) {
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
     * @param listener listener
     */
    public void dialog(Context context, String title, String message, final OnDialogListener listener) {
        dialog(context, title, message, BasicApplication.CONSTANTS.CONFIRM, BasicApplication.CONSTANTS.CANCEL, listener);
    }

    /**
     * 提供 《btn1Text按钮》及《btn2Text按钮》 通过回调函数在确认中做处理
     *
     * @param context  Context
     * @param title    标题
     * @param message  提示语
     * @param btn1Text 按钮1名字
     * @param btn2Text 按钮2名字
     * @param listener listener
     */
    public void dialog(Context context, String title, String message, String btn1Text, String btn2Text, final OnDialogListener listener) {
        Builder builder = createBuilder(context, title);
        setMessage(message, builder);
        addButton(builder, listener, btn1Text);
        addButton2(builder, listener, btn2Text);
        show(builder);
    }

    /**
     * 提供 《确认按钮》及《取消按钮》 通过回调函数在确认中做处理，
     * DialogBuilderView将被添加到Builder中
     * builder.setView(dialogView.getView(context));
     *
     * @param context    Context
     * @param title      标题
     * @param message    提示语
     * @param dialogView DialogBuilderView 将会使用Builder.setView(View)中
     */
    public void dialogBuilderView(Context context, String title, String message, BaseDialogView dialogView) {
        Builder builder = createBuilder(context, title);
        setMessage(message, builder);
        builder.setView(dialogView.getView(context));
        addButton(builder, dialogView, BasicApplication.CONSTANTS.CONFIRM);
        addButton2(builder, dialogView, BasicApplication.CONSTANTS.CANCEL);
        dialogView.dialog = show(builder);
    }

    /**
     * 仅提供一个窗体，界面样式均由DialogContentView.getView(Content)创建
     *
     * @param context                Context
     * @param canceledOnTouchOutside true/false 点击空白区域是否关闭对话框
     * @param dialogView             DialogContentView
     */
    public void dialogView(Context context, boolean canceledOnTouchOutside, BaseDialogView dialogView) {
        dialogView.dialog = new Dialog(context, R.style.BaseDialogViewTheme);
        dialogView.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogView.dialog.setContentView(dialogView.getView(context));
        dialogView.dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        dialogView.dialog.show();
    }

    /**
     * 网络异常使用
     *
     * @param context Context
     * @param titleId 提示语ID
     */
    public void netWorkErrDialog(final Context context, int titleId) {
        netWorkErrDialog(context, context.getResources().getString(titleId));
    }

    /**
     * 网络异常使用
     *
     * @param context Context
     * @param title   提示语
     */
    public void netWorkErrDialog(final Context context, String title) {
        Builder builder = new Builder(context).setTitle(title);
        setMessage(BasicApplication.CONSTANTS.NET_WORK_ERROR_TO_SETTINGS, builder);

        addButton(context, builder);

        addButton2(builder, null, BasicApplication.CONSTANTS.CANCEL);
        show(builder);
    }

    /**
     * 取消按钮
     *
     * @param builder Builder
     */
    private void addButton2(Builder builder, final OnDialogListener listener, final String text) {
        builder.setNegativeButton(text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (listener != null) {
                    listener.cancel();
                }
            }
        });
    }

    /**
     * 确认按钮+回调
     *
     * @param listener listener
     * @param builder  Builder
     */
    private void addButton(Builder builder, final OnDialogListener listener, final String text) {
        builder.setPositiveButton(text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (listener != null) {
                    listener.execution();
                    listener.cancel();
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
    private void addButton(final Context context, Builder builder) {
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
    private void addButton(final Builder builder) {
        builder.setPositiveButton(BasicApplication.CONSTANTS.CONFIRM, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    private void setMessage(String message, Builder builder) {
        if (message != null) {
            builder.setMessage(Html.fromHtml(message));
        }
    }

    private void setMessage(Spanned message, Builder builder) {
        builder.setMessage(message);
    }

    private Builder createBuilder(Context context, int title) {
        return createBuilder(context, context.getResources().getString(title));
    }

    private Builder createBuilder(Context context, String title) {
        Builder builder = new Builder(context, themeResId);
        if (!TextUtils.isNull(title)) {
            builder.setTitle(title);
        }
        builder.setCancelable(CANCELABLE);
        return builder;
    }

    /**
     * @author LDM CallBack.java 2011-12-11 上午11:59:40
     */
    public abstract static class OnDialogListener {

        public Object paramObj;

        public OnDialogListener() {
        }

        public OnDialogListener(Object paramObj) {
            this.paramObj = paramObj;
        }

        public abstract void execution();

        public void cancel() {
        }
    }

    /**
     * OnCheckDialogListener, 提供了一个复选框选中状态时的监听
     */
    public abstract static class BaseDialogView extends OnDialogListener {
        Dialog dialog;

        public void dismiss() {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        public abstract View getView(Context context);
    }

}