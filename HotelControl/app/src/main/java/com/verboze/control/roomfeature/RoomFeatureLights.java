package com.verboze.control.roomfeature;

import android.app.Activity;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.verboze.control.LightButton;
import com.verboze.control.Main;
import com.verboze.control.R;
import com.verboze.control.RoomControllerDevice;


/**
 * Created by hasan on 7/14/17.
 */
public class RoomFeatureLights extends RoomFeature {
    View view = null;
    LightButton[] switches = null;
    ImageView[] seekIcons = null;
    TextView[] seekTexts = null;
    SeekBar[] sliders = null;
    LightButton allToggle = null;
    long[] timers = null;
    int[] rememberedLights;
    int[] rememberedDimmers;

    /** switches will send tn-tm (t0-t4 for example), this is the base index to start from */
    protected int base_switch_sending_index;
    /** dimmers will send ln-lm (l0-l2 for example), this is the base index to start from */
    protected int base_dimmer_sending_index;
    /** number of switches to display */
    protected int num_lights;
    /** number of dimmers to display */
    protected int num_dimmers;
    /** IDs of switch views (all of which CAN be displayed, according to num_lights) */
    protected int[] switch_ids;
    /** IDs of dimmer views (all of which CAN be displayed, according to num_dimmers) */
    protected int[] dimmer_ids;
    /** IDs of dimmer icon views (all of which CAN be displayed, according to num_dimmers) */
    protected int[] dimmer_icon_ids;
    /** IDs of dimmer text views (all of which CAN be displayed, according to num_dimmers) */
    protected int[] dimmer_text_ids;
    /** texts to make left-to-right on arabic */
    protected int[] ltr_texts;
    /** base resource to inflate */
    protected int base_resource;

    void UpdateAllSwitch() {
        boolean allOff1 = true;
        for (int i = 0; i < switches.length && allOff1; i++)
            if (switches[i].isOn())
                allOff1 = false;
        for (int i = 0; i < sliders.length && allOff1; i++)
            if (sliders[i].getProgress() > 0)
                allOff1 = false;

        if (!allOff1 && !allToggle.isOn())
            allToggle.setState(true);
        else if (allOff1 && allToggle.isOn())
            allToggle.setState(false);
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < switches.length; i++) {
                if (v == switches[i]) {
                    switches[i].toggle();
                    boolean val = switches[i].isOn();
                    int port = base_switch_sending_index + i;
                    activity.communication.addToQueue("t" + Integer.toString(port) + ":" + (val ? "1" : "0") + "\n");
                    timers[i] = System.currentTimeMillis();

                    UpdateAllSwitch();
                    return;
                }
            }
            if (v == allToggle) {
                allToggle.toggle();
                if (allToggle.isOn()) {
                    // boot up all remembered state
                    for (int i = 0; i < switches.length; i++) {
                        switches[i].setState(rememberedLights[i] != 0);
                        int port = base_switch_sending_index + i;
                        activity.communication.addToQueue("t" + Integer.toString(port) + ":" + Integer.toString(rememberedLights[i]) + "\n");
                        timers[i] = System.currentTimeMillis();
                    }
                    for (int i = 0; i < sliders.length; i++) {
                        sliders[i].setProgress(rememberedDimmers[i]);
                        int port = base_dimmer_sending_index + i;
                        activity.communication.addToQueue("l" + Integer.toString(port) + ":" + Integer.toString(rememberedDimmers[i]) + "\n");
                        seekIcons[i].setAlpha((float) rememberedDimmers[i] / 150.0f + 0.333f);
                        timers[i + switch_ids.length] = System.currentTimeMillis();
                    }
                } else {
                    // turn off all lights
                    for (int i = 0; i < switches.length; i++) {
                        rememberedLights[i] = switches[i].isOn() ? 1 : 0;
                        switches[i].setState(false);
                        int port = base_switch_sending_index + i;
                        activity.communication.addToQueue("t" + Integer.toString(port) + ":0\n");
                        timers[i] = System.currentTimeMillis();
                    }
                    for (int i = 0; i < sliders.length; i++) {
                        rememberedDimmers[i] = sliders[i].getProgress();
                        sliders[i].setProgress(0);
                        int port = base_dimmer_sending_index + i;
                        activity.communication.addToQueue("l" + Integer.toString(port) + ":0\n");
                        seekIcons[i].setAlpha((float) 0 / 150.0f + 0.333f);
                        timers[i + switch_ids.length] = System.currentTimeMillis();
                    }
                }
            }
        }
    };

    SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        private void UpdateProgress(SeekBar seekBar) {
            for (int i = 0; i < sliders.length; i++) {
                if (seekBar == sliders[i]) {
                    int val = sliders[i].getProgress();
                    int port = base_dimmer_sending_index + i;
                    activity.communication.addToQueue("l" + Integer.toString(port) + ":" + Integer.toString(val) + "\n");
                    seekIcons[i].setAlpha((float) val / 150.0f + 0.333f);
                    timers[i + switch_ids.length] = System.currentTimeMillis();

                    UpdateAllSwitch();
                }
            }
        }
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser)
                return;
            UpdateProgress(seekBar);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            UpdateProgress(seekBar);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            UpdateProgress(seekBar);
        }
    };

    public RoomFeatureLights(Main ac) {
        super(ac);
        base_switch_sending_index = 0;
        base_dimmer_sending_index = 0;
        num_lights = 3;
        num_dimmers = 1;
        switch_ids = new int[] {R.id.light1, R.id.light2, R.id.light3, R.id.light4};
        dimmer_ids = new int[] {R.id.dimmer1, R.id.dimmer2};
        dimmer_icon_ids = new int[] {R.id.dimmer_icon1, R.id.dimmer_icon2};
        dimmer_text_ids = new int[] {R.id.textView5, R.id.textView6};
        ltr_texts = new int[] {R.id.textView3};
        base_resource = R.layout.lights;
    }

    @Override
    public void reInit(String cur_lang) {
        if (switches == null) {
            switches = new LightButton[switch_ids.length];
            for (int i = 0; i < switches.length; i++)
                switches[i] = null;
        }
        if (sliders == null) {
            sliders = new SeekBar[dimmer_ids.length];
            for (int i = 0; i < sliders.length; i++)
                sliders[i] = null;
        }
        if (seekIcons == null) {
            seekIcons = new ImageView[dimmer_ids.length];
            for (int i = 0; i < seekIcons.length; i++)
                seekIcons[i] = null;
        }
        if (seekTexts == null) {
            seekTexts = new TextView[dimmer_text_ids.length];
            for (int i = 0; i < seekTexts.length; i++)
                seekTexts[i] = null;
        }
        if (rememberedLights == null) {
            rememberedLights = new int[switches.length];
            rememberedDimmers = new int[sliders.length];
        }
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            view = inflater.inflate(base_resource, null);

            for (int i = 0; i < switch_ids.length; i++) {
                if (switches[i] == null) {
                    switches[i] = (LightButton) view.findViewById(switch_ids[i]);
                    switches[i].setOnClickListener(buttonListener);
                }
            }

            for (int i = 0; i < dimmer_ids.length; i++) {
                if (sliders[i] == null) {
                    sliders[i] = (SeekBar) view.findViewById(dimmer_ids[i]);
                    sliders[i].setOnSeekBarChangeListener(seekListener);
                }
                if (seekIcons[i] == null) {
                    seekIcons[i] = (ImageView) view.findViewById(dimmer_icon_ids[i]);
                    seekIcons[i].setAlpha(0.333f);
                }
                if (seekTexts[i] == null)
                    seekTexts[i] = (TextView) view.findViewById(dimmer_text_ids[i]);
            }

            allToggle = (LightButton) view.findViewById(R.id.light5);
            allToggle.setOnClickListener(buttonListener);

            if (cur_lang.equals("ar")) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.RIGHT;
                params.rightMargin = 20;
                for (int i = 0; i < ltr_texts.length; i++)
                    ((TextView)view.findViewById(ltr_texts[i])).setLayoutParams(params);
            }
        }
        if (timers == null) {
            timers = new long[switch_ids.length + dimmer_ids.length];
        }
    }

    @Override
    public void onClick(RoomControllerDevice cur_device, ViewGroup main_tab) {
//        if (cur_device.data.length() == 4) {
//            num_lights = Character.getNumericValue(cur_device.data.charAt(1));
//            num_dimmers = Character.getNumericValue(cur_device.data.charAt(2));
//        }
        for (int i = 0; i < switch_ids.length; i++)
            switches[i].setVisibility(i < num_lights ? View.VISIBLE : View.INVISIBLE);
        for (int i = 0; i < dimmer_ids.length; i++) {
            sliders[i].setVisibility(i < num_dimmers ? View.VISIBLE : View.INVISIBLE);
            seekIcons[i].setVisibility(i < num_dimmers ? View.VISIBLE : View.INVISIBLE);
            seekTexts[i].setVisibility(i < num_dimmers ? View.VISIBLE : View.INVISIBLE);
        }
        main_tab.addView(view);
    }

    @Override
    public void onServerData(int[] lights, int[] dimmers) {
        long curTime = System.currentTimeMillis();
        for (int i = base_switch_sending_index; i < Math.min(base_switch_sending_index + switch_ids.length, lights.length); i++) {
            int index = i - base_switch_sending_index;
            if (curTime - timers[index] > 2000) {
                switches[index].setState(lights[i] == 1);
            }
        }
        for (int i = base_dimmer_sending_index; i < Math.min(base_dimmer_sending_index + dimmer_ids.length, dimmers.length); i++) {
            int index = i - base_dimmer_sending_index;
            if (curTime - timers[switch_ids.length + index] > 2000) {
                sliders[index].setProgress(dimmers[i]);
                seekIcons[index].setAlpha((float) dimmers[i] / 150.0f + 0.333f);
                UpdateAllSwitch();
            }
        }
    }
}