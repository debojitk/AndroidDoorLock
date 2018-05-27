package com.test.arduinosocket.network;

/**
 * Created by debojitk on 11/08/2016.
 */

import android.app.Activity;
import android.content.Context;

import android.net.wifi.WifiManager;
import android.util.Log;

import com.test.arduinosocket.activity.AsyncListenActivity;
import com.test.arduinosocket.activity.LockManagementActivity;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.MyApplication;
import com.test.arduinosocket.common.Utils;
import com.test.arduinosocket.core.CommandData;
import com.test.arduinosocket.core.Device;
import com.test.arduinosocket.core.DeviceManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class UDPBroadcastCommandProcessor implements Runnable {

    private DatagramSocket socket;
    private DatagramPacket packet;
    private InetSocketAddress remoteSocket;
    private Activity activity;
    private String myIp;
    private byte[] recvBuf = new byte[1024];
    private boolean wifiTurnedOn = true;
    private boolean running = false;
    private Context context;
    private DeviceManager deviceManager;
    private String sessionId;
    private static UDPBroadcastCommandProcessor server;
    private Thread startedThread;

    public static synchronized UDPBroadcastCommandProcessor getInstance() {
        if (server == null) {
            server = new UDPBroadcastCommandProcessor();
        }
        return server;
    }

    public void setDeviceManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    private UDPBroadcastCommandProcessor() {
        init();
    }
    private void init(){
        try {
            myIp = Utils.getIPAddress(true);
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            socket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 8888));
            socket.setSoTimeout(2000);
            sessionId=String.valueOf(System.currentTimeMillis());
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, ex.getMessage());
        }
    }
    public synchronized void startService(){
        if(startedThread==null || startedThread.getState()!=Thread.State.RUNNABLE) {
            init();
            running=true;
            wifiTurnedOn=true;
            startedThread = new Thread(this);
            startedThread.start();
        }
    }
    public void dispose() {
        //disableSocketTimeout();
        Log.d(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "Disposing me-" + startedThread);
        this.setWifiTurnedOn(false);
        this.setRunning(false);
        while(startedThread!=null && startedThread.getState()!= Thread.State.TERMINATED){
            try {
                this.socket.close();
                Log.d(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "socket is " + socket + " Closed=" + socket.isClosed() + " Connected=" + socket.isConnected());
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(this.socket!=null){
            try{
                this.socket.close();
            }catch(Exception ex){
                Log.e(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "An error occured while closing socket", ex);
            }
        }
        //this.socket=null;
        //server = null;
    }


    public void setWifiTurnedOn(boolean wifiTurnedOn) {
        this.wifiTurnedOn = wifiTurnedOn;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    public void setActivity(AsyncListenActivity activity) {
        this.activity = activity;
    }


    public void enableSocketTimeout() {
        if (socket != null) {
            try {
                socket.setSoTimeout(3000);
            } catch (SocketException e) {
                Log.e(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "An error occurred");
            }
        }
    }

    public void disableSocketTimeout() {
        if (socket != null) {
            try {
                socket.setSoTimeout(0);
            } catch (SocketException e) {
                Log.e(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "An error occurred");
            }
        }

    }


    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public void searchActiveDevices(long timeout) throws UnknownHostException{
        if(wifiTurnedOn) {
            long startTime = System.currentTimeMillis();
            boolean connected = false;
            String msg = "Searching for device..";
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if(wifiManager==null){
                return;
            }
            WifiManager.WifiLock wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, Constants.LOG_TAG_SERVICE);
            wifiLock.acquire();
            Log.d(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "Acquiring wifi_full lock");
            while ((System.currentTimeMillis() <= startTime + timeout) && running) {
                //1. broadcasting presence
                byte[] sendData = (new CommandData().setCommand(Constants.UDP_CONN_BC_REQUEST_PHONE)
                        .setDeviceId(this.deviceManager.getPhoneId())
                        .setDeviceKey(this.deviceManager.getPhoneKey())
                        .setData(sessionId)
                        .buildCommandString()).getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName("255.255.255.255"), 6666);
                try {
                    socket.send(sendPacket);
                } catch (Exception ex) {
                    Utils.showMessage("Please turn on Wifi");
                    //ex.printStackTrace();
                }
                msg += ".";
                while (System.currentTimeMillis() <= startTime + timeout && running) {
                    Utils.showMessage(msg);
                    try {
                        byte[] recvBuf = new byte[1024];
                        packet = new DatagramPacket(recvBuf, recvBuf.length);
                        socket.receive(packet);
                        processReceivedMessage(packet);
                    } catch (SocketTimeoutException ex) {
                        Log.e(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "SocketTimeoutException occurred for socket is " +
                                socket + " Closed=" + socket.isClosed() + " Connected=" + socket.isConnected());
                        break;
                    } catch (Exception ex) {
                        Log.e(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "SocketException occurred for socket is " +
                                socket + " Closed=" + socket.isClosed() + " Connected=" + socket.isConnected(), ex);
                        running = false;
                        break;
                    }
                }
            }
            Log.d(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "Releasing wifi_full lock");
            wifiLock.release();
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            if (!running) {
                Log.d(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "Thread is not in running state");
                return;
            }
            if (wifiTurnedOn) {
                searchActiveDevices(15000);
            } else {
                if (!wifiTurnedOn) {
                    Log.d(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "wifi not turned on");
                }
                if (!running) {
                    Log.d(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "Thread is not in running state");
                }
            }
            if(socket!=null && !socket.isClosed()) {
                socket.setSoTimeout(0);//disabling timeout
            }
            if(deviceManager.getLiveDeviceCount()==0) {
                Utils.showMessage("No Responding device found, when it would be online connection will be automatically made!");
            }else{
                Utils.showMessage("Live device count: " + deviceManager.getLiveDeviceCount());
            }
            //receiving broadcast here
            while (wifiTurnedOn && running) {
                try {
                    //Receive a packet
                    recvBuf = new byte[1024];
                    packet = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(packet);
                    //TODO if reset is called it should not enable buttons, they would get automatically enabled
                    //it should process connect, pair, and notify
                    processReceivedMessage(packet);
                } catch (SocketTimeoutException ex) {
                    Log.e(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "SocketTimeoutException occurred for socket is " + socket);
                } catch (IllegalArgumentException ex) {
                    Log.e(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "An error occured but recovered: ", ex);
                    Utils.showMessage("Please stop and start wifi connection");
                }catch(Exception ex){
                    Log.e(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "SocketTimeoutException occurred for socket is " + socket, ex);
                    break;
                }
            }
            running = false;
        } catch (IOException ex) {
            Log.e(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "An error occurred, exiting thread", ex);
        } finally {
            Log.d(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "exiting thread");
        }
    }

    public void processReceivedMessage(DatagramPacket packet) {
        try {
            //See if the packet holds the right command (message)
            String responseMessage = null;
            String requestMessage = new String(packet.getData()).trim();
            Log.d(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "message is: " + requestMessage);
            CommandData commandData = new CommandData(requestMessage);
            if(commandData.getCommand()==null) return;
            //Utils.showMessage("Received message:" + requestMessage);
            //setRemoteSocket(new InetSocketAddress (((InetSocketAddress)packet.getSocketAddress()).getAddress(),((InetSocketAddress)packet.getSocketAddress()).getPort()));
            /**
             * Connection handling
             * 1. If device sends UDP_CONN_BC_REQUEST_DEVICE
             *      then its a device initiated request, and connection established in two step handshaking
             *      phone sends UDP_CONN_BC_RESPONSE_DEVICE
             *      device starts web socket connection
             *      phone receives new web socket connection request
             *      Connection gets established.
             * 2. If phone sends UDP_CONN_BC_REQUEST_PHONE
             *      then its a phone initiated request, and connection established in three step handshaking
             *      device sends UDP_CONN_BC_RESPONSE_PHONE
             *      phone responds back with UDP_CONN_BC_RESPONSE_PHONE:ACK
             *      device starts web socket connection
             *      phone receives new web socket connection request
             *      Connection gets established.
             */
            if (commandData.getCommand().equals(Constants.UDP_CONN_BC_REQUEST_DEVICE)) {
                //message initiated from device
                if (this.deviceManager.checkPairedDevice(commandData.getDeviceId(), commandData.getDeviceKey())
                        && (this.deviceManager.getDevice(commandData.getDeviceId())==null)
                        && this.deviceManager.getConnectingDevice(commandData.getDeviceId())==null){
                    //device is paired, so it can be connected, send the success response
                    Utils.showMessage("Incoming Request from this IP: " + myIp);
                    responseMessage = new CommandData().setCommand(Constants.UDP_CONN_BC_RESPONSE_DEVICE)
                            .setDeviceId(this.deviceManager.getPhoneId())
                            .setDeviceKey(this.deviceManager.getPhoneKey())
                            .setData(sessionId)
                            .buildCommandString();

                    //add a new device
                    this.deviceManager.addToConnectingDeviceList(new Device(commandData.getDeviceId(), commandData.getDeviceKey(),
                            packet.getAddress(), packet.getPort(), commandData.getDeviceType()));
                }
            } else if (commandData.getCommand().equals(Constants.UDP_CONN_BC_RESPONSE_PHONE)) {
                //message initiated from phone
                Utils.showMessage("Connected to " + remoteSocket);
                if (this.deviceManager.checkPairedDevice(commandData.getDeviceId(), commandData.getDeviceKey())) {
                    //add a new device if already  not added
                    responseMessage = new CommandData().setCommand(Constants.UDP_CONN_BC_RESPONSE_PHONE)
                            .setDeviceId(this.deviceManager.getPhoneId())
                            .setDeviceKey(this.deviceManager.getPhoneKey())
                            .setData(sessionId)
                            .setResponse(true)
                            .buildCommandString();

                    //add a new device
                    this.deviceManager.addToConnectingDeviceList(new Device(commandData.getDeviceId(),
                            commandData.getDeviceKey(), packet.getAddress(), packet.getPort(), commandData.getDeviceType()));
                } else {
                    responseMessage = new CommandData().setCommand(Constants.UDP_CONN_BC_RESPONSE_PHONE)
                            .setDeviceId(this.deviceManager.getPhoneId())
                            .setDeviceKey(this.deviceManager.getPhoneKey())
                            .setResponse(true)
                            .setError(true)
                            .setData(commandData.getDeviceId() + " is not paired")
                            .buildCommandString();
                }
            } else if (commandData.getCommand().startsWith(Constants.UDP_PAIR_BROADCAST)) {
                //response=UDP_PAIR_BROADCAST:ACK:<pairingId>:<phoneId>:<phoneKey>
                processPairing(packet, commandData);
                //add a new device if already  not added
            } else {
                responseMessage = "UNKNOWN";
            }
            //Send a response
            if (responseMessage != null) {
                DatagramPacket sendPacket = new DatagramPacket(responseMessage.getBytes(), responseMessage.length(), packet.getAddress(), packet.getPort());
                socket.send(sendPacket);
                System.out.println("Sent packet to: " + sendPacket.getAddress().getHostAddress() + ":" + sendPacket.getPort());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void processPairing(DatagramPacket packet, CommandData commandData) {
        /**
         * 1. Request from device: UDP_PAIR_BROADCAST:deviceid:devicekeyChallenge:deviceType
         * 2. Response from phone: UDP_PAIR_BROADCAST:phoneid:phonekey:ACK:devicekeyChallenge
         * 3. if device accepts it it sends:
         *      UDP_PAIR_BROADCAST_ACCEPT:deviceid:devicekey:deviceType
         *      In response phone sends
         *      UDP_PAIR_BROADCAST_ACCEPT:phoneid:phonekey:ACK:devicekey
         * 4. if device rejects it sends
         *      UDP_PAIR_BROADCAST_REJECT:deviceid
         */
        if (MyApplication.getCurrentActivity() instanceof LockManagementActivity) {
            String responseMessage = null;
            try {
                final LockManagementActivity lockManagementActivity = (LockManagementActivity) MyApplication.getCurrentActivity();
                if (commandData.getCommand().equalsIgnoreCase(Constants.UDP_PAIR_BROADCAST)) {
                    if (deviceManager.getPairedDevice(commandData.getDeviceId()) == null
                            && deviceManager.getTempPairingDeviceMap().get(commandData.getDeviceId()) == null) {
                        //check for devices those are not yet paired
                        InetAddress remoteIp = ((InetSocketAddress) packet.getSocketAddress()).getAddress();
                        int remotePort = ((InetSocketAddress) packet.getSocketAddress()).getPort();

                        final Device tempPairingDevice = new Device(commandData.getDeviceId(), commandData.getDeviceKey(), remoteIp, remotePort, commandData.getDeviceType());//here the key is the challenge key
                        tempPairingDevice.setDeviceKey(commandData.getDeviceKey());
                        deviceManager.getTempPairingDeviceMap().put(tempPairingDevice.getDeviceId(), tempPairingDevice);
                        lockManagementActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lockManagementActivity.addDeviceToAvailableList(tempPairingDevice);
                            }
                        });
                    } else {
                        Utils.showMessage(commandData.getDeviceId() + " is already paired or listed as available device. Please remove and then re-pair.");
                    }
                } else if (commandData.getCommand().equalsIgnoreCase(Constants.UDP_PAIR_BROADCAST_ACCEPT)) {
                    if (deviceManager.getTempPairingDeviceMap().get(commandData.getDeviceId()) != null) {
                        //pairing was in progress, it is time to accept
                        InetAddress remoteIp = ((InetSocketAddress) packet.getSocketAddress()).getAddress();
                        //remove from temp list
                        final Device tempPairingDevice = deviceManager.getTempPairingDeviceMap().remove(commandData.getDeviceId());
                        tempPairingDevice.setDeviceKey(commandData.getDeviceKey());
                        deviceManager.persistNewlyAddedDevice(tempPairingDevice);
                        this.deviceManager.addToConnectingDeviceList(new Device(commandData.getDeviceId(),
                                commandData.getDeviceKey(), packet.getAddress(), packet.getPort(), commandData.getDeviceType()));
                        //update views
                        lockManagementActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lockManagementActivity.removeDeviceFromAvailableList(tempPairingDevice);
                                lockManagementActivity.addDeviceToPairedList(tempPairingDevice);
                            }
                        });
                        //sending acceptance response
                        responseMessage = new CommandData().setCommand(Constants.UDP_PAIR_BROADCAST_ACCEPT)
                                .setDeviceId(deviceManager.getPhoneId())
                                .setDeviceKey(deviceManager.getPhoneKey())
                                .setResponse(true)
                                .setError(false)
                                .setData(commandData.getDeviceKey())
                                .buildCommandString();
                        if (responseMessage != null) {
                            DatagramPacket sendPacket = new DatagramPacket(responseMessage.getBytes(), responseMessage.length(), packet.getAddress(), packet.getPort());
                            socket.send(sendPacket);
                            System.out.println("Sent packet to: " + sendPacket.getAddress().getHostAddress() + ":" + sendPacket.getPort());
                        }

                        Utils.showMessage("New device paired successfully...");

                    }
                } else if (commandData.getCommand().equalsIgnoreCase(Constants.UDP_PAIR_BROADCAST_REJECT)) {
                    Utils.showMessage("Pairing failed for device: " + commandData.getDeviceId() + ". Error is: " + commandData.getData());
                    InetAddress remoteIp = ((InetSocketAddress) packet.getSocketAddress()).getAddress();
                    //remove from temp list
                    final Device tempPairingDevice = deviceManager.getTempPairingDeviceMap().remove(commandData.getDeviceId());
                    lockManagementActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lockManagementActivity.removeDeviceFromAvailableList(tempPairingDevice);
                        }
                    });
                }
            } catch (Exception ex) {
                //ignore
            }

        }
        Log.d(Constants.LOG_TAG_SERVICE_UDP_PROCESOR, "processPairing() completed.");
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public synchronized InetSocketAddress getRemoteSocketAddress() {
        return remoteSocket;
    }

    public boolean sendMessage(String message, Device recipientDevice) {
        if (!isRunning()) return false;
        boolean retVal = false;
        try {
            DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), recipientDevice.getDeviceIp(), Constants.UDP_REMOTE_PORT);
            socket.send(sendPacket);
            retVal = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retVal;
    }


}