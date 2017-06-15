package com.test.arduinosocket.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.test.arduinosocket.R;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.common.Utils;
import com.test.arduinosocket.core.DeviceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by debojitk on 13/09/2016.
 */
public class MyPreferencesActivity extends PreferenceActivity {
    private static boolean preferenceChanged=false;
    private static Context context;
    private MyPreferenceFragment fragment;
    private AppCompatDelegate mDelegate;
    private DeviceManager deviceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        deviceManager =DeviceManager.getInstance();
        context=getBaseContext();
        preferenceChanged=false;
        fragment=new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content,fragment).commit();
    }
    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }
    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }
    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }
    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }
    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }
    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }
    public static class MyPreferenceFragment extends PreferenceFragment
    {
        private WifiManager mWifiManager;
        private List<ScanResult> mScanResults=new ArrayList<>();
        private SharedPreferences SP;
        private ListPreference listPreference;
        private ListPreference wifiNetworkMode;
        private EditTextPreference deviceWifiSsid;
        private EditTextPreference maxRecLen;
        private CheckBoxPreference notAtHome;
        public final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    synchronized (mScanResults) {
                        mScanResults = mWifiManager.getScanResults();
                        populatePreferenceList(listPreference,mScanResults);
                    }
                }
            }
        };

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            SP = PreferenceManager.getDefaultSharedPreferences(context);
            mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            context.registerReceiver(mWifiScanReceiver,
                    new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            mWifiManager.startScan();
            Utils.showMessage("Wifi scan in progress");

            addPreferencesFromResource(R.xml.preferences);
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
        private void populatePreferenceList(ListPreference pref, List<ScanResult> scanResults){
            if(scanResults!=null) {
                synchronized (scanResults) {
                    CharSequence[] entries=new CharSequence[scanResults.size()];
                    CharSequence[] values=new CharSequence[scanResults.size()];
                    int i=0;
                    for(ScanResult result:scanResults){
                        entries[i]=result.SSID;
                        values[i]=result.SSID;
                        i++;
                    }
                    if(scanResults.isEmpty()){
                        pref.setEnabled(false);
                    }else{
                        pref.setEntries(entries);
                        pref.setEntryValues(values);
                        pref.setEnabled(true);
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        serializePreferenceValues();
    }
    @Override
    public void onStop(){
        super.onStop();
        getDelegate().onStop();
        serializePreferenceValues();
    }
    @Override
    public void onPause(){
        super.onPause();
        serializePreferenceValues();
    }
    private void serializePreferenceValues(){
        try {
            context.unregisterReceiver(fragment.mWifiScanReceiver);
        }catch(Exception ex){

        }
        if(preferenceChanged) {
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String mode = SP.getString("wifiNetworkMode", "softap");
            String softAPSsid = SP.getString("deviceWifiSsid", "ESPAP");
            String stationModeSsid = SP.getString("wifiNetwork", "debojit-dlink");
            String stationPassword = SP.getString("wifiPassword", "India@123");
            String maxRecLenStr=SP.getString("maxRecLen", "200");
            int maxRecLen=Integer.parseInt(maxRecLenStr)*1024;//in bytes
            boolean notAtHome=SP.getBoolean("notAtHome",false);
            final StringBuffer sb = new StringBuffer();
            sb.append("mode=").append(mode).append(",");
            sb.append("softap_ssid=").append(softAPSsid).append(",");
            sb.append("station_ssid=").append(stationModeSsid).append(",");
            sb.append("station_pwd=").append(stationPassword).append(",");
            sb.append("not_at_home=").append(Boolean.toString(notAtHome)).append(",");
            sb.append("max_rec_len=").append(Integer.toString(maxRecLen));
            Log.d(Constants.LOG_TAG_MESSAGE,"Serialized settings: "+sb.toString());
            try {
                new Thread() {
                    @Override
                    public void run() {
                        if (deviceManager != null ) {
                            deviceManager.sendMessageRequest(Constants.SAVE_CONFIG, sb.toString(), null);
                        }
                    }
                }.start();
            } catch (Exception ex) {
                //ignore
            }
            preferenceChanged=false;
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        preferenceChanged=false;
    }
    @Override
    public void onResume(){
        super.onResume();
        preferenceChanged=false;
    }
}