package com.elynx.pogoxmitm.modules;

import android.app.AndroidAppHelper;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import de.robv.android.xposed.XposedBridge;

public abstract class ModuleBase {

    protected void showToast(final String toastText) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AndroidAppHelper.currentApplication().getBaseContext(), toastText, Toast.LENGTH_SHORT).show();

                log(toastText);
            }
        });
    }

    protected void log(String text) {
        XposedBridge.log("[PoGo MITM] " + text);
    }
}
