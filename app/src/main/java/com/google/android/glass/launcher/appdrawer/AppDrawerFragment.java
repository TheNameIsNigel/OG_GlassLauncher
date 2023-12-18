package com.google.android.glass.launcher.appdrawer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.ChangeBounds;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import com.google.android.glass.launcher.viewmodels.AppsLifecycleObserver;
import com.google.android.glass.launcher.viewmodels.AppsViewModel;
import com.google.android.glass.launcher.viewmodels.ClockViewModel;
import com.google.android.glass.ui.GlassBaseFragment;
import com.google.android.glass.ui.GlassGestureDetector;
import com.google.android.glass.utils.TextViewTransition;
import java.util.List;

public class AppDrawerFragment extends GlassBaseFragment {

    /* renamed from: -com-google-android-glass-ui-GlassGestureDetector$GestureSwitchesValues  reason: not valid java name */
    private static final /* synthetic */ int[] f6comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues = null;
    private static final TransitionSet APP_DRAWER_TRANSITION_SET = new TransitionSet().addTransition(new ChangeBounds()).addTransition(new TextViewTransition()).setDuration(TRANSITION_DURATION_MILLIS);
    private static final String SHOW_SYSTEM_APPS = "SHOW_SYSTEM_APPS";
    private static final long TRANSITION_DURATION_MILLIS = 500;
    private AppDrawerViewHelper appDrawerViewHelper;
    private AppsLifecycleObserver appsLifecycleObserver;
    private boolean showSystemApps;

    /* renamed from: -getcom-google-android-glass-ui-GlassGestureDetector$GestureSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m172getcomgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues() {
        if (f6comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues != null) {
            return f6comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues;
        }
        int[] iArr = new int[GlassGestureDetector.Gesture.values().length];
        try {
            iArr[GlassGestureDetector.Gesture.SWIPE_BACKWARD.ordinal()] = 4;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.SWIPE_DOWN.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.SWIPE_FORWARD.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.SWIPE_UP.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.TAP.ordinal()] = 3;
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
        f6comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues = iArr;
        return iArr;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        boolean z = false;
        super.onCreate(savedInstanceState);
        setSharedElementEnterTransition(APP_DRAWER_TRANSITION_SET);
        if (getArguments() != null) {
            z = getArguments().getBoolean(SHOW_SYSTEM_APPS, false);
        }
        this.showSystemApps = z;
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(2131361818, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ClockViewModel clockViewModel = (ClockViewModel) ViewModelProviders.of(getActivity()).get(ClockViewModel.class);
        TextClock textClock = (TextClock) view.findViewById(2131230749);
        LiveData<String> format12Hour = clockViewModel.getFormat12Hour();
        textClock.getClass();
        format12Hour.observe(this, new $Lambda$JOYEKuadl2KdsA6UUDhn_n7kE((byte) 0, textClock));
        LiveData<String> format24Hour = clockViewModel.getFormat24Hour();
        textClock.getClass();
        format24Hour.observe(this, new $Lambda$JOYEKuadl2KdsA6UUDhn_n7kE((byte) 1, textClock));
        this.appDrawerViewHelper = new AppDrawerViewHelper(view);
        AppsViewModel appsViewModel = (AppsViewModel) ViewModelProviders.of(getActivity()).get(AppsViewModel.class);
        this.appsLifecycleObserver = new AppsLifecycleObserver(getContext(), appsViewModel);
        getLifecycle().addObserver(this.appsLifecycleObserver);
        LiveData<List<AppItem>> apps = this.showSystemApps ? appsViewModel.getSystemApps() : appsViewModel.getGlassApps();
        AppDrawerViewHelper appDrawerViewHelper2 = this.appDrawerViewHelper;
        appDrawerViewHelper2.getClass();
        apps.observe(this, new $Lambda$JOYEKuadl2KdsA6UUDhn_n7kE((byte) 2, appDrawerViewHelper2));
    }

    public void onDestroyView() {
        super.onDestroyView();
        getLifecycle().removeObserver(this.appsLifecycleObserver);
    }

    public boolean onGesture(GlassGestureDetector.Gesture glassGesture) {
        switch (m172getcomgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues()[glassGesture.ordinal()]) {
            case 1:
            case 2:
                getNavigationManager().navigateBack();
                return true;
            case 3:
                AppItemHandler.onItemClick(this.appDrawerViewHelper.getCurrentAppView(), this.appDrawerViewHelper.getCurrentAppItem());
                return true;
            default:
                return super.onGesture(glassGesture);
        }
    }
}
