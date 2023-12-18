package android.support.v4.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class LruCache<K, V> {
    private int createCount;
    private int evictionCount;
    private int hitCount;
    private final LinkedHashMap<K, V> map;
    private int maxSize;
    private int missCount;
    private int putCount;
    private int size;

    public LruCache(int maxSize2) {
        if (maxSize2 <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize2;
        this.map = new LinkedHashMap<>(0, 0.75f, true);
    }

    public void resize(int maxSize2) {
        if (maxSize2 <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        synchronized (this) {
            this.maxSize = maxSize2;
        }
        trimToSize(maxSize2);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0024, code lost:
        r0 = create(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        if (r0 != null) goto L_0x002e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002a, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002e, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r4.createCount++;
        r1 = r4.map.put(r5, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003b, code lost:
        if (r1 == null) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x003d, code lost:
        r4.map.put(r5, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0042, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0043, code lost:
        if (r1 == null) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0045, code lost:
        entryRemoved(false, r5, r0, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0049, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r4.size += safeSizeOf(r5, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0057, code lost:
        trimToSize(r4.maxSize);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x005c, code lost:
        return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final V get(K r5) {
        /*
            r4 = this;
            r3 = 0
            if (r5 != 0) goto L_0x000c
            java.lang.NullPointerException r2 = new java.lang.NullPointerException
            java.lang.String r3 = "key == null"
            r2.<init>(r3)
            throw r2
        L_0x000c:
            monitor-enter(r4)
            java.util.LinkedHashMap<K, V> r2 = r4.map     // Catch:{ all -> 0x002b }
            java.lang.Object r1 = r2.get(r5)     // Catch:{ all -> 0x002b }
            if (r1 == 0) goto L_0x001d
            int r2 = r4.hitCount     // Catch:{ all -> 0x002b }
            int r2 = r2 + 1
            r4.hitCount = r2     // Catch:{ all -> 0x002b }
            monitor-exit(r4)
            return r1
        L_0x001d:
            int r2 = r4.missCount     // Catch:{ all -> 0x002b }
            int r2 = r2 + 1
            r4.missCount = r2     // Catch:{ all -> 0x002b }
            monitor-exit(r4)
            java.lang.Object r0 = r4.create(r5)
            if (r0 != 0) goto L_0x002e
            return r3
        L_0x002b:
            r2 = move-exception
            monitor-exit(r4)
            throw r2
        L_0x002e:
            monitor-enter(r4)
            int r2 = r4.createCount     // Catch:{ all -> 0x0054 }
            int r2 = r2 + 1
            r4.createCount = r2     // Catch:{ all -> 0x0054 }
            java.util.LinkedHashMap<K, V> r2 = r4.map     // Catch:{ all -> 0x0054 }
            java.lang.Object r1 = r2.put(r5, r0)     // Catch:{ all -> 0x0054 }
            if (r1 == 0) goto L_0x004a
            java.util.LinkedHashMap<K, V> r2 = r4.map     // Catch:{ all -> 0x0054 }
            r2.put(r5, r1)     // Catch:{ all -> 0x0054 }
        L_0x0042:
            monitor-exit(r4)
            if (r1 == 0) goto L_0x0057
            r2 = 0
            r4.entryRemoved(r2, r5, r0, r1)
            return r1
        L_0x004a:
            int r2 = r4.size     // Catch:{ all -> 0x0054 }
            int r3 = r4.safeSizeOf(r5, r0)     // Catch:{ all -> 0x0054 }
            int r2 = r2 + r3
            r4.size = r2     // Catch:{ all -> 0x0054 }
            goto L_0x0042
        L_0x0054:
            r2 = move-exception
            monitor-exit(r4)
            throw r2
        L_0x0057:
            int r2 = r4.maxSize
            r4.trimToSize(r2)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.util.LruCache.get(java.lang.Object):java.lang.Object");
    }

    public final V put(K key, V value) {
        V previous;
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }
        synchronized (this) {
            this.putCount++;
            this.size += safeSizeOf(key, value);
            previous = this.map.put(key, value);
            if (previous != null) {
                this.size -= safeSizeOf(key, previous);
            }
        }
        if (previous != null) {
            entryRemoved(false, key, previous, value);
        }
        trimToSize(this.maxSize);
        return previous;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0032, code lost:
        throw new java.lang.IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void trimToSize(int r7) {
        /*
            r6 = this;
        L_0x0000:
            monitor-enter(r6)
            int r3 = r6.size     // Catch:{ all -> 0x0033 }
            if (r3 < 0) goto L_0x0011
            java.util.LinkedHashMap<K, V> r3 = r6.map     // Catch:{ all -> 0x0033 }
            boolean r3 = r3.isEmpty()     // Catch:{ all -> 0x0033 }
            if (r3 == 0) goto L_0x0036
            int r3 = r6.size     // Catch:{ all -> 0x0033 }
            if (r3 == 0) goto L_0x0036
        L_0x0011:
            java.lang.IllegalStateException r3 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0033 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0033 }
            r4.<init>()     // Catch:{ all -> 0x0033 }
            java.lang.Class r5 = r6.getClass()     // Catch:{ all -> 0x0033 }
            java.lang.String r5 = r5.getName()     // Catch:{ all -> 0x0033 }
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ all -> 0x0033 }
            java.lang.String r5 = ".sizeOf() is reporting inconsistent results!"
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ all -> 0x0033 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0033 }
            r3.<init>(r4)     // Catch:{ all -> 0x0033 }
            throw r3     // Catch:{ all -> 0x0033 }
        L_0x0033:
            r3 = move-exception
            monitor-exit(r6)
            throw r3
        L_0x0036:
            int r3 = r6.size     // Catch:{ all -> 0x0033 }
            if (r3 <= r7) goto L_0x0042
            java.util.LinkedHashMap<K, V> r3 = r6.map     // Catch:{ all -> 0x0033 }
            boolean r3 = r3.isEmpty()     // Catch:{ all -> 0x0033 }
            if (r3 == 0) goto L_0x0044
        L_0x0042:
            monitor-exit(r6)
            return
        L_0x0044:
            java.util.LinkedHashMap<K, V> r3 = r6.map     // Catch:{ all -> 0x0033 }
            java.util.Set r3 = r3.entrySet()     // Catch:{ all -> 0x0033 }
            java.util.Iterator r3 = r3.iterator()     // Catch:{ all -> 0x0033 }
            java.lang.Object r1 = r3.next()     // Catch:{ all -> 0x0033 }
            java.util.Map$Entry r1 = (java.util.Map.Entry) r1     // Catch:{ all -> 0x0033 }
            java.lang.Object r0 = r1.getKey()     // Catch:{ all -> 0x0033 }
            java.lang.Object r2 = r1.getValue()     // Catch:{ all -> 0x0033 }
            java.util.LinkedHashMap<K, V> r3 = r6.map     // Catch:{ all -> 0x0033 }
            r3.remove(r0)     // Catch:{ all -> 0x0033 }
            int r3 = r6.size     // Catch:{ all -> 0x0033 }
            int r4 = r6.safeSizeOf(r0, r2)     // Catch:{ all -> 0x0033 }
            int r3 = r3 - r4
            r6.size = r3     // Catch:{ all -> 0x0033 }
            int r3 = r6.evictionCount     // Catch:{ all -> 0x0033 }
            int r3 = r3 + 1
            r6.evictionCount = r3     // Catch:{ all -> 0x0033 }
            monitor-exit(r6)
            r3 = 1
            r4 = 0
            r6.entryRemoved(r3, r0, r2, r4)
            goto L_0x0000
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.util.LruCache.trimToSize(int):void");
    }

    public final V remove(K key) {
        V previous;
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        synchronized (this) {
            previous = this.map.remove(key);
            if (previous != null) {
                this.size -= safeSizeOf(key, previous);
            }
        }
        if (previous != null) {
            entryRemoved(false, key, previous, (V) null);
        }
        return previous;
    }

    /* access modifiers changed from: protected */
    public void entryRemoved(boolean evicted, K k, V v, V v2) {
    }

    /* access modifiers changed from: protected */
    public V create(K k) {
        return null;
    }

    private int safeSizeOf(K key, V value) {
        int result = sizeOf(key, value);
        if (result >= 0) {
            return result;
        }
        throw new IllegalStateException("Negative size: " + key + "=" + value);
    }

    /* access modifiers changed from: protected */
    public int sizeOf(K k, V v) {
        return 1;
    }

    public final void evictAll() {
        trimToSize(-1);
    }

    public final synchronized int size() {
        return this.size;
    }

    public final synchronized int maxSize() {
        return this.maxSize;
    }

    public final synchronized int hitCount() {
        return this.hitCount;
    }

    public final synchronized int missCount() {
        return this.missCount;
    }

    public final synchronized int createCount() {
        return this.createCount;
    }

    public final synchronized int putCount() {
        return this.putCount;
    }

    public final synchronized int evictionCount() {
        return this.evictionCount;
    }

    public final synchronized Map<K, V> snapshot() {
        return new LinkedHashMap(this.map);
    }

    public final synchronized String toString() {
        int accesses;
        accesses = this.hitCount + this.missCount;
        return String.format(Locale.US, "LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", new Object[]{Integer.valueOf(this.maxSize), Integer.valueOf(this.hitCount), Integer.valueOf(this.missCount), Integer.valueOf(accesses != 0 ? (this.hitCount * 100) / accesses : 0)});
    }
}
