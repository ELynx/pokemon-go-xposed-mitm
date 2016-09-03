package com.elynx.pogoxmitm;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

/**
 * Whole settings scheme is taken from https://github.com/krokofant/JodelXposed
 * Big thanks to authors for advice and code alike
 */

public class MainActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    protected CheckBoxPreference ivHackPreference;
    protected CheckBoxPreference fortHackPreference;
    protected CheckBoxPreference exportHackPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        ivHackPreference = (CheckBoxPreference) findPreference("pref_iv");
        fortHackPreference = (CheckBoxPreference) findPreference("pref_fort");
        exportHackPreference = (CheckBoxPreference) findPreference("pref_export");

        ivHackPreference.setOnPreferenceChangeListener(this);
        fortHackPreference.setOnPreferenceChangeListener(this);
        exportHackPreference.setOnPreferenceChangeListener(this);

        updateFieldsFromSettings();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateFieldsFromSettings();
    }

    protected void updateFieldsFromSettings() {
        ivHackPreference.setChecked(Options.getInstance().getIvHack().isActive());
        fortHackPreference.setChecked(Options.getInstance().getFortHack().isActive());
        exportHackPreference.setChecked(Options.getInstance().getExportHack().isActive());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(0, 0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object obj) {
        boolean value = false;

        if (obj instanceof Boolean) {
            value = (Boolean) obj;
            android.util.Log.i("[PoGo MITM]", preference.getKey() + " " + obj.toString());
        }

        boolean doSave = false;

        switch (preference.getKey()) {
            case "pref_iv":
                Options.getInstance().getIvHack().setActive(value);
                doSave = true;
                break;
            case "pref_fort":
                Options.getInstance().getFortHack().setActive(value);
                doSave = true;
                break;
            case "pref_export":
                Options.getInstance().getExportHack().setActive(value);
                doSave = true;
                break;
        }

        if (doSave) {
            Options.getInstance().save();
        }

        return doSave;
    }
}
