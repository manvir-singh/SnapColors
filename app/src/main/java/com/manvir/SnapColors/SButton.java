package com.manvir.SnapColors;

import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.drawable.Drawable;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

/**
 * Created by Manvir on 11/16/2014.
 */
public class SButton extends ImageButton{
    XModuleResources modRes = App.modRes;

    public SButton(Context context, int btnImageId, RelativeLayout ly, int topMargin, int leftMargin) {
        super(context);

        RelativeLayout.LayoutParams btnParmas = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        btnParmas.leftMargin = leftMargin;
        btnParmas.topMargin = topMargin;
        btnParmas.width = 100;
        btnParmas.height = 100;

        setBackgroundDrawable(modRes.getDrawable(R.drawable.roundcorner));
        setImageDrawable(modRes.getDrawable(btnImageId));
        ly.addView(this, btnParmas);
    }
}
