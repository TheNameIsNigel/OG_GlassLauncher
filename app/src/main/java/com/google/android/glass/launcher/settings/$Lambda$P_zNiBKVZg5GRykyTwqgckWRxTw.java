package com.google.android.glass.launcher.settings;

import android.arch.lifecycle.Observer;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

/* renamed from: com.google.android.glass.launcher.settings.-$Lambda$P_zNiBKVZg5GRykyTwqgckWRxTw  reason: invalid class name */
final /* synthetic */ class $Lambda$P_zNiBKVZg5GRykyTwqgckWRxTw implements Observer {
    private final /* synthetic */ byte $id;

    /* renamed from: -$f0  reason: not valid java name */
    private final /* synthetic */ Object f11$f0;

    private final /* synthetic */ void $m$0(Object arg0) {
        ((TextClock) this.f11$f0).setFormat12Hour((String) arg0);
    }

    private final /* synthetic */ void $m$1(Object arg0) {
        ((TextClock) this.f11$f0).setFormat24Hour((String) arg0);
    }

    private final /* synthetic */ void $m$2(Object arg0) {
        ((ImageView) this.f11$f0).setImageLevel(((Integer) arg0).intValue());
    }

    private final /* synthetic */ void $m$3(Object arg0) {
        ((TextView) this.f11$f0).setText((String) arg0);
    }

    public /* synthetic */ $Lambda$P_zNiBKVZg5GRykyTwqgckWRxTw(byte b, Object obj) {
        this.$id = b;
        this.f11$f0 = obj;
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
            case 3:
                $m$3(obj);
                return;
            default:
                throw new AssertionError();
        }
    }
}
