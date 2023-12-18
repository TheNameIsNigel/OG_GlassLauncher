package com.google.android.glass.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class GlassSeekBar extends SeekBar {
    public GlassSeekBar(Context context) {
        super(context);
    }

    public GlassSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GlassSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GlassSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
