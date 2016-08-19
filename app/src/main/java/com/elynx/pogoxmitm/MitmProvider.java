package com.elynx.pogoxmitm;

import com.github.aeonlucid.pogoprotos.networking.Envelopes.RequestEnvelope;
import com.github.aeonlucid.pogoprotos.networking.Envelopes.ResponseEnvelope;
import com.github.aeonlucid.pogoprotos.networking.Requests;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;

/**
 * Class that does actual manipulations on data
 * Should be made reentrant and synchronized, since it is called from threads
 */
public class MitmProvider {
    protected static ThreadLocal<List<Requests.RequestType>> requestTypes = new ThreadLocal<List<Requests.RequestType>>() {
        @Override
        protected List<Requests.RequestType> initialValue() {
            return new ArrayList<>();
        }
    };

    /**
     * Processes single package going from client to server
     * roData is created by allocate and had to have array
     *
     * @param roData Read-only buffer to be processed
     * @return ByteBuffer with new content if data was changed, null otherwise
     */
    public static ByteBuffer processOutboundPackage(ByteBuffer roData) {
        roData.rewind();

        if (BuildConfig.DEBUG) {
            XposedBridge.log("Processing outbound package of size " + Integer.toString(roData.remaining()));
        }

        List<Requests.RequestType> types = requestTypes.get();
        types.clear();

        try {
            byte[] buffer = new byte[roData.remaining()];
            roData.get(buffer);

            RequestEnvelope request = RequestEnvelope.parseFrom(buffer);

            for (Requests.Request singleRequest : request.getRequestsList()) {
                types.add(singleRequest.getRequestType());
            }
        } catch (InvalidProtocolBufferException e) {
            XposedBridge.log(e);
        } catch (Throwable e) {
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
        roData.rewind();

        if (BuildConfig.DEBUG) {
            XposedBridge.log("Processing inbound package of size " + Integer.toString(roData.remaining()));
        }

        List<Requests.RequestType> types = requestTypes.get();

        if (!types.contains(IvHack.monitoredType()) && !types.contains(FortHack.monitoredType()))
            return null;

        boolean wasModified = false;

        try {
            byte[] buffer = new byte[roData.remaining()];
            roData.get(buffer);

            ResponseEnvelope.Builder response = ResponseEnvelope.parseFrom(buffer).toBuilder();

            // TODO why this is happening? some requests don't end in returns?
            if (response.getReturnsCount() != types.size()) {

                if (BuildConfig.DEBUG) {
                    String infoDump = "[PoGo-MITM ERROR] Request for [" + Integer.toString(types.size()) +
                            "] items but response is [" + Integer.toString(response.getReturnsCount()) + "] items\n";

                    infoDump += "Requested";

                    for (Requests.RequestType type : types) {
                        infoDump += " " + type.toString();
                    }

                    infoDump += "\nResponded\n" + response.toString();

                    XposedBridge.log(infoDump);
                }

                return null;
            }

            for (int returnNo = 0; returnNo < types.size(); ++returnNo) {
                ByteString hacked = null;

                if (types.get(returnNo) == IvHack.monitoredType()) {
                    hacked = IvHack.hack(response.getReturns(returnNo));
                }

                if (types.get(returnNo) == FortHack.monitoredType()) {
                    hacked = FortHack.hack(response.getReturns(returnNo));
                }

                if (hacked != null) {
                    response.setReturns(returnNo, hacked);
                    wasModified = true;
                }
            }

            if (wasModified) {
                return ByteBuffer.wrap(response.build().toByteArray());
            }
        } catch (InvalidProtocolBufferException e) {
            XposedBridge.log(e);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        return null;
    }
}
