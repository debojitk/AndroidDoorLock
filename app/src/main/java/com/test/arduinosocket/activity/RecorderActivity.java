package com.test.arduinosocket.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.test.arduinosocket.R;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.common.Utils;
import com.test.arduinosocket.core.CommandData;
import com.test.arduinosocket.core.CommandResponseHandler;
import com.test.arduinosocket.core.Device;
import com.test.arduinosocket.core.DeviceManager;
import com.test.arduinosocket.network.TCPIPAudioController;

public class RecorderActivity extends AppCompatActivity implements CommandResponseHandler {
    private ImageView imageViewRecordMessage, imageViewPlayMessage, imageViewRestorePrevious, imageViewRestoreDefault;
    private Button buttonShowRecordedMessages;
    private Spinner recordFileSpinner;
    private boolean isPlaying=false;
    private boolean isRecording=false;
    private TCPIPAudioController tcpipAudioController;
    private DeviceManager deviceManager;
    @Override
    public void handleResponse(CommandData response, Device device) {
        if(response.isResponse()){
            switch (response.getCommand()){
                case "GET_MESSAGES" :
                    processRecordedMessages(response.getData());
                    break;
                case "STOP_RECORD":
                    notifyActivityOnStopRecord();
                    break;
                case "STOP_PLAY":
                    notifyActivityOnStopPlayback();
                    break;
                default:
                    Utils.showMessage("Nothing to do.");
            }
        }
        if(response.isError()){
            Utils.showMessage(response.getData());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        imageViewPlayMessage=(ImageView)findViewById(R.id.imageViewPlayMessage);
        imageViewRecordMessage=(ImageView)findViewById(R.id.imageViewRecordMessage);
        imageViewRestorePrevious=(ImageView)findViewById(R.id.imageViewRestorePrevious);
        imageViewRestoreDefault=(ImageView)findViewById(R.id.imageViewRestoreDefault);
        recordFileSpinner=(Spinner)findViewById(R.id.recordFileSpinner);
        buttonShowRecordedMessages=(Button)findViewById(R.id.buttonShowRecordedMessages);

        buttonShowRecordedMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RecorderActivity.this, MessageViewActivity.class);
                //startActivity(i);
                new Thread() {
                    @Override
                    public void run() {
                        deviceManager.sendMessageRequest(Constants.GET_MESSAGES, null, RecorderActivity.this);
                    }
                }.start();


            }
        });

        imageViewPlayMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlaying){
                    isPlaying=false;
                    imageViewPlayMessage.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                    stopPlay();
                }else{
                    imageViewPlayMessage.setImageResource(R.drawable.ic_stop_black_24dp);
                    isPlaying=true;
                    startPlay(getFileName());
                }
            }
        });
        imageViewRecordMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Utils.showMessage("Recording disabled");
                if(isRecording){
                    isRecording=false;
                    imageViewRecordMessage.setImageResource(R.drawable.ic_mic_black_24dp);
                    stopRecording();
                }else{
                    imageViewRecordMessage.setImageResource(R.drawable.ic_stop_black_24dp);
                    isRecording=true;
                    startRecording(getFileName());
                }
            }
        });
        imageViewRestoreDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        imageViewRestorePrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        tcpipAudioController = TCPIPAudioController.getInstance();
        deviceManager=DeviceManager.getInstance();
    }
    private String getFileName(){
        String retVal= getResources().getStringArray(R.array.voice_array_file_name)[recordFileSpinner.getSelectedItemPosition()];;
        //retVal="/"+retVal+".raw";
        retVal=retVal+".raw";
        return retVal;
    }

    public void startRecording(final String fileName) {
        if (tcpipAudioController.startCommunication(false, true)) {
            new Thread() {
                @Override
                public void run() {
                    deviceManager.sendMessageRequest(Constants.START_RECORD , fileName, RecorderActivity.this);
                }
            }.start();

        } else {
            Utils.showMessage("Recording can't be started");
        }
    }

    public void stopRecording() {
        new Thread() {
            @Override
            public void run() {
                deviceManager.sendMessageRequest(Constants.STOP_RECORD);//no handling required
            }
        }.start();
        tcpipAudioController.stopCommunication();
    }

    public void startPlay(final String fileName) {
        new Thread() {
            @Override
            public void run() {
                deviceManager.sendMessageRequest(Constants.START_PLAY , fileName, null);//no handling required
            }
        }.start();
    }

    public void stopPlay() {
        new Thread() {
            @Override
            public void run() {
                deviceManager.sendMessageRequest(Constants.STOP_PLAY);
            }
        }.start();
    }

    public void notifyActivityOnStopRecord(){
        tcpipAudioController.stopCommunication();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isRecording=false;
                imageViewRecordMessage.setImageResource(R.drawable.ic_mic_black_24dp);
            }
        });
    }
    public void notifyActivityOnStopPlayback(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isPlaying=false;
                imageViewPlayMessage.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            }
        });
    }

    public void processRecordedMessages(final String message){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(RecorderActivity.this, MessageViewActivity.class);
                i.putExtra("messages",message);
                startActivity(i);
            }
        });
    }

}
