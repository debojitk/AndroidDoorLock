package com.test.arduinosocket.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.test.arduinosocket.activity.AsyncListenActivity;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.R;
import com.test.arduinosocket.common.Utils;
import com.test.arduinosocket.core.DeviceManager;

/**
 * Created by debojitk on 17/08/2016.
 */
public class NotificationEventHandler extends Service {
    private DeviceManager server;
    /** interface for clients that bind */
    IBinder mBinder=new LocalBinder();

    /** indicates whether onRebind should be used */
    boolean mAllowRebind;

    public NotificationEventHandler() {
        //this.runner = new ServiceRunner(this);
        //runner.setRunning(false);
        this.server=DeviceManager.getInstance();
    }

    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Constants.LOG_TAG_SERVICE,"service bound from intent "+intent.getType());
        //startService();
        return mBinder;
    }

    /** Called when all clients have unbound with unbindService() */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(Constants.LOG_TAG_SERVICE,"service unbound from intent "+intent.getType());
        return mAllowRebind;
    }

    /** Called when a client is binding to the service with bindService()*/
    @Override
    public void onRebind(Intent intent) {
        Log.d(Constants.LOG_TAG_MESSAGE, "rebind called");

    }

    public DeviceManager getServer(){
        return server;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        startForeground(1,createNotification("NotificationReceiverService Running...",getBaseContext(),false));
        Utils.removeNotification(Constants.WIFI_STOPPED_NOTIFICATION_ID,getBaseContext());
        startService();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.showMessage("Notification Service Destroyed");
        Utils.createNotification(Constants.WIFI_STOPPED_NOTIFICATION_ID, "Service stopped, please turn on Wifi", getBaseContext(), false);
    }

    private synchronized void startService(){
        server.start(getApplicationContext());
    }
    public class LocalBinder extends Binder{
        public NotificationEventHandler getServiceInstance(){
            return NotificationEventHandler.this;
        }
    }
	
    public static Notification createNotification(String contentText, Context context, boolean isSticky){
        // prepare intent which is triggered if the
        // notification is selected

        //Intent intent = new Intent(context, NotificationEventHandler.class);
        //PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent myIntent = new Intent(context, AsyncListenActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,myIntent, Intent.FILL_IN_ACTION);
        // build notification
        // the addAction re-use the same intent to keep the example short
        Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(), R.drawable.notify_large);
        Notification.Builder notificationBuilder  = new Notification.Builder(context)
                .setContentTitle(Constants.DOOR_LOCKER_STATUS)
                .setContentText(contentText)
                .setStyle(new Notification.BigTextStyle().bigText(contentText))
                .setLargeIcon(Bitmap.createScaledBitmap(bitmap , 112, 112, false))
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.PRIORITY_MAX);
        if(isSticky){
            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        }else{
            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }


        Notification notification  =notificationBuilder.build();
        notification.flags=isSticky?Notification.FLAG_INSISTENT|Notification.FLAG_AUTO_CANCEL:Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        return notification;
    }

}
