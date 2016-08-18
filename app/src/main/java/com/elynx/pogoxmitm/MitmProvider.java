package com.elynx.pogoxmitm;

import com.github.aeonlucid.pogoprotos.networking.Envelopes.RequestEnvelope;
import com.github.aeonlucid.pogoprotos.networking.Envelopes.ResponseEnvelope;
import com.github.aeonlucid.pogoprotos.networking.Requests;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;

import de.robv.android.xposed.XposedBridge;

/**
 * Class that does actual manipulations on data
 * Should be made reentrant and synchronized, since it is called from threads
 */
public class MitmProvider {
    /**
     * Processes single package going from client to server
     * roData is created by allocate and had to have array
     *
     * @param roData Read-only buffer to be processed
     * @return ByteBuffer with new content if data was changed, null otherwise
     */
    public static ByteBuffer processOutboundPackage(ByteBuffer roData) {
        RpcContext context = Injector.rpcContext.get();
        context.serverRequestTypes.clear();

        try {
            byte[] buffer = roData.array().clone();
            RequestEnvelope request = RequestEnvelope.parseFrom(buffer);

            for (Requests.Request singleRequest : request.getRequestsList()) {
                context.serverRequestTypes.add(singleRequest.getRequestType());
            }
        } catch (InvalidProtocolBufferException e) {
            XposedBridge.log(e);
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
    public static ByteBuffer processInboundPackage(ByteBuffer roData) {
        RpcContext context = Injector.rpcContext.get();

        if (!context.serverRequestTypes.contains(Requests.RequestType.GET_INVENTORY))
            return null;

        boolean wasModified = false;

        try {
            byte[] buffer = roData.array().clone();
            ResponseEnvelope.Builder response = ResponseEnvelope.parseFrom(buffer).toBuilder();

            // TODO why this is happening? some requests don't end in returns?
            if (response.getReturnsCount() != context.serverRequestTypes.size()) {

                if (BuildConfig.DEBUG) {
                    String infoDump = "[PoGo-MITM ERROR] Request for [" + Integer.toString(context.serverRequestTypes.size()) +
                            "] items but response is [" + Integer.toString(response.getReturnsCount()) + "] items\n";

                    infoDump += "Requested";

                    for (Requests.RequestType type : context.serverRequestTypes) {
                        infoDump += " " + type.toString();
                    }

                    infoDump += "\nResponded\n" + response.toString();

                    XposedBridge.log(infoDump);
                }

                return null;
            }

            for (int returnNo = 0; returnNo < context.serverRequestTypes.size(); ++returnNo) {
                if (context.serverRequestTypes.get(returnNo) == Requests.RequestType.GET_INVENTORY) {
                    ByteString hacked = IvHack.hack(response.getReturns(returnNo));

                    if (hacked != null) {
                        response.setReturns(returnNo, hacked);
                        wasModified = true;
                    }
                }
            }

            if (wasModified) {
                return ByteBuffer.wrap(response.build().toByteArray());
            }
        } catch (InvalidProtocolBufferException e) {
            XposedBridge.log(e);
        }

        return null;
    }
}
