package com.google.android.glass.launcher.viewmodels;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;

public class AppsLifecycleObserver_LifecycleAdapter implements GenericLifecycleObserver {
    final AppsLifecycleObserver mReceiver;

    AppsLifecycleObserver_LifecycleAdapter(AppsLifecycleObserver receiver) {
        this.mReceiver = receiver;
    }

    public void onStateChanged(LifecycleOwner owner, Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            this.mReceiver.onResume();
        }
        if (event == Lifecycle.Event.ON_PAUSE) {
            this.mReceiver.onPause();
        }
        if (event == Lifecycle.Event.ON_CREATE) {
            this.mReceiver.loadAppsAsync();
        }
    }

    public Object getReceiver() {
        return this.mReceiver;
    }
}
