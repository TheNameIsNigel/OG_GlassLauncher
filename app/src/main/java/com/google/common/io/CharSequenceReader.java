package com.google.common.io;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

final class CharSequenceReader extends Reader {
    private int mark;
    private int pos;
    private CharSequence seq;

    public CharSequenceReader(CharSequence seq2) {
        this.seq = (CharSequence) Preconditions.checkNotNull(seq2);
    }

    private void checkOpen() throws IOException {
        if (this.seq == null) {
            throw new IOException("reader closed");
        }
    }

    private boolean hasRemaining() {
        return remaining() > 0;
    }

    private int remaining() {
        return this.seq.length() - this.pos;
    }

    public synchronized int read(CharBuffer target) throws IOException {
        Preconditions.checkNotNull(target);
        checkOpen();
        if (!hasRemaining()) {
            return -1;
        }
        int charsToRead = Math.min(target.remaining(), remaining());
        for (int i = 0; i < charsToRead; i++) {
            CharSequence charSequence = this.seq;
            int i2 = this.pos;
            this.pos = i2 + 1;
            target.put(charSequence.charAt(i2));
        }
        return charsToRead;
    }

    public synchronized int read() throws IOException {
        char c;
        checkOpen();
        if (hasRemaining()) {
            CharSequence charSequence = this.seq;
            int i = this.pos;
            this.pos = i + 1;
            c = charSequence.charAt(i);
        } else {
            c = 65535;
        }
        return c;
    }

    public synchronized int read(char[] cbuf, int off, int len) throws IOException {
        Preconditions.checkPositionIndexes(off, off + len, cbuf.length);
        checkOpen();
        if (!hasRemaining()) {
            return -1;
        }
        int charsToRead = Math.min(len, remaining());
        for (int i = 0; i < charsToRead; i++) {
            CharSequence charSequence = this.seq;
            int i2 = this.pos;
            this.pos = i2 + 1;
            cbuf[off + i] = charSequence.charAt(i2);
        }
        return charsToRead;
    }

    public synchronized long skip(long n) throws IOException {
        long j;
        boolean z = true;
        synchronized (this) {
            if (n < 0) {
                z = false;
            }
            Preconditions.checkArgument(z, "n (%s) may not be negative", Long.valueOf(n));
            checkOpen();
            int charsToSkip = (int) Math.min((long) remaining(), n);
            this.pos += charsToSkip;
            j = (long) charsToSkip;
        }
        return j;
    }

    public synchronized boolean ready() throws IOException {
        checkOpen();
        return true;
    }

    public boolean markSupported() {
        return true;
    }

    public synchronized void mark(int readAheadLimit) throws IOException {
        boolean z = true;
        synchronized (this) {
            if (readAheadLimit < 0) {
                z = false;
            }
            Preconditions.checkArgument(z, "readAheadLimit (%s) may not be negative", Integer.valueOf(readAheadLimit));
            checkOpen();
            this.mark = this.pos;
        }
    }

    public synchronized void reset() throws IOException {
        checkOpen();
        this.pos = this.mark;
    }

    public synchronized void close() throws IOException {
        this.seq = null;
    }
}
