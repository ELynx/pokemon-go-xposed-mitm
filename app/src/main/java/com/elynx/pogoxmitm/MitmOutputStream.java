package com.elynx.pogoxmitm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Implements output stream that asks MITM provider before writing
 */
public class MitmOutputStream extends ByteArrayOutputStream {
    OutputStream target = null;
    int requestId = 0;

    static final int AverageRequestSize = 2048; // average request is around 1500 bytes

    public MitmOutputStream(OutputStream target, int requestId) {
        super(AverageRequestSize);
        this.target = target; // can become null if connection is lost
        this.requestId = requestId;
    }

    @Override
    public void close() throws IOException {
        doMitmOnStoredData();
        sendStoredData();

        if (target != null) {
            target.close();
        }

        super.close();
    }

    @Override
    public void flush() throws IOException {
        doMitmOnStoredData();
        sendStoredData();

        if (target != null) {
            target.flush();
        }
    }

    protected void doMitmOnStoredData() {
        // wrapper that will go to mitm class
        ByteBuffer wrapped = ByteBuffer.wrap(buf, 0, count).asReadOnlyBuffer();
        boolean connectionOk = target != null;

        // null if original is fine, otherwise contains new data
        ByteBuffer fromMitm = MitmProvider.processOutboundPackage(wrapped, requestId, connectionOk);

        if (fromMitm != null) {
            reset();
            fromMitm.rewind();
            write(fromMitm.array(), fromMitm.arrayOffset(), fromMitm.remaining());
        }
    }

    protected void sendStoredData() throws IOException {
        if (target != null) {
            writeTo(target);
        }

        reset();
    }
}
