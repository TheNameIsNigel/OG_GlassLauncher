package com.google.android.glass.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.glass.ui.GlassNavigationManager;

public abstract class GlassBaseActivity extends GlassLifecycleActivity implements GlassNavigationManager.NavigationManagerProvider {
    private View decorView;
    private GlassGestureDetector glassGestureDetector;
    private GlassNavigationManager navigationManager;

    /* access modifiers changed from: protected */
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.decorView = getWindow().getDecorView();
        this.decorView.setOnSystemUiVisibilityChangeListener(new $Lambda$Zazkpe9e4FKm_qzFPXn6r3FADog(this));
        this.navigationManager = new GlassNavigationManager(getSupportFragmentManager());
        this.glassGestureDetector = new GlassGestureDetector(this, this.navigationManager);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: lambda$-com_google_android_glass_ui_GlassBaseActivity_915  reason: not valid java name */
    public /* synthetic */ void m209lambda$com_google_android_glass_ui_GlassBaseActivity_915(int visibility) {
        if ((visibility & 4) == 0) {
            hideSystemUI();
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        hideSystemUI();
    }

    private void hideSystemUI() {
        this.decorView.setSystemUiVisibility(3846);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (!this.glassGestureDetector.onTouchEvent(motionEvent)) {
            return super.dispatchTouchEvent(motionEvent);
        }
        return true;
    }

    public GlassNavigationManager getNavigationManager() {
        return this.navigationManager;
    }

    public void onBackPressed() {
        if (!this.navigationManager.navigateBack()) {
            super.onBackPressed();
        }
    }
}
