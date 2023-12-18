package com.google.common.io;

import com.google.common.base.Preconditions;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public abstract class ByteSink {
    public abstract OutputStream openStream() throws IOException;

    protected ByteSink() {
    }

    public CharSink asCharSink(Charset charset) {
        return new AsCharSink(this, charset, (AsCharSink) null);
    }

    public OutputStream openBufferedStream() throws IOException {
        OutputStream out = openStream();
        if (out instanceof BufferedOutputStream) {
            return (BufferedOutputStream) out;
        }
        return new BufferedOutputStream(out);
    }

    public void write(byte[] bytes) throws IOException {
        Preconditions.checkNotNull(bytes);
        Closer closer = Closer.create();
        try {
            OutputStream out = (OutputStream) closer.register(openStream());
            out.write(bytes);
            out.flush();
            closer.close();
        } catch (Throwable th) {
            closer.close();
            throw th;
        }
    }

    public long writeFrom(InputStream input) throws IOException {
        Preconditions.checkNotNull(input);
        Closer closer = Closer.create();
        try {
            OutputStream out = (OutputStream) closer.register(openStream());
            long written = ByteStreams.copy(input, out);
            out.flush();
            closer.close();
            return written;
        } catch (Throwable th) {
            closer.close();
            throw th;
        }
    }

    private final class AsCharSink extends CharSink {
        private final Charset charset;

        /* synthetic */ AsCharSink(ByteSink this$02, Charset charset2, AsCharSink asCharSink) {
            this(charset2);
        }

        private AsCharSink(Charset charset2) {
            this.charset = (Charset) Preconditions.checkNotNull(charset2);
        }

        public Writer openStream() throws IOException {
            return new OutputStreamWriter(ByteSink.this.openStream(), this.charset);
        }

        public String toString() {
            return ByteSink.this.toString() + ".asCharSink(" + this.charset + ")";
        }
    }
}
