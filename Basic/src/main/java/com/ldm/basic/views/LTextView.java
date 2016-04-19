package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ldm on 16/4/11.
 * 这个TextView去掉了上下两边的空白区域，且仅适合用来显示标准文本使用，当遇到一些特殊字符时可能会出现显示不完整的问题
 */
public class LTextView extends TextView {

    private int lines = 1;//默认单行
    private List<String> texts;//分析后的文本集合，在绘制时使用
    private float lineSpace = 0;//默认行间距
    private int maxWidth = 0, minWidth = 0;//最大/最小宽度
    private int width;//控件的宽度

    public LTextView(Context context) {
        super(context);
        init(null);
    }

    public LTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }


    public LTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 目前仅支持通过android:lines="number"设置支持行数，-1为无限
     *
     * @param attrs AttributeSet
     */
    private void init(AttributeSet attrs) {
        if (attrs != null) {
            lines = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "lines", 1);
            minWidth = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "minWidth", 0);
            maxWidth = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "maxWidth", 0);
        }
    }

    @Override
    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    @Override
    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getTextViewWidth();
        int height = (int) getTextViewHeight();
        setMeasuredDimension(width, height);
        scrollTo(0, 0);
    }

    /**
     * 返回文本的可用宽度
     *
     * @return int px
     */
    private int getTextViewWidth() {
        int width;
        if (getLayoutParams().width > 0) {
            width = getLayoutParams().width;
        } else {
            String text = getText().toString();
            if (text.length() > 0) {
                width = (int) getPaint().measureText(text) + getPaddingLeft() + getPaddingRight();
            } else {
                width = getMeasuredWidth();
            }
        }
        if (maxWidth > 0) {
            width = Math.min(maxWidth, width);
        }
        if (width < minWidth) {
            width = minWidth;
        }
        return width;
    }

    /**
     * 返回TextView的高度，这个高度不含上下的白边
     *
     * @return px
     */
    private float getTextViewHeight() {
        /**
         * 计算最适合的文本
         * extra值为了增加底部的边距，如果不增加这2dp的附加值文本显示会被切掉一块
         */
        final Paint paint = getPaint();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();
        final int lines = getTextLines();
        final float singleRowHeight = (paint.getTextSize());
        lineSpace = getPaint().getFontMetricsInt(null) - singleRowHeight;
        return paddingTop + paddingBottom + lines * singleRowHeight + lineSpace * (lines - 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (texts != null && texts.size() > 0) {
            final TextPaint paint = getPaint();
            final int top = getPaddingTop();
            final int left = getPaddingLeft();
            float fontOffset = paint.getFontSpacing() - getTextSize();
            final float singleLineBottom = (paint.getTextSize() - fontOffset * 1.25f);
            int lastBottom = top;
            for (String str : texts) {
                lastBottom += singleLineBottom;
                canvas.drawText(str, left, lastBottom, paint);
                lastBottom += lineSpace;
            }
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        getTextLines();
    }

    /**
     * 这个方法返回当前文本的行数，并填充了texts属性
     *
     * @return text lines
     */
    private int getTextLines() {
        if (getText().length() <= 0) {
            /**
             * 没有文本时使用使用单行
             */
            return 1;
        }
        if (texts == null) {
            texts = new ArrayList<>();
        } else {
            texts.clear();
        }
        StringBuilder lineText = new StringBuilder();
        final Paint paint = getPaint();
        final CharSequence c = getText();
        final int len = c.length();
        final int padding = getPaddingLeft() + getPaddingRight();
        int lastCharWidth = 0;
        for (int i = 0; i < len; i++) {
            String s = String.valueOf(c.charAt(i));
            if (paint.measureText(lineText.toString() + s) <= width - padding) {
                lineText.append(s);
                lastCharWidth = (int) paint.measureText(s);
            } else {
                if (texts.size() == lines - 1 && TextUtils.TruncateAt.END == getEllipsize()) {
                    if (lineText.length() > 2) {
                        /**
                         * 这里检查最后一个字符是否与 "..." 宽度符合，如果不如何删除最后两个字符然后填充 "..."
                         */
                        if (lastCharWidth >= paint.measureText("...")) {
                            lineText.deleteCharAt(lineText.length() - 1);
                        } else {
                            lineText.deleteCharAt(lineText.length() - 1);
                            lineText.deleteCharAt(lineText.length() - 1);
                        }
                        //删掉后面的字符，并填充...
                        lineText.append("...");
                    }
                }
                //满足最大行时退出循环
                if (texts.size() >= lines) {
                    break;
                }
                texts.add(lineText.toString());
                lineText = new StringBuilder();
                lineText.append(s);
            }
            if (i == len - 1 && lineText.length() > 0 && texts.size() < lines) {
                texts.add(lineText.toString());
            }
        }
        return texts.size();
    }

}
