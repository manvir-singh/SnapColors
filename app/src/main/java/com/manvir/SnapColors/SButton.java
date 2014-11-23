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
    public SButton(Context context, int btnImageId, RelativeLayout ly, int topMargin, int leftMargin) {
        super(context);
        //Setup our params for our ImageButton.
        RelativeLayout.LayoutParams btnParmas = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        btnParmas.leftMargin = leftMargin;
        btnParmas.topMargin = topMargin;
        btnParmas.width = 100;
        btnParmas.height = 100;
        setBackgroundDrawable(App.modRes.getDrawable(R.drawable.roundcorner));//Set the background drawable
        setImageDrawable(App.modRes.getDrawable(btnImageId));// Set ImageButton drawable.
        //Set the scale so the image fits into the ImageButton no matter the size of the image.
        setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ly.addView(this, btnParmas);
    }
}
