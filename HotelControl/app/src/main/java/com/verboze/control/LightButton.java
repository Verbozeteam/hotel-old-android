package com.verboze.control;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class LightButton extends ImageView {
    private boolean state = false;
    public int reson = R.drawable.lighton;
    public int resoff = R.drawable.lightoff;

    public LightButton(Context context) {
        super(context);
    }

    public LightButton(Context context, AttributeSet set) {
        super(context, set);
    }
    public LightButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setState(boolean s) {
        state = s;
        if (state)
            this.setImageResource(reson);
        else
            this.setImageResource(resoff);
    }

    public void toggle() {
        state = !state;
        if (state)
            this.setImageResource(reson);
        else
            this.setImageResource(resoff);

    }

    public boolean isOn() {
        return state;
    }
};
