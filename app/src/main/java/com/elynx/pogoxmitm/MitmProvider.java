package com.elynx.pogoxmitm;

import com.elynx.pogoxmitm.module.ModuleSupervisor;
import com.github.aeonlucid.pogoprotos.networking.Envelopes;

import java.nio.ByteBuffer;

/**
 * Class that does actual manipulations on data
 * Should be made reentrant and synchronized, since it is called from threads
 */
public class MitmProvider {
    //TODO remove this shit
    protected static final Boolean sync = false;

    /**
     * Private constructor because Singleton
     */
    private MitmProvider() {
    }

    /**
     * Holder class as described here http://programador.ru/singleton/
     * And invented by Bill Pugh / https://www.cs.umd.edu/~pugh/
     */
    private static class Holder {
        private static final MitmProvider INSTANCE = new MitmProvider();
    }

    /**
     * Get instance of MitmProvider
     *
     * @return Singleton MitmProvider
     */
    public static MitmProvider getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Processes single package going from client to server
     * roData is created by allocate and had to have array
     *
     * @param roData Read-only buffer to be processed
     * @return ByteBuffer with new content if data was changed, null otherwise
     */
    public ByteBuffer processOutboundPackage(ByteBuffer roData, int exchangeId, boolean connectionOk) {
        roData.rewind();

        if (BuildConfig.DEBUG) {
            Log.d("Processing outbound package of size " + Integer.toString(roData.remaining()));
        }

        try {
            byte[] buffer = new byte[roData.remaining()];
            roData.get(buffer);

            Envelopes.RequestEnvelope request = Envelopes.RequestEnvelope.parseFrom(buffer);

            synchronized (sync) {
                ModuleSupervisor modules = ModuleSupervisor.getInstance();
                // use all of this
                // buffer;
                // exchangeId;
                // connectionOk;
            }
        } catch (Throwable e) {
            Log.e(e);
        }

        return null;
    }

    /**
     * Processes single package going from server to client
     * roData is created by allocate and had to have array
     *
     * @param roData Read-only buffer to be processed
     * @return ByteBuffer with new content if data was changed, null otherwise
     */
    public ByteBuffer processInboundPackage(ByteBuffer roData, int exchangeId, boolean connectionOk) {
        roData.rewind();

        if (BuildConfig.DEBUG) {
            Log.d("Processing inbound package of size " + Integer.toString(roData.remaining()));
        }

        try {
            //byte[] buffer = new byte[roData.remaining()];
            //roData.get(buffer);

            synchronized (sync) {
                // use all of this
                // buffer
                // exchangeId
                // connectionOk
            }
        } catch (Throwable e) {
            Log.e(e);
        }

        return null;
    }
}
