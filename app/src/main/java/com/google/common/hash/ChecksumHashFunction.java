package com.google.common.hash;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import java.io.Serializable;
import java.util.zip.Checksum;

final class ChecksumHashFunction extends AbstractStreamingHashFunction implements Serializable {
    private static final long serialVersionUID = 0;
    /* access modifiers changed from: private */
    public final int bits;
    private final Supplier<? extends Checksum> checksumSupplier;
    private final String toString;

    ChecksumHashFunction(Supplier<? extends Checksum> checksumSupplier2, int bits2, String toString2) {
        boolean z;
        this.checksumSupplier = (Supplier) Preconditions.checkNotNull(checksumSupplier2);
        if (bits2 == 32 || bits2 == 64) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z, "bits (%s) must be either 32 or 64", Integer.valueOf(bits2));
        this.bits = bits2;
        this.toString = (String) Preconditions.checkNotNull(toString2);
    }

    public int bits() {
        return this.bits;
    }

    public Hasher newHasher() {
        return new ChecksumHasher(this, (Checksum) this.checksumSupplier.get(), (ChecksumHasher) null);
    }

    public String toString() {
        return this.toString;
    }

    private final class ChecksumHasher extends AbstractByteHasher {
        private final Checksum checksum;

        /* synthetic */ ChecksumHasher(ChecksumHashFunction this$02, Checksum checksum2, ChecksumHasher checksumHasher) {
            this(checksum2);
        }

        private ChecksumHasher(Checksum checksum2) {
            this.checksum = (Checksum) Preconditions.checkNotNull(checksum2);
        }

        /* access modifiers changed from: protected */
        public void update(byte b) {
            this.checksum.update(b);
        }

        /* access modifiers changed from: protected */
        public void update(byte[] bytes, int off, int len) {
            this.checksum.update(bytes, off, len);
        }

        public HashCode hash() {
            long value = this.checksum.getValue();
            if (ChecksumHashFunction.this.bits == 32) {
                return HashCode.fromInt((int) value);
            }
            return HashCode.fromLong(value);
        }
    }
}
