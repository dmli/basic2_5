package com.ldm.basic.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Created by ldm on 14-2-26.
 * 通过点击图标或文字展示出来一个PopupWindow菜单
 */
public class LMenuView extends TextView implements View.OnClickListener {


    private PopupWindow popupWindow;
    private View contentView;
    private int gravity = -1, animationStyle = -1;
    private MenuViewAdapter adapter;
    private View anchor, parent;//anchor下拉菜单的锚点 , 如果设置了parent将忽略anchor
    private float offX, offY;//偏移量

    public LMenuView(Context context) {
        super(context);
        init(context);
    }

    public LMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        anchor = this;
        this.setOnClickListener(this);
    }

    /**
     * 显示菜单
     */
    public void showDropMenu() {
        if (contentView != null) {
            if (popupWindow == null) {
                popupWindow = new PopupWindow(getContext());
                popupWindow.setWidth(contentView.getMeasuredWidth());
                popupWindow.setHeight(contentView.getMeasuredHeight());
                popupWindow.getBackground().setAlpha(0);
                popupWindow.setFocusable(true);
                popupWindow.setTouchable(true);
                popupWindow.setContentView(contentView);
            }
            popupWindow.setAnimationStyle(animationStyle);
            if (parent != null) {
                popupWindow.showAtLocation(parent, gravity, (int) (offX * parent.getWidth()), (int) (offY * parent.getHeight()));
            } else {
                popupWindow.showAsDropDown(anchor, (int) (offX * anchor.getWidth()), (int) (offY * anchor.getHeight()));
            }
        }
    }

    /**
     * 设置适配器
     *
     * @param adapter MenuViewAdapter
     */
    public void setAdapter(MenuViewAdapter adapter) {
        this.adapter = adapter;
        if (adapter != null) {
            contentView = adapter.getContentView(getContext(), this);
            if (contentView != null) {
                contentView.measure(1, 1);
                popupWindow = null;
            }
        }
    }

    /**
     * 隐藏菜单
     */
    public void menuDismiss() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    /**
     * 设置popupWindow的style
     *
     * @param animationStyle R.style.xxx
     */
    public void setAnimationStyle(int animationStyle) {
        this.animationStyle = animationStyle;
    }

    /**
     * 是否处于显示状态
     *
     * @return true显示状态
     */
    public boolean isShowing() {
        return popupWindow != null && popupWindow.isShowing();
    }

    /**
     * 设置菜单的锚点
     *
     * @param anchor view
     */
    public void setAnchor(View anchor) {
        this.anchor = anchor;
    }

    /**
     * 设置菜单的parent，设置了parent后将会忽略anchor
     *
     * @param parent 父节点
     */
    public void setParent(View parent, int gravity) {
        this.parent = parent;
        this.gravity = gravity;
    }

    /**
     * 设置菜单出现位置的偏移量
     *
     * @param x 0.0f ~ 1.0f
     * @param y 0.0f ~ 1.0f
     */
    public void setMenuOffset(float x, float y) {
        this.offX = x;
        this.offY = y;
    }

    @Override
    public void onClick(View v) {
        if (v != null) {
            if (this.equals(v)) {
                if (isShowing()) {
                    menuDismiss();
                } else {
                    showDropMenu();
                }
            } else {
                if (adapter != null) {
                    adapter.onMenuItemClick(v.getId());
                }
            }
        }
    }

    public interface MenuViewAdapter {

        public View getContentView(Context context, View.OnClickListener listener);

        public void onMenuItemClick(int viewId);
    }
}
