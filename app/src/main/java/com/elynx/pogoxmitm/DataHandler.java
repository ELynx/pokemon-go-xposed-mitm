package com.elynx.pogoxmitm;

import com.github.aeonlucid.pogoprotos.networking.Envelopes;
import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;

import de.robv.android.xposed.XposedBridge;

/**
 * Class that does actual manipulations on data
 * Should be made reentrant and synchronized, since it is called from threads
 */
public class DataHandler {
    public static boolean doIvHack = false;
    public static boolean doSpeedHack = false;

    /**
     * Processes single package going from client to server
     * data is created by allocate and had to have array
     * data is given here completely for destruction, it will be thrown away if false is returned
     * parseFrom seems to consume content of data, so unless it is reassembled DO return false,
     * or nothing will reach actual net code
     *
     * @param requestId ID of request to which data belongs
     * @param data      Data buffer to be processed
     * @return True if data was changed, false otherwise
     */
    public static boolean processOutboundPackage(int requestId, ByteBuffer data) throws Throwable {
        String dataDump = "Not parsed...";

        try {
            // data is no longer usable after this call
            Envelopes.RequestEnvelope req = Envelopes.RequestEnvelope.parseFrom(data.array());
            dataDump = req.toString();
            // reassemble data here and return true
        } catch (InvalidProtocolBufferException e) {
            XposedBridge.log(e);
        }

        XposedBridge.log("[request] " + Integer.toString(requestId) + "\n" + dataDump);

        // false means "throw away data and send what was meant to be sent"
        return false;
    }

    /**
     * Processes single package going from server to client
     * data is created by allocate and had to have array
     * data is given here completely for destruction, it will be thrown away if false is returned
     * parseFrom seems to consume content of data, so unless it is reassembled DO return false,
     * or nothing will reach actual client code
     *
     * @param requestId ID of request to which data belongs
     * @param data      Data buffer to be processed
     * @return True if data was changed, false otherwise
     */
    public static boolean processInboundPackage(int requestId, ByteBuffer data) throws Throwable {
        String dataDump = "Not parsed...";

        try {
            // data is no longer usable after this call
            Envelopes.ResponseEnvelope req = Envelopes.ResponseEnvelope.parseFrom(data.array());
            dataDump = req.toString();
            // reassemble data here and return true
        } catch (InvalidProtocolBufferException e) {
            XposedBridge.log(e);
        }

        XposedBridge.log("[response] " + Integer.toString(requestId) + "\n" + dataDump);

        // false means "throw away data and return what was meant to be returned"
        return false;
    }
}
