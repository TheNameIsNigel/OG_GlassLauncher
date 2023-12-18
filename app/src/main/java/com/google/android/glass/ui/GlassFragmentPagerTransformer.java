package com.google.android.glass.ui;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;

public class GlassFragmentPagerTransformer implements ViewPager.PageTransformer {
    public void transformPage(@NonNull View page, float position) {
        page.setAlpha(1.0f - Math.abs(position));
    }
}
