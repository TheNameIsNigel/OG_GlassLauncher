package com.google.android.glass.launcher.viewmodels;

import com.google.android.glass.launcher.appdrawer.AppItem;
import java.util.Comparator;

/* renamed from: com.google.android.glass.launcher.viewmodels.-$Lambda$TGekWQ_iNqBpgLcmA3Ty8RAo3HU  reason: invalid class name */
final /* synthetic */ class $Lambda$TGekWQ_iNqBpgLcmA3Ty8RAo3HU implements Comparator {
    public static final /* synthetic */ $Lambda$TGekWQ_iNqBpgLcmA3Ty8RAo3HU $INST$0 = new $Lambda$TGekWQ_iNqBpgLcmA3Ty8RAo3HU();

    private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
        return ((AppItem) arg0).getName().compareTo(((AppItem) arg1).getName());
    }

    private /* synthetic */ $Lambda$TGekWQ_iNqBpgLcmA3Ty8RAo3HU() {
    }

    public final int compare(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}
