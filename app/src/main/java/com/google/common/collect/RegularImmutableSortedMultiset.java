package com.google.common.collect;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset;
import com.google.common.primitives.Ints;
import javax.annotation.Nullable;

final class RegularImmutableSortedMultiset<E> extends ImmutableSortedMultiset<E> {
    private final transient int[] counts;
    private final transient long[] cumulativeCounts;
    private final transient RegularImmutableSortedSet<E> elementSet;
    private final transient int length;
    private final transient int offset;

    RegularImmutableSortedMultiset(RegularImmutableSortedSet<E> elementSet2, int[] counts2, long[] cumulativeCounts2, int offset2, int length2) {
        this.elementSet = elementSet2;
        this.counts = counts2;
        this.cumulativeCounts = cumulativeCounts2;
        this.offset = offset2;
        this.length = length2;
    }

    /* access modifiers changed from: package-private */
    public Multiset.Entry<E> getEntry(int index) {
        return Multisets.immutableEntry(this.elementSet.asList().get(index), this.counts[this.offset + index]);
    }

    public Multiset.Entry<E> firstEntry() {
        return getEntry(0);
    }

    public Multiset.Entry<E> lastEntry() {
        return getEntry(this.length - 1);
    }

    public int count(@Nullable Object element) {
        int index = this.elementSet.indexOf(element);
        if (index == -1) {
            return 0;
        }
        return this.counts[this.offset + index];
    }

    public int size() {
        return Ints.saturatedCast(this.cumulativeCounts[this.offset + this.length] - this.cumulativeCounts[this.offset]);
    }

    public ImmutableSortedSet<E> elementSet() {
        return this.elementSet;
    }

    public ImmutableSortedMultiset<E> headMultiset(E upperBound, BoundType boundType) {
        return getSubMultiset(0, this.elementSet.headIndex(upperBound, Preconditions.checkNotNull(boundType) == BoundType.CLOSED));
    }

    public ImmutableSortedMultiset<E> tailMultiset(E lowerBound, BoundType boundType) {
        return getSubMultiset(this.elementSet.tailIndex(lowerBound, Preconditions.checkNotNull(boundType) == BoundType.CLOSED), this.length);
    }

    /* access modifiers changed from: package-private */
    public ImmutableSortedMultiset<E> getSubMultiset(int from, int to) {
        Preconditions.checkPositionIndexes(from, to, this.length);
        if (from == to) {
            return emptyMultiset(comparator());
        }
        if (from == 0 && to == this.length) {
            return this;
        }
        return new RegularImmutableSortedMultiset((RegularImmutableSortedSet) this.elementSet.getSubSet(from, to), this.counts, this.cumulativeCounts, this.offset + from, to - from);
    }

    /* access modifiers changed from: package-private */
    public boolean isPartialView() {
        return this.offset > 0 || this.length < this.counts.length;
    }
}
