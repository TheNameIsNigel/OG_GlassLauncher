package com.google.android.glass.launcher.settings;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import com.google.android.glass.launcher.viewmodels.ClockViewModel;
import com.google.android.glass.launcher.viewmodels.IndicatorsLifecycleObserver;
import com.google.android.glass.launcher.viewmodels.IndicatorsViewModel;
import com.google.android.glass.ui.GlassBaseFragment;
import com.google.android.glass.ui.GlassGestureDetector;
import com.google.android.glass.utils.IntentUtils;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends GlassBaseFragment {

    /* renamed from: -com-google-android-glass-ui-GlassGestureDetector$GestureSwitchesValues  reason: not valid java name */
    private static final /* synthetic */ int[] f12comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues = null;
    private static final String GLASS_SETTINGS_ACTIVITY_NAME = "com.google.android.glass.settings.MainActivity";
    private static final String GLASS_SETTINGS_PACKAGE_NAME = "com.google.android.glass.settings";
    private final Map<TextView, LiveData<String>> descriptionsMap = new HashMap();
    private final Map<ImageView, LiveData<Integer>> levelsMap = new HashMap();

    /* renamed from: -getcom-google-android-glass-ui-GlassGestureDetector$GestureSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m204getcomgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues() {
        if (f12comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues != null) {
            return f12comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues;
        }
        int[] iArr = new int[GlassGestureDetector.Gesture.values().length];
        try {
            iArr[GlassGestureDetector.Gesture.SWIPE_BACKWARD.ordinal()] = 2;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.SWIPE_DOWN.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.SWIPE_FORWARD.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.SWIPE_UP.ordinal()] = 5;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[GlassGestureDetector.Gesture.TAP.ordinal()] = 1;
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
        f12comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues = iArr;
        return iArr;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(2131361849, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        IndicatorsViewModel indicatorsViewModel = (IndicatorsViewModel) ViewModelProviders.of(getActivity()).get(IndicatorsViewModel.class);
        this.levelsMap.put((ImageView) view.findViewById(2131230933), indicatorsViewModel.getWifiLevel());
        this.levelsMap.put((ImageView) view.findViewById(2131230757), indicatorsViewModel.getBatteryLevel());
        this.levelsMap.put((ImageView) view.findViewById(2131230761), indicatorsViewModel.getBluetoothLevel());
        this.levelsMap.put((ImageView) view.findViewById(2131230931), indicatorsViewModel.getVolumeLevel());
        this.descriptionsMap.put((TextView) view.findViewById(2131230932), indicatorsViewModel.getWifiDescription());
        this.descriptionsMap.put((TextView) view.findViewById(2131230756), indicatorsViewModel.getBatteryDescription());
        this.descriptionsMap.put((TextView) view.findViewById(2131230760), indicatorsViewModel.getBluetoothDescription());
        this.descriptionsMap.put((TextView) view.findViewById(2131230930), indicatorsViewModel.getVolumeDescription());
        observeClockViewModel(view);
        observeIndicatorsViewModel(indicatorsViewModel);
    }

    public boolean onGesture(GlassGestureDetector.Gesture glassGesture) {
        switch (m204getcomgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues()[glassGesture.ordinal()]) {
            case 1:
                if (!IntentUtils.isPackageInstalled(getContext(), GLASS_SETTINGS_PACKAGE_NAME)) {
                    return true;
                }
                startActivity(new Intent().setComponent(new ComponentName(GLASS_SETTINGS_PACKAGE_NAME, GLASS_SETTINGS_ACTIVITY_NAME)));
                return true;
            default:
                return super.onGesture(glassGesture);
        }
    }

    private void observeClockViewModel(View view) {
        ClockViewModel clockViewModel = (ClockViewModel) ViewModelProviders.of(getActivity()).get(ClockViewModel.class);
        TextClock textClock = (TextClock) view.findViewById(2131230883);
        LiveData<String> format12Hour = clockViewModel.getFormat12Hour();
        textClock.getClass();
        format12Hour.observe(this, new $Lambda$P_zNiBKVZg5GRykyTwqgckWRxTw((byte) 0, textClock));
        LiveData<String> format24Hour = clockViewModel.getFormat24Hour();
        textClock.getClass();
        format24Hour.observe(this, new $Lambda$P_zNiBKVZg5GRykyTwqgckWRxTw((byte) 1, textClock));
    }

    private void observeIndicatorsViewModel(IndicatorsViewModel indicatorsViewModel) {
        getLifecycle().addObserver(new IndicatorsLifecycleObserver(getContext(), indicatorsViewModel));
        for (Map.Entry<ImageView, LiveData<Integer>> entry : this.levelsMap.entrySet()) {
            ImageView key = entry.getKey();
            key.getClass();
            entry.getValue().observe(this, new $Lambda$P_zNiBKVZg5GRykyTwqgckWRxTw((byte) 2, key));
        }
        for (Map.Entry<TextView, LiveData<String>> entry2 : this.descriptionsMap.entrySet()) {
            TextView key2 = entry2.getKey();
            key2.getClass();
            entry2.getValue().observe(this, new $Lambda$P_zNiBKVZg5GRykyTwqgckWRxTw((byte) 3, key2));
        }
    }
}
