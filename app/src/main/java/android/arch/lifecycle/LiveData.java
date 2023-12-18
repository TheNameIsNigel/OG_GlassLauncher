package android.arch.lifecycle;

import android.arch.core.executor.AppToolkitTaskExecutor;
import android.arch.core.internal.SafeIterableMap;
import android.arch.lifecycle.Lifecycle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

public abstract class LiveData<T> {
    private static final LifecycleOwner ALWAYS_ON = new LifecycleOwner() {
        private LifecycleRegistry mRegistry = init();

        private LifecycleRegistry init() {
            LifecycleRegistry registry = new LifecycleRegistry(this);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_START);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
            return registry;
        }

        public Lifecycle getLifecycle() {
            return this.mRegistry;
        }
    };
    /* access modifiers changed from: private */
    public static final Object NOT_SET = new Object();
    static final int START_VERSION = -1;
    /* access modifiers changed from: private */
    public int mActiveCount = 0;
    private volatile Object mData = NOT_SET;
    /* access modifiers changed from: private */
    public final Object mDataLock = new Object();
    private boolean mDispatchInvalidated;
    private boolean mDispatchingValue;
    private SafeIterableMap<Observer<T>, LiveData<T>.LifecycleBoundObserver> mObservers = new SafeIterableMap<>();
    /* access modifiers changed from: private */
    public volatile Object mPendingData = NOT_SET;
    private final Runnable mPostValueRunnable = new Runnable() {
        public void run() {
            Object newValue;
            synchronized (LiveData.this.mDataLock) {
                newValue = LiveData.this.mPendingData;
                Object unused = LiveData.this.mPendingData = LiveData.NOT_SET;
            }
            LiveData.this.setValue(newValue);
        }
    };
    private int mVersion = -1;

    private void considerNotify(LiveData<T>.LifecycleBoundObserver observer) {
        if (observer.active && isActiveState(observer.owner.getLifecycle().getCurrentState()) && observer.lastVersion < this.mVersion) {
            observer.lastVersion = this.mVersion;
            observer.observer.onChanged(this.mData);
        }
    }

    /* access modifiers changed from: private */
    public void dispatchingValue(@Nullable LiveData<T>.LifecycleBoundObserver initiator) {
        if (!this.mDispatchingValue) {
            this.mDispatchingValue = true;
            do {
                this.mDispatchInvalidated = false;
                if (initiator == null) {
                    Iterator<Map.Entry<Observer<T>, LiveData<T>.LifecycleBoundObserver>> iterator = this.mObservers.iteratorWithAdditions();
                    while (iterator.hasNext()) {
                        considerNotify((LifecycleBoundObserver) iterator.next().getValue());
                        if (this.mDispatchInvalidated) {
                            break;
                        }
                    }
                } else {
                    considerNotify(initiator);
                    initiator = null;
                }
            } while (this.mDispatchInvalidated);
            this.mDispatchingValue = false;
            return;
        }
        this.mDispatchInvalidated = true;
    }

    @MainThread
    public void observe(LifecycleOwner owner, Observer<T> observer) {
        if (owner.getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) {
            LiveData<T>.LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer);
            LiveData<T>.LifecycleBoundObserver existing = this.mObservers.putIfAbsent(observer, wrapper);
            if (existing != null && existing.owner != wrapper.owner) {
                throw new IllegalArgumentException("Cannot add the same observer with different lifecycles");
            } else if (existing == null) {
                owner.getLifecycle().addObserver(wrapper);
                wrapper.activeStateChanged(isActiveState(owner.getLifecycle().getCurrentState()));
            }
        }
    }

    @MainThread
    public void observeForever(Observer<T> observer) {
        observe(ALWAYS_ON, observer);
    }

    @MainThread
    public void removeObserver(Observer<T> observer) {
        assertMainThread("removeObserver");
        LiveData<T>.LifecycleBoundObserver removed = this.mObservers.remove(observer);
        if (removed != null) {
            removed.owner.getLifecycle().removeObserver(removed);
            removed.activeStateChanged(false);
        }
    }

    @MainThread
    public void removeObservers(LifecycleOwner owner) {
        assertMainThread("removeObservers");
        Iterator<Map.Entry<Observer<T>, LiveData<T>.LifecycleBoundObserver>> it = this.mObservers.iterator();
        while (it.hasNext()) {
            Map.Entry<Observer<T>, LiveData<T>.LifecycleBoundObserver> entry = it.next();
            if (entry.getValue().owner == owner) {
                removeObserver(entry.getKey());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void postValue(T value) {
        boolean postTask = false;
        synchronized (this.mDataLock) {
            if (this.mPendingData == NOT_SET) {
                postTask = true;
            }
            this.mPendingData = value;
        }
        if (postTask) {
            AppToolkitTaskExecutor.getInstance().postToMainThread(this.mPostValueRunnable);
        }
    }

    /* access modifiers changed from: protected */
    @MainThread
    public void setValue(T value) {
        assertMainThread("setValue");
        this.mVersion++;
        this.mData = value;
        dispatchingValue((LiveData<T>.LifecycleBoundObserver) null);
    }

    @Nullable
    public T getValue() {
        Object data = this.mData;
        if (data == NOT_SET) {
            return null;
        }
        return data;
    }

    /* access modifiers changed from: package-private */
    public int getVersion() {
        return this.mVersion;
    }

    /* access modifiers changed from: protected */
    public void onActive() {
    }

    /* access modifiers changed from: protected */
    public void onInactive() {
    }

    public boolean hasObservers() {
        return this.mObservers.size() > 0;
    }

    public boolean hasActiveObservers() {
        return this.mActiveCount > 0;
    }

    class LifecycleBoundObserver implements LifecycleObserver {
        public boolean active;
        public int lastVersion = -1;
        public final Observer<T> observer;
        public final LifecycleOwner owner;

        LifecycleBoundObserver(LifecycleOwner owner2, Observer<T> observer2) {
            this.owner = owner2;
            this.observer = observer2;
        }

        /* access modifiers changed from: package-private */
        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        public void onStateChange() {
            if (this.owner.getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) {
                activeStateChanged(LiveData.isActiveState(this.owner.getLifecycle().getCurrentState()));
            } else {
                LiveData.this.removeObserver(this.observer);
            }
        }

        /* access modifiers changed from: package-private */
        public void activeStateChanged(boolean newActive) {
            int i = 1;
            boolean wasInactive = false;
            if (newActive != this.active) {
                this.active = newActive;
                if (LiveData.this.mActiveCount == 0) {
                    wasInactive = true;
                }
                LiveData liveData = LiveData.this;
                int access$300 = liveData.mActiveCount;
                if (!this.active) {
                    i = -1;
                }
                int unused = liveData.mActiveCount = i + access$300;
                if (wasInactive && this.active) {
                    LiveData.this.onActive();
                }
                if (LiveData.this.mActiveCount == 0 && !this.active) {
                    LiveData.this.onInactive();
                }
                if (this.active) {
                    LiveData.this.dispatchingValue(this);
                }
            }
        }
    }

    static boolean isActiveState(Lifecycle.State state) {
        return state.isAtLeast(Lifecycle.State.STARTED);
    }

    private void assertMainThread(String methodName) {
        if (!AppToolkitTaskExecutor.getInstance().isMainThread()) {
            throw new IllegalStateException("Cannot invoke " + methodName + " on a background" + " thread");
        }
    }
}
