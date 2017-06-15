package com.test.arduinosocket;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.test.arduinosocket.activity.AsyncListenActivity;
import com.test.arduinosocket.common.Constants;


public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static boolean mainActivityVisible;
    private static AsyncListenActivity mainActivity = null;
    private static Activity mCurrentActivity = null;

    @Nullable
    public static Activity getCurrentActivity(){
        if(mCurrentActivity==null || mCurrentActivity!=null && mCurrentActivity.isDestroyed()){
            return null;
        }else {
            return mCurrentActivity;
        }
    }
    public static void setCurrentActivity(Activity mCurrentActivity){
        MyApplication.mCurrentActivity = mCurrentActivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Register to be notified of activity state changes
        registerActivityLifecycleCallbacks(this);
    }

    public static  boolean isMainActivityVisible() {
        return mainActivityVisible;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(Constants.LOG_TAG_MESSAGE, "Activity resumed: "+activity);
        mCurrentActivity=activity;
        if (activity instanceof AsyncListenActivity) {
            mainActivity = (AsyncListenActivity) activity;
        }
        printCurrentActivity();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(Constants.LOG_TAG_MESSAGE, "onActivityStopped: "+activity);
        /*mCurrentActivity=null;
        if (activity instanceof AsyncListenActivity) {
            mainActivityVisible = false;
        }*/
        printCurrentActivity();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        Log.d(Constants.LOG_TAG_MESSAGE, "onActivityCreated: "+activity);
        mCurrentActivity=activity;
        if (activity instanceof AsyncListenActivity) {
            mainActivityVisible = true;
            mainActivity=(AsyncListenActivity)activity;
        }
        printCurrentActivity();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(Constants.LOG_TAG_MESSAGE, "onActivityPaused: "+activity);
        mCurrentActivity=null;
        if (activity instanceof AsyncListenActivity) {
            mainActivityVisible = false;
        }
        printCurrentActivity();
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(Constants.LOG_TAG_MESSAGE, "onActivityDestroyed"+activity);
        /*mCurrentActivity=null;
        if (activity instanceof AsyncListenActivity) {
            mainActivityVisible = false;
        }*/
        printCurrentActivity();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        Log.d(Constants.LOG_TAG_MESSAGE, "onActivitySaveInstanceState");
        printCurrentActivity();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(Constants.LOG_TAG_MESSAGE, "onActivityStarted: "+activity);
        mCurrentActivity= activity;
        if (activity instanceof AsyncListenActivity) {
            mainActivityVisible = true;
            mainActivity=(AsyncListenActivity)activity;
        }
        printCurrentActivity();
    }
    // Other state change callback stubs
    public void printCurrentActivity(){
        Log.d(Constants.LOG_TAG_MESSAGE,"Current activity is: " + mCurrentActivity);
    }
}