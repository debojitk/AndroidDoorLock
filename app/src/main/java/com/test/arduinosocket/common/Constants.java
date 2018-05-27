package com.test.arduinosocket.common;

/**
 * Created by debojitk on 27/08/2016.
 */
public class Constants {
    public static String UDP_CONN_BC_REQUEST_PHONE="UDP_CONN_BC_REQUEST_PHONE";
    public static String UDP_CONN_BC_RESPONSE_PHONE="UDP_CONN_BC_RESPONSE_PHONE";

    public static String UDP_CONN_BC_REQUEST_DEVICE="UDP_CONN_BC_REQUEST_DEVICE";
    public static String UDP_CONN_BC_RESPONSE_DEVICE="UDP_CONN_BC_RESPONSE_DEVICE";


    public static String UDP_PAIR_BROADCAST="UDP_PAIR_BROADCAST";
    public static String UDP_PAIR_BROADCAST_ACCEPT="UDP_PAIR_BROADCAST_ACCEPT";
    public static String UDP_PAIR_BROADCAST_REJECT="UDP_PAIR_BROADCAST_REJECT";
    public static String START_RECORD="START_RECORD";
    public static String GET_MESSAGES="GET_MESSAGES";
    public static String DELETE_FILE="DELETE_FILE";

    public static String RESTORE="RESTORE";
    public static String STOP_RECORD="STOP_RECORD";
    public static String START_PLAY ="START_PLAY";
    public static String STOP_PLAY ="STOP_PLAY";
    public static String START_COMM="START_COMM";
    public static String STOP_COMM="STOP_COMM";
    public static String NOTIFY="NOTIFY";
    public static String NOTIFY_ACCEPT ="NOTIFY_ACCEPT";
    public static String SAVE_CONFIG ="SAVE_CONFIG";
    public static String COLON=":";
    public static String ACK="ACK";
    public static String NACK="NACK";
    public static String LOG_TAG_SERVICE="SERVICE";
    public static String LOG_TAG_MESSAGE="MSG";
    public static String LOG_TAG_SERVICE_UDP_PROCESOR="Service-UDPBCProcessor";
    public static String LOG_TAG_TOAST="TOAST";
    public static String CUSTOM_BROADCAST_INTENT="com.test.arduinosocket.ACTIVITY_STARTED";
    public static String DOOR_LOCKER_STATUS="Door Locker Status";
    public static String YES_RESPONSE="YesResponse";
    public static String NO_RESPONSE="NoResponse";
    public static String INTENT_DEVICE_ID="DEVICE_ID";
    public static String INTENT_DEVICE_STATUS="DEVICE_STATUS";
    public static String LOCAL_BC_EVENT_NOTIFICATION="com.test.arduinosocket.RING_NOTIFICATION";
    public static String LOCAL_BC_EVENT_PLAYBACK_STOPPED="com.test.arduinosocket.PLAYBACK_STOPPED";
    public static String LOCAL_BC_EVENT_RECORDING_STOPPED="com.test.arduinosocket.RECORDING_STOPPED";
    public static String LOCAL_BC_EVENT_RESTORE_COMPLETED="com.test.arduinosocket.RESTORE_COMPLETED";
    public static String LOCAL_BC_EVENT_ACTIVE_DEVICE_CHANGED="com.test.arduinosocket.ACTIVE_DEVICE_CHANGED";
    public static String LOCAL_BC_EVENT_LOCK_DEVICE_CHANGED="com.test.arduinosocket.LOCK_DEVICE_CHANGED";
    public static String LOCAL_BC_EVENT_DATA ="localBCEventData";

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

    public static String DEVICE_TYPE_COMM="COMM";
    public static String DEVICE_TYPE_LOCK="LOCK";

    public static String DEVICE_STATUS_ACTIVE="ACTIVE";
    public static String DEVICE_STATUS_INACTIVE="INACTIVE";

}
