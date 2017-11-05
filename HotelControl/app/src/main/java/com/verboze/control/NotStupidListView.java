package com.verboze.control;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class NotStupidListView extends LinearLayout {
    public interface OnItemSelectionChangeListener {
        public void OnItemSelectionChange(NotStupidListView view, int new_index);
    }

    private OnItemSelectionChangeListener m_sel_listener = null;
    private int m_cur_selection = -1;
    private ArrayList<TextView> m_items = new ArrayList<TextView>();

    private void setup() {
        setOrientation(LinearLayout.VERTICAL);
    }

    public NotStupidListView(Context context) {
        super(context);
        setup();
    }

    public NotStupidListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public NotStupidListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setItemSelection(int index, boolean selected) {
        if (selected)
            m_items.get(index).setBackgroundColor(getResources().getColor(R.color.colorBackground));
        else
            m_items.get(index).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    private void setSelectedItemFromView(View view) {
        if (!isEnabled())
            return;

        for (int i = 0; i < m_items.size(); i++) {
            if (view == m_items.get(i)) {
                setItemSelection(i, true);
                if (m_sel_listener != null && m_cur_selection != i)
                    m_sel_listener.OnItemSelectionChange(this, i);
                m_cur_selection = i;
            } else {
                setItemSelection(i, false);
            }
        }
    }

    public void addItem(String name) {
        insertItem(name, m_items.size());
    }

    public void insertItem(String name, int pos) {
        if (pos < 0) pos = 0;
        if (pos > m_items.size()) pos = m_items.size();

        TextView tv = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.listviewentry, null);
        tv.setText(name);
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedItemFromView(view);
            }
        });
        tv.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                setSelectedItemFromView(view);
                return false;
            }
        });
        addView(tv, pos);
        m_items.add(pos, tv);
    }

    public void clearItems() {
        removeAllViews();
        m_items.clear();
    }

    public void setSelection(int index) {
        for (int i = 0; i < m_items.size(); i++)
            setItemSelection(i, i == index);
        m_cur_selection = index;
    }

    public void clearSelection() {
        setSelection(-1);
    }

    public String getItem(int index) {
        if (index < m_items.size())
            return m_items.get(index).getText().toString();
        return "";
    }

    public void setOnSelectionChangeListener(OnItemSelectionChangeListener l) {
        m_sel_listener = l;
    }
}
