package com.google.common.math;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Booleans;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Iterator;

@GwtCompatible(emulated = true)
public final class DoubleMath {

    /* renamed from: -java-math-RoundingModeSwitchesValues  reason: not valid java name */
    private static final /* synthetic */ int[] f29javamathRoundingModeSwitchesValues = null;
    private static final double LN_2 = Math.log(2.0d);
    @VisibleForTesting
    static final int MAX_FACTORIAL = 170;
    private static final double MAX_INT_AS_DOUBLE = 2.147483647E9d;
    private static final double MAX_LONG_AS_DOUBLE_PLUS_ONE = 9.223372036854776E18d;
    private static final double MIN_INT_AS_DOUBLE = -2.147483648E9d;
    private static final double MIN_LONG_AS_DOUBLE = -9.223372036854776E18d;
    @VisibleForTesting
    static final double[] everySixteenthFactorial = {1.0d, 2.0922789888E13d, 2.631308369336935E35d, 1.2413915592536073E61d, 1.2688693218588417E89d, 7.156945704626381E118d, 9.916779348709496E149d, 1.974506857221074E182d, 3.856204823625804E215d, 5.5502938327393044E249d, 4.7147236359920616E284d};

    /* renamed from: -getjava-math-RoundingModeSwitchesValues  reason: not valid java name */
    private static /* synthetic */ int[] m403getjavamathRoundingModeSwitchesValues() {
        if (f29javamathRoundingModeSwitchesValues != null) {
            return f29javamathRoundingModeSwitchesValues;
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
        f29javamathRoundingModeSwitchesValues = iArr;
        return iArr;
    }

    @GwtIncompatible("#isMathematicalInteger, com.google.common.math.DoubleUtils")
    static double roundIntermediate(double x, RoundingMode mode) {
        if (!DoubleUtils.isFinite(x)) {
            throw new ArithmeticException("input is infinite or NaN");
        }
        switch (m403getjavamathRoundingModeSwitchesValues()[mode.ordinal()]) {
            case 1:
                if (x <= LN_2 || isMathematicalInteger(x)) {
                    return x;
                }
                return x + 1.0d;
            case 2:
                return x;
            case 3:
                if (x >= LN_2 || isMathematicalInteger(x)) {
                    return x;
                }
                return x - 1.0d;
            case 4:
                double z = Math.rint(x);
                if (Math.abs(x - z) == 0.5d) {
                    return x;
                }
                return z;
            case 5:
                return Math.rint(x);
            case 6:
                double z2 = Math.rint(x);
                if (Math.abs(x - z2) == 0.5d) {
                    return Math.copySign(0.5d, x) + x;
                }
                return z2;
            case 7:
                MathPreconditions.checkRoundingUnnecessary(isMathematicalInteger(x));
                return x;
            case 8:
                if (isMathematicalInteger(x)) {
                    return x;
                }
                return Math.copySign(1.0d, x) + x;
            default:
                throw new AssertionError();
        }
    }

    @GwtIncompatible("#roundIntermediate")
    public static int roundToInt(double x, RoundingMode mode) {
        boolean z;
        boolean z2 = true;
        double z3 = roundIntermediate(x, mode);
        if (z3 > -2.147483649E9d) {
            z = true;
        } else {
            z = false;
        }
        if (z3 >= 2.147483648E9d) {
            z2 = false;
        }
        MathPreconditions.checkInRange(z2 & z);
        return (int) z3;
    }

    @GwtIncompatible("#roundIntermediate")
    public static long roundToLong(double x, RoundingMode mode) {
        boolean z;
        boolean z2 = true;
        double z3 = roundIntermediate(x, mode);
        if (MIN_LONG_AS_DOUBLE - z3 < 1.0d) {
            z = true;
        } else {
            z = false;
        }
        if (z3 >= MAX_LONG_AS_DOUBLE_PLUS_ONE) {
            z2 = false;
        }
        MathPreconditions.checkInRange(z2 & z);
        return (long) z3;
    }

    @GwtIncompatible("#roundIntermediate, java.lang.Math.getExponent, com.google.common.math.DoubleUtils")
    public static BigInteger roundToBigInteger(double x, RoundingMode mode) {
        boolean z;
        boolean z2 = true;
        double x2 = roundIntermediate(x, mode);
        if (MIN_LONG_AS_DOUBLE - x2 < 1.0d) {
            z = true;
        } else {
            z = false;
        }
        if (x2 >= MAX_LONG_AS_DOUBLE_PLUS_ONE) {
            z2 = false;
        }
        if (z2 && z) {
            return BigInteger.valueOf((long) x2);
        }
        BigInteger result = BigInteger.valueOf(DoubleUtils.getSignificand(x2)).shiftLeft(Math.getExponent(x2) - 52);
        return x2 < LN_2 ? result.negate() : result;
    }

    @GwtIncompatible("com.google.common.math.DoubleUtils")
    public static boolean isPowerOfTwo(double x) {
        if (x <= LN_2 || !DoubleUtils.isFinite(x)) {
            return false;
        }
        return LongMath.isPowerOfTwo(DoubleUtils.getSignificand(x));
    }

    public static double log2(double x) {
        return Math.log(x) / LN_2;
    }

    @GwtIncompatible("java.lang.Math.getExponent, com.google.common.math.DoubleUtils")
    public static int log2(double x, RoundingMode mode) {
        boolean z;
        boolean increment;
        boolean z2 = true;
        boolean z3 = false;
        if (x > LN_2) {
            z = DoubleUtils.isFinite(x);
        } else {
            z = false;
        }
        Preconditions.checkArgument(z, "x must be positive and finite");
        int exponent = Math.getExponent(x);
        if (!DoubleUtils.isNormal(x)) {
            return log2(4.503599627370496E15d * x, mode) - 52;
        }
        switch (m403getjavamathRoundingModeSwitchesValues()[mode.ordinal()]) {
            case 1:
                increment = !isPowerOfTwo(x);
                break;
            case 2:
                if (exponent < 0) {
                    z3 = true;
                }
                increment = z3 & (!isPowerOfTwo(x));
                break;
            case 3:
                break;
            case 4:
            case 5:
            case 6:
                double xScaled = DoubleUtils.scaleNormalize(x);
                if (xScaled * xScaled <= 2.0d) {
                    increment = false;
                    break;
                } else {
                    increment = true;
                    break;
                }
            case 7:
                MathPreconditions.checkRoundingUnnecessary(isPowerOfTwo(x));
                break;
            case 8:
                if (exponent < 0) {
                    z2 = false;
                }
                increment = z2 & (!isPowerOfTwo(x));
                break;
            default:
                throw new AssertionError();
        }
        increment = false;
        return increment ? exponent + 1 : exponent;
    }

    @GwtIncompatible("java.lang.Math.getExponent, com.google.common.math.DoubleUtils")
    public static boolean isMathematicalInteger(double x) {
        if (DoubleUtils.isFinite(x)) {
            return x == LN_2 || 52 - Long.numberOfTrailingZeros(DoubleUtils.getSignificand(x)) <= Math.getExponent(x);
        }
        return false;
    }

    public static double factorial(int n) {
        MathPreconditions.checkNonNegative("n", n);
        if (n > MAX_FACTORIAL) {
            return Double.POSITIVE_INFINITY;
        }
        double accum = 1.0d;
        for (int i = (n & -16) + 1; i <= n; i++) {
            accum *= (double) i;
        }
        return everySixteenthFactorial[n >> 4] * accum;
    }

    public static boolean fuzzyEquals(double a, double b, double tolerance) {
        MathPreconditions.checkNonNegative("tolerance", tolerance);
        if (Math.copySign(a - b, 1.0d) <= tolerance || a == b) {
            return true;
        }
        if (Double.isNaN(a)) {
            return Double.isNaN(b);
        }
        return false;
    }

    public static int fuzzyCompare(double a, double b, double tolerance) {
        if (fuzzyEquals(a, b, tolerance)) {
            return 0;
        }
        if (a < b) {
            return -1;
        }
        if (a > b) {
            return 1;
        }
        return Booleans.compare(Double.isNaN(a), Double.isNaN(b));
    }

    @GwtIncompatible("com.google.common.math.DoubleUtils")
    private static final class MeanAccumulator {
        private long count;
        private double mean;

        /* synthetic */ MeanAccumulator(MeanAccumulator meanAccumulator) {
            this();
        }

        private MeanAccumulator() {
            this.count = 0;
            this.mean = DoubleMath.LN_2;
        }

        /* access modifiers changed from: package-private */
        public void add(double value) {
            Preconditions.checkArgument(DoubleUtils.isFinite(value));
            this.count++;
            this.mean += (value - this.mean) / ((double) this.count);
        }

        /* access modifiers changed from: package-private */
        public double mean() {
            Preconditions.checkArgument(this.count > 0, "Cannot take mean of 0 values");
            return this.mean;
        }
    }

    @GwtIncompatible("MeanAccumulator")
    public static double mean(double... values) {
        MeanAccumulator accumulator = new MeanAccumulator((MeanAccumulator) null);
        for (double value : values) {
            accumulator.add(value);
        }
        return accumulator.mean();
    }

    @GwtIncompatible("MeanAccumulator")
    public static double mean(int... values) {
        MeanAccumulator accumulator = new MeanAccumulator((MeanAccumulator) null);
        for (int value : values) {
            accumulator.add((double) value);
        }
        return accumulator.mean();
    }

    @GwtIncompatible("MeanAccumulator")
    public static double mean(long... values) {
        MeanAccumulator accumulator = new MeanAccumulator((MeanAccumulator) null);
        for (long value : values) {
            accumulator.add((double) value);
        }
        return accumulator.mean();
    }

    @GwtIncompatible("MeanAccumulator")
    public static double mean(Iterable<? extends Number> values) {
        MeanAccumulator accumulator = new MeanAccumulator((MeanAccumulator) null);
        for (Number value : values) {
            accumulator.add(value.doubleValue());
        }
        return accumulator.mean();
    }

    @GwtIncompatible("MeanAccumulator")
    public static double mean(Iterator<? extends Number> values) {
        MeanAccumulator accumulator = new MeanAccumulator((MeanAccumulator) null);
        while (values.hasNext()) {
            accumulator.add(((Number) values.next()).doubleValue());
        }
        return accumulator.mean();
    }

    private DoubleMath() {
    }
}
