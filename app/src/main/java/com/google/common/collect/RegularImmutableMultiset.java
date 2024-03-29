package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.Multiset;
import java.util.Map;
import javax.annotation.Nullable;

@GwtCompatible(serializable = true)
class RegularImmutableMultiset<E> extends ImmutableMultiset<E> {
    private final transient ImmutableMap<E, Integer> map;
    private final transient int size;

    RegularImmutableMultiset(ImmutableMap<E, Integer> map2, int size2) {
        this.map = map2;
        this.size = size2;
    }

    /* access modifiers changed from: package-private */
    public boolean isPartialView() {
        return this.map.isPartialView();
    }

    public int count(@Nullable Object element) {
        Integer value = this.map.get(element);
        if (value == null) {
            return 0;
        }
        return value.intValue();
    }

    public int size() {
        return this.size;
    }

    public boolean contains(@Nullable Object element) {
        return this.map.containsKey(element);
    }

    public ImmutableSet<E> elementSet() {
        return this.map.keySet();
    }

    /* access modifiers changed from: package-private */
    public Multiset.Entry<E> getEntry(int index) {
        Map.Entry<E, Integer> mapEntry = (Map.Entry) this.map.entrySet().asList().get(index);
        return Multisets.immutableEntry(mapEntry.getKey(), mapEntry.getValue().intValue());
    }

    public int hashCode() {
        return this.map.hashCode();
    }
}
