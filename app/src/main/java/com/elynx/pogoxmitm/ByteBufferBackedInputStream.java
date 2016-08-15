package com.elynx.pogoxmitm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Gloriously copy-pasted from StackOverflow
 * https://stackoverflow.com/questions/4332264/wrapping-a-bytebuffer-with-an-inputstream/6603018#6603018
 * StackOverflow - cutting corners since forever
 */
public class ByteBufferBackedInputStream extends InputStream {

    ByteBuffer buf;

    public ByteBufferBackedInputStream(ByteBuffer buf) {
        this.buf = buf;
        this.buf.rewind();
    }

    @Override
    public int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        return buf.get() & 0xFF;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
    }

    @Override
    public int available() throws IOException {
        if (!buf.hasRemaining()) {
            return 0;
        }

        return buf.remaining();
    }
}
