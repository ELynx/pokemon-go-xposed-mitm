package com.elynx.pogoxmitm;

import android.os.Build;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Class that manages injection of code into target app
 */
public class Injector implements IXposedHookLoadPackage {
    protected static String[] Methods = {"GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS", "TRACE"};

    protected static ThreadLocal<RpcContext> rpcContext = new ThreadLocal<RpcContext>() {
        @Override
        protected RpcContext initialValue() {
            return new RpcContext();
        }
    };

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.nianticlabs.pokemongo"))
            return;

        // real Http class names are from
        // https://goshin.github.io/2016/07/14/Black-box-test-using-Xposed/
        String HttpURLConnectionImplName;
        int apiLevel = Build.VERSION.SDK_INT;

        if (apiLevel >= 23) {
            HttpURLConnectionImplName = "com.android.okhttp.internal.huc.HttpURLConnectionImpl";
        } else if (apiLevel >= 19) {
            HttpURLConnectionImplName = "com.android.okhttp.internal.http.HttpURLConnectionImpl";
        } else {
            HttpURLConnectionImplName = "libcore.net.http.HttpURLConnectionImpl";
        }

        XposedBridge.log("Injecting into PoGo");

        // methods below are roughly in order or being called
        // note that joinHeaders and readDataStream are called from doSyncRequest

        // method is executed in thread context
        findAndHookMethod("com.nianticlabs.nia.network.NiaNet", lpparam.classLoader,
                //               0 object id 1 id       2 url         3 method   4 headers     5 buffer          6 offset   7 size
                "doSyncRequest", long.class, int.class, String.class, int.class, String.class, ByteBuffer.class, int.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        RpcContext context = rpcContext.get();

                        // fill in the context, this is threaded so each thread have individual

                        context.threadId = Thread.currentThread().getId();
                        context.niaRequest = true;

                        context.objectId = (long) param.args[0];
                        context.requestId = (int) param.args[1];
                        context.url = (String) param.args[2];
                        context.method = Methods[(int) param.args[3]];
                        context.requestHeaders = (String) param.args[4];
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        // radical option
                        // rpcContext.remove();

                        rpcContext.set(new RpcContext());
                    }
                });

        // method is executed in thread context
        findAndHookMethod("com.nianticlabs.nia.network.NiaNet", lpparam.classLoader,
                "joinHeaders", HttpURLConnection.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        RpcContext context = rpcContext.get();

                        context.responseHeaders = (String) param.getResult();
                        context.responseCode = ((HttpURLConnection) param.args[0]).getResponseCode();
                    }
                });

        // method is executed in thread context
        findAndHookMethod("com.nianticlabs.nia.network.NiaNet", lpparam.classLoader,
                "readDataSteam", HttpURLConnection.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        RpcContext context = rpcContext.get();

                        context.niaResponse = true;
                    }
                });

        // method is executed in unknown context, make sure this is response for NiaNet
        findAndHookMethod(HttpURLConnectionImplName, lpparam.classLoader,
                "getOutputStream",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        RpcContext context = rpcContext.get();

                        if (!context.niaRequest)
                            return;

                        XposedBridge.log("[request] " + context.shortDump());

                        MitmOutputStream replacement = new MitmOutputStream((OutputStream) param.getResult());
                        param.setResult(replacement);
                    }
                });

        // method is executed in unknown context, make sure this is response for NiaNet
        findAndHookMethod(HttpURLConnectionImplName, lpparam.classLoader,
                "getInputStream",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        RpcContext context = rpcContext.get();

                        if (!context.niaResponse)
                            return;

                        XposedBridge.log("[response] " + context.shortDump());

                        MitmInputStream replacement = new MitmInputStream((InputStream) param.getResult());
                        param.setResult(replacement);
                    }
                });
    }
}
