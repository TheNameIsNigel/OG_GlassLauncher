package com.google.common.math;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import java.math.RoundingMode;

@GwtCompatible(emulated = true)
public final class LongMath {

    /* renamed from: -java-math-RoundingModeSwitchesValues  reason: not valid java name */
    private static final /* synthetic */ int[] f31javamathRoundingModeSwitchesValues = null;
    @VisibleForTesting
    static final long FLOOR_SQRT_MAX_LONG = 3037000499L;
    @VisibleForTesting
    static final long MAX_POWER_OF_SQRT2_UNSIGNED = -5402926248376769404L;
    static final int[] biggestBinomials = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 3810779, 121977, 16175, 4337, 1733, 887, 534, 361, 265, 206, 169, 143, 125, 111, 101, 94, 88, 83, 79, 76, 74, 72, 70, 69, 68, 67, 67, 66, 66, 66, 66};
    @VisibleForTesting
    static final int[] biggestSimpleBinomials = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2642246, 86251, 11724, 3218, 1313, 684, 419, 287, 214, 169, 139, 119, 105, 95, 87, 81, 76, 73, 70, 68, 66, 64, 63, 62, 62, 61, 61, 61};
    static final long[] factorials = {1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800, 39916800, 479001600, 6227020800L, 87178291200L, 1307674368000L, 20922789888000L, 355687428096000L, 6402373705728000L, 121645100408832000L, 2432902008176640000L};
    @GwtIncompatible("TODO")
    @VisibleForTesting
    static final long[] halfPowersOf10 = {3, 31, 316, 3162, 31622, 316227, 3162277, 31622776, 316227766, 3162277660L, 31622776601L, 316227766016L, 3162277660168L, 31622776601683L, 316227766016837L, 3162277660168379L, 31622776601683793L, 316227766016837933L, 3162277660168379331L};
    @VisibleForTesting
    static final byte[] maxLog10ForLeadingZeros = {19, Ascii.DC2, Ascii.DC2, Ascii.DC2, Ascii.DC2, 17, 17, 17, Ascii.DLE, Ascii.DLE, Ascii.DLE, Ascii.SI, Ascii.SI, Ascii.SI, Ascii.SI, Ascii.SO, Ascii.SO, Ascii.SO, Ascii.CR, Ascii.CR, Ascii.CR, Ascii.FF, Ascii.FF, Ascii.FF, Ascii.FF, Ascii.VT, Ascii.VT, Ascii.VT, 10, 10, 10, 9, 9, 9, 9, 8, 8, 8, 7, 7, 7, 6, 6, 6, 6, 5, 5, 5, 4, 4, 4, 3, 3, 3, 3, 2, 2, 2, 1, 1, 1, 0, 0, 0};
    @GwtIncompatible("TODO")
    @VisibleForTesting
    static final long[] powersOf10 = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L, 10000000000000000L, 100000000000000000L, 1000000000000000000L};

    /* renamed from: -getjava-math-RoundingModeSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m405getjavamathRoundingModeSwitchesValues() {
        if (f31javamathRoundingModeSwitchesValues != null) {
            return f31javamathRoundingModeSwitchesValues;
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
        f31javamathRoundingModeSwitchesValues = iArr;
        return iArr;
    }

    public static boolean isPowerOfTwo(long x) {
        boolean z = true;
        boolean z2 = x > 0;
        if (((x - 1) & x) != 0) {
            z = false;
        }
        return z & z2;
    }

    @VisibleForTesting
    static int lessThanBranchFree(long x, long y) {
        return (int) ((~(~(x - y))) >>> 63);
    }

    public static int log2(long x, RoundingMode mode) {
        MathPreconditions.checkPositive("x", x);
        switch (m405getjavamathRoundingModeSwitchesValues()[mode.ordinal()]) {
            case 1:
            case 8:
                return 64 - Long.numberOfLeadingZeros(x - 1);
            case 2:
            case 3:
                break;
            case 4:
            case 5:
            case 6:
                int leadingZeros = Long.numberOfLeadingZeros(x);
                return lessThanBranchFree(MAX_POWER_OF_SQRT2_UNSIGNED >>> leadingZeros, x) + (63 - leadingZeros);
            case 7:
                MathPreconditions.checkRoundingUnnecessary(isPowerOfTwo(x));
                break;
            default:
                throw new AssertionError("impossible");
        }
        return 63 - Long.numberOfLeadingZeros(x);
    }

    @GwtIncompatible("TODO")
    public static int log10(long x, RoundingMode mode) {
        MathPreconditions.checkPositive("x", x);
        int logFloor = log10Floor(x);
        long floorPow = powersOf10[logFloor];
        switch (m405getjavamathRoundingModeSwitchesValues()[mode.ordinal()]) {
            case 1:
            case 8:
                return lessThanBranchFree(floorPow, x) + logFloor;
            case 2:
            case 3:
                break;
            case 4:
            case 5:
            case 6:
                return lessThanBranchFree(halfPowersOf10[logFloor], x) + logFloor;
            case 7:
                MathPreconditions.checkRoundingUnnecessary(x == floorPow);
                break;
            default:
                throw new AssertionError();
        }
        return logFloor;
    }

    @GwtIncompatible("TODO")
    static int log10Floor(long x) {
        byte y = maxLog10ForLeadingZeros[Long.numberOfLeadingZeros(x)];
        return y - lessThanBranchFree(x, powersOf10[y]);
    }

    @GwtIncompatible("TODO")
    public static long pow(long b, int k) {
        long j;
        int i = 1;
        MathPreconditions.checkNonNegative("exponent", k);
        if (-2 > b || b > 2) {
            long accum = 1;
            while (true) {
                switch (k) {
                    case 0:
                        return accum;
                    case 1:
                        return accum * b;
                    default:
                        if ((k & 1) == 0) {
                            j = 1;
                        } else {
                            j = b;
                        }
                        accum *= j;
                        b *= b;
                        k >>= 1;
                }
            }
        } else {
            switch ((int) b) {
                case -2:
                    if (k < 64) {
                        return (k & 1) == 0 ? 1 << k : -(1 << k);
                    }
                    return 0;
                case -1:
                    if ((k & 1) != 0) {
                        i = -1;
                    }
                    return (long) i;
                case 0:
                    if (k != 0) {
                        i = 0;
                    }
                    return (long) i;
                case 1:
                    return 1;
                case 2:
                    if (k < 64) {
                        return 1 << k;
                    }
                    return 0;
                default:
                    throw new AssertionError();
            }
        }
    }

    @GwtIncompatible("TODO")
    public static long sqrt(long x, RoundingMode mode) {
        MathPreconditions.checkNonNegative("x", x);
        if (fitsInInt(x)) {
            return (long) IntMath.sqrt((int) x, mode);
        }
        long guess = (long) Math.sqrt((double) x);
        long guessSquared = guess * guess;
        switch (m405getjavamathRoundingModeSwitchesValues()[mode.ordinal()]) {
            case 1:
            case 8:
                if (x > guessSquared) {
                    return 1 + guess;
                }
                return guess;
            case 2:
            case 3:
                if (x < guessSquared) {
                    return guess - 1;
                }
                return guess;
            case 4:
            case 5:
            case 6:
                long sqrtFloor = guess - ((long) (x < guessSquared ? 1 : 0));
                return ((long) lessThanBranchFree((sqrtFloor * sqrtFloor) + sqrtFloor, x)) + sqrtFloor;
            case 7:
                MathPreconditions.checkRoundingUnnecessary(guessSquared == x);
                return guess;
            default:
                throw new AssertionError();
        }
    }

    @GwtIncompatible("TODO")
    public static long divide(long p, long q, RoundingMode mode) {
        boolean increment;
        Preconditions.checkNotNull(mode);
        long div = p / q;
        long rem = p - (q * div);
        if (rem == 0) {
            return div;
        }
        int signum = ((int) ((p ^ q) >> 63)) | 1;
        switch (m405getjavamathRoundingModeSwitchesValues()[mode.ordinal()]) {
            case 1:
                if (signum <= 0) {
                    increment = false;
                    break;
                } else {
                    increment = true;
                    break;
                }
            case 2:
                break;
            case 3:
                if (signum >= 0) {
                    increment = false;
                    break;
                } else {
                    increment = true;
                    break;
                }
            case 4:
            case 5:
            case 6:
                long absRem = Math.abs(rem);
                long cmpRemToHalfDivisor = absRem - (Math.abs(q) - absRem);
                if (cmpRemToHalfDivisor != 0) {
                    if (cmpRemToHalfDivisor <= 0) {
                        increment = false;
                        break;
                    } else {
                        increment = true;
                        break;
                    }
                } else {
                    increment = (mode == RoundingMode.HALF_UP) | (((1 & div) != 0) & (mode == RoundingMode.HALF_EVEN));
                    break;
                }
            case 7:
                MathPreconditions.checkRoundingUnnecessary(rem == 0);
                break;
            case 8:
                increment = true;
                break;
            default:
                throw new AssertionError();
        }
        increment = false;
        return increment ? div + ((long) signum) : div;
    }

    @GwtIncompatible("TODO")
    public static int mod(long x, int m) {
        return (int) mod(x, (long) m);
    }

    @GwtIncompatible("TODO")
    public static long mod(long x, long m) {
        if (m <= 0) {
            throw new ArithmeticException("Modulus must be positive");
        }
        long result = x % m;
        return result >= 0 ? result : result + m;
    }

    public static long gcd(long a, long b) {
        MathPreconditions.checkNonNegative("a", a);
        MathPreconditions.checkNonNegative("b", b);
        if (a == 0) {
            return b;
        }
        if (b == 0) {
            return a;
        }
        int aTwos = Long.numberOfTrailingZeros(a);
        long a2 = a >> aTwos;
        int bTwos = Long.numberOfTrailingZeros(b);
        long b2 = b >> bTwos;
        while (a2 != b2) {
            long delta = a2 - b2;
            long minDeltaOrZero = delta & (delta >> 63);
            long a3 = (delta - minDeltaOrZero) - minDeltaOrZero;
            b2 += minDeltaOrZero;
            a2 = a3 >> Long.numberOfTrailingZeros(a3);
        }
        return a2 << Math.min(aTwos, bTwos);
    }

    @GwtIncompatible("TODO")
    public static long checkedAdd(long a, long b) {
        boolean z;
        boolean z2 = true;
        long result = a + b;
        if ((a ^ b) < 0) {
            z = true;
        } else {
            z = false;
        }
        if ((a ^ result) < 0) {
            z2 = false;
        }
        MathPreconditions.checkNoOverflow(z2 | z);
        return result;
    }

    @GwtIncompatible("TODO")
    public static long checkedSubtract(long a, long b) {
        boolean z;
        boolean z2 = true;
        long result = a - b;
        if ((a ^ b) >= 0) {
            z = true;
        } else {
            z = false;
        }
        if ((a ^ result) < 0) {
            z2 = false;
        }
        MathPreconditions.checkNoOverflow(z2 | z);
        return result;
    }

    @GwtIncompatible("TODO")
    public static long checkedMultiply(long a, long b) {
        boolean z;
        boolean z2;
        boolean z3 = true;
        int leadingZeros = Long.numberOfLeadingZeros(a) + Long.numberOfLeadingZeros(~a) + Long.numberOfLeadingZeros(b) + Long.numberOfLeadingZeros(~b);
        if (leadingZeros > 65) {
            return a * b;
        }
        if (leadingZeros >= 64) {
            z = true;
        } else {
            z = false;
        }
        MathPreconditions.checkNoOverflow(z);
        if (a >= 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        MathPreconditions.checkNoOverflow((b != Long.MIN_VALUE) | z2);
        long result = a * b;
        if (!(a == 0 || result / a == b)) {
            z3 = false;
        }
        MathPreconditions.checkNoOverflow(z3);
        return result;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v9, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v12, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v15, resolved type: int} */
    /* JADX WARNING: type inference failed for: r2v0 */
    /* JADX WARNING: type inference failed for: r2v2 */
    /* JADX WARNING: type inference failed for: r2v8 */
    /* JADX WARNING: type inference failed for: r2v11 */
    /* JADX WARNING: type inference failed for: r2v14 */
    /* JADX WARNING: Multi-variable type inference failed */
    @com.google.common.annotations.GwtIncompatible("TODO")
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long checkedPow(long r10, int r12) {
        /*
            r8 = 1
            r2 = 1
            r3 = 0
            java.lang.String r4 = "exponent"
            com.google.common.math.MathPreconditions.checkNonNegative((java.lang.String) r4, (int) r12)
            r4 = -2
            int r4 = (r10 > r4 ? 1 : (r10 == r4 ? 0 : -1))
            if (r4 < 0) goto L_0x0025
            r5 = r2
        L_0x0011:
            r6 = 2
            int r4 = (r10 > r6 ? 1 : (r10 == r6 ? 0 : -1))
            if (r4 > 0) goto L_0x0027
            r4 = r2
        L_0x0018:
            r4 = r4 & r5
            if (r4 == 0) goto L_0x0058
            int r4 = (int) r10
            switch(r4) {
                case -2: goto L_0x0044;
                case -1: goto L_0x0030;
                case 0: goto L_0x0029;
                case 1: goto L_0x002f;
                case 2: goto L_0x0038;
                default: goto L_0x001f;
            }
        L_0x001f:
            java.lang.AssertionError r2 = new java.lang.AssertionError
            r2.<init>()
            throw r2
        L_0x0025:
            r5 = r3
            goto L_0x0011
        L_0x0027:
            r4 = r3
            goto L_0x0018
        L_0x0029:
            if (r12 != 0) goto L_0x002d
        L_0x002b:
            long r2 = (long) r2
            return r2
        L_0x002d:
            r2 = r3
            goto L_0x002b
        L_0x002f:
            return r8
        L_0x0030:
            r3 = r12 & 1
            if (r3 != 0) goto L_0x0036
        L_0x0034:
            long r2 = (long) r2
            return r2
        L_0x0036:
            r2 = -1
            goto L_0x0034
        L_0x0038:
            r4 = 63
            if (r12 >= r4) goto L_0x0042
        L_0x003c:
            com.google.common.math.MathPreconditions.checkNoOverflow(r2)
            long r2 = r8 << r12
            return r2
        L_0x0042:
            r2 = r3
            goto L_0x003c
        L_0x0044:
            r4 = 64
            if (r12 >= r4) goto L_0x0052
        L_0x0048:
            com.google.common.math.MathPreconditions.checkNoOverflow(r2)
            r2 = r12 & 1
            if (r2 != 0) goto L_0x0054
            long r2 = r8 << r12
        L_0x0051:
            return r2
        L_0x0052:
            r2 = r3
            goto L_0x0048
        L_0x0054:
            r2 = -1
            long r2 = r2 << r12
            goto L_0x0051
        L_0x0058:
            r0 = 1
        L_0x005a:
            switch(r12) {
                case 0: goto L_0x0078;
                case 1: goto L_0x0079;
                default: goto L_0x005d;
            }
        L_0x005d:
            r4 = r12 & 1
            if (r4 == 0) goto L_0x0065
            long r0 = checkedMultiply(r0, r10)
        L_0x0065:
            int r12 = r12 >> 1
            if (r12 <= 0) goto L_0x005a
            r4 = 3037000499(0xb504f333, double:1.500477613E-314)
            int r4 = (r10 > r4 ? 1 : (r10 == r4 ? 0 : -1))
            if (r4 > 0) goto L_0x007e
            r4 = r2
        L_0x0073:
            com.google.common.math.MathPreconditions.checkNoOverflow(r4)
            long r10 = r10 * r10
            goto L_0x005a
        L_0x0078:
            return r0
        L_0x0079:
            long r2 = checkedMultiply(r0, r10)
            return r2
        L_0x007e:
            r4 = r3
            goto L_0x0073
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.common.math.LongMath.checkedPow(long, int):long");
    }

    @GwtIncompatible("TODO")
    public static long factorial(int n) {
        MathPreconditions.checkNonNegative("n", n);
        if (n < factorials.length) {
            return factorials[n];
        }
        return Long.MAX_VALUE;
    }

    public static long binomial(int n, int k) {
        MathPreconditions.checkNonNegative("n", n);
        MathPreconditions.checkNonNegative("k", k);
        Preconditions.checkArgument(k <= n, "k (%s) > n (%s)", Integer.valueOf(k), Integer.valueOf(n));
        if (k > (n >> 1)) {
            k = n - k;
        }
        switch (k) {
            case 0:
                return 1;
            case 1:
                return (long) n;
            default:
                if (n < factorials.length) {
                    return factorials[n] / (factorials[k] * factorials[n - k]);
                }
                if (k >= biggestBinomials.length || n > biggestBinomials[k]) {
                    return Long.MAX_VALUE;
                }
                if (k >= biggestSimpleBinomials.length || n > biggestSimpleBinomials[k]) {
                    int nBits = log2((long) n, RoundingMode.CEILING);
                    long result = 1;
                    long numerator = (long) n;
                    long denominator = 1;
                    int numeratorBits = nBits;
                    int i = 2;
                    int n2 = n - 1;
                    while (i <= k) {
                        if (numeratorBits + nBits < 63) {
                            numerator *= (long) n2;
                            denominator *= (long) i;
                            numeratorBits += nBits;
                        } else {
                            result = multiplyFraction(result, numerator, denominator);
                            numerator = (long) n2;
                            denominator = (long) i;
                            numeratorBits = nBits;
                        }
                        i++;
                        n2--;
                    }
                    return multiplyFraction(result, numerator, denominator);
                }
                long result2 = (long) n;
                int n3 = n - 1;
                for (int i2 = 2; i2 <= k; i2++) {
                    result2 = (result2 * ((long) n3)) / ((long) i2);
                    n3--;
                }
                return result2;
        }
    }

    static long multiplyFraction(long x, long numerator, long denominator) {
        if (x == 1) {
            return numerator / denominator;
        }
        long commonDivisor = gcd(x, denominator);
        return (numerator / (denominator / commonDivisor)) * (x / commonDivisor);
    }

    static boolean fitsInInt(long x) {
        return ((long) ((int) x)) == x;
    }

    public static long mean(long x, long y) {
        return (x & y) + ((x ^ y) >> 1);
    }

    private LongMath() {
    }
}
