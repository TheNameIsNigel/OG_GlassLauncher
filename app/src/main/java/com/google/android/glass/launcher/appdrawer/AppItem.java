package com.google.android.glass.launcher.appdrawer;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import java.util.Objects;

public class AppItem {
    private final String activityName;
    private final Drawable icon;
    private final String name;
    private final String packageName;

    public AppItem(String name2, String packageName2, String activityName2, Drawable icon2) {
        this.name = name2;
        this.packageName = packageName2;
        this.activityName = activityName2;
        this.icon = icon2;
    }

    public String getName() {
        return this.name;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getActivityName() {
        return this.activityName;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AppItem)) {
            return false;
        }
        AppItem that = (AppItem) other;
        if (!Objects.equals(this.name, that.name) || !Objects.equals(this.packageName, that.packageName) || !Objects.equals(this.activityName, that.activityName)) {
            return false;
        }
        return Objects.equals(this.icon, that.icon);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.name, this.packageName, this.activityName, this.icon});
    }

    @NonNull
    public String toString() {
        return this.name + " " + this.packageName + " " + this.activityName + " " + this.icon;
    }
}
