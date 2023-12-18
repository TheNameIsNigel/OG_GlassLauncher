package android.arch.lifecycle;

import android.support.v4.app.FragmentActivity;

public class LifecycleActivity extends FragmentActivity implements LifecycleRegistryOwner {
    private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);

    public LifecycleRegistry getLifecycle() {
        return this.mRegistry;
    }
}
