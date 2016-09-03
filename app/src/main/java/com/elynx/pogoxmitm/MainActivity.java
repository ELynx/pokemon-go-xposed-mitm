package com.elynx.pogoxmitm;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.view.MenuItem;

/**
 * Whole settings scheme is taken from https://github.com/krokofant/JodelXposed
 * Big thanks to authors for advice and code alike
 */

public class MainActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    protected SwitchPreference ivHackPreference;
    protected SwitchPreference fortHackPreference;
    protected SwitchPreference exportHackPreference;

    protected Options options;

    protected Boolean ivHack;
    protected Boolean fortHack;
    protected Boolean exportHack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = Options.getInstance();
        ivHack = Options.getInstance().getIvHack();
        fortHack = Options.getInstance().getFortHack();
        exportHack = Options.getInstance().getExportHack();

        addPreferencesFromResource(R.xml.preferences);

        ivHackPreference = (SwitchPreference) findPreference("pref_iv");
        fortHackPreference = (SwitchPreference) findPreference("pref_fort");
        exportHackPreference = (SwitchPreference) findPreference("pref_export");

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
        ivHackPreference.setChecked(ivHack);
        fortHackPreference.setChecked(fortHack);
        exportHackPreference.setChecked(exportHack);
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
        boolean value = (boolean) obj;
        boolean doSave = false;

        switch (preference.getKey()) {
            case "pref_iv":
                ivHack = value;
                doSave = true;
                break;
            case "pref_fort":
                fortHack = value;
                doSave = true;
                break;
            case "pref_export":
                exportHack = value;
                doSave = true;
                break;
        }

        if (doSave) {
            options.save();
        }

        return true;
    }
}
