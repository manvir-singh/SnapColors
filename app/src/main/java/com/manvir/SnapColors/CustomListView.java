package com.manvir.SnapColors;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class CustomListView {
    private final ListView listView;
    private final ViewGroup recyclerView;
    private final Context con;

    public CustomListView(Context con, ListView listView, ViewGroup recyclerView) {
        this.con = con;
        this.listView = listView;
        this.recyclerView = recyclerView;
    }

    public void addFooterView(View v){
//        if (listView == null) {
//            recyclerView.addView(v);
//        } else {
//            listView.addFooterView(v);
//        }
    }

    public void removeFooterView(View v) {
//        if (listView == null) {
//            recyclerView.removeView(v);
//        } else {
//            listView.removeFooterView(v);
//        }
    }
}
