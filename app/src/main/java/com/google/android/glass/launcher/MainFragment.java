package com.google.android.glass.launcher;

import com.google.android.glass.launcher.launcher.LauncherFragment;
import com.google.android.glass.launcher.settings.SettingsFragment;
import com.google.android.glass.ui.GlassBaseFragment;
import com.google.android.glass.ui.GlassFragmentPager;
import com.google.android.glass.ui.GlassGestureDetector;
import com.google.common.collect.ImmutableList;

public class MainFragment extends GlassFragmentPager {

    /* renamed from: -com-google-android-glass-ui-GlassGestureDetector$GestureSwitchesValues  reason: not valid java name */
    private static final /* synthetic */ int[] f4comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues = null;

    /* renamed from: -getcom-google-android-glass-ui-GlassGestureDetector$GestureSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m168getcomgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues() {
        if (f4comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues != null) {
            return f4comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues;
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
        f4comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues = iArr;
        return iArr;
    }

    public ImmutableList<Class<? extends GlassBaseFragment>> getFragments() {
        return ImmutableList.of(SettingsFragment.class, LauncherFragment.class);
    }

    public int getDefaultFragmentIndex() {
        return 1;
    }

    public boolean isPageIndicatorEnabled() {
        return false;
    }

    public boolean onGesture(GlassGestureDetector.Gesture glassGesture) {
        switch (m168getcomgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues()[glassGesture.ordinal()]) {
            case 1:
            case 2:
                this.viewPager.setCurrentItem(getDefaultFragmentIndex(), true);
                return true;
            default:
                return super.onGesture(glassGesture);
        }
    }
}
