package com.google.android.glass.ui;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

public class GlassGestureDetector {
    private static final int FIRST_FINGER_POINTER_INDEX = 0;
    private static final int SECOND_FINGER_POINTER_INDEX = 1;
    static final int SWIPE_DISTANCE_THRESHOLD_PX = 100;
    static final int SWIPE_VELOCITY_THRESHOLD_PX = 100;
    private static final double TAN_ANGLE_DEGREES = Math.tan(Math.toRadians(60.0d));
    private static final int TAP_AND_HOLD_THRESHOLD_MS = ViewConfiguration.getLongPressTimeout();
    private static final int VELOCITY_UNIT = 1000;
    private MotionEvent currentDownEvent;
    private float firstFingerDistanceX;
    private float firstFingerDistanceY;
    private float firstFingerDownX;
    private float firstFingerDownY;
    private float firstFingerLastFocusX;
    private float firstFingerLastFocusY;
    private float firstFingerVelocityX;
    private float firstFingerVelocityY;
    private boolean isActionDownPerformed = false;
    private boolean isInTapRegion;
    /* access modifiers changed from: private */
    public boolean isTapAndHoldPerformed = false;
    private boolean isTwoFingerGesture = false;
    /* access modifiers changed from: private */
    public OnGestureListener onGestureListener;
    private float secondFingerDistanceX;
    private float secondFingerDistanceY;
    private float secondFingerDownX;
    private float secondFingerDownY;
    final CountDownTimer tapAndHoldCountDownTimer = new CountDownTimer((long) TAP_AND_HOLD_THRESHOLD_MS, (long) TAP_AND_HOLD_THRESHOLD_MS) {
        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            boolean unused = GlassGestureDetector.this.isTapAndHoldPerformed = true;
            GlassGestureDetector.this.onGestureListener.onGesture(Gesture.TAP_AND_HOLD);
        }
    };
    private final int touchSlopSquare;
    private VelocityTracker velocityTracker;

    public enum Gesture {
        TAP,
        TAP_AND_HOLD,
        TWO_FINGER_TAP,
        SWIPE_FORWARD,
        TWO_FINGER_SWIPE_FORWARD,
        SWIPE_BACKWARD,
        TWO_FINGER_SWIPE_BACKWARD,
        SWIPE_UP,
        TWO_FINGER_SWIPE_UP,
        SWIPE_DOWN,
        TWO_FINGER_SWIPE_DOWN
    }

    public interface OnGestureListener {
        boolean onGesture(Gesture gesture);

        boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        void onTouchEnded() {
        }
    }

    public GlassGestureDetector(Context context, OnGestureListener onGestureListener2) {
        int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.touchSlopSquare = touchSlop * touchSlop;
        this.onGestureListener = onGestureListener2;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        }
        this.velocityTracker.addMovement(motionEvent);
        switch (motionEvent.getAction() & 255) {
            case 0:
                this.tapAndHoldCountDownTimer.start();
                float x = motionEvent.getX();
                this.firstFingerLastFocusX = x;
                this.firstFingerDownX = x;
                float y = motionEvent.getY();
                this.firstFingerLastFocusY = y;
                this.firstFingerDownY = y;
                this.isActionDownPerformed = true;
                this.isInTapRegion = true;
                if (this.currentDownEvent != null) {
                    this.currentDownEvent.recycle();
                }
                this.currentDownEvent = MotionEvent.obtain(motionEvent);
                return false;
            case 1:
                this.tapAndHoldCountDownTimer.cancel();
                this.velocityTracker.computeCurrentVelocity(1000);
                this.firstFingerVelocityX = this.velocityTracker.getXVelocity(motionEvent.getPointerId(motionEvent.getActionIndex()));
                this.firstFingerVelocityY = this.velocityTracker.getYVelocity(motionEvent.getPointerId(motionEvent.getActionIndex()));
                boolean handled = detectGesture();
                onTouchEnded();
                return handled;
            case 2:
                float firstFingerFocusX = motionEvent.getX(0);
                float firstFingerFocusY = motionEvent.getY(0);
                float scrollX = this.firstFingerLastFocusX - firstFingerFocusX;
                float scrollY = this.firstFingerLastFocusY - firstFingerFocusY;
                this.firstFingerDistanceX = firstFingerFocusX - this.firstFingerDownX;
                this.firstFingerDistanceY = firstFingerFocusY - this.firstFingerDownY;
                if (motionEvent.getPointerCount() > 1) {
                    this.secondFingerDistanceX = motionEvent.getX(1) - this.secondFingerDownX;
                    this.secondFingerDistanceY = motionEvent.getY(1) - this.secondFingerDownY;
                }
                if (this.isInTapRegion) {
                    float distance = (this.firstFingerDistanceX * this.firstFingerDistanceX) + (this.firstFingerDistanceY * this.firstFingerDistanceY);
                    float distanceSecondFinger = 0.0f;
                    if (motionEvent.getPointerCount() > 1) {
                        distanceSecondFinger = (this.secondFingerDistanceX * this.secondFingerDistanceX) + (this.secondFingerDistanceY * this.secondFingerDistanceY);
                    }
                    if (distance > ((float) this.touchSlopSquare) || distanceSecondFinger > ((float) this.touchSlopSquare)) {
                        this.tapAndHoldCountDownTimer.cancel();
                        this.isInTapRegion = false;
                    }
                }
                if (Math.abs(scrollX) < 1.0f && Math.abs(scrollY) < 1.0f) {
                    return false;
                }
                boolean handled2 = this.onGestureListener.onScroll(this.currentDownEvent, motionEvent, scrollX, scrollY);
                this.firstFingerLastFocusX = firstFingerFocusX;
                this.firstFingerLastFocusY = firstFingerFocusY;
                return handled2;
            case 3:
                this.tapAndHoldCountDownTimer.cancel();
                this.velocityTracker.recycle();
                this.velocityTracker = null;
                this.isInTapRegion = false;
                this.isTapAndHoldPerformed = false;
                return false;
            case 5:
                this.tapAndHoldCountDownTimer.cancel();
                this.isTwoFingerGesture = true;
                this.secondFingerDownX = motionEvent.getX(motionEvent.getActionIndex());
                this.secondFingerDownY = motionEvent.getY(motionEvent.getActionIndex());
                return false;
            default:
                return false;
        }
    }

    private boolean detectGesture() {
        double tan;
        double tanSecondFinger;
        if (!this.isActionDownPerformed || this.isTapAndHoldPerformed) {
            return false;
        }
        if (this.firstFingerDistanceX != 0.0f) {
            tan = (double) Math.abs(this.firstFingerDistanceY / this.firstFingerDistanceX);
        } else {
            tan = Double.MAX_VALUE;
        }
        if (!this.isTwoFingerGesture) {
            return detectOneFingerGesture(tan);
        }
        if (this.secondFingerDistanceX != 0.0f) {
            tanSecondFinger = (double) Math.abs(this.secondFingerDistanceY / this.secondFingerDistanceX);
        } else {
            tanSecondFinger = Double.MAX_VALUE;
        }
        return detectTwoFingerGesture(tan, tanSecondFinger);
    }

    private boolean detectOneFingerGesture(double tan) {
        if (tan > TAN_ANGLE_DEGREES) {
            if (Math.abs(this.firstFingerDistanceY) < 100.0f || Math.abs(this.firstFingerVelocityY) < 100.0f) {
                if (this.isInTapRegion) {
                    return this.onGestureListener.onGesture(Gesture.TAP);
                }
                return false;
            } else if (this.firstFingerDistanceY < 0.0f) {
                return this.onGestureListener.onGesture(Gesture.SWIPE_UP);
            } else {
                if (this.firstFingerDistanceY > 0.0f) {
                    return this.onGestureListener.onGesture(Gesture.SWIPE_DOWN);
                }
                return false;
            }
        } else if (Math.abs(this.firstFingerDistanceX) < 100.0f || Math.abs(this.firstFingerVelocityX) < 100.0f) {
            if (this.isInTapRegion) {
                return this.onGestureListener.onGesture(Gesture.TAP);
            }
            return false;
        } else if (this.firstFingerDistanceX < 0.0f) {
            return this.onGestureListener.onGesture(Gesture.SWIPE_FORWARD);
        } else {
            if (this.firstFingerDistanceX > 0.0f) {
                return this.onGestureListener.onGesture(Gesture.SWIPE_BACKWARD);
            }
            return false;
        }
    }

    private boolean detectTwoFingerGesture(double tan, double tanSecondFinger) {
        if (tan <= TAN_ANGLE_DEGREES || tanSecondFinger <= TAN_ANGLE_DEGREES) {
            if (Math.abs(this.firstFingerDistanceX) < 100.0f || Math.abs(this.firstFingerVelocityX) < 100.0f) {
                if (this.isInTapRegion) {
                    return this.onGestureListener.onGesture(Gesture.TWO_FINGER_TAP);
                }
                return false;
            } else if (this.firstFingerDistanceX < 0.0f && this.secondFingerDistanceX < 0.0f) {
                return this.onGestureListener.onGesture(Gesture.TWO_FINGER_SWIPE_FORWARD);
            } else {
                if (this.firstFingerDistanceX <= 0.0f || this.secondFingerDistanceX <= 0.0f) {
                    return false;
                }
                return this.onGestureListener.onGesture(Gesture.TWO_FINGER_SWIPE_BACKWARD);
            }
        } else if (Math.abs(this.firstFingerDistanceY) < 100.0f || Math.abs(this.firstFingerVelocityY) < 100.0f) {
            if (this.isInTapRegion) {
                return this.onGestureListener.onGesture(Gesture.TWO_FINGER_TAP);
            }
            return false;
        } else if (this.firstFingerDistanceY < 0.0f && this.secondFingerDistanceY < 0.0f) {
            return this.onGestureListener.onGesture(Gesture.TWO_FINGER_SWIPE_UP);
        } else {
            if (this.firstFingerDistanceY <= 0.0f || this.secondFingerDistanceY <= 0.0f) {
                return false;
            }
            return this.onGestureListener.onGesture(Gesture.TWO_FINGER_SWIPE_DOWN);
        }
    }

    private void onTouchEnded() {
        this.isTwoFingerGesture = false;
        if (this.velocityTracker != null) {
            this.velocityTracker.recycle();
            this.velocityTracker = null;
        }
        this.isActionDownPerformed = false;
        this.isTapAndHoldPerformed = false;
        this.onGestureListener.onTouchEnded();
    }
}
