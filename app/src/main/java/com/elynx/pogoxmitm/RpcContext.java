package com.elynx.pogoxmitm;

import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;

/**
 * Context of Rpc call in NiaNet
 */
class RpcContext {
    private static final String apiPrefix = "https://pgorelease.nianticlabs.com/plfe";
    private static final String apiSuffix = "/rpc";
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

    public static RpcContext fromConnection(URLConnection urlConnection, boolean isRequest) {
        RpcContext rpcContext = new RpcContext();

        try {
            rpcContext.url = urlConnection.getURL().toExternalForm();
            boolean isNia = rpcContext.url != null &&
                    rpcContext.url.startsWith(apiPrefix) && rpcContext.url.endsWith(apiSuffix);
            rpcContext.niaRequest = isRequest && isNia;
            rpcContext.niaResponse = !isRequest && isNia;
            rpcContext.threadId = Thread.currentThread().getId();

            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
                rpcContext.method = httpConnection.getRequestMethod();
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        return rpcContext;
    }

    public String shortDump() {
        return Integer.toString(requestId) + " " + method + " " + url;
    }
}
