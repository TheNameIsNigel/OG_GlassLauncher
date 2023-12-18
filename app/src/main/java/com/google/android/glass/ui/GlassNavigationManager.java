package com.google.android.glass.ui;

import android.support.annotation.AnimRes;
import android.support.annotation.AnimatorRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.glass.ui.GlassGestureDetector;
import java.util.Stack;

public class GlassNavigationManager implements GlassGestureDetector.OnGestureListener {
    private FragmentManager fragmentManager;
    private Stack<GlassBaseFragment> fragments = new Stack<>();

    public interface NavigationManagerProvider {
        GlassNavigationManager getNavigationManager();
    }

    GlassNavigationManager(FragmentManager fragmentManager2) {
        if (this.fragmentManager != null) {
            throw new IllegalStateException("Already initialized!");
        }
        this.fragmentManager = fragmentManager2;
    }

    private void navigate(GlassBaseFragment to, @IdRes int container, boolean addToBackStack, @AnimRes @AnimatorRes int enterAnimation, @AnimRes @AnimatorRes int exitAnimation, @AnimRes @AnimatorRes int popEnterAnimation, @AnimRes @AnimatorRes int popExitAnimation, View sharedElement, @Nullable String tag) {
        FragmentTransaction transaction = this.fragmentManager.beginTransaction();
        if (sharedElement != null) {
            transaction.setReorderingAllowed(true);
            transaction.addSharedElement(sharedElement, ViewCompat.getTransitionName(sharedElement));
        }
        if (addToBackStack) {
            transaction.addToBackStack(to.toString());
        }
        transaction.setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation);
        transaction.replace(container, to, tag);
        transaction.commit();
        this.fragments.push(to);
    }

    public void navigateAsRoot(GlassBaseFragment fragment, @IdRes int container) {
        clearBackStack();
        navigate(fragment, container, false, 0, 0, 0, 0, (View) null, (String) null);
    }

    public void navigateFadeInOut(GlassBaseFragment to, @IdRes int container, View sharedElement) {
        navigateFadeInOut(to, container, true, sharedElement, (String) null);
    }

    public void navigateFadeInOut(GlassBaseFragment to, @IdRes int container, boolean addToBackStack, View sharedElement, @Nullable String tag) {
        navigate(to, container, addToBackStack, 17432576, 17432577, 17432576, 17432577, sharedElement, tag);
    }

    public boolean navigateBack() {
        if (this.fragments.size() <= 1) {
            return false;
        }
        this.fragmentManager.popBackStack();
        this.fragments.pop();
        return true;
    }

    @Nullable
    public Fragment getFragmentByTag(String tag) {
        return this.fragmentManager.findFragmentByTag(tag);
    }

    private void clearBackStack() {
        for (int i = 0; i < this.fragmentManager.getBackStackEntryCount(); i++) {
            this.fragmentManager.popBackStack();
        }
        this.fragments.clear();
    }

    public boolean onGesture(GlassGestureDetector.Gesture gesture) {
        if (this.fragments.empty() || !this.fragments.peek().isVisible()) {
            return false;
        }
        return this.fragments.peek().onGesture(gesture);
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (this.fragments.empty() || !this.fragments.peek().isVisible()) {
            return false;
        }
        return this.fragments.peek().onScroll(e1, e2, distanceX, distanceY);
    }

    public void onTouchEnded() {
        if (!this.fragments.empty() && this.fragments.peek().isVisible()) {
            this.fragments.peek().onTouchEnded();
        }
    }
}
