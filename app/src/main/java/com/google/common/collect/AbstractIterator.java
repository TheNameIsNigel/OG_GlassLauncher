package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.NoSuchElementException;

@GwtCompatible
public abstract class AbstractIterator<T> extends UnmodifiableIterator<T> {

    /* renamed from: -com-google-common-collect-AbstractIterator$StateSwitchesValues  reason: not valid java name */
    private static final /* synthetic */ int[] f23comgooglecommoncollectAbstractIterator$StateSwitchesValues = null;
    private T next;
    private State state = State.NOT_READY;

    private enum State {
        READY,
        NOT_READY,
        DONE,
        FAILED
    }

    /* renamed from: -getcom-google-common-collect-AbstractIterator$StateSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m229getcomgooglecommoncollectAbstractIterator$StateSwitchesValues() {
        if (f23comgooglecommoncollectAbstractIterator$StateSwitchesValues != null) {
            return f23comgooglecommoncollectAbstractIterator$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.DONE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.FAILED.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.NOT_READY.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.READY.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        f23comgooglecommoncollectAbstractIterator$StateSwitchesValues = iArr;
        return iArr;
    }

    /* access modifiers changed from: protected */
    public abstract T computeNext();

    protected AbstractIterator() {
    }

    /* access modifiers changed from: protected */
    public final T endOfData() {
        this.state = State.DONE;
        return null;
    }

    public final boolean hasNext() {
        Preconditions.checkState(this.state != State.FAILED);
        switch (m229getcomgooglecommoncollectAbstractIterator$StateSwitchesValues()[this.state.ordinal()]) {
            case 1:
                return false;
            case 2:
                return true;
            default:
                return tryToComputeNext();
        }
    }

    private boolean tryToComputeNext() {
        this.state = State.FAILED;
        this.next = computeNext();
        if (this.state == State.DONE) {
            return false;
        }
        this.state = State.READY;
        return true;
    }

    public final T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        this.state = State.NOT_READY;
        T result = this.next;
        this.next = null;
        return result;
    }

    public final T peek() {
        if (hasNext()) {
            return this.next;
        }
        throw new NoSuchElementException();
    }
}
