package com.verboze.control;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class DimmerLayout extends LinearLayout {
    private Main main = null;

    public DimmerLayout(Context context) {
        super(context);
    }

    public DimmerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DimmerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMain(Main m) {
        main = m;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (main != null)
            return main.setScreenDim(true);
        return false;
    }
}
