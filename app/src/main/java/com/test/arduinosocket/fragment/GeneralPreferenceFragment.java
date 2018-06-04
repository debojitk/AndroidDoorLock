package com.test.arduinosocket.fragment;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.test.arduinosocket.R;

/**
 * Created by debojitk on 30/05/2018.
 */

public class GeneralPreferenceFragment extends PreferenceFragment {
    private WifiManager mWifiManager;
    protected Context context;
    protected boolean preferenceChanged=false;
    private EditTextPreference deviceName;
    private EditTextPreference deviceId;
    private EditTextPreference deviceKey;
    private EditTextPreference masterPin;
    private CheckBoxPreference serverEnabledOnWifiEnabled;
    private CheckBoxPreference pinUnlock;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LinearLayout v = (LinearLayout) super.onCreateView(inflater, container, savedInstanceState);

        View view=inflater.inflate(R.layout.button_array, v, false);
        v.addView(view);
        return v;

    }
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        context=getActivity().getApplicationContext();
        getPreferenceManager().setSharedPreferencesName(context.getString(R.string.com_app_wifilock_general_pref));
        addPreferencesFromResource(R.xml.this_device_preferences);
        deviceName=(EditTextPreference)findPreference("phoneName");
        if(deviceName.getText()!=null){
            deviceName.setSummary(deviceName.getText());
        }
        deviceName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                deviceName.setSummary(newValue.toString());
                return true;
            }
        });
        deviceId=(EditTextPreference)findPreference("phoneId");
        if(deviceId.getText()!=null){
            deviceId.setSummary(deviceId.getText());
        }
        deviceId.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                deviceId.setSummary(newValue.toString());
                return true;
            }
        });

        deviceKey=(EditTextPreference)findPreference("phoneKey");
        deviceKey.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        if(deviceKey.getText()!=null){
            deviceKey.setSummary(deviceKey.getText());
        }
        deviceKey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                deviceKey.setSummary(newValue.toString());
                return true;
            }
        });

    }

}
