package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import javax.annotation.Nullable;

@GwtCompatible(emulated = true)
public abstract class ImmutableCollection<E> extends AbstractCollection<E> implements Serializable {
    private transient ImmutableList<E> asList;

    /* access modifiers changed from: package-private */
    public abstract boolean isPartialView();

    public abstract UnmodifiableIterator<E> iterator();

    ImmutableCollection() {
    }

    public final Object[] toArray() {
        int size = size();
        if (size == 0) {
            return ObjectArrays.EMPTY_ARRAY;
        }
        Object[] result = new Object[size];
        copyIntoArray(result, 0);
        return result;
    }

    public final <T> T[] toArray(T[] other) {
        Preconditions.checkNotNull(other);
        int size = size();
        if (other.length < size) {
            other = ObjectArrays.newArray(other, size);
        } else if (other.length > size) {
            other[size] = null;
        }
        copyIntoArray(other, 0);
        return other;
    }

    public boolean contains(@Nullable Object object) {
        if (object != null) {
            return super.contains(object);
        }
        return false;
    }

    @Deprecated
    public final boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final boolean addAll(Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    public ImmutableList<E> asList() {
        ImmutableList<E> list = this.asList;
        if (list != null) {
            return list;
        }
        ImmutableList<E> list2 = createAsList();
        this.asList = list2;
        return list2;
    }

    /* access modifiers changed from: package-private */
    public ImmutableList<E> createAsList() {
        switch (size()) {
            case 0:
                return ImmutableList.of();
            case 1:
                return ImmutableList.of(iterator().next());
            default:
                return new RegularImmutableAsList(this, toArray());
        }
    }

    /* access modifiers changed from: package-private */
    public int copyIntoArray(Object[] dst, int offset) {
        Iterator e$iterator = iterator();
        while (e$iterator.hasNext()) {
            dst[offset] = e$iterator.next();
            offset++;
        }
        return offset;
    }

    /* access modifiers changed from: package-private */
    public Object writeReplace() {
        return new ImmutableList.SerializedForm(toArray());
    }

    public static abstract class Builder<E> {
        static final int DEFAULT_INITIAL_CAPACITY = 4;

        public abstract Builder<E> add(E e);

        public abstract ImmutableCollection<E> build();

        static int expandedCapacity(int oldCapacity, int minCapacity) {
            if (minCapacity < 0) {
                throw new AssertionError("cannot store more than MAX_VALUE elements");
            }
            int newCapacity = (oldCapacity >> 1) + oldCapacity + 1;
            if (newCapacity < minCapacity) {
                newCapacity = Integer.highestOneBit(minCapacity - 1) << 1;
            }
            if (newCapacity < 0) {
                return Integer.MAX_VALUE;
            }
            return newCapacity;
        }

        Builder() {
        }

        public Builder<E> add(E... elements) {
            for (E element : elements) {
                add(element);
            }
            return this;
        }

        public Builder<E> addAll(Iterable<? extends E> elements) {
            for (E element : elements) {
                add(element);
            }
            return this;
        }

        public Builder<E> addAll(Iterator<? extends E> elements) {
            while (elements.hasNext()) {
                add(elements.next());
            }
            return this;
        }
    }

    static abstract class ArrayBasedBuilder<E> extends Builder<E> {
        Object[] contents;
        int size = 0;

        ArrayBasedBuilder(int initialCapacity) {
            CollectPreconditions.checkNonnegative(initialCapacity, "initialCapacity");
            this.contents = new Object[initialCapacity];
        }

        private void ensureCapacity(int minCapacity) {
            if (this.contents.length < minCapacity) {
                this.contents = ObjectArrays.arraysCopyOf(this.contents, expandedCapacity(this.contents.length, minCapacity));
            }
        }

        public ArrayBasedBuilder<E> add(E element) {
            Preconditions.checkNotNull(element);
            ensureCapacity(this.size + 1);
            Object[] objArr = this.contents;
            int i = this.size;
            this.size = i + 1;
            objArr[i] = element;
            return this;
        }

        public Builder<E> add(E... elements) {
            ObjectArrays.checkElementsNotNull(elements);
            ensureCapacity(this.size + elements.length);
            System.arraycopy(elements, 0, this.contents, this.size, elements.length);
            this.size += elements.length;
            return this;
        }

        public Builder<E> addAll(Iterable<? extends E> elements) {
            if (elements instanceof Collection) {
                ensureCapacity(this.size + ((Collection) elements).size());
            }
            super.addAll(elements);
            return this;
        }
    }
}
