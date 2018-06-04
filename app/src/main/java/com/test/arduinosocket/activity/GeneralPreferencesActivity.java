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
import com.test.arduinosocket.fragment.GeneralPreferenceFragment;
import com.test.arduinosocket.fragment.LockDevicePreferenceFragment;

import java.util.Map;

/**
 * Created by debojitk on 02/06/2018.
 */
public class GeneralPreferencesActivity extends PreferenceActivity {
    private GeneralPreferenceFragment fragment;
    private AppCompatDelegate mDelegate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        fragment=new GeneralPreferenceFragment();
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
    }

    @Override
    public void onStart(){
        super.onStart();
    }
    @Override
    public void onResume(){
        super.onResume();
    }
}