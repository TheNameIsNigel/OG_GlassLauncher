package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nullable;

@GwtCompatible
public final class Functions {
    private Functions() {
    }

    public static Function<Object, String> toStringFunction() {
        return ToStringFunction.INSTANCE;
    }

    private enum ToStringFunction implements Function<Object, String> {
        INSTANCE;

        public String apply(Object o) {
            Preconditions.checkNotNull(o);
            return o.toString();
        }

        public String toString() {
            return "toString";
        }
    }

    public static <E> Function<E, E> identity() {
        return IdentityFunction.INSTANCE;
    }

    private enum IdentityFunction implements Function<Object, Object> {
        INSTANCE;

        @Nullable
        public Object apply(@Nullable Object o) {
            return o;
        }

        public String toString() {
            return "identity";
        }
    }

    public static <K, V> Function<K, V> forMap(Map<K, V> map) {
        return new FunctionForMapNoDefault(map);
    }

    private static class FunctionForMapNoDefault<K, V> implements Function<K, V>, Serializable {
        private static final long serialVersionUID = 0;
        final Map<K, V> map;

        FunctionForMapNoDefault(Map<K, V> map2) {
            this.map = (Map) Preconditions.checkNotNull(map2);
        }

        public V apply(@Nullable K key) {
            boolean z;
            V result = this.map.get(key);
            if (result == null) {
                z = this.map.containsKey(key);
            } else {
                z = true;
            }
            Preconditions.checkArgument(z, "Key '%s' not present in map", key);
            return result;
        }

        public boolean equals(@Nullable Object o) {
            if (o instanceof FunctionForMapNoDefault) {
                return this.map.equals(((FunctionForMapNoDefault) o).map);
            }
            return false;
        }

        public int hashCode() {
            return this.map.hashCode();
        }

        public String toString() {
            return "forMap(" + this.map + ")";
        }
    }

    public static <K, V> Function<K, V> forMap(Map<K, ? extends V> map, @Nullable V defaultValue) {
        return new ForMapWithDefault(map, defaultValue);
    }

    private static class ForMapWithDefault<K, V> implements Function<K, V>, Serializable {
        private static final long serialVersionUID = 0;
        final V defaultValue;
        final Map<K, ? extends V> map;

        ForMapWithDefault(Map<K, ? extends V> map2, @Nullable V defaultValue2) {
            this.map = (Map) Preconditions.checkNotNull(map2);
            this.defaultValue = defaultValue2;
        }

        public V apply(@Nullable K key) {
            V result = this.map.get(key);
            return (result != null || this.map.containsKey(key)) ? result : this.defaultValue;
        }

        public boolean equals(@Nullable Object o) {
            if (!(o instanceof ForMapWithDefault)) {
                return false;
            }
            ForMapWithDefault<?, ?> that = (ForMapWithDefault) o;
            if (this.map.equals(that.map)) {
                return Objects.equal(this.defaultValue, that.defaultValue);
            }
            return false;
        }

        public int hashCode() {
            return Objects.hashCode(this.map, this.defaultValue);
        }

        public String toString() {
            return "forMap(" + this.map + ", defaultValue=" + this.defaultValue + ")";
        }
    }

    public static <A, B, C> Function<A, C> compose(Function<B, C> g, Function<A, ? extends B> f) {
        return new FunctionComposition(g, f);
    }

    private static class FunctionComposition<A, B, C> implements Function<A, C>, Serializable {
        private static final long serialVersionUID = 0;
        private final Function<A, ? extends B> f;
        private final Function<B, C> g;

        public FunctionComposition(Function<B, C> g2, Function<A, ? extends B> f2) {
            this.g = (Function) Preconditions.checkNotNull(g2);
            this.f = (Function) Preconditions.checkNotNull(f2);
        }

        public C apply(@Nullable A a) {
            return this.g.apply(this.f.apply(a));
        }

        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof FunctionComposition)) {
                return false;
            }
            FunctionComposition<?, ?, ?> that = (FunctionComposition) obj;
            if (this.f.equals(that.f)) {
                return this.g.equals(that.g);
            }
            return false;
        }

        public int hashCode() {
            return this.f.hashCode() ^ this.g.hashCode();
        }

        public String toString() {
            return this.g + "(" + this.f + ")";
        }
    }

    public static <T> Function<T, Boolean> forPredicate(Predicate<T> predicate) {
        return new PredicateFunction(predicate, (PredicateFunction) null);
    }

    private static class PredicateFunction<T> implements Function<T, Boolean>, Serializable {
        private static final long serialVersionUID = 0;
        private final Predicate<T> predicate;

        /* synthetic */ PredicateFunction(Predicate predicate2, PredicateFunction predicateFunction) {
            this(predicate2);
        }

        private PredicateFunction(Predicate<T> predicate2) {
            this.predicate = (Predicate) Preconditions.checkNotNull(predicate2);
        }

        public Boolean apply(@Nullable T t) {
            return Boolean.valueOf(this.predicate.apply(t));
        }

        public boolean equals(@Nullable Object obj) {
            if (obj instanceof PredicateFunction) {
                return this.predicate.equals(((PredicateFunction) obj).predicate);
            }
            return false;
        }

        public int hashCode() {
            return this.predicate.hashCode();
        }

        public String toString() {
            return "forPredicate(" + this.predicate + ")";
        }
    }

    public static <E> Function<Object, E> constant(@Nullable E value) {
        return new ConstantFunction(value);
    }

    private static class ConstantFunction<E> implements Function<Object, E>, Serializable {
        private static final long serialVersionUID = 0;
        private final E value;

        public ConstantFunction(@Nullable E value2) {
            this.value = value2;
        }

        public E apply(@Nullable Object from) {
            return this.value;
        }

        public boolean equals(@Nullable Object obj) {
            if (obj instanceof ConstantFunction) {
                return Objects.equal(this.value, ((ConstantFunction) obj).value);
            }
            return false;
        }

        public int hashCode() {
            if (this.value == null) {
                return 0;
            }
            return this.value.hashCode();
        }

        public String toString() {
            return "constant(" + this.value + ")";
        }
    }

    @Beta
    public static <T> Function<Object, T> forSupplier(Supplier<T> supplier) {
        return new SupplierFunction(supplier, (SupplierFunction) null);
    }

    private static class SupplierFunction<T> implements Function<Object, T>, Serializable {
        private static final long serialVersionUID = 0;
        private final Supplier<T> supplier;

        /* synthetic */ SupplierFunction(Supplier supplier2, SupplierFunction supplierFunction) {
            this(supplier2);
        }

        private SupplierFunction(Supplier<T> supplier2) {
            this.supplier = (Supplier) Preconditions.checkNotNull(supplier2);
        }

        public T apply(@Nullable Object input) {
            return this.supplier.get();
        }

        public boolean equals(@Nullable Object obj) {
            if (obj instanceof SupplierFunction) {
                return this.supplier.equals(((SupplierFunction) obj).supplier);
            }
            return false;
        }

        public int hashCode() {
            return this.supplier.hashCode();
        }

        public String toString() {
            return "forSupplier(" + this.supplier + ")";
        }
    }
}
