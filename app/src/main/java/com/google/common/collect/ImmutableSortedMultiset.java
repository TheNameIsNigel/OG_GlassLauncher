package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@GwtIncompatible("hasn't been tested yet")
@Beta
public abstract class ImmutableSortedMultiset<E> extends ImmutableSortedMultisetFauxverideShim<E> implements SortedMultiset<E> {
    private static final ImmutableSortedMultiset<Comparable> NATURAL_EMPTY_MULTISET = new EmptyImmutableSortedMultiset(NATURAL_ORDER);
    private static final Comparator<Comparable> NATURAL_ORDER = Ordering.natural();
    transient ImmutableSortedMultiset<E> descendingMultiset;

    public abstract ImmutableSortedSet<E> elementSet();

    public abstract ImmutableSortedMultiset<E> headMultiset(E e, BoundType boundType);

    public abstract ImmutableSortedMultiset<E> tailMultiset(E e, BoundType boundType);

    public static <E> ImmutableSortedMultiset<E> of() {
        return NATURAL_EMPTY_MULTISET;
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> of(E element) {
        return new RegularImmutableSortedMultiset((RegularImmutableSortedSet) ImmutableSortedSet.of(element), new int[]{1}, new long[]{0, 1}, 0, 1);
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> of(E e1, E e2) {
        return copyOf(Ordering.natural(), Arrays.asList(new Comparable[]{e1, e2}));
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> of(E e1, E e2, E e3) {
        return copyOf(Ordering.natural(), Arrays.asList(new Comparable[]{e1, e2, e3}));
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> of(E e1, E e2, E e3, E e4) {
        return copyOf(Ordering.natural(), Arrays.asList(new Comparable[]{e1, e2, e3, e4}));
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> of(E e1, E e2, E e3, E e4, E e5) {
        return copyOf(Ordering.natural(), Arrays.asList(new Comparable[]{e1, e2, e3, e4, e5}));
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E... remaining) {
        List<E> all = Lists.newArrayListWithCapacity(remaining.length + 6);
        Collections.addAll(all, new Comparable[]{e1, e2, e3, e4, e5, e6});
        Collections.addAll(all, remaining);
        return copyOf(Ordering.natural(), all);
    }

    public static <E extends Comparable<? super E>> ImmutableSortedMultiset<E> copyOf(E[] elements) {
        return copyOf(Ordering.natural(), Arrays.asList(elements));
    }

    public static <E> ImmutableSortedMultiset<E> copyOf(Iterable<? extends E> elements) {
        return copyOf(Ordering.natural(), elements);
    }

    public static <E> ImmutableSortedMultiset<E> copyOf(Iterator<? extends E> elements) {
        return copyOf(Ordering.natural(), elements);
    }

    public static <E> ImmutableSortedMultiset<E> copyOf(Comparator<? super E> comparator, Iterator<? extends E> elements) {
        Preconditions.checkNotNull(comparator);
        return new Builder(comparator).addAll((Iterator) elements).build();
    }

    public /* bridge */ /* synthetic */ Iterator iterator() {
        return iterator();
    }

    public static <E> ImmutableSortedMultiset<E> copyOf(Comparator<? super E> comparator, Iterable<? extends E> elements) {
        if (elements instanceof ImmutableSortedMultiset) {
            ImmutableSortedMultiset<E> multiset = (ImmutableSortedMultiset) elements;
            if (comparator.equals(multiset.comparator())) {
                if (multiset.isPartialView()) {
                    return copyOfSortedEntries(comparator, multiset.entrySet().asList());
                }
                return multiset;
            }
        }
        Iterable<? extends E> elements2 = Lists.newArrayList(elements);
        TreeMultiset<E> sortedCopy = TreeMultiset.create((Comparator) Preconditions.checkNotNull(comparator));
        Iterables.addAll(sortedCopy, elements2);
        return copyOfSortedEntries(comparator, sortedCopy.entrySet());
    }

    public static <E> ImmutableSortedMultiset<E> copyOfSorted(SortedMultiset<E> sortedMultiset) {
        return copyOfSortedEntries(sortedMultiset.comparator(), Lists.newArrayList(sortedMultiset.entrySet()));
    }

    private static <E> ImmutableSortedMultiset<E> copyOfSortedEntries(Comparator<? super E> comparator, Collection<Multiset.Entry<E>> entries) {
        if (entries.isEmpty()) {
            return emptyMultiset(comparator);
        }
        ImmutableList.Builder<E> elementsBuilder = new ImmutableList.Builder<>(entries.size());
        int[] counts = new int[entries.size()];
        long[] cumulativeCounts = new long[(entries.size() + 1)];
        int i = 0;
        for (Multiset.Entry<E> entry : entries) {
            elementsBuilder.add((Object) entry.getElement());
            counts[i] = entry.getCount();
            cumulativeCounts[i + 1] = cumulativeCounts[i] + ((long) counts[i]);
            i++;
        }
        return new RegularImmutableSortedMultiset(new RegularImmutableSortedSet(elementsBuilder.build(), comparator), counts, cumulativeCounts, 0, entries.size());
    }

    static <E> ImmutableSortedMultiset<E> emptyMultiset(Comparator<? super E> comparator) {
        if (NATURAL_ORDER.equals(comparator)) {
            return NATURAL_EMPTY_MULTISET;
        }
        return new EmptyImmutableSortedMultiset(comparator);
    }

    ImmutableSortedMultiset() {
    }

    public final Comparator<? super E> comparator() {
        return elementSet().comparator();
    }

    public ImmutableSortedMultiset<E> descendingMultiset() {
        ImmutableSortedMultiset<E> result = this.descendingMultiset;
        if (result != null) {
            return result;
        }
        DescendingImmutableSortedMultiset descendingImmutableSortedMultiset = new DescendingImmutableSortedMultiset(this);
        this.descendingMultiset = descendingImmutableSortedMultiset;
        return descendingImmutableSortedMultiset;
    }

    public /* bridge */ /* synthetic */ Set entrySet() {
        return entrySet();
    }

    @Deprecated
    public final Multiset.Entry<E> pollFirstEntry() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final Multiset.Entry<E> pollLastEntry() {
        throw new UnsupportedOperationException();
    }

    public ImmutableSortedMultiset<E> subMultiset(E lowerBound, BoundType lowerBoundType, E upperBound, BoundType upperBoundType) {
        boolean z;
        if (comparator().compare(lowerBound, upperBound) <= 0) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z, "Expected lowerBound <= upperBound but %s > %s", lowerBound, upperBound);
        return tailMultiset(lowerBound, lowerBoundType).headMultiset(upperBound, upperBoundType);
    }

    public static <E> Builder<E> orderedBy(Comparator<E> comparator) {
        return new Builder<>(comparator);
    }

    public static <E extends Comparable<E>> Builder<E> reverseOrder() {
        return new Builder<>(Ordering.natural().reverse());
    }

    public static <E extends Comparable<E>> Builder<E> naturalOrder() {
        return new Builder<>(Ordering.natural());
    }

    public static class Builder<E> extends ImmutableMultiset.Builder<E> {
        public Builder(Comparator<? super E> comparator) {
            super(TreeMultiset.create((Comparator) Preconditions.checkNotNull(comparator)));
        }

        public Builder<E> add(E element) {
            super.add((Object) element);
            return this;
        }

        public Builder<E> addCopies(E element, int occurrences) {
            super.addCopies(element, occurrences);
            return this;
        }

        public Builder<E> setCount(E element, int count) {
            super.setCount(element, count);
            return this;
        }

        public Builder<E> add(E... elements) {
            super.add((Object[]) elements);
            return this;
        }

        public Builder<E> addAll(Iterable<? extends E> elements) {
            super.addAll((Iterable) elements);
            return this;
        }

        public Builder<E> addAll(Iterator<? extends E> elements) {
            super.addAll((Iterator) elements);
            return this;
        }

        public ImmutableSortedMultiset<E> build() {
            return ImmutableSortedMultiset.copyOfSorted((SortedMultiset) this.contents);
        }
    }

    private static final class SerializedForm<E> implements Serializable {
        Comparator<? super E> comparator;
        int[] counts;
        E[] elements;

        SerializedForm(SortedMultiset<E> multiset) {
            this.comparator = multiset.comparator();
            int n = multiset.entrySet().size();
            this.elements = new Object[n];
            this.counts = new int[n];
            int i = 0;
            for (Multiset.Entry<E> entry : multiset.entrySet()) {
                this.elements[i] = entry.getElement();
                this.counts[i] = entry.getCount();
                i++;
            }
        }

        /* access modifiers changed from: package-private */
        public Object readResolve() {
            int n = this.elements.length;
            Builder<E> builder = new Builder<>(this.comparator);
            for (int i = 0; i < n; i++) {
                builder.addCopies((Object) this.elements[i], this.counts[i]);
            }
            return builder.build();
        }
    }

    /* access modifiers changed from: package-private */
    public Object writeReplace() {
        return new SerializedForm(this);
    }
}
