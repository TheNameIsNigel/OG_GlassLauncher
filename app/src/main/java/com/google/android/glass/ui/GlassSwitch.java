package com.google.android.glass.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Switch;

public class GlassSwitch extends Switch {
    public GlassSwitch(Context context) {
        super(context);
    }

    public GlassSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GlassSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GlassSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
