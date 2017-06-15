package com.test.arduinosocket.activity.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TextView;

import com.test.arduinosocket.R;
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
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = (Integer)view.getTag();
                deviceManager.setCurrentDevice(deviceManager.getDevice(mainText.getText().toString()));
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
        radioButton.setTag(position);
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

}
