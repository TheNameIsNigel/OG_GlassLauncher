package com.google.android.glass.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class GlassCardFragment extends GlassBaseFragment {
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.glass_card_view, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = (TextView) view.findViewById(R.id.glass_card_title);
        if (getTitle() != null) {
            title.setText(getTitle());
        } else if (getTitleRes() != -1) {
            title.setText(getTitleRes());
        } else {
            title.setVisibility(8);
        }
        TextView subtitle = (TextView) view.findViewById(R.id.glass_card_subtitle);
        if (getSubtitle() != null) {
            subtitle.setText(getSubtitle());
        } else if (getSubtitleRes() != -1) {
            subtitle.setText(getSubtitleRes());
        } else {
            subtitle.setVisibility(8);
        }
        TextView action = (TextView) view.findViewById(R.id.glass_card_action);
        if (getAction() != null) {
            action.setText(getAction());
        } else if (getActionRes() != -1) {
            action.setText(getActionRes());
        } else {
            action.setVisibility(8);
        }
        ImageView icon = (ImageView) view.findViewById(R.id.glass_card_icon);
        if (getIcon() != null) {
            icon.setImageDrawable(getIcon());
        } else if (getIconRes() != -1) {
            icon.setImageResource(getIconRes());
        } else {
            icon.setVisibility(8);
            title.setTextAppearance(R.style.TextStyle_Title);
        }
    }

    public String getTitle() {
        return null;
    }

    @StringRes
    public int getTitleRes() {
        return -1;
    }

    public String getSubtitle() {
        return null;
    }

    @StringRes
    public int getSubtitleRes() {
        return -1;
    }

    public String getAction() {
        return null;
    }

    @StringRes
    public int getActionRes() {
        return -1;
    }

    public Drawable getIcon() {
        return null;
    }

    @DrawableRes
    public int getIconRes() {
        return -1;
    }
}
