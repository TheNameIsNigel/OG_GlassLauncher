package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;

@GwtCompatible(emulated = true)
public final class HashBiMap<K, V> extends AbstractMap<K, V> implements BiMap<K, V>, Serializable {
    private static final double LOAD_FACTOR = 1.0d;
    @GwtIncompatible("Not needed in emulated source")
    private static final long serialVersionUID = 0;
    /* access modifiers changed from: private */
    public transient BiEntry<K, V>[] hashTableKToV;
    private transient BiEntry<K, V>[] hashTableVToK;
    private transient BiMap<V, K> inverse;
    private transient int mask;
    /* access modifiers changed from: private */
    public transient int modCount;
    /* access modifiers changed from: private */
    public transient int size;

    public static <K, V> HashBiMap<K, V> create() {
        return create(16);
    }

    public static <K, V> HashBiMap<K, V> create(int expectedSize) {
        return new HashBiMap<>(expectedSize);
    }

    public static <K, V> HashBiMap<K, V> create(Map<? extends K, ? extends V> map) {
        HashBiMap<K, V> bimap = create(map.size());
        bimap.putAll(map);
        return bimap;
    }

    private static final class BiEntry<K, V> extends ImmutableEntry<K, V> {
        final int keyHash;
        @Nullable
        BiEntry<K, V> nextInKToVBucket;
        @Nullable
        BiEntry<K, V> nextInVToKBucket;
        final int valueHash;

        BiEntry(K key, int keyHash2, V value, int valueHash2) {
            super(key, value);
            this.keyHash = keyHash2;
            this.valueHash = valueHash2;
        }
    }

    private HashBiMap(int expectedSize) {
        init(expectedSize);
    }

    private void init(int expectedSize) {
        CollectPreconditions.checkNonnegative(expectedSize, "expectedSize");
        int tableSize = Hashing.closedTableSize(expectedSize, LOAD_FACTOR);
        this.hashTableKToV = createTable(tableSize);
        this.hashTableVToK = createTable(tableSize);
        this.mask = tableSize - 1;
        this.modCount = 0;
        this.size = 0;
    }

    /* access modifiers changed from: private */
    public void delete(BiEntry<K, V> entry) {
        int keyBucket = entry.keyHash & this.mask;
        BiEntry<K, V> prevBucketEntry = null;
        for (BiEntry<K, V> bucketEntry = this.hashTableKToV[keyBucket]; bucketEntry != entry; bucketEntry = bucketEntry.nextInKToVBucket) {
            prevBucketEntry = bucketEntry;
        }
        if (prevBucketEntry == null) {
            this.hashTableKToV[keyBucket] = entry.nextInKToVBucket;
        } else {
            prevBucketEntry.nextInKToVBucket = entry.nextInKToVBucket;
        }
        int valueBucket = entry.valueHash & this.mask;
        BiEntry<K, V> prevBucketEntry2 = null;
        for (BiEntry<K, V> bucketEntry2 = this.hashTableVToK[valueBucket]; bucketEntry2 != entry; bucketEntry2 = bucketEntry2.nextInVToKBucket) {
            prevBucketEntry2 = bucketEntry2;
        }
        if (prevBucketEntry2 == null) {
            this.hashTableVToK[valueBucket] = entry.nextInVToKBucket;
        } else {
            prevBucketEntry2.nextInVToKBucket = entry.nextInVToKBucket;
        }
        this.size--;
        this.modCount++;
    }

    /* access modifiers changed from: private */
    public void insert(BiEntry<K, V> entry) {
        int keyBucket = entry.keyHash & this.mask;
        entry.nextInKToVBucket = this.hashTableKToV[keyBucket];
        this.hashTableKToV[keyBucket] = entry;
        int valueBucket = entry.valueHash & this.mask;
        entry.nextInVToKBucket = this.hashTableVToK[valueBucket];
        this.hashTableVToK[valueBucket] = entry;
        this.size++;
        this.modCount++;
    }

    /* access modifiers changed from: private */
    public static int hash(@Nullable Object o) {
        return Hashing.smear(o == null ? 0 : o.hashCode());
    }

    /* access modifiers changed from: private */
    public BiEntry<K, V> seekByKey(@Nullable Object key, int keyHash) {
        for (BiEntry<K, V> entry = this.hashTableKToV[this.mask & keyHash]; entry != null; entry = entry.nextInKToVBucket) {
            if (keyHash == entry.keyHash && Objects.equal(key, entry.key)) {
                return entry;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public BiEntry<K, V> seekByValue(@Nullable Object value, int valueHash) {
        for (BiEntry<K, V> entry = this.hashTableVToK[this.mask & valueHash]; entry != null; entry = entry.nextInVToKBucket) {
            if (valueHash == entry.valueHash && Objects.equal(value, entry.value)) {
                return entry;
            }
        }
        return null;
    }

    public boolean containsKey(@Nullable Object key) {
        return seekByKey(key, hash(key)) != null;
    }

    public boolean containsValue(@Nullable Object value) {
        return seekByValue(value, hash(value)) != null;
    }

    @Nullable
    public V get(@Nullable Object key) {
        BiEntry<K, V> entry = seekByKey(key, hash(key));
        if (entry == null) {
            return null;
        }
        return entry.value;
    }

    public V put(@Nullable K key, @Nullable V value) {
        return put(key, value, false);
    }

    public V forcePut(@Nullable K key, @Nullable V value) {
        return put(key, value, true);
    }

    private V put(@Nullable K key, @Nullable V value, boolean force) {
        int keyHash = hash(key);
        int valueHash = hash(value);
        BiEntry<K, V> oldEntryForKey = seekByKey(key, keyHash);
        if (oldEntryForKey != null && valueHash == oldEntryForKey.valueHash && Objects.equal(value, oldEntryForKey.value)) {
            return value;
        }
        BiEntry<K, V> oldEntryForValue = seekByValue(value, valueHash);
        if (oldEntryForValue != null) {
            if (force) {
                delete(oldEntryForValue);
            } else {
                throw new IllegalArgumentException("value already present: " + value);
            }
        }
        if (oldEntryForKey != null) {
            delete(oldEntryForKey);
        }
        insert(new BiEntry<>(key, keyHash, value, valueHash));
        rehashIfNecessary();
        if (oldEntryForKey == null) {
            return null;
        }
        return oldEntryForKey.value;
    }

    /* access modifiers changed from: private */
    @Nullable
    public K putInverse(@Nullable V value, @Nullable K key, boolean force) {
        int valueHash = hash(value);
        int keyHash = hash(key);
        BiEntry<K, V> oldEntryForValue = seekByValue(value, valueHash);
        if (oldEntryForValue != null && keyHash == oldEntryForValue.keyHash && Objects.equal(key, oldEntryForValue.key)) {
            return key;
        }
        BiEntry<K, V> oldEntryForKey = seekByKey(key, keyHash);
        if (oldEntryForKey != null) {
            if (force) {
                delete(oldEntryForKey);
            } else {
                throw new IllegalArgumentException("value already present: " + key);
            }
        }
        if (oldEntryForValue != null) {
            delete(oldEntryForValue);
        }
        insert(new BiEntry<>(key, keyHash, value, valueHash));
        rehashIfNecessary();
        if (oldEntryForValue == null) {
            return null;
        }
        return oldEntryForValue.key;
    }

    private void rehashIfNecessary() {
        BiEntry<K, V>[] oldKToV = this.hashTableKToV;
        if (Hashing.needsResizing(this.size, oldKToV.length, LOAD_FACTOR)) {
            int newTableSize = oldKToV.length * 2;
            this.hashTableKToV = createTable(newTableSize);
            this.hashTableVToK = createTable(newTableSize);
            this.mask = newTableSize - 1;
            this.size = 0;
            for (BiEntry<K, V> entry : oldKToV) {
                while (entry != null) {
                    BiEntry<K, V> nextEntry = entry.nextInKToVBucket;
                    insert(entry);
                    entry = nextEntry;
                }
            }
            this.modCount++;
        }
    }

    private BiEntry<K, V>[] createTable(int length) {
        return new BiEntry[length];
    }

    public V remove(@Nullable Object key) {
        BiEntry<K, V> entry = seekByKey(key, hash(key));
        if (entry == null) {
            return null;
        }
        delete(entry);
        return entry.value;
    }

    public void clear() {
        this.size = 0;
        Arrays.fill(this.hashTableKToV, (Object) null);
        Arrays.fill(this.hashTableVToK, (Object) null);
        this.modCount++;
    }

    public int size() {
        return this.size;
    }

    abstract class Itr<T> implements Iterator<T> {
        int expectedModCount = HashBiMap.this.modCount;
        BiEntry<K, V> next = null;
        int nextBucket = 0;
        BiEntry<K, V> toRemove = null;

        /* access modifiers changed from: package-private */
        public abstract T output(BiEntry<K, V> biEntry);

        Itr() {
        }

        private void checkForConcurrentModification() {
            if (HashBiMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        public boolean hasNext() {
            checkForConcurrentModification();
            if (this.next != null) {
                return true;
            }
            while (this.nextBucket < HashBiMap.this.hashTableKToV.length) {
                if (HashBiMap.this.hashTableKToV[this.nextBucket] != null) {
                    BiEntry<K, V>[] r0 = HashBiMap.this.hashTableKToV;
                    int i = this.nextBucket;
                    this.nextBucket = i + 1;
                    this.next = r0[i];
                    return true;
                }
                this.nextBucket++;
            }
            return false;
        }

        public T next() {
            checkForConcurrentModification();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            BiEntry<K, V> entry = this.next;
            this.next = entry.nextInKToVBucket;
            this.toRemove = entry;
            return output(entry);
        }

        public void remove() {
            checkForConcurrentModification();
            CollectPreconditions.checkRemove(this.toRemove != null);
            HashBiMap.this.delete(this.toRemove);
            this.expectedModCount = HashBiMap.this.modCount;
            this.toRemove = null;
        }
    }

    public Set<K> keySet() {
        return new KeySet();
    }

    private final class KeySet extends Maps.KeySet<K, V> {
        KeySet() {
            super(HashBiMap.this);
        }

        public Iterator<K> iterator() {
            return new HashBiMap<K, V>.Itr<K>(HashBiMap.this) {
                /* access modifiers changed from: package-private */
                public K output(BiEntry<K, V> entry) {
                    return entry.key;
                }
            };
        }

        public boolean remove(@Nullable Object o) {
            BiEntry<K, V> entry = HashBiMap.this.seekByKey(o, HashBiMap.hash(o));
            if (entry == null) {
                return false;
            }
            HashBiMap.this.delete(entry);
            return true;
        }
    }

    public Set<V> values() {
        return inverse().keySet();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return new EntrySet(this, (EntrySet) null);
    }

    private final class EntrySet extends Maps.EntrySet<K, V> {
        /* synthetic */ EntrySet(HashBiMap this$02, EntrySet entrySet) {
            this();
        }

        private EntrySet() {
        }

        /* access modifiers changed from: package-private */
        public Map<K, V> map() {
            return HashBiMap.this;
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            return new HashBiMap<K, V>.Itr<Map.Entry<K, V>>(HashBiMap.this) {
                /* access modifiers changed from: package-private */
                public Map.Entry<K, V> output(BiEntry<K, V> entry) {
                    return new MapEntry(entry);
                }

                /* renamed from: com.google.common.collect.HashBiMap$EntrySet$1$MapEntry */
                class MapEntry extends AbstractMapEntry<K, V> {
                    BiEntry<K, V> delegate;

                    MapEntry(BiEntry<K, V> entry) {
                        this.delegate = entry;
                    }

                    public K getKey() {
                        return this.delegate.key;
                    }

                    public V getValue() {
                        return this.delegate.value;
                    }

                    public V setValue(V value) {
                        boolean z;
                        V oldValue = this.delegate.value;
                        int valueHash = HashBiMap.hash(value);
                        if (valueHash == this.delegate.valueHash && Objects.equal(value, oldValue)) {
                            return value;
                        }
                        if (HashBiMap.this.seekByValue(value, valueHash) == null) {
                            z = true;
                        } else {
                            z = false;
                        }
                        Preconditions.checkArgument(z, "value already present: %s", value);
                        HashBiMap.this.delete(this.delegate);
                        BiEntry<K, V> newEntry = new BiEntry<>(this.delegate.key, this.delegate.keyHash, value, valueHash);
                        HashBiMap.this.insert(newEntry);
                        AnonymousClass1.this.expectedModCount = HashBiMap.this.modCount;
                        if (AnonymousClass1.this.toRemove == this.delegate) {
                            AnonymousClass1.this.toRemove = newEntry;
                        }
                        this.delegate = newEntry;
                        return oldValue;
                    }
                }
            };
        }
    }

    public BiMap<V, K> inverse() {
        if (this.inverse != null) {
            return this.inverse;
        }
        Inverse inverse2 = new Inverse(this, (Inverse) null);
        this.inverse = inverse2;
        return inverse2;
    }

    private final class Inverse extends AbstractMap<V, K> implements BiMap<V, K>, Serializable {
        /* synthetic */ Inverse(HashBiMap this$02, Inverse inverse) {
            this();
        }

        private Inverse() {
        }

        /* access modifiers changed from: package-private */
        public BiMap<K, V> forward() {
            return HashBiMap.this;
        }

        public int size() {
            return HashBiMap.this.size;
        }

        public void clear() {
            forward().clear();
        }

        public boolean containsKey(@Nullable Object value) {
            return forward().containsValue(value);
        }

        public K get(@Nullable Object value) {
            BiEntry<K, V> entry = HashBiMap.this.seekByValue(value, HashBiMap.hash(value));
            if (entry == null) {
                return null;
            }
            return entry.key;
        }

        public K put(@Nullable V value, @Nullable K key) {
            return HashBiMap.this.putInverse(value, key, false);
        }

        public K forcePut(@Nullable V value, @Nullable K key) {
            return HashBiMap.this.putInverse(value, key, true);
        }

        public K remove(@Nullable Object value) {
            BiEntry<K, V> entry = HashBiMap.this.seekByValue(value, HashBiMap.hash(value));
            if (entry == null) {
                return null;
            }
            HashBiMap.this.delete(entry);
            return entry.key;
        }

        public BiMap<K, V> inverse() {
            return forward();
        }

        public Set<V> keySet() {
            return new InverseKeySet();
        }

        private final class InverseKeySet extends Maps.KeySet<V, K> {
            InverseKeySet() {
                super(Inverse.this);
            }

            public boolean remove(@Nullable Object o) {
                BiEntry<K, V> entry = HashBiMap.this.seekByValue(o, HashBiMap.hash(o));
                if (entry == null) {
                    return false;
                }
                HashBiMap.this.delete(entry);
                return true;
            }

            public Iterator<V> iterator() {
                return new HashBiMap<K, V>.Itr<V>(HashBiMap.this) {
                    /* access modifiers changed from: package-private */
                    public V output(BiEntry<K, V> entry) {
                        return entry.value;
                    }
                };
            }
        }

        public Set<K> values() {
            return forward().keySet();
        }

        public Set<Map.Entry<V, K>> entrySet() {
            return new Maps.EntrySet<V, K>() {
                /* access modifiers changed from: package-private */
                public Map<V, K> map() {
                    return Inverse.this;
                }

                public Iterator<Map.Entry<V, K>> iterator() {
                    return new HashBiMap<K, V>.Itr<Map.Entry<V, K>>(HashBiMap.this) {
                        /* access modifiers changed from: package-private */
                        public Map.Entry<V, K> output(BiEntry<K, V> entry) {
                            return new InverseEntry(entry);
                        }

                        /* renamed from: com.google.common.collect.HashBiMap$Inverse$1$1$InverseEntry */
                        class InverseEntry extends AbstractMapEntry<V, K> {
                            BiEntry<K, V> delegate;

                            InverseEntry(BiEntry<K, V> entry) {
                                this.delegate = entry;
                            }

                            public V getKey() {
                                return this.delegate.value;
                            }

                            public K getValue() {
                                return this.delegate.key;
                            }

                            public K setValue(K key) {
                                boolean z;
                                K oldKey = this.delegate.key;
                                int keyHash = HashBiMap.hash(key);
                                if (keyHash == this.delegate.keyHash && Objects.equal(key, oldKey)) {
                                    return key;
                                }
                                if (HashBiMap.this.seekByKey(key, keyHash) == null) {
                                    z = true;
                                } else {
                                    z = false;
                                }
                                Preconditions.checkArgument(z, "value already present: %s", key);
                                HashBiMap.this.delete(this.delegate);
                                HashBiMap.this.insert(new BiEntry<>(key, keyHash, this.delegate.value, this.delegate.valueHash));
                                AnonymousClass1.this.expectedModCount = HashBiMap.this.modCount;
                                return oldKey;
                            }
                        }
                    };
                }
            };
        }

        /* access modifiers changed from: package-private */
        public Object writeReplace() {
            return new InverseSerializedForm(HashBiMap.this);
        }
    }

    private static final class InverseSerializedForm<K, V> implements Serializable {
        private final HashBiMap<K, V> bimap;

        InverseSerializedForm(HashBiMap<K, V> bimap2) {
            this.bimap = bimap2;
        }

        /* access modifiers changed from: package-private */
        public Object readResolve() {
            return this.bimap.inverse();
        }
    }

    @GwtIncompatible("java.io.ObjectOutputStream")
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        Serialization.writeMap(this, stream);
    }

    @GwtIncompatible("java.io.ObjectInputStream")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        int size2 = Serialization.readCount(stream);
        init(size2);
        Serialization.populateMap(this, stream, size2);
    }
}
