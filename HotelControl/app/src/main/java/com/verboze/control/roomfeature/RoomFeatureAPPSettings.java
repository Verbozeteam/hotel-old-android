package com.verboze.control.roomfeature;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.verboze.control.Main;
import com.verboze.control.R;
import com.verboze.control.RoomControllerDevice;

/**
 * Created by hasan on 7/14/17.
 */
public class RoomFeatureAPPSettings extends RoomFeature {
    LinearLayout view = null;
    Spinner spinner = null;
    int sel = 0;

    public RoomFeatureAPPSettings(Main ac) {
        super(ac);
    }

    @Override
    public void reInit(String cur_lang) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            view = (LinearLayout)inflater.inflate(R.layout.appsettings, null);
        }
        if (spinner == null) {
            spinner = (Spinner)view.findViewById(R.id.language_spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                    R.array.language_list, R.layout.spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinner.setAdapter(adapter);
            sel = 0;
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (sel != position) {
                        if (position == 0) {
                            activity.SetLocale("en");
                        } else if (position == 1) {
                            activity.SetLocale("ar");
                        }
                    }
                    sel = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (cur_lang.equals("ar")) {
                sel = 1;
                spinner.setSelection(1);
            }
        }
    }

    @Override
    public void onClick(RoomControllerDevice cur_device, ViewGroup main_tab) {
        main_tab.addView(view);
    }
}
