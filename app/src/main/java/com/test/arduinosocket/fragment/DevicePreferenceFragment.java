package com.test.arduinosocket.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.test.arduinosocket.common.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by debojitk on 30/05/2018.
 */

public class DevicePreferenceFragment extends PreferenceFragment {
    private WifiManager mWifiManager;
    private List<ScanResult> mScanResults=new ArrayList<>();
    protected ListPreference listPreference;
    protected Context context;
    protected boolean preferenceChanged=false;

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
        getPreferenceManager().setSharedPreferencesName("test_pref");
        context=getActivity().getApplicationContext();
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        context.registerReceiver(mWifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mWifiManager.startScan();
        Utils.showMessage("Wifi scan in progress");
    }
    protected void populatePreferenceList(ListPreference pref, List<ScanResult> scanResults){
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

    public WifiManager getmWifiManager() {
        return mWifiManager;
    }

    public void setmWifiManager(WifiManager mWifiManager) {
        this.mWifiManager = mWifiManager;
    }

    public boolean isPreferenceChanged() {
        return preferenceChanged;
    }

    public void setPreferenceChanged(boolean preferenceChanged) {
        this.preferenceChanged = preferenceChanged;
    }
}
