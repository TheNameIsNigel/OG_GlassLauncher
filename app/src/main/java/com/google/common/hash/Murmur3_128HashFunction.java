package com.google.common.hash;

import com.google.common.hash.AbstractStreamingHashFunction;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.annotation.Nullable;

final class Murmur3_128HashFunction extends AbstractStreamingHashFunction implements Serializable {
    private static final long serialVersionUID = 0;
    private final int seed;

    Murmur3_128HashFunction(int seed2) {
        this.seed = seed2;
    }

    public int bits() {
        return 128;
    }

    public Hasher newHasher() {
        return new Murmur3_128Hasher(this.seed);
    }

    public String toString() {
        return "Hashing.murmur3_128(" + this.seed + ")";
    }

    public boolean equals(@Nullable Object object) {
        if (!(object instanceof Murmur3_128HashFunction) || this.seed != ((Murmur3_128HashFunction) object).seed) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getClass().hashCode() ^ this.seed;
    }

    private static final class Murmur3_128Hasher extends AbstractStreamingHashFunction.AbstractStreamingHasher {
        private static final long C1 = -8663945395140668459L;
        private static final long C2 = 5545529020109919103L;
        private static final int CHUNK_SIZE = 16;
        private long h1;
        private long h2;
        private int length = 0;

        Murmur3_128Hasher(int seed) {
            super(16);
            this.h1 = (long) seed;
            this.h2 = (long) seed;
        }

        /* access modifiers changed from: protected */
        public void process(ByteBuffer bb) {
            bmix64(bb.getLong(), bb.getLong());
            this.length += 16;
        }

        private void bmix64(long k1, long k2) {
            this.h1 ^= mixK1(k1);
            this.h1 = Long.rotateLeft(this.h1, 27);
            this.h1 += this.h2;
            this.h1 = (this.h1 * 5) + 1390208809;
            this.h2 ^= mixK2(k2);
            this.h2 = Long.rotateLeft(this.h2, 31);
            this.h2 += this.h1;
            this.h2 = (this.h2 * 5) + 944331445;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x007a, code lost:
            r2 = r2 ^ ((long) com.google.common.primitives.UnsignedBytes.toInt(r14.get(8)));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0084, code lost:
            r0 = 0 ^ r14.getLong();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x008a, code lost:
            r13.h1 ^= mixK1(r0);
            r13.h2 ^= mixK2(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x009c, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x00ac, code lost:
            r0 = r0 ^ (((long) com.google.common.primitives.UnsignedBytes.toInt(r14.get(5))) << 40);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x00ba, code lost:
            r0 = r0 ^ (((long) com.google.common.primitives.UnsignedBytes.toInt(r14.get(4))) << 32);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x00c6, code lost:
            r0 = r0 ^ (((long) com.google.common.primitives.UnsignedBytes.toInt(r14.get(3))) << 24);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x00d2, code lost:
            r0 = r0 ^ (((long) com.google.common.primitives.UnsignedBytes.toInt(r14.get(2))) << 16);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x00de, code lost:
            r0 = r0 ^ (((long) com.google.common.primitives.UnsignedBytes.toInt(r14.get(1))) << 8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x00ea, code lost:
            r0 = r0 ^ ((long) com.google.common.primitives.UnsignedBytes.toInt(r14.get(0)));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:5:0x0037, code lost:
            r2 = r2 ^ (((long) com.google.common.primitives.UnsignedBytes.toInt(r14.get(13))) << 40);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:6:0x0046, code lost:
            r2 = r2 ^ (((long) com.google.common.primitives.UnsignedBytes.toInt(r14.get(12))) << 32);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:7:0x0053, code lost:
            r2 = r2 ^ (((long) com.google.common.primitives.UnsignedBytes.toInt(r14.get(11))) << 24);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0060, code lost:
            r2 = r2 ^ (((long) com.google.common.primitives.UnsignedBytes.toInt(r14.get(10))) << 16);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x006d, code lost:
            r2 = r2 ^ (((long) com.google.common.primitives.UnsignedBytes.toInt(r14.get(9))) << 8);
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void processRemaining(java.nio.ByteBuffer r14) {
            /*
                r13 = this;
                r12 = 32
                r11 = 24
                r10 = 16
                r8 = 0
                r7 = 8
                r0 = 0
                r2 = 0
                int r4 = r13.length
                int r5 = r14.remaining()
                int r4 = r4 + r5
                r13.length = r4
                int r4 = r14.remaining()
                switch(r4) {
                    case 1: goto L_0x00ea;
                    case 2: goto L_0x00de;
                    case 3: goto L_0x00d2;
                    case 4: goto L_0x00c6;
                    case 5: goto L_0x00ba;
                    case 6: goto L_0x00ac;
                    case 7: goto L_0x009d;
                    case 8: goto L_0x0084;
                    case 9: goto L_0x007a;
                    case 10: goto L_0x006d;
                    case 11: goto L_0x0060;
                    case 12: goto L_0x0053;
                    case 13: goto L_0x0046;
                    case 14: goto L_0x0037;
                    case 15: goto L_0x0027;
                    default: goto L_0x001e;
                }
            L_0x001e:
                java.lang.AssertionError r4 = new java.lang.AssertionError
                java.lang.String r5 = "Should never get here."
                r4.<init>(r5)
                throw r4
            L_0x0027:
                r4 = 14
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                r6 = 48
                long r4 = r4 << r6
                long r2 = r8 ^ r4
            L_0x0037:
                r4 = 13
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                r6 = 40
                long r4 = r4 << r6
                long r2 = r2 ^ r4
            L_0x0046:
                r4 = 12
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                long r4 = r4 << r12
                long r2 = r2 ^ r4
            L_0x0053:
                r4 = 11
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                long r4 = r4 << r11
                long r2 = r2 ^ r4
            L_0x0060:
                r4 = 10
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                long r4 = r4 << r10
                long r2 = r2 ^ r4
            L_0x006d:
                r4 = 9
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                long r4 = r4 << r7
                long r2 = r2 ^ r4
            L_0x007a:
                byte r4 = r14.get(r7)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                long r2 = r2 ^ r4
            L_0x0084:
                long r4 = r14.getLong()
                long r0 = r8 ^ r4
            L_0x008a:
                long r4 = r13.h1
                long r6 = mixK1(r0)
                long r4 = r4 ^ r6
                r13.h1 = r4
                long r4 = r13.h2
                long r6 = mixK2(r2)
                long r4 = r4 ^ r6
                r13.h2 = r4
                return
            L_0x009d:
                r4 = 6
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                r6 = 48
                long r4 = r4 << r6
                long r0 = r8 ^ r4
            L_0x00ac:
                r4 = 5
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                r6 = 40
                long r4 = r4 << r6
                long r0 = r0 ^ r4
            L_0x00ba:
                r4 = 4
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                long r4 = r4 << r12
                long r0 = r0 ^ r4
            L_0x00c6:
                r4 = 3
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                long r4 = r4 << r11
                long r0 = r0 ^ r4
            L_0x00d2:
                r4 = 2
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                long r4 = r4 << r10
                long r0 = r0 ^ r4
            L_0x00de:
                r4 = 1
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                long r4 = r4 << r7
                long r0 = r0 ^ r4
            L_0x00ea:
                r4 = 0
                byte r4 = r14.get(r4)
                int r4 = com.google.common.primitives.UnsignedBytes.toInt(r4)
                long r4 = (long) r4
                long r0 = r0 ^ r4
                goto L_0x008a
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.common.hash.Murmur3_128HashFunction.Murmur3_128Hasher.processRemaining(java.nio.ByteBuffer):void");
        }

        public HashCode makeHash() {
            this.h1 ^= (long) this.length;
            this.h2 ^= (long) this.length;
            this.h1 += this.h2;
            this.h2 += this.h1;
            this.h1 = fmix64(this.h1);
            this.h2 = fmix64(this.h2);
            this.h1 += this.h2;
            this.h2 += this.h1;
            return HashCode.fromBytesNoCopy(ByteBuffer.wrap(new byte[16]).order(ByteOrder.LITTLE_ENDIAN).putLong(this.h1).putLong(this.h2).array());
        }

        private static long fmix64(long k) {
            long k2 = (k ^ (k >>> 33)) * -49064778989728563L;
            long k3 = (k2 ^ (k2 >>> 33)) * -4265267296055464877L;
            return k3 ^ (k3 >>> 33);
        }

        private static long mixK1(long k1) {
            return Long.rotateLeft(k1 * C1, 31) * C2;
        }

        private static long mixK2(long k2) {
            return Long.rotateLeft(k2 * C2, 33) * C1;
        }
    }
}
