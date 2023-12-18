package android.arch.lifecycle;

import android.arch.lifecycle.Lifecycle;

public interface GenericLifecycleObserver extends LifecycleObserver {
    Object getReceiver();

    void onStateChanged(LifecycleOwner lifecycleOwner, Lifecycle.Event event);
}
