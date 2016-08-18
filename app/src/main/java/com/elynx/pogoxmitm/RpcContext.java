package com.elynx.pogoxmitm;

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

    public String shortDump() {
        return Integer.toString(requestId) + " " + method + " " + url;
    }
}
