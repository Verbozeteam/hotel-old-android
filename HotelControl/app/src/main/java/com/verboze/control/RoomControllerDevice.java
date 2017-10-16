package com.verboze.control;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by hasan on 7/14/17.
 */
public class RoomControllerDevice {
    public String IP;
    public String name;
    public int type;
    public String data;

    public RoomControllerDevice(String ip, String n, int t, String d) {
        IP = ip;
        name = n;
        type = t;
        data = d;
    }

    public static RoomControllerDevice GetSavedDevice(Activity ac) {
        SharedPreferences sharedPref = ac.getPreferences(Context.MODE_PRIVATE);
        if (!sharedPref.contains("default_device_IP"))
            return null;

        RoomControllerDevice d = null;
        try {
            d = new RoomControllerDevice(
                    sharedPref.getString("default_device_IP", "not found"),
                    sharedPref.getString("default_device_name", "not found"),
                    sharedPref.getInt("default_device_type", 0),
                    sharedPref.getString("default_device_data", "")
            );
            if (d.IP.equals("not found"))
                return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return d;
    }

    public static void SaveDeviceData(Activity ac, RoomControllerDevice d) {
        SharedPreferences sharedPref = ac.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("default_device_IP", d.IP);
        editor.putString("default_device_name", d.name);
        editor.putInt("default_device_type", d.type);
        editor.putString("default_device_data", d.data);
        editor.apply();
    }
}
