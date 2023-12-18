package com.google.common.collect;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.common.collect.MapMakerInternalMap;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

class ComputingConcurrentHashMap<K, V> extends MapMakerInternalMap<K, V> {
    private static final long serialVersionUID = 4;
    final Function<? super K, ? extends V> computingFunction;

    ComputingConcurrentHashMap(MapMaker builder, Function<? super K, ? extends V> computingFunction2) {
        super(builder);
        this.computingFunction = (Function) Preconditions.checkNotNull(computingFunction2);
    }

    /* access modifiers changed from: package-private */
    public MapMakerInternalMap.Segment<K, V> createSegment(int initialCapacity, int maxSegmentSize) {
        return new ComputingSegment(this, initialCapacity, maxSegmentSize);
    }

    /* access modifiers changed from: package-private */
    public ComputingSegment<K, V> segmentFor(int hash) {
        return (ComputingSegment) super.segmentFor(hash);
    }

    /* access modifiers changed from: package-private */
    public V getOrCompute(K key) throws ExecutionException {
        int hash = hash(Preconditions.checkNotNull(key));
        return segmentFor(hash).getOrCompute(key, hash, this.computingFunction);
    }

    static final class ComputingSegment<K, V> extends MapMakerInternalMap.Segment<K, V> {
        ComputingSegment(MapMakerInternalMap<K, V> map, int initialCapacity, int maxSegmentSize) {
            super(map, initialCapacity, maxSegmentSize);
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
            if ((!r4.getValueReference().isComputingReference()) != false) goto L_0x0021;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0060, code lost:
            if (r4.getValueReference().isComputingReference() == false) goto L_0x008d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0062, code lost:
            r3 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
            r10 = r4.getValueReference().get();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x0095, code lost:
            if (r10 != null) goto L_0x00b8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x0097, code lost:
            enqueueNotification(r5, r16, r10, com.google.common.collect.MapMaker.RemovalCause.COLLECTED);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x009e, code lost:
            r14.evictionQueue.remove(r4);
            r14.expirationQueue.remove(r4);
            r14.count = r8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:51:0x00be, code lost:
            if (r14.map.expires() == false) goto L_0x00d0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:0x00c6, code lost:
            if (r14.map.isExpired(r4) == false) goto L_0x00d0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x00c8, code lost:
            enqueueNotification(r5, r16, r10, com.google.common.collect.MapMaker.RemovalCause.EXPIRED);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:55:0x00d0, code lost:
            recordLockedRead(r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
            unlock();
            postWriteCleanup();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:58:0x00d9, code lost:
            postReadCleanup();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:0x00dc, code lost:
            return r10;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public V getOrCompute(K r15, int r16, com.google.common.base.Function<? super K, ? extends V> r17) throws java.util.concurrent.ExecutionException {
            /*
                r14 = this;
            L_0x0000:
                com.google.common.collect.MapMakerInternalMap$ReferenceEntry r4 = r14.getEntry(r15, r16)     // Catch:{ all -> 0x00b3 }
                if (r4 == 0) goto L_0x0013
                java.lang.Object r10 = r14.getLiveValue(r4)     // Catch:{ all -> 0x00b3 }
                if (r10 == 0) goto L_0x0013
                r14.recordRead(r4)     // Catch:{ all -> 0x00b3 }
                r14.postReadCleanup()
                return r10
            L_0x0013:
                if (r4 == 0) goto L_0x0021
                com.google.common.collect.MapMakerInternalMap$ValueReference r12 = r4.getValueReference()     // Catch:{ all -> 0x00b3 }
                boolean r12 = r12.isComputingReference()     // Catch:{ all -> 0x00b3 }
                r12 = r12 ^ 1
                if (r12 == 0) goto L_0x00e8
            L_0x0021:
                r3 = 1
                r1 = 0
                r14.lock()     // Catch:{ all -> 0x00b3 }
                r14.preWriteCleanup()     // Catch:{ all -> 0x00ab }
                int r12 = r14.count     // Catch:{ all -> 0x00ab }
                int r8 = r12 + -1
                java.util.concurrent.atomic.AtomicReferenceArray r9 = r14.table     // Catch:{ all -> 0x00ab }
                int r12 = r9.length()     // Catch:{ all -> 0x00ab }
                int r12 = r12 + -1
                r7 = r16 & r12
                java.lang.Object r6 = r9.get(r7)     // Catch:{ all -> 0x00ab }
                com.google.common.collect.MapMakerInternalMap$ReferenceEntry r6 = (com.google.common.collect.MapMakerInternalMap.ReferenceEntry) r6     // Catch:{ all -> 0x00ab }
                r4 = r6
            L_0x003e:
                if (r4 == 0) goto L_0x0063
                java.lang.Object r5 = r4.getKey()     // Catch:{ all -> 0x00ab }
                int r12 = r4.getHash()     // Catch:{ all -> 0x00ab }
                r0 = r16
                if (r12 != r0) goto L_0x00dd
                if (r5 == 0) goto L_0x00dd
                com.google.common.collect.MapMakerInternalMap r12 = r14.map     // Catch:{ all -> 0x00ab }
                com.google.common.base.Equivalence<java.lang.Object> r12 = r12.keyEquivalence     // Catch:{ all -> 0x00ab }
                boolean r12 = r12.equivalent(r15, r5)     // Catch:{ all -> 0x00ab }
                if (r12 == 0) goto L_0x00dd
                com.google.common.collect.MapMakerInternalMap$ValueReference r11 = r4.getValueReference()     // Catch:{ all -> 0x00ab }
                boolean r12 = r11.isComputingReference()     // Catch:{ all -> 0x00ab }
                if (r12 == 0) goto L_0x008d
                r3 = 0
            L_0x0063:
                if (r3 == 0) goto L_0x007b
                com.google.common.collect.ComputingConcurrentHashMap$ComputingValueReference r2 = new com.google.common.collect.ComputingConcurrentHashMap$ComputingValueReference     // Catch:{ all -> 0x00ab }
                r0 = r17
                r2.<init>(r0)     // Catch:{ all -> 0x00ab }
                if (r4 != 0) goto L_0x00e3
                r0 = r16
                com.google.common.collect.MapMakerInternalMap$ReferenceEntry r4 = r14.newEntry(r15, r0, r6)     // Catch:{ all -> 0x0105 }
                r4.setValueReference(r2)     // Catch:{ all -> 0x0105 }
                r9.set(r7, r4)     // Catch:{ all -> 0x0105 }
                r1 = r2
            L_0x007b:
                r14.unlock()     // Catch:{ all -> 0x00b3 }
                r14.postWriteCleanup()     // Catch:{ all -> 0x00b3 }
                if (r3 == 0) goto L_0x00e8
                r0 = r16
                java.lang.Object r12 = r14.compute(r15, r0, r4, r1)     // Catch:{ all -> 0x00b3 }
                r14.postReadCleanup()
                return r12
            L_0x008d:
                com.google.common.collect.MapMakerInternalMap$ValueReference r12 = r4.getValueReference()     // Catch:{ all -> 0x00ab }
                java.lang.Object r10 = r12.get()     // Catch:{ all -> 0x00ab }
                if (r10 != 0) goto L_0x00b8
                com.google.common.collect.MapMaker$RemovalCause r12 = com.google.common.collect.MapMaker.RemovalCause.COLLECTED     // Catch:{ all -> 0x00ab }
                r0 = r16
                r14.enqueueNotification(r5, r0, r10, r12)     // Catch:{ all -> 0x00ab }
            L_0x009e:
                java.util.Queue r12 = r14.evictionQueue     // Catch:{ all -> 0x00ab }
                r12.remove(r4)     // Catch:{ all -> 0x00ab }
                java.util.Queue r12 = r14.expirationQueue     // Catch:{ all -> 0x00ab }
                r12.remove(r4)     // Catch:{ all -> 0x00ab }
                r14.count = r8     // Catch:{ all -> 0x00ab }
                goto L_0x0063
            L_0x00ab:
                r12 = move-exception
            L_0x00ac:
                r14.unlock()     // Catch:{ all -> 0x00b3 }
                r14.postWriteCleanup()     // Catch:{ all -> 0x00b3 }
                throw r12     // Catch:{ all -> 0x00b3 }
            L_0x00b3:
                r12 = move-exception
                r14.postReadCleanup()
                throw r12
            L_0x00b8:
                com.google.common.collect.MapMakerInternalMap r12 = r14.map     // Catch:{ all -> 0x00ab }
                boolean r12 = r12.expires()     // Catch:{ all -> 0x00ab }
                if (r12 == 0) goto L_0x00d0
                com.google.common.collect.MapMakerInternalMap r12 = r14.map     // Catch:{ all -> 0x00ab }
                boolean r12 = r12.isExpired(r4)     // Catch:{ all -> 0x00ab }
                if (r12 == 0) goto L_0x00d0
                com.google.common.collect.MapMaker$RemovalCause r12 = com.google.common.collect.MapMaker.RemovalCause.EXPIRED     // Catch:{ all -> 0x00ab }
                r0 = r16
                r14.enqueueNotification(r5, r0, r10, r12)     // Catch:{ all -> 0x00ab }
                goto L_0x009e
            L_0x00d0:
                r14.recordLockedRead(r4)     // Catch:{ all -> 0x00ab }
                r14.unlock()     // Catch:{ all -> 0x00b3 }
                r14.postWriteCleanup()     // Catch:{ all -> 0x00b3 }
                r14.postReadCleanup()
                return r10
            L_0x00dd:
                com.google.common.collect.MapMakerInternalMap$ReferenceEntry r4 = r4.getNext()     // Catch:{ all -> 0x00ab }
                goto L_0x003e
            L_0x00e3:
                r4.setValueReference(r2)     // Catch:{ all -> 0x0105 }
                r1 = r2
                goto L_0x007b
            L_0x00e8:
                boolean r12 = java.lang.Thread.holdsLock(r4)     // Catch:{ all -> 0x00b3 }
                r12 = r12 ^ 1
                java.lang.String r13 = "Recursive computation"
                com.google.common.base.Preconditions.checkState(r12, r13)     // Catch:{ all -> 0x00b3 }
                com.google.common.collect.MapMakerInternalMap$ValueReference r12 = r4.getValueReference()     // Catch:{ all -> 0x00b3 }
                java.lang.Object r10 = r12.waitForValue()     // Catch:{ all -> 0x00b3 }
                if (r10 == 0) goto L_0x0000
                r14.recordRead(r4)     // Catch:{ all -> 0x00b3 }
                r14.postReadCleanup()
                return r10
            L_0x0105:
                r12 = move-exception
                r1 = r2
                goto L_0x00ac
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.common.collect.ComputingConcurrentHashMap.ComputingSegment.getOrCompute(java.lang.Object, int, com.google.common.base.Function):java.lang.Object");
        }

        /* access modifiers changed from: package-private */
        public V compute(K key, int hash, MapMakerInternalMap.ReferenceEntry<K, V> e, ComputingValueReference<K, V> computingValueReference) throws ExecutionException {
            V value = null;
            long nanoTime = System.nanoTime();
            long end = 0;
            try {
                synchronized (e) {
                    value = computingValueReference.compute(key, hash);
                    end = System.nanoTime();
                }
                if (!(value == null || put(key, hash, value, true) == null)) {
                    enqueueNotification(key, hash, value, MapMaker.RemovalCause.REPLACED);
                }
                return value;
            } finally {
                if (end == 0) {
                    long end2 = System.nanoTime();
                }
                if (value == null) {
                    clearValue(key, hash, computingValueReference);
                }
            }
        }
    }

    private static final class ComputationExceptionReference<K, V> implements MapMakerInternalMap.ValueReference<K, V> {
        final Throwable t;

        ComputationExceptionReference(Throwable t2) {
            this.t = t2;
        }

        public V get() {
            return null;
        }

        public MapMakerInternalMap.ReferenceEntry<K, V> getEntry() {
            return null;
        }

        public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> referenceQueue, V v, MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry) {
            return this;
        }

        public boolean isComputingReference() {
            return false;
        }

        public V waitForValue() throws ExecutionException {
            throw new ExecutionException(this.t);
        }

        public void clear(MapMakerInternalMap.ValueReference<K, V> valueReference) {
        }
    }

    private static final class ComputedReference<K, V> implements MapMakerInternalMap.ValueReference<K, V> {
        final V value;

        ComputedReference(@Nullable V value2) {
            this.value = value2;
        }

        public V get() {
            return this.value;
        }

        public MapMakerInternalMap.ReferenceEntry<K, V> getEntry() {
            return null;
        }

        public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> referenceQueue, V v, MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry) {
            return this;
        }

        public boolean isComputingReference() {
            return false;
        }

        public V waitForValue() {
            return get();
        }

        public void clear(MapMakerInternalMap.ValueReference<K, V> valueReference) {
        }
    }

    private static final class ComputingValueReference<K, V> implements MapMakerInternalMap.ValueReference<K, V> {
        @GuardedBy("ComputingValueReference.this")
        volatile MapMakerInternalMap.ValueReference<K, V> computedReference = ComputingConcurrentHashMap.unset();
        final Function<? super K, ? extends V> computingFunction;

        public ComputingValueReference(Function<? super K, ? extends V> computingFunction2) {
            this.computingFunction = computingFunction2;
        }

        public V get() {
            return null;
        }

        public MapMakerInternalMap.ReferenceEntry<K, V> getEntry() {
            return null;
        }

        public MapMakerInternalMap.ValueReference<K, V> copyFor(ReferenceQueue<V> referenceQueue, @Nullable V v, MapMakerInternalMap.ReferenceEntry<K, V> referenceEntry) {
            return this;
        }

        public boolean isComputingReference() {
            return true;
        }

        public V waitForValue() throws ExecutionException {
            if (this.computedReference == ComputingConcurrentHashMap.UNSET) {
                boolean interrupted = false;
                try {
                    synchronized (this) {
                        while (this.computedReference == ComputingConcurrentHashMap.UNSET) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                interrupted = true;
                            }
                        }
                    }
                } finally {
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            return this.computedReference.waitForValue();
        }

        public void clear(MapMakerInternalMap.ValueReference<K, V> newValue) {
            setValueReference(newValue);
        }

        /* access modifiers changed from: package-private */
        public V compute(K key, int hash) throws ExecutionException {
            try {
                V value = this.computingFunction.apply(key);
                setValueReference(new ComputedReference(value));
                return value;
            } catch (Throwable t) {
                setValueReference(new ComputationExceptionReference(t));
                throw new ExecutionException(t);
            }
        }

        /* access modifiers changed from: package-private */
        public void setValueReference(MapMakerInternalMap.ValueReference<K, V> valueReference) {
            synchronized (this) {
                if (this.computedReference == ComputingConcurrentHashMap.UNSET) {
                    this.computedReference = valueReference;
                    notifyAll();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Object writeReplace() {
        return new ComputingSerializationProxy(this.keyStrength, this.valueStrength, this.keyEquivalence, this.valueEquivalence, this.expireAfterWriteNanos, this.expireAfterAccessNanos, this.maximumSize, this.concurrencyLevel, this.removalListener, this, this.computingFunction);
    }

    static final class ComputingSerializationProxy<K, V> extends MapMakerInternalMap.AbstractSerializationProxy<K, V> {
        private static final long serialVersionUID = 4;
        final Function<? super K, ? extends V> computingFunction;

        ComputingSerializationProxy(MapMakerInternalMap.Strength keyStrength, MapMakerInternalMap.Strength valueStrength, Equivalence<Object> keyEquivalence, Equivalence<Object> valueEquivalence, long expireAfterWriteNanos, long expireAfterAccessNanos, int maximumSize, int concurrencyLevel, MapMaker.RemovalListener<? super K, ? super V> removalListener, ConcurrentMap<K, V> delegate, Function<? super K, ? extends V> computingFunction2) {
            super(keyStrength, valueStrength, keyEquivalence, valueEquivalence, expireAfterWriteNanos, expireAfterAccessNanos, maximumSize, concurrencyLevel, removalListener, delegate);
            this.computingFunction = computingFunction2;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            writeMapTo(out);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.delegate = readMapMaker(in).makeComputingMap(this.computingFunction);
            readEntries(in);
        }

        /* access modifiers changed from: package-private */
        public Object readResolve() {
            return this.delegate;
        }
    }
}
