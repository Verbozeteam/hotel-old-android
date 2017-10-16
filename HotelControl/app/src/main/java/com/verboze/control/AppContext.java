package com.verboze.control;


import android.app.ActionBar;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import java.io.DataOutputStream;

public class AppContext extends Application {

    private AppContext instance;
    private PowerManager.WakeLock wakeLock;
    private OnScreenOffReceiver onScreenOffReceiver;


    private void startKioskService() { // ... and this method
        startService(new Intent(this, KioskService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        registerKioskModeScreenOffReceiver();
        startKioskService();  // add this

        Process proc = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            String ProcID = "79"; //HONEYCOMB AND OLDER

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                ProcID = "42"; //ICS AND NEWER

            try {
                proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "service call activity " + ProcID + " s16 com.android.systemui"});
            } catch (Exception e) {
                Log.w("Main", "Failed to kill task bar (1).");
                e.printStackTrace();
            }
            try {
                proc.waitFor();
            } catch (Exception e) {
                Log.w("Main", "Failed to kill task bar (2).");
                e.printStackTrace();
            }
        } else {
            // EDIT Build.prop file and add qemu.hw.mainkeys=1
        }
    }

    private void registerKioskModeScreenOffReceiver() {
        // register screen off receiver
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        onScreenOffReceiver = new OnScreenOffReceiver();
        registerReceiver(onScreenOffReceiver, filter);
    }

    public PowerManager.WakeLock getWakeLock() {
        if(wakeLock == null) {
            // lazy loading: first call, create wakeLock via PowerManager.
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakeup");
        }
        return wakeLock;
    }
}
