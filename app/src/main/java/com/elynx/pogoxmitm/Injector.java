package com.elynx.pogoxmitm;

import java.nio.ByteBuffer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodHook;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Class what manages injection of code into target app
 */
public class Injector implements IXposedHookLoadPackage {
    private static final String[] Methods = {"GET", "HEAD", "POST", "PUT", "DELETE"};

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.nianticlabs.pokemongo"))
            return;

        XposedBridge.log("Injecting into PoGo");

        findAndHookMethod("com.nianticlabs.nia.network.NiaNet", lpparam.classLoader,
         // void request   0 unused    1 id       2 url->      3 method -> 4 headers ->  5 buffer ->      6 offset -> 7 size ->
                "request", long.class, int.class, String.class, int.class, String.class, ByteBuffer.class, int.class, int.class,
                new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                //byteBuffer = (ByteBuffer)param.args[5];
                //byte[] bytes = {};
                //byteBuffer.get(bytes, param.args[6], param.args[7]);

                String dump = "[request] " + Methods[(int)param.args[3]] + " url " + (String)param.args[2] + " hdr " + (String)param.args[4];
                XposedBridge.log(dump);
            }
        });
    }
}
