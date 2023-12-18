package com.google.common.math;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@GwtCompatible(emulated = true)
public final class BigIntegerMath {

    /* renamed from: -java-math-RoundingModeSwitchesValues  reason: not valid java name */
    private static final /* synthetic */ int[] f28javamathRoundingModeSwitchesValues = null;
    private static final double LN_10 = Math.log(10.0d);
    private static final double LN_2 = Math.log(2.0d);
    @VisibleForTesting
    static final BigInteger SQRT2_PRECOMPUTED_BITS = new BigInteger("16a09e667f3bcc908b2fb1366ea957d3e3adec17512775099da2f590b0667322a", 16);
    @VisibleForTesting
    static final int SQRT2_PRECOMPUTE_THRESHOLD = 256;

    /* renamed from: -getjava-math-RoundingModeSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m402getjavamathRoundingModeSwitchesValues() {
        if (f28javamathRoundingModeSwitchesValues != null) {
            return f28javamathRoundingModeSwitchesValues;
        }
        int[] iArr = new int[RoundingMode.values().length];
        try {
            iArr[RoundingMode.CEILING.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RoundingMode.DOWN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RoundingMode.FLOOR.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[RoundingMode.HALF_DOWN.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[RoundingMode.HALF_EVEN.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[RoundingMode.HALF_UP.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[RoundingMode.UNNECESSARY.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[RoundingMode.UP.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        f28javamathRoundingModeSwitchesValues = iArr;
        return iArr;
    }

    public static boolean isPowerOfTwo(BigInteger x) {
        Preconditions.checkNotNull(x);
        if (x.signum() <= 0 || x.getLowestSetBit() != x.bitLength() - 1) {
            return false;
        }
        return true;
    }

    public static int log2(BigInteger x, RoundingMode mode) {
        MathPreconditions.checkPositive("x", (BigInteger) Preconditions.checkNotNull(x));
        int logFloor = x.bitLength() - 1;
        switch (m402getjavamathRoundingModeSwitchesValues()[mode.ordinal()]) {
            case 1:
            case 8:
                return isPowerOfTwo(x) ? logFloor : logFloor + 1;
            case 2:
            case 3:
                break;
            case 4:
            case 5:
            case 6:
                if (logFloor >= 256) {
                    return x.pow(2).bitLength() + -1 < (logFloor * 2) + 1 ? logFloor : logFloor + 1;
                }
                if (x.compareTo(SQRT2_PRECOMPUTED_BITS.shiftRight(256 - logFloor)) <= 0) {
                    return logFloor;
                }
                return logFloor + 1;
            case 7:
                MathPreconditions.checkRoundingUnnecessary(isPowerOfTwo(x));
                break;
            default:
                throw new AssertionError();
        }
        return logFloor;
    }

    @GwtIncompatible("TODO")
    public static int log10(BigInteger x, RoundingMode mode) {
        MathPreconditions.checkPositive("x", x);
        if (fitsInLong(x)) {
            return LongMath.log10(x.longValue(), mode);
        }
        int approxLog10 = (int) ((((double) log2(x, RoundingMode.FLOOR)) * LN_2) / LN_10);
        BigInteger approxPow = BigInteger.TEN.pow(approxLog10);
        int approxCmp = approxPow.compareTo(x);
        if (approxCmp > 0) {
            do {
                approxLog10--;
                approxPow = approxPow.divide(BigInteger.TEN);
                approxCmp = approxPow.compareTo(x);
            } while (approxCmp > 0);
        } else {
            BigInteger nextPow = BigInteger.TEN.multiply(approxPow);
            int nextCmp = nextPow.compareTo(x);
            while (nextCmp <= 0) {
                approxLog10++;
                approxPow = nextPow;
                approxCmp = nextCmp;
                nextPow = BigInteger.TEN.multiply(approxPow);
                nextCmp = nextPow.compareTo(x);
            }
        }
        int floorLog = approxLog10;
        BigInteger floorPow = approxPow;
        int floorCmp = approxCmp;
        switch (m402getjavamathRoundingModeSwitchesValues()[mode.ordinal()]) {
            case 1:
            case 8:
                return floorPow.equals(x) ? floorLog : floorLog + 1;
            case 2:
            case 3:
                break;
            case 4:
            case 5:
            case 6:
                return x.pow(2).compareTo(floorPow.pow(2).multiply(BigInteger.TEN)) <= 0 ? floorLog : floorLog + 1;
            case 7:
                MathPreconditions.checkRoundingUnnecessary(floorCmp == 0);
                break;
            default:
                throw new AssertionError();
        }
        return floorLog;
    }

    @GwtIncompatible("TODO")
    public static BigInteger sqrt(BigInteger x, RoundingMode mode) {
        boolean z;
        MathPreconditions.checkNonNegative("x", x);
        if (fitsInLong(x)) {
            return BigInteger.valueOf(LongMath.sqrt(x.longValue(), mode));
        }
        BigInteger sqrtFloor = sqrtFloor(x);
        switch (m402getjavamathRoundingModeSwitchesValues()[mode.ordinal()]) {
            case 1:
            case 8:
                int sqrtFloorInt = sqrtFloor.intValue();
                if (sqrtFloorInt * sqrtFloorInt == x.intValue()) {
                    z = sqrtFloor.pow(2).equals(x);
                } else {
                    z = false;
                }
                return z ? sqrtFloor : sqrtFloor.add(BigInteger.ONE);
            case 2:
            case 3:
                break;
            case 4:
            case 5:
            case 6:
                if (sqrtFloor.pow(2).add(sqrtFloor).compareTo(x) >= 0) {
                    return sqrtFloor;
                }
                return sqrtFloor.add(BigInteger.ONE);
            case 7:
                MathPreconditions.checkRoundingUnnecessary(sqrtFloor.pow(2).equals(x));
                break;
            default:
                throw new AssertionError();
        }
        return sqrtFloor;
    }

    @GwtIncompatible("TODO")
    private static BigInteger sqrtFloor(BigInteger x) {
        BigInteger sqrt0;
        BigInteger sqrt02;
        int log2 = log2(x, RoundingMode.FLOOR);
        if (log2 < 1023) {
            sqrt0 = sqrtApproxWithDoubles(x);
        } else {
            int shift = (log2 - 52) & -2;
            sqrt0 = sqrtApproxWithDoubles(x.shiftRight(shift)).shiftLeft(shift >> 1);
        }
        BigInteger sqrt1 = sqrt0.add(x.divide(sqrt0)).shiftRight(1);
        if (sqrt0.equals(sqrt1)) {
            return sqrt0;
        }
        do {
            sqrt02 = sqrt1;
            sqrt1 = sqrt02.add(x.divide(sqrt1)).shiftRight(1);
        } while (sqrt1.compareTo(sqrt02) < 0);
        return sqrt02;
    }

    @GwtIncompatible("TODO")
    private static BigInteger sqrtApproxWithDoubles(BigInteger x) {
        return DoubleMath.roundToBigInteger(Math.sqrt(DoubleUtils.bigToDouble(x)), RoundingMode.HALF_EVEN);
    }

    @GwtIncompatible("TODO")
    public static BigInteger divide(BigInteger p, BigInteger q, RoundingMode mode) {
        return new BigDecimal(p).divide(new BigDecimal(q), 0, mode).toBigIntegerExact();
    }

    public static BigInteger factorial(int n) {
        MathPreconditions.checkNonNegative("n", n);
        if (n < LongMath.factorials.length) {
            return BigInteger.valueOf(LongMath.factorials[n]);
        }
        ArrayList<BigInteger> bignums = new ArrayList<>(IntMath.divide(IntMath.log2(n, RoundingMode.CEILING) * n, 64, RoundingMode.CEILING));
        int startingNumber = LongMath.factorials.length;
        long product = LongMath.factorials[startingNumber - 1];
        int shift = Long.numberOfTrailingZeros(product);
        long product2 = product >> shift;
        int productBits = LongMath.log2(product2, RoundingMode.FLOOR) + 1;
        int bits = LongMath.log2((long) startingNumber, RoundingMode.FLOOR) + 1;
        int nextPowerOfTwo = 1 << (bits - 1);
        for (long num = (long) startingNumber; num <= ((long) n); num++) {
            if ((((long) nextPowerOfTwo) & num) != 0) {
                nextPowerOfTwo <<= 1;
                bits++;
            }
            int tz = Long.numberOfTrailingZeros(num);
            long normalizedNum = num >> tz;
            shift += tz;
            if ((bits - tz) + productBits >= 64) {
                bignums.add(BigInteger.valueOf(product2));
                product2 = 1;
            }
            product2 *= normalizedNum;
            productBits = LongMath.log2(product2, RoundingMode.FLOOR) + 1;
        }
        if (product2 > 1) {
            bignums.add(BigInteger.valueOf(product2));
        }
        return listProduct(bignums).shiftLeft(shift);
    }

    static BigInteger listProduct(List<BigInteger> nums) {
        return listProduct(nums, 0, nums.size());
    }

    static BigInteger listProduct(List<BigInteger> nums, int start, int end) {
        switch (end - start) {
            case 0:
                return BigInteger.ONE;
            case 1:
                return nums.get(start);
            case 2:
                return nums.get(start).multiply(nums.get(start + 1));
            case 3:
                return nums.get(start).multiply(nums.get(start + 1)).multiply(nums.get(start + 2));
            default:
                int m = (end + start) >>> 1;
                return listProduct(nums, start, m).multiply(listProduct(nums, m, end));
        }
    }

    public static BigInteger binomial(int n, int k) {
        MathPreconditions.checkNonNegative("n", n);
        MathPreconditions.checkNonNegative("k", k);
        Preconditions.checkArgument(k <= n, "k (%s) > n (%s)", Integer.valueOf(k), Integer.valueOf(n));
        if (k > (n >> 1)) {
            k = n - k;
        }
        if (k < LongMath.biggestBinomials.length && n <= LongMath.biggestBinomials[k]) {
            return BigInteger.valueOf(LongMath.binomial(n, k));
        }
        BigInteger accum = BigInteger.ONE;
        long numeratorAccum = (long) n;
        long denominatorAccum = 1;
        int bits = LongMath.log2((long) n, RoundingMode.CEILING);
        int numeratorBits = bits;
        for (int i = 1; i < k; i++) {
            int p = n - i;
            int q = i + 1;
            if (numeratorBits + bits >= 63) {
                accum = accum.multiply(BigInteger.valueOf(numeratorAccum)).divide(BigInteger.valueOf(denominatorAccum));
                numeratorAccum = (long) p;
                denominatorAccum = (long) q;
                numeratorBits = bits;
            } else {
                numeratorAccum *= (long) p;
                denominatorAccum *= (long) q;
                numeratorBits += bits;
            }
        }
        return accum.multiply(BigInteger.valueOf(numeratorAccum)).divide(BigInteger.valueOf(denominatorAccum));
    }

    @GwtIncompatible("TODO")
    static boolean fitsInLong(BigInteger x) {
        return x.bitLength() <= 63;
    }

    private BigIntegerMath() {
    }
}
