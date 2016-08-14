package com.elynx.pogoxmitm;

import java.nio.ByteBuffer;

import de.robv.android.xposed.XposedBridge;

/**
 * Class that does actual manipulations on data
 * Should be made reentrant and synchronized, since it is called from threads
 */
public class DataHandler {
    /**
     * Processes single package going from client to server
     *
     * @param requestId ID of request to which data belongs
     * @param data      Data buffer to be processed
     * @return True if data was changed, false otherwise
     */
    public static boolean processOutboundPackage(int requestId, ByteBuffer data) {
        // data is created by allocate and had to have array
        String dataDump = new String(data.array());
        XposedBridge.log("[request] " + Integer.toString(requestId) + " " + dataDump);

        return false;
    }
}
