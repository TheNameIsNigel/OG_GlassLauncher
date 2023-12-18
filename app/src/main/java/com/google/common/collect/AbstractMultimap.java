package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

@GwtCompatible
abstract class AbstractMultimap<K, V> implements Multimap<K, V> {
    private transient Map<K, Collection<V>> asMap;
    private transient Collection<Map.Entry<K, V>> entries;
    private transient Set<K> keySet;
    private transient Multiset<K> keys;
    private transient Collection<V> values;

    /* access modifiers changed from: package-private */
    public abstract Map<K, Collection<V>> createAsMap();

    /* access modifiers changed from: package-private */
    public abstract Iterator<Map.Entry<K, V>> entryIterator();

    AbstractMultimap() {
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean containsValue(@Nullable Object value) {
        for (Collection<V> collection : asMap().values()) {
            if (collection.contains(value)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
        Collection<V> collection = (Collection) asMap().get(key);
        if (collection != null) {
            return collection.contains(value);
        }
        return false;
    }

    public boolean remove(@Nullable Object key, @Nullable Object value) {
        Collection<V> collection = (Collection) asMap().get(key);
        if (collection != null) {
            return collection.remove(value);
        }
        return false;
    }

    public boolean put(@Nullable K key, @Nullable V value) {
        return get(key).add(value);
    }

    public boolean putAll(@Nullable K key, Iterable<? extends V> values2) {
        Preconditions.checkNotNull(values2);
        if (values2 instanceof Collection) {
            Collection<? extends V> valueCollection = (Collection) values2;
            if (!valueCollection.isEmpty()) {
                return get(key).addAll(valueCollection);
            }
            return false;
        }
        Iterator<? extends V> valueItr = values2.iterator();
        if (valueItr.hasNext()) {
            return Iterators.addAll(get(key), valueItr);
        }
        return false;
    }

    public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
        boolean changed = false;
        for (Map.Entry<? extends K, ? extends V> entry : multimap.entries()) {
            changed |= put(entry.getKey(), entry.getValue());
        }
        return changed;
    }

    public Collection<V> replaceValues(@Nullable K key, Iterable<? extends V> values2) {
        Preconditions.checkNotNull(values2);
        Collection<V> result = removeAll(key);
        putAll(key, values2);
        return result;
    }

    public Collection<Map.Entry<K, V>> entries() {
        Collection<Map.Entry<K, V>> result = this.entries;
        if (result != null) {
            return result;
        }
        Collection<Map.Entry<K, V>> result2 = createEntries();
        this.entries = result2;
        return result2;
    }

    /* access modifiers changed from: package-private */
    public Collection<Map.Entry<K, V>> createEntries() {
        if (this instanceof SetMultimap) {
            return new EntrySet(this, (EntrySet) null);
        }
        return new Entries(this, (Entries) null);
    }

    private class Entries extends Multimaps.Entries<K, V> {
        /* synthetic */ Entries(AbstractMultimap this$02, Entries entries) {
            this();
        }

        private Entries() {
        }

        /* access modifiers changed from: package-private */
        public Multimap<K, V> multimap() {
            return AbstractMultimap.this;
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            return AbstractMultimap.this.entryIterator();
        }
    }

    private class EntrySet extends AbstractMultimap<K, V>.Entries implements Set<Map.Entry<K, V>> {
        /* synthetic */ EntrySet(AbstractMultimap this$02, EntrySet entrySet) {
            this();
        }

        private EntrySet() {
            super(AbstractMultimap.this, (Entries) null);
        }

        public int hashCode() {
            return Sets.hashCodeImpl(this);
        }

        public boolean equals(@Nullable Object obj) {
            return Sets.equalsImpl(this, obj);
        }
    }

    public Set<K> keySet() {
        Set<K> result = this.keySet;
        if (result != null) {
            return result;
        }
        Set<K> result2 = createKeySet();
        this.keySet = result2;
        return result2;
    }

    /* access modifiers changed from: package-private */
    public Set<K> createKeySet() {
        return new Maps.KeySet(asMap());
    }

    public Multiset<K> keys() {
        Multiset<K> result = this.keys;
        if (result != null) {
            return result;
        }
        Multiset<K> result2 = createKeys();
        this.keys = result2;
        return result2;
    }

    /* access modifiers changed from: package-private */
    public Multiset<K> createKeys() {
        return new Multimaps.Keys(this);
    }

    public Collection<V> values() {
        Collection<V> result = this.values;
        if (result != null) {
            return result;
        }
        Collection<V> result2 = createValues();
        this.values = result2;
        return result2;
    }

    /* access modifiers changed from: package-private */
    public Collection<V> createValues() {
        return new Values();
    }

    class Values extends AbstractCollection<V> {
        Values() {
        }

        public Iterator<V> iterator() {
            return AbstractMultimap.this.valueIterator();
        }

        public int size() {
            return AbstractMultimap.this.size();
        }

        public boolean contains(@Nullable Object o) {
            return AbstractMultimap.this.containsValue(o);
        }

        public void clear() {
            AbstractMultimap.this.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public Iterator<V> valueIterator() {
        return Maps.valueIterator(entries().iterator());
    }

    public Map<K, Collection<V>> asMap() {
        Map<K, Collection<V>> result = this.asMap;
        if (result != null) {
            return result;
        }
        Map<K, Collection<V>> result2 = createAsMap();
        this.asMap = result2;
        return result2;
    }

    public boolean equals(@Nullable Object object) {
        return Multimaps.equalsImpl(this, object);
    }

    public int hashCode() {
        return asMap().hashCode();
    }

    public String toString() {
        return asMap().toString();
    }
}
