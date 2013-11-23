/*
 * This file is part of MultiROM Manager.
 *
 * MultiROM Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MultiROM Manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MultiROM Manager. If not, see <http://www.gnu.org/licenses/>.
 */

package com.tassadar.multirommgr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
    public static final String GENERAL_UPDATE_CHECK =  "general_update_check";
    public static final String GENERAL_AUTO_REBOOT = "general_auto_reboot";
    public static final String GENERAL_DOWNLOAD_DIR = "general_download_dir";
    public static final String UTOUCH_SHOW_HIDDEN = "utouch_show_hidden";
    public static final String UTOUCH_DELETE_FILES = "utouch_delete_files";
    public static final String ABOUT_VERSION = "about_version";
    public static final String ABOUT_LICENSES = "about_licenses";
    public static final String DEV_ENABLE = "dev_enable";
    public static final String DEV_MANIFEST_URL = "dev_manifest_url";
    public static final String DEV_DEVICE_NAME = "dev_device_name";

    private static final String SRC_URL = "http://github.com/Tasssadar/MultiROMMgr";
    private static final int DEV_STEPS = 7;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences p = MultiROMMgrApplication.getPreferences();
        if(p.getString(GENERAL_DOWNLOAD_DIR, null) == null)
            Utils.setDownloadDir(Utils.getDefaultDownloadDir());

        addPreferencesFromResource(R.xml.settings);

        addDevOptions();

        p.registerOnSharedPreferenceChangeListener(this);
        updatePrefText();

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            Preference pref = findPreference(ABOUT_VERSION);
            pref.setSummary(pInfo.versionName);
            pref.setOnPreferenceClickListener(this);
        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Preference pref = findPreference(ABOUT_LICENSES);
        pref.setOnPreferenceClickListener(this);
    }

    public void onDestroy() {
        super.onDestroy();

        SharedPreferences p = MultiROMMgrApplication.getPreferences();
        p.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void updatePrefText() {
        Preference pref = findPreference(UTOUCH_DELETE_FILES);
        pref.setSummary(Utils.getString(R.string.pref_delete_utouch_files_summ,
                Utils.getDownloadDir(), UbuntuInstallTask.DOWN_DIR));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences p, String key) {
        if(key.equals(GENERAL_UPDATE_CHECK)) {
            UpdateChecker.updateAlarmStatus();
        } else if(key.equals(GENERAL_DOWNLOAD_DIR)) {
            Utils.setDownloadDir(p.getString(key, Utils.getDefaultDownloadDir()));
            updatePrefText();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        if(pref.getKey().equals(ABOUT_VERSION)) {
            if(m_clickCounter == -1) {
                Toast.makeText(this, R.string.already_developer, Toast.LENGTH_SHORT).show();
                return true;
            }

            ++m_clickCounter;

            if(m_clickCounter == DEV_STEPS) {
                Toast.makeText(this, R.string.now_developer, Toast.LENGTH_SHORT).show();

                SharedPreferences.Editor p = MultiROMMgrApplication.getPreferences().edit();
                p.putBoolean(DEV_ENABLE, true);
                p.commit();

                addDevOptions();
            } else if(m_clickCounter >= 3) {
                String s = getString(R.string.steps_developer, DEV_STEPS - m_clickCounter);
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if(pref.getKey().equals(ABOUT_LICENSES)) {
            final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(SRC_URL));
            startActivity(intent);
            return true;
        }
        return false;
    }

    private void addDevOptions() {
        SharedPreferences p = MultiROMMgrApplication.getPreferences();
        if(!p.getBoolean(DEV_ENABLE, false))
            return;

        m_clickCounter = -1;
        addPreferencesFromResource(R.xml.dev_options);

        EditTextPreference pref = (EditTextPreference)findPreference(DEV_MANIFEST_URL);
        pref.setText(p.getString(DEV_MANIFEST_URL, Manifest.DEFAULT_URL));
        pref = (EditTextPreference)findPreference(DEV_DEVICE_NAME);
        pref.setText(p.getString(DEV_DEVICE_NAME, Build.BOARD));
    }

    private int m_clickCounter;
}
