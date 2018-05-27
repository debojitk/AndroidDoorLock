package com.test.arduinosocket.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.test.arduinosocket.activity.AsyncListenActivity;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.MyApplication;
import com.test.arduinosocket.common.Utils;
import com.test.arduinosocket.core.DeviceManager;

public class NotificationReceiver extends BroadcastReceiver {
    private static boolean alreadyConnected=false;
    private Intent mServiceIntent;
    private WifiManager.WifiLock wifiLock;

    public NotificationReceiver() {
        //Log.d(Constants.LOG_TAG_MESSAGE,"NotificationReceiver initialized");
    }

    public static boolean isAlreadyConnected() {
        return alreadyConnected;
    }

    public static void setAlreadyConnected(boolean alreadyConnected) {
        NotificationReceiver.alreadyConnected = alreadyConnected;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(Constants.LOG_TAG_MESSAGE,"Intent received "+intent.getAction());
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //WifiManager manager=(WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        WifiManager wifiManager = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
        wifiLock=wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, Constants.LOG_TAG_SERVICE);


        boolean isConnected = wifi != null && wifi.isConnectedOrConnecting();
        if (isConnected&&!alreadyConnected) {
            Log.d("Network Available ", "YES");
            mServiceIntent=new Intent(context,NotificationEventHandler.class);
            context.startService(mServiceIntent);

            WifiInfo info = wifiManager.getConnectionInfo ();
            String ssid  = info.getSSID();
            try{
                //MyApplication.getCurrentActivity().setCommButtonsState(true);
                if(MyApplication.getCurrentActivity()!=null && MyApplication.getCurrentActivity() instanceof AsyncListenActivity) {
                    AsyncListenActivity activity=(AsyncListenActivity)MyApplication.getCurrentActivity();
                    activity.setSSID(ssid);
                    activity.setServerAddress("(" + Utils.getIPAddress(true) + ")");
                }
            }catch (Exception ex){
                //ignore
            }
            alreadyConnected=true;

        }
        if (!isConnected&&alreadyConnected) {
            mServiceIntent=new Intent(context,NotificationEventHandler.class);
            context.stopService(mServiceIntent);
            try{
                //wifi disconnected so remove all devices and set current device as null
                DeviceManager.getInstance().disposeAll();
            }catch (Exception ex){
                //ignore
                Log.d(Constants.LOG_TAG_SERVICE, "An exception occurred", ex);
            }
            Log.d("Network Available ", "NO");
            alreadyConnected=false;
        }
    }

}
