package com.google.android.glass.launcher.appdrawer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.List;

public class AppDrawerAdapter extends RecyclerView.Adapter<AppDrawerViewHolder> {
    private final List<AppItem> apps;

    AppDrawerAdapter(List<AppItem> apps2) {
        this.apps = apps2;
    }

    @NonNull
    public AppDrawerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppDrawerViewHolder(LayoutInflater.from(parent.getContext()).inflate(2131361819, parent, false));
    }

    public void onBindViewHolder(@NonNull AppDrawerViewHolder holder, int position) {
        holder.bind(this.apps.get(position));
    }

    public int getItemCount() {
        return this.apps.size();
    }

    static class AppDrawerViewHolder extends RecyclerView.ViewHolder {
        private final View view;

        AppDrawerViewHolder(@NonNull View view2) {
            super(view2);
            this.view = view2;
        }

        /* access modifiers changed from: package-private */
        public void bind(AppItem appItem) {
            ImageView imageView = (ImageView) this.view.findViewById(2131230815);
            imageView.setImageDrawable(appItem.getIcon());
            imageView.setContentDescription(appItem.getName());
        }
    }
}
