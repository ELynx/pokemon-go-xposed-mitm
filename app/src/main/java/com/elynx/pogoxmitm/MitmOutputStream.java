package com.elynx.pogoxmitm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Implements output stream that asks MITM provider before writing
 */
public class MitmOutputStream extends ByteArrayOutputStream {
    OutputStream target;

    public MitmOutputStream(OutputStream target) {
        super(2048); // average request is around 1500 bytes
        this.target = target;
    }

    @Override
    public void close() throws IOException {
        doMitmOnStoredData();
        sendStoredData();
        target.close();

        super.close();
    }

    @Override
    public void flush() throws IOException {
        doMitmOnStoredData();
        sendStoredData();
        target.flush();
    }

    protected void doMitmOnStoredData() {
        // wrapper that will go to mitm class
        ByteBuffer wrapped = ByteBuffer.wrap(buf, 0, count).asReadOnlyBuffer();

        // null if original is fine, otherwise contains new data
        ByteBuffer fromMitm = MitmProvider.processOutboundPackage(wrapped);

        if (fromMitm != null) {
            reset();
            fromMitm.rewind();
            write(fromMitm.array(), fromMitm.arrayOffset(), fromMitm.remaining());
        }
    }

    protected void sendStoredData() throws IOException {
        writeTo(target);
        reset();
    }
}
