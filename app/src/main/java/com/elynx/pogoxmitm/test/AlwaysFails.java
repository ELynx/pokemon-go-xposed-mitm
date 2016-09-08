package com.elynx.pogoxmitm.test;

import com.elynx.pogoxmitm.module.ModuleManager;
import com.github.aeonlucid.pogoprotos.networking.Requests;
import com.google.protobuf.ByteString;

import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;

/**
 * Module that always fails to initialize
 */
public class AlwaysFails implements ModuleManager {
    public String userspaceName() {
        return "Test module - fail";
    }

    public String userspaceBrief() {
        return "Module that always fails to initialize";
    }

    public String userspaceInfo() {
        return "See less at https://example.com";
    }

    public long moduleId() {
        CRC32 crc32 = new CRC32();
        //TODO verify that this works
        crc32.update(userspaceName().getBytes());

        return crc32.getValue();
    }

    public boolean init() {
        return false;
    }

    public Set<Requests.RequestType> requestTypes() {
        return new HashSet<>();
    }

    public Set<Requests.RequestType> responseTypes() {
        return new HashSet<>();
    }

    public ByteString processRequest(Requests.RequestType type, ByteString data, int exchangeId, boolean connectionOk) {
        return null;
    }

    public ByteString processResponse(Requests.RequestType type, ByteString data, int exchangeId, boolean connectionOk) {
        return null;
    }
}
