package com.elynx.pogoxmitm;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Activity to handle user settings.
 * A lot of it is taken from https://github.com/stephaneclotilde/SMSXposed
 */

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PreferencesFragment()).commit();
    }

    public static class PreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.preferences);

            //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            //SharedPreferences.Editor edit = settings.edit();
            //edit.commit();
        }
    }
}
