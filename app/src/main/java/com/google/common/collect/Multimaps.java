package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;

@GwtCompatible(emulated = true)
public final class Multimaps {
    private Multimaps() {
    }

    public static <K, V> Multimap<K, V> newMultimap(Map<K, Collection<V>> map, Supplier<? extends Collection<V>> factory) {
        return new CustomMultimap(map, factory);
    }

    private static class CustomMultimap<K, V> extends AbstractMapBasedMultimap<K, V> {
        @GwtIncompatible("java serialization not supported")
        private static final long serialVersionUID = 0;
        transient Supplier<? extends Collection<V>> factory;

        CustomMultimap(Map<K, Collection<V>> map, Supplier<? extends Collection<V>> factory2) {
            super(map);
            this.factory = (Supplier) Preconditions.checkNotNull(factory2);
        }

        /* access modifiers changed from: protected */
        public Collection<V> createCollection() {
            return (Collection) this.factory.get();
        }

        @GwtIncompatible("java.io.ObjectOutputStream")
        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.defaultWriteObject();
            stream.writeObject(this.factory);
            stream.writeObject(backingMap());
        }

        @GwtIncompatible("java.io.ObjectInputStream")
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            this.factory = (Supplier) stream.readObject();
            setMap((Map) stream.readObject());
        }
    }

    public static <K, V> ListMultimap<K, V> newListMultimap(Map<K, Collection<V>> map, Supplier<? extends List<V>> factory) {
        return new CustomListMultimap(map, factory);
    }

    private static class CustomListMultimap<K, V> extends AbstractListMultimap<K, V> {
        @GwtIncompatible("java serialization not supported")
        private static final long serialVersionUID = 0;
        transient Supplier<? extends List<V>> factory;

        CustomListMultimap(Map<K, Collection<V>> map, Supplier<? extends List<V>> factory2) {
            super(map);
            this.factory = (Supplier) Preconditions.checkNotNull(factory2);
        }

        /* access modifiers changed from: protected */
        public List<V> createCollection() {
            return (List) this.factory.get();
        }

        @GwtIncompatible("java.io.ObjectOutputStream")
        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.defaultWriteObject();
            stream.writeObject(this.factory);
            stream.writeObject(backingMap());
        }

        @GwtIncompatible("java.io.ObjectInputStream")
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            this.factory = (Supplier) stream.readObject();
            setMap((Map) stream.readObject());
        }
    }

    public static <K, V> SetMultimap<K, V> newSetMultimap(Map<K, Collection<V>> map, Supplier<? extends Set<V>> factory) {
        return new CustomSetMultimap(map, factory);
    }

    private static class CustomSetMultimap<K, V> extends AbstractSetMultimap<K, V> {
        @GwtIncompatible("not needed in emulated source")
        private static final long serialVersionUID = 0;
        transient Supplier<? extends Set<V>> factory;

        CustomSetMultimap(Map<K, Collection<V>> map, Supplier<? extends Set<V>> factory2) {
            super(map);
            this.factory = (Supplier) Preconditions.checkNotNull(factory2);
        }

        /* access modifiers changed from: protected */
        public Set<V> createCollection() {
            return (Set) this.factory.get();
        }

        @GwtIncompatible("java.io.ObjectOutputStream")
        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.defaultWriteObject();
            stream.writeObject(this.factory);
            stream.writeObject(backingMap());
        }

        @GwtIncompatible("java.io.ObjectInputStream")
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            this.factory = (Supplier) stream.readObject();
            setMap((Map) stream.readObject());
        }
    }

    public static <K, V> SortedSetMultimap<K, V> newSortedSetMultimap(Map<K, Collection<V>> map, Supplier<? extends SortedSet<V>> factory) {
        return new CustomSortedSetMultimap(map, factory);
    }

    private static class CustomSortedSetMultimap<K, V> extends AbstractSortedSetMultimap<K, V> {
        @GwtIncompatible("not needed in emulated source")
        private static final long serialVersionUID = 0;
        transient Supplier<? extends SortedSet<V>> factory;
        transient Comparator<? super V> valueComparator;

        CustomSortedSetMultimap(Map<K, Collection<V>> map, Supplier<? extends SortedSet<V>> factory2) {
            super(map);
            this.factory = (Supplier) Preconditions.checkNotNull(factory2);
            this.valueComparator = ((SortedSet) factory2.get()).comparator();
        }

        /* access modifiers changed from: protected */
        public SortedSet<V> createCollection() {
            return (SortedSet) this.factory.get();
        }

        public Comparator<? super V> valueComparator() {
            return this.valueComparator;
        }

        @GwtIncompatible("java.io.ObjectOutputStream")
        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.defaultWriteObject();
            stream.writeObject(this.factory);
            stream.writeObject(backingMap());
        }

        @GwtIncompatible("java.io.ObjectInputStream")
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            this.factory = (Supplier) stream.readObject();
            this.valueComparator = ((SortedSet) this.factory.get()).comparator();
            setMap((Map) stream.readObject());
        }
    }

    public static <K, V, M extends Multimap<K, V>> M invertFrom(Multimap<? extends V, ? extends K> source, M dest) {
        Preconditions.checkNotNull(dest);
        for (Map.Entry<? extends V, ? extends K> entry : source.entries()) {
            dest.put(entry.getValue(), entry.getKey());
        }
        return dest;
    }

    public static <K, V> Multimap<K, V> synchronizedMultimap(Multimap<K, V> multimap) {
        return Synchronized.multimap(multimap, (Object) null);
    }

    public static <K, V> Multimap<K, V> unmodifiableMultimap(Multimap<K, V> delegate) {
        if ((delegate instanceof UnmodifiableMultimap) || (delegate instanceof ImmutableMultimap)) {
            return delegate;
        }
        return new UnmodifiableMultimap(delegate);
    }

    @Deprecated
    public static <K, V> Multimap<K, V> unmodifiableMultimap(ImmutableMultimap<K, V> delegate) {
        return (Multimap) Preconditions.checkNotNull(delegate);
    }

    private static class UnmodifiableMultimap<K, V> extends ForwardingMultimap<K, V> implements Serializable {
        private static final long serialVersionUID = 0;
        final Multimap<K, V> delegate;
        transient Collection<Map.Entry<K, V>> entries;
        transient Set<K> keySet;
        transient Multiset<K> keys;
        transient Map<K, Collection<V>> map;
        transient Collection<V> values;

        UnmodifiableMultimap(Multimap<K, V> delegate2) {
            this.delegate = (Multimap) Preconditions.checkNotNull(delegate2);
        }

        /* access modifiers changed from: protected */
        public Multimap<K, V> delegate() {
            return this.delegate;
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public Map<K, Collection<V>> asMap() {
            Map<K, Collection<V>> result = this.map;
            if (result != null) {
                return result;
            }
            Map<K, Collection<V>> result2 = Collections.unmodifiableMap(Maps.transformValues(this.delegate.asMap(), new Function<Collection<V>, Collection<V>>() {
                public Collection<V> apply(Collection<V> collection) {
                    return Multimaps.unmodifiableValueCollection(collection);
                }
            }));
            this.map = result2;
            return result2;
        }

        public Collection<Map.Entry<K, V>> entries() {
            Collection<Map.Entry<K, V>> result = this.entries;
            if (result != null) {
                return result;
            }
            Collection<Map.Entry<K, V>> result2 = Multimaps.unmodifiableEntries(this.delegate.entries());
            this.entries = result2;
            return result2;
        }

        public Collection<V> get(K key) {
            return Multimaps.unmodifiableValueCollection(this.delegate.get(key));
        }

        public Multiset<K> keys() {
            Multiset<K> result = this.keys;
            if (result != null) {
                return result;
            }
            Multiset<K> result2 = Multisets.unmodifiableMultiset(this.delegate.keys());
            this.keys = result2;
            return result2;
        }

        public Set<K> keySet() {
            Set<K> result = this.keySet;
            if (result != null) {
                return result;
            }
            Set<K> result2 = Collections.unmodifiableSet(this.delegate.keySet());
            this.keySet = result2;
            return result2;
        }

        public boolean put(K k, V v) {
            throw new UnsupportedOperationException();
        }

        public boolean putAll(K k, Iterable<? extends V> iterable) {
            throw new UnsupportedOperationException();
        }

        public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        public Collection<V> removeAll(Object key) {
            throw new UnsupportedOperationException();
        }

        public Collection<V> replaceValues(K k, Iterable<? extends V> iterable) {
            throw new UnsupportedOperationException();
        }

        public Collection<V> values() {
            Collection<V> result = this.values;
            if (result != null) {
                return result;
            }
            Collection<V> result2 = Collections.unmodifiableCollection(this.delegate.values());
            this.values = result2;
            return result2;
        }
    }

    private static class UnmodifiableListMultimap<K, V> extends UnmodifiableMultimap<K, V> implements ListMultimap<K, V> {
        private static final long serialVersionUID = 0;

        UnmodifiableListMultimap(ListMultimap<K, V> delegate) {
            super(delegate);
        }

        public ListMultimap<K, V> delegate() {
            return (ListMultimap) super.delegate();
        }

        public List<V> get(K key) {
            return Collections.unmodifiableList(delegate().get(key));
        }

        public List<V> removeAll(Object key) {
            throw new UnsupportedOperationException();
        }

        public List<V> replaceValues(K k, Iterable<? extends V> iterable) {
            throw new UnsupportedOperationException();
        }
    }

    private static class UnmodifiableSetMultimap<K, V> extends UnmodifiableMultimap<K, V> implements SetMultimap<K, V> {
        private static final long serialVersionUID = 0;

        UnmodifiableSetMultimap(SetMultimap<K, V> delegate) {
            super(delegate);
        }

        public SetMultimap<K, V> delegate() {
            return (SetMultimap) super.delegate();
        }

        public Set<V> get(K key) {
            return Collections.unmodifiableSet(delegate().get(key));
        }

        public Set<Map.Entry<K, V>> entries() {
            return Maps.unmodifiableEntrySet(delegate().entries());
        }

        public Set<V> removeAll(Object key) {
            throw new UnsupportedOperationException();
        }

        public Set<V> replaceValues(K k, Iterable<? extends V> iterable) {
            throw new UnsupportedOperationException();
        }
    }

    private static class UnmodifiableSortedSetMultimap<K, V> extends UnmodifiableSetMultimap<K, V> implements SortedSetMultimap<K, V> {
        private static final long serialVersionUID = 0;

        UnmodifiableSortedSetMultimap(SortedSetMultimap<K, V> delegate) {
            super(delegate);
        }

        public SortedSetMultimap<K, V> delegate() {
            return (SortedSetMultimap) super.delegate();
        }

        public SortedSet<V> get(K key) {
            return Collections.unmodifiableSortedSet(delegate().get(key));
        }

        public SortedSet<V> removeAll(Object key) {
            throw new UnsupportedOperationException();
        }

        public SortedSet<V> replaceValues(K k, Iterable<? extends V> iterable) {
            throw new UnsupportedOperationException();
        }

        public Comparator<? super V> valueComparator() {
            return delegate().valueComparator();
        }
    }

    public static <K, V> SetMultimap<K, V> synchronizedSetMultimap(SetMultimap<K, V> multimap) {
        return Synchronized.setMultimap(multimap, (Object) null);
    }

    public static <K, V> SetMultimap<K, V> unmodifiableSetMultimap(SetMultimap<K, V> delegate) {
        if ((delegate instanceof UnmodifiableSetMultimap) || (delegate instanceof ImmutableSetMultimap)) {
            return delegate;
        }
        return new UnmodifiableSetMultimap(delegate);
    }

    @Deprecated
    public static <K, V> SetMultimap<K, V> unmodifiableSetMultimap(ImmutableSetMultimap<K, V> delegate) {
        return (SetMultimap) Preconditions.checkNotNull(delegate);
    }

    public static <K, V> SortedSetMultimap<K, V> synchronizedSortedSetMultimap(SortedSetMultimap<K, V> multimap) {
        return Synchronized.sortedSetMultimap(multimap, (Object) null);
    }

    public static <K, V> SortedSetMultimap<K, V> unmodifiableSortedSetMultimap(SortedSetMultimap<K, V> delegate) {
        if (delegate instanceof UnmodifiableSortedSetMultimap) {
            return delegate;
        }
        return new UnmodifiableSortedSetMultimap(delegate);
    }

    public static <K, V> ListMultimap<K, V> synchronizedListMultimap(ListMultimap<K, V> multimap) {
        return Synchronized.listMultimap(multimap, (Object) null);
    }

    public static <K, V> ListMultimap<K, V> unmodifiableListMultimap(ListMultimap<K, V> delegate) {
        if ((delegate instanceof UnmodifiableListMultimap) || (delegate instanceof ImmutableListMultimap)) {
            return delegate;
        }
        return new UnmodifiableListMultimap(delegate);
    }

    @Deprecated
    public static <K, V> ListMultimap<K, V> unmodifiableListMultimap(ImmutableListMultimap<K, V> delegate) {
        return (ListMultimap) Preconditions.checkNotNull(delegate);
    }

    /* access modifiers changed from: private */
    public static <V> Collection<V> unmodifiableValueCollection(Collection<V> collection) {
        if (collection instanceof SortedSet) {
            return Collections.unmodifiableSortedSet((SortedSet) collection);
        }
        if (collection instanceof Set) {
            return Collections.unmodifiableSet((Set) collection);
        }
        if (collection instanceof List) {
            return Collections.unmodifiableList((List) collection);
        }
        return Collections.unmodifiableCollection(collection);
    }

    /* access modifiers changed from: private */
    public static <K, V> Collection<Map.Entry<K, V>> unmodifiableEntries(Collection<Map.Entry<K, V>> entries) {
        if (entries instanceof Set) {
            return Maps.unmodifiableEntrySet((Set) entries);
        }
        return new Maps.UnmodifiableEntries(Collections.unmodifiableCollection(entries));
    }

    @Beta
    public static <K, V> Map<K, List<V>> asMap(ListMultimap<K, V> multimap) {
        return multimap.asMap();
    }

    @Beta
    public static <K, V> Map<K, Set<V>> asMap(SetMultimap<K, V> multimap) {
        return multimap.asMap();
    }

    @Beta
    public static <K, V> Map<K, SortedSet<V>> asMap(SortedSetMultimap<K, V> multimap) {
        return multimap.asMap();
    }

    @Beta
    public static <K, V> Map<K, Collection<V>> asMap(Multimap<K, V> multimap) {
        return multimap.asMap();
    }

    public static <K, V> SetMultimap<K, V> forMap(Map<K, V> map) {
        return new MapMultimap(map);
    }

    private static class MapMultimap<K, V> extends AbstractMultimap<K, V> implements SetMultimap<K, V>, Serializable {
        private static final long serialVersionUID = 7845222491160860175L;
        final Map<K, V> map;

        MapMultimap(Map<K, V> map2) {
            this.map = (Map) Preconditions.checkNotNull(map2);
        }

        public int size() {
            return this.map.size();
        }

        public boolean containsKey(Object key) {
            return this.map.containsKey(key);
        }

        public boolean containsValue(Object value) {
            return this.map.containsValue(value);
        }

        public boolean containsEntry(Object key, Object value) {
            return this.map.entrySet().contains(Maps.immutableEntry(key, value));
        }

        public Set<V> get(final K key) {
            return new Sets.ImprovedAbstractSet<V>() {
                public Iterator<V> iterator() {
                    final Object obj = key;
                    return new Iterator<V>() {
                        int i;

                        public boolean hasNext() {
                            if (this.i == 0) {
                                return MapMultimap.this.map.containsKey(obj);
                            }
                            return false;
                        }

                        public V next() {
                            if (!hasNext()) {
                                throw new NoSuchElementException();
                            }
                            this.i++;
                            return MapMultimap.this.map.get(obj);
                        }

                        public void remove() {
                            boolean z = true;
                            if (this.i != 1) {
                                z = false;
                            }
                            CollectPreconditions.checkRemove(z);
                            this.i = -1;
                            MapMultimap.this.map.remove(obj);
                        }
                    };
                }

                public int size() {
                    return MapMultimap.this.map.containsKey(key) ? 1 : 0;
                }
            };
        }

        public boolean put(K k, V v) {
            throw new UnsupportedOperationException();
        }

        public boolean putAll(K k, Iterable<? extends V> iterable) {
            throw new UnsupportedOperationException();
        }

        public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
            throw new UnsupportedOperationException();
        }

        public Set<V> replaceValues(K k, Iterable<? extends V> iterable) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object key, Object value) {
            return this.map.entrySet().remove(Maps.immutableEntry(key, value));
        }

        public Set<V> removeAll(Object key) {
            Set<V> values = new HashSet<>(2);
            if (!this.map.containsKey(key)) {
                return values;
            }
            values.add(this.map.remove(key));
            return values;
        }

        public void clear() {
            this.map.clear();
        }

        public Set<K> keySet() {
            return this.map.keySet();
        }

        public Collection<V> values() {
            return this.map.values();
        }

        public Set<Map.Entry<K, V>> entries() {
            return this.map.entrySet();
        }

        /* access modifiers changed from: package-private */
        public Iterator<Map.Entry<K, V>> entryIterator() {
            return this.map.entrySet().iterator();
        }

        /* access modifiers changed from: package-private */
        public Map<K, Collection<V>> createAsMap() {
            return new AsMap(this);
        }

        public int hashCode() {
            return this.map.hashCode();
        }
    }

    public static <K, V1, V2> Multimap<K, V2> transformValues(Multimap<K, V1> fromMultimap, Function<? super V1, V2> function) {
        Preconditions.checkNotNull(function);
        return transformEntries(fromMultimap, Maps.asEntryTransformer(function));
    }

    public static <K, V1, V2> Multimap<K, V2> transformEntries(Multimap<K, V1> fromMap, Maps.EntryTransformer<? super K, ? super V1, V2> transformer) {
        return new TransformedEntriesMultimap(fromMap, transformer);
    }

    private static class TransformedEntriesMultimap<K, V1, V2> extends AbstractMultimap<K, V2> {
        final Multimap<K, V1> fromMultimap;
        final Maps.EntryTransformer<? super K, ? super V1, V2> transformer;

        TransformedEntriesMultimap(Multimap<K, V1> fromMultimap2, Maps.EntryTransformer<? super K, ? super V1, V2> transformer2) {
            this.fromMultimap = (Multimap) Preconditions.checkNotNull(fromMultimap2);
            this.transformer = (Maps.EntryTransformer) Preconditions.checkNotNull(transformer2);
        }

        /* access modifiers changed from: package-private */
        public Collection<V2> transform(K key, Collection<V1> values) {
            Function<? super V1, V2> function = Maps.asValueToValueFunction(this.transformer, key);
            if (values instanceof List) {
                return Lists.transform((List) values, function);
            }
            return Collections2.transform(values, function);
        }

        /* access modifiers changed from: package-private */
        public Map<K, Collection<V2>> createAsMap() {
            return Maps.transformEntries(this.fromMultimap.asMap(), new Maps.EntryTransformer<K, Collection<V1>, Collection<V2>>() {
                public Collection<V2> transformEntry(K key, Collection<V1> value) {
                    return TransformedEntriesMultimap.this.transform(key, value);
                }
            });
        }

        public void clear() {
            this.fromMultimap.clear();
        }

        public boolean containsKey(Object key) {
            return this.fromMultimap.containsKey(key);
        }

        /* access modifiers changed from: package-private */
        public Iterator<Map.Entry<K, V2>> entryIterator() {
            return Iterators.transform(this.fromMultimap.entries().iterator(), Maps.asEntryToEntryFunction(this.transformer));
        }

        public Collection<V2> get(K key) {
            return transform(key, this.fromMultimap.get(key));
        }

        public boolean isEmpty() {
            return this.fromMultimap.isEmpty();
        }

        public Set<K> keySet() {
            return this.fromMultimap.keySet();
        }

        public Multiset<K> keys() {
            return this.fromMultimap.keys();
        }

        public boolean put(K k, V2 v2) {
            throw new UnsupportedOperationException();
        }

        public boolean putAll(K k, Iterable<? extends V2> iterable) {
            throw new UnsupportedOperationException();
        }

        public boolean putAll(Multimap<? extends K, ? extends V2> multimap) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object key, Object value) {
            return get(key).remove(value);
        }

        public Collection<V2> removeAll(Object key) {
            return transform(key, this.fromMultimap.removeAll(key));
        }

        public Collection<V2> replaceValues(K k, Iterable<? extends V2> iterable) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return this.fromMultimap.size();
        }

        /* access modifiers changed from: package-private */
        public Collection<V2> createValues() {
            return Collections2.transform(this.fromMultimap.entries(), Maps.asEntryToValueFunction(this.transformer));
        }
    }

    public static <K, V1, V2> ListMultimap<K, V2> transformValues(ListMultimap<K, V1> fromMultimap, Function<? super V1, V2> function) {
        Preconditions.checkNotNull(function);
        return transformEntries(fromMultimap, Maps.asEntryTransformer(function));
    }

    public static <K, V1, V2> ListMultimap<K, V2> transformEntries(ListMultimap<K, V1> fromMap, Maps.EntryTransformer<? super K, ? super V1, V2> transformer) {
        return new TransformedEntriesListMultimap(fromMap, transformer);
    }

    private static final class TransformedEntriesListMultimap<K, V1, V2> extends TransformedEntriesMultimap<K, V1, V2> implements ListMultimap<K, V2> {
        TransformedEntriesListMultimap(ListMultimap<K, V1> fromMultimap, Maps.EntryTransformer<? super K, ? super V1, V2> transformer) {
            super(fromMultimap, transformer);
        }

        /* access modifiers changed from: package-private */
        public List<V2> transform(K key, Collection<V1> values) {
            return Lists.transform((List) values, Maps.asValueToValueFunction(this.transformer, key));
        }

        public List<V2> get(K key) {
            return transform((Object) key, this.fromMultimap.get(key));
        }

        public List<V2> removeAll(Object key) {
            return transform(key, this.fromMultimap.removeAll(key));
        }

        public List<V2> replaceValues(K k, Iterable<? extends V2> iterable) {
            throw new UnsupportedOperationException();
        }
    }

    public static <K, V> ImmutableListMultimap<K, V> index(Iterable<V> values, Function<? super V, K> keyFunction) {
        return index(values.iterator(), keyFunction);
    }

    public static <K, V> ImmutableListMultimap<K, V> index(Iterator<V> values, Function<? super V, K> keyFunction) {
        Preconditions.checkNotNull(keyFunction);
        ImmutableListMultimap.Builder<K, V> builder = ImmutableListMultimap.builder();
        while (values.hasNext()) {
            V value = values.next();
            Preconditions.checkNotNull(value, values);
            builder.put(keyFunction.apply(value), value);
        }
        return builder.build();
    }

    static class Keys<K, V> extends AbstractMultiset<K> {
        final Multimap<K, V> multimap;

        Keys(Multimap<K, V> multimap2) {
            this.multimap = multimap2;
        }

        /* access modifiers changed from: package-private */
        public Iterator<Multiset.Entry<K>> entryIterator() {
            return new TransformedIterator<Map.Entry<K, Collection<V>>, Multiset.Entry<K>>(this.multimap.asMap().entrySet().iterator()) {
                /* access modifiers changed from: package-private */
                public Multiset.Entry<K> transform(final Map.Entry<K, Collection<V>> backingEntry) {
                    return new Multisets.AbstractEntry<K>() {
                        public K getElement() {
                            return backingEntry.getKey();
                        }

                        public int getCount() {
                            return ((Collection) backingEntry.getValue()).size();
                        }
                    };
                }
            };
        }

        /* access modifiers changed from: package-private */
        public int distinctElements() {
            return this.multimap.asMap().size();
        }

        /* access modifiers changed from: package-private */
        public Set<Multiset.Entry<K>> createEntrySet() {
            return new KeysEntrySet();
        }

        class KeysEntrySet extends Multisets.EntrySet<K> {
            KeysEntrySet() {
            }

            /* access modifiers changed from: package-private */
            public Multiset<K> multiset() {
                return Keys.this;
            }

            public Iterator<Multiset.Entry<K>> iterator() {
                return Keys.this.entryIterator();
            }

            public int size() {
                return Keys.this.distinctElements();
            }

            public boolean isEmpty() {
                return Keys.this.multimap.isEmpty();
            }

            public boolean contains(@Nullable Object o) {
                if (!(o instanceof Multiset.Entry)) {
                    return false;
                }
                Multiset.Entry<?> entry = (Multiset.Entry) o;
                Collection<V> collection = Keys.this.multimap.asMap().get(entry.getElement());
                if (collection == null || collection.size() != entry.getCount()) {
                    return false;
                }
                return true;
            }

            public boolean remove(@Nullable Object o) {
                if (!(o instanceof Multiset.Entry)) {
                    return false;
                }
                Multiset.Entry<?> entry = (Multiset.Entry) o;
                Collection<V> collection = Keys.this.multimap.asMap().get(entry.getElement());
                if (collection == null || collection.size() != entry.getCount()) {
                    return false;
                }
                collection.clear();
                return true;
            }
        }

        public boolean contains(@Nullable Object element) {
            return this.multimap.containsKey(element);
        }

        public Iterator<K> iterator() {
            return Maps.keyIterator(this.multimap.entries().iterator());
        }

        public int count(@Nullable Object element) {
            Collection<V> values = (Collection) Maps.safeGet(this.multimap.asMap(), element);
            if (values == null) {
                return 0;
            }
            return values.size();
        }

        public int remove(@Nullable Object element, int occurrences) {
            CollectPreconditions.checkNonnegative(occurrences, "occurrences");
            if (occurrences == 0) {
                return count(element);
            }
            Collection<V> values = (Collection) Maps.safeGet(this.multimap.asMap(), element);
            if (values == null) {
                return 0;
            }
            int oldCount = values.size();
            if (occurrences >= oldCount) {
                values.clear();
            } else {
                Iterator<V> iterator = values.iterator();
                for (int i = 0; i < occurrences; i++) {
                    iterator.next();
                    iterator.remove();
                }
            }
            return oldCount;
        }

        public void clear() {
            this.multimap.clear();
        }

        public Set<K> elementSet() {
            return this.multimap.keySet();
        }
    }

    static abstract class Entries<K, V> extends AbstractCollection<Map.Entry<K, V>> {
        /* access modifiers changed from: package-private */
        public abstract Multimap<K, V> multimap();

        Entries() {
        }

        public int size() {
            return multimap().size();
        }

        public boolean contains(@Nullable Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry) o;
            return multimap().containsEntry(entry.getKey(), entry.getValue());
        }

        public boolean remove(@Nullable Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry) o;
            return multimap().remove(entry.getKey(), entry.getValue());
        }

        public void clear() {
            multimap().clear();
        }
    }

    static final class AsMap<K, V> extends Maps.ImprovedAbstractMap<K, Collection<V>> {
        /* access modifiers changed from: private */
        public final Multimap<K, V> multimap;

        AsMap(Multimap<K, V> multimap2) {
            this.multimap = (Multimap) Preconditions.checkNotNull(multimap2);
        }

        public int size() {
            return this.multimap.keySet().size();
        }

        /* access modifiers changed from: protected */
        public Set<Map.Entry<K, Collection<V>>> createEntrySet() {
            return new EntrySet();
        }

        /* access modifiers changed from: package-private */
        public void removeValuesForKey(Object key) {
            this.multimap.keySet().remove(key);
        }

        class EntrySet extends Maps.EntrySet<K, Collection<V>> {
            EntrySet() {
            }

            /* access modifiers changed from: package-private */
            public Map<K, Collection<V>> map() {
                return AsMap.this;
            }

            public Iterator<Map.Entry<K, Collection<V>>> iterator() {
                return Maps.asMapEntryIterator(AsMap.this.multimap.keySet(), new Function<K, Collection<V>>() {
                    public Collection<V> apply(K key) {
                        return AsMap.this.multimap.get(key);
                    }
                });
            }

            public boolean remove(Object o) {
                if (!contains(o)) {
                    return false;
                }
                AsMap.this.removeValuesForKey(((Map.Entry) o).getKey());
                return true;
            }
        }

        public Collection<V> get(Object key) {
            if (containsKey(key)) {
                return this.multimap.get(key);
            }
            return null;
        }

        public Collection<V> remove(Object key) {
            if (containsKey(key)) {
                return this.multimap.removeAll(key);
            }
            return null;
        }

        public Set<K> keySet() {
            return this.multimap.keySet();
        }

        public boolean isEmpty() {
            return this.multimap.isEmpty();
        }

        public boolean containsKey(Object key) {
            return this.multimap.containsKey(key);
        }

        public void clear() {
            this.multimap.clear();
        }
    }

    public static <K, V> Multimap<K, V> filterKeys(Multimap<K, V> unfiltered, Predicate<? super K> keyPredicate) {
        if (unfiltered instanceof SetMultimap) {
            return filterKeys((SetMultimap) unfiltered, keyPredicate);
        }
        if (unfiltered instanceof ListMultimap) {
            return filterKeys((ListMultimap) unfiltered, keyPredicate);
        }
        if (unfiltered instanceof FilteredKeyMultimap) {
            FilteredKeyMultimap<K, V> prev = (FilteredKeyMultimap) unfiltered;
            return new FilteredKeyMultimap(prev.unfiltered, Predicates.and(prev.keyPredicate, keyPredicate));
        } else if (unfiltered instanceof FilteredMultimap) {
            return filterFiltered((FilteredMultimap) unfiltered, Maps.keyPredicateOnEntries(keyPredicate));
        } else {
            return new FilteredKeyMultimap(unfiltered, keyPredicate);
        }
    }

    public static <K, V> SetMultimap<K, V> filterKeys(SetMultimap<K, V> unfiltered, Predicate<? super K> keyPredicate) {
        if (unfiltered instanceof FilteredKeySetMultimap) {
            FilteredKeySetMultimap<K, V> prev = (FilteredKeySetMultimap) unfiltered;
            return new FilteredKeySetMultimap(prev.unfiltered(), Predicates.and(prev.keyPredicate, keyPredicate));
        } else if (unfiltered instanceof FilteredSetMultimap) {
            return filterFiltered((FilteredSetMultimap) unfiltered, Maps.keyPredicateOnEntries(keyPredicate));
        } else {
            return new FilteredKeySetMultimap(unfiltered, keyPredicate);
        }
    }

    public static <K, V> ListMultimap<K, V> filterKeys(ListMultimap<K, V> unfiltered, Predicate<? super K> keyPredicate) {
        if (!(unfiltered instanceof FilteredKeyListMultimap)) {
            return new FilteredKeyListMultimap(unfiltered, keyPredicate);
        }
        FilteredKeyListMultimap<K, V> prev = (FilteredKeyListMultimap) unfiltered;
        return new FilteredKeyListMultimap(prev.unfiltered(), Predicates.and(prev.keyPredicate, keyPredicate));
    }

    public static <K, V> Multimap<K, V> filterValues(Multimap<K, V> unfiltered, Predicate<? super V> valuePredicate) {
        return filterEntries(unfiltered, Maps.valuePredicateOnEntries(valuePredicate));
    }

    public static <K, V> SetMultimap<K, V> filterValues(SetMultimap<K, V> unfiltered, Predicate<? super V> valuePredicate) {
        return filterEntries(unfiltered, Maps.valuePredicateOnEntries(valuePredicate));
    }

    public static <K, V> Multimap<K, V> filterEntries(Multimap<K, V> unfiltered, Predicate<? super Map.Entry<K, V>> entryPredicate) {
        Preconditions.checkNotNull(entryPredicate);
        if (unfiltered instanceof SetMultimap) {
            return filterEntries((SetMultimap) unfiltered, entryPredicate);
        }
        if (unfiltered instanceof FilteredMultimap) {
            return filterFiltered((FilteredMultimap) unfiltered, entryPredicate);
        }
        return new FilteredEntryMultimap((Multimap) Preconditions.checkNotNull(unfiltered), entryPredicate);
    }

    public static <K, V> SetMultimap<K, V> filterEntries(SetMultimap<K, V> unfiltered, Predicate<? super Map.Entry<K, V>> entryPredicate) {
        Preconditions.checkNotNull(entryPredicate);
        if (unfiltered instanceof FilteredSetMultimap) {
            return filterFiltered((FilteredSetMultimap) unfiltered, entryPredicate);
        }
        return new FilteredEntrySetMultimap((SetMultimap) Preconditions.checkNotNull(unfiltered), entryPredicate);
    }

    private static <K, V> Multimap<K, V> filterFiltered(FilteredMultimap<K, V> multimap, Predicate<? super Map.Entry<K, V>> entryPredicate) {
        return new FilteredEntryMultimap(multimap.unfiltered(), Predicates.and(multimap.entryPredicate(), entryPredicate));
    }

    private static <K, V> SetMultimap<K, V> filterFiltered(FilteredSetMultimap<K, V> multimap, Predicate<? super Map.Entry<K, V>> entryPredicate) {
        return new FilteredEntrySetMultimap(multimap.unfiltered(), Predicates.and(multimap.entryPredicate(), entryPredicate));
    }

    static boolean equalsImpl(Multimap<?, ?> multimap, @Nullable Object object) {
        if (object == multimap) {
            return true;
        }
        if (object instanceof Multimap) {
            return multimap.asMap().equals(((Multimap) object).asMap());
        }
        return false;
    }
}
