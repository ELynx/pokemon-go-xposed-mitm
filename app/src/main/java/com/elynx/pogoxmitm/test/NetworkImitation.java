package com.elynx.pogoxmitm.test;

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
        boolean outConnectionOk = Math.random() <= NetworkP;

        // OK to use original buffer IMO, since it is used as read-only
        ByteBuffer outbound = MitmProvider.processOutboundPackage(data.asReadOnlyBuffer(), exchangeId, outConnectionOk);

        // if Mitm returned null then "send" original data
        if (outbound == null) {
            outbound = ByteBuffer.allocate(data.capacity());

            data.rewind();
            outbound.put(data.array(), data.arrayOffset(), data.remaining());
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
                byte bPrev = outbound.array()[i-1];
                if (bPrev == 0x21 || bPrev == 0x31) {
                    b = 0x31;
                }
            }

            outbound.array()[i] = b;
        }

        long ping = Math.round(Math.random() * 1000.0);

        try {
            Thread.sleep(ping);
        } catch (InterruptedException e)
        {
            org.ruboto.Log.e(e.getMessage());
        }

        // end of server imitation

        // stop if nothing was "sent"
        if (!outConnectionOk)
            return;

        boolean inConnectionOk = Math.random() <= NetworkP;

        ByteBuffer inbound = MitmProvider.processInboundPackage(outbound.asReadOnlyBuffer(), exchangeId, inConnectionOk);

        // if Mitm returned null then "return" original data
        if (inbound == null) {
            inbound = ByteBuffer.allocate(outbound.capacity());

            outbound.rewind();
            inbound.put(outbound.array(), outbound.arrayOffset(), outbound.remaining());
        }

        inbound.rewind();
        String result = new String(inbound.array(), inbound.arrayOffset(), inbound.remaining());

        org.ruboto.Log.i("Finally " + result);
    }
}
