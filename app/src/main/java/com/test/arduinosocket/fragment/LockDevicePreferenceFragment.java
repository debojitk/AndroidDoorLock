package com.test.arduinosocket.fragment;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;

import com.test.arduinosocket.R;

/**
 * Created by debojitk on 30/05/2018.
 */

public class LockDevicePreferenceFragment extends DevicePreferenceFragment {
    private ListPreference wifiNetworkMode;
    private EditTextPreference deviceWifiSsid;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lock_device_preferences);
        listPreference = (ListPreference) findPreference("wifiNetwork");
        listPreference.setEnabled(false);
        if(listPreference.getValue()!=null){
            listPreference.setSummary("SSID:"+listPreference.getValue());
        }
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                preference.setSummary("SSID: "+o.toString());
                preferenceChanged=true;
                return true;
            }
        });

        wifiNetworkMode=(ListPreference)findPreference("wifiNetworkMode");
        if(wifiNetworkMode.getValue()!=null){
            wifiNetworkMode.setSummary("Mode:"+wifiNetworkMode.getValue());
        }
        wifiNetworkMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                preference.setSummary("Mode: "+o.toString());
                preferenceChanged=true;
                return true;
            }
        });

        deviceWifiSsid=(EditTextPreference)findPreference("deviceWifiSsid");
        if(deviceWifiSsid.getText()!=null){
            deviceWifiSsid.setSummary(deviceWifiSsid.getText());
        }
        deviceWifiSsid.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                preference.setSummary(o.toString());
                preferenceChanged=true;
                return true;
            }
        });
    }
}
