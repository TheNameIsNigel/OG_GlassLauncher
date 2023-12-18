package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import java.io.Serializable;
import java.lang.Comparable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import javax.annotation.Nullable;

@GwtCompatible
public final class Range<C extends Comparable> implements Predicate<C>, Serializable {

    /* renamed from: -com-google-common-collect-BoundTypeSwitchesValues  reason: not valid java name */
    private static final /* synthetic */ int[] f26comgooglecommoncollectBoundTypeSwitchesValues = null;
    private static final Range<Comparable> ALL = new Range<>(Cut.belowAll(), Cut.aboveAll());
    private static final Function<Range, Cut> LOWER_BOUND_FN = new Function<Range, Cut>() {
        public Cut apply(Range range) {
            return range.lowerBound;
        }
    };
    static final Ordering<Range<?>> RANGE_LEX_ORDERING = new Ordering<Range<?>>() {
        public int compare(Range<?> left, Range<?> right) {
            return ComparisonChain.start().compare((Comparable<?>) left.lowerBound, (Comparable<?>) right.lowerBound).compare((Comparable<?>) left.upperBound, (Comparable<?>) right.upperBound).result();
        }
    };
    private static final Function<Range, Cut> UPPER_BOUND_FN = new Function<Range, Cut>() {
        public Cut apply(Range range) {
            return range.upperBound;
        }
    };
    private static final long serialVersionUID = 0;
    final Cut<C> lowerBound;
    final Cut<C> upperBound;

    /* renamed from: -getcom-google-common-collect-BoundTypeSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m327getcomgooglecommoncollectBoundTypeSwitchesValues() {
        if (f26comgooglecommoncollectBoundTypeSwitchesValues != null) {
            return f26comgooglecommoncollectBoundTypeSwitchesValues;
        }
        int[] iArr = new int[BoundType.values().length];
        try {
            iArr[BoundType.CLOSED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[BoundType.OPEN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        f26comgooglecommoncollectBoundTypeSwitchesValues = iArr;
        return iArr;
    }

    static <C extends Comparable<?>> Function<Range<C>, Cut<C>> lowerBoundFn() {
        return LOWER_BOUND_FN;
    }

    static <C extends Comparable<?>> Function<Range<C>, Cut<C>> upperBoundFn() {
        return UPPER_BOUND_FN;
    }

    static <C extends Comparable<?>> Range<C> create(Cut<C> lowerBound2, Cut<C> upperBound2) {
        return new Range<>(lowerBound2, upperBound2);
    }

    public static <C extends Comparable<?>> Range<C> open(C lower, C upper) {
        return create(Cut.aboveValue(lower), Cut.belowValue(upper));
    }

    public static <C extends Comparable<?>> Range<C> closed(C lower, C upper) {
        return create(Cut.belowValue(lower), Cut.aboveValue(upper));
    }

    public static <C extends Comparable<?>> Range<C> closedOpen(C lower, C upper) {
        return create(Cut.belowValue(lower), Cut.belowValue(upper));
    }

    public static <C extends Comparable<?>> Range<C> openClosed(C lower, C upper) {
        return create(Cut.aboveValue(lower), Cut.aboveValue(upper));
    }

    public static <C extends Comparable<?>> Range<C> range(C lower, BoundType lowerType, C upper, BoundType upperType) {
        Cut<C> lowerBound2;
        Cut<C> upperBound2;
        Preconditions.checkNotNull(lowerType);
        Preconditions.checkNotNull(upperType);
        if (lowerType == BoundType.OPEN) {
            lowerBound2 = Cut.aboveValue(lower);
        } else {
            lowerBound2 = Cut.belowValue(lower);
        }
        if (upperType == BoundType.OPEN) {
            upperBound2 = Cut.belowValue(upper);
        } else {
            upperBound2 = Cut.aboveValue(upper);
        }
        return create(lowerBound2, upperBound2);
    }

    public static <C extends Comparable<?>> Range<C> lessThan(C endpoint) {
        return create(Cut.belowAll(), Cut.belowValue(endpoint));
    }

    public static <C extends Comparable<?>> Range<C> atMost(C endpoint) {
        return create(Cut.belowAll(), Cut.aboveValue(endpoint));
    }

    public static <C extends Comparable<?>> Range<C> upTo(C endpoint, BoundType boundType) {
        switch (m327getcomgooglecommoncollectBoundTypeSwitchesValues()[boundType.ordinal()]) {
            case 1:
                return atMost(endpoint);
            case 2:
                return lessThan(endpoint);
            default:
                throw new AssertionError();
        }
    }

    public static <C extends Comparable<?>> Range<C> greaterThan(C endpoint) {
        return create(Cut.aboveValue(endpoint), Cut.aboveAll());
    }

    public static <C extends Comparable<?>> Range<C> atLeast(C endpoint) {
        return create(Cut.belowValue(endpoint), Cut.aboveAll());
    }

    public static <C extends Comparable<?>> Range<C> downTo(C endpoint, BoundType boundType) {
        switch (m327getcomgooglecommoncollectBoundTypeSwitchesValues()[boundType.ordinal()]) {
            case 1:
                return atLeast(endpoint);
            case 2:
                return greaterThan(endpoint);
            default:
                throw new AssertionError();
        }
    }

    public static <C extends Comparable<?>> Range<C> all() {
        return ALL;
    }

    public static <C extends Comparable<?>> Range<C> singleton(C value) {
        return closed(value, value);
    }

    public static <C extends Comparable<?>> Range<C> encloseAll(Iterable<C> values) {
        Preconditions.checkNotNull(values);
        if (values instanceof ContiguousSet) {
            return ((ContiguousSet) values).range();
        }
        Iterator<C> valueIterator = values.iterator();
        C min = (Comparable) Preconditions.checkNotNull((Comparable) valueIterator.next());
        C max = min;
        while (valueIterator.hasNext()) {
            C value = (Comparable) Preconditions.checkNotNull((Comparable) valueIterator.next());
            min = (Comparable) Ordering.natural().min(min, value);
            max = (Comparable) Ordering.natural().max(max, value);
        }
        return closed(min, max);
    }

    private Range(Cut<C> lowerBound2, Cut<C> upperBound2) {
        if (lowerBound2.compareTo(upperBound2) > 0 || lowerBound2 == Cut.aboveAll() || upperBound2 == Cut.belowAll()) {
            throw new IllegalArgumentException("Invalid range: " + toString(lowerBound2, upperBound2));
        }
        this.lowerBound = (Cut) Preconditions.checkNotNull(lowerBound2);
        this.upperBound = (Cut) Preconditions.checkNotNull(upperBound2);
    }

    public boolean hasLowerBound() {
        return this.lowerBound != Cut.belowAll();
    }

    public C lowerEndpoint() {
        return this.lowerBound.endpoint();
    }

    public BoundType lowerBoundType() {
        return this.lowerBound.typeAsLowerBound();
    }

    public boolean hasUpperBound() {
        return this.upperBound != Cut.aboveAll();
    }

    public C upperEndpoint() {
        return this.upperBound.endpoint();
    }

    public BoundType upperBoundType() {
        return this.upperBound.typeAsUpperBound();
    }

    public boolean isEmpty() {
        return this.lowerBound.equals(this.upperBound);
    }

    public boolean contains(C value) {
        Preconditions.checkNotNull(value);
        if (this.lowerBound.isLessThan(value)) {
            return !this.upperBound.isLessThan(value);
        }
        return false;
    }

    @Deprecated
    public boolean apply(C input) {
        return contains(input);
    }

    public boolean containsAll(Iterable<? extends C> values) {
        if (Iterables.isEmpty(values)) {
            return true;
        }
        if (values instanceof SortedSet) {
            SortedSet<? extends C> set = cast(values);
            Comparator<? super Object> comparator = set.comparator();
            if (Ordering.natural().equals(comparator) || comparator == null) {
                if (contains((Comparable) set.first())) {
                    return contains((Comparable) set.last());
                }
                return false;
            }
        }
        for (C value : values) {
            if (!contains(value)) {
                return false;
            }
        }
        return true;
    }

    public boolean encloses(Range<C> other) {
        if (this.lowerBound.compareTo(other.lowerBound) > 0 || this.upperBound.compareTo(other.upperBound) < 0) {
            return false;
        }
        return true;
    }

    public boolean isConnected(Range<C> other) {
        if (this.lowerBound.compareTo(other.upperBound) > 0 || other.lowerBound.compareTo(this.upperBound) > 0) {
            return false;
        }
        return true;
    }

    public Range<C> intersection(Range<C> connectedRange) {
        int lowerCmp = this.lowerBound.compareTo(connectedRange.lowerBound);
        int upperCmp = this.upperBound.compareTo(connectedRange.upperBound);
        if (lowerCmp >= 0 && upperCmp <= 0) {
            return this;
        }
        if (lowerCmp <= 0 && upperCmp >= 0) {
            return connectedRange;
        }
        return create(lowerCmp >= 0 ? this.lowerBound : connectedRange.lowerBound, upperCmp <= 0 ? this.upperBound : connectedRange.upperBound);
    }

    public Range<C> span(Range<C> other) {
        int lowerCmp = this.lowerBound.compareTo(other.lowerBound);
        int upperCmp = this.upperBound.compareTo(other.upperBound);
        if (lowerCmp <= 0 && upperCmp >= 0) {
            return this;
        }
        if (lowerCmp >= 0 && upperCmp <= 0) {
            return other;
        }
        return create(lowerCmp <= 0 ? this.lowerBound : other.lowerBound, upperCmp >= 0 ? this.upperBound : other.upperBound);
    }

    public Range<C> canonical(DiscreteDomain<C> domain) {
        Preconditions.checkNotNull(domain);
        Cut<C> lower = this.lowerBound.canonical(domain);
        Cut<C> upper = this.upperBound.canonical(domain);
        return (lower == this.lowerBound && upper == this.upperBound) ? this : create(lower, upper);
    }

    public boolean equals(@Nullable Object object) {
        if (!(object instanceof Range)) {
            return false;
        }
        Range<?> other = (Range) object;
        if (this.lowerBound.equals(other.lowerBound)) {
            return this.upperBound.equals(other.upperBound);
        }
        return false;
    }

    public int hashCode() {
        return (this.lowerBound.hashCode() * 31) + this.upperBound.hashCode();
    }

    public String toString() {
        return toString(this.lowerBound, this.upperBound);
    }

    private static String toString(Cut<?> lowerBound2, Cut<?> upperBound2) {
        StringBuilder sb = new StringBuilder(16);
        lowerBound2.describeAsLowerBound(sb);
        sb.append(8229);
        upperBound2.describeAsUpperBound(sb);
        return sb.toString();
    }

    private static <T> SortedSet<T> cast(Iterable<T> iterable) {
        return (SortedSet) iterable;
    }

    /* access modifiers changed from: package-private */
    public Object readResolve() {
        if (equals(ALL)) {
            return all();
        }
        return this;
    }

    static int compareOrThrow(Comparable left, Comparable right) {
        return left.compareTo(right);
    }
}
