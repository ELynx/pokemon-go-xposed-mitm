package com.elynx.pogoxmitm;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class HackSettings extends Activity implements CompoundButton.OnCheckedChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hack_settings);

        restoreSettings();
        setupScreen();
    }

    // TODO make this approach work
    //@Override
    //protected void onDestroy() {
    //    super.onDestroy();
    //
    //    saveSettings();
    //}

    protected void saveSettings() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("doIvHack", MitmProvider.doIvHack);
        editor.putBoolean("doSpeedHack", MitmProvider.doSpeedHack);
        editor.commit();
    }

    protected void restoreSettings() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        MitmProvider.doIvHack = pref.getBoolean("doIvHack", true);
        MitmProvider.doSpeedHack = pref.getBoolean("doSpeedHack", false);
    }

    protected void setupScreen() {
        CheckBox ivCheckBox = (CheckBox) findViewById(R.id.ivCheckBox);
        ivCheckBox.setChecked(MitmProvider.doIvHack);
        if (ivCheckBox.getOnFocusChangeListener() == null)
            ivCheckBox.setOnCheckedChangeListener(this);

        CheckBox speedCheckBox = (CheckBox) findViewById(R.id.speedCheckBox);
        speedCheckBox.setChecked(MitmProvider.doSpeedHack);
        if (speedCheckBox.getOnFocusChangeListener() == null)
            speedCheckBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.ivCheckBox:
                MitmProvider.doIvHack = isChecked;
                break;
            case R.id.speedCheckBox:
                MitmProvider.doSpeedHack = isChecked;
                break;
        }

        // TODO is this even OK?
        saveSettings();
    }
}
