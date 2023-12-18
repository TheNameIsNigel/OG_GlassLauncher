package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

final class ListenerCallQueue<L> implements Runnable {
    private static final Logger logger = Logger.getLogger(ListenerCallQueue.class.getName());
    private final Executor executor;
    @GuardedBy("this")
    private boolean isThreadScheduled;
    private final L listener;
    @GuardedBy("this")
    private final Queue<Callback<L>> waitQueue = Queues.newArrayDeque();

    static abstract class Callback<L> {
        /* access modifiers changed from: private */
        public final String methodCall;

        /* access modifiers changed from: package-private */
        public abstract void call(L l);

        Callback(String methodCall2) {
            this.methodCall = methodCall2;
        }

        /* access modifiers changed from: package-private */
        public void enqueueOn(Iterable<ListenerCallQueue<L>> queues) {
            for (ListenerCallQueue<L> queue : queues) {
                queue.add(this);
            }
        }
    }

    ListenerCallQueue(L listener2, Executor executor2) {
        this.listener = Preconditions.checkNotNull(listener2);
        this.executor = (Executor) Preconditions.checkNotNull(executor2);
    }

    /* access modifiers changed from: package-private */
    public synchronized void add(Callback<L> callback) {
        this.waitQueue.add(callback);
    }

    /* access modifiers changed from: package-private */
    public void execute() {
        boolean scheduleTaskRunner = false;
        synchronized (this) {
            if (!this.isThreadScheduled) {
                this.isThreadScheduled = true;
                scheduleTaskRunner = true;
            }
        }
        if (scheduleTaskRunner) {
            try {
                this.executor.execute(this);
            } catch (RuntimeException e) {
                synchronized (this) {
                    this.isThreadScheduled = false;
                    logger.log(Level.SEVERE, "Exception while running callbacks for " + this.listener + " on " + this.executor, e);
                    throw e;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0016, code lost:
        if (0 == 0) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0018, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r7.isThreadScheduled = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001c, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r1.call(r7.listener);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0011, code lost:
        r7.isThreadScheduled = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0014, code lost:
        r2 = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        /*
            r7 = this;
            r2 = 1
        L_0x0001:
            monitor-enter(r7)     // Catch:{ all -> 0x0053 }
            boolean r3 = r7.isThreadScheduled     // Catch:{ all -> 0x005c }
            com.google.common.base.Preconditions.checkState(r3)     // Catch:{ all -> 0x005c }
            java.util.Queue<com.google.common.util.concurrent.ListenerCallQueue$Callback<L>> r3 = r7.waitQueue     // Catch:{ all -> 0x005c }
            java.lang.Object r1 = r3.poll()     // Catch:{ all -> 0x005c }
            com.google.common.util.concurrent.ListenerCallQueue$Callback r1 = (com.google.common.util.concurrent.ListenerCallQueue.Callback) r1     // Catch:{ all -> 0x005c }
            if (r1 != 0) goto L_0x001e
            r3 = 0
            r7.isThreadScheduled = r3     // Catch:{ all -> 0x005c }
            r2 = 0
            monitor-exit(r7)     // Catch:{ all -> 0x0053 }
            if (r2 == 0) goto L_0x001d
            monitor-enter(r7)
            r3 = 0
            r7.isThreadScheduled = r3     // Catch:{ all -> 0x005f }
            monitor-exit(r7)
        L_0x001d:
            return
        L_0x001e:
            monitor-exit(r7)     // Catch:{ all -> 0x0053 }
            L r3 = r7.listener     // Catch:{ RuntimeException -> 0x0025 }
            r1.call(r3)     // Catch:{ RuntimeException -> 0x0025 }
            goto L_0x0001
        L_0x0025:
            r0 = move-exception
            java.util.logging.Logger r3 = logger     // Catch:{ all -> 0x0053 }
            java.util.logging.Level r4 = java.util.logging.Level.SEVERE     // Catch:{ all -> 0x0053 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0053 }
            r5.<init>()     // Catch:{ all -> 0x0053 }
            java.lang.String r6 = "Exception while executing callback: "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0053 }
            L r6 = r7.listener     // Catch:{ all -> 0x0053 }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0053 }
            java.lang.String r6 = "."
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0053 }
            java.lang.String r6 = r1.methodCall     // Catch:{ all -> 0x0053 }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0053 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0053 }
            r3.log(r4, r5, r0)     // Catch:{ all -> 0x0053 }
            goto L_0x0001
        L_0x0053:
            r3 = move-exception
            if (r2 == 0) goto L_0x005b
            monitor-enter(r7)
            r4 = 0
            r7.isThreadScheduled = r4     // Catch:{ all -> 0x0062 }
            monitor-exit(r7)
        L_0x005b:
            throw r3
        L_0x005c:
            r3 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x0053 }
            throw r3     // Catch:{ all -> 0x0053 }
        L_0x005f:
            r3 = move-exception
            monitor-exit(r7)
            throw r3
        L_0x0062:
            r3 = move-exception
            monitor-exit(r7)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.common.util.concurrent.ListenerCallQueue.run():void");
    }
}
