package com.google.android.glass.launcher.keyevent;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.util.Log;
import com.google.android.glass.launcher.keyevent.KeyEventHandler;
import com.google.android.glass.utils.IntentUtils;

public class KeyEventListener implements KeyEventHandler.OnKeyEventListener {
    private static final String TAG = KeyEventListener.class.getSimpleName();
    private Context context;

    public KeyEventListener(Context context2) {
        this.context = context2;
    }

    public void onKeycodeCameraKeyUp() {
        try {
            this.context.startActivity(IntentUtils.getImageCaptureIntent());
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Can't find any Activity to handle the 'android.media.action.IMAGE_CAPTURE' intent. Ignoring.");
        }
    }

    public void onKeycodeCameraKeyLongPress() {
        try {
            this.context.startActivity(IntentUtils.getVideoCaptureIntent());
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Can't find any Activity to handle the 'android.media.action.VIDEO_CAPTURE' intent. Ignoring.");
        }
    }
}
