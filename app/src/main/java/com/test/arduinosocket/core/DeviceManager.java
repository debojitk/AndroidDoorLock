package com.test.arduinosocket.core;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;

import com.test.arduinosocket.MyApplication;
import com.test.arduinosocket.R;
import com.test.arduinosocket.activity.AsyncListenActivity;
import com.test.arduinosocket.activity.CallNotificationActivity;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.network.UDPBroadcastCommandProcessor;
import com.test.arduinosocket.common.Utils;
import com.test.arduinosocket.network.WebSocketServerWrapper;

import org.java_websocket.WebSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Created by administrator on 4/21/2017.
 */

public class DeviceManager implements Runnable{
    private Map<String, Device> deviceMap=new HashMap<>();
    private Map<String, Device> tempPairingDeviceMap=new HashMap<>();
    private Map<String, ConnectingDevice> tempConnectingDeviceMap=new HashMap<>();
    private Device currentDevice;
    private UDPBroadcastCommandProcessor broadcastCommandProcessor;
    private WebSocketServerWrapper webSocketServer;
    private static DeviceManager deviceManagerSingleton;
    private boolean started=false;
    private String phoneId="MyPhone1";
    private String phoneKey="232423";
    private Context context;
    private Queue<CommandHolder> requestQueue=new ArrayDeque<CommandHolder>();
    private SharedPreferences storedDeviceMap;

    public static synchronized DeviceManager getInstance(){
        if(deviceManagerSingleton==null){
            deviceManagerSingleton=new DeviceManager();
        }

        return deviceManagerSingleton;
    }
    private DeviceManager(){
        this.broadcastCommandProcessor=UDPBroadcastCommandProcessor.getInstance();
        this.broadcastCommandProcessor.setDeviceManager(this);
        this.webSocketServer=WebSocketServerWrapper.getInstance();
        this.webSocketServer.setDeviceManager(this);
    }

    public Device getDevice(String deviceId){
        Device device=null;
        device=deviceMap.get(deviceId);
        return device;
    }
    public Device getDevice(InetAddress ip){
        Device device=null;
        for(Device device1:deviceMap.values()){
            if(device1.getDeviceIp().equals(ip)){
                device=device1;
                break;
            }
        }
        return device;
    }
    public Map<String, Device> getTempPairingDeviceMap(){
        return tempPairingDeviceMap;
    }

    public void persistNewlyAddedDevice(Device device){
        storedDeviceMap = this.context.getSharedPreferences(
                context.getString(R.string.com_app_wifilock_paired_device_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = storedDeviceMap.edit();
        editor.putString(device.getDeviceId(), Utils.serialize(device));
        editor.commit();
    }

    public boolean isAlreadyPaired(Device device){
        boolean retVal=false;
        storedDeviceMap = this.context.getSharedPreferences(
                context.getString(R.string.com_app_wifilock_paired_device_info), Context.MODE_PRIVATE);
        if(storedDeviceMap.getString(device.getDeviceId(), null)==null){
            retVal=false;
        }else{
            retVal=true;
        }
        return retVal;
    }
    public Map<String,Device> getAllPairedDevices(){
        Map<String,?> devicesList=new HashMap<>();
        Map<String, Device> retVal=new HashMap<>();
        storedDeviceMap = this.context.getSharedPreferences(
                context.getString(R.string.com_app_wifilock_paired_device_info), Context.MODE_PRIVATE);
        devicesList=storedDeviceMap.getAll();
        for(Map.Entry<String, ?> deviceEntry:devicesList.entrySet()){
            String deviceData=deviceEntry.getValue().toString();
            Device device=Utils.deserialize(deviceData, Device.class);
            retVal.put(deviceEntry.getKey(), device);
        }
        return retVal;
    }

    public void removePairedDevice(Device device){
        storedDeviceMap = this.context.getSharedPreferences(
                context.getString(R.string.com_app_wifilock_paired_device_info), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=storedDeviceMap.edit();
        editor.remove(device.getDeviceId());
        editor.commit();
    }

    public Device getPairedDevice(String deviceId){
        Device retVal=null;
        storedDeviceMap = this.context.getSharedPreferences(
                context.getString(R.string.com_app_wifilock_paired_device_info), Context.MODE_PRIVATE);
        String deviceString=storedDeviceMap.getString(deviceId, null);
        if(deviceString!=null){
            retVal=Utils.deserialize(deviceString, Device.class);
        }
        return retVal;
    }

    public void start(Context context){
        if(!started) {
            this.context = context;
            Thread localThread = new Thread(this);
            try {
                //WebSocketImpl.DEBUG=true;
                this.webSocketServer.start();
            } catch (Exception ex) {
                Log.e(Constants.LOG_TAG_SERVICE, "An error occurred while starting websocket server", ex);
            }
            localThread.start();
            startBroadcastProcessor(context);

            started = true;
        }else{
            Log.d(Constants.LOG_TAG_SERVICE, "DeviceManager services already started!");
        }
    }
    public void stop(){
        this.started=false;
        try {
            this.webSocketServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.broadcastCommandProcessor.stop();
    }
    public void startBroadcastProcessor(Context context){
        if(!broadcastCommandProcessor.isRunning()){
            broadcastCommandProcessor.setRunning(true);
            broadcastCommandProcessor.setWifiTurnedOn(true);
            broadcastCommandProcessor.setContext(context);
            Thread t=new Thread(broadcastCommandProcessor);
            t.start();
            Utils.showMessage("Service started");
        }else{
            Utils.showMessage("Service already started");
        }

    }
    public void run(){

    }

    public void addDevice(String deviceId, String deviceKey, InetAddress deviceIp, int remotePort){
        //String key=deviceId+"-"+deviceIp;
        String key=deviceId;
        if(deviceMap.get(key)==null) {
            Device device = new Device(deviceId, deviceKey, deviceIp, remotePort);
            deviceMap.put(key, device);
            if(currentDevice == null) {
                setCurrentDevice(device);
            }
        }
    }

    public void addDevice(Device device){
        //String key=device.getDeviceId()+"-"+device.getDeviceIp();
        String key=device.getDeviceId();
        if(deviceMap.get(key)==null) {
            deviceMap.put(key, device);
            if(currentDevice == null) {
                setCurrentDevice(device);
            }
        }
    }

    public Device removeDevice(Device device){
        //String key=device.getDeviceId()+"-"+device.getDeviceIp();
        if(device!=null) {
            String key = device.getDeviceId();
            device = deviceMap.remove(key);
            if(device.getWebSocketConnection()!=null && !device.getWebSocketConnection().isClosed()){
                device.getWebSocketConnection().close();
            }
            if (deviceMap.isEmpty()) {
                setCurrentDevice(null);
            } else {
                if (device!=null && getCurrentDevice().equals(device)) {
                    //current device is being removed, select the first connected device as current device
                    for (Map.Entry<String, Device> entry : deviceMap.entrySet()) {
                        setCurrentDevice(entry.getValue());
                        break;
                    }
                }
            }
        }
        return device;
    }

    public void removeAllDevices(){
        for (Map.Entry<String, Device> entry : deviceMap.entrySet()) {
            Device device=entry.getValue();
            if(device.getWebSocketConnection()!=null && !device.getWebSocketConnection().isClosed()){
                try {
                    device.getWebSocketConnection().close();
                }catch(Exception ex){
                    //ignore
                }
            }
        }
        deviceMap.clear();
        setCurrentDevice(null);
    }
    public String getSSid() {
        String retVal = "";
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnectedOrConnecting()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            retVal = info.getSSID();
        }
        return retVal;
    }

    public String getIP() {
        String retVal = "";
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnectedOrConnecting()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            retVal = Utils.getIPAddress(true);
        }
        return retVal;
    }

    public int getLiveDeviceCount(){
        return deviceMap.size();
    }

    public void setCurrentDevice(Device device){
        this.currentDevice=device;
        Intent intent = new Intent(Constants.LOCAL_BC_EVENT_ACTIVE_DEVICE_CHANGED);
        intent.putExtra(Constants.INTENT_DEVICE_ID, device!=null?device.getDeviceId():null);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);

    }
    public Device getCurrentDevice(){
        return this.currentDevice;
    }

    public UDPBroadcastCommandProcessor getBroadcastCommandProcessor() {
        broadcastCommandProcessor=UDPBroadcastCommandProcessor.getInstance();
        return broadcastCommandProcessor;
    }

    public void setBroadcastCommandProcessor(UDPBroadcastCommandProcessor broadcastCommandProcessor) {
        this.broadcastCommandProcessor = broadcastCommandProcessor;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public String getPhoneKey() {
        return phoneKey;
    }

    public void setPhoneKey(String phoneKey) {
        this.phoneKey = phoneKey;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
    public boolean linkDeviceWithWebSocketConnection(WebSocket webSocket, Device device){
        boolean retVal=false;
        if(webSocket!=null) {
            ConnectingDevice connDevice=tempConnectingDeviceMap.get(device.getDeviceId());
            if (connDevice!=null){
                connDevice.completeConnection();
                connDevice.getDevice().setWebSocketConnection(webSocket);
                retVal= true;
            }else{
                connDevice.rejectConnection();
                retVal= false;
            }
        }
        return retVal;
    }

    public void addToConnectingDeviceList(Device device){
        if(!tempConnectingDeviceMap.containsKey(device.getDeviceId())) {
            ConnectingDevice connectingDevice = new ConnectingDevice(device);
            connectingDevice.startTimeoutThread(60000);
            tempConnectingDeviceMap.put(device.getDeviceId(), connectingDevice);
        }
    }

    private  class ConnectingDevice implements Runnable{
        private Device device;
        private Thread thread;
        private boolean connected=false;
        private long timeoutInMillis=5000;
        public ConnectingDevice(Device device){
            this.device=device;
            this.thread=new Thread(this);
        }

        public void startTimeoutThread(){
            startTimeoutThread(timeoutInMillis);
        }
        public void startTimeoutThread(long timeoutInMillis){
            this.timeoutInMillis=timeoutInMillis;
            this.thread.start();
        }

        public Device getDevice() {
            return device;
        }

        public void setDevice(Device device) {
            this.device = device;
        }

        public Thread getThread() {
            return thread;
        }

        public void setThread(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(timeoutInMillis);
            } catch (InterruptedException e) {
                //e.printStackTrace();
                Log.e(Constants.LOG_TAG_SERVICE, "timeout thread interrupted");
            }
            tempConnectingDeviceMap.remove(device.getDeviceId());
            if(connected){
                addDevice(this.getDevice());
                Log.i(Constants.LOG_TAG_SERVICE, "device gets added to the devicelist: "+device.getDeviceId());
            }else{
                Log.i(Constants.LOG_TAG_SERVICE, "no connection occurred with due time: "+timeoutInMillis +" msec");
            }
        }
        public void completeConnection(){
            connected=true;
            this.thread.interrupt();
        }
        public void rejectConnection(){
            connected=false;
            this.thread.interrupt();
        }

    }

    public void delegateCommand(WebSocket webSocket, String message){
        CommandData commandData=new CommandData(message);
        if(webSocket!=null) {
            boolean found=false;
            Device matchDevice=null;
            InetAddress remoteAddress = webSocket.getRemoteSocketAddress().getAddress();
            Device device=getDevice(commandData.getDeviceId());
            if(device!=null){
                if(!device.getWebSocketConnection().equals(webSocket)){
                    //renewing the websocket of the device
                    device.setWebSocketConnection(webSocket);
                }
                matchDevice=device;
            }
            if(matchDevice!=null){
                int size=requestQueue.size();
                int i=0;
                if(commandData.isResponse()) {
                    while (true) {
                        CommandHolder commandHolder = requestQueue.remove();
                        if (commandHolder.getCommand().equalsIgnoreCase(commandData.getCommand())
                                && commandHolder.getDevice().equals(matchDevice)) {
                            try {
                                if (commandHolder.getResponseHandler() != null) {
                                    commandHolder.getResponseHandler().handleResponse(commandData, matchDevice);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        } else {
                            requestQueue.add(commandHolder);
                        }
                        i++;
                        if (i > size) {
                            break;
                        }
                    }
                }else{
                    handleRequest(webSocket, commandData);
                }

            }
        }

    }

    private boolean notificationInProgress=false;
    public void handleRequest(WebSocket socketConnection, CommandData commandData){
        if(Constants.NOTIFY.equals(commandData.getCommand())){
            if(!notificationInProgress) {
                notificationInProgress = true;
                if(isScreenOn() && isScreenUnlocked()){
                    Activity activity = MyApplication.getCurrentActivity();
                    if (activity != null && activity instanceof AsyncListenActivity) {
                        AsyncListenActivity asyncListenActivity = (AsyncListenActivity) activity;
                        asyncListenActivity.doNotifyAction(commandData.getDeviceId());
                    } else {
                        Utils.removeNotification(Constants.DEVICE_REQUEST_NOTIFICATION_ID, getContext());
                        Utils.createNotificationWithYesNo("Door bell ringing - "+commandData.getDeviceId(), getContext(), true, commandData.getDeviceId());
                    }

                }else {
                    Intent i = new Intent(this.context, CallNotificationActivity.class);
                    i.putExtra(Constants.CALL_NOTIFICATION_CLIENT, commandData.buildCommandString());
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.context.startActivity(i);
                }
            }
        }else if(Constants.NOTIFY_COMPLETE.equals(commandData.getCommand())){
            if(notificationInProgress){
                resetNotificationProcessingOnMessage(commandData);
            }
        }
    }

    public void resetNotificationProcessingOnMessage(CommandData commandData){
        this.notificationInProgress=false;
        Log.d(Constants.LOG_TAG_MESSAGE, "Broadcasting message");
        Intent intent = new Intent(Constants.LOCAL_BC_EVENT_NOTIFICATION);
        intent.putExtra(Constants.CALL_NOTIFICATION_CLIENT, commandData.buildCommandString());
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
        //if any notification is running remove that notification
        Utils.removeNotification(Constants.DEVICE_REQUEST_NOTIFICATION_ID, this.context);

    }
    public void resetNotificationProcessing(){
        this.notificationInProgress=false;
    }

    public void sendMessageRequest(String command, String message, CommandResponseHandler responseHandler){
        try{
            requestQueue.add(new CommandHolder(command, message, responseHandler, getCurrentDevice()));
            String inputMessage=command+ Constants.COLON+getPhoneId()+Constants.COLON+getPhoneKey();
            if(message!=null && message.length()>0){
                inputMessage+=Constants.COLON+message;
            }
            getCurrentDevice().getWebSocketConnection().send(inputMessage);
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }
    public void sendMessageResponse(final String message, final String callingDeviceId){
        new Thread(){
            @Override
            public void run() {
                Device callingDevice=getDevice(callingDeviceId);
                if(callingDevice!=null && callingDevice.getWebSocketConnection()!=null && !callingDevice.getWebSocketConnection().isClosed()){
                    callingDevice.getWebSocketConnection().send(message);
                }else {
                    getCurrentDevice().getWebSocketConnection().send(message);
                }
            }
        }.start();
    }

    public void sendMessageRequest(String command){
        sendMessageRequest(command, null, null);
    }
    public boolean checkPairedDevice(String deviceId, String deviceKey){
        Device device = getPairedDevice(deviceId);
        if(device!=null && deviceKey.equals(device.getDeviceKey())){
            return true;
        }else{
            return false;
        }
    }

    public boolean isScreenOn(){
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        for (Display display : dm.getDisplays()) {
            if (display.getState() != Display.STATE_OFF) {
                return true;
            }
        }
        return false;
    }

    public boolean isScreenUnlocked(){
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if( myKM.inKeyguardRestrictedInputMode()) {
            return false;
        } else {
            return true;
        }
    }


    private class CommandHolder{
        private String command;
        private String message;
        private CommandResponseHandler responseHandler;
        private Device device;

        public CommandHolder(String command, String message, CommandResponseHandler responseHandler, Device device){
            this.command=command;
            this.message=message;
            this.responseHandler=responseHandler;
            this.device=device;
        }
        public CommandHolder(String command){
            this(command, null, null, null);
        }

        public CommandResponseHandler getResponseHandler() {
            return responseHandler;
        }

        public void setResponseHandler(CommandResponseHandler responseHandler) {
            this.responseHandler = responseHandler;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public Device getDevice() {
            return device;
        }

        public void setDevice(Device device) {
            this.device = device;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof CommandHolder){
                CommandHolder that=(CommandHolder)obj;
                if(this.command.equalsIgnoreCase(that.command) && this.device.equals(that)){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }
    }

}
