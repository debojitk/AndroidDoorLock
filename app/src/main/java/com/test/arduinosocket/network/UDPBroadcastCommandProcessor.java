package com.test.arduinosocket.network;

/**
 * Created by debojitk on 11/08/2016.
 */

import android.app.Activity;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.test.arduinosocket.activity.AsyncListenActivity;
import com.test.arduinosocket.activity.LockManagementActivity;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.MyApplication;
import com.test.arduinosocket.activity.RecorderActivity;
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

    private static UDPBroadcastCommandProcessor server;

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
        try {
            myIp = Utils.getIPAddress(true);
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            socket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 8888));
            socket.setSoTimeout(2000);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(Constants.LOG_TAG_MESSAGE, ex.getMessage());
        }

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
                Log.e("MSG", "An error occurred");
            }
        }
    }

    public void disableSocketTimeout() {
        if (socket != null) {
            try {
                socket.setSoTimeout(0);
            } catch (SocketException e) {
                Log.e("MSG", "An error occurred");
            }
        }

    }


    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }


    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            long timeout = 15000;
            boolean connected = false;
            int retryCount = 3;
            String msg = "Searching for device..";
            if (!running) {
                Log.d(Constants.LOG_TAG_MESSAGE, "Thread is not in running state");
                return;
            }
            long startTime = System.currentTimeMillis();
            if (wifiTurnedOn) {
                while ((System.currentTimeMillis() <= startTime + timeout) && running) {
                    //1. broadcasting presence
                    byte[] sendData = (new CommandData().setCommand(Constants.UDP_CONNECT_BC_REQUEST)
                            .setDeviceId(this.deviceManager.getPhoneId())
                            .setDeviceKey(this.deviceManager.getPhoneKey()).buildCommandString()).getBytes();

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
                            Log.d("MSG", "DEBUG socket disconnected");
                            Log.e("MSG", "socket disconnected");
                            break;
                        }
                    }
                }
            } else {
                if (!wifiTurnedOn) {
                    Log.d(Constants.LOG_TAG_MESSAGE, "wifi not turned on");
                }
                if (!running) {
                    Log.d(Constants.LOG_TAG_MESSAGE, "Thread is not in running state");
                }
            }
            socket.setSoTimeout(50000);//disabling timeout
            if (!connected) {
                Utils.showMessage("No Responding device found, when it would be online connection will be automatically made!");
                activity = MyApplication.getCurrentActivity();

                if (activity != null && activity instanceof AsyncListenActivity) {
                    AsyncListenActivity asyncListenActivity = (AsyncListenActivity) activity;
                    Log.d(Constants.LOG_TAG_MESSAGE, "Service not connected, so setting remotesocket as null");
                }
            }
            //receiving broadcast here
            while (wifiTurnedOn && running) {
                try {
                    System.out.print(">");

                    //Receive a packet
                    recvBuf = new byte[1024];
                    packet = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(packet);
                    //TODO if reset is called it should not enable buttons, they would get automatically enabled
                    //it should process connect, pair, and notify
                    processReceivedMessage(packet);
                } catch (SocketTimeoutException ex) {
                    Log.e(Constants.LOG_TAG_SERVICE, "socket timed out");
                } catch (IllegalArgumentException ex) {
                    Log.e(Constants.LOG_TAG_MESSAGE, "An error occured but recovered: ", ex);
                    Utils.showMessage("Please stop and start wifi connection");
                }
            }
            running = false;
        } catch (IOException ex) {
            Log.e(Constants.LOG_TAG_MESSAGE, "An error occurred, exiting thread", ex);
        } finally {
            Log.d(Constants.LOG_TAG_MESSAGE, "exiting thread");
        }
    }

    public void processReceivedMessage(DatagramPacket packet) {
        try {
            //See if the packet holds the right command (message)
            String responseMessage = null;
            String requestMessage = new String(packet.getData()).trim();
            CommandData commandData = new CommandData(requestMessage);
            Log.d("MSG", "message is: " + requestMessage);
            Utils.showMessage("Received message:" + requestMessage);
            //setRemoteSocket(new InetSocketAddress (((InetSocketAddress)packet.getSocketAddress()).getAddress(),((InetSocketAddress)packet.getSocketAddress()).getPort()));
            /**
             * Connection handling
             * 1. If device sends UDP_CONN_BC_REQUEST
             *      then its a device initiated request, and connection established in two step handshaking
             *      phone sends UDP_CONN_BC_RESPONSE
             *      device starts web socket connection
             *      phone receives new web socket connection request
             *      Connection gets established.
             * 2. If phone sends UDP_CONN_BC_REQUEST
             *      then its a phone initiated request, and connection established in three step handshaking
             *      device sends UDP_CONN_BC_RESPONSE
             *      phone responds back with UDP_CONN_BC_RESPONSE:ACK
             *      device starts web socket connection
             *      phone receives new web socket connection request
             *      Connection gets established.
             */
            if (commandData.getCommand().equals(Constants.UDP_CONNECT_BC_REQUEST)) {
                //message initiated from device
                if (this.deviceManager.checkPairedDevice(commandData.getDeviceId(), commandData.getDeviceKey())) {
                    //device is paired, so it can be connected, send the success response
                    Utils.showMessage("Incoming Request. Connected to " + remoteSocket + " from this IP: " + myIp);
                    responseMessage = new CommandData().setCommand(Constants.UDP_CONNECT_BC_RESPONSE)
                            .setDeviceId(this.deviceManager.getPhoneId())
                            .setDeviceKey(this.deviceManager.getPhoneKey()).buildCommandString();

                    //add a new device
                    this.deviceManager.addToConnectingDeviceList(new Device(commandData.getDeviceId(), commandData.getDeviceKey(), packet.getAddress(), packet.getPort()));
                }
            } else if (commandData.getCommand().equals(Constants.UDP_CONNECT_BC_RESPONSE)) {
                //message initiated from phone
                Utils.showMessage("Connected to " + remoteSocket);
                if (this.deviceManager.checkPairedDevice(commandData.getDeviceId(), commandData.getDeviceKey())) {
                    //add a new device if already  not added
                    responseMessage = new CommandData().setCommand(Constants.UDP_CONNECT_BC_RESPONSE)
                            .setDeviceId(this.deviceManager.getPhoneId())
                            .setDeviceKey(this.deviceManager.getPhoneKey())
                            .setData(commandData.getDeviceId())
                            .setResponse(true)
                            .buildCommandString();

                    //add a new device
                    this.deviceManager.addToConnectingDeviceList(new Device(commandData.getDeviceId(),
                            commandData.getDeviceKey(), packet.getAddress(), packet.getPort()));
                } else {
                    responseMessage = new CommandData().setCommand(Constants.UDP_CONNECT_BC_RESPONSE)
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
         * 1. Request from device: UDP_PAIR_BROADCAST:deviceid:devicekeyChallenge
         * 2. Response from phone: UDP_PAIR_BROADCAST:phoneid:phonekey:ACK:devicekeyChallenge
         * 3. if device accepts it it sends:
         *      UDP_PAIR_BROADCAST_ACCEPT:deviceid:devicekey
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

                        final Device tempPairingDevice = new Device(commandData.getDeviceId(), commandData.getDeviceKey(), remoteIp, remotePort);//here the key is the challenge key
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
                        deviceManager.addDevice(tempPairingDevice);//adds to live device list
                        deviceManager.persistNewlyAddedDevice(tempPairingDevice);
                        if (deviceManager.getCurrentDevice() == null || deviceManager.getLiveDeviceCount() == 1) {
                            deviceManager.setCurrentDevice(tempPairingDevice);
                        }
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
        Log.d(Constants.LOG_TAG_SERVICE, "processPairing() completed.");
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

    public void stop() {
        this.setRunning(false);
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
        }
        server = null;
    }
}