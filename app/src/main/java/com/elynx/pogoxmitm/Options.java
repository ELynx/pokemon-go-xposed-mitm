package com.elynx.pogoxmitm;

import android.os.Environment;
import android.os.FileObserver;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.robv.android.xposed.XposedBridge;

public class Options extends FileObserver {
    public static String SettingsPath = Environment.getExternalStorageDirectory() + "/Pokemon/.pogoxmitm-settings";

    private static Options singleton;

    public static Options getInstance() {
        if (singleton == null)
            singleton = new Options();
        return singleton;
    }

    public static class OptionsObject {
        public Boolean ivHack = new Boolean(true);
        public Boolean fortHack = new Boolean(false);
        public Boolean exportHack = new Boolean(false);
    }

    public Boolean getIvHack() {
        return this.options.ivHack;
    }

    public Boolean getFortHack() {
        return this.options.fortHack;
    }

    public Boolean getExportHack() {
        return this.options.exportHack;
    }

    private File settingsFile;
    private JsonAdapter<OptionsObject> jsonAdapter;
    public OptionsObject options;

    public Options() {
        super(SettingsPath, CLOSE_WRITE);
        Moshi moshi = new Moshi.Builder().build();
        jsonAdapter = moshi.adapter(OptionsObject.class);
        XposedBridge.log("[PoGo MITM] Init file object with path: " + SettingsPath);
        settingsFile = new File(SettingsPath);

        if (!settingsFile.exists()) {
            options = new OptionsObject();
            save();
        } else {
            load();
        }
        startWatching();
    }

    private void writeFile() {
        String settingsJson = jsonAdapter.toJson(options);
        try {
            XposedBridge.log(String.format("[PoGo MITM] Writing %s to file", settingsJson));
            FileOutputStream output = FileUtils.openOutputStream(settingsFile);
            output.write(settingsJson.getBytes());
            output.close();
        } catch (IOException e) {
            XposedBridge.log("[PoGo MITM] ERROR Could not write to file");
            XposedBridge.log(e);
        }
    }

    public void save() {
        writeFile();
    }

    public void load() {
        XposedBridge.log("[PoGo MITM] Loading settings file");
        try {
            String json = FileUtils.readFileToString(settingsFile, Charsets.UTF_8);
            options = jsonAdapter.fromJson(json);

            XposedBridge.log("[PoGo MITM] Settings"
                    + "\nIvHack " + options.ivHack.toString()
                    + "\nFortHack " + options.fortHack.toString()
                    + "\nExportHack " + options.exportHack.toString()
            );

        } catch (IOException e) {
            XposedBridge.log(e);
        }
    }

    @Override
    public void onEvent(int event, String path) {
        load();
    }
}
