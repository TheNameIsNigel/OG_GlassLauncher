package com.google.android.glass.launcher.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import com.google.android.glass.launcher.appdrawer.AppItem;
import java.util.List;

public class AppsViewModel extends ViewModel {
    private final MutableLiveData<List<AppItem>> glassApps = new MutableLiveData<>();
    private final MutableLiveData<List<AppItem>> systemApps = new MutableLiveData<>();

    public LiveData<List<AppItem>> getSystemApps() {
        return this.systemApps;
    }

    /* access modifiers changed from: package-private */
    public void setSystemApps(List<AppItem> appItems) {
        this.systemApps.setValue(appItems);
    }

    public LiveData<List<AppItem>> getGlassApps() {
        return this.glassApps;
    }

    /* access modifiers changed from: package-private */
    public void setGlassApps(List<AppItem> appItems) {
        this.glassApps.setValue(appItems);
    }
}
