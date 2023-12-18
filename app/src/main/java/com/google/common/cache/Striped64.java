package com.google.common.cache;

import java.util.Random;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

abstract class Striped64 extends Number {
    static final int NCPU = Runtime.getRuntime().availableProcessors();
    private static final AtomicLongFieldUpdater<Striped64> baseUpdater = AtomicLongFieldUpdater.newUpdater(Striped64.class, "base");
    private static final AtomicIntegerFieldUpdater<Striped64> busyUpdater = AtomicIntegerFieldUpdater.newUpdater(Striped64.class, "busy");
    static final Random rng = new Random();
    static final ThreadLocal<int[]> threadHashCode = new ThreadLocal<>();
    volatile transient long base;
    volatile transient int busy;
    volatile transient Cell[] cells;

    /* access modifiers changed from: package-private */
    public abstract long fn(long j, long j2);

    static final class Cell {
        private static final AtomicLongFieldUpdater<Cell> valueUpdater = AtomicLongFieldUpdater.newUpdater(Cell.class, "value");
        volatile long p0;
        volatile long p1;
        volatile long p2;
        volatile long p3;
        volatile long p4;
        volatile long p5;
        volatile long p6;
        volatile long q0;
        volatile long q1;
        volatile long q2;
        volatile long q3;
        volatile long q4;
        volatile long q5;
        volatile long q6;
        volatile long value;

        Cell(long x) {
            this.value = x;
        }

        /* access modifiers changed from: package-private */
        public final boolean cas(long cmp, long val) {
            return valueUpdater.compareAndSet(this, cmp, val);
        }
    }

    Striped64() {
    }

    /* access modifiers changed from: package-private */
    public final boolean casBase(long cmp, long val) {
        return baseUpdater.compareAndSet(this, cmp, val);
    }

    /* access modifiers changed from: package-private */
    public final boolean casBusy() {
        return busyUpdater.compareAndSet(this, 0, 1);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public final void retryUpdate(long x, int[] hc, boolean wasUncontended) {
        int h;
        int n;
        int m;
        if (hc == null) {
            hc = new int[1];
            threadHashCode.set(hc);
            int r = rng.nextInt();
            if (r == 0) {
                h = 1;
            } else {
                h = r;
            }
            hc[0] = h;
        } else {
            h = hc[0];
        }
        boolean collide = false;
        while (true) {
            Cell[] as = this.cells;
            if (as != null && (n = as.length) > 0) {
                Cell a = as[(n - 1) & h];
                if (a == null) {
                    if (this.busy == 0) {
                        Cell cell = new Cell(x);
                        if (this.busy == 0 && casBusy()) {
                            boolean created = false;
                            try {
                                Cell[] rs = this.cells;
                                if (rs != null && (m = rs.length) > 0) {
                                    int j = (m - 1) & h;
                                    if (rs[j] == null) {
                                        rs[j] = cell;
                                        created = true;
                                    }
                                }
                                if (created) {
                                    return;
                                }
                            } finally {
                                this.busy = 0;
                            }
                        }
                    }
                    collide = false;
                } else if (!wasUncontended) {
                    wasUncontended = true;
                } else {
                    long v = a.value;
                    if (a.cas(v, fn(v, x))) {
                        return;
                    }
                    if (n >= NCPU || this.cells != as) {
                        collide = false;
                    } else if (!collide) {
                        collide = true;
                    } else if (this.busy == 0 && casBusy()) {
                        try {
                            if (this.cells == as) {
                                Cell[] rs2 = new Cell[(n << 1)];
                                System.arraycopy(as, 0, rs2, 0, n);
                                this.cells = rs2;
                            }
                            this.busy = 0;
                            collide = false;
                        } catch (Throwable th) {
                            this.busy = 0;
                            throw th;
                        }
                    }
                }
                int h2 = h ^ (h << 13);
                int h3 = h2 ^ (h2 >>> 17);
                h = h3 ^ (h3 << 5);
                hc[0] = h;
            } else if (this.busy == 0 && this.cells == as && casBusy()) {
                boolean init = false;
                try {
                    if (this.cells == as) {
                        Cell[] rs3 = new Cell[2];
                        rs3[h & 1] = new Cell(x);
                        this.cells = rs3;
                        init = true;
                    }
                    if (init) {
                        return;
                    }
                } finally {
                    this.busy = 0;
                }
            } else {
                long v2 = this.base;
                if (casBase(v2, fn(v2, x))) {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void internalReset(long initialValue) {
        Cell[] as = this.cells;
        this.base = initialValue;
        if (as != null) {
            for (Cell a : as) {
                if (a != null) {
                    a.value = initialValue;
                }
            }
        }
    }
}
