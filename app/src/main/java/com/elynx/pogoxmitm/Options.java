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
        android.util.Log.i("[PoGo MITM]", "Init file object with path: " + SettingsPath);
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
            android.util.Log.i("[PoGo MITM]", "Writing %s to file " + settingsJson);
            FileOutputStream output = FileUtils.openOutputStream(settingsFile);
            output.write(settingsJson.getBytes());
            output.close();
        } catch (IOException e) {
            android.util.Log.i("[PoGo MITM]", "ERROR Could not write to file");
            android.util.Log.i("[PoGo MITM]", e.toString());
        }
    }

    public void save() {
        writeFile();
    }

    public void load() {
        android.util.Log.i("[PoGo MITM]", "Loading settings file");
        try {
            String json = FileUtils.readFileToString(settingsFile, Charsets.UTF_8);
            options = jsonAdapter.fromJson(json);

            android.util.Log.i("[PoGo MITM]", "Settings"
                    + "\nIvHack " + options.ivHack.toString()
                    + "\nFortHack " + options.fortHack.toString()
                    + "\nExportHack " + options.exportHack.toString()
            );

        } catch (IOException e) {
            android.util.Log.i("[PoGo MITM]", e.toString());
        }
    }

    @Override
    public void onEvent(int event, String path) {
        load();
    }
}
