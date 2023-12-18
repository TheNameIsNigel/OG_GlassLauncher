package com.google.android.glass.launcher.indicators;

import android.arch.lifecycle.Observer;
import android.widget.ImageView;

/* renamed from: com.google.android.glass.launcher.indicators.-$Lambda$sbmy5At8QXwuWXn1NU2w_ZV6p-s  reason: invalid class name */
final /* synthetic */ class $Lambda$sbmy5At8QXwuWXn1NU2w_ZV6ps implements Observer {

    /* renamed from: -$f0  reason: not valid java name */
    private final /* synthetic */ Object f7$f0;

    private final /* synthetic */ void $m$0(Object arg0) {
        ((ImageView) this.f7$f0).setImageLevel(((Integer) arg0).intValue());
    }

    public /* synthetic */ $Lambda$sbmy5At8QXwuWXn1NU2w_ZV6ps(Object obj) {
        this.f7$f0 = obj;
    }

    public final void onChanged(Object obj) {
        $m$0(obj);
    }
}
