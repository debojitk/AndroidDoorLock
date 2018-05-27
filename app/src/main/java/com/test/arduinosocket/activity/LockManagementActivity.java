package com.test.arduinosocket.activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.test.arduinosocket.MyApplication;
import com.test.arduinosocket.R;
import com.test.arduinosocket.activity.adapters.AvailableDeviceListAdapter;
import com.test.arduinosocket.activity.adapters.PairedDeviceListAdapter;
import com.test.arduinosocket.common.Constants;
import com.test.arduinosocket.core.Device;
import com.test.arduinosocket.core.DeviceManager;
import com.test.arduinosocket.network.UDPBroadcastCommandProcessor;

import java.net.UnknownHostException;
import java.util.ArrayList;


public class LockManagementActivity extends AppCompatActivity {

    private SharedPreferences deviceMap;
    private PairedDeviceListAdapter pairedDeviceListAdapter;
    private ArrayList<Device> pairedList;
    private AvailableDeviceListAdapter availableDeviceListAdapter;
    private ArrayList<Device> availableList;
    private Button refreshDeviceList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_management);
        //Initializing the Listview for existing locks
        pairedList = new ArrayList<>();
        pairedList.addAll(DeviceManager.getInstance().getAllPairedDevices(this).values());
        pairedDeviceListAdapter = new PairedDeviceListAdapter(this, pairedList);
        ListView pairedDeviceListView = (ListView) findViewById(R.id.lvwLocks);
        pairedDeviceListView.setAdapter(pairedDeviceListAdapter);


        availableList = new ArrayList<>();
        availableDeviceListAdapter = new AvailableDeviceListAdapter(this, availableList);
        ListView availableDeviceListView = (ListView) findViewById(R.id.lvwOtherLocks);
        availableDeviceListView.setAdapter(availableDeviceListAdapter);

        refreshDeviceList=(Button)findViewById(R.id.btnRefreshList);
        refreshDeviceList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceManager.getInstance().getTempPairingDeviceMap().clear();
                availableList.clear();
                availableDeviceListAdapter.notifyDataSetChanged();
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            UDPBroadcastCommandProcessor.getInstance().searchActiveDevices(10000);
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }catch(Exception ex){
                            Log.e(Constants.LOG_TAG_MESSAGE, "An error occurred in searchDevices", ex);
                        }
                    }
                }.start();
            }
        });

        MyApplication.setCurrentActivity(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.label_lock_management_menu, menu);
        return true;
    }

    public void addDeviceToAvailableList(Device device){
        availableList.add(device);
        //availableDeviceListAdapter.setNotifyOnChange(true);
        availableDeviceListAdapter.notifyDataSetChanged();

    }
    public void removeDeviceFromAvailableList(Device device){
        availableList.remove(device);
        availableDeviceListAdapter.setNotifyOnChange(true);
        availableDeviceListAdapter.notifyDataSetChanged();

    }
    public void addDeviceToPairedList(Device device){
        pairedList.add(device);
        pairedDeviceListAdapter.setNotifyOnChange(true);
        pairedDeviceListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeviceManager.getInstance().getTempPairingDeviceMap().clear();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyApplication.setCurrentActivity(this);
    }
}
