package com.google.android.glass.launcher;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import com.google.android.glass.launcher.keyevent.KeyEventHandler;
import com.google.android.glass.launcher.keyevent.KeyEventListener;
import com.google.android.glass.launcher.viewmodels.AppsLifecycleObserver;
import com.google.android.glass.launcher.viewmodels.AppsViewModel;
import com.google.android.glass.ui.GlassBaseActivity;

public class MainActivity extends GlassBaseActivity {
    private static final String FAST_PAIR_CHANGE_ACTION = "android.settings.FAST_PAIR_CHANGE_ACTION";
    private static final String FAST_PAIR_CONFIGURE_KEY = "FAST_PAIR_CONFIGURED";
    private static final String SETTINGS_PACKAGE = "com.google.android.glass.settings";
    private static final String SHARED_PREFERENCE_KEY = "com.google.android.glass.settings";
    private KeyEventHandler keyEventHandler;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2131361838);
        getNavigationManager().navigateAsRoot(new MainFragment(), 2131230773);
        SharedPreferences preferences = getSharedPreferences("com.google.android.glass.settings", 0);
        if (!preferences.getBoolean(FAST_PAIR_CONFIGURE_KEY, false)) {
            preferences.edit().putBoolean(FAST_PAIR_CONFIGURE_KEY, true).commit();
            startFastPairSettings();
        }
        getLifecycle().addObserver(new AppsLifecycleObserver(this, (AppsViewModel) ViewModelProviders.of((FragmentActivity) this).get(AppsViewModel.class)));
        this.keyEventHandler = new KeyEventHandler(new KeyEventListener(this));
    }

    public void onBackPressed() {
        getNavigationManager().navigateBack();
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!this.keyEventHandler.onKeyUp(keyCode)) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (!this.keyEventHandler.onKeyLongPress(keyCode)) {
            return super.onKeyLongPress(keyCode, event);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void startFastPairSettings() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.glass.settings");
        if (intent != null) {
            intent.setFlags(536870912);
            intent.setAction(FAST_PAIR_CHANGE_ACTION);
            startActivity(intent);
        }
    }
}
