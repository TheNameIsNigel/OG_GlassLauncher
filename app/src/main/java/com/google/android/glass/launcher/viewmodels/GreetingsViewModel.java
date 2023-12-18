package com.google.android.glass.launcher.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class GreetingsViewModel extends ViewModel {
    private final MutableLiveData<String> greetingsMessage = new MutableLiveData<>();

    public LiveData<String> getGreetingsMessage() {
        return this.greetingsMessage;
    }

    /* access modifiers changed from: package-private */
    public void setGreetingsMessage(String message) {
        this.greetingsMessage.setValue(message);
    }
}
