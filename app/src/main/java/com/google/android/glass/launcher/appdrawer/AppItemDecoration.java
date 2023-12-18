package com.google.android.glass.launcher.appdrawer;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class AppItemDecoration extends RecyclerView.ItemDecoration {
    private final Resources resources;

    AppItemDecoration(Resources resources2) {
        this.resources = resources2;
    }

    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildViewHolder(view).getAdapterPosition();
        if (position == 0 || position == state.getItemCount() - 1) {
            int elementWidth = (int) this.resources.getDimension(2131099727);
            int padding = ((Resources.getSystem().getDisplayMetrics().widthPixels / 2) - (elementWidth / 2)) - ((int) this.resources.getDimension(2131099726));
            if (position == 0) {
                outRect.left = padding;
            } else {
                outRect.right = padding;
            }
        }
    }
}
