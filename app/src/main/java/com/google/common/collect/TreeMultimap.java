package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractMapBasedMultimap;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nullable;

@GwtCompatible(emulated = true, serializable = true)
public class TreeMultimap<K, V> extends AbstractSortedKeySortedSetMultimap<K, V> {
    @GwtIncompatible("not needed in emulated source")
    private static final long serialVersionUID = 0;
    private transient Comparator<? super K> keyComparator;
    private transient Comparator<? super V> valueComparator;

    public /* bridge */ /* synthetic */ void clear() {
        super.clear();
    }

    public /* bridge */ /* synthetic */ boolean containsEntry(@Nullable Object obj, @Nullable Object obj2) {
        return super.containsEntry(obj, obj2);
    }

    public /* bridge */ /* synthetic */ boolean containsKey(@Nullable Object obj) {
        return super.containsKey(obj);
    }

    public /* bridge */ /* synthetic */ boolean containsValue(@Nullable Object obj) {
        return super.containsValue(obj);
    }

    public /* bridge */ /* synthetic */ boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }

    public /* bridge */ /* synthetic */ boolean isEmpty() {
        return super.isEmpty();
    }

    public /* bridge */ /* synthetic */ Multiset keys() {
        return super.keys();
    }

    public /* bridge */ /* synthetic */ boolean put(@Nullable Object obj, @Nullable Object obj2) {
        return super.put(obj, obj2);
    }

    public /* bridge */ /* synthetic */ boolean putAll(Multimap multimap) {
        return super.putAll(multimap);
    }

    public /* bridge */ /* synthetic */ boolean putAll(@Nullable Object obj, Iterable iterable) {
        return super.putAll(obj, iterable);
    }

    public /* bridge */ /* synthetic */ boolean remove(@Nullable Object obj, @Nullable Object obj2) {
        return super.remove(obj, obj2);
    }

    public /* bridge */ /* synthetic */ int size() {
        return super.size();
    }

    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    public /* bridge */ /* synthetic */ Collection values() {
        return super.values();
    }

    public static <K extends Comparable, V extends Comparable> TreeMultimap<K, V> create() {
        return new TreeMultimap<>(Ordering.natural(), Ordering.natural());
    }

    public static <K, V> TreeMultimap<K, V> create(Comparator<? super K> keyComparator2, Comparator<? super V> valueComparator2) {
        return new TreeMultimap<>((Comparator) Preconditions.checkNotNull(keyComparator2), (Comparator) Preconditions.checkNotNull(valueComparator2));
    }

    public static <K extends Comparable, V extends Comparable> TreeMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
        return new TreeMultimap<>(Ordering.natural(), Ordering.natural(), multimap);
    }

    TreeMultimap(Comparator<? super K> keyComparator2, Comparator<? super V> valueComparator2) {
        super(new TreeMap(keyComparator2));
        this.keyComparator = keyComparator2;
        this.valueComparator = valueComparator2;
    }

    private TreeMultimap(Comparator<? super K> keyComparator2, Comparator<? super V> valueComparator2, Multimap<? extends K, ? extends V> multimap) {
        this(keyComparator2, valueComparator2);
        putAll(multimap);
    }

    /* access modifiers changed from: package-private */
    public SortedSet<V> createCollection() {
        return new TreeSet(this.valueComparator);
    }

    /* access modifiers changed from: package-private */
    public Collection<V> createCollection(@Nullable K key) {
        if (key == null) {
            keyComparator().compare(key, key);
        }
        return super.createCollection(key);
    }

    public Comparator<? super K> keyComparator() {
        return this.keyComparator;
    }

    public Comparator<? super V> valueComparator() {
        return this.valueComparator;
    }

    /* access modifiers changed from: package-private */
    @GwtIncompatible("NavigableMap")
    public NavigableMap<K, Collection<V>> backingMap() {
        return (NavigableMap) super.backingMap();
    }

    @GwtIncompatible("NavigableSet")
    public NavigableSet<V> get(@Nullable K key) {
        return (NavigableSet) super.get((Object) key);
    }

    /* access modifiers changed from: package-private */
    @GwtIncompatible("NavigableSet")
    public Collection<V> unmodifiableCollectionSubclass(Collection<V> collection) {
        return Sets.unmodifiableNavigableSet((NavigableSet) collection);
    }

    /* access modifiers changed from: package-private */
    @GwtIncompatible("NavigableSet")
    public Collection<V> wrapCollection(K key, Collection<V> collection) {
        return new AbstractMapBasedMultimap.WrappedNavigableSet(key, (NavigableSet) collection, (AbstractMapBasedMultimap<K, V>.WrappedCollection) null);
    }

    @GwtIncompatible("NavigableSet")
    public NavigableSet<K> keySet() {
        return (NavigableSet) super.keySet();
    }

    /* access modifiers changed from: package-private */
    @GwtIncompatible("NavigableSet")
    public NavigableSet<K> createKeySet() {
        return new AbstractMapBasedMultimap.NavigableKeySet(backingMap());
    }

    @GwtIncompatible("NavigableMap")
    public NavigableMap<K, Collection<V>> asMap() {
        return (NavigableMap) super.asMap();
    }

    /* access modifiers changed from: package-private */
    @GwtIncompatible("NavigableMap")
    public NavigableMap<K, Collection<V>> createAsMap() {
        return new AbstractMapBasedMultimap.NavigableAsMap(backingMap());
    }

    @GwtIncompatible("java.io.ObjectOutputStream")
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(keyComparator());
        stream.writeObject(valueComparator());
        Serialization.writeMultimap(this, stream);
    }

    @GwtIncompatible("java.io.ObjectInputStream")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.keyComparator = (Comparator) Preconditions.checkNotNull((Comparator) stream.readObject());
        this.valueComparator = (Comparator) Preconditions.checkNotNull((Comparator) stream.readObject());
        setMap(new TreeMap(this.keyComparator));
        Serialization.populateMultimap(this, stream);
    }
}
