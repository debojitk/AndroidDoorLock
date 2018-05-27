package com.test.arduinosocket.network;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.common.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by debojitk on 18/09/2016.
 */
public class TCPIPAudioController extends AsyncTask<Void, String, Void> {

    String response = "";
    private ServerSocket serverSocket;
    private SocketAddress remoteSocketAddress;
    private int port=8086;
    private boolean send=true;
    private boolean receive=true;
    private boolean running=true;
    private Socket socket;
    boolean socketStatus = false;
    private ReceiveData receiveData;
    private SendData sendData;
    private static TCPIPAudioController tcpipController;
    public static synchronized TCPIPAudioController getInstance(){
        if(tcpipController==null ||!tcpipController.isRunning()||tcpipController.isCancelled()){
            tcpipController=new TCPIPAudioController();
            tcpipController.execute();
        }
        return tcpipController;
    }

    public TCPIPAudioController() {
        Log.w("MSG", "Entering async task");
    }

    public boolean isRunning(){
        return this.getStatus()==Status.RUNNING;
    }

    public void doCancel(){
        running=false;
    }

    public boolean isSocketStatus() {
        return socketStatus;
    }

    public void setSocketStatus(boolean socketStatus) {
        this.socketStatus = socketStatus;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        try {
            serverSocket = new ServerSocket(port);
            while(running){
                System.out.println("->Waiting for client on port " + serverSocket.getLocalPort() + "...");
                try {
                    socket = serverSocket.accept();
                    remoteSocketAddress = socket.getRemoteSocketAddress();
                    Log.i("MSG", "->Just connected to " + remoteSocketAddress);

                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if(socket!=null) {
                    if(receive) {
                        receiveData = new ReceiveData(socket);
                        Thread receiveThread = new Thread(receiveData);
                        receiveThread.start();
                    }
                    if(send) {
                        sendData = new SendData(socket);
                        Thread sendThread = new Thread(sendData);
                        sendThread.start();
                    }
                    socketStatus = true;
                }else{
                    socketStatus=false;
                }

            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
            socketStatus = false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "IOException: " + e.toString();
            socketStatus=false;
        }
        return null;
    }
    public boolean startCommunication(boolean receive, boolean send) {
        if (socketStatus) {
            Utils.showMessage("Already talking to a Socket!! Disconnect and try again!");
            return false;
        } else {
            //check if tcpipcontroller is running
            //applyRequiredAudioSettings();
            tcpipController= TCPIPAudioController.getInstance();
            tcpipController.setSend(send);
            tcpipController.setReceive(receive);
            Utils.showMessage("Connecting...!");
            socketStatus = true;
            return true;
        } //else when already active socket conn.
    }

    public void stopCommunication() {
        //restoreAudioSettings();
        if (!socketStatus) {
            Utils.showMessage("SOCKET Already Closed!!");
        }else {
            try {
                tcpipController= TCPIPAudioController.getInstance();
                if (tcpipController != null) {
                    Utils.showMessage("Disconnecting...!");
                    socketStatus = false;
                    closeSocket();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public boolean isSend() {
        return send;
    }

    public void setSend(boolean send) {
        this.send = send;
    }

    public boolean isReceive() {
        return receive;
    }

    public void setReceive(boolean receive) {
        this.receive = receive;
    }

    @Override
    protected void onProgressUpdate(String... values) {
    }

    @Override
    protected void onPostExecute(Void result) {
    }
    public void closeSocket(){
        if(sendData!=null) {
            sendData.close();
        }
        if(receiveData!=null) {
            receiveData.close();
        }
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        socketStatus=false;
    }
    public class ReceiveData implements Runnable {
        private Socket socket;
        private DataInputStream in;
        private boolean closed=false;

        public ReceiveData(Socket socket) {
            this.socket = socket;
            try {
                this.in = new DataInputStream(this.socket.getInputStream());
            }catch(Exception ex){
                ex.printStackTrace();
                Log.w("MSG","An error occurred"+ex.getMessage());
            }
        }

        public void close(){
            this.closed=true;
        }
        public void run() {
            try {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
                int bufferSize = AudioTrack.getMinBufferSize(Constants.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_8BIT);

                if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
                    bufferSize = Constants.SAMPLE_RATE * 2;
                }

                int readBytes;
                byte[] buffer = new byte[bufferSize];
                bufferSize = bufferSize * 2;
                AudioTrack audioTrack = new AudioTrack(
                        AudioManager.STREAM_VOICE_CALL,
                        Constants.SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_8BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM);
                audioTrack.play();

                //audioTrack.setPreferredDevice(AudioDeviceInfo.TYPE_BUILTIN_EARPIECE);
                while (!closed) {
                    try {
                        if (in.available() > 0) {
                            readBytes = in.read(buffer, 0, buffer.length);
                            audioTrack.write(buffer, 0, readBytes);
                        }
                    } catch (SocketTimeoutException s) {
                        System.out.println("Socket timed out!");
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        audioTrack.release();
                        in.close();
                        socket.close();
                        break;
                    }
                }
                if(audioTrack!=null) {
                    audioTrack.release();
                }
                if(in!=null) {
                    in.close();
                }
                if(!socket.isClosed()) {
                    socket.close();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            socketStatus=false;
        }
    }
    public class SendData implements Runnable {
        private Socket socket;
        private DataOutputStream out;
        private boolean closed=false;
        //private int[] mSampleRates = new int[] { 8000, 11025, 19250, 22050, 44100 };
        //private int[] mSampleRates = new int[] { 44100,22050,19250,11025,8000 };
        private int[] mSampleRates = new int[] {Constants.SAMPLE_RATE};
        private int selectedSampleRate;
        private int bufferSize;
        public SendData(Socket socket) {
            this.socket = socket;
            try {
                this.out = new DataOutputStream(this.socket.getOutputStream());
            }catch(Exception ex){
                ex.printStackTrace();
                Log.w("MSG","An error occured"+ex.getMessage());
            }
        }

        public void close(){
            this.closed=true;
        }
        public AudioRecord findAudioRecord() {
            for (int rate : mSampleRates) {
                for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                    for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                        try {
                            Log.d("MSG", "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                    + channelConfig);
                            int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                            if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                                // check if we can instantiate and have a success
                                int mode=MediaRecorder.AudioSource.DEFAULT;
                                if(send&&!receive){
                                    //recording audio so max mic volume
                                    mode=MediaRecorder.AudioSource.DEFAULT;
                                }else if(send && receive){
                                    //use VOIP settings
                                    mode=MediaRecorder.AudioSource.VOICE_RECOGNITION;
                                }
                                AudioRecord recorder = new AudioRecord(mode, rate, channelConfig, audioFormat, bufferSize);

                                if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                                    this.bufferSize=bufferSize;
                                    this.selectedSampleRate=rate;
                                    return recorder;
                                }
                            }
                        } catch (Exception e) {
                            Log.e("MSG", rate + "Exception, keep trying.",e);
                        }
                    }
                }
            }
            return null;
        }
        public void run() {
            try {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
                // buffer size in bytes
                int bufferSize = AudioRecord.getMinBufferSize(Constants.SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_8BIT);

                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    bufferSize = Constants.SAMPLE_RATE * 2;
                }

                int readBytes=0;
                bufferSize=2920;
                byte[] buffer = new byte[bufferSize];
                //bufferSize = bufferSize * 2;
                AudioRecord record = findAudioRecord();
                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e("MSG", "Audio Record can't initialize!");
                    return;
                }
                record.startRecording();

                Log.v("MSG", "Start recording");
                while (!closed) {
                    try {
                        readBytes=record.read(buffer,0,bufferSize);
                        if(readBytes>0){
                            out.write(buffer,0, readBytes);
                        }
                    } catch (SocketTimeoutException s) {
                        System.out.println("Socket timed out!");
                        break;
                    } catch (IOException e) {
                        break;
                    }
                }
                if(record!=null) {
                    record.release();
                }
                if(out!=null) {
                    out.close();
                }
                if(socket!=null && !socket.isClosed()) {
                    socket.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            socketStatus=false;
        }
    }
}

