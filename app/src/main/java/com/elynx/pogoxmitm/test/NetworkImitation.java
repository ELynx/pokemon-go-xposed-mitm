package com.elynx.pogoxmitm.test;

import com.elynx.pogoxmitm.Log;
import com.elynx.pogoxmitm.MitmProvider;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class that imitates network connection and Mitm streams
 */
public class NetworkImitation {
    protected static final double NetworkP = 0.9;

    protected static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 3000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
    protected static Integer exchangeId = 0;

    protected static String results = new String();

    public static void clearResults() {
        synchronized (results) {
            results = "";
        }
    }

    public static void appendResults(String text) {
        synchronized (results) {
            results += "\n" + text;
        }
    }

    public static String getResults() {
        synchronized (results) {
            return new String(results);
        }
    }

    public static void pushData(final ByteBuffer data) {
        synchronized (exchangeId) {
            ++exchangeId;
            threadPoolExecutor.execute(new Runnable() {
                public void run() {
                    doPushData(exchangeId, data);
                }
            });
        }
    }

    public static void doPushData(int exchangeId, ByteBuffer data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.capacity());
        data.get(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.capacity());

        boolean outConnectionOk = Math.random() <= NetworkP;

        ByteBuffer outbound = MitmProvider.processOutboundPackage(byteBuffer.asReadOnlyBuffer(), exchangeId, outConnectionOk);

        // if Mitm returned null then "send" original data
        if (outbound == null) {
            outbound = byteBuffer;
        }

        //imitate server

        boolean once = true;
        for (int i = outbound.arrayOffset(); i < outbound.capacity(); ++i) {
            byte b = outbound.array()[i];

            if (once && b >= 0x61 && b <= 0x7A) {
                b -= 0x20;
                once = false;
            }

            if (b == 0x21 && i > 0) {
                byte bPrev = outbound.array()[i - 1];
                if (bPrev == 0x21 || bPrev == 0x31) {
                    b = 0x31;
                }
            }

            outbound.array()[i] = b;
        }

        long ping = Math.round(Math.random() * 1000.0);

        try {
            Thread.sleep(ping);
        } catch (InterruptedException e) {
            Log.e(e.getMessage());
        }

        // end of server imitation

        // stop if nothing was "sent"
        if (!outConnectionOk) {
            appendResults("Connection lost");
            return;
        }

        boolean inConnectionOk = Math.random() <= NetworkP;

        ByteBuffer inbound = MitmProvider.processInboundPackage(outbound.asReadOnlyBuffer(), exchangeId, inConnectionOk);

        // if Mitm returned null then "return" original data
        if (inbound == null) {
            inbound = outbound;
        }

        inbound.rewind();
        String result = new String(inbound.array(), inbound.arrayOffset(), inbound.remaining());

        appendResults(result);
    }
}
