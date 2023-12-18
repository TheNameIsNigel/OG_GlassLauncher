package com.google.android.glass.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

public class IntentUtils {
    public static final String GLASS_APPLICATION_CATEGORY = "com.google.android.glass.category.DIRECTORY";
    public static final String GLASS_BETA_CATEGORY = "com.google.android.glass.category.BETA";
    private static final String PACKAGE_DATA_SCHEME = "package";

    public static IntentFilter getTimeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        return intentFilter;
    }

    public static IntentFilter getBatteryIntentFilter() {
        return new IntentFilter("android.intent.action.BATTERY_CHANGED");
    }

    public static IntentFilter getWifiIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        return intentFilter;
    }

    public static IntentFilter getAppsIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme(PACKAGE_DATA_SCHEME);
        return intentFilter;
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent != null && packageManager.queryIntentActivities(intent, 65536).size() > 0) {
            return true;
        }
        return false;
    }

    public static Intent getImageCaptureIntent() {
        return new Intent("android.media.action.IMAGE_CAPTURE");
    }

    public static Intent getVideoCaptureIntent() {
        return new Intent("android.media.action.VIDEO_CAPTURE");
    }
}
