package com.verboze.control.roomfeature;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.verboze.control.Main;
import com.verboze.control.R;
import com.verboze.control.RoomControllerDevice;
import com.triggertrap.seekarc.SeekArc;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

/**
 * Created by hasan on 7/14/17.
 */
public class RoomFeatureAC extends RoomFeature {
    int num_acs = 1;
    int current_fan_speeds[] = null;
    View view = null;
    SeekArc arcs[] = null;
    TextView texts[] = null;
    TextView degTexts[] = null;
    TextView unitTexts[] = null;
    TextView room_texts[] = null;
    TextView room_texts_ar[] = null;
    ToggleButton acToggles[] = null;
    RadioButton radioButtons[][] = null;
    int lastSentVal[] = null;
    long timers[] = null;
    String cur_lang;

    SeekArc.OnSeekArcChangeListener seekListener = new SeekArc.OnSeekArcChangeListener() {
        private void SetAC(SeekArc ac, boolean sendMsg) {
            for (int i = 0; i < num_acs; i++) {
                if (arcs[i] == ac) {
                    double fVal = (1.0 - (float) arcs[i].getProgress() / 100.0f) * 16 + 16;
                    int val = (int) (fVal * 2.0f);
                    texts[i].setText(Double.toString((double)val / 2.0));
                    if (!sendMsg)
                        return;
                    if (val != lastSentVal[i]) {
                        activity.communication.addToQueue("a" + Integer.toString(i) + ":" + Integer.toString(val) + "\n");
                    }
                    lastSentVal[i] = val;
                    timers[i] = System.currentTimeMillis();
                }
            }
        }

        @Override
        public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
            SetAC(seekArc, fromUser);
        }

        @Override
        public void onStartTrackingTouch(SeekArc seekArc) {
            SetAC(seekArc, true);
        }

        @Override
        public void onStopTrackingTouch(SeekArc seekArc) {
            SetAC(seekArc, true);
        }
    };

    private void SetACToggle(int ac_index, int state) {
        if (state == 0) {
            arcs[ac_index].setEnabled(false);
            arcs[ac_index].setAlpha(0.2f);
            texts[ac_index].setAlpha(0.2f);
            degTexts[ac_index].setAlpha(0.2f);
            unitTexts[ac_index].setAlpha(0.2f);
            for (int i = 0; i < radioButtons[ac_index].length; i++)
                radioButtons[ac_index][i].setEnabled(false);
        } else {
            arcs[ac_index].setEnabled(true);
            arcs[ac_index].setAlpha(1.0f);
            texts[ac_index].setAlpha(1.0f);
            degTexts[ac_index].setAlpha(1.0f);
            unitTexts[ac_index].setAlpha(1.0f);
            for (int i = 0; i < radioButtons[ac_index].length; i++)
                radioButtons[ac_index][i].setEnabled(true);
        }
    }

    ToggleButton.OnClickListener clickListener = new ToggleButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < num_acs; i++) {
                if ((ToggleButton)v == acToggles[i]) {
                    int state = acToggles[i].isChecked() ? 1 : 0;
                    SetACToggle(i, state);
                    int fan_speed = state == 0 ? 0 : current_fan_speeds[i];
                    activity.communication.addToQueue("f" + Integer.toString(i) + ":" + Integer.toString(fan_speed) + "\n");
                    timers[i] = System.currentTimeMillis();
                }
            }
        }
    };

    RadioButton.OnClickListener radioListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            for (int i = 0; i < num_acs; i++) {
                for (int j = 0; j < radioButtons[i].length; j++) {
                    if (view == (View)radioButtons[i][j]) {
                        current_fan_speeds[i] = j + 1;

                        int state = acToggles[i].isChecked() ? 1 : 0;
                        int fan_speed = state == 0 ? 0 : current_fan_speeds[i];
                        activity.communication.addToQueue("f" + Integer.toString(i) + ":" + Integer.toString(fan_speed) + "\n");
                    }
                }
            }
        }
    };

    public RoomFeatureAC(Main ac) {
        super(ac);
    }

    @Override
    public void reInit(String cl) {
        cur_lang = cl;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            view = inflater.inflate(R.layout.acs, null);

            int IDs1[] = {R.id.ac1text};//, R.id.ac2text};
            int IDs2[] = {R.id.seekArc1};//, R.id.seekArc2};
            int IDs3[] = {R.id.roomtemp};
            int IDs4[] = {R.id.roomtemp_ar};
            int IDs5[] = {R.id.toggleButton};
            int IDs6[] = {R.id.textDegree};
            int IDs7[] = {R.id.textCelcius};
            int IDs8[][] = {{R.id.radioButton2, R.id.radioButton3}};

            current_fan_speeds = new int[num_acs];
            lastSentVal = new int[num_acs];
            texts = new TextView[num_acs];
            arcs = new SeekArc[num_acs];
            room_texts = new TextView[num_acs];
            room_texts_ar = new TextView[num_acs];
            acToggles = new ToggleButton[num_acs];
            degTexts = new TextView[num_acs];
            unitTexts = new TextView[num_acs];
            radioButtons = new RadioButton[num_acs][2];

            for (int i = 0; i < num_acs; i++) {
                lastSentVal[i] = 0;

                texts[i] = (TextView) view.findViewById(IDs1[i]);
                room_texts[i] = (TextView)view.findViewById(IDs3[i]);
                room_texts_ar[i] = (TextView)view.findViewById(IDs4[i]);
                degTexts[i] = (TextView)view.findViewById(IDs6[i]);
                unitTexts[i] = (TextView)view.findViewById(IDs7[i]);

                arcs[i] = (SeekArc) view.findViewById(IDs2[i]);
                arcs[i].setArcWidth(20);
                arcs[i].setProgressWidth(20);
                arcs[i].setRoundedEdges(true);
                arcs[i].setOnSeekArcChangeListener(seekListener);
                arcs[i].setProgress(0);

                acToggles[i] = (ToggleButton) view.findViewById(IDs5[i]);
                acToggles[i].setOnClickListener(clickListener);

                for (int j = 0; j < 2; j++) {
                    radioButtons[i][j] = (RadioButton) view.findViewById(IDs8[i][j]);
                    radioButtons[i][j].setOnClickListener(radioListener);
                }
                current_fan_speeds[i] = 1;
                radioButtons[i][0].setChecked(true);
            }
        }
        if (timers == null)
            timers = new long[num_acs];
    }

    @Override
    public void onClick(RoomControllerDevice cur_device, ViewGroup main_tab) {
        int actual_num_acs = 1;
        if (cur_device.data.length() == 4) {
            actual_num_acs = Character.getNumericValue(cur_device.data.charAt(0));
        }
        for (int i = 0; i < num_acs; i++) {
            arcs[i].setVisibility(i < actual_num_acs ? View.VISIBLE : View.INVISIBLE);
            arcs[i].setEnabled(true);
            texts[i].setVisibility(i < actual_num_acs ? View.VISIBLE : View.INVISIBLE);
        }
        if (cur_lang.equals("ar")) {
            for (int i = 0; i < num_acs; i++) {
                room_texts[i].setVisibility(View.INVISIBLE);
                room_texts_ar[i].setVisibility(View.VISIBLE);
            }
        } else {
            for (int i = 0; i < num_acs; i++) {
                room_texts[i].setVisibility(View.VISIBLE);
                room_texts_ar[i].setVisibility(View.INVISIBLE);
            }
        }
        main_tab.addView(view);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < num_acs; i++) {
                    arcs[i].setEnabled(acToggles[i].isChecked());
                }
            }
        }, 1000);
    }

    @Override
    public void onServerData(int[] setPTs, int[] fanSpeeds, int[] temps) {
        long curTime = System.currentTimeMillis();
        for (int i = 0; i < ((setPTs.length < num_acs) ? setPTs.length : num_acs); i++) {
            if (curTime - timers[i] > 4000) {
                int curProg = (int) (((1.0 - (float) arcs[i].getProgress() / 100.0f) * 16 + 16) * 2.0f);
                if (curProg != setPTs[i]) {
                    arcs[i].setProgress((int) ((1.0 - (float) ((float) setPTs[i] / 2.0 - 16) / 16.0) * 100.0));
                    texts[i].setText(Double.toString((double)setPTs[i] / 2.0));
                }
            }
            if (room_texts != null) {
                if (room_texts[i].getVisibility() == View.VISIBLE)
                    room_texts[i].setText(Integer.toString(temps[i]));
                else
                    room_texts[i].setText("");
                if (room_texts_ar[i].getVisibility() == View.VISIBLE)
                    room_texts_ar[i].setText(Integer.toString(temps[i]));
                else
                    room_texts_ar[i].setText("");
            }
        }

        for (int i = 0; i < ((fanSpeeds.length < num_acs) ? fanSpeeds.length : num_acs); i++) {
            if (curTime - timers[i] > 4000) {
                acToggles[i].setChecked(fanSpeeds[i] != 0);
                SetACToggle(i, fanSpeeds[i]);
            }
        }
    }
}
