package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ImmutableMapEntry;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nullable;

@GwtCompatible(emulated = true, serializable = true)
class RegularImmutableBiMap<K, V> extends ImmutableBiMap<K, V> {
    static final double MAX_LOAD_FACTOR = 1.2d;
    /* access modifiers changed from: private */
    public final transient ImmutableMapEntry<K, V>[] entries;
    /* access modifiers changed from: private */
    public final transient int hashCode;
    private transient ImmutableBiMap<V, K> inverse;
    private final transient ImmutableMapEntry<K, V>[] keyTable;
    /* access modifiers changed from: private */
    public final transient int mask;
    /* access modifiers changed from: private */
    public final transient ImmutableMapEntry<K, V>[] valueTable;

    RegularImmutableBiMap(ImmutableMapEntry.TerminalEntry<?, ?>... entriesToAdd) {
        this(entriesToAdd.length, entriesToAdd);
    }

    RegularImmutableBiMap(int n, ImmutableMapEntry.TerminalEntry<?, ?>[] entriesToAdd) {
        ImmutableMapEntry<K, V> newEntry;
        int tableSize = Hashing.closedTableSize(n, MAX_LOAD_FACTOR);
        this.mask = tableSize - 1;
        ImmutableMapEntry<K, V>[] keyTable2 = createEntryArray(tableSize);
        ImmutableMapEntry<K, V>[] valueTable2 = createEntryArray(tableSize);
        ImmutableMapEntry<K, V>[] entries2 = createEntryArray(n);
        int hashCode2 = 0;
        for (int i = 0; i < n; i++) {
            ImmutableMapEntry.TerminalEntry<K, V> entry = entriesToAdd[i];
            K key = entry.getKey();
            V value = entry.getValue();
            int keyHash = key.hashCode();
            int valueHash = value.hashCode();
            int keyBucket = Hashing.smear(keyHash) & this.mask;
            int valueBucket = Hashing.smear(valueHash) & this.mask;
            ImmutableMapEntry<K, V> nextInKeyBucket = keyTable2[keyBucket];
            for (ImmutableMapEntry<K, V> keyEntry = nextInKeyBucket; keyEntry != null; keyEntry = keyEntry.getNextInKeyBucket()) {
                checkNoConflict(!key.equals(keyEntry.getKey()), "key", entry, keyEntry);
            }
            ImmutableMapEntry<K, V> nextInValueBucket = valueTable2[valueBucket];
            for (ImmutableMapEntry<K, V> valueEntry = nextInValueBucket; valueEntry != null; valueEntry = valueEntry.getNextInValueBucket()) {
                checkNoConflict(!value.equals(valueEntry.getValue()), "value", entry, valueEntry);
            }
            if (nextInKeyBucket == null && nextInValueBucket == null) {
                newEntry = entry;
            } else {
                newEntry = new NonTerminalBiMapEntry<>(entry, nextInKeyBucket, nextInValueBucket);
            }
            keyTable2[keyBucket] = newEntry;
            valueTable2[valueBucket] = newEntry;
            entries2[i] = newEntry;
            hashCode2 += keyHash ^ valueHash;
        }
        this.keyTable = keyTable2;
        this.valueTable = valueTable2;
        this.entries = entries2;
        this.hashCode = hashCode2;
    }

    RegularImmutableBiMap(Map.Entry<?, ?>[] entriesToAdd) {
        ImmutableMapEntry<K, V> newEntry;
        int n = entriesToAdd.length;
        int tableSize = Hashing.closedTableSize(n, MAX_LOAD_FACTOR);
        this.mask = tableSize - 1;
        ImmutableMapEntry<K, V>[] keyTable2 = createEntryArray(tableSize);
        ImmutableMapEntry<K, V>[] valueTable2 = createEntryArray(tableSize);
        ImmutableMapEntry<K, V>[] entries2 = createEntryArray(n);
        int hashCode2 = 0;
        for (int i = 0; i < n; i++) {
            Map.Entry<K, V> entry = entriesToAdd[i];
            K key = entry.getKey();
            V value = entry.getValue();
            CollectPreconditions.checkEntryNotNull(key, value);
            int keyHash = key.hashCode();
            int valueHash = value.hashCode();
            int keyBucket = Hashing.smear(keyHash) & this.mask;
            int valueBucket = Hashing.smear(valueHash) & this.mask;
            ImmutableMapEntry<K, V> nextInKeyBucket = keyTable2[keyBucket];
            for (ImmutableMapEntry<K, V> keyEntry = nextInKeyBucket; keyEntry != null; keyEntry = keyEntry.getNextInKeyBucket()) {
                checkNoConflict(!key.equals(keyEntry.getKey()), "key", entry, keyEntry);
            }
            ImmutableMapEntry<K, V> nextInValueBucket = valueTable2[valueBucket];
            for (ImmutableMapEntry<K, V> valueEntry = nextInValueBucket; valueEntry != null; valueEntry = valueEntry.getNextInValueBucket()) {
                checkNoConflict(!value.equals(valueEntry.getValue()), "value", entry, valueEntry);
            }
            if (nextInKeyBucket == null && nextInValueBucket == null) {
                newEntry = new ImmutableMapEntry.TerminalEntry<>(key, value);
            } else {
                newEntry = new NonTerminalBiMapEntry<>(key, value, nextInKeyBucket, nextInValueBucket);
            }
            keyTable2[keyBucket] = newEntry;
            valueTable2[valueBucket] = newEntry;
            entries2[i] = newEntry;
            hashCode2 += keyHash ^ valueHash;
        }
        this.keyTable = keyTable2;
        this.valueTable = valueTable2;
        this.entries = entries2;
        this.hashCode = hashCode2;
    }

    private static final class NonTerminalBiMapEntry<K, V> extends ImmutableMapEntry<K, V> {
        @Nullable
        private final ImmutableMapEntry<K, V> nextInKeyBucket;
        @Nullable
        private final ImmutableMapEntry<K, V> nextInValueBucket;

        NonTerminalBiMapEntry(K key, V value, @Nullable ImmutableMapEntry<K, V> nextInKeyBucket2, @Nullable ImmutableMapEntry<K, V> nextInValueBucket2) {
            super(key, value);
            this.nextInKeyBucket = nextInKeyBucket2;
            this.nextInValueBucket = nextInValueBucket2;
        }

        NonTerminalBiMapEntry(ImmutableMapEntry<K, V> contents, @Nullable ImmutableMapEntry<K, V> nextInKeyBucket2, @Nullable ImmutableMapEntry<K, V> nextInValueBucket2) {
            super(contents);
            this.nextInKeyBucket = nextInKeyBucket2;
            this.nextInValueBucket = nextInValueBucket2;
        }

        /* access modifiers changed from: package-private */
        @Nullable
        public ImmutableMapEntry<K, V> getNextInKeyBucket() {
            return this.nextInKeyBucket;
        }

        /* access modifiers changed from: package-private */
        @Nullable
        public ImmutableMapEntry<K, V> getNextInValueBucket() {
            return this.nextInValueBucket;
        }
    }

    private static <K, V> ImmutableMapEntry<K, V>[] createEntryArray(int length) {
        return new ImmutableMapEntry[length];
    }

    @Nullable
    public V get(@Nullable Object key) {
        if (key == null) {
            return null;
        }
        for (ImmutableMapEntry<K, V> entry = this.keyTable[Hashing.smear(key.hashCode()) & this.mask]; entry != null; entry = entry.getNextInKeyBucket()) {
            if (key.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return new ImmutableMapEntrySet<K, V>() {
            /* access modifiers changed from: package-private */
            public ImmutableMap<K, V> map() {
                return RegularImmutableBiMap.this;
            }

            public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
                return asList().iterator();
            }

            /* access modifiers changed from: package-private */
            public ImmutableList<Map.Entry<K, V>> createAsList() {
                return new RegularImmutableAsList(this, (Object[]) RegularImmutableBiMap.this.entries);
            }

            /* access modifiers changed from: package-private */
            public boolean isHashCodeFast() {
                return true;
            }

            public int hashCode() {
                return RegularImmutableBiMap.this.hashCode;
            }
        };
    }

    /* access modifiers changed from: package-private */
    public boolean isPartialView() {
        return false;
    }

    public int size() {
        return this.entries.length;
    }

    public ImmutableBiMap<V, K> inverse() {
        ImmutableBiMap<V, K> result = this.inverse;
        if (result != null) {
            return result;
        }
        ImmutableBiMap<V, K> result2 = new Inverse(this, (Inverse) null);
        this.inverse = result2;
        return result2;
    }

    private final class Inverse extends ImmutableBiMap<V, K> {
        /* synthetic */ Inverse(RegularImmutableBiMap this$02, Inverse inverse) {
            this();
        }

        private Inverse() {
        }

        public int size() {
            return inverse().size();
        }

        public ImmutableBiMap<K, V> inverse() {
            return RegularImmutableBiMap.this;
        }

        public K get(@Nullable Object value) {
            if (value == null) {
                return null;
            }
            for (ImmutableMapEntry<K, V> entry = RegularImmutableBiMap.this.valueTable[Hashing.smear(value.hashCode()) & RegularImmutableBiMap.this.mask]; entry != null; entry = entry.getNextInValueBucket()) {
                if (value.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public ImmutableSet<Map.Entry<V, K>> createEntrySet() {
            return new InverseEntrySet();
        }

        final class InverseEntrySet extends ImmutableMapEntrySet<V, K> {
            InverseEntrySet() {
            }

            /* access modifiers changed from: package-private */
            public ImmutableMap<V, K> map() {
                return Inverse.this;
            }

            /* access modifiers changed from: package-private */
            public boolean isHashCodeFast() {
                return true;
            }

            public int hashCode() {
                return RegularImmutableBiMap.this.hashCode;
            }

            public UnmodifiableIterator<Map.Entry<V, K>> iterator() {
                return asList().iterator();
            }

            /* access modifiers changed from: package-private */
            public ImmutableList<Map.Entry<V, K>> createAsList() {
                return new ImmutableAsList<Map.Entry<V, K>>() {
                    public Map.Entry<V, K> get(int index) {
                        Map.Entry<K, V> entry = RegularImmutableBiMap.this.entries[index];
                        return Maps.immutableEntry(entry.getValue(), entry.getKey());
                    }

                    /* access modifiers changed from: package-private */
                    public ImmutableCollection<Map.Entry<V, K>> delegateCollection() {
                        return InverseEntrySet.this;
                    }
                };
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isPartialView() {
            return false;
        }

        /* access modifiers changed from: package-private */
        public Object writeReplace() {
            return new InverseSerializedForm(RegularImmutableBiMap.this);
        }
    }

    private static class InverseSerializedForm<K, V> implements Serializable {
        private static final long serialVersionUID = 1;
        private final ImmutableBiMap<K, V> forward;

        InverseSerializedForm(ImmutableBiMap<K, V> forward2) {
            this.forward = forward2;
        }

        /* access modifiers changed from: package-private */
        public Object readResolve() {
            return this.forward.inverse();
        }
    }
}
