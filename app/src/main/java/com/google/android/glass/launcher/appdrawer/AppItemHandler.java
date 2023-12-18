package com.google.android.glass.launcher.appdrawer;

import android.content.ComponentName;
import android.content.Intent;
import android.view.View;

class AppItemHandler {
    AppItemHandler() {
    }

    static void onItemClick(View view, AppItem appItem) {
        if (view != null) {
            view.getContext().startActivity(new Intent().setComponent(new ComponentName(appItem.getPackageName(), appItem.getActivityName())));
        }
    }
}
