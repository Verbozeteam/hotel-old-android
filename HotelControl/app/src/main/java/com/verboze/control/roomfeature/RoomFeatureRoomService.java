package com.verboze.control.roomfeature;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.verboze.control.LightButton;
import com.verboze.control.Main;
import com.verboze.control.R;
import com.verboze.control.RoomControllerDevice;

/**
 * Created by hasan on 7/14/17.
 */
public class RoomFeatureRoomService extends RoomFeature {
    View view = null;
    LightButton[] buttons = null;
    TextView[] texts = null;
    long[] timers = null;

    private void SetButtonState(int i, boolean isOn) {
        buttons[i].setState(isOn);
        activity.communication.addToQueue("s" + Integer.toString(i) + ":" + (isOn ? "1" : "0") + "\n");
        if (isOn)
            texts[i].setTextColor(activity.getResources().getColor(R.color.colorWhite));
        else
            texts[i].setTextColor(activity.getResources().getColor(R.color.colorBackground));
        timers[i] = System.currentTimeMillis();
        return;
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < buttons.length; i++) {
                if (v == buttons[i]) {
                    SetButtonState(i, !buttons[i].isOn());
                    // make sure we dont have both DND and room service
                    if (buttons[i].isOn()) // if this is turned on, force the other to turn off
                        SetButtonState(1-i, false);
                }
            }
        }
    };

    public RoomFeatureRoomService(Main ac) {
        super(ac);
    }

    @Override
    public void reInit(String cur_lang) {
        if (buttons == null) {
            buttons = new LightButton[2];
            for (int i = 0; i < 2; i++)
                buttons[i] = null;
            texts = new TextView[2];
            for (int i = 0; i < 2; i++)
                texts[i] = null;
        }
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            view = inflater.inflate(R.layout.service, null);
            int[] ids = {R.id.service1, R.id.service2};
            int[] tids = {R.id.textView8, R.id.textView9};

            for (int i = 0; i < 2; i++) {
                if (buttons[i] == null) {
                    buttons[i] = (LightButton) view.findViewById(ids[i]);
                    buttons[i].setOnClickListener(buttonListener);
                }
                if (texts[i] == null)
                    texts[i] = (TextView)view.findViewById(tids[i]);
            }
            buttons[0].reson = R.drawable.cardon;
            buttons[0].resoff = R.drawable.card;
            buttons[1].reson = R.drawable.cardon;
            buttons[1].resoff = R.drawable.card;
        }
        if (timers == null) {
            timers = new long[2];
        }
    }

    @Override
    public void onClick(RoomControllerDevice cur_device, ViewGroup main_tab) {
        main_tab.addView(view);
    }

    @Override
    public void onServerData(int[] switchstates, int[] nothing) {
        long curTime = System.currentTimeMillis();
        for (int i = 0; i < Math.min(2, switchstates.length); i++) {
            if (curTime - timers[i] > 2000) {
                buttons[i].setState(switchstates[i] == 1);
            }
        }
    }
}
