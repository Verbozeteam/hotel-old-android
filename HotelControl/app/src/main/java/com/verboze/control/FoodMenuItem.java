package com.verboze.control;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class FoodMenuItem extends LinearLayout {
    public String itemName;
    TextView countText;

    public interface Listener {
        void OnOrder(String item, int count);
    };

    public FoodMenuItem(Context context) {
        super(context);
    }

    public FoodMenuItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FoodMenuItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void Initialize(String itName, Listener l) {
        itemName = itName;
        this.setOrientation(HORIZONTAL);
        this.setWeightSum(10.0f);
        this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 80));
        //this.setBackgroundResource(R.drawable.border);
        TextView text = new TextView(getContext());
        text.setLayoutParams(new LinearLayout.LayoutParams(500, LayoutParams.MATCH_PARENT, 5.0f));
        text.setText(itemName);
        text.setTextSize(30.0f);
        this.addView(text);
        LinearLayout actions = new LinearLayout(getContext());
        actions.setLayoutParams(new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT, 5.0f));
        this.addView(actions);

        ImageButton less = new ImageButton(getContext());
        less.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
        less.setBackgroundResource(R.drawable.downarrow);
        less.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = Integer.parseInt(countText.getText().toString().substring(3));
                if (val > 1)
                    val -= 1;
                countText.setText(" x " + Integer.toString(val));
            }
        });
        actions.addView(less);

        ImageButton more = new ImageButton(getContext());
        LinearLayout.LayoutParams lps_more = new LinearLayout.LayoutParams(50, 50);
        lps_more.setMargins(5, 0, 0, 0);
        more.setLayoutParams(lps_more);
        more.setBackgroundResource(R.drawable.uparrow);
        more.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = Integer.parseInt(countText.getText().toString().substring(3));
                if (val < 255)
                    val += 1;
                countText.setText(" x " + Integer.toString(val));
            }
        });
        actions.addView(more);

        countText = new TextView(getContext());
        countText.setTextSize(30.0f);
        countText.setText(" x 1");
        actions.addView(countText);

        Button order = new Button(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(20, 0, 0, 0);
        order.setLayoutParams(lp);
        order.setText("Order");
        if (getResources().getConfiguration().locale.getLanguage().equals("ar"))
            order.setText("اطلب");
        final String it = itName;
        final Listener listener = l;
        order.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = Integer.parseInt(countText.getText().toString().substring(3));
                countText.setText(" x 1");
                listener.OnOrder(it, val);
            }
        });
        actions.addView(order);
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (int i = 0; i < ((LinearLayout)getChildAt(1)).getChildCount(); i++) {
            ((LinearLayout)getChildAt(1)).getChildAt(i).setEnabled(enabled);
        }
    }
}
