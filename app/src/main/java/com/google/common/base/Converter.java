package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import java.io.Serializable;
import java.util.Iterator;
import javax.annotation.Nullable;

@GwtCompatible
@Beta
public abstract class Converter<A, B> implements Function<A, B> {
    private final boolean handleNullAutomatically;
    private transient Converter<B, A> reverse;

    /* access modifiers changed from: protected */
    public abstract A doBackward(B b);

    /* access modifiers changed from: protected */
    public abstract B doForward(A a);

    protected Converter() {
        this(true);
    }

    Converter(boolean handleNullAutomatically2) {
        this.handleNullAutomatically = handleNullAutomatically2;
    }

    @Nullable
    public final B convert(@Nullable A a) {
        return correctedDoForward(a);
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public B correctedDoForward(@Nullable A a) {
        if (!this.handleNullAutomatically) {
            return doForward(a);
        }
        if (a == null) {
            return null;
        }
        return Preconditions.checkNotNull(doForward(a));
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public A correctedDoBackward(@Nullable B b) {
        if (!this.handleNullAutomatically) {
            return doBackward(b);
        }
        if (b == null) {
            return null;
        }
        return Preconditions.checkNotNull(doBackward(b));
    }

    public Iterable<B> convertAll(final Iterable<? extends A> fromIterable) {
        Preconditions.checkNotNull(fromIterable, "fromIterable");
        return new Iterable<B>() {
            public Iterator<B> iterator() {
                final Iterable iterable = fromIterable;
                return new Iterator<B>() {
                    private final Iterator<? extends A> fromIterator = iterable.iterator();

                    public boolean hasNext() {
                        return this.fromIterator.hasNext();
                    }

                    public B next() {
                        return Converter.this.convert(this.fromIterator.next());
                    }

                    public void remove() {
                        this.fromIterator.remove();
                    }
                };
            }
        };
    }

    public Converter<B, A> reverse() {
        Converter<B, A> result = this.reverse;
        if (result != null) {
            return result;
        }
        Converter<B, A> result2 = new ReverseConverter<>(this);
        this.reverse = result2;
        return result2;
    }

    private static final class ReverseConverter<A, B> extends Converter<B, A> implements Serializable {
        private static final long serialVersionUID = 0;
        final Converter<A, B> original;

        ReverseConverter(Converter<A, B> original2) {
            this.original = original2;
        }

        /* access modifiers changed from: protected */
        public A doForward(B b) {
            throw new AssertionError();
        }

        /* access modifiers changed from: protected */
        public B doBackward(A a) {
            throw new AssertionError();
        }

        /* access modifiers changed from: package-private */
        @Nullable
        public A correctedDoForward(@Nullable B b) {
            return this.original.correctedDoBackward(b);
        }

        /* access modifiers changed from: package-private */
        @Nullable
        public B correctedDoBackward(@Nullable A a) {
            return this.original.correctedDoForward(a);
        }

        public Converter<A, B> reverse() {
            return this.original;
        }

        public boolean equals(@Nullable Object object) {
            if (object instanceof ReverseConverter) {
                return this.original.equals(((ReverseConverter) object).original);
            }
            return false;
        }

        public int hashCode() {
            return ~this.original.hashCode();
        }

        public String toString() {
            return this.original + ".reverse()";
        }
    }

    public final <C> Converter<A, C> andThen(Converter<B, C> secondConverter) {
        return doAndThen(secondConverter);
    }

    /* access modifiers changed from: package-private */
    public <C> Converter<A, C> doAndThen(Converter<B, C> secondConverter) {
        return new ConverterComposition(this, (Converter) Preconditions.checkNotNull(secondConverter));
    }

    private static final class ConverterComposition<A, B, C> extends Converter<A, C> implements Serializable {
        private static final long serialVersionUID = 0;
        final Converter<A, B> first;
        final Converter<B, C> second;

        ConverterComposition(Converter<A, B> first2, Converter<B, C> second2) {
            this.first = first2;
            this.second = second2;
        }

        /* access modifiers changed from: protected */
        public C doForward(A a) {
            throw new AssertionError();
        }

        /* access modifiers changed from: protected */
        public A doBackward(C c) {
            throw new AssertionError();
        }

        /* access modifiers changed from: package-private */
        @Nullable
        public C correctedDoForward(@Nullable A a) {
            return this.second.correctedDoForward(this.first.correctedDoForward(a));
        }

        /* access modifiers changed from: package-private */
        @Nullable
        public A correctedDoBackward(@Nullable C c) {
            return this.first.correctedDoBackward(this.second.correctedDoBackward(c));
        }

        public boolean equals(@Nullable Object object) {
            if (!(object instanceof ConverterComposition)) {
                return false;
            }
            ConverterComposition<?, ?, ?> that = (ConverterComposition) object;
            if (this.first.equals(that.first)) {
                return this.second.equals(that.second);
            }
            return false;
        }

        public int hashCode() {
            return (this.first.hashCode() * 31) + this.second.hashCode();
        }

        public String toString() {
            return this.first + ".andThen(" + this.second + ")";
        }
    }

    @Deprecated
    @Nullable
    public final B apply(@Nullable A a) {
        return convert(a);
    }

    public boolean equals(@Nullable Object object) {
        return super.equals(object);
    }

    public static <A, B> Converter<A, B> from(Function<? super A, ? extends B> forwardFunction, Function<? super B, ? extends A> backwardFunction) {
        return new FunctionBasedConverter(forwardFunction, backwardFunction, (FunctionBasedConverter) null);
    }

    private static final class FunctionBasedConverter<A, B> extends Converter<A, B> implements Serializable {
        private final Function<? super B, ? extends A> backwardFunction;
        private final Function<? super A, ? extends B> forwardFunction;

        /* synthetic */ FunctionBasedConverter(Function forwardFunction2, Function backwardFunction2, FunctionBasedConverter functionBasedConverter) {
            this(forwardFunction2, backwardFunction2);
        }

        private FunctionBasedConverter(Function<? super A, ? extends B> forwardFunction2, Function<? super B, ? extends A> backwardFunction2) {
            this.forwardFunction = (Function) Preconditions.checkNotNull(forwardFunction2);
            this.backwardFunction = (Function) Preconditions.checkNotNull(backwardFunction2);
        }

        /* access modifiers changed from: protected */
        public B doForward(A a) {
            return this.forwardFunction.apply(a);
        }

        /* access modifiers changed from: protected */
        public A doBackward(B b) {
            return this.backwardFunction.apply(b);
        }

        public boolean equals(@Nullable Object object) {
            if (!(object instanceof FunctionBasedConverter)) {
                return false;
            }
            FunctionBasedConverter<?, ?> that = (FunctionBasedConverter) object;
            if (this.forwardFunction.equals(that.forwardFunction)) {
                return this.backwardFunction.equals(that.backwardFunction);
            }
            return false;
        }

        public int hashCode() {
            return (this.forwardFunction.hashCode() * 31) + this.backwardFunction.hashCode();
        }

        public String toString() {
            return "Converter.from(" + this.forwardFunction + ", " + this.backwardFunction + ")";
        }
    }

    public static <T> Converter<T, T> identity() {
        return IdentityConverter.INSTANCE;
    }

    private static final class IdentityConverter<T> extends Converter<T, T> implements Serializable {
        static final IdentityConverter INSTANCE = new IdentityConverter();
        private static final long serialVersionUID = 0;

        private IdentityConverter() {
        }

        /* access modifiers changed from: protected */
        public T doForward(T t) {
            return t;
        }

        /* access modifiers changed from: protected */
        public T doBackward(T t) {
            return t;
        }

        public IdentityConverter<T> reverse() {
            return this;
        }

        /* access modifiers changed from: package-private */
        public <S> Converter<T, S> doAndThen(Converter<T, S> otherConverter) {
            return (Converter) Preconditions.checkNotNull(otherConverter, "otherConverter");
        }

        public String toString() {
            return "Converter.identity()";
        }

        private Object readResolve() {
            return INSTANCE;
        }
    }
}
