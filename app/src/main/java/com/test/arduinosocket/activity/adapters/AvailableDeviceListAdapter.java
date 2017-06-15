package com.test.arduinosocket.activity.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.test.arduinosocket.R;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.core.CommandData;
import com.test.arduinosocket.core.Device;
import com.test.arduinosocket.core.DeviceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 15-04-2017.
 */

public class AvailableDeviceListAdapter extends ArrayAdapter<Device> {
    private final Context context;
    private final List<Device> values;
    private int selectedPosition = 0;
    private DeviceManager deviceManager;

    public AvailableDeviceListAdapter(Context context, List<Device> values) {
        super(context, R.layout.list_item_available_devices, values);
        this.context = context;
        this.values = values;

        if(values == null) {
            values = new ArrayList<Device>();
        }
        deviceManager=DeviceManager.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_available_devices, parent, false);
            final TextView mainText = (TextView)view.findViewById(R.id.textViewAvailableLockName);
            mainText.setText(values.get(position).getDeviceId());
            final Device tempDevice=deviceManager.getTempPairingDeviceMap().get(values.get(position).getDeviceId());
            if(tempDevice!=null){
                TextView ipText = (TextView) view.findViewById(R.id.textViewAvailableDevicesIP);
                ipText.setText(tempDevice.getDeviceIp().getHostAddress());
                ImageView pairImageView=(ImageView)view.findViewById(R.id.imageViewPair);
                pairImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String responseMessage=new CommandData().setCommand(Constants.UDP_PAIR_BROADCAST)
                                .setData(tempDevice.getDeviceKey())
                                .setDeviceId(deviceManager.getPhoneId())
                                .setDeviceKey(deviceManager.getPhoneKey())
                                .setResponse(true)
                                .setError(false)
                                .setData(tempDevice.getDeviceKey())
                                .buildCommandString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                deviceManager.getBroadcastCommandProcessor().sendMessage(responseMessage, tempDevice);
                            }
                        }).start();
                    }
                });
            }
        }
        return view;
    }
}
