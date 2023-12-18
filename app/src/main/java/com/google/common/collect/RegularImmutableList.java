package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import javax.annotation.Nullable;

@GwtCompatible(emulated = true, serializable = true)
class RegularImmutableList<E> extends ImmutableList<E> {
    private final transient Object[] array;
    private final transient int offset;
    private final transient int size;

    RegularImmutableList(Object[] array2, int offset2, int size2) {
        this.offset = offset2;
        this.size = size2;
        this.array = array2;
    }

    RegularImmutableList(Object[] array2) {
        this(array2, 0, array2.length);
    }

    public int size() {
        return this.size;
    }

    /* access modifiers changed from: package-private */
    public boolean isPartialView() {
        return this.size != this.array.length;
    }

    /* access modifiers changed from: package-private */
    public int copyIntoArray(Object[] dst, int dstOff) {
        System.arraycopy(this.array, this.offset, dst, dstOff, this.size);
        return this.size + dstOff;
    }

    public E get(int index) {
        Preconditions.checkElementIndex(index, this.size);
        return this.array[this.offset + index];
    }

    public int indexOf(@Nullable Object object) {
        if (object == null) {
            return -1;
        }
        for (int i = 0; i < this.size; i++) {
            if (this.array[this.offset + i].equals(object)) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(@Nullable Object object) {
        if (object == null) {
            return -1;
        }
        for (int i = this.size - 1; i >= 0; i--) {
            if (this.array[this.offset + i].equals(object)) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public ImmutableList<E> subListUnchecked(int fromIndex, int toIndex) {
        return new RegularImmutableList(this.array, this.offset + fromIndex, toIndex - fromIndex);
    }

    public UnmodifiableListIterator<E> listIterator(int index) {
        return Iterators.forArray(this.array, this.offset, this.size, index);
    }
}
