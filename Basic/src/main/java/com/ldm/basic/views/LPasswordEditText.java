package com.ldm.basic.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class LPasswordEditText extends EditText implements TextWatcher {

    private Drawable drawable;
    private long downTime;
    private float x;
    private int viewWidth;
    private boolean isShowDeleteIcon = false;

    public LPasswordEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LPasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LPasswordEditText(Context context) {
        super(context);
        init();
    }

    private void init() {
        setMaxLines(1);
        drawable = null;
        isShowDeleteIcon = getText().length() != 0;
        control();
        addTextChangedListener(this);
    }

    public void setDeleteIcon(int icon) {
        drawable = getResources().getDrawable(icon);
        control();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downTime = System.currentTimeMillis();
            x = event.getX();
            viewWidth = getWidth();
        }
        if (event.getAction() == MotionEvent.ACTION_UP && isShowDeleteIcon
                && System.currentTimeMillis() - downTime < 100) {
            if (viewWidth - x < drawable.getIntrinsicWidth()) {
                setText("");
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        isShowDeleteIcon = s.length() != 0;
        control();
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    private void control() {
        setCompoundDrawablesWithIntrinsicBounds(null, null, isShowDeleteIcon ? drawable : null, null);
    }
}
