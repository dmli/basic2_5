package com.ldm.basic.views;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Created by ldm on 14-2-13.
 * 自定义下拉菜单
 */
public class LSpinner extends TextView implements View.OnClickListener {

    private PopupWindow popupWindow;
    private MySpinnerAdapter lAdapter;
    private static final int ITEM_ID = 100000010;
    private ListView contentView;
    private int animationStyle;
    private Context context;
    private int backgroundId;
    private Drawable divider;
    private int dividerHeight;
    private int popHeight;
    private int[] contentPadding;
    private int xOff, yOff;//PopupWindow 出现位置的偏移量

    public LSpinner(Context context) {
        super(context);
        init(context);
    }

    public LSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
        this.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        this.setPadding(20, 10, 0, 10);
    }

    private void init(Context context) {
        xOff = 0;
        yOff = 0;
        animationStyle = -1;
        popHeight = 240;
        contentPadding = new int[]{2, 2, 2, 2};
        this.context = context;
        this.setOnClickListener(this);
    }

    /**
     * 显示菜单
     */
    public void showDropMenu() {
        if (popupWindow == null) {
            popupWindow = new PopupWindow(context);
            popupWindow.setWidth(this.getWidth());
            popupWindow.setHeight(popHeight);
            popupWindow.getBackground().setAlpha(0);
            popupWindow.setFocusable(true);
            popupWindow.setTouchable(true);
            popupWindow.setContentView(getPopupWindowLayout());
        }
        popupWindow.setAnimationStyle(animationStyle);
        popupWindow.showAsDropDown(this, xOff, yOff);
    }

    private View getPopupWindowLayout() {
        if (contentView == null) {
            if (lAdapter != null) {
                contentView = new ListView(context);
                contentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                contentView.setCacheColorHint(Color.argb(0, 0, 0, 0));
                if (backgroundId != 0) {
                    contentView.setBackgroundResource(backgroundId);
                } else {
                    contentView.setBackgroundColor(Color.argb(0, 0, 0, 0));
                }
                contentView.setPadding(contentPadding[0], contentPadding[1], contentPadding[2], contentPadding[3]);
                contentView.setFadingEdgeLength(0);
                if (divider == null) {
                    contentView.setDivider(null);
                    contentView.setDividerHeight(0);
                } else {
                    contentView.setDivider(divider);
                    contentView.setDividerHeight(dividerHeight);
                }
                contentView.setAdapter(lAdapter);
            }
        }
        return contentView;
    }

    /**
     * 设置适配器
     *
     * @param adapter MySpinnerAdapter适配器
     */
    public void setAdapter(MySpinnerAdapter adapter) {
        adapter.setSpinner(this);
        this.lAdapter = adapter;
        SpinnerRes res = this.lAdapter.getSelectedItem();
        if (res != null) {
            setText(res.getValue());
        }
    }

    public MySpinnerAdapter getAdapter() {
        return lAdapter;
    }

    /**
     * 设置item背景，该方法需要在setAdapter后调用
     *
     * @param id R.drawable.xxx | R.color.xxx
     */
    public void setChildBackground(int id) {
        if (lAdapter != null) {
            lAdapter.setChildBackground(id);
        }
    }

    /**
     * 设置分割器
     *
     * @param divider       Drawable
     * @param dividerHeight 高度
     */
    public void setDivider(Drawable divider, int dividerHeight) {
        this.divider = divider;
        this.dividerHeight = dividerHeight;
    }

    /**
     * 设置pop菜单的高度 默认240
     *
     * @param popHeight 高度
     */
    public void setPopHeight(int popHeight) {
        this.popHeight = popHeight;
    }

    /**
     * 设置选中的item背景，该方法需要在setAdapter后调用
     *
     * @param id R.drawable.xxx | R.color.xxx
     */
    public void setSelectedBackground(int id) {
        if (lAdapter != null) {
            lAdapter.setSelectedBackground(id);
        }
    }

    /**
     * 根据code设置选中项
     *
     * @param code string
     */
    public void setSelectedToCode(String code) {
        if (lAdapter != null) {
            lAdapter.setDefSelectedToCode(code);
        }
    }

    /**
     * 根据value设置选中项
     *
     * @param value string
     */
    public void setSelectedToValue(String value) {
        if (lAdapter != null) {
            lAdapter.setDefSelectedToValue(value);
        }
    }

    /**
     * 设置菜单背景
     *
     * @param backgroundId R.drawable.*
     */
    public void setPopBackground(int backgroundId) {
        this.backgroundId = backgroundId;
    }

    /**
     * 设置内容体的内边距
     *
     * @param left   int
     * @param top    int
     * @param right  int
     * @param bottom int
     */
    public void setContentPadding(int left, int top, int right, int bottom) {
        this.contentPadding[0] = left;
        this.contentPadding[1] = top;
        this.contentPadding[2] = right;
        this.contentPadding[3] = bottom;
    }

    /**
     * 设置pop出现位置的偏移量 默认0
     *
     * @param xOff int
     * @param yOff int
     */
    public void setPopPositionOffset(int xOff, int yOff) {
        this.xOff = xOff;
        this.yOff = yOff;
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
     * 是否处于显示状态
     *
     * @return true显示状态
     */
    private boolean isShowing() {
        return popupWindow != null && popupWindow.isShowing();
    }


    @Override
    public void onClick(View v) {
        if (this.equals(v)) {
            if (isShowing()) {
                menuDismiss();
            } else {
                showDropMenu();
            }
        } else if (getContext() != null && v.getId() == ITEM_ID) {
            int position = -1;
            try {
                position = Integer.parseInt(v.getTag().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (position > -1 && position < lAdapter.getCount()) {
                lAdapter.switchSelected(position);
                menuDismiss();
            }
        }
    }

    /**
     * 获取当前选中值
     *
     * @return SpinnerRes 没有选中项时为空
     */
    public SpinnerRes getSelectedRes() {
        return lAdapter == null ? null : lAdapter.getSelectedItem();
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
     * Spinner适配器，需要用户实现createItemView方法
     */
    public abstract static class MySpinnerAdapter extends BaseAdapter {

        protected LayoutInflater _layoutInflater;
        private List<SpinnerRes> data;
        private Context context;
        private LSpinner spinner;
        public int childBackground;
        public int selectedBackground;
        public int selectedIndex;//当前选中项的索引

        protected MySpinnerAdapter(Context context, List<SpinnerRes> data) {
            _layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.context = context;
            this.data = data;
            selectedIndex = -1;
        }

        public void setSpinner(LSpinner spinner) {
            this.spinner = spinner;
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public SpinnerRes getItem(int position) {
            return data == null ? null : data.get(position);
        }

        public void switchSelected(int newPosition) {
            if (data != null) {
                if (selectedIndex != -1) {
                    data.get(selectedIndex).setDefSelected(false);
                }
                if (spinner != null) {
                    if (newPosition >= 0 && newPosition < data.size()) {
                        SpinnerRes res = data.get(newPosition);
                        res.setDefSelected(true);
                        spinner.setText(res.getValue());
                    }
                }
            }
        }

        /**
         * 获取选中项
         *
         * @return SpinnerRes
         */
        public SpinnerRes getSelectedItem() {
            if (data != null) {
                if (selectedIndex != -1) {
                    return data.get(selectedIndex);
                }
                for (SpinnerRes spinnerRes : data) {
                    if (spinnerRes.isDefSelected()) {
                        return spinnerRes;
                    }
                }
            }
            return null;
        }

        /**
         * 根据code设置选中项
         *
         * @param code 值
         * @return -1设置失败
         */
        public int setDefSelectedToCode(String code) {
            int result = -1;
            if (data != null) {
                result = findSelectedInfo(code, 0);
                if (result >= 0 && result > data.size()) {
                    switchSelected(result);
                }
            }
            return result;
        }

        /**
         * 根据value设置选中项
         *
         * @param value value
         * @return -1设置失败
         */
        public int setDefSelectedToValue(String value) {
            int result = -1;
            if (data != null) {
                result = findSelectedInfo(value, 1);
                if (result >= 0 && result > data.size()) {
                    switchSelected(result);
                }
            }
            return result;
        }

        /**
         * 该方法返回原显示项与新显示项位置
         *
         * @param text 值
         * @param type 0使用code 1使用value
         * @return int  新的选中项位置
         */
        private int findSelectedInfo(String text, int type) {
            int selected = -1;
            if (data != null && text != null) {
                int len = data.size();
                for (int i = 0; i < len; i++) {
                    if (type == 0) {
                        if (text.equals(data.get(i).code)) {
                            selected = i;
                        }
                    } else {
                        if (text.equals(data.get(i).code)) {
                            selected = i;
                        }
                    }
                    if (selected != -1) {
                        break;//退出循环
                    }
                }
            }
            return selected;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void setChildBackground(int childBackground) {
            this.childBackground = childBackground;
        }

        public void setSelectedBackground(int selectedBackground) {
            this.selectedBackground = selectedBackground;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout ll;
            SpinnerRes res = getItem(position);
            if (convertView == null) {
                ll = new LinearLayout(context);
                ll.setOnClickListener(spinner);

                View v = createItemView(_layoutInflater, position, null, parent, res);
                if (v != null) {
                    ll.addView(v);
                }
            } else {
                ll = (LinearLayout) convertView;
                createItemView(_layoutInflater, position, ll.getChildAt(0), parent, res);
            }
            ll.setId(ITEM_ID);
            ll.setTag(position + "");

            if (getItem(position).isDefSelected()) {
                ll.setBackgroundResource(selectedBackground);
                selectedIndex = position;
                spinner.setText(getItem(position).getValue());
            } else {
                ll.setBackgroundResource(childBackground);
            }
            return ll;
        }

        public abstract View createItemView(LayoutInflater layoutInflater, int position, View convertView, ViewGroup parent, SpinnerRes res);
    }

    /**
     * 实体
     */
    public static class SpinnerRes {

        private String code;
        private String value;
        private boolean defSelected;

        public SpinnerRes() {
        }

        public SpinnerRes(String code, String value) {
            this.code = code;
            this.value = value;
        }

        public SpinnerRes(String code, String value, boolean defSelected) {
            this.code = code;
            this.value = value;
            this.defSelected = defSelected;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isDefSelected() {
            return defSelected;
        }

        public void setDefSelected(boolean defSelected) {
            this.defSelected = defSelected;
        }
    }
}
