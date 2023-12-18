package com.google.android.glass.launcher.viewmodels;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;

public class GreetingsLifecycleObserver_LifecycleAdapter implements GenericLifecycleObserver {
    final GreetingsLifecycleObserver mReceiver;

    GreetingsLifecycleObserver_LifecycleAdapter(GreetingsLifecycleObserver receiver) {
        this.mReceiver = receiver;
    }

    public void onStateChanged(LifecycleOwner owner, Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            this.mReceiver.updateGreetingsMessage();
            this.mReceiver.onResume();
        }
        if (event == Lifecycle.Event.ON_PAUSE) {
            this.mReceiver.onPause();
        }
    }

    public Object getReceiver() {
        return this.mReceiver;
    }
}
