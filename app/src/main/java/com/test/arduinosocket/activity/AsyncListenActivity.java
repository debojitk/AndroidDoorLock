package com.test.arduinosocket.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.arduinosocket.R;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.common.Utils;
import com.test.arduinosocket.core.CommandData;
import com.test.arduinosocket.core.Device;
import com.test.arduinosocket.core.DeviceManager;
import com.test.arduinosocket.network.TCPIPAudioController;
import com.test.arduinosocket.network.UDPBroadcastCommandProcessor;
import com.test.arduinosocket.notification.NotificationEventHandler;

public class AsyncListenActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        NavigationView.OnNavigationItemSelectedListener {

    private ImageView buttonImageViewConnect, buttonImageViewDisconnect, buttonImageViewLocker, buttonImageViewHello, buttonImageViewReset;
    private TextView ssidLabel, ipLabel, serverInfoLabel;
    private int oldAudioMode, oldRingerMode;
    boolean isSpeakerPhoneOn;
    private TCPIPAudioController tcpipAudioController;
    private DialogInterface.OnClickListener dialogClickListener;
    private boolean userAccessGiven = false;
    private UDPBroadcastCommandProcessor udpBroadcastServer;
    private DeviceManager deviceManager;
    private Intent mServiceIntent;

    static boolean active = false;
    private boolean mBounded;
    private NotificationEventHandler serviceInstance;
    private boolean imageButtonLockPressed = false;

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public void setDeviceManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    public boolean isImageButtonLockPressed() {
        return imageButtonLockPressed;
    }

    public void setImageButtonLockPressed(boolean imageButtonLockPressed) {
        this.imageButtonLockPressed = imageButtonLockPressed;
    }

    private BroadcastReceiver mNotifyMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
        }
    };

    private BroadcastReceiver mActiveDeviceChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String deviceId = intent.getStringExtra(Constants.INTENT_DEVICE_ID);
            Log.d(Constants.LOG_TAG_MESSAGE, "Got deviceId: " + deviceId);
            //TODO render UI based on currentDevice
            updateDisplayState();
        }
    };


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Utils.showMessage("Service is disconnected");
            mBounded = false;
            serviceInstance = null;
            updateDisplayState();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Constants.LOG_TAG_MESSAGE, "onServiceConnected called");
            Utils.showMessage("Service is connected");
            mBounded = true;
            NotificationEventHandler.LocalBinder mLocalBinder = (NotificationEventHandler.LocalBinder) service;
            serviceInstance = mLocalBinder.getServiceInstance();
            setDeviceManager(serviceInstance.getServer());
            setUdpBroadcastServer(getDeviceManager().getBroadcastCommandProcessor());
            if (getUdpBroadcastServer() != null) {
                getUdpBroadcastServer().setActivity(AsyncListenActivity.this);
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, MyPreferencesActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            // Handle the camera action
        } else if (id == R.id.nav_history) {

        } else if (id == R.id.nav_recorder) {
            Intent i = new Intent(this, RecorderActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_status) {
            Intent i = new Intent(this, CallNotificationActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if(id == R.id.nav_locks){
            Intent i = new Intent(this, LockManagementActivity.class);
            startActivity(i);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public UDPBroadcastCommandProcessor getUdpBroadcastServer() {
        if(udpBroadcastServer ==null){
            udpBroadcastServer =UDPBroadcastCommandProcessor.getInstance();
        }
        return udpBroadcastServer;
    }

    public void setUdpBroadcastServer(UDPBroadcastCommandProcessor udpBroadcastServer) {
        this.udpBroadcastServer = udpBroadcastServer;
    }

    public TCPIPAudioController getTcpipAudioController() {
        return tcpipAudioController;
    }

    // Method to start the service
    public void startService() {
        startService(new Intent(getApplicationContext(), NotificationEventHandler.class));
    }

    // Method to stop the service
    public void stopService() {
        stopService(new Intent(getApplicationContext(), NotificationEventHandler.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("MSG", "Audio record permission granted");
            }
        } else if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("MSG", "Location permission granted");
            }
        }
    }

    public AppCompatActivity getActivity() {
        return this;
    }

    public void setServerAddress(final String address) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ipLabel.setText(address);
            }
        });
    }

    public void setSSID(final String ssid) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ssidLabel.setText(ssid);
            }
        });

    }

    public void setServerInfo(final String serverInfo) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverInfoLabel.setText(serverInfo);
            }
        });

    }

    @Override
    protected void onStart() {
        Log.d(Constants.LOG_TAG_MESSAGE, " activity onstart called");
        super.onStart();
        active = true;
        Intent mIntent = new Intent(getBaseContext(), NotificationEventHandler.class);
        bindService(mIntent, mConnection, 0);

        String intentExtra = getIntent().getStringExtra(Constants.CALL_NOTIFICATION_CLIENT);
        String deviceId=getIntent().getStringExtra("DEVICE_ID");
        if(deviceId!=null) {
            if (Constants.YES_RESPONSE.equals(intentExtra)) {
                Log.d(Constants.LOG_TAG_MESSAGE, "yes response intent received");
                sendNotifyResponse(true, deviceId);
                deviceManager.setCurrentDevice(deviceManager.getDevice(deviceId));
                buttonImageViewConnect.performClick();

            } else if (Constants.NO_RESPONSE.equals(intentExtra)) {
                Log.d(Constants.LOG_TAG_MESSAGE, "no response intent received");
                sendNotifyResponse(false, deviceId);
                finish();
            }
            Utils.removeNotification(Constants.DEVICE_REQUEST_NOTIFICATION_ID, getBaseContext());
            deviceManager.resetNotificationProcessing();
        }

        //registering for different local broadcast events
        LocalBroadcastManager.getInstance(this).registerReceiver(mActiveDeviceChangedReceiver,
                new IntentFilter(Constants.LOCAL_BC_EVENT_ACTIVE_DEVICE_CHANGED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mNotifyMessageReceiver,
                new IntentFilter(Constants.LOCAL_BC_EVENT_ACTIVE_DEVICE_CHANGED));

        //initial rendering
        updateDisplayState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        Log.d(Constants.LOG_TAG_MESSAGE, " activity onstop called");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mActiveDeviceChangedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotifyMessageReceiver);

        super.onStop();
        active = false;
        //de-registering activity from service
        if (getUdpBroadcastServer() != null) {
            getUdpBroadcastServer().setActivity(null);
        }
        unbindService(mConnection);
        mBounded = false;
        //if wifi is turned on do not stop service
    }

    public void updateDisplayState(){
        Device device=deviceManager.getCurrentDevice();
        if(device!=null){
            setAllButtonsState(true);
            setServerInfo(device.getDeviceId()+"-"+device.getDeviceIp()+":"+device.getDevicePort());
        }else{
            setAllButtonsState(false);
            setServerInfo("No locks connected.");
        }
        setSSID(deviceManager.getSSid());
        setServerAddress(deviceManager.getIP());
    }

    public synchronized void setAllButtonsState(final boolean enable) {
        Log.d(Constants.LOG_TAG_MESSAGE,"setAllButtonsState called: "+enable);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!enable) {
                    buttonImageViewLocker.setImageResource(R.drawable.lock_sleek_disabled);
                    buttonImageViewConnect.setImageResource(R.drawable.mic_grey);
                    buttonImageViewDisconnect.setImageResource(R.drawable.mic_grey_cross);
                    buttonImageViewHello.setImageResource(R.drawable.electronic_lock_grey);
                    buttonImageViewReset.setColorFilter(Color.rgb(83, 81, 81), PorterDuff.Mode.SRC_ATOP);
                } else {
                    if (imageButtonLockPressed) {
                        buttonImageViewLocker.setImageResource(R.drawable.lock_sleek_on);
                    } else {
                        buttonImageViewLocker.setImageResource(R.drawable.lock_sleek_red);
                    }
                    buttonImageViewConnect.setImageResource(R.drawable.mic_green);
                    buttonImageViewDisconnect.setImageResource(R.drawable.mic_red);
                    buttonImageViewHello.setImageResource(R.drawable.electronic_lock);
                    buttonImageViewReset.setColorFilter(Color.rgb(6, 178, 10), PorterDuff.Mode.SRC_ATOP);
                }
                buttonImageViewLocker.setEnabled(enable);
                buttonImageViewConnect.setEnabled(enable);
                buttonImageViewDisconnect.setEnabled(enable);
                buttonImageViewHello.setEnabled(enable);
                buttonImageViewReset.setEnabled(enable);

//                buttonConnect.setEnabled(enable);
//                buttonSayHello.setEnabled(enable);
//                buttonDiscon.setEnabled(enable);
//                buttonOpenDoor.setEnabled(enable);
//                buttonCloseDoor.setEnabled(enable);
//                buttonStartService.setEnabled(enable);
//                buttonStopService.setEnabled(enable);
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.LOG_TAG_MESSAGE, " activity oncreate called");

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_navigation_drawer);


        //get the image view,required for
        buttonImageViewLocker = (ImageView) findViewById(R.id.imageViewLocker);
        buttonImageViewConnect = (ImageView) findViewById(R.id.imageViewConnect);
        buttonImageViewDisconnect = (ImageView) findViewById(R.id.imageViewDisconnect);
        buttonImageViewHello = (ImageView) findViewById(R.id.imageViewHello);
        buttonImageViewReset = (ImageView) findViewById(R.id.imageViewReset);

        ssidLabel = (TextView) findViewById(R.id.ssidLabel);
        ipLabel = (TextView) findViewById(R.id.ipLabel);
        serverInfoLabel = (TextView) findViewById(R.id.serverInfoLabel);


        buttonImageViewConnect.setOnClickListener(buttonConnectOnClickListener);
        buttonImageViewDisconnect.setOnClickListener(buttonDisconnectOnCLickListener);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set the ontouch listener
        buttonImageViewLocker.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageView view = (ImageView) v;
                        //overlay is black with transparency of 0x77 (119)
                        if (!imageButtonLockPressed) {
                            buttonImageViewLocker.setImageResource(R.drawable.lock_sleek_red);
                            new Thread() {
                                @Override
                                public void run() {
                                    deviceManager.sendMessageRequest("CLOSE_DOOR");
                                }
                            }.start();
                            imageButtonLockPressed = true;
                        } else {
                            buttonImageViewLocker.setImageResource(R.drawable.lock_sleek_on);
                            new Thread() {
                                @Override
                                public void run() {
                                    deviceManager.sendMessageRequest("OPEN_DOOR");
                                }
                            }.start();

                            imageButtonLockPressed = false;
                        }
                        break;
                    }
                }

                return true;
            }
        });


        dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        userAccessGiven = true;
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        userAccessGiven = false;
                        break;
                }
                getAudioPermission();
            }
        };

        buttonImageViewHello.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                new Thread() {
                    @Override
                    public void run() {
                        deviceManager.sendMessageRequest("HELLO");
                    }
                }.start();
            }
        });
        buttonImageViewReset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                new Thread() {
                    @Override
                    public void run() {
                        deviceManager.sendMessageRequest("RESET");
                    }
                }.start();
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            getAudioPermission();
            getLocationPermission();
        }

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnectedOrConnecting()) {
            // Do whatever
            Log.d(Constants.LOG_TAG_MESSAGE, "wifi not connected show dialog");
            showWifiDialog();
        } else {
            Log.d(Constants.LOG_TAG_MESSAGE, "Wifi already enabled, send service");
            Intent intent = new Intent(Constants.CUSTOM_BROADCAST_INTENT);
            intent.setAction(Constants.CUSTOM_BROADCAST_INTENT);
            getBaseContext().sendBroadcast(intent);
        }
        //starting tcpip controller
        new Thread(){
            @Override
            public void run() {
                tcpipAudioController = TCPIPAudioController.getInstance();
                deviceManager=DeviceManager.getInstance();
                udpBroadcastServer =UDPBroadcastCommandProcessor.getInstance();
            }
        }.start();
    }

    private void showWifiDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                this);

        // Setting Dialog Title
        alertDialog.setTitle("Confirm...");

        // Setting Dialog Message
        alertDialog.setMessage("Do you want to go to wifi settings?");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.mipmap.ic_launcher);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        // Activity transfer to wifi settings
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event

                        dialog.cancel();
                        updateDisplayState();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }
    private void showOpenConversationDialog(final String deviceId) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                this);

        // Setting Dialog Title
        alertDialog.setTitle("Confirm...");

        // Setting Dialog Message
        alertDialog.setMessage("Do you want to start conversation?");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.mipmap.ic_launcher);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /*if (r.isPlaying()) {
                            r.stop();
                        }*/
                        sendNotifyResponse(true, deviceId);
                        buttonImageViewConnect.performClick();
                    }
                });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        /*if (r.isPlaying()) {
                            r.stop();
                        }*/
                        dialog.cancel();
                        //setAllButtonsState(false);
                        sendNotifyResponse(false, deviceId);
                        deviceManager.resetNotificationProcessing();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }


    OnClickListener buttonConnectOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            //apply audio settings
            //open tcp/ip socket from here
            //send message to open socket at the other end
            applyRequiredAudioSettings();
            if (tcpipAudioController.startCommunication(true, true)) {
                new Thread() {
                    @Override
                    public void run() {
                        deviceManager.sendMessageRequest(Constants.START_COMM);
                    }
                }.start();
            }
        }
    };


    OnClickListener buttonDisconnectOnCLickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //apply audio settings
            //stop communication at this end
            //ask other end to stop
            restoreAudioSettings();
            new Thread() {
                @Override
                public void run() {
                    deviceManager.sendMessageRequest(Constants.STOP_COMM);
                }
            }.start();
            tcpipAudioController.stopCommunication();
        }
    };

    public void restoreAudioSettings() {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(isSpeakerPhoneOn);
        audioManager.setMode(oldAudioMode);
        audioManager.setRingerMode(oldRingerMode);
    }

    public void applyRequiredAudioSettings() {

        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        oldAudioMode = audioManager.getMode();
        oldRingerMode = audioManager.getRingerMode();
        isSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
        //applying required settings
        audioManager.setSpeakerphoneOn(false);

    }

    public void doNotifyAction(final String deviceId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showOpenConversationDialog(deviceId);
            }
        });
    }


    public void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
    }

    public void getAudioPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (!userAccessGiven || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Click OK to provide Audio Record permission.").setPositiveButton("OK", dialogClickListener).show();
                //.setNegativeButton("No", dialogClickListener).show();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    public void sendNotifyResponse(boolean response, final String deviceId){
        CommandData commandData=new CommandData();
        final String notifyCommand=commandData.setCommand(Constants.NOTIFY)
                .setDeviceId(deviceManager.getCurrentDevice().getDeviceId())
                .setDeviceKey(deviceManager.getCurrentDevice().getDeviceKey())
                .setResponse(true)
                .setError(!response)
                .buildCommandString();

        new Thread(){
            @Override
            public void run() {
                deviceManager.sendMessageResponse(notifyCommand, deviceId);
            }
        }.start();
    }
}