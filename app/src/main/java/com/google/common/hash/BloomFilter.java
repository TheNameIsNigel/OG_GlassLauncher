package com.google.common.hash;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.hash.BloomFilterStrategies;
import com.google.common.primitives.SignedBytes;
import com.google.common.primitives.UnsignedBytes;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import javax.annotation.Nullable;

@Beta
public final class BloomFilter<T> implements Predicate<T>, Serializable {
    private static final Strategy DEFAULT_STRATEGY = BloomFilterStrategies.MURMUR128_MITZ_64;
    /* access modifiers changed from: private */
    public final BloomFilterStrategies.BitArray bits;
    /* access modifiers changed from: private */
    public final Funnel<? super T> funnel;
    /* access modifiers changed from: private */
    public final int numHashFunctions;
    /* access modifiers changed from: private */
    public final Strategy strategy;

    interface Strategy extends Serializable {
        <T> boolean mightContain(T t, Funnel<? super T> funnel, int i, BloomFilterStrategies.BitArray bitArray);

        int ordinal();

        <T> boolean put(T t, Funnel<? super T> funnel, int i, BloomFilterStrategies.BitArray bitArray);
    }

    /* synthetic */ BloomFilter(BloomFilterStrategies.BitArray bits2, int numHashFunctions2, Funnel funnel2, Strategy strategy2, BloomFilter bloomFilter) {
        this(bits2, numHashFunctions2, funnel2, strategy2);
    }

    private BloomFilter(BloomFilterStrategies.BitArray bits2, int numHashFunctions2, Funnel<? super T> funnel2, Strategy strategy2) {
        boolean z;
        Preconditions.checkArgument(numHashFunctions2 > 0, "numHashFunctions (%s) must be > 0", Integer.valueOf(numHashFunctions2));
        if (numHashFunctions2 <= 255) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z, "numHashFunctions (%s) must be <= 255", Integer.valueOf(numHashFunctions2));
        this.bits = (BloomFilterStrategies.BitArray) Preconditions.checkNotNull(bits2);
        this.numHashFunctions = numHashFunctions2;
        this.funnel = (Funnel) Preconditions.checkNotNull(funnel2);
        this.strategy = (Strategy) Preconditions.checkNotNull(strategy2);
    }

    public BloomFilter<T> copy() {
        return new BloomFilter<>(this.bits.copy(), this.numHashFunctions, this.funnel, this.strategy);
    }

    public boolean mightContain(T object) {
        return this.strategy.mightContain(object, this.funnel, this.numHashFunctions, this.bits);
    }

    @Deprecated
    public boolean apply(T input) {
        return mightContain(input);
    }

    public boolean put(T object) {
        return this.strategy.put(object, this.funnel, this.numHashFunctions, this.bits);
    }

    public double expectedFpp() {
        return Math.pow(((double) this.bits.bitCount()) / ((double) bitSize()), (double) this.numHashFunctions);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public long bitSize() {
        return this.bits.bitSize();
    }

    public boolean isCompatible(BloomFilter<T> that) {
        Preconditions.checkNotNull(that);
        if (this == that || this.numHashFunctions != that.numHashFunctions || bitSize() != that.bitSize() || !this.strategy.equals(that.strategy)) {
            return false;
        }
        return this.funnel.equals(that.funnel);
    }

    public void putAll(BloomFilter<T> that) {
        boolean z;
        boolean z2;
        Preconditions.checkNotNull(that);
        Preconditions.checkArgument(this != that, "Cannot combine a BloomFilter with itself.");
        if (this.numHashFunctions == that.numHashFunctions) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z, "BloomFilters must have the same number of hash functions (%s != %s)", Integer.valueOf(this.numHashFunctions), Integer.valueOf(that.numHashFunctions));
        if (bitSize() == that.bitSize()) {
            z2 = true;
        } else {
            z2 = false;
        }
        Preconditions.checkArgument(z2, "BloomFilters must have the same size underlying bit arrays (%s != %s)", Long.valueOf(bitSize()), Long.valueOf(that.bitSize()));
        Preconditions.checkArgument(this.strategy.equals(that.strategy), "BloomFilters must have equal strategies (%s != %s)", this.strategy, that.strategy);
        Preconditions.checkArgument(this.funnel.equals(that.funnel), "BloomFilters must have equal funnels (%s != %s)", this.funnel, that.funnel);
        this.bits.putAll(that.bits);
    }

    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof BloomFilter)) {
            return false;
        }
        BloomFilter<?> that = (BloomFilter) object;
        if (this.numHashFunctions != that.numHashFunctions || !this.funnel.equals(that.funnel) || !this.bits.equals(that.bits)) {
            return false;
        }
        return this.strategy.equals(that.strategy);
    }

    public int hashCode() {
        return Objects.hashCode(Integer.valueOf(this.numHashFunctions), this.funnel, this.strategy, this.bits);
    }

    public static <T> BloomFilter<T> create(Funnel<? super T> funnel2, int expectedInsertions, double fpp) {
        return create(funnel2, expectedInsertions, fpp, DEFAULT_STRATEGY);
    }

    @VisibleForTesting
    static <T> BloomFilter<T> create(Funnel<? super T> funnel2, int expectedInsertions, double fpp, Strategy strategy2) {
        boolean z;
        boolean z2;
        boolean z3;
        Preconditions.checkNotNull(funnel2);
        if (expectedInsertions >= 0) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z, "Expected insertions (%s) must be >= 0", Integer.valueOf(expectedInsertions));
        if (fpp > 0.0d) {
            z2 = true;
        } else {
            z2 = false;
        }
        Preconditions.checkArgument(z2, "False positive probability (%s) must be > 0.0", Double.valueOf(fpp));
        if (fpp < 1.0d) {
            z3 = true;
        } else {
            z3 = false;
        }
        Preconditions.checkArgument(z3, "False positive probability (%s) must be < 1.0", Double.valueOf(fpp));
        Preconditions.checkNotNull(strategy2);
        if (expectedInsertions == 0) {
            expectedInsertions = 1;
        }
        long numBits = optimalNumOfBits((long) expectedInsertions, fpp);
        try {
            return new BloomFilter<>(new BloomFilterStrategies.BitArray(numBits), optimalNumOfHashFunctions((long) expectedInsertions, numBits), funnel2, strategy2);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create BloomFilter of " + numBits + " bits", e);
        }
    }

    public static <T> BloomFilter<T> create(Funnel<? super T> funnel2, int expectedInsertions) {
        return create(funnel2, expectedInsertions, 0.03d);
    }

    @VisibleForTesting
    static int optimalNumOfHashFunctions(long n, long m) {
        return Math.max(1, (int) Math.round((((double) m) / ((double) n)) * Math.log(2.0d)));
    }

    @VisibleForTesting
    static long optimalNumOfBits(long n, double p) {
        if (p == 0.0d) {
            p = Double.MIN_VALUE;
        }
        return (long) ((((double) (-n)) * Math.log(p)) / (Math.log(2.0d) * Math.log(2.0d)));
    }

    private Object writeReplace() {
        return new SerialForm(this);
    }

    private static class SerialForm<T> implements Serializable {
        private static final long serialVersionUID = 1;
        final long[] data;
        final Funnel<? super T> funnel;
        final int numHashFunctions;
        final Strategy strategy;

        SerialForm(BloomFilter<T> bf) {
            this.data = bf.bits.data;
            this.numHashFunctions = bf.numHashFunctions;
            this.funnel = bf.funnel;
            this.strategy = bf.strategy;
        }

        /* access modifiers changed from: package-private */
        public Object readResolve() {
            return new BloomFilter(new BloomFilterStrategies.BitArray(this.data), this.numHashFunctions, this.funnel, this.strategy, (BloomFilter) null);
        }
    }

    public void writeTo(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        dout.writeByte(SignedBytes.checkedCast((long) this.strategy.ordinal()));
        dout.writeByte(UnsignedBytes.checkedCast((long) this.numHashFunctions));
        dout.writeInt(this.bits.data.length);
        for (long value : this.bits.data) {
            dout.writeLong(value);
        }
    }

    public static <T> BloomFilter<T> readFrom(InputStream in, Funnel<T> funnel2) throws IOException {
        Preconditions.checkNotNull(in, "InputStream");
        Preconditions.checkNotNull(funnel2, "Funnel");
        try {
            DataInputStream din = new DataInputStream(in);
            int strategyOrdinal = din.readByte();
            int numHashFunctions2 = UnsignedBytes.toInt(din.readByte());
            int dataLength = din.readInt();
            Strategy strategy2 = BloomFilterStrategies.values()[strategyOrdinal];
            long[] data = new long[dataLength];
            for (int i = 0; i < data.length; i++) {
                data[i] = din.readLong();
            }
            return new BloomFilter<>(new BloomFilterStrategies.BitArray(data), numHashFunctions2, funnel2, strategy2);
        } catch (RuntimeException e) {
            IOException ioException = new IOException("Unable to deserialize BloomFilter from InputStream. strategyOrdinal: " + -1 + " numHashFunctions: " + -1 + " dataLength: " + -1);
            ioException.initCause(e);
            throw ioException;
        }
    }
}
