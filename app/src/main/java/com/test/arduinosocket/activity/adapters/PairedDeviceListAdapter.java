package com.test.arduinosocket.activity.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.test.arduinosocket.R;
import com.test.arduinosocket.activity.DevicePreferencesActivity;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.core.Device;
import com.test.arduinosocket.core.DeviceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 15-04-2017.
 */

public class PairedDeviceListAdapter  extends ArrayAdapter<Device> {
    private final Context context;
    private final List<Device> values;
    private int selectedPosition = 0;
    private DeviceManager deviceManager;

    public PairedDeviceListAdapter(Context context, List<Device> values) {
        super(context, R.layout.list_item_paired_devices, values);
        this.context = context;
        this.values = values;

        if(values == null) {
            values = new ArrayList<Device>();
        }
        deviceManager=DeviceManager.getInstance();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_paired_devices, parent, false);
        }
        RadioButton r = (RadioButton)view.findViewById(R.id.radiobutton);
        final TextView mainText = (TextView)view.findViewById(R.id.textViewPairedLockName);
        mainText.setText(values.get(position).getDeviceId());
        RadioButton radioButton = (RadioButton)view.findViewById(R.id.radiobutton);
        //radioButton.setChecked(position == selectedPosition);
        radioButton.setTag(position);
        view.setTag(position);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = (Integer)view.getTag();
                deviceManager.setCurrentDevice(deviceManager.getDevice(getItem(selectedPosition).getDeviceId()));
                notifyDataSetChanged();

            }
        });
        ImageView imgDeleteButton=(ImageView)view.findViewById(R.id.imageViewUnpair);
        final View tempView=view;
        imgDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmDialog(values.get(position), tempView);
            }
        });

        ImageView imgSettingsButton=(ImageView) view.findViewById(R.id.imageViewSettings);
        imgSettingsButton.setTag(position);
        imgSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), DevicePreferencesActivity.class);
                i.putExtra("deviceId", values.get((Integer)v.getTag()).getDeviceId());
                getContext().startActivity(i);
            }
        });
        LinearLayout layoutRowSelector=(LinearLayout)view.findViewById(R.id.layoutRowSelector);
        layoutRowSelector.setTag(position);
        Device device=deviceManager.getDevice(values.get(position).getDeviceId());
        if(device!=null){
            LinearLayout layout=(LinearLayout)view.findViewById(R.id.layoutDeviceStatus);
            layout.setBackgroundColor(Color.GREEN);
            TextView ipText = (TextView) view.findViewById(R.id.textViewIP);
            ipText.setText(device.getDeviceIp().getHostAddress());
            if(deviceManager.getCurrentDevice().getDeviceId().equals(device.getDeviceId())){
                radioButton.setChecked(true);
                selectedPosition=position;
            }else{
                radioButton.setChecked(false);
            }
        }else{
            LinearLayout layout=(LinearLayout)view.findViewById(R.id.layoutDeviceStatus);
            layout.setBackgroundColor(Color.DKGRAY);
            TextView ipText = (TextView) view.findViewById(R.id.textViewIP);
            ipText.setText("Not Connected");
            radioButton.setEnabled(false);
            radioButton.setChecked(false);
        }

        //fetching data from persisted objects
        if(values.get(position)!=null) {
            if (values.get(position).getDeviceType().equalsIgnoreCase(Constants.DEVICE_TYPE_LOCK)) {
                radioButton.setEnabled(false);
                radioButton.setChecked(false);
                layoutRowSelector.setEnabled(false);
                mainText.append(" (" + Constants.DEVICE_TYPE_LOCK + ")");
            } else if (values.get(position).getDeviceType().equalsIgnoreCase(Constants.DEVICE_TYPE_COMM)) {
                mainText.append(" (" + Constants.DEVICE_TYPE_COMM + ")");
            }
        }

        layoutRowSelector.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(view.getTag()!=null) {
                    selectedPosition = (Integer) view.getTag();
                }
                notifyDataSetChanged();
                showPairedDeviceDialog(view, getItem(selectedPosition));
            }
        });


        return view;
    }
    private void showDeleteConfirmDialog(final Device device, final View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                this.context);

        // Setting Dialog Title
        alertDialog.setTitle("Confirm Unpair");

        // Setting Dialog Message
        alertDialog.setMessage("Do you want to unpair this device: "+device.getDeviceId()+" ?");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.mipmap.ic_launcher);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeDevice(device);
                        values.remove(device);
                        notifyDataSetChanged();
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
    private void removeDevice(Device device){
        deviceManager.removeDevice(device);
        deviceManager.removePairedDevice(device);
    }

    private void showPairedDeviceDialog(final View view, final Device commDevice){
        try {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_listview_paired_devices);
            //selecting only lock devices to be shown
            ArrayList<Device> pairedList = new ArrayList<>();
            for(Device device:deviceManager.getAllPairedDevices(context).values()){
                if(device.getDeviceType().equalsIgnoreCase(Constants.DEVICE_TYPE_LOCK)){
                    pairedList.add(device);
                }
            }
            final PairedLockDeviceListAdapter pairedDeviceListAdapter = new PairedLockDeviceListAdapter(context, pairedList);
            ListView pairedDeviceListView = (ListView) dialog.findViewById(R.id.lvPairedDevices);
            Button closeButton = (Button) dialog.findViewById(R.id.closeButton);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //interlinking comm device and lock device
                    if(!pairedDeviceListAdapter.isEmpty()) {
                        Device selectedLockDevice = deviceManager.getDevice(pairedDeviceListAdapter.getItem(pairedDeviceListAdapter.getSelectedPosition()).getDeviceId());
                        Device selectedCommDevice = deviceManager.getDevice(commDevice.getDeviceId());
                        if(deviceManager.getDevice(selectedCommDevice.getLinkDevice())!=null){
                            deviceManager.getDevice(selectedCommDevice.getLinkDevice()).setLinkDevice(null);
                            deviceManager.persistDeviceData(deviceManager.getDevice(selectedCommDevice.getLinkDevice()));
                        }
                        selectedCommDevice.setLinkDevice(selectedLockDevice.getDeviceId());
                        selectedLockDevice.setLinkDevice(selectedCommDevice.getDeviceId());
                        deviceManager.persistDeviceData(selectedCommDevice);
                        deviceManager.persistDeviceData(selectedLockDevice);
                    }
                    dialog.dismiss();
                }
            });
            pairedDeviceListView.setAdapter(pairedDeviceListAdapter);
            dialog.show();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
