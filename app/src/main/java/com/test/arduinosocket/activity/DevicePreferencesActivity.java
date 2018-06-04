package com.test.arduinosocket.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.core.Device;
import com.test.arduinosocket.core.DeviceManager;
import com.test.arduinosocket.fragment.CommDevicePreferenceFragment;
import com.test.arduinosocket.fragment.DevicePreferenceFragment;
import com.test.arduinosocket.fragment.LockDevicePreferenceFragment;

import java.util.Map;

/**
 * Created by debojitk on 13/09/2016.
 */
public class DevicePreferencesActivity extends PreferenceActivity {
    private boolean preferenceChanged=false;
    private Context context;
    private DevicePreferenceFragment fragment;
    private AppCompatDelegate mDelegate;
    private DeviceManager deviceManager;
    private SharedPreferences preferences;
    private String deviceId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        deviceManager =DeviceManager.getInstance();
        context=getBaseContext();
        preferences=getSharedPreferences("test_pref", MODE_PRIVATE);

        //loading device preferences from serialized device object
        deviceId=getIntent().getStringExtra("deviceId");
        Device persistedDevice=deviceManager.getAllPairedDevices(context).get(deviceId);
        Map<String, Object> devicePreferences=persistedDevice.getSettingsMap();
        if(devicePreferences!=null && !devicePreferences.isEmpty()) {
            SharedPreferences.Editor editor = preferences.edit();
            for (Map.Entry<String, Object> entry : devicePreferences.entrySet()) {
                if(entry.getValue().getClass().equals(Integer.class)){
                    editor.putInt(entry.getKey(), (Integer)entry.getValue());
                }else if(entry.getValue().getClass().equals(Boolean.class)){
                    editor.putBoolean(entry.getKey(), (Boolean)entry.getValue());
                }else if(entry.getValue().getClass().equals(String.class)){
                    editor.putString(entry.getKey(), (String)entry.getValue());
                }
            }
            editor.apply();
        }
        preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                //TODO: add changelistener
                preferenceChanged=true;
                Device persistedDevice=deviceManager.getAllPairedDevices(context).get(deviceId);
                Map<String, ?> map=sharedPreferences.getAll();
                if(map.get(key).getClass().equals(String.class)){
                    persistedDevice.getSettingsMap().put(key, sharedPreferences.getString(key, getDefaultValue(key)));
                }else if(map.get(key).getClass().equals(Boolean.class)){
                    persistedDevice.getSettingsMap().put(key, sharedPreferences.getBoolean(key, false));
                }else if(map.get(key).getClass().equals(Integer.class)){
                    persistedDevice.getSettingsMap().put(key, sharedPreferences.getInt(key, 0));
                }

                deviceManager.persistDeviceData(persistedDevice);
            }
        });

        if(Constants.DEVICE_TYPE_COMM.equals(persistedDevice.getDeviceType())){
            fragment=new CommDevicePreferenceFragment();
        }else if(Constants.DEVICE_TYPE_LOCK.equals(persistedDevice.getDeviceType())){
            fragment=new LockDevicePreferenceFragment();
        }
        getFragmentManager().beginTransaction().replace(android.R.id.content,fragment).commit();
    }

    private String getDefaultValue(String key){
        String retVal="";

        return retVal;
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


/*    public static class DynamicPreferenceFragment extends PreferenceFragment
    {
        private SharedPreferences SP;
        private EditTextPreference maxRecLen;
        private CheckBoxPreference notAtHome;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName("test_pref");
            addPreferencesFromResource(R.xml.prefscreen);
            PreferenceScreen preferenceScreen=getPreferenceScreen();
            SP = getPreferenceScreen().getSharedPreferences();
            Log.d("PREF_LOG", SP.getAll().toString());

            maxRecLen= new EditTextPreference(preferenceScreen.getContext());
            maxRecLen.setKey("maxRecLen");
            maxRecLen.setTitle("Max Record Length");
            //maxRecLen.setText(comm_device_preferences.getString("maxRecLen", "defVal"));
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
            preferenceScreen.addPreference(maxRecLen);

            notAtHome=new CheckBoxPreference (preferenceScreen.getContext());
            notAtHome.setKey("notAtHome");
            notAtHome.setTitle("Not at Home");
            notAtHome.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    preferenceChanged=true;
                    return true;
                }
            });
            preferenceScreen.addPreference(notAtHome);
        }

    }*/

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
            Map<String, ?> map=preferences.getAll();
            final StringBuffer sb = new StringBuffer();
            for(Map.Entry<String, ?> entry:map.entrySet()){
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
            }
            Log.d(Constants.LOG_TAG_MESSAGE,"Serialized settings: "+sb.toString());
            //clear the preference
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear().apply();

            try {
                new Thread() {
                    @Override
                    public void run() {
                        if (deviceManager != null && deviceId!=null) {
                            deviceManager.sendMessageRequest(deviceId, Constants.SAVE_CONFIG, sb.toString(), null);
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