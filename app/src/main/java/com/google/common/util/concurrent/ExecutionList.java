package com.google.common.util.concurrent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

public final class ExecutionList {
    @VisibleForTesting
    static final Logger log = Logger.getLogger(ExecutionList.class.getName());
    @GuardedBy("this")
    private boolean executed;
    @GuardedBy("this")
    private RunnableExecutorPair runnables;

    public void add(Runnable runnable, Executor executor) {
        Preconditions.checkNotNull(runnable, "Runnable was null.");
        Preconditions.checkNotNull(executor, "Executor was null.");
        synchronized (this) {
            if (!this.executed) {
                this.runnables = new RunnableExecutorPair(runnable, executor, this.runnables);
            } else {
                executeListener(runnable, executor);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0010, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0011, code lost:
        if (r0 == null) goto L_0x001d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0013, code lost:
        r2 = r0;
        r0 = r0.next;
        r2.next = r1;
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x001d, code lost:
        if (r1 == null) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x001f, code lost:
        executeListener(r1.runnable, r1.executor);
        r1 = r1.next;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0029, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void execute() {
        /*
            r5 = this;
            monitor-enter(r5)
            boolean r3 = r5.executed     // Catch:{ all -> 0x001a }
            if (r3 == 0) goto L_0x0007
            monitor-exit(r5)
            return
        L_0x0007:
            r3 = 1
            r5.executed = r3     // Catch:{ all -> 0x001a }
            com.google.common.util.concurrent.ExecutionList$RunnableExecutorPair r0 = r5.runnables     // Catch:{ all -> 0x001a }
            r3 = 0
            r5.runnables = r3     // Catch:{ all -> 0x001a }
            monitor-exit(r5)
            r1 = 0
        L_0x0011:
            if (r0 == 0) goto L_0x001d
            r2 = r0
            com.google.common.util.concurrent.ExecutionList$RunnableExecutorPair r0 = r0.next
            r2.next = r1
            r1 = r2
            goto L_0x0011
        L_0x001a:
            r3 = move-exception
            monitor-exit(r5)
            throw r3
        L_0x001d:
            if (r1 == 0) goto L_0x0029
            java.lang.Runnable r3 = r1.runnable
            java.util.concurrent.Executor r4 = r1.executor
            executeListener(r3, r4)
            com.google.common.util.concurrent.ExecutionList$RunnableExecutorPair r1 = r1.next
            goto L_0x001d
        L_0x0029:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.common.util.concurrent.ExecutionList.execute():void");
    }

    private static void executeListener(Runnable runnable, Executor executor) {
        try {
            executor.execute(runnable);
        } catch (RuntimeException e) {
            log.log(Level.SEVERE, "RuntimeException while executing runnable " + runnable + " with executor " + executor, e);
        }
    }

    private static final class RunnableExecutorPair {
        final Executor executor;
        @Nullable
        RunnableExecutorPair next;
        final Runnable runnable;

        RunnableExecutorPair(Runnable runnable2, Executor executor2, RunnableExecutorPair next2) {
            this.runnable = runnable2;
            this.executor = executor2;
            this.next = next2;
        }
    }
}
