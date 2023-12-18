package com.google.android.glass.launcher.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class ClockViewModel extends ViewModel {
    private static final String DEFAULT_FORMAT_12_HOUR = "h:mm";
    private static final String DEFAULT_FORMAT_24_HOUR = "H:mm";
    private final MutableLiveData<String> format12Hour = new MutableLiveData<>();
    private final MutableLiveData<String> format24Hour = new MutableLiveData<>();

    public ClockViewModel() {
        this.format12Hour.setValue(DEFAULT_FORMAT_12_HOUR);
        this.format24Hour.setValue(DEFAULT_FORMAT_24_HOUR);
    }

    public LiveData<String> getFormat12Hour() {
        return this.format12Hour;
    }

    public void setFormat12Hour(String format) {
        this.format12Hour.setValue(format);
    }

    public LiveData<String> getFormat24Hour() {
        return this.format24Hour;
    }

    public void setFormat24Hour(String format) {
        this.format24Hour.setValue(format);
    }
}
