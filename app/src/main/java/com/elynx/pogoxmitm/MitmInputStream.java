package com.elynx.pogoxmitm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Implements input stream that asks MITM provider before giving data away
 * <p/>
 * Based on answer on StackOverflow
 * https://stackoverflow.com/questions/4332264/wrapping-a-bytebuffer-with-an-inputstream/6603018#6603018
 */
public class MitmInputStream extends InputStream {
    ByteBuffer buffer;
    boolean mitmDone = false;

    public MitmInputStream(InputStream target) {
        // read all data from target into buffer
        // from http://www.gregbugaj.com/?p=283

        int AverageRequestSize = 4096; // average response is around 3500 bytes

        ByteArrayOutputStream os = new ByteArrayOutputStream(AverageRequestSize);

        byte[] bytes = new byte[AverageRequestSize];
        int bytesRead;

        try {
            while ((bytesRead = target.read(bytes, 0, bytes.length)) != -1) {
                os.write(bytes, 0, bytesRead);
            }
            os.flush();

            target.close();
        } catch (IOException e) {
            os.reset();
        }

        buffer = ByteBuffer.wrap(os.toByteArray());
    }

    @Override
    public int read() throws IOException {
        doMitmOnStoredData();

        if (!buffer.hasRemaining()) {
            return -1;
        }

        return buffer.get() & 0xFF;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        doMitmOnStoredData();

        if (!buffer.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buffer.remaining());
        buffer.get(bytes, off, len);
        return len;
    }

    @Override
    public int available() throws IOException {
        doMitmOnStoredData();

        if (!buffer.hasRemaining()) {
            return 0;
        }

        return buffer.remaining();
    }

    protected void doMitmOnStoredData() {
        if (mitmDone)
            return;

        // null if original is fine, otherwise contains new data
        ByteBuffer fromMitm = MitmProvider.processInboundPackage(buffer.asReadOnlyBuffer());

        if (fromMitm != null) {
            buffer = fromMitm;
        }

        mitmDone = true;
    }
}
