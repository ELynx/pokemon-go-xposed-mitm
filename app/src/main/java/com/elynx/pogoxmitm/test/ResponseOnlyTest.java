package com.elynx.pogoxmitm.test;

import com.github.aeonlucid.pogoprotos.networking.Requests;
import com.google.protobuf.ByteString;

import java.util.HashSet;
import java.util.Set;

/**
 * Module that works only on server response
 */
public class ResponseOnlyTest extends AlwaysFails {
    public String userspaceName() {
        return "Test module - RO";
    }

    public String userspaceBrief() {
        return "Module that does nothing";
    }

    public String userspaceInfo() {
        return "Peace";
    }

    public boolean init() {
        return true;
    }

    public Set<Requests.RequestType> responseTypes() {
        Set<Requests.RequestType> types = new HashSet<>();
        types.add(Requests.RequestType.DEBUG_UPDATE_INVENTORY);

        return types;
    }

    public ByteString processResponse(Requests.RequestType type, ByteString data) {
        if (data.size() < 5) {
            //TODO valid?
            return data.concat(data);
        }

        return null;
    }
}
