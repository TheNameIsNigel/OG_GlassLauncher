package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

final class SerializingExecutor implements Executor {
    /* access modifiers changed from: private */
    public static final Logger log = Logger.getLogger(SerializingExecutor.class.getName());
    private final Executor executor;
    /* access modifiers changed from: private */
    public final Object internalLock = new Object() {
        public String toString() {
            return "SerializingExecutor lock: " + super.toString();
        }
    };
    /* access modifiers changed from: private */
    @GuardedBy("internalLock")
    public boolean isThreadScheduled = false;
    private final TaskRunner taskRunner = new TaskRunner(this, (TaskRunner) null);
    /* access modifiers changed from: private */
    @GuardedBy("internalLock")
    public final Queue<Runnable> waitQueue = new ArrayDeque();

    public SerializingExecutor(Executor executor2) {
        Preconditions.checkNotNull(executor2, "'executor' must not be null.");
        this.executor = executor2;
    }

    public void execute(Runnable r) {
        Preconditions.checkNotNull(r, "'r' must not be null.");
        boolean scheduleTaskRunner = false;
        synchronized (this.internalLock) {
            this.waitQueue.add(r);
            if (!this.isThreadScheduled) {
                this.isThreadScheduled = true;
                scheduleTaskRunner = true;
            }
        }
        if (scheduleTaskRunner) {
            try {
                this.executor.execute(this.taskRunner);
                if (0 != 0) {
                    synchronized (this.internalLock) {
                        this.isThreadScheduled = false;
                    }
                }
            } catch (Throwable th) {
                if (1 != 0) {
                    synchronized (this.internalLock) {
                        this.isThreadScheduled = false;
                    }
                }
                throw th;
            }
        }
    }

    private class TaskRunner implements Runnable {
        /* synthetic */ TaskRunner(SerializingExecutor this$02, TaskRunner taskRunner) {
            this();
        }

        private TaskRunner() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0027, code lost:
            if (0 == 0) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0029, code lost:
            r4 = com.google.common.util.concurrent.SerializingExecutor.m466get0(r7.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x002f, code lost:
            monitor-enter(r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
            com.google.common.util.concurrent.SerializingExecutor.m470set0(r7.this$0, false);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0036, code lost:
            monitor-exit(r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
            r1.run();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:7:0x001f, code lost:
            com.google.common.util.concurrent.SerializingExecutor.m470set0(r7.this$0, false);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0025, code lost:
            r2 = false;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r7 = this;
                r2 = 1
            L_0x0001:
                com.google.common.util.concurrent.SerializingExecutor r3 = com.google.common.util.concurrent.SerializingExecutor.this     // Catch:{ all -> 0x005c }
                boolean r3 = r3.isThreadScheduled     // Catch:{ all -> 0x005c }
                com.google.common.base.Preconditions.checkState(r3)     // Catch:{ all -> 0x005c }
                com.google.common.util.concurrent.SerializingExecutor r3 = com.google.common.util.concurrent.SerializingExecutor.this     // Catch:{ all -> 0x005c }
                java.lang.Object r4 = r3.internalLock     // Catch:{ all -> 0x005c }
                monitor-enter(r4)     // Catch:{ all -> 0x005c }
                com.google.common.util.concurrent.SerializingExecutor r3 = com.google.common.util.concurrent.SerializingExecutor.this     // Catch:{ all -> 0x006e }
                java.util.Queue r3 = r3.waitQueue     // Catch:{ all -> 0x006e }
                java.lang.Object r1 = r3.poll()     // Catch:{ all -> 0x006e }
                java.lang.Runnable r1 = (java.lang.Runnable) r1     // Catch:{ all -> 0x006e }
                if (r1 != 0) goto L_0x0038
                com.google.common.util.concurrent.SerializingExecutor r3 = com.google.common.util.concurrent.SerializingExecutor.this     // Catch:{ all -> 0x006e }
                r5 = 0
                boolean unused = r3.isThreadScheduled = r5     // Catch:{ all -> 0x006e }
                r2 = 0
                monitor-exit(r4)     // Catch:{ all -> 0x005c }
                if (r2 == 0) goto L_0x0037
                com.google.common.util.concurrent.SerializingExecutor r3 = com.google.common.util.concurrent.SerializingExecutor.this
                java.lang.Object r4 = r3.internalLock
                monitor-enter(r4)
                com.google.common.util.concurrent.SerializingExecutor r3 = com.google.common.util.concurrent.SerializingExecutor.this     // Catch:{ all -> 0x0071 }
                r5 = 0
                boolean unused = r3.isThreadScheduled = r5     // Catch:{ all -> 0x0071 }
                monitor-exit(r4)
            L_0x0037:
                return
            L_0x0038:
                monitor-exit(r4)     // Catch:{ all -> 0x005c }
                r1.run()     // Catch:{ RuntimeException -> 0x003d }
                goto L_0x0001
            L_0x003d:
                r0 = move-exception
                java.util.logging.Logger r3 = com.google.common.util.concurrent.SerializingExecutor.log     // Catch:{ all -> 0x005c }
                java.util.logging.Level r4 = java.util.logging.Level.SEVERE     // Catch:{ all -> 0x005c }
                java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x005c }
                r5.<init>()     // Catch:{ all -> 0x005c }
                java.lang.String r6 = "Exception while executing runnable "
                java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x005c }
                java.lang.StringBuilder r5 = r5.append(r1)     // Catch:{ all -> 0x005c }
                java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x005c }
                r3.log(r4, r5, r0)     // Catch:{ all -> 0x005c }
                goto L_0x0001
            L_0x005c:
                r3 = move-exception
                if (r2 == 0) goto L_0x006d
                com.google.common.util.concurrent.SerializingExecutor r4 = com.google.common.util.concurrent.SerializingExecutor.this
                java.lang.Object r4 = r4.internalLock
                monitor-enter(r4)
                com.google.common.util.concurrent.SerializingExecutor r5 = com.google.common.util.concurrent.SerializingExecutor.this     // Catch:{ all -> 0x0074 }
                r6 = 0
                boolean unused = r5.isThreadScheduled = r6     // Catch:{ all -> 0x0074 }
                monitor-exit(r4)
            L_0x006d:
                throw r3
            L_0x006e:
                r3 = move-exception
                monitor-exit(r4)     // Catch:{ all -> 0x005c }
                throw r3     // Catch:{ all -> 0x005c }
            L_0x0071:
                r3 = move-exception
                monitor-exit(r4)
                throw r3
            L_0x0074:
                r3 = move-exception
                monitor-exit(r4)
                throw r3
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.common.util.concurrent.SerializingExecutor.TaskRunner.run():void");
        }
    }
}
