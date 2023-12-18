package com.google.android.glass.launcher.viewmodels;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;

public class IndicatorsLifecycleObserver_LifecycleAdapter implements GenericLifecycleObserver {
    final IndicatorsLifecycleObserver mReceiver;

    IndicatorsLifecycleObserver_LifecycleAdapter(IndicatorsLifecycleObserver receiver) {
        this.mReceiver = receiver;
    }

    public void onStateChanged(LifecycleOwner owner, Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            this.mReceiver.updateWifiStatus();
            this.mReceiver.onResume();
            this.mReceiver.updateBatteryStatus();
            this.mReceiver.updateVolumeStatus();
            this.mReceiver.updateBluetoothStatus();
        }
        if (event == Lifecycle.Event.ON_PAUSE) {
            this.mReceiver.onPause();
        }
    }

    public Object getReceiver() {
        return this.mReceiver;
    }
}
