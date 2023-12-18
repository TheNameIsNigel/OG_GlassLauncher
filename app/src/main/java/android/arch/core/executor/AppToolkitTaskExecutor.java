package android.arch.core.executor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import java.util.concurrent.Executor;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class AppToolkitTaskExecutor extends TaskExecutor {
    @NonNull
    private static final Executor sIOThreadExecutor = new Executor() {
        public void execute(Runnable command) {
            AppToolkitTaskExecutor.getInstance().executeOnDiskIO(command);
        }
    };
    private static volatile AppToolkitTaskExecutor sInstance;
    @NonNull
    private static final Executor sMainThreadExecutor = new Executor() {
        public void execute(Runnable command) {
            AppToolkitTaskExecutor.getInstance().postToMainThread(command);
        }
    };
    @NonNull
    private TaskExecutor mDefaultTaskExecutor = new DefaultTaskExecutor();
    @NonNull
    private TaskExecutor mDelegate = this.mDefaultTaskExecutor;

    private AppToolkitTaskExecutor() {
    }

    public static AppToolkitTaskExecutor getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        Object obj = AppToolkitTaskExecutor.class;
        synchronized (AppToolkitTaskExecutor.class) {
            if (sInstance == null) {
                sInstance = new AppToolkitTaskExecutor();
            }
            return sInstance;
        }
    }

    public void setDelegate(@Nullable TaskExecutor taskExecutor) {
        if (taskExecutor == null) {
            taskExecutor = this.mDefaultTaskExecutor;
        }
        this.mDelegate = taskExecutor;
    }

    public void executeOnDiskIO(Runnable runnable) {
        this.mDelegate.executeOnDiskIO(runnable);
    }

    public void postToMainThread(Runnable runnable) {
        this.mDelegate.postToMainThread(runnable);
    }

    @NonNull
    public static Executor getMainThreadExecutor() {
        return sMainThreadExecutor;
    }

    @NonNull
    public static Executor getIOThreadExecutor() {
        return sIOThreadExecutor;
    }

    public boolean isMainThread() {
        return this.mDelegate.isMainThread();
    }
}
