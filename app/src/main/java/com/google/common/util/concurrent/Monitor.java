package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.concurrent.GuardedBy;

@Beta
public final class Monitor {
    @GuardedBy("lock")
    private Guard activeGuards;
    private final boolean fair;
    /* access modifiers changed from: private */
    public final ReentrantLock lock;

    @Beta
    public static abstract class Guard {
        final Condition condition;
        final Monitor monitor;
        @GuardedBy("monitor.lock")
        Guard next;
        @GuardedBy("monitor.lock")
        int waiterCount = 0;

        public abstract boolean isSatisfied();

        protected Guard(Monitor monitor2) {
            this.monitor = (Monitor) Preconditions.checkNotNull(monitor2, "monitor");
            this.condition = monitor2.lock.newCondition();
        }
    }

    public Monitor() {
        this(false);
    }

    public Monitor(boolean fair2) {
        this.activeGuards = null;
        this.fair = fair2;
        this.lock = new ReentrantLock(fair2);
    }

    public void enter() {
        this.lock.lock();
    }

    public void enterInterruptibly() throws InterruptedException {
        this.lock.lockInterruptibly();
    }

    public boolean enter(long time, TimeUnit unit) {
        boolean tryLock;
        long timeoutNanos = unit.toNanos(time);
        ReentrantLock lock2 = this.lock;
        if (!this.fair && lock2.tryLock()) {
            return true;
        }
        long deadline = System.nanoTime() + timeoutNanos;
        boolean interrupted = Thread.interrupted();
        while (true) {
            try {
                tryLock = lock2.tryLock(timeoutNanos, TimeUnit.NANOSECONDS);
                break;
            } catch (InterruptedException e) {
                interrupted = true;
                timeoutNanos = deadline - System.nanoTime();
            } catch (Throwable th) {
                if (1 != 0) {
                    Thread.currentThread().interrupt();
                }
                throw th;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return tryLock;
    }

    public boolean enterInterruptibly(long time, TimeUnit unit) throws InterruptedException {
        return this.lock.tryLock(time, unit);
    }

    public boolean tryEnter() {
        return this.lock.tryLock();
    }

    public void enterWhen(Guard guard) throws InterruptedException {
        if (guard.monitor != this) {
            throw new IllegalMonitorStateException();
        }
        ReentrantLock lock2 = this.lock;
        boolean signalBeforeWaiting = lock2.isHeldByCurrentThread();
        lock2.lockInterruptibly();
        boolean satisfied = false;
        try {
            if (!guard.isSatisfied()) {
                await(guard, signalBeforeWaiting);
            }
            satisfied = true;
        } finally {
            if (!satisfied) {
                leave();
            }
        }
    }

    public void enterWhenUninterruptibly(Guard guard) {
        if (guard.monitor != this) {
            throw new IllegalMonitorStateException();
        }
        ReentrantLock lock2 = this.lock;
        boolean signalBeforeWaiting = lock2.isHeldByCurrentThread();
        lock2.lock();
        boolean satisfied = false;
        try {
            if (!guard.isSatisfied()) {
                awaitUninterruptibly(guard, signalBeforeWaiting);
            }
            satisfied = true;
        } finally {
            if (!satisfied) {
                leave();
            }
        }
    }

    public boolean enterWhen(Guard guard, long time, TimeUnit unit) throws InterruptedException {
        long timeoutNanos = unit.toNanos(time);
        if (guard.monitor != this) {
            throw new IllegalMonitorStateException();
        }
        ReentrantLock lock2 = this.lock;
        boolean reentrant = lock2.isHeldByCurrentThread();
        if (this.fair || (!lock2.tryLock())) {
            long deadline = System.nanoTime() + timeoutNanos;
            if (!lock2.tryLock(time, unit)) {
                return false;
            }
            timeoutNanos = deadline - System.nanoTime();
        }
        try {
            boolean satisfied = !guard.isSatisfied() ? awaitNanos(guard, timeoutNanos, reentrant) : true;
            if (!satisfied) {
                if (0 != 0 && (!reentrant)) {
                    try {
                        signalNextWaiter();
                    } catch (Throwable th) {
                        lock2.unlock();
                        throw th;
                    }
                }
            }
            return satisfied;
        } catch (Throwable th2) {
            if (0 == 0) {
                if (1 != 0 && (!reentrant)) {
                    signalNextWaiter();
                }
            }
            throw th2;
        } finally {
            lock2.unlock();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x005b A[Catch:{ InterruptedException -> 0x0074, all -> 0x007e }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0065 A[SYNTHETIC, Splitter:B:31:0x0065] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x006a A[DONT_GENERATE] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0072  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean enterWhenUninterruptibly(com.google.common.util.concurrent.Monitor.Guard r17, long r18, java.util.concurrent.TimeUnit r20) {
        /*
            r16 = this;
            r0 = r20
            r1 = r18
            long r12 = r0.toNanos(r1)
            r0 = r17
            com.google.common.util.concurrent.Monitor r14 = r0.monitor
            r0 = r16
            if (r14 == r0) goto L_0x0016
            java.lang.IllegalMonitorStateException r14 = new java.lang.IllegalMonitorStateException
            r14.<init>()
            throw r14
        L_0x0016:
            r0 = r16
            java.util.concurrent.locks.ReentrantLock r8 = r0.lock
            long r14 = java.lang.System.nanoTime()
            long r4 = r14 + r12
            boolean r11 = r8.isHeldByCurrentThread()
            boolean r7 = java.lang.Thread.interrupted()
            r0 = r16
            boolean r14 = r0.fair     // Catch:{ all -> 0x0085 }
            if (r14 != 0) goto L_0x0036
            boolean r14 = r8.tryLock()     // Catch:{ all -> 0x0085 }
            r14 = r14 ^ 1
            if (r14 == 0) goto L_0x0054
        L_0x0036:
            r9 = 0
        L_0x0037:
            java.util.concurrent.TimeUnit r14 = java.util.concurrent.TimeUnit.NANOSECONDS     // Catch:{ InterruptedException -> 0x004a }
            boolean r9 = r8.tryLock(r12, r14)     // Catch:{ InterruptedException -> 0x004a }
            if (r9 != 0) goto L_0x004c
            r14 = 0
            if (r7 == 0) goto L_0x0049
            java.lang.Thread r15 = java.lang.Thread.currentThread()
            r15.interrupt()
        L_0x0049:
            return r14
        L_0x004a:
            r6 = move-exception
            r7 = 1
        L_0x004c:
            long r14 = java.lang.System.nanoTime()     // Catch:{ all -> 0x0085 }
            long r12 = r4 - r14
            if (r9 == 0) goto L_0x0037
        L_0x0054:
            r10 = 0
        L_0x0055:
            boolean r14 = r17.isSatisfied()     // Catch:{ InterruptedException -> 0x0074 }
            if (r14 != 0) goto L_0x0072
            r0 = r16
            r1 = r17
            boolean r10 = r0.awaitNanos(r1, r12, r11)     // Catch:{ InterruptedException -> 0x0074 }
        L_0x0063:
            if (r10 != 0) goto L_0x0068
            r8.unlock()     // Catch:{ all -> 0x0085 }
        L_0x0068:
            if (r7 == 0) goto L_0x0071
            java.lang.Thread r14 = java.lang.Thread.currentThread()
            r14.interrupt()
        L_0x0071:
            return r10
        L_0x0072:
            r10 = 1
            goto L_0x0063
        L_0x0074:
            r6 = move-exception
            r7 = 1
            r11 = 0
            long r14 = java.lang.System.nanoTime()     // Catch:{ all -> 0x007e }
            long r12 = r4 - r14
            goto L_0x0055
        L_0x007e:
            r14 = move-exception
            if (r10 != 0) goto L_0x0084
            r8.unlock()     // Catch:{ all -> 0x0085 }
        L_0x0084:
            throw r14     // Catch:{ all -> 0x0085 }
        L_0x0085:
            r14 = move-exception
            if (r7 == 0) goto L_0x008f
            java.lang.Thread r15 = java.lang.Thread.currentThread()
            r15.interrupt()
        L_0x008f:
            throw r14
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.common.util.concurrent.Monitor.enterWhenUninterruptibly(com.google.common.util.concurrent.Monitor$Guard, long, java.util.concurrent.TimeUnit):boolean");
    }

    public boolean enterIf(Guard guard) {
        if (guard.monitor != this) {
            throw new IllegalMonitorStateException();
        }
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        boolean satisfied = false;
        try {
            satisfied = guard.isSatisfied();
            return satisfied;
        } finally {
            if (!satisfied) {
                lock2.unlock();
            }
        }
    }

    public boolean enterIfInterruptibly(Guard guard) throws InterruptedException {
        if (guard.monitor != this) {
            throw new IllegalMonitorStateException();
        }
        ReentrantLock lock2 = this.lock;
        lock2.lockInterruptibly();
        boolean satisfied = false;
        try {
            satisfied = guard.isSatisfied();
            return satisfied;
        } finally {
            if (!satisfied) {
                lock2.unlock();
            }
        }
    }

    public boolean enterIf(Guard guard, long time, TimeUnit unit) {
        if (guard.monitor != this) {
            throw new IllegalMonitorStateException();
        } else if (!enter(time, unit)) {
            return false;
        } else {
            boolean satisfied = false;
            try {
                satisfied = guard.isSatisfied();
                return satisfied;
            } finally {
                if (!satisfied) {
                    this.lock.unlock();
                }
            }
        }
    }

    public boolean enterIfInterruptibly(Guard guard, long time, TimeUnit unit) throws InterruptedException {
        if (guard.monitor != this) {
            throw new IllegalMonitorStateException();
        }
        ReentrantLock lock2 = this.lock;
        if (!lock2.tryLock(time, unit)) {
            return false;
        }
        boolean satisfied = false;
        try {
            satisfied = guard.isSatisfied();
            return satisfied;
        } finally {
            if (!satisfied) {
                lock2.unlock();
            }
        }
    }

    public boolean tryEnterIf(Guard guard) {
        if (guard.monitor != this) {
            throw new IllegalMonitorStateException();
        }
        ReentrantLock lock2 = this.lock;
        if (!lock2.tryLock()) {
            return false;
        }
        boolean satisfied = false;
        try {
            satisfied = guard.isSatisfied();
            return satisfied;
        } finally {
            if (!satisfied) {
                lock2.unlock();
            }
        }
    }

    public void waitFor(Guard guard) throws InterruptedException {
        if (!(guard.monitor == this) || !this.lock.isHeldByCurrentThread()) {
            throw new IllegalMonitorStateException();
        } else if (!guard.isSatisfied()) {
            await(guard, true);
        }
    }

    public void waitForUninterruptibly(Guard guard) {
        if (!(guard.monitor == this) || !this.lock.isHeldByCurrentThread()) {
            throw new IllegalMonitorStateException();
        } else if (!guard.isSatisfied()) {
            awaitUninterruptibly(guard, true);
        }
    }

    public boolean waitFor(Guard guard, long time, TimeUnit unit) throws InterruptedException {
        long timeoutNanos = unit.toNanos(time);
        if (!(guard.monitor == this) || !this.lock.isHeldByCurrentThread()) {
            throw new IllegalMonitorStateException();
        } else if (!guard.isSatisfied()) {
            return awaitNanos(guard, timeoutNanos, true);
        } else {
            return true;
        }
    }

    public boolean waitForUninterruptibly(Guard guard, long time, TimeUnit unit) {
        long timeoutNanos = unit.toNanos(time);
        if (!(guard.monitor == this) || !this.lock.isHeldByCurrentThread()) {
            throw new IllegalMonitorStateException();
        } else if (guard.isSatisfied()) {
            return true;
        } else {
            boolean signalBeforeWaiting = true;
            long deadline = System.nanoTime() + timeoutNanos;
            boolean interrupted = Thread.interrupted();
            while (true) {
                try {
                    boolean awaitNanos = awaitNanos(guard, timeoutNanos, signalBeforeWaiting);
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                    return awaitNanos;
                } catch (InterruptedException e) {
                    interrupted = true;
                    if (guard.isSatisfied()) {
                        if (1 != 0) {
                            Thread.currentThread().interrupt();
                        }
                        return true;
                    }
                    signalBeforeWaiting = false;
                    timeoutNanos = deadline - System.nanoTime();
                } catch (Throwable th) {
                    if (1 != 0) {
                        Thread.currentThread().interrupt();
                    }
                    throw th;
                }
            }
        }
    }

    public void leave() {
        ReentrantLock lock2 = this.lock;
        try {
            if (lock2.getHoldCount() == 1) {
                signalNextWaiter();
            }
        } finally {
            lock2.unlock();
        }
    }

    public boolean isFair() {
        return this.fair;
    }

    public boolean isOccupied() {
        return this.lock.isLocked();
    }

    public boolean isOccupiedByCurrentThread() {
        return this.lock.isHeldByCurrentThread();
    }

    public int getOccupiedDepth() {
        return this.lock.getHoldCount();
    }

    public int getQueueLength() {
        return this.lock.getQueueLength();
    }

    public boolean hasQueuedThreads() {
        return this.lock.hasQueuedThreads();
    }

    public boolean hasQueuedThread(Thread thread) {
        return this.lock.hasQueuedThread(thread);
    }

    public boolean hasWaiters(Guard guard) {
        return getWaitQueueLength(guard) > 0;
    }

    public int getWaitQueueLength(Guard guard) {
        if (guard.monitor != this) {
            throw new IllegalMonitorStateException();
        }
        this.lock.lock();
        try {
            return guard.waiterCount;
        } finally {
            this.lock.unlock();
        }
    }

    @GuardedBy("lock")
    private void signalNextWaiter() {
        for (Guard guard = this.activeGuards; guard != null; guard = guard.next) {
            if (isSatisfied(guard)) {
                guard.condition.signal();
                return;
            }
        }
    }

    @GuardedBy("lock")
    private boolean isSatisfied(Guard guard) {
        try {
            return guard.isSatisfied();
        } catch (Throwable throwable) {
            signalAllWaiters();
            throw Throwables.propagate(throwable);
        }
    }

    @GuardedBy("lock")
    private void signalAllWaiters() {
        for (Guard guard = this.activeGuards; guard != null; guard = guard.next) {
            guard.condition.signalAll();
        }
    }

    @GuardedBy("lock")
    private void beginWaitingFor(Guard guard) {
        int waiters = guard.waiterCount;
        guard.waiterCount = waiters + 1;
        if (waiters == 0) {
            guard.next = this.activeGuards;
            this.activeGuards = guard;
        }
    }

    @GuardedBy("lock")
    private void endWaitingFor(Guard guard) {
        int waiters = guard.waiterCount - 1;
        guard.waiterCount = waiters;
        if (waiters == 0) {
            Guard p = this.activeGuards;
            Guard guard2 = null;
            while (p != guard) {
                guard2 = p;
                p = p.next;
            }
            if (guard2 == null) {
                this.activeGuards = p.next;
            } else {
                guard2.next = p.next;
            }
            p.next = null;
        }
    }

    @GuardedBy("lock")
    private void await(Guard guard, boolean signalBeforeWaiting) throws InterruptedException {
        if (signalBeforeWaiting) {
            signalNextWaiter();
        }
        beginWaitingFor(guard);
        do {
            try {
                guard.condition.await();
            } finally {
                endWaitingFor(guard);
            }
        } while (!guard.isSatisfied());
    }

    @GuardedBy("lock")
    private void awaitUninterruptibly(Guard guard, boolean signalBeforeWaiting) {
        if (signalBeforeWaiting) {
            signalNextWaiter();
        }
        beginWaitingFor(guard);
        do {
            try {
                guard.condition.awaitUninterruptibly();
            } finally {
                endWaitingFor(guard);
            }
        } while (!guard.isSatisfied());
    }

    @GuardedBy("lock")
    private boolean awaitNanos(Guard guard, long nanos, boolean signalBeforeWaiting) throws InterruptedException {
        if (signalBeforeWaiting) {
            signalNextWaiter();
        }
        beginWaitingFor(guard);
        while (nanos >= 0) {
            try {
                nanos = guard.condition.awaitNanos(nanos);
                if (guard.isSatisfied()) {
                    return true;
                }
            } finally {
                endWaitingFor(guard);
            }
        }
        endWaitingFor(guard);
        return false;
    }
}
