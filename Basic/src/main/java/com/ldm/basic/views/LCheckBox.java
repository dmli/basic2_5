package com.ldm.basic.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * LDM  2012-4-24 下午3:00:10
 * <p/>
 * 提供了一组BaseCheckBox 使用方法：
 * BaseCheckBox是基于LinearLayout改造的一组CheckBoxChild控件集合，
 * 通过创建BaseCheckBox并传入正确的构造参数生成BaseCheckBox， 将创建的BaseCheckBox添加至界面布局内即可。
 * <p/>
 * 获取返回参数： 可以通过getSelectedKey()及getSelectedValue()方法获取当前被选中的控件的Key与Value
 */
public class LCheckBox extends LinearLayout implements OnClickListener {

    private Context lContext; // 使用者

    private boolean isCheck; // 是否可以多选

    private List<Attribute> lNewData; // 内部使用数据

    private Map<String, String> lData; // 传入数据

    private ChildStyle lChildStyle; // CheckBoxChild样式Bean

    private int selectedId = -1; // 单选使用

    private OnChangeListener listener;

    private List<CheckBoxChild> lChild = new ArrayList<CheckBoxChild>(); // CheckBoxChild集合

    /**
     * @param context    上下文
     * @param data       显示数据 Map<String, String>
     *                   如果需要保持显示数据的顺序性，请使用LinkedHashMap<String, String>
     *                   BaseCheckBox中尚未加入自定义排序功能, 如果data为空将抛出NullPointerException
     * @param isCheck    是否可以多选
     * @param childStyle 子节点的样式BaseCheckBox.ChildStyle
     */
    public LCheckBox(Context context, Map<String, String> data, boolean isCheck, ChildStyle childStyle) {
        super(context);
        lContext = context;
        lData = data;
        lChildStyle = childStyle;
        this.isCheck = isCheck;
        init();
    }

    public LCheckBox(Context context, AttributeSet attrs, Map<String, String> data, boolean isCheck, ChildStyle childStyle) {
        super(context, attrs);
        lContext = context;
        lData = data;
        lChildStyle = childStyle;
        this.isCheck = isCheck;
        init();
    }

    private void init() {

        // 验证数据的可用性
        dataVerification();

        // 整理数据，将数据格式转换成内部使用格式ArrayList<BaseCheckBox.Attribute>
        dataFormat();

        // 初始BaseCheckBox
        initCheckBoxChild();

    }

    /**
     * 验证数据的可用性
     */
    private void dataVerification() {
        if (lData == null || lData.isEmpty())
            throw new NullPointerException("data中未包含数据项.");

        if (lChildStyle.selected_Off == 0 || lChildStyle.selected_On == 0)
            throw new NullPointerException("ChildStyle中selected_Off与selected_On为空");
    }

    /**
     * 初始化CheckBoxChild
     */
    private void initCheckBoxChild() {
        if (lChildStyle.rowSize == 0) {
            // 设置水平显示
            this.setOrientation(HORIZONTAL);

            for (int i = 0; i < lNewData.size(); i++) {
                this.addView(getView(i));
            }
        } else {
            // 设置垂直显示
            this.setOrientation(VERTICAL);

            // 计算行数
            int rowLen = lNewData.size() / lChildStyle.rowSize;

            // 初始化整行数据
            for (int i = 0; i < rowLen; i++) {
                LinearLayout row = createRow();
                for (int j = 0; j < lChildStyle.rowSize; j++) {
                    row.addView(getView((i * lChildStyle.rowSize) + j));
                }
                this.addView(row);
            }

            // 计算不足一行数据
            int remain = lNewData.size() % lChildStyle.rowSize;
            if (remain > 0) {
                LinearLayout row = createRow();
                for (int i = 0; i < remain; i++) {
                    row.addView(getView(rowLen * lChildStyle.rowSize + i));
                }
                this.addView(row);
            }
        }
    }

    private LinearLayout createRow() {
        LinearLayout l = new LinearLayout(lContext);
        l.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        l.setPadding(0, lChildStyle.marginTop, 0, 0);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    /**
     * 数据转型
     */
    private void dataFormat() {
        lNewData = new ArrayList<Attribute>();
        Set<String> set = lData.keySet();
        for (String key : set) {
            Attribute attr = new Attribute();
            attr.key = key;
            attr.value = lData.get(key);
            lNewData.add(attr);
        }
    }

    /**
     * 生成单个CheckBoxChild
     *
     * @param position 位置
     * @return View
     */
    private View getView(int position) {
        return createView(position);
    }

    /**
     * 创建CheckBoxChild
     *
     * @param position 位置
     * @return CheckBoxChild
     */
    private CheckBoxChild createView(int position) {

        CheckBoxChild b = new CheckBoxChild(lContext);
        b.position = position;

        String spacing = "";
        for (int i = 0; i < lChildStyle.spacing; i++) {
            spacing += " ";
        }

        b.setText(spacing + lNewData.get(position).value);

        b.setCompoundDrawablesWithIntrinsicBounds(lChildStyle.selected_Off, 0, 0, 0);

        b.setTextColor(lChildStyle.textColor);

        if (lChildStyle.textSize != 0)
            b.setTextSize(lChildStyle.textSize);

        if (lChildStyle.width != 0)
            b.setWidth(lChildStyle.width);

        // 设置点击事件
        if (lChildStyle.isClick) {
            b.setOnClickListener(this);
        }

        // 添加至
        lChild.add(b);
        return b;
    }

    /**
     * 设置默认的多选项
     *
     * @param positions 设置默认选中向
     */
    public void setDefSelected(int[] positions) {
        if (!isCheck) {
            throw new ClassCastException("BaseCheckBox状态转换异常，请先设置BaseCheckBox为多选状态，在使用此方法。");
        }
        for (int i = 0; i < positions.length; i++) {
            initCheckState(lChild.get(i));
        }
    }

    /**
     * 根据KEY设置默认选项 多选使用
     *
     * @param keys keys
     */
    public void setDefSelected(String[] keys) {
        if (!isCheck) {
            throw new ClassCastException("BaseCheckBox状态转换异常，请先设置BaseCheckBox为多选状态，在使用此方法。");
        }
        for (String key : keys) {
            for (int j = 0; j < lNewData.size(); j++) {
                if (key.equals(lNewData.get(j).key)) {
                    initCheckState(lChild.get(j));
                    break;
                }
            }
        }
    }


    /**
     * 设置默认的单选项
     *
     * @param position 默认选中项
     */
    public void setDefSelected(int position) {
        if (isCheck) {
            throw new ClassCastException("BaseCheckBox状态转换异常，请先设置BaseCheckBox为单选状态，在使用此方法。");
        }
        initSingleState(lChild.get(position));// 单选
    }

    /**
     * 根据KEY设置默认选项 单选使用
     *
     * @param key key
     */
    public void setDefSelected(String key) {
        if (isCheck) {
            throw new ClassCastException("BaseCheckBox状态转换异常，请先设置BaseCheckBox为单选状态，在使用此方法。");
        }
        for (int i = 0; i < lNewData.size(); i++) {
            if (key.equals(lNewData.get(i).key)) {
                initSingleState(lChild.get(i));// 单选
                break;
            }
        }
    }

    /**
     * CheckBoxChild的点击处理事件
     */
    @Override
    public void onClick(View v) {

        CheckBoxChild child = (CheckBoxChild) v;
        if (isCheck) {
            initCheckState(child);// 多选
            if (listener != null) {
                listener.checkedChangeListener(getTag() + "", getSelectedKeys());
            }
        } else {
            initSingleState(child);// 单选
            if (listener != null) {
                listener.radioChangeListener(getTag() + "", getSelectedKey());
            }
        }
    }

    /**
     * 处理单选CheckBoxChild的状态转换
     *
     * @param child CheckBoxChild
     */
    private void initSingleState(CheckBoxChild child) {
        if (child == null || selectedId == child.position)
            return;

        if (selectedId != -1) {
            lChild.get(selectedId).setCompoundDrawablesWithIntrinsicBounds(lChildStyle.selected_Off, 0, 0, 0);

        }
        child.setCompoundDrawablesWithIntrinsicBounds(lChildStyle.selected_On, 0, 0, 0);
        selectedId = child.position;
    }

    /**
     * 处理多选CheckBoxChild的状态转换
     *
     * @param child CheckBoxChild
     */
    private void initCheckState(CheckBoxChild child) {
        if (child == null)
            return;

        if (child.check) {
            child.setCompoundDrawablesWithIntrinsicBounds(lChildStyle.selected_Off, 0, 0, 0);
            child.check = false;
        } else {
            child.setCompoundDrawablesWithIntrinsicBounds(lChildStyle.selected_On, 0, 0, 0);
            child.check = true;
        }
    }

    /**
     * 获取单个选中项的KEY
     *
     * @return KEY
     */
    public String getSelectedKey() {
        if (!isCheck)
            return lNewData.get(selectedId).key;
        return null;
    }

    /**
     * 获取单个选中项的Value
     *
     * @return Value
     */
    public String getSelectedValue() {
        if (!isCheck)
            return lNewData.get(selectedId).value;
        return null;
    }

    /**
     * 获取多选状态下的选中项KEY集合
     *
     * @return KEY集合
     */
    public List<String> getSelectedKeys() {
        if (!isCheck)
            return null;

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < lChild.size(); i++) {
            if (lChild.get(i).check) {
                list.add(lNewData.get(i).key);
            }
        }
        return list;
    }

    /**
     * 获取多选状态下的选中项Value集合
     *
     * @return Value集合
     */
    public List<String> getSelectedValues() {
        if (!isCheck)
            return null;

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < lChild.size(); i++) {
            if (lChild.get(i).check) {
                list.add(lNewData.get(i).value);
            }
        }
        return list;
    }

    public void setListener(OnChangeListener listener) {
        this.listener = listener;
    }

    private class CheckBoxChild extends TextView {

        // 保留CheckBoxChild出现的位置
        private int position = -1;

        // 多选时使用，用于验证选中状态 默认false
        private boolean check;

        public CheckBoxChild(Context context) {
            super(context);
            init();
        }

        public CheckBoxChild(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public CheckBoxChild(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init();
        }

        private void init() {
            // 初始后隐藏背景
            // super.getBackground().setAlpha(0);

        }

    }

    /**
     * CheckBoxChild属性
     *
     * @author LDM
     */
    private class Attribute {

        /**
         * KEY 隐藏保存
         */
        public String key;

        /**
         * Value 显示
         */
        public String value;

    }

    /**
     * CheckBoxChild样式类
     *
     * @author LDM
     */
    public static class ChildStyle {

        /**
         * 字体颜色
         */
        public int textColor = Color.BLACK;

        /**
         * 字体大小
         */
        public int textSize;

        /**
         * 选中图标 必填项
         */
        public int selected_On;

        /**
         * 未选中图标 必填项
         */
        public int selected_Off;

        /**
         * 每行显示数量 默认为一行显示
         */
        public int rowSize;

        /**
         * 行间距，默认为5
         */
        public int marginTop = 5;

        /**
         * 图标与文字间的距离 默认1
         * spacing是“空格”个数，而不是像素点
         */
        public int spacing = 1;

        /**
         * CheckBoxChild 的宽度 默认为自动适应 单位像素
         */
        public int width;

        /**
         * 是否可以点击
         */
        public boolean isClick;

    }

    public static class OnChangeListener {

        /**
         * 多选回调
         *
         * @param tag 用户与存储在该View上的tag
         * @param key 改变项的KEY的集合
         */
        public void checkedChangeListener(String tag, List<String> key) {
        }

        /**
         * 单选回调
         *
         * @param tag 用户与存储在该View上的tag
         * @param key 改变项的KEY
         */
        public void radioChangeListener(String tag, String key) {
        }
    }

}
