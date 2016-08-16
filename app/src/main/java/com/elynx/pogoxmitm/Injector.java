package com.elynx.pogoxmitm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    public static boolean doOutbound = false;
    public static boolean doInbound = true;

    private static String[] Methods = {"GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS", "TRACE"};

    private static ThreadLocal<RpcContext> rpcContext = new ThreadLocal<RpcContext>() {
        @Override
        protected RpcContext initialValue() {
            return new RpcContext();
        }
    };

    // from http://www.gregbugaj.com/?p=283
    private ByteBuffer bufferFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048]; // TODO determine average response size
        int read;
        while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
            os.write(buffer, 0, read);
        }
        os.flush();

        buffer = os.toByteArray();
        return ByteBuffer.wrap(buffer);
    }

    private ByteBuffer copyBuffer(ByteBuffer original) throws IOException {
        original.rewind();

        ByteBuffer duplicate = ByteBuffer.allocate(original.capacity());
        duplicate.put(original);

        original.rewind();
        duplicate.rewind();

        return duplicate;
    }

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.nianticlabs.pokemongo"))
            return;

        XposedBridge.log("Injecting into PoGo");

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

                        XposedBridge.log("[request] " + context.shortDump());

                        // if modification of outbound data is not needed stop at bookkeeping
                        // for current project scope early return is a valid choice
                        if (!doOutbound)
                            return;

                        // read all data from original buffer that is available at the moment

                        ByteBuffer source = (ByteBuffer) param.args[5];
                        ByteBuffer unmodified = ByteBuffer.allocate(source.remaining());

                        if (source.hasArray()) {
                            // make size calculations
                            int offset = source.arrayOffset() + (int) param.args[6];
                            int length = Math.min((int) param.args[7], unmodified.capacity());

                            // copy bytes from original to buffer
                            unmodified.put(source.array(), offset, length);
                        } else {
                            // let original pass bytes as it can
                            source.get(unmodified.array(), unmodified.arrayOffset(), unmodified.capacity());
                        }

                        // buffer to be torn apart by parsers
                        ByteBuffer modified = copyBuffer(unmodified);

                        // process data
                        boolean wasModified = DataHandler.processOutboundPackage(context.requestId, modified);
                        ByteBuffer toServer = wasModified ? modified : unmodified;

                        // prepare data for original method
                        toServer.rewind();
                        param.args[5] = toServer;
                        param.args[6] = toServer.arrayOffset();
                        param.args[7] = toServer.remaining();
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
        findAndHookMethod(HttpURLConnection.class,
                "getInputStream",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        RpcContext context = rpcContext.get();

                        if (!context.niaResponse)
                            return;

                        XposedBridge.log("[response] " + context.shortDump());

                        // if modification of inbound data is not needed stop at bookkeeping
                        if (!doInbound)
                            return;

                        InputStream source = (InputStream) param.getResult();
                        ByteBuffer unmodified = bufferFromStream(source);
                        // TODO close stream?
                        ByteBuffer modified = copyBuffer(unmodified);

                        // process data
                        boolean wasModified = DataHandler.processInboundPackage(context.requestId, modified);
                        ByteBuffer toClient = wasModified ? modified : unmodified;

                        // nastily replace
                        ByteBufferBackedInputStream replacement = new ByteBufferBackedInputStream(toClient);
                        param.setResult(replacement);
                    }
                });
    }
}
