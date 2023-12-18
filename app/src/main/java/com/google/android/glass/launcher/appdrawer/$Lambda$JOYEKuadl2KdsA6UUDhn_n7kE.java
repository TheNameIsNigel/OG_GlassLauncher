package com.google.android.glass.launcher.appdrawer;

import android.arch.lifecycle.Observer;
import android.widget.TextClock;
import java.util.List;

/* renamed from: com.google.android.glass.launcher.appdrawer.-$Lambda$J-OYEKuadl2Kds-A6UUDhn_n7kE  reason: invalid class name */
final /* synthetic */ class $Lambda$JOYEKuadl2KdsA6UUDhn_n7kE implements Observer {
    private final /* synthetic */ byte $id;

    /* renamed from: -$f0  reason: not valid java name */
    private final /* synthetic */ Object f5$f0;

    private final /* synthetic */ void $m$0(Object arg0) {
        ((TextClock) this.f5$f0).setFormat12Hour((String) arg0);
    }

    private final /* synthetic */ void $m$1(Object arg0) {
        ((TextClock) this.f5$f0).setFormat24Hour((String) arg0);
    }

    private final /* synthetic */ void $m$2(Object arg0) {
        ((AppDrawerViewHelper) this.f5$f0).setApps((List) arg0);
    }

    public /* synthetic */ $Lambda$JOYEKuadl2KdsA6UUDhn_n7kE(byte b, Object obj) {
        this.$id = b;
        this.f5$f0 = obj;
    }

    public final void onChanged(Object obj) {
        switch (this.$id) {
            case 0:
                $m$0(obj);
                return;
            case 1:
                $m$1(obj);
                return;
            case 2:
                $m$2(obj);
                return;
            default:
                throw new AssertionError();
        }
    }
}
