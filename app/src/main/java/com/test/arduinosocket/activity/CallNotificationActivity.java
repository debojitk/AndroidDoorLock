package com.test.arduinosocket.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.test.arduinosocket.MyApplication;
import com.test.arduinosocket.R;
import com.test.arduinosocket.activity.layouts.UnlockBarLayout;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.core.CommandData;
import com.test.arduinosocket.core.DeviceManager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CallNotificationActivity extends Activity {
    private Ringtone ringtone;
    private DeviceManager deviceManager;
    private Thread notificationStopThread;
    private CommandData commandData;
    private TextView callingDoor;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            if(notificationStopThread!=null){
                notificationStopThread.interrupt();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_fullscreen);
        callingDoor=(TextView) findViewById(R.id.callingDoor);
        UnlockBarLayout unlock = (UnlockBarLayout) findViewById(R.id.unlock);
        deviceManager=DeviceManager.getInstance();

        // Attach listener
        unlock.setOnUnlockListener(new UnlockBarLayout.OnUnlockListener() {
            @Override
            public void onAccept()
            {
                //Utils.showMessage("Unlocked!");
                if(ringtone!=null && ringtone.isPlaying()){
                    ringtone.stop();
                }
                sendNotifyResponse(true);
                notificationStopThread.interrupt();
                deviceManager.setCurrentDevice(deviceManager.getDevice(commandData.getDeviceId()));
                deviceManager.resetNotificationProcessing();
                Intent i = new Intent(getApplicationContext(), AsyncListenActivity.class);
                startActivity(i);
            }

            @Override
            public void onReject() {
                if(ringtone!=null && ringtone.isPlaying()){
                    ringtone.stop();
                }
                //send NACK response
                sendNotifyResponse(false);
                //finish the activity
                notificationStopThread.interrupt();
                deviceManager.resetNotificationProcessing();
            }
        });

    }

    @Override
    protected void onStart() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.LOCAL_BC_EVENT_NOTIFICATION));
        MyApplication.setCurrentActivity(this);
        String intentExtra = getIntent().getStringExtra(Constants.LOCAL_BC_EVENT_DATA);
        commandData=new CommandData(intentExtra);
        super.onStart();
        callingDoor.setText(commandData.getDeviceId()+" is calling");
        notificationStopThread=new Thread(){
            @Override
            public void run() {

                if (ringtone == null) {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
                }
                if (!ringtone.isPlaying()) {
                    ringtone.play();
                }
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    Log.e(Constants.LOG_TAG_MESSAGE, "Ring notification handler interrupted");
                }
                if(ringtone!=null && ringtone.isPlaying()){
                    ringtone.stop();
                }
                deviceManager.resetNotificationProcessing();
                commandData=null;
                finish();
            }
        };
        notificationStopThread.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(ringtone!=null && ringtone.isPlaying()){
            ringtone.stop();
        }
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onStop();
    }

    public void sendNotifyResponse(boolean response){
        final CommandData commandData=new CommandData();
        String notifyCommand=commandData.setCommand(Constants.NOTIFY)
                .setDeviceId(deviceManager.getPhoneId())
                .setDeviceKey(deviceManager.getPhoneKey())
                .setResponse(true)
                .setError(!response)
                .buildCommandString();

        final String finalResponse=notifyCommand;
        new Thread(){
            @Override
            public void run() {
                deviceManager.sendMessageResponse(finalResponse, commandData.getDeviceId());
            }
        }.start();
    }

}
