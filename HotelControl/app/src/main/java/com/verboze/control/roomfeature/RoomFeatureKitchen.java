package com.verboze.control.roomfeature;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.verboze.control.FoodMenuItem;
import com.verboze.control.Main;
import com.verboze.control.R;
import com.verboze.control.RoomControllerDevice;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by hasan on 7/14/17.
 */
public class RoomFeatureKitchen extends RoomFeature {
    View view = null;
    LinearLayout orderdrawer = null;
    LinearLayout orderList = null;
    LinearLayout foodsList = null;
    Button neworder = null;
    Button placeorder = null;
    String[] menu = null;
    RoomControllerDevice cur_device;
    String kitchen_thing_id = "";
    public String my_room_name = "";

    class OrderItem {
        public int count;
        public int food_id;
        public String name;
        public LinearLayout container;
        public ImageView status;
        public TextView text;

        public OrderItem(int fid, int c, String n) {
            final OrderItem me = this;
            count = c;
            food_id = fid;
            name = n;
            container = new LinearLayout(activity.getApplicationContext());
            container.setOrientation(LinearLayout.HORIZONTAL);
            orderList.addView(container);

            status = new ImageView(activity.getApplicationContext());
            status.setLayoutParams(new LinearLayout.LayoutParams(45, 45));
            status.setImageResource(R.drawable.cancel);
            status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orderList.removeView(me.container);
                    order.remove(me);
                }
            });
            container.addView(status);

            text = new TextView(activity.getApplicationContext());
            text.setTextSize(24.0f);
            container.addView(text);

            Update();
        }

        public void Update() {
            text.setText(Integer.toString(count) + "x " + menu[food_id]);
        }
    }

    ArrayList<OrderItem> order = null;

    public RoomFeatureKitchen(Main ac) {
        super(ac);
    }

    void AddToOrder(int id, int count, String name) {
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i).food_id == id) {
                order.get(i).count += count;
                order.get(i).Update();
                return;
            }
        }
        order.add(new OrderItem(id, count, name));
    }

    Button.OnClickListener orderListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == neworder) {
                orderList.removeAllViews();
                order.clear();
                placeorder.setEnabled(true);
                foodsList.setEnabled(true);
                for (int i = 0; i < foodsList.getChildCount(); i++) {
                    View child = foodsList.getChildAt(i);
                    child.setEnabled(true);
                }
                placeorder.setText("Place Order");
                if (activity.getResources().getConfiguration().locale.getLanguage().equals("ar"))
                    placeorder.setText("إرسال الطلب");
            } else if (v == placeorder) {
                if (order.size() > 0) {
                    try {
                        JSONArray order_json = new JSONArray();
                        for (int i = 0; i < order.size(); i++) {
                            JSONObject oi = new JSONObject();
                            oi.put("name", order.get(i).name);
                            oi.put("quantity", order.get(i).count);
                            order_json.put(oi);
                        }
                        JSONObject msg_json = new JSONObject();
                        msg_json.put("thing", kitchen_thing_id);
                        msg_json.put("order", order_json);
                        msg_json.put("placed_by_name", my_room_name);
                        //activity.communication_kitchen.addToQueue(msg_json.toString());

                        placeorder.setEnabled(false);
                        placeorder.setText("Order Has Been Sent");
                        if (activity.getResources().getConfiguration().locale.getLanguage().equals("ar"))
                            placeorder.setText("تم إرسال الطلب");
                        foodsList.setEnabled(false);
                        for (int i = 0; i < foodsList.getChildCount(); i++) {
                            FoodMenuItem child = (FoodMenuItem) foodsList.getChildAt(i);
                            child.setEnabled(false);
                        }
                        for (int i = 0; i < order.size(); i++) {
                            order.get(i).status.setImageResource(R.drawable.loading);
                            order.get(i).status.setOnClickListener(null);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    };

    private void on_kitchen_update(final JSONObject kitchen_obj) {
        if (menu == null) {
            try {
                JSONArray menu_array_obj = kitchen_obj.getJSONArray("menu");
                menu = new String[menu_array_obj.length()];
                for (int i = 0; i < menu_array_obj.length(); i++) {
                    menu[i] = menu_array_obj.getJSONObject(i).getString("name");
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        foodsList.removeAllViews();
                        for (int i = 0; i < menu.length; i++) {
                            FoodMenuItem it = new FoodMenuItem(activity.getApplicationContext());
                            it.Initialize(menu[i], new FoodMenuItem.Listener() {
                                @Override
                                public void OnOrder(String item, int count) {
                                    for (int i = 0; i < menu.length; i++) {
                                        if (menu[i].equals(item)) {
                                            AddToOrder(i, count, menu[i]);
                                            break;
                                        }
                                    }
                                }
                            });
                            foodsList.addView(it);
                        }
                    }
                });
            } catch (Exception e) {
                menu = null;
            }
        }

        try {
            JSONArray orders = kitchen_obj.getJSONArray("orders");
            for (int i = 0; i < orders.length(); i++) {
                JSONObject order_obj = orders.getJSONObject(i);
                if (order_obj.getString("placed_by_name").equals(my_room_name)) {
                    JSONArray items_obj = order_obj.getJSONArray("items");
                    for (int j = 0; j < items_obj.length(); j++) {
                        JSONObject item_obj = items_obj.getJSONObject(j);
                        String item_name = item_obj.getString("name");
                        final int status = item_obj.getInt("status");
                        if (status != -1) {
                            for (int k = 0; k < order.size(); k++) {
                                final OrderItem oi = order.get(k);
                                if (oi.name.equals(item_name)) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (status == 0)
                                                oi.status.setImageResource(R.drawable.rejected);
                                            else if (status == 1)
                                                oi.status.setImageResource(R.drawable.accepted);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public void onData(String json_str) {
        try {
            JSONObject obj = new JSONObject(json_str);

            if (obj.has("config") && (kitchen_thing_id == null || kitchen_thing_id.equals(""))) { // this is a config response!
                Iterator<String> thing_ids = obj.keys();
                while (thing_ids.hasNext()) {
                    try {
                        String thing_id = thing_ids.next();
                        JSONObject thing = obj.getJSONObject(thing_id);
                        if (thing.getString("category").equals("kitchen_controls")) {
                            kitchen_thing_id = thing_id;
                            on_kitchen_update(thing);
                            break;
                        }
                    } catch (Exception e) {}
                }
            }

            if (kitchen_thing_id != null && !kitchen_thing_id.equals("") && obj.has(kitchen_thing_id)) {
                JSONObject kitchen_obj = obj.getJSONObject(kitchen_thing_id);
                on_kitchen_update(kitchen_obj);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            if (kitchen_thing_id == null || kitchen_thing_id.equals("")) {
                JSONObject initial_fetch = new JSONObject();
                initial_fetch.put("code", 0); // will get things
                //activity.communication_kitchen.addToQueue(initial_fetch.toString());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void onConnected() {
        kitchen_thing_id = "";
        menu = null;
        try {
            JSONObject initial_fetch = new JSONObject();
            initial_fetch.put("code", 0); // will get things
            //activity.communication_kitchen.addToQueue(initial_fetch.toString());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void onDisconnected() {
    }

    @Override
    public void reInit(String cur_lang) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            view = inflater.inflate(R.layout.kitchen, null);

            neworder = (Button) view.findViewById(R.id.neworder);
            neworder.setOnClickListener(orderListener);
            placeorder = (Button) view.findViewById(R.id.placeorder);
            placeorder.setOnClickListener(orderListener);

            orderdrawer = (LinearLayout) view.findViewById(R.id.orderdrawer);
            orderList = (LinearLayout) view.findViewById(R.id.currentorders);
            foodsList = (LinearLayout) view.findViewById(R.id.availablefoods);

            (view.findViewById(R.id.orderstoggle)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (orderdrawer.isShown()) {
                        orderdrawer.setVisibility(View.GONE);
                        ((Button) v).setText("Show Order");
                    } else {
                        orderdrawer.setVisibility(View.VISIBLE);
                        ((Button) v).setText("Hide Order");
                    }
                }
            });

            order = new ArrayList<OrderItem>();
        }
    }

    @Override
    public void onClick(RoomControllerDevice cd, ViewGroup main_tab) {
        cur_device = cd;
        main_tab.addView(view);
    }
}
