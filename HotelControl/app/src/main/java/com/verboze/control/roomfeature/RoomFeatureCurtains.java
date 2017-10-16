package com.verboze.control.roomfeature;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.verboze.control.Main;
import com.verboze.control.R;
import com.verboze.control.RoomControllerDevice;

/**
 * Created by hasan on 7/14/17.
 */
public class RoomFeatureCurtains extends RoomFeature {
    View view = null;
    ImageView[] buttons = null;
    ImageView[] icons = null;

    ImageView.OnTouchListener listener = new ImageView.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent motion) {
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i] == v) {
                    switch (motion.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            if (i % 2 == 0) { // up arrow
                                buttons[i].setImageResource(R.drawable.uparrowglow);
                                activity.communication.addToQueue("c" + Integer.toString(i / 2) + ":1\n");
                            } else {
                                buttons[i].setImageResource(R.drawable.downarrowglow);
                                activity.communication.addToQueue("c" + Integer.toString(i / 2) + ":2\n");
                            }
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP: {
                            if (i % 2 == 0) { // up arrow
                                buttons[i].setImageResource(R.drawable.uparrow);
                            } else {
                                buttons[i].setImageResource(R.drawable.downarrow);
                            }
                            activity.communication.addToQueue("c" + Integer.toString(i / 2) + ":0\n");
                            break;
                        }
                    }
                    break;
                }
            }
            return true;
        }
    };

    public RoomFeatureCurtains(Main ac) {
        super(ac);
    }

    @Override
    public void reInit(String cur_lang) {
        if (buttons == null) {
            buttons = new ImageView[6];
        }
        if (icons == null) {
            icons = new ImageView[3];
        }
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            view = inflater.inflate(R.layout.curtains, null);
            int[] ids = {R.id.curtainup1, R.id.curtaindown1,
                    R.id.curtainup2, R.id.curtaindown2,
                    R.id.curtainup3, R.id.curtaindown3};
            icons[0] = (ImageView) view.findViewById(R.id.curtain_icon1);
            icons[1] = (ImageView) view.findViewById(R.id.curtain_icon2);
            icons[2] = (ImageView) view.findViewById(R.id.curtain_icon3);
            for (int i = 0; i < 6; i++) {
                buttons[i] = (ImageView) view.findViewById(ids[i]);
                buttons[i].setOnTouchListener(listener);
            }
        }
    }

    @Override
    public void onClick(RoomControllerDevice cur_device, ViewGroup main_tab) {
        int num_curtains = 3;
        if (cur_device.data.length() == 4) {
            num_curtains = Character.getNumericValue(cur_device.data.charAt(3));
        }
        for (int i = 0; i < 6; i++)
            buttons[i].setVisibility(i < num_curtains * 2 ? View.VISIBLE : View.INVISIBLE);
        for (int i = 0; i < 3; i++)
            icons[i].setVisibility(i < num_curtains ? View.VISIBLE : View.INVISIBLE);
        main_tab.addView(view);
    }
}
