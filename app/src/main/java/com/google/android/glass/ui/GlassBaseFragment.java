package com.google.android.glass.ui;

import android.content.Intent;
import com.google.android.glass.ui.GlassGestureDetector;
import com.google.android.glass.ui.GlassNavigationManager;

public abstract class GlassBaseFragment extends GlassLifecycleFragment implements GlassGestureDetector.OnGestureListener, GlassNavigationManager.NavigationManagerProvider {

    /* renamed from: -com-google-android-glass-ui-GlassGestureDetector$GestureSwitchesValues  reason: not valid java name */
    private static final /* synthetic */ int[] f19comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues = null;

    /* renamed from: -getcom-google-android-glass-ui-GlassGestureDetector$GestureSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m210getcomgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues() {
        if (f19comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues != null) {
            return f19comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues;
        }
        int[] iArr = new int[GlassGestureDetector.Gesture.values().length];
        try {
            iArr[GlassGestureDetector.Gesture.SWIPE_BACKWARD.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.SWIPE_DOWN.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.SWIPE_FORWARD.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.SWIPE_UP.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.TAP.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.TAP_AND_HOLD.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.TWO_FINGER_SWIPE_BACKWARD.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.TWO_FINGER_SWIPE_DOWN.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.TWO_FINGER_SWIPE_FORWARD.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.TWO_FINGER_SWIPE_UP.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.TWO_FINGER_TAP.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        f19comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues = iArr;
        return iArr;
    }

    public GlassNavigationManager getNavigationManager() {
        return ((GlassNavigationManager.NavigationManagerProvider) getActivity()).getNavigationManager();
    }

    public boolean onGesture(GlassGestureDetector.Gesture glassGesture) {
        switch (m210getcomgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues()[glassGesture.ordinal()]) {
            case 1:
                if (!getNavigationManager().navigateBack()) {
                    getActivity().onBackPressed();
                }
                return true;
            case 2:
                startActivity(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").setFlags(268435456));
                return true;
            default:
                return false;
        }
    }
}
