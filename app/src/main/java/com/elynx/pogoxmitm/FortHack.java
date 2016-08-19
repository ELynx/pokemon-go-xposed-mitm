package com.elynx.pogoxmitm;

import com.github.aeonlucid.pogoprotos.networking.Requests;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Class that shows some more info for fort
 */
public class FortHack {
    public static Requests.RequestType monitoredType() { return Requests.RequestType.FORT_DETAILS; }

    public static ByteString hack(ByteString response) throws InvalidProtocolBufferException {
        return null;
    }
}
