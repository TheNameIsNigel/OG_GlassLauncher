package com.google.android.glass.launcher.keyevent;

public class KeyEventHandler {
    private boolean isLongPressPerformed = false;
    private final OnKeyEventListener onKeyEventListener;

    interface OnKeyEventListener {
        void onKeycodeCameraKeyLongPress();

        void onKeycodeCameraKeyUp();
    }

    public KeyEventHandler(OnKeyEventListener onKeyEventListener2) {
        this.onKeyEventListener = onKeyEventListener2;
    }

    public boolean onKeyUp(int keyCode) {
        if (this.isLongPressPerformed) {
            this.isLongPressPerformed = false;
            return false;
        }
        switch (keyCode) {
            case 27:
                this.onKeyEventListener.onKeycodeCameraKeyUp();
                return true;
            default:
                return false;
        }
    }

    public boolean onKeyLongPress(int keyCode) {
        switch (keyCode) {
            case 27:
                this.onKeyEventListener.onKeycodeCameraKeyLongPress();
                this.isLongPressPerformed = true;
                return true;
            default:
                return false;
        }
    }
}
