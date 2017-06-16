package com.test.arduinosocket.common;

/**
 * Created by debojitk on 27/08/2016.
 */
public class Constants {
    public static String UDP_CONNECT_BROADCAST="UDP_CONNECT_BROADCAST";
    public static String UDP_CONNECT_BC_REQUEST="UDP_CONNECT_BC_REQUEST";
    public static String UDP_CONNECT_BC_RESPONSE="UDP_CONNECT_BC_RESPONSE";
    public static String UDP_PAIR_BROADCAST="UDP_PAIR_BROADCAST";
    public static String UDP_PAIR_BROADCAST_ACCEPT="UDP_PAIR_BROADCAST_ACCEPT";
    public static String UDP_PAIR_BROADCAST_REJECT="UDP_PAIR_BROADCAST_REJECT";
    public static String START_RECORD="START_RECORD";
    public static String GET_MESSAGES="GET_MESSAGES";
    public static String DELETE_FILE="DELETE_FILE";

    public static String STOP_RECORD="STOP_RECORD";
    public static String START_PLAY ="START_PLAY";
    public static String STOP_PLAY ="STOP_PLAY";
    public static String START_COMM="START_COMM";
    public static String STOP_COMM="STOP_COMM";
    public static String NOTIFY="NOTIFY";
    public static String NOTIFY_COMPLETE="NOTIFY_COMPLETE";
    public static String SAVE_CONFIG ="SAVE_CONFIG";
    public static String COLON=":";
    public static String ACK="ACK";
    public static String NACK="NACK";
    public static String LOG_TAG_SERVICE="SERVICE";
    public static String LOG_TAG_MESSAGE="MSG";
    public static String LOG_TAG_TOAST="TOAST";
    public static String CUSTOM_BROADCAST_INTENT="com.test.arduinosocket.ACTIVITY_STARTED";
    public static String DOOR_LOCKER_STATUS="Door Locker Status";
    public static String YES_RESPONSE="YesResponse";
    public static String NO_RESPONSE="NoResponse";
    public static String CALL_NOTIFICATION_CLIENT="callNotificationIntent";
    public static String INTENT_DEVICE_ID="DEVICE_ID";
    public static String LOCAL_BC_EVENT_NOTIFICATION="com.test.arduinosocket.RING_NOTIFICATION";
    public static String LOCAL_BC_EVENT_ACTIVE_DEVICE_CHANGED="com.test.arduinosocket.ACTIVE_DEVICE_CHANGED";

    public static String MESSAGES_STRING="messages";
    public  static  String NOT_CONNECTED="Not Connected to wifi";
    public static int WIFI_STOPPED_NOTIFICATION_ID=100001;
    public static int DEVICE_REQUEST_NOTIFICATION_ID=100002;
    public static int INBUFFER=2920;
    public static int OUTBUFFER=2920;
    public static int TCP_IP_PORT=8086;
    public static int WEB_SOCKET_SERVER_PORT=8087;
    public static int UDP_REMOTE_PORT=6666;
    public static int SAMPLE_RATE = 22050;
    public static int HEARTBEAT_DELAY = 5000;

}
