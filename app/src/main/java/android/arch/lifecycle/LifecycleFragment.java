package android.arch.lifecycle;

import android.support.v4.app.Fragment;

public class LifecycleFragment extends Fragment implements LifecycleRegistryOwner {
    LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

    public LifecycleRegistry getLifecycle() {
        return this.mLifecycleRegistry;
    }
}
