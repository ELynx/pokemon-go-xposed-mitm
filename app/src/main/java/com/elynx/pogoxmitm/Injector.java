package com.elynx.pogoxmitm;

import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Class that manages injection of code into target app
 */
public class Injector implements IXposedHookLoadPackage {
    private static Boolean busyOut = false;
    private static Boolean busyIn = false;

    private static final String[] Methods = {"GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS", "TRACE"};

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.nianticlabs.pokemongo"))
            return;

        XposedBridge.log("Injecting into PoGo");

        findAndHookMethod("com.nianticlabs.nia.network.NiaNet", lpparam.classLoader,
         // void request   0 object id 1 id       2 url->      3 method -> 4 headers ->  5 buffer ->      6 offset -> 7 size ->
                "request", long.class, int.class, String.class, int.class, String.class, ByteBuffer.class, int.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        synchronized (busyOut) {
                            busyOut = true;

                            //byteBuffer = (ByteBuffer)param.args[5];
                            //byte[] bytes = {};
                            //byteBuffer.get(bytes, param.args[6], param.args[7]);

                            String dump = "[request " + param.args[0].toString() +  "] " + Methods[(int) param.args[3]] + " url " + (String) param.args[2] + " hdr " + (String) param.args[4];
                            XposedBridge.log(dump);

                            busyOut = false;
                        }
                    }
                });

        findAndHookMethod("com.nianticlabs.nia.network.NiaNet", lpparam.classLoader,
         // void nativeCallback   0 object    id 1 http  2 headers     3 buffer          4 offset    5 size
                "nativeCallback", long.class, int.class, String.class, ByteBuffer.class, int.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        synchronized (busyIn) {
                            busyIn = true;

                            //byteBuffer = (ByteBuffer)param.args[3];
                            //byte[] bytes = {};
                            //byteBuffer.get(bytes, param.args[4], param.args[5]);

                            String dump = "[response " + param.args[0].toString() + "] hdr " + (String) param.args[2];
                            XposedBridge.log(dump);

                            busyIn = false;
                        }
                    }
                });
    }
}
