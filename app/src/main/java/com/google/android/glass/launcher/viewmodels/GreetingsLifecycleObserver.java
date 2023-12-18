package com.google.android.glass.launcher.viewmodels;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import com.google.android.glass.utils.IntentUtils;
import java.util.Calendar;

public class GreetingsLifecycleObserver implements LifecycleObserver {
    private static final int AFTERNOON_END_HOUR = 16;
    private static final int AFTERNOON_START_HOUR = 12;
    private static final int MORNING_END_HOUR = 12;
    private static final int MORNING_START_HOUR = 5;
    private Calendar calendar;
    private final Context context;
    private final GreetingsViewModel greetingsViewModel;
    private final BroadcastReceiver timeBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            GreetingsLifecycleObserver.this.refreshCalendar(Calendar.getInstance());
            GreetingsLifecycleObserver.this.updateGreetingsMessage();
        }
    };

    public GreetingsLifecycleObserver(Context context2, GreetingsViewModel greetingsViewModel2, Calendar calendar2) {
        this.context = context2;
        this.greetingsViewModel = greetingsViewModel2;
        this.calendar = calendar2;
    }

    public void refreshCalendar(Calendar calendar2) {
        this.calendar = calendar2;
    }

    /* access modifiers changed from: protected */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        this.context.registerReceiver(this.timeBroadcastReceiver, IntentUtils.getTimeIntentFilter());
    }

    /* access modifiers changed from: protected */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        this.context.unregisterReceiver(this.timeBroadcastReceiver);
    }

    /* access modifiers changed from: protected */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void updateGreetingsMessage() {
        TimeOfDay timeOfDay;
        int hour = this.calendar.get(11);
        if (hour >= 5 && hour < 12) {
            timeOfDay = TimeOfDay.MORNING;
        } else if (hour < 12 || hour >= 16) {
            timeOfDay = TimeOfDay.EVENING;
        } else {
            timeOfDay = TimeOfDay.AFTERNOON;
        }
        this.greetingsViewModel.setGreetingsMessage(this.context.getString(timeOfDay.message));
    }

    enum TimeOfDay {
        MORNING(2131492904),
        AFTERNOON(2131492902),
        EVENING(2131492903);
        
        /* access modifiers changed from: private */
        @StringRes
        public int message;

        private TimeOfDay(int message2) {
            this.message = message2;
        }
    }
}
