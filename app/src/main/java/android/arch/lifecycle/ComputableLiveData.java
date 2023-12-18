package android.arch.lifecycle;

import android.arch.core.executor.AppToolkitTaskExecutor;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import java.util.concurrent.atomic.AtomicBoolean;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public abstract class ComputableLiveData<T> {
    /* access modifiers changed from: private */
    public AtomicBoolean mComputing = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public AtomicBoolean mInvalid = new AtomicBoolean(true);
    @VisibleForTesting
    final Runnable mInvalidationRunnable = new Runnable() {
        @MainThread
        public void run() {
            boolean isActive = ComputableLiveData.this.mLiveData.hasActiveObservers();
            if (ComputableLiveData.this.mInvalid.compareAndSet(false, true) && isActive) {
                AppToolkitTaskExecutor.getInstance().executeOnDiskIO(ComputableLiveData.this.mRefreshRunnable);
            }
        }
    };
    /* access modifiers changed from: private */
    public final LiveData<T> mLiveData = new LiveData<T>() {
        /* access modifiers changed from: protected */
        public void onActive() {
            AppToolkitTaskExecutor.getInstance().executeOnDiskIO(ComputableLiveData.this.mRefreshRunnable);
        }
    };
    @VisibleForTesting
    final Runnable mRefreshRunnable = new Runnable() {
        /* JADX WARNING: Removed duplicated region for block: B:4:0x0012  */
        @android.support.annotation.WorkerThread
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r8 = this;
                r7 = 1
                r6 = 0
            L_0x0002:
                r1 = 0
                android.arch.lifecycle.ComputableLiveData r3 = android.arch.lifecycle.ComputableLiveData.this
                java.util.concurrent.atomic.AtomicBoolean r3 = r3.mComputing
                boolean r3 = r3.compareAndSet(r6, r7)
                if (r3 != 0) goto L_0x0012
            L_0x000f:
                if (r1 != 0) goto L_0x004a
            L_0x0011:
                return
            L_0x0012:
                r2 = 0
            L_0x0013:
                android.arch.lifecycle.ComputableLiveData r3 = android.arch.lifecycle.ComputableLiveData.this     // Catch:{ all -> 0x003f }
                java.util.concurrent.atomic.AtomicBoolean r3 = r3.mInvalid     // Catch:{ all -> 0x003f }
                r4 = 1
                r5 = 0
                boolean r3 = r3.compareAndSet(r4, r5)     // Catch:{ all -> 0x003f }
                if (r3 != 0) goto L_0x002d
                if (r1 != 0) goto L_0x0035
            L_0x0023:
                android.arch.lifecycle.ComputableLiveData r3 = android.arch.lifecycle.ComputableLiveData.this
                java.util.concurrent.atomic.AtomicBoolean r3 = r3.mComputing
                r3.set(r6)
                goto L_0x000f
            L_0x002d:
                r1 = 1
                android.arch.lifecycle.ComputableLiveData r3 = android.arch.lifecycle.ComputableLiveData.this     // Catch:{ all -> 0x003f }
                java.lang.Object r2 = r3.compute()     // Catch:{ all -> 0x003f }
                goto L_0x0013
            L_0x0035:
                android.arch.lifecycle.ComputableLiveData r3 = android.arch.lifecycle.ComputableLiveData.this     // Catch:{ all -> 0x003f }
                android.arch.lifecycle.LiveData r3 = r3.mLiveData     // Catch:{ all -> 0x003f }
                r3.postValue(r2)     // Catch:{ all -> 0x003f }
                goto L_0x0023
            L_0x003f:
                r0 = move-exception
                android.arch.lifecycle.ComputableLiveData r3 = android.arch.lifecycle.ComputableLiveData.this
                java.util.concurrent.atomic.AtomicBoolean r3 = r3.mComputing
                r3.set(r6)
                throw r0
            L_0x004a:
                android.arch.lifecycle.ComputableLiveData r3 = android.arch.lifecycle.ComputableLiveData.this
                java.util.concurrent.atomic.AtomicBoolean r3 = r3.mInvalid
                boolean r3 = r3.get()
                if (r3 == 0) goto L_0x0011
                goto L_0x0002
            */
            throw new UnsupportedOperationException("Method not decompiled: android.arch.lifecycle.ComputableLiveData.AnonymousClass2.run():void");
        }
    };

    /* access modifiers changed from: protected */
    @WorkerThread
    public abstract T compute();

    @NonNull
    public LiveData<T> getLiveData() {
        return this.mLiveData;
    }

    public void invalidate() {
        AppToolkitTaskExecutor.getInstance().executeOnMainThread(this.mInvalidationRunnable);
    }
}
