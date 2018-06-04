package com.test.arduinosocket.core;

import com.google.gson.Gson;

import org.java_websocket.WebSocket;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by administrator on 4/20/2017.
 */

public class Device implements Serializable{
    private String deviceId;
    private String deviceKey;
    private transient boolean connected;
    private InetAddress deviceIp;
    private int devicePort;
    private int localPort;
    private int localHeartbeatPort;
    private transient WebSocket webSocketConnection;
    private boolean defaultDevice;
    private String deviceType;
    private String linkDevice;
    private Map<String, Object> settings=new HashMap<>();

    public boolean isDefaultDevice() {
        return defaultDevice;
    }

    public void setDefaultDevice(boolean defaultDevice) {
        this.defaultDevice = defaultDevice;
    }

    public WebSocket getWebSocketConnection() {
        return webSocketConnection;
    }

    public void setWebSocketConnection(WebSocket webSocketConnection) {
        this.webSocketConnection = webSocketConnection;
    }

    public int getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(int devicePort) {
        this.devicePort = devicePort;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getLocalHeartbeatPort() {
        return localHeartbeatPort;
    }

    public void setLocalHeartbeatPort(int localHeartbeatPort) {
        this.localHeartbeatPort = localHeartbeatPort;
    }

    public Device(String deviceId, String deviceKey, InetAddress deviceIp, int remotePort, String deviceType){
        this.deviceId=deviceId;
        this.deviceIp=deviceIp;
        this.devicePort=remotePort;
        this.deviceKey=deviceKey;
        this.deviceType=deviceType;
    }

    public void stop(){

    }
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }


    public InetAddress getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(InetAddress deviceIp) {
        this.deviceIp = deviceIp;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getLinkDevice() {
        return linkDevice;
    }

    public void setLinkDevice(String linkDevice) {
        this.linkDevice = linkDevice;
    }
    public Map<String, Object> getSettingsMap(){
        if(settings==null){
            settings=new HashMap<>();
        }
        return settings;
    }
    public void setSettingsMap(Map<String, Object> settings){
        this.settings=settings;
    }
}
