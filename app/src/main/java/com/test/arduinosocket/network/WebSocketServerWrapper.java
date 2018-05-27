package com.test.arduinosocket.network;


import android.content.Intent;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.test.arduinosocket.R;
import com.test.arduinosocket.activity.CallNotificationActivity;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.core.Device;
import com.test.arduinosocket.core.DeviceManager;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import static android.content.Context.WINDOW_SERVICE;

public class WebSocketServerWrapper extends WebSocketServer {
    private int counter = 0;
    private DeviceManager deviceManager;
    private static HashMap<WebSocket, HeartbeatManager> connectionMap = new HashMap<>();



    private static WebSocketServerWrapper serverInstance;

    public static synchronized WebSocketServerWrapper getInstance() {
        if (serverInstance == null) {
            try {
                serverInstance = new WebSocketServerWrapper(Constants.WEB_SOCKET_SERVER_PORT);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return serverInstance;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public void setDeviceManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    public WebSocketServerWrapper(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public WebSocketServerWrapper(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        counter++;
        System.out.println("Opened connection number: " + counter);
        System.out.println("Remote address is: " + conn.getRemoteSocketAddress());
        boolean connected = false;
        if (handshake != null) {
            String deviceInfo = handshake.getResourceDescriptor();

            if (deviceInfo != null && deviceInfo.trim().length() > 0) {
                if(deviceInfo.startsWith("/")){
                    deviceInfo=deviceInfo.substring(1);
                }
                String[] deviceInfoArr = deviceInfo.split("\\:");
                if (deviceInfoArr.length == 2) {
                    String deviceId = deviceInfoArr[0];
                    String deviceKey = deviceInfoArr[1];
                    if (deviceManager.checkPairedDevice(deviceId, deviceKey)) {
                        Device device = deviceManager.getPairedDevice(deviceId);
                        connected=deviceManager.linkDeviceWithWebSocketConnection(conn, device);
                        if(connected){
                            HeartbeatManager manager = new HeartbeatManager(conn, device);
                            connectionMap.put(conn, manager);
                            manager.startManager();
                        }
                    }
                }
            }
        }
        connected=true;
        if (!connected) {
            conn.close();
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed due to: "+reason);
        HeartbeatManager manager=connectionMap.get(conn);
        if(manager!=null){
            manager.interrupt();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("Error:");
        ex.printStackTrace();

    }

    public void onStart() {
        System.out.println("Server started!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println(message);
        deviceManager.delegateCommand(conn, message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer blob) {
        conn.send(blob);
    }

    @Override
    public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
        FrameBuilder builder = (FrameBuilder) frame;
        builder.setTransferemasked(false);
        conn.sendFrame(frame);
    }

    @Override
    public void onWebsocketPong(WebSocket conn, Framedata f) {
        super.onWebsocketPong(conn, f);
        System.out.println("Pong received: " + new String(f.getPayloadData().array()));
        HeartbeatManager manager = connectionMap.get(conn);
        if (manager != null) {
            manager.setLastPongTime(System.currentTimeMillis());
        }
    }
    public class HeartbeatManager implements Runnable {
        private WebSocket conn;
        private long lastPongTime;
        private long currentMillis;
        private long sleepTime = 15000L;
        private String payload;
        private Thread runnerThread;
        private Device device;
        private boolean pongReceived=false;

        public HeartbeatManager(WebSocket conn, Device device) {
            // TODO Auto-generated constructor stub
            this.conn = conn;
            payload = "payload=" + device.getDeviceId() + ":" + device.getDeviceKey() + ":" + conn.getRemoteSocketAddress();
            this.device=device;
        }

        public void startManager() {
            runnerThread = new Thread(this);
            runnerThread.start();
        }

        public Device getDevice() {
            return device;
        }

        public void setDevice(Device device) {
            this.device = device;
        }

        /**
         * @return the lastPongTime
         */
        public long getLastPongTime() {
            return lastPongTime;
        }

        /**
         * @param lastPongTime the lastPongTime to set
         */
        public void setLastPongTime(long lastPongTime) {
            this.lastPongTime = lastPongTime;
            pongReceived=true;
        }

        /**
         * @return the conn
         */
        public WebSocket getConn() {
            return conn;
        }

        /**
         * @param conn the conn to set
         */
        public void setConn(WebSocket conn) {
            this.conn = conn;
        }

        /**
         * @return the currentMillis
         */
        public long getCurrentMillis() {
            return currentMillis;
        }

        /**
         * @param currentMillis the currentMillis to set
         */
        public void setCurrentMillis(long currentMillis) {
            this.currentMillis = currentMillis;
        }

        /**
         * @return the sleepTime
         */
        public long getSleepTime() {
            return sleepTime;
        }

        /**
         * @param sleepTime the sleepTime to set
         */
        public void setSleepTime(long sleepTime) {
            this.sleepTime = sleepTime;
        }

        public void interrupt(){
            try {
                this.runnerThread.interrupt();
            }catch(Exception ex){}
        }
        @Override
        public void run() {
            long tempSleepTime = sleepTime;
            lastPongTime = System.currentTimeMillis() + tempSleepTime;
            int pongFailCount = 0;
            while (true) {
                try {
                    //sending ping
                    FramedataImpl1 resp = new FramedataImpl1();
                    resp.setFin(true);
                    resp.setOptcode(Framedata.Opcode.PING);
                    resp.setPayload(ByteBuffer.wrap(payload.getBytes()));
                    resp.setTransferemasked(false);
                    if (!conn.isClosed()) {
                        pongReceived=false;
                        conn.sendFrame(resp);
                        System.out.println(payload);
                    } else {
                        break;
                    }
                    Thread.sleep(tempSleepTime);
                    if (!pongReceived) {
                        Log.i(Constants.LOG_TAG_SERVICE,"Pong not received for long  time - " + (currentMillis - lastPongTime));
                        pongFailCount++;
                        if (pongFailCount > 2) {
                            Log.i(Constants.LOG_TAG_SERVICE,"All ping failed, it seems device is not available anymore.");
                            //pongFailCount=0;
                            break;
                        }
                    } else {
                        pongFailCount = 0;//resetting pongFailCount
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    break;
                }
            }
            Log.i(Constants.LOG_TAG_SERVICE,"Heartbeat manager closing, Removing device from device list");
            connectionMap.remove(conn);
            deviceManager.removeDevice(device);
            counter--;
        }
    }
}