package com.verboze.control;

import com.automation.CommunicationManager;

import com.verboze.control.roomfeature.RoomFeature;
import com.verboze.control.roomfeature.RoomFeatureAC;
import com.verboze.control.roomfeature.RoomFeatureAPPSettings;
import com.verboze.control.roomfeature.RoomFeatureBathroom;
import com.verboze.control.roomfeature.RoomFeatureCurtains;
import com.verboze.control.roomfeature.RoomFeatureKitchen;
import com.verboze.control.roomfeature.RoomFeatureLights;
import com.verboze.control.roomfeature.RoomFeatureRoomService;
import com.verboze.control.roomfeature.RoomFeatureSettings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Main
        extends Activity
        implements ListView.OnItemClickListener, ImageView.OnLongClickListener {

    public boolean IS_TABLET = true;

    private LinearLayout main_tab;
    private RelativeLayout overlayer = null;
    private WebView ads = null;
    private ImageView settings_img;

    private RoomFeatureSettings settings;

    private ListView room;
    private ArrayAdapter<String> room_adapter;

    private ListView extra;
    private ArrayAdapter<String> extra_adapter;

    private RoomFeature cur_room_feature;
    private HashMap<String, RoomFeature> rooms;

    public CommunicationManager communication = null;
    long refresh_timer = 0;
    private String current_device_name = "";
    private Handler periodic_task_handler = null;

    private long screenDimTimer = 0;
    private long lastID = -1;

    public synchronized boolean setScreenDim(boolean bright) {
        if (bright) {
            screenDimTimer = System.currentTimeMillis();
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.screenBrightness = 1.0f;
            getWindow().setAttributes(params);
            if (overlayer != null) {
                boolean ret = overlayer.getVisibility() == View.VISIBLE;
                overlayer.setVisibility(View.INVISIBLE);
                return ret;
            }
        } else {
            long time = System.currentTimeMillis();
            long elapsed = time - screenDimTimer;
            long duration = 20 * 1000; // 20 seconds
            if (elapsed > duration) {
                boolean update_visibility = true;
                if (overlayer != null) {
                    if (overlayer.getVisibility() == View.VISIBLE)
                        update_visibility = false;
                }
                if (update_visibility) {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.screenBrightness = 0.0f;
                    getWindow().setAttributes(params);
                }
                if (overlayer != null) {
                    if (update_visibility) {
                        overlayer.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (sharedPref.contains("default_locale")) {
            String locale = sharedPref.getString("default_locale", "en");
            if (!locale.equals(getResources().getConfiguration().locale.getLanguage()))
                SetLocale(locale);
        }

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); // Hide the status bar

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (IS_TABLET)
            setContentView(R.layout.tablet);
        else
            setContentView(R.layout.phone);

        main_tab = (LinearLayout)findViewById(R.id.main_tab);
        overlayer = (RelativeLayout)findViewById(R.id.overlayer);
        overlayer.setVisibility(View.INVISIBLE);
        ((DimmerLayout)findViewById(R.id.overall)).setMain(this);
        screenDimTimer = System.currentTimeMillis();
        periodic_task_handler = new Handler();
        periodic_task_handler .postDelayed(new Runnable() {
            public void run() {
                setScreenDim(false);

                CommunicationManager.DiscoverDevices(new CommunicationManager.DeviceDiscoveryCallback() {
                    @Override
                    public void onDeviceFound(final String addr, final String text, final int type, final String data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (text.equals(current_device_name))
                                    communication.SetServerIP(addr);
                            }
                        });
                    }
                });

                periodic_task_handler .postDelayed(this, 5000);
            }
        }, 5000);

        SetupRoom();

        communication = CommunicationManager.Create("Writer", new CommunicationManager.ServerDataCallback() {
            @Override
            public void onServerData(final int[] arg1, final int[] arg2, final int[] arg3, final int[] arg4, final int[] arg5) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RoomFeature f;
                        if (arg1 != null && arg2 != null) {
                            f = rooms.get("Lights");
                            if (f != null)
                                f.onServerData(arg1, arg2);
                            f = rooms.get("Bathroom");
                            if (f != null)
                                f.onServerData(arg1, arg2);
                            if (arg1.length >= 10) {
                                int[] switches = {arg1[6], arg1[7]};
                                f = rooms.get("Room Service");
                                if (f != null)
                                    f.onServerData(switches);
                            }
                        }
                        if (arg3 != null && arg4 != null && arg5 != null) {
                            f = rooms.get("AC");
                            if (f != null)
                                f.onServerData(arg3, arg4, arg5);
                        }
                    }
                });
            }
        });

        RoomControllerDevice d = RoomControllerDevice.GetSavedDevice(this);

        SetCurrentDevice(null);
        DiscoverRooms(); // will discover kitchen (if exists)

        if (d != null) {
            settings.cur_device = d;
            SetCurrentDevice(d);
            SetCurrentFeature(rooms.get(GetEnglishFromLocale(room.getItemAtPosition(0).toString())));
            room.requestFocusFromTouch();
            room.setSelection(0);
            room.setItemChecked(0, true);
        }
    }

    @Override
    protected void onDestroy() {
        if (communication != null)
            communication.Stop();
        if (periodic_task_handler != null)
            periodic_task_handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public void SetCurrentDevice(RoomControllerDevice d) {
        room.setEnabled(false);
        extra.setEnabled(false);
        for (String k: rooms.keySet()) {
            RoomFeature f = rooms.get(k);
            f.reInit(getResources().getConfiguration().locale.getLanguage());
        }
        room.setEnabled(true);
        extra.setEnabled(true);

        if (d != null) {
            current_device_name = d.name;
            communication.SetServerIP(d.IP);
        }
    }

    private void SetCurrentFeature(RoomFeature feature) {
        main_tab.removeAllViews();
        cur_room_feature = feature;
        cur_room_feature.onClick(settings.cur_device, main_tab);
    }

    public String GetEnglishFromLocale(String str) {
        switch (str) {
            case "الستائر":
                return "Curtains";
            case "خدمة الغرفة":
                return "Room Service";
            case "المكيف":
                return "AC";
            case "الإضاءة":
                return "Lights";
            case "الحمام":
                return "Bathroom";
            case "الإعدادات":
                return "Settings";
            case "مطبخ":
            case "المطبخ":
                return "Kitchen";
        }
        return str;
    }

    public void SetLocale(String lang) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("default_locale", lang);
        editor.apply();

        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, Main.class);
        startActivity(refresh);
        finish();
    }

    private void clearListViewSelection(ListView lv, ArrayAdapter<String> adapter) {
        lv.setItemChecked(-1, false);
        lv.setAdapter(adapter);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == settings_img) {
            clearListViewSelection(room, room_adapter);
            clearListViewSelection(extra, extra_adapter);
            RoomFeature settings = rooms.get("Settings");
            settings.reInit(getResources().getConfiguration().locale.getLanguage());
            SetCurrentFeature(settings);
        }

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getAdapter() == room_adapter) {
            clearListViewSelection(extra, extra_adapter);
            String str = GetEnglishFromLocale(room.getItemAtPosition(position).toString());
            SetCurrentFeature(rooms.get(str));
        }
        else if(parent.getAdapter() == extra_adapter) {
            clearListViewSelection(room, room_adapter);
            String str = GetEnglishFromLocale(extra.getItemAtPosition(position).toString());
            if (str.equals("Settings")) {
                rooms.get("APPSettings").reInit(getResources().getConfiguration().locale.getLanguage());
                SetCurrentFeature(rooms.get("APPSettings"));
            } else {
                if (cur_room_feature != rooms.get("Kitchen")) {
                    RoomControllerDevice new_device = settings.available_devices.get(str);
                    for (String k : rooms.keySet()) {
                        RoomFeature f = rooms.get(k);
                        f.reInit(getResources().getConfiguration().locale.getLanguage());
                    }
                    RoomFeatureKitchen kitchen = (RoomFeatureKitchen)rooms.get("Kitchen");
                    kitchen.my_room_name = settings.cur_device.name;
                    SetCurrentFeature(kitchen);
                    //communication_kitchen.SetServerAddress(new_device.IP, 7990);
                }
            }
        }
    }

    private void SetupRoom() {
        settings_img = (ImageView)findViewById(R.id.settings_img);
        settings_img.setOnLongClickListener(this);

        room_adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.listviewentry,
                new ArrayList<String>());
        room = (ListView) findViewById(R.id.room_list);
        room.setAdapter(room_adapter);
        room.setOnItemClickListener(this);
        room.setChoiceMode(ListView.CHOICE_MODE_NONE);
        room.setEnabled(false);

        extra_adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.listviewentry,
                new ArrayList<String>());
        extra = (ListView) findViewById(R.id.extra_list);
        extra.setAdapter(extra_adapter);
        extra.setOnItemClickListener(this);
        extra.setChoiceMode(ListView.CHOICE_MODE_NONE);
        extra.setEnabled(false);

        cur_room_feature = null;
        rooms = new HashMap<>();

        settings = new RoomFeatureSettings(this);
        rooms.put("Settings", settings);
        extra_adapter.add(getString(R.string.settings));

        rooms.put("APPSettings", new RoomFeatureAPPSettings(this));

        //rooms.put("Kitchen", new RoomFeatureKitchen(this));

        rooms.put("Lights", new RoomFeatureLights(this));
        room_adapter.add(getString(R.string.lights));

        rooms.put("Bathroom", new RoomFeatureBathroom(this));
        room_adapter.add(getString(R.string.bathroom));

//        rooms.put("Curtains", new RoomFeatureCurtains(this));
//        room_adapter.add(getString(R.string.curtains));

        rooms.put("AC", new RoomFeatureAC(this));
        room_adapter.add(getString(R.string.acs));

        rooms.put("Room Service", new RoomFeatureRoomService(this));
        room_adapter.add(getString(R.string.room));
    }

    public void DiscoverRooms() {
        long curTime = System.currentTimeMillis();
        if (curTime - refresh_timer < 4000) {
            return;
        }
        refresh_timer = curTime;

        room.setEnabled(false);
        extra.setEnabled(false);
        settings.settings_adapter.clear();
        extra_adapter.clear();
        extra_adapter.add(getString(R.string.settings));
        settings.available_devices.clear();
        settings.cur_device = null;

        CommunicationManager.DiscoverDevices(new CommunicationManager.DeviceDiscoveryCallback() {
            @Override
            public void onDeviceFound(final String addr, final String text, final int type, final String data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RoomControllerDevice d = new RoomControllerDevice(addr, text, type, data);
                        if (type == 6) { // kitchen
                            // d.IP = "k" + d.IP; // k<IP> instead of <IP> DEPRICATED
                            String cur_lang = getResources().getConfiguration().locale.getLanguage();
                            if (text.toLowerCase().equals("kitchen") && cur_lang == "ar")
                                extra_adapter.insert("المطبخ", 0);
                            else
                                extra_adapter.insert(text, 0);
                        } else
                            settings.settings_adapter.add(text);
                        settings.available_devices.put(text, d);
                    }
                });
            }
        });
    }

    /**
     * DONT ALLOW ANY DIALOGUES
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }
    /**
     * BLOCKED KEYS
     */
    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (blockedKeys.contains(event.getKeyCode())) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }
    @Override
    public void onBackPressed() {
    }
}
