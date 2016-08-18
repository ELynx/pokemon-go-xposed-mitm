package com.elynx.pogoxmitm;

import com.github.aeonlucid.pogoprotos.networking.Requests;

import java.util.ArrayList;
import java.util.List;

/**
 * Context of Rpc call in NiaNet
 */
class RpcContext {
    long threadId = 0L;
    boolean niaRequest = false;
    boolean niaResponse = false;

    long objectId = 0L;
    int requestId = 0;
    String url;
    String method;
    String requestHeaders;
    String responseHeaders;
    int responseCode = 0;

    ArrayList<Requests.RequestType> serverRequestTypes = new ArrayList<>();

    public String shortDump() {
        return Integer.toString(requestId) + " " + method + " " + url;
    }
}
