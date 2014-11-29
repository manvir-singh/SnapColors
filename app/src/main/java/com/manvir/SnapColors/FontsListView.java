package com.manvir.SnapColors;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;

public class FontsListView extends RelativeLayout {
    public FontsListView(Context context, final Typeface typefaceDef) {
        super(context);
        setBackgroundDrawable(App.modRes.getDrawable(R.drawable.bgviewdraw));

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.addView(inflater.inflate(App.modRes.getLayout(R.layout.fonts_list_view), null));

        ScrollView scrollView = (ScrollView)findViewById(R.id.ScrollViewFontsList);
        scrollView.setLayoutParams(new LayoutParams(App.size.x, LayoutParams.WRAP_CONTENT));
        scrollView.setPersistentDrawingCache(ViewGroup.PERSISTENT_ALL_CACHES);
        scrollView.setAlwaysDrawnWithCacheEnabled(true);

        Button btnCancel = (Button)findViewById(R.id.cancel);
        btnCancel.setBackgroundDrawable(App.modRes.getDrawable(R.drawable.roundcorner));
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RelativeLayout) FontsListView.this.getParent()).removeView(FontsListView.this);
                App.editText.setTypeface(typefaceDef);
            }
        });

        final String fontsDir = context.getExternalFilesDir(null).getAbsolutePath();
        File file[] = new File(fontsDir).listFiles();
        for (File aFile : file) {
            String fontName = aFile.getName().replace(".ttf", "").replace(".TTF", "");
            Typeface face = Typeface.createFromFile(fontsDir + "/" + fontName + ".ttf");
            TextView v = new TextView(context);
            v.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            v.setTypeface(face);
            v.setTextSize(20);
            v.setText(fontName);
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fontName = ((TextView)v).getText().toString();
                    Typeface face = Typeface.createFromFile(fontsDir+ "/" + fontName+".ttf");
                    App.editText.setTypeface(face);
                    ((RelativeLayout) FontsListView.this.getParent()).removeView(FontsListView.this);
                }
            });
            ((LinearLayout)findViewById(R.id.fontsmainLayout)).addView(v);
        }
    }
}
