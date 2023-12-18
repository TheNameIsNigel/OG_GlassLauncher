package com.google.android.glass.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.glass.ui.GlassGestureDetector;
import com.google.common.collect.ImmutableList;

public abstract class GlassFragmentPager extends GlassBaseFragment {
    private static final int PAGE_INDICATOR_THRESHOLD = 2;
    private GlassFragmentPagerAdapter pagerAdapter;
    protected ViewPager viewPager;

    public abstract ImmutableList<Class<? extends GlassBaseFragment>> getFragments();

    public int getDefaultFragmentIndex() {
        return 0;
    }

    public boolean isPageIndicatorEnabled() {
        return true;
    }

    public String getTitle() {
        return null;
    }

    @StringRes
    public int getTitleRes() {
        return -1;
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pager_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImmutableList<Class<? extends GlassBaseFragment>> fragments = getFragments();
        if (getDefaultFragmentIndex() >= fragments.size()) {
            throw new IndexOutOfBoundsException("Default fragment index (" + getDefaultFragmentIndex() + ") greater than or equals to fragments quantity(" + fragments.size() + "): " + fragments);
        }
        this.viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        this.pagerAdapter = new GlassFragmentPagerAdapter(getContext(), getChildFragmentManager(), fragments);
        this.viewPager.setAdapter(this.pagerAdapter);
        this.viewPager.setPageTransformer(false, new GlassFragmentPagerTransformer());
        this.viewPager.setCurrentItem(getDefaultFragmentIndex());
        view.findViewById(R.id.overlay_view).setOnTouchListener(new $Lambda$d26v94cqOs_UTGPa6gxBsoJWtaw(this));
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.page_indicator);
        if (fragments.size() < 2 || (!isPageIndicatorEnabled())) {
            tabLayout.setVisibility(8);
        } else {
            tabLayout.setupWithViewPager(this.viewPager);
        }
        TextView title = (TextView) view.findViewById(R.id.pager_title);
        if (getTitle() != null) {
            title.setText(getTitle());
        } else if (getTitleRes() != -1) {
            title.setText(getTitleRes());
        } else {
            title.setVisibility(8);
        }
        if (title.getVisibility() != 8) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(getContext(), R.layout.pager_fragment);
            constraintSet.connect(R.id.view_pager, 4, R.id.page_indicator, 3);
            constraintSet.applyTo((ConstraintLayout) view.findViewById(R.id.container));
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: lambda$-com_google_android_glass_ui_GlassFragmentPager_2783  reason: not valid java name */
    public /* synthetic */ boolean m211lambda$com_google_android_glass_ui_GlassFragmentPager_2783(View v, MotionEvent event) {
        return this.viewPager.onTouchEvent(event);
    }

    public boolean onGesture(GlassGestureDetector.Gesture glassGesture) {
        GlassBaseFragment fragment = getCurrentFragment();
        if (fragment != null) {
            return fragment.onGesture(glassGesture);
        }
        return false;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        GlassBaseFragment fragment = getCurrentFragment();
        if (fragment != null) {
            return fragment.onScroll(e1, e2, distanceX, distanceY);
        }
        return false;
    }

    public void onTouchEnded() {
        GlassBaseFragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.onTouchEnded();
        }
    }

    private GlassBaseFragment getCurrentFragment() {
        return (GlassBaseFragment) this.pagerAdapter.getCurrentFragment(this.viewPager.getCurrentItem());
    }
}
