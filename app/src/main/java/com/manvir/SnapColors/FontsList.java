package com.manvir.SnapColors;

import android.app.ListActivity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

public class FontsList extends ListActivity implements AbsListView.OnScrollListener{
    TextView text;
    ListView listView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.select_dialog_singlechoice){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    text = (TextView) view.findViewById(android.R.id.text1);
                    text.setTextColor(Color.BLACK);
                    return view;
                }
            };
            final String fontsDir = this.createPackageContext("com.snapchat.android", 0).getExternalFilesDir(null).getAbsolutePath();
            File file[] = new File(fontsDir).listFiles();
            for (File aFile : file) {
                arrayAdapter.add(aFile.getName().replace(".ttf", "").replace(".TTF", ""));
            }
            this.setListAdapter(arrayAdapter);
            this.listView = getListView();
            getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
                boolean firstTime = true;
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {}
                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if(firstTime){
                        ListView lv = listView;
                        int childCount = lv.getChildCount();
                        for (int i = 0; i < childCount; i++)
                        {
                            TextView v = (TextView)lv.getChildAt(i);
                            String strName = v.getText().toString();
                            Typeface typeface = Typeface.createFromFile(fontsDir+ "/" + strName+".ttf");
                            v.setTypeface(typeface);
                        }
                        firstTime = false;
                    }else {
                        firstTime = true;
                    }

                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
