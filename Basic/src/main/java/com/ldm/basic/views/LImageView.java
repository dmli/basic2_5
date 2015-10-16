package com.ldm.basic.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class LImageView extends ImageView{

	   public LImageView(Context context) {
	        super(context);
	    }

	    public LImageView(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }

	    public LImageView(Context context, AttributeSet attrs, int defStyleAttr) {
	        super(context, attrs, defStyleAttr);
	    }

	    @Override
	    protected void onDraw(Canvas canvas) {
	        try {
	            super.onDraw(canvas);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	
}
