package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;

@GwtCompatible
public abstract class ForwardingList<E> extends ForwardingCollection<E> implements List<E> {
    /* access modifiers changed from: protected */
    public abstract List<E> delegate();

    protected ForwardingList() {
    }

    public void add(int index, E element) {
        delegate().add(index, element);
    }

    public boolean addAll(int index, Collection<? extends E> elements) {
        return delegate().addAll(index, elements);
    }

    public E get(int index) {
        return delegate().get(index);
    }

    public int indexOf(Object element) {
        return delegate().indexOf(element);
    }

    public int lastIndexOf(Object element) {
        return delegate().lastIndexOf(element);
    }

    public ListIterator<E> listIterator() {
        return delegate().listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        return delegate().listIterator(index);
    }

    public E remove(int index) {
        return delegate().remove(index);
    }

    public E set(int index, E element) {
        return delegate().set(index, element);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return delegate().subList(fromIndex, toIndex);
    }

    public boolean equals(@Nullable Object object) {
        if (object != this) {
            return delegate().equals(object);
        }
        return true;
    }

    public int hashCode() {
        return delegate().hashCode();
    }

    /* access modifiers changed from: protected */
    public boolean standardAdd(E element) {
        add(size(), element);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean standardAddAll(int index, Iterable<? extends E> elements) {
        return Lists.addAllImpl(this, index, elements);
    }

    /* access modifiers changed from: protected */
    public int standardIndexOf(@Nullable Object element) {
        return Lists.indexOfImpl(this, element);
    }

    /* access modifiers changed from: protected */
    public int standardLastIndexOf(@Nullable Object element) {
        return Lists.lastIndexOfImpl(this, element);
    }

    /* access modifiers changed from: protected */
    public Iterator<E> standardIterator() {
        return listIterator();
    }

    /* access modifiers changed from: protected */
    public ListIterator<E> standardListIterator() {
        return listIterator(0);
    }

    /* access modifiers changed from: protected */
    @Beta
    public ListIterator<E> standardListIterator(int start) {
        return Lists.listIteratorImpl(this, start);
    }

    /* access modifiers changed from: protected */
    @Beta
    public List<E> standardSubList(int fromIndex, int toIndex) {
        return Lists.subListImpl(this, fromIndex, toIndex);
    }

    /* access modifiers changed from: protected */
    @Beta
    public boolean standardEquals(@Nullable Object object) {
        return Lists.equalsImpl(this, object);
    }

    /* access modifiers changed from: protected */
    @Beta
    public int standardHashCode() {
        return Lists.hashCodeImpl(this);
    }
}
