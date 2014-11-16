package com.manvir.SnapColors;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;

public class Picker extends View{

	public Picker(Context context) {
		super(context);
		
		RelativeLayout.LayoutParams ly = new RelativeLayout.LayoutParams(10, LayoutParams.MATCH_PARENT);
		ly.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

		setLayoutParams(ly);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

	}

}
