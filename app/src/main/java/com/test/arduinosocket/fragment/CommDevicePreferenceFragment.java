package com.test.arduinosocket.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;

import com.test.arduinosocket.R;
import com.test.arduinosocket.common.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by debojitk on 30/05/2018.
 */

public class CommDevicePreferenceFragment extends DevicePreferenceFragment {
    private ListPreference wifiNetworkMode;
    private EditTextPreference deviceWifiSsid;
    private EditTextPreference maxRecLen;
    private CheckBoxPreference notAtHome;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.comm_device_preferences);
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
        maxRecLen=(EditTextPreference) findPreference("maxRecLen");
        if(maxRecLen.getText()!=null){
            maxRecLen.setSummary(maxRecLen.getSummary()+" - "+maxRecLen.getText());
        }
        maxRecLen.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                preference.setSummary("Size: "+o.toString());
                preferenceChanged=true;
                return true;
            }
        });
        notAtHome=(CheckBoxPreference) findPreference("notAtHome");
        notAtHome.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                preferenceChanged=true;
                return true;
            }
        });
    }
}
