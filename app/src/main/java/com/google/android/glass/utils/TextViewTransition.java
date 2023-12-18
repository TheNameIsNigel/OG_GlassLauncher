package com.google.android.glass.utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextViewTransition extends Transition {
    private static final String FONT_SIZE = "TextViewTransition:fontSize";
    private static final String[] PROPERTIES = {FONT_SIZE};
    private static final String TEXT_SIZE = "textSize";

    public TextViewTransition() {
        addTarget(TextView.class);
    }

    public TextViewTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        addTarget(TextView.class);
    }

    public String[] getTransitionProperties() {
        return PROPERTIES;
    }

    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        if (transitionValues.view instanceof TextView) {
            TextView view = (TextView) transitionValues.view;
            transitionValues.values.put(FONT_SIZE, Float.valueOf(view.getTextSize() / view.getResources().getDisplayMetrics().scaledDensity));
        }
    }

    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }
        TextView textView = (TextView) endValues.view;
        Object startFonSizeObject = startValues.values.get(FONT_SIZE);
        float startFontSize = startFonSizeObject instanceof Float ? ((Float) startFonSizeObject).floatValue() : 0.0f;
        Object endFontSizeObject = endValues.values.get(FONT_SIZE);
        return ObjectAnimator.ofFloat(textView, TEXT_SIZE, new float[]{startFontSize, endFontSizeObject instanceof Float ? ((Float) endFontSizeObject).floatValue() : 0.0f});
    }
}
