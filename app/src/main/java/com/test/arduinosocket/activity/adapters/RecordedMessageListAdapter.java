package com.test.arduinosocket.activity.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.test.arduinosocket.R;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.common.Utils;
import com.test.arduinosocket.core.CommandData;
import com.test.arduinosocket.core.CommandResponseHandler;
import com.test.arduinosocket.core.Device;
import com.test.arduinosocket.core.DeviceManager;

/**
 * Created by debojitk on 19/09/2016.
 */
public class RecordedMessageListAdapter extends ArrayAdapter<String> implements CommandResponseHandler {
    private final Activity context;
    private final String[] web;
    private DeviceManager deviceManager;
    private View currentMessageView;

    public RecordedMessageListAdapter(Activity context,
                                      String[] web) {
        super(context, R.layout.list_single, web);
        this.context = context;
        this.web = web;
        this.deviceManager =DeviceManager.getInstance();
    }

    @Override
    public void handleResponse(CommandData response, Device device) {
        if(response.isResponse()){
            switch (response.getCommand()){
                case "STOP_PLAY"://called when the file plahyback is completed
                    new Thread(){
                        public void run(){
                            ((ImageView)currentMessageView).setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                        }
                    }.start();
                    break;
                default:
                    Utils.showMessage("Nothing to do.");
            }
        }
        if(response.isError()){
            Utils.showMessage(response.getData());
        }
    }

    public void startPlay(final String fileName) {
        new Thread() {
            @Override
            public void run() {
                deviceManager.sendMessageRequest(Constants.START_PLAY, fileName, RecordedMessageListAdapter.this);
            }
        }.start();
    }
    public void deleteMessage(final String fileName) {
        new Thread() {
            @Override
            public void run() {
                deviceManager.sendMessageRequest(Constants.DELETE_FILE , fileName, null);
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

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.textViewMessage);
        txtTitle.setText(web[position]);

        ImageView imageViewPlay = (ImageView) rowView.findViewById(R.id.imageViewPlay);
        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            boolean isPlaying=false;
            @Override
            public void onClick(View view) {

                TableLayout tl=(TableLayout) (view.getParent().getParent());
                TextView txtTitle = (TextView) tl.findViewById(R.id.textViewMessage);
                String cmdInput=Constants.MESSAGES_STRING+"/"+txtTitle.getText();
                Log.d(Constants.LOG_TAG_SERVICE, "Message is: "+cmdInput);
                if(isPlaying){
                    isPlaying=false;
                    ((ImageView)view).setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                    stopPlay();
                }else{
                    ((ImageView)view).setImageResource(R.drawable.ic_stop_black_24dp);
                    isPlaying=true;
                    currentMessageView=view;//storing view for future use
                    startPlay(cmdInput);
                }

            }
        });

        ImageView imageViewDelete = (ImageView) rowView.findViewById(R.id.imageViewDelete);
        imageViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TableLayout tl=(TableLayout) (view.getParent().getParent());
                TextView txtTitle = (TextView) tl.findViewById(R.id.textViewMessage);
                String cmdInput=Constants.MESSAGES_STRING+"/"+txtTitle.getText();
                Log.d(Constants.LOG_TAG_SERVICE, "Delete message is: "+cmdInput);
                showDeleteConfirmDialog(txtTitle.getText().toString(), view);
            }
        });

        return rowView;
    }

    private void showDeleteConfirmDialog(final String messageName, final View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                this.context);

        // Setting Dialog Title
        alertDialog.setTitle("Confirm Delete");

        // Setting Dialog Message
        alertDialog.setMessage("Do you want to delete the message "+messageName+" ?");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.mipmap.ic_launcher);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(Constants.MESSAGES_STRING+"/"+messageName);
                        View row=(View)view.getParent();
                        TableLayout tl=(TableLayout) row.getParent();
                        tl.removeView(row);
                        tl.invalidate();
                    }
                });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }


}
