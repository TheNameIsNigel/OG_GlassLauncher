package com.google.android.glass.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;
import com.google.common.collect.ImmutableList;

public class GlassFragmentPagerAdapter extends FragmentPagerAdapter {
    private final Context context;
    private final FragmentManager fragmentManager;
    private final ImmutableList<Class<? extends GlassBaseFragment>> fragments;
    private final SparseArray<String> instantiatedFragmentsTags = new SparseArray<>();

    GlassFragmentPagerAdapter(Context context2, FragmentManager fragmentManager2, ImmutableList<Class<? extends GlassBaseFragment>> fragments2) {
        super(fragmentManager2);
        this.context = context2;
        this.fragmentManager = fragmentManager2;
        this.fragments = fragments2;
    }

    @NonNull
    public Fragment getItem(int position) {
        return Fragment.instantiate(this.context, ((Class) this.fragments.get(position)).getName());
    }

    public int getCount() {
        return this.fragments.size();
    }

    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        this.instantiatedFragmentsTags.put(position, fragment.getTag());
        return fragment;
    }

    /* access modifiers changed from: package-private */
    public Fragment getCurrentFragment(int position) {
        return this.fragmentManager.findFragmentByTag(this.instantiatedFragmentsTags.get(position));
    }
}
