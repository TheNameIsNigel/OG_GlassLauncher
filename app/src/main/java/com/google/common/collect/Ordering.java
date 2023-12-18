package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

@GwtCompatible
public abstract class Ordering<T> implements Comparator<T> {
    static final int LEFT_IS_GREATER = 1;
    static final int RIGHT_IS_GREATER = -1;

    public abstract int compare(@Nullable T t, @Nullable T t2);

    @GwtCompatible(serializable = true)
    public static <C extends Comparable> Ordering<C> natural() {
        return NaturalOrdering.INSTANCE;
    }

    @GwtCompatible(serializable = true)
    public static <T> Ordering<T> from(Comparator<T> comparator) {
        if (comparator instanceof Ordering) {
            return (Ordering) comparator;
        }
        return new ComparatorOrdering(comparator);
    }

    @GwtCompatible(serializable = true)
    @Deprecated
    public static <T> Ordering<T> from(Ordering<T> ordering) {
        return (Ordering) Preconditions.checkNotNull(ordering);
    }

    @GwtCompatible(serializable = true)
    public static <T> Ordering<T> explicit(List<T> valuesInOrder) {
        return new ExplicitOrdering(valuesInOrder);
    }

    @GwtCompatible(serializable = true)
    public static <T> Ordering<T> explicit(T leastValue, T... remainingValuesInOrder) {
        return explicit(Lists.asList(leastValue, remainingValuesInOrder));
    }

    @GwtCompatible(serializable = true)
    public static Ordering<Object> allEqual() {
        return AllEqualOrdering.INSTANCE;
    }

    @GwtCompatible(serializable = true)
    public static Ordering<Object> usingToString() {
        return UsingToStringOrdering.INSTANCE;
    }

    public static Ordering<Object> arbitrary() {
        return ArbitraryOrderingHolder.ARBITRARY_ORDERING;
    }

    private static class ArbitraryOrderingHolder {
        static final Ordering<Object> ARBITRARY_ORDERING = new ArbitraryOrdering();

        private ArbitraryOrderingHolder() {
        }
    }

    @VisibleForTesting
    static class ArbitraryOrdering extends Ordering<Object> {
        private Map<Object, Integer> uids = Platform.tryWeakKeys(new MapMaker()).makeComputingMap(new Function<Object, Integer>() {
            final AtomicInteger counter = new AtomicInteger(0);

            public Integer apply(Object from) {
                return Integer.valueOf(this.counter.getAndIncrement());
            }
        });

        ArbitraryOrdering() {
        }

        public int compare(Object left, Object right) {
            if (left == right) {
                return 0;
            }
            if (left == null) {
                return -1;
            }
            if (right == null) {
                return 1;
            }
            int leftCode = identityHashCode(left);
            int rightCode = identityHashCode(right);
            if (leftCode == rightCode) {
                int result = this.uids.get(left).compareTo(this.uids.get(right));
                if (result != 0) {
                    return result;
                }
                throw new AssertionError();
            } else if (leftCode < rightCode) {
                return -1;
            } else {
                return 1;
            }
        }

        public String toString() {
            return "Ordering.arbitrary()";
        }

        /* access modifiers changed from: package-private */
        public int identityHashCode(Object object) {
            return System.identityHashCode(object);
        }
    }

    protected Ordering() {
    }

    @GwtCompatible(serializable = true)
    public <S extends T> Ordering<S> reverse() {
        return new ReverseOrdering(this);
    }

    @GwtCompatible(serializable = true)
    public <S extends T> Ordering<S> nullsFirst() {
        return new NullsFirstOrdering(this);
    }

    @GwtCompatible(serializable = true)
    public <S extends T> Ordering<S> nullsLast() {
        return new NullsLastOrdering(this);
    }

    @GwtCompatible(serializable = true)
    public <F> Ordering<F> onResultOf(Function<F, ? extends T> function) {
        return new ByFunctionOrdering(function, this);
    }

    /* access modifiers changed from: package-private */
    public <T2 extends T> Ordering<Map.Entry<T2, ?>> onKeys() {
        return onResultOf(Maps.keyFunction());
    }

    @GwtCompatible(serializable = true)
    public <U extends T> Ordering<U> compound(Comparator<? super U> secondaryComparator) {
        return new CompoundOrdering(this, (Comparator) Preconditions.checkNotNull(secondaryComparator));
    }

    @GwtCompatible(serializable = true)
    public static <T> Ordering<T> compound(Iterable<? extends Comparator<? super T>> comparators) {
        return new CompoundOrdering(comparators);
    }

    @GwtCompatible(serializable = true)
    public <S extends T> Ordering<Iterable<S>> lexicographical() {
        return new LexicographicalOrdering(this);
    }

    public <E extends T> E min(Iterator<E> iterator) {
        E minSoFar = iterator.next();
        while (iterator.hasNext()) {
            minSoFar = min(minSoFar, iterator.next());
        }
        return minSoFar;
    }

    public <E extends T> E min(Iterable<E> iterable) {
        return min(iterable.iterator());
    }

    public <E extends T> E min(@Nullable E a, @Nullable E b) {
        return compare(a, b) <= 0 ? a : b;
    }

    public <E extends T> E min(@Nullable E a, @Nullable E b, @Nullable E c, E... rest) {
        E minSoFar = min(min(a, b), c);
        for (E r : rest) {
            minSoFar = min(minSoFar, r);
        }
        return minSoFar;
    }

    public <E extends T> E max(Iterator<E> iterator) {
        E maxSoFar = iterator.next();
        while (iterator.hasNext()) {
            maxSoFar = max(maxSoFar, iterator.next());
        }
        return maxSoFar;
    }

    public <E extends T> E max(Iterable<E> iterable) {
        return max(iterable.iterator());
    }

    public <E extends T> E max(@Nullable E a, @Nullable E b) {
        return compare(a, b) >= 0 ? a : b;
    }

    public <E extends T> E max(@Nullable E a, @Nullable E b, @Nullable E c, E... rest) {
        E maxSoFar = max(max(a, b), c);
        for (E r : rest) {
            maxSoFar = max(maxSoFar, r);
        }
        return maxSoFar;
    }

    public <E extends T> List<E> leastOf(Iterable<E> iterable, int k) {
        if (iterable instanceof Collection) {
            Collection<E> collection = (Collection) iterable;
            if (((long) collection.size()) <= ((long) k) * 2) {
                E[] array = collection.toArray();
                Arrays.sort(array, this);
                if (array.length > k) {
                    array = ObjectArrays.arraysCopyOf(array, k);
                }
                return Collections.unmodifiableList(Arrays.asList(array));
            }
        }
        return leastOf(iterable.iterator(), k);
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0071  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00b7 A[SYNTHETIC] */
    public <E extends T> java.util.List<E> leastOf(java.util.Iterator<E> r16, int r17) {
        /*
            r15 = this;
            com.google.common.base.Preconditions.checkNotNull(r16)
            java.lang.String r14 = "k"
            r0 = r17
            com.google.common.collect.CollectPreconditions.checkNonnegative(r0, r14)
            if (r17 == 0) goto L_0x0015
            boolean r14 = r16.hasNext()
            r14 = r14 ^ 1
            if (r14 == 0) goto L_0x001a
        L_0x0015:
            com.google.common.collect.ImmutableList r14 = com.google.common.collect.ImmutableList.of()
            return r14
        L_0x001a:
            r14 = 1073741823(0x3fffffff, float:1.9999999)
            r0 = r17
            if (r0 < r14) goto L_0x0045
            java.util.ArrayList r8 = com.google.common.collect.Lists.newArrayList(r16)
            java.util.Collections.sort(r8, r15)
            int r14 = r8.size()
            r0 = r17
            if (r14 <= r0) goto L_0x003d
            int r14 = r8.size()
            r0 = r17
            java.util.List r14 = r8.subList(r0, r14)
            r14.clear()
        L_0x003d:
            r8.trimToSize()
            java.util.List r14 = java.util.Collections.unmodifiableList(r8)
            return r14
        L_0x0045:
            int r2 = r17 * 2
            java.lang.Object[] r1 = new java.lang.Object[r2]
            java.lang.Object r13 = r16.next()
            r14 = 0
            r1[r14] = r13
            r3 = 1
            r4 = r3
        L_0x0052:
            r0 = r17
            if (r4 >= r0) goto L_0x006a
            boolean r14 = r16.hasNext()
            if (r14 == 0) goto L_0x00ce
            java.lang.Object r5 = r16.next()
            int r3 = r4 + 1
            r1[r4] = r5
            java.lang.Object r13 = r15.max(r13, r5)
            r4 = r3
            goto L_0x0052
        L_0x006a:
            r3 = r4
        L_0x006b:
            boolean r14 = r16.hasNext()
            if (r14 == 0) goto L_0x00b7
            java.lang.Object r5 = r16.next()
            int r14 = r15.compare(r5, r13)
            if (r14 >= 0) goto L_0x006b
            int r4 = r3 + 1
            r1[r3] = r5
            if (r4 != r2) goto L_0x00ce
            r7 = 0
            int r12 = r2 + -1
            r9 = 0
        L_0x0085:
            if (r7 >= r12) goto L_0x00a4
            int r14 = r7 + r12
            int r14 = r14 + 1
            int r10 = r14 >>> 1
            int r11 = r15.partition(r1, r7, r12, r10)
            r0 = r17
            if (r11 <= r0) goto L_0x0098
            int r12 = r11 + -1
            goto L_0x0085
        L_0x0098:
            r0 = r17
            if (r11 >= r0) goto L_0x00a4
            int r14 = r7 + 1
            int r7 = java.lang.Math.max(r11, r14)
            r9 = r11
            goto L_0x0085
        L_0x00a4:
            r3 = r17
            r13 = r1[r9]
            int r6 = r9 + 1
        L_0x00aa:
            r0 = r17
            if (r6 >= r0) goto L_0x006b
            r14 = r1[r6]
            java.lang.Object r13 = r15.max(r13, r14)
            int r6 = r6 + 1
            goto L_0x00aa
        L_0x00b7:
            r14 = 0
            java.util.Arrays.sort(r1, r14, r3, r15)
            r0 = r17
            int r3 = java.lang.Math.min(r3, r0)
            java.lang.Object[] r14 = com.google.common.collect.ObjectArrays.arraysCopyOf(r1, r3)
            java.util.List r14 = java.util.Arrays.asList(r14)
            java.util.List r14 = java.util.Collections.unmodifiableList(r14)
            return r14
        L_0x00ce:
            r3 = r4
            goto L_0x006b
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.common.collect.Ordering.leastOf(java.util.Iterator, int):java.util.List");
    }

    private <E extends T> int partition(E[] values, int left, int right, int pivotIndex) {
        E pivotValue = values[pivotIndex];
        values[pivotIndex] = values[right];
        values[right] = pivotValue;
        int storeIndex = left;
        for (int i = left; i < right; i++) {
            if (compare(values[i], pivotValue) < 0) {
                ObjectArrays.swap(values, storeIndex, i);
                storeIndex++;
            }
        }
        ObjectArrays.swap(values, right, storeIndex);
        return storeIndex;
    }

    public <E extends T> List<E> greatestOf(Iterable<E> iterable, int k) {
        return reverse().leastOf(iterable, k);
    }

    public <E extends T> List<E> greatestOf(Iterator<E> iterator, int k) {
        return reverse().leastOf(iterator, k);
    }

    public <E extends T> List<E> sortedCopy(Iterable<E> elements) {
        E[] array = Iterables.toArray(elements);
        Arrays.sort(array, this);
        return Lists.newArrayList(Arrays.asList(array));
    }

    public <E extends T> ImmutableList<E> immutableSortedCopy(Iterable<E> elements) {
        E[] array = Iterables.toArray(elements);
        for (E e : array) {
            Preconditions.checkNotNull(e);
        }
        Arrays.sort(array, this);
        return ImmutableList.asImmutableList(array);
    }

    public boolean isOrdered(Iterable<? extends T> iterable) {
        Iterator<? extends T> it = iterable.iterator();
        if (!it.hasNext()) {
            return true;
        }
        T prev = it.next();
        while (it.hasNext()) {
            T next = it.next();
            if (compare(prev, next) > 0) {
                return false;
            }
            prev = next;
        }
        return true;
    }

    public boolean isStrictlyOrdered(Iterable<? extends T> iterable) {
        Iterator<? extends T> it = iterable.iterator();
        if (!it.hasNext()) {
            return true;
        }
        T prev = it.next();
        while (it.hasNext()) {
            T next = it.next();
            if (compare(prev, next) >= 0) {
                return false;
            }
            prev = next;
        }
        return true;
    }

    public int binarySearch(List<? extends T> sortedList, @Nullable T key) {
        return Collections.binarySearch(sortedList, key, this);
    }

    @VisibleForTesting
    static class IncomparableValueException extends ClassCastException {
        private static final long serialVersionUID = 0;
        final Object value;

        IncomparableValueException(Object value2) {
            super("Cannot compare value: " + value2);
            this.value = value2;
        }
    }
}
