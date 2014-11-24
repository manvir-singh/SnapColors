package com.manvir.SnapColors;

import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.drawable.Drawable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SButton extends ImageButton{

    /**
     * @param context The application context.
    */
    public SButton(Context context, int btnImageId, RelativeLayout ly, int leftMargin) {
        super(context);
        //Setup our params for our ImageButton.
        RelativeLayout.LayoutParams btnParmas = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        btnParmas.leftMargin = leftMargin;
        btnParmas.topMargin = 50;
        btnParmas.bottomMargin = 50;
        btnParmas.width = 100;
        btnParmas.height = 100;
        setBackgroundDrawable(App.modRes.getDrawable(R.drawable.roundcorner));//Set the background drawable.
        setImageDrawable(App.modRes.getDrawable(btnImageId));// Set ImageButton drawable.
        setScaleType(ImageView.ScaleType.CENTER_INSIDE); //Fit image into view regardless of image size.
        ly.addView(this, btnParmas);
    }
}
