package com.google.android.glass.launcher.appdrawer;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.glass.utils.ImageUtils;
import java.util.ArrayList;
import java.util.List;

class AppDrawerViewHelper {
    private final AppDrawerAdapter appDrawerAdapter = new AppDrawerAdapter(this.appItems);
    private final AppItemPaletteListener appItemPaletteListener = new AppItemPaletteListener(this, (AppItemPaletteListener) null);
    /* access modifiers changed from: private */
    public final List<AppItem> appItems = new ArrayList();
    /* access modifiers changed from: private */
    public RecyclerView appsRecyclerView;
    /* access modifiers changed from: private */
    public final Context context;
    /* access modifiers changed from: private */
    public int currentAppIndex;
    /* access modifiers changed from: private */
    public TextView currentAppTextView;
    /* access modifiers changed from: private */
    public View currentAppView;
    /* access modifiers changed from: private */
    public ImageView imageViewIconHighlight;
    /* access modifiers changed from: private */
    public final RecyclerView.LayoutManager layoutManager;
    /* access modifiers changed from: private */
    public final SnapHelper snapHelper = new LinearSnapHelper();

    AppDrawerViewHelper(View view) {
        this.context = view.getContext();
        this.appsRecyclerView = (RecyclerView) view.findViewById(2131230752);
        this.currentAppTextView = (TextView) view.findViewById(2131230908);
        this.imageViewIconHighlight = (ImageView) view.findViewById(2131230819);
        this.layoutManager = new LinearLayoutManager(view.getContext(), 0, false);
        this.snapHelper.attachToRecyclerView(this.appsRecyclerView);
        this.appsRecyclerView.setOnFlingListener(this.snapHelper);
        this.appsRecyclerView.setLayoutManager(this.layoutManager);
        this.appsRecyclerView.setHasFixedSize(true);
        this.appsRecyclerView.setAdapter(this.appDrawerAdapter);
        this.appsRecyclerView.addOnScrollListener(new AppsRecyclerViewOnScrollListener(this, (AppsRecyclerViewOnScrollListener) null));
        this.appsRecyclerView.addItemDecoration(new AppItemDecoration(view.getResources()));
    }

    /* access modifiers changed from: package-private */
    public void setApps(List<AppItem> appItems2) {
        this.appItems.clear();
        if (appItems2.isEmpty()) {
            this.currentAppTextView.setText(this.context.getString(2131492901));
            this.imageViewIconHighlight.setVisibility(4);
            this.appsRecyclerView.setBackgroundColor(ContextCompat.getColor(this.context, 17170444));
            this.appDrawerAdapter.notifyDataSetChanged();
            return;
        }
        if (this.currentAppIndex >= appItems2.size()) {
            this.currentAppIndex = appItems2.size() - 1;
        }
        this.appItems.addAll(appItems2);
        this.appDrawerAdapter.notifyDataSetChanged();
        setLayoutForAppItem(this.currentAppIndex);
    }

    /* access modifiers changed from: package-private */
    public View getCurrentAppView() {
        return this.currentAppView;
    }

    /* access modifiers changed from: package-private */
    public AppItem getCurrentAppItem() {
        if (this.appItems.isEmpty()) {
            return null;
        }
        return this.appItems.get(this.currentAppIndex);
    }

    /* access modifiers changed from: private */
    public void setLayoutForAppItem(int index) {
        this.currentAppTextView.setText(this.appItems.get(index).getName());
        this.imageViewIconHighlight.setVisibility(0);
        Palette.from(((BitmapDrawable) this.appItems.get(index).getIcon()).getBitmap()).generate(this.appItemPaletteListener);
    }

    private final class AppItemPaletteListener implements Palette.PaletteAsyncListener {
        private static final int BACKGROUND_TRANSITION_DURATION = 200;
        private Drawable lastBackgroundDrawable;

        /* synthetic */ AppItemPaletteListener(AppDrawerViewHelper this$02, AppItemPaletteListener appItemPaletteListener) {
            this();
        }

        private AppItemPaletteListener() {
        }

        public void onGenerated(@NonNull Palette palette) {
            if (AppDrawerViewHelper.this.context != null) {
                GradientDrawable gradientDrawable = ImageUtils.getGradientDrawableFromPalette(AppDrawerViewHelper.this.context, palette);
                if (this.lastBackgroundDrawable != null) {
                    TransitionDrawable transition = new TransitionDrawable(new Drawable[]{this.lastBackgroundDrawable, gradientDrawable});
                    AppDrawerViewHelper.this.appsRecyclerView.setBackground(transition);
                    transition.startTransition(200);
                } else {
                    AppDrawerViewHelper.this.appsRecyclerView.setBackground(gradientDrawable);
                }
                this.lastBackgroundDrawable = gradientDrawable;
            }
        }
    }

    private final class AppsRecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {
        private static final float ALPHA_OPAQUE = 1.0f;
        private static final float ALPHA_TRANSPARENT = 0.0f;
        private static final int DISAPPEAR_TRANSITION_DURATION = 50;
        private static final int FAST_APPEAR_TRANSITION_DURATION = 100;
        private static final int SLOW_APPEAR_TRANSITION_DURATION = 500;
        private int previousScrollingState;

        /* synthetic */ AppsRecyclerViewOnScrollListener(AppDrawerViewHelper this$02, AppsRecyclerViewOnScrollListener appsRecyclerViewOnScrollListener) {
            this();
        }

        private AppsRecyclerViewOnScrollListener() {
        }

        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (!AppDrawerViewHelper.this.appItems.isEmpty()) {
                switch (newState) {
                    case 0:
                        if (this.previousScrollingState == 1) {
                            AppDrawerViewHelper.this.setLayoutForAppItem(AppDrawerViewHelper.this.currentAppIndex);
                            AppDrawerViewHelper.this.imageViewIconHighlight.animate().alpha(ALPHA_OPAQUE).setDuration(500);
                            AppDrawerViewHelper.this.currentAppTextView.animate().alpha(ALPHA_OPAQUE).setDuration(100);
                            this.previousScrollingState = newState;
                            return;
                        }
                        return;
                    case 1:
                        AppDrawerViewHelper.this.imageViewIconHighlight.animate().alpha(0.0f).setDuration(50);
                        AppDrawerViewHelper.this.currentAppTextView.animate().alpha(0.0f).setDuration(50);
                        this.previousScrollingState = newState;
                        return;
                    default:
                        return;
                }
            }
        }

        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            View foundView = AppDrawerViewHelper.this.snapHelper.findSnapView(AppDrawerViewHelper.this.layoutManager);
            if (foundView != null) {
                View unused = AppDrawerViewHelper.this.currentAppView = foundView;
                int unused2 = AppDrawerViewHelper.this.currentAppIndex = AppDrawerViewHelper.this.layoutManager.getPosition(foundView);
            }
        }
    }
}
