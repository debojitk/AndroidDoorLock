package com.test.arduinosocket.activity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.test.arduinosocket.R;
import com.test.arduinosocket.core.Device;
import com.test.arduinosocket.core.DeviceManager;

import java.util.List;

/**
 * Created by Administrator on 15-04-2017.
 */

public class PairedLockDeviceListAdapter extends ArrayAdapter<Device> {
    private final Context context;
    private final List<Device> values;
    private int selectedPosition = 0;
    private DeviceManager deviceManager;

    public PairedLockDeviceListAdapter(Context context, List<Device> values) {
        super(context, R.layout.list_item_paired_lock_devices_for_comm_attach, values);
        this.context = context;
        this.values = values;
        deviceManager=DeviceManager.getInstance();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_paired_lock_devices_for_comm_attach, parent, false);
        }
        final TextView mainText = (TextView)view.findViewById(R.id.commDeviceTextView);
        mainText.setText(values.get(position).getDeviceId());
        RadioButton radioButton = (RadioButton)view.findViewById(R.id.commDeviceRadio);
        radioButton.setChecked(position == selectedPosition);
        radioButton.setTag(position);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = (Integer)view.getTag();
                notifyDataSetChanged();

            }
        });
        return view;
    }

    public int getSelectedPosition(){
        return selectedPosition;
    }
}
