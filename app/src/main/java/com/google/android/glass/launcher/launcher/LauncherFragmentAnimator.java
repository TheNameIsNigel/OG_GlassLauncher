package com.google.android.glass.launcher.launcher;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import com.google.android.glass.launcher.indicators.IndicatorsFragment;
import java.util.UUID;

class LauncherFragmentAnimator {
    private static final float ALPHA_OPAQUE = 1.0f;
    private static final float ALPHA_TRANSPARENT = 0.0f;
    private static final long ARROW_ANIMATION_DURATION = 500;
    private static final float ARROW_ENTRY_TRANSLATION_DISTANCE = 50.0f;
    private static final float ARROW_TRANSLATION_DISTANCE = 16.0f;
    private static final long GREETINGS_ANIMATION_DURATION = 1000;
    private static final float GREETINGS_ENTRY_TRANSLATION_DISTANCE = 30.0f;
    private static final float GREETINGS_TRANSLATION_DISTANCE = -30.0f;
    private static final long INDICATORS_HIDE_ANIMATION_DELAY = 10000;
    private static final String INDICATOR_FRAGMENT_TAG = (IndicatorsFragment.class.getName() + ":" + UUID.randomUUID());
    private final Handler handler = new Handler();
    private final LauncherFragment launcherFragment;

    LauncherFragmentAnimator(LauncherFragment launcherFragment2) {
        this.launcherFragment = launcherFragment2;
    }

    /* access modifiers changed from: package-private */
    public void startEntryAnimation() {
        this.handler.removeCallbacksAndMessages((Object) null);
        setViewsTransparent(this.launcherFragment.getGreetings(), this.launcherFragment.getLeftArrow());
        this.launcherFragment.getLeftArrow().setTranslationX(-50.0f);
        this.launcherFragment.getGreetings().setTranslationY(GREETINGS_ENTRY_TRANSLATION_DISTANCE);
        this.launcherFragment.getGreetings().animate().alpha(ALPHA_OPAQUE).translationY(GREETINGS_TRANSLATION_DISTANCE).setDuration(GREETINGS_ANIMATION_DURATION).withEndAction(startArrowsEntryAnimation());
    }

    private Runnable startIndicatorEntryAnimation() {
        return new $Lambda$XEAi7YN3xyT1zPDYG2y6jPqxHJw((byte) 1, this);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: lambda$-com_google_android_glass_launcher_launcher_LauncherFragmentAnimator_2830  reason: not valid java name */
    public /* synthetic */ void m197lambda$com_google_android_glass_launcher_launcher_LauncherFragmentAnimator_2830() {
        if (isFragmentInSafeState() && this.launcherFragment.getActivity() != null) {
            FragmentManager fragmentManager = this.launcherFragment.getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = fragmentManager.findFragmentByTag(INDICATOR_FRAGMENT_TAG);
            if (fragment != null) {
                transaction.remove(fragment);
            }
            transaction.setCustomAnimations(2130837505, 0).add(2131230826, new IndicatorsFragment(), INDICATOR_FRAGMENT_TAG).commitAllowingStateLoss();
            this.handler.postDelayed(startIndicatorExitAnimation(), INDICATORS_HIDE_ANIMATION_DELAY);
            setViewsTransparent(this.launcherFragment.getLeftArrow());
        }
    }

    private Runnable startIndicatorExitAnimation() {
        return new $Lambda$XEAi7YN3xyT1zPDYG2y6jPqxHJw((byte) 2, this);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: lambda$-com_google_android_glass_launcher_launcher_LauncherFragmentAnimator_3977  reason: not valid java name */
    public /* synthetic */ void m198lambda$com_google_android_glass_launcher_launcher_LauncherFragmentAnimator_3977() {
        if (isFragmentInSafeState() && this.launcherFragment.getActivity() != null) {
            FragmentManager fragmentManager = this.launcherFragment.getActivity().getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(INDICATOR_FRAGMENT_TAG);
            if (fragment != null) {
                fragmentManager.beginTransaction().setCustomAnimations(0, 2130837506).remove(fragment).commitAllowingStateLoss();
            }
            this.handler.post(startArrowsEntryAnimation());
        }
    }

    private Runnable startArrowsEntryAnimation() {
        return new $Lambda$XEAi7YN3xyT1zPDYG2y6jPqxHJw((byte) 0, this);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: lambda$-com_google_android_glass_launcher_launcher_LauncherFragmentAnimator_4830  reason: not valid java name */
    public /* synthetic */ void m199lambda$com_google_android_glass_launcher_launcher_LauncherFragmentAnimator_4830() {
        if (isFragmentInSafeState()) {
            this.launcherFragment.getLeftArrow().animate().alpha(ALPHA_OPAQUE).translationX(ARROW_TRANSLATION_DISTANCE).setDuration(ARROW_ANIMATION_DURATION);
        }
    }

    private void setViewsTransparent(View... views) {
        for (View view : views) {
            view.setAlpha(0.0f);
        }
    }

    private boolean isFragmentInSafeState() {
        if (!this.launcherFragment.isVisible() || !this.launcherFragment.isResumed() || !(!this.launcherFragment.isRemoving())) {
            return false;
        }
        return !this.launcherFragment.isStateSaved();
    }
}
