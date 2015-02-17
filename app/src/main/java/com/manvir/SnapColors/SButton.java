package com.manvir.SnapColors;

import android.content.Context;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class SButton extends ImageButton{
    public SButton(Context context, int btnImageId, RelativeLayout ly, int leftMargin) {
        super(context);
        //Setup our params for our ImageButton.
        int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getContext().getResources().getDisplayMetrics());
        int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getContext().getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams btnParmas = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        btnParmas.leftMargin = leftMargin;
        btnParmas.width = widthPx;
        btnParmas.height = heightPx;
        setBackgroundDrawable(App.modRes.getDrawable(R.drawable.roundcorner));//Set the background drawable.
        setImageDrawable(App.modRes.getDrawable(btnImageId));// Set ImageButton drawable.
        setScaleType(ScaleType.CENTER_CROP); //Fit image into view regardless of image size. CENTER_CROP CENTER
        ly.addView(this, btnParmas);
    }
}
