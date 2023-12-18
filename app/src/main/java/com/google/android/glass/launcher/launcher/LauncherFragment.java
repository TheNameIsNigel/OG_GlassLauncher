package com.google.android.glass.launcher.launcher;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import com.google.android.glass.fastpairservice.aidl.IFastPairService;
import com.google.android.glass.fastpairservice.aidl.IFastPairStateCallback;
import com.google.android.glass.launcher.appdrawer.AppDrawerFragment;
import com.google.android.glass.launcher.viewmodels.ClockViewModel;
import com.google.android.glass.launcher.viewmodels.GreetingsLifecycleObserver;
import com.google.android.glass.launcher.viewmodels.GreetingsViewModel;
import com.google.android.glass.ui.GlassBaseFragment;
import com.google.android.glass.ui.GlassGestureDetector;
import java.util.Calendar;

public class LauncherFragment extends GlassBaseFragment {

    /* renamed from: -com-google-android-glass-ui-GlassGestureDetector$GestureSwitchesValues  reason: not valid java name */
    private static final /* synthetic */ int[] f10comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues = null;
    /* access modifiers changed from: private */
    public static final String TAG = LauncherFragment.class.getSimpleName();
    private BluetoothAdapter bluetoothAdapter;
    /* access modifiers changed from: private */
    public boolean fastPairInProgress;
    /* access modifiers changed from: private */
    public IFastPairService fastPairService;
    /* access modifiers changed from: private */
    public IFastPairStateCallback.Stub fastPairStateCallback = new IFastPairStateCallback.Stub() {
        public void onKeysWritten(int numKeys) {
            LauncherFragment.this.updateBluetoothAdapter();
        }

        public void onPairingStarted() {
            synchronized (LauncherFragment.this) {
                boolean unused = LauncherFragment.this.fastPairInProgress = true;
            }
        }

        public void onPairingCompleted(boolean successful) {
            synchronized (LauncherFragment.this) {
                boolean unused = LauncherFragment.this.fastPairInProgress = false;
                if (!LauncherFragment.this.launcherResumed) {
                    LauncherFragment.this.disableFastPair();
                }
            }
        }
    };
    private TextView greetings;
    private GreetingsLifecycleObserver greetingsLifecycleObserver;
    private final LauncherFragmentAnimator launcherFragmentAnimator = new LauncherFragmentAnimator(this);
    /* access modifiers changed from: private */
    public boolean launcherResumed;
    private ImageView leftArrow;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder) {
            synchronized (LauncherFragment.this) {
                IFastPairService unused = LauncherFragment.this.fastPairService = IFastPairService.Stub.asInterface(binder);
                try {
                    LauncherFragment.this.fastPairService.setStateCallback(LauncherFragment.this.fastPairStateCallback);
                } catch (RemoteException e) {
                    Log.w(LauncherFragment.TAG, "Could not access Fast Pair service");
                }
                LauncherFragment.this.updateBluetoothAdapter();
            }
            return;
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (LauncherFragment.this) {
                IFastPairService unused = LauncherFragment.this.fastPairService = null;
            }
        }
    };
    private TextClock textClock;

    /* renamed from: -getcom-google-android-glass-ui-GlassGestureDetector$GestureSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m192getcomgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues() {
        if (f10comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues != null) {
            return f10comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues;
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
        f10comgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues = iArr;
        return iArr;
    }

    /* access modifiers changed from: package-private */
    public View getGreetings() {
        return this.greetings;
    }

    /* access modifiers changed from: package-private */
    public View getLeftArrow() {
        return this.leftArrow;
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(2131361837, container, false);
        this.leftArrow = (ImageView) view.findViewById(2131230816);
        this.greetings = (TextView) view.findViewById(2131230803);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        getContext().bindServiceAsUser(new Intent().setComponent(new ComponentName(IFastPairService.PACKAGE, IFastPairService.CLASS)), this.serviceConnection, 1, Process.myUserHandle());
        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ClockViewModel clockViewModel = (ClockViewModel) ViewModelProviders.of(getActivity()).get(ClockViewModel.class);
        GreetingsViewModel greetingsViewModel = (GreetingsViewModel) ViewModelProviders.of(getActivity()).get(GreetingsViewModel.class);
        this.greetingsLifecycleObserver = new GreetingsLifecycleObserver(getContext(), greetingsViewModel, Calendar.getInstance());
        getLifecycle().addObserver(this.greetingsLifecycleObserver);
        this.textClock = (TextClock) view.findViewById(2131230827);
        LiveData<String> format12Hour = clockViewModel.getFormat12Hour();
        TextClock textClock2 = this.textClock;
        textClock2.getClass();
        format12Hour.observe(this, new $Lambda$Z5lEYezyAD8fUNUhk583oyfG5Y((byte) 0, textClock2));
        LiveData<String> format24Hour = clockViewModel.getFormat24Hour();
        TextClock textClock3 = this.textClock;
        textClock3.getClass();
        format24Hour.observe(this, new $Lambda$Z5lEYezyAD8fUNUhk583oyfG5Y((byte) 1, textClock3));
        LiveData<String> greetingsMessage = greetingsViewModel.getGreetingsMessage();
        TextView textView = this.greetings;
        textView.getClass();
        greetingsMessage.observe(this, new $Lambda$Z5lEYezyAD8fUNUhk583oyfG5Y((byte) 2, textView));
    }

    public void onResume() {
        this.greetingsLifecycleObserver.refreshCalendar(Calendar.getInstance());
        super.onResume();
        synchronized (this) {
            this.launcherResumed = true;
            updateBluetoothAdapter();
        }
        this.launcherFragmentAnimator.startEntryAnimation();
    }

    /* access modifiers changed from: private */
    public synchronized void updateBluetoothAdapter() {
        int pairedKeys = -1;
        if (this.fastPairService != null) {
            try {
                if (this.fastPairService.isEnabled() && (!this.fastPairService.isActive())) {
                    this.fastPairService.setActive(true);
                }
                pairedKeys = this.fastPairService.getPairedKeyCount();
            } catch (RemoteException e) {
                Log.w(TAG, "Could not access Fast Pair service");
            }
        }
        if (pairedKeys == 0) {
            this.bluetoothAdapter.setScanMode(23);
        } else {
            this.bluetoothAdapter.setScanMode(21);
        }
        return;
    }

    public void onPause() {
        super.onPause();
        synchronized (this) {
            this.launcherResumed = false;
            if (!this.fastPairInProgress) {
                disableFastPair();
            }
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        synchronized (this) {
            if (this.fastPairService != null) {
                try {
                    this.fastPairService.setStateCallback((IFastPairStateCallback) null);
                } catch (RemoteException e) {
                    Log.w(TAG, "Could not access Fast Pair service");
                }
            }
            disableFastPair();
        }
        getContext().unbindService(this.serviceConnection);
        getLifecycle().removeObserver(this.greetingsLifecycleObserver);
        this.bluetoothAdapter.setScanMode(21);
        return;
    }

    public boolean onGesture(GlassGestureDetector.Gesture glassGesture) {
        switch (m192getcomgoogleandroidglassuiGlassGestureDetector$GestureSwitchesValues()[glassGesture.ordinal()]) {
            case 1:
                getNavigationManager().navigateFadeInOut(new AppDrawerFragment(), 2131230773, this.textClock);
                return true;
            default:
                return super.onGesture(glassGesture);
        }
    }

    /* access modifiers changed from: private */
    public synchronized void disableFastPair() {
        if (this.fastPairService != null) {
            try {
                this.fastPairService.setActive(false);
            } catch (RemoteException e) {
                Log.w(TAG, "Could not access fast pair service");
            }
        } else {
            return;
        }
    }
}
