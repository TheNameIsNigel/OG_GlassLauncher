package com.google.android.glass.launcher.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class IndicatorsViewModel extends ViewModel {
    private final MutableLiveData<String> batteryDescription = new MutableLiveData<>();
    private final MutableLiveData<Integer> batteryLevel = new MutableLiveData<>();
    private final MutableLiveData<String> bluetoothDescription = new MutableLiveData<>();
    private final MutableLiveData<Integer> bluetoothLevel = new MutableLiveData<>();
    private final MutableLiveData<String> volumeDescription = new MutableLiveData<>();
    private final MutableLiveData<Integer> volumeLevel = new MutableLiveData<>();
    private final MutableLiveData<String> wifiDescription = new MutableLiveData<>();
    private final MutableLiveData<Integer> wifiLevel = new MutableLiveData<>();

    public LiveData<Integer> getWifiLevel() {
        return this.wifiLevel;
    }

    /* access modifiers changed from: package-private */
    public void setWifiLevel(int level) {
        this.wifiLevel.setValue(Integer.valueOf(level));
    }

    public MutableLiveData<String> getWifiDescription() {
        return this.wifiDescription;
    }

    /* access modifiers changed from: package-private */
    public void setWifiDescription(String description) {
        this.wifiDescription.setValue(description);
    }

    public LiveData<Integer> getBatteryLevel() {
        return this.batteryLevel;
    }

    /* access modifiers changed from: package-private */
    public void setBatteryLevel(int level) {
        this.batteryLevel.setValue(Integer.valueOf(level));
    }

    public MutableLiveData<String> getBatteryDescription() {
        return this.batteryDescription;
    }

    /* access modifiers changed from: package-private */
    public void setBatteryDescription(String description) {
        this.batteryDescription.setValue(description);
    }

    public LiveData<Integer> getBluetoothLevel() {
        return this.bluetoothLevel;
    }

    /* access modifiers changed from: package-private */
    public void setBluetoothLevel(int level) {
        this.bluetoothLevel.setValue(Integer.valueOf(level));
    }

    public MutableLiveData<String> getBluetoothDescription() {
        return this.bluetoothDescription;
    }

    /* access modifiers changed from: package-private */
    public void setBluetoothDescription(String description) {
        this.bluetoothDescription.setValue(description);
    }

    public LiveData<Integer> getVolumeLevel() {
        return this.volumeLevel;
    }

    /* access modifiers changed from: package-private */
    public void setVolumeLevel(int level) {
        this.volumeLevel.setValue(Integer.valueOf(level));
    }

    public MutableLiveData<String> getVolumeDescription() {
        return this.volumeDescription;
    }

    /* access modifiers changed from: package-private */
    public void setVolumeDescription(String description) {
        this.volumeDescription.setValue(description);
    }
}
