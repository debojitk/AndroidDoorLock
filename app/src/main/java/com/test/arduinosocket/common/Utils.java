package com.test.arduinosocket.common;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.test.arduinosocket.activity.AsyncListenActivity;
import com.test.arduinosocket.MyApplication;
import com.test.arduinosocket.R;

import java.io.*;
import java.net.*;
import java.util.*;

import static android.content.Intent.*;
//import org.apache.http.conn.util.InetAddressUtils;

public class Utils {

    /**
     * Convert byte array to hex string
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sbuf = new StringBuilder();
        for(int idx=0; idx < bytes.length; idx++) {
            int intVal = bytes[idx] & 0xff;
            if (intVal < 0x10) sbuf.append("0");
            sbuf.append(Integer.toHexString(intVal).toUpperCase());
        }
        return sbuf.toString();
    }

    /**
     * Get utf8 byte array.
     * @param str
     * @return  array of NULL if error was found
     */
    public static byte[] getUTF8Bytes(String str) {
        try { return str.getBytes("UTF-8"); } catch (Exception ex) { return null; }
    }

    /**
     * Load UTF8withBOM or any ansi text file.
     * @param filename
     * @return
     * @throws java.io.IOException
     */
    public static String loadFileAsString(String filename) throws java.io.IOException {
        final int BUFLEN=1024;
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename), BUFLEN);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFLEN);
            byte[] bytes = new byte[BUFLEN];
            boolean isUTF8=false;
            int read,count=0;
            while((read=is.read(bytes)) != -1) {
                if (count==0 && bytes[0]==(byte)0xEF && bytes[1]==(byte)0xBB && bytes[2]==(byte)0xBF ) {
                    isUTF8=true;
                    baos.write(bytes, 3, read-3); // drop UTF8 bom marker
                } else {
                    baos.write(bytes, 0, read);
                }
                count+=read;
            }
            return isUTF8 ? new String(baos.toByteArray(), "UTF-8") : new String(baos.toByteArray());
        } finally {
            try{ is.close(); } catch(Exception ex){}
        }
    }

    /**
     * Returns MAC address of the given interface name.
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return  mac address or empty string
     */
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac==null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx=0; idx<mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                return buf.toString();
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if(!intf.getName().toLowerCase().startsWith("wlan")){
                    continue;
                }

                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
    public static void createNotification(int notificationId, String contentText, Context context, boolean isSticky){
        // prepare intent which is triggered if the
        // notification is selected

        //Intent intent = new Intent(context, NotificationEventHandler.class);
        //PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent myIntent = new Intent(context, AsyncListenActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,myIntent, FILL_IN_ACTION);
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

        notificationManager.notify(notificationId, notification);
    }
    public static void createNotificationWithYesNo(String contentText, Context context, boolean isSticky, String deviceId){
        // prepare intent which is triggered if the
        // notification is selected

        Intent myIntent = new Intent(context, AsyncListenActivity.class);
        myIntent.setAction("YES_ACTION");
        myIntent.putExtra(Constants.CALL_NOTIFICATION_CLIENT, Constants.YES_RESPONSE);
        myIntent.putExtra("DEVICE_ID", deviceId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,myIntent, FILL_IN_ACTION);

        Intent yesReceive = new Intent(context, AsyncListenActivity.class);
        yesReceive.setAction("YES_ACTION");
        yesReceive.putExtra(Constants.CALL_NOTIFICATION_CLIENT, Constants.YES_RESPONSE);
        yesReceive.putExtra("DEVICE_ID", deviceId);
        PendingIntent pendingIntentYes = PendingIntent.getActivity(context, 1, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action yesAction = new Notification.Action.Builder(R.drawable.ic_action_done, "Yes", pendingIntentYes).build();

        Intent noReceive = new Intent(context, AsyncListenActivity.class);
        noReceive.setAction("NO_ACTION");
        noReceive.putExtra(Constants.CALL_NOTIFICATION_CLIENT, Constants.NO_RESPONSE);
        noReceive.putExtra("DEVICE_ID", deviceId);
        PendingIntent pendingIntentNo = PendingIntent.getActivity(context, 1, noReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action noAction = new Notification.Action.Builder(R.drawable.ic_action_highlight_remove, "No", pendingIntentNo).build();
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
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_VIBRATE|Notification.FLAG_ONGOING_EVENT);
        //if (Build.VERSION.SDK_INT >= 21) notificationBuilder.setVibrate(new long[0]);
        notificationBuilder.addAction(yesAction);
        notificationBuilder.addAction(noAction);

        if(isSticky){
            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        }else{
            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        Notification notification  =notificationBuilder.build();
        notification.flags=isSticky?Notification.FLAG_INSISTENT:Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(Constants.DEVICE_REQUEST_NOTIFICATION_ID, notification);
    }

    public static void removeNotification(int notificationId, Context context){
        NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(notificationId);
    }

    private static Toast prevToast=null;
    private static Activity runningActivity=null;
    public static void showMessage(final String text){
        Log.i(Constants.LOG_TAG_TOAST,text);
        try {
            runningActivity= MyApplication.getCurrentActivity();
            if (runningActivity != null) {
                if (prevToast != null) {
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            prevToast.cancel();
                        }
                    });
                }
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            prevToast = Toast.makeText(runningActivity.getBaseContext(), text, Toast.LENGTH_LONG);
                            prevToast.show();
                        }catch(Exception ex){
                            //ignore
                        }
                    }
                });
            }
        }catch(Exception ex){
            //ignore
        }

    }
    public static String serialize(Object object){
        Gson gson=new Gson();
        String jsonStr=gson.toJson(object);
        return jsonStr;
    }

    public static <T> T deserialize(String serializedStr, Class<T> clazz){
        Gson gson=new Gson();
        return gson.fromJson(serializedStr, clazz);
    }


}
