package com.verboze.control.roomfeature;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.verboze.control.Main;
import com.verboze.control.R;
import com.verboze.control.RoomControllerDevice;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hasan on 7/14/17.
 */
public class RoomFeatureSettings extends RoomFeature {
    LinearLayout view = null;
    Button refresh = null;

    ListView devices = null;
    public ArrayAdapter<String> settings_adapter = null;

    public RoomControllerDevice cur_device;
    public HashMap<String, RoomControllerDevice> available_devices;

    Button.OnClickListener listener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == refresh) {
                activity.DiscoverRooms();
            }
        }
    };

    ListView.OnItemClickListener deviceSelectListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String str = devices.getItemAtPosition(position).toString();
            RoomControllerDevice new_device = available_devices.get(str);
            if (new_device != cur_device) {
                cur_device = new_device;
                RoomControllerDevice.SaveDeviceData(activity, new_device);
                activity.SetCurrentDevice(new_device);
            }
        }
    };

    public RoomFeatureSettings(Main ac) {
        super(ac);
    }

    @Override
    public void reInit(String cur_lang) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            view = (LinearLayout) inflater.inflate(R.layout.settings, null);
        }
        if (devices == null) {
            ArrayList<String> settings_adapterList = new ArrayList<String>();
            settings_adapter = new ArrayAdapter<>(activity.getApplicationContext(),
                    android.R.layout.simple_list_item_1,
                    settings_adapterList);
            devices = (ListView) view.findViewById(R.id.devices_list);
            devices.setAdapter(settings_adapter);
            devices.setOnItemClickListener(deviceSelectListener);
            available_devices = new HashMap<>();
        }
        if (refresh == null) {
            refresh = (Button) view.findViewById(R.id.refresh);
            refresh.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(RoomControllerDevice cd, ViewGroup main_tab) {
        main_tab.addView(view);
    }
}
