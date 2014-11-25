package com.manvir.SnapColors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class ColorPicker extends RelativeLayout{
    OnColorSelectedListener colorSelectedListener;
    public ColorPicker(Context context) {
        super(context);
        setBackgroundDrawable(App.modRes.getDrawable(R.drawable.bgviewdraw));
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.addView(inflater.inflate(App.modRes.getLayout(R.layout.color_picker), null));

        ImageButton colorRed = (ImageButton)findViewById(R.id.colorRed);
        colorRed.setImageDrawable(App.modRes.getDrawable(R.drawable.roundcorner));
        colorRed.setBackgroundDrawable(makeBitmapFromColor(Color.RED));
        colorRed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                colorSelectedListener.OnSelected(Color.RED);
            }
        });

        Button btnDone = (Button)findViewById(R.id.done);
        btnDone.getLayoutParams().width = (App.size.x/2)-40;
        btnDone.setBackgroundDrawable(App.modRes.getDrawable(R.drawable.roundcorner));
        btnDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                colorSelectedListener.OnDone();
            }
        });

        Button btnCancel = (Button)findViewById(R.id.cancel);
        btnCancel.getLayoutParams().width = (App.size.x/2)-40;
        btnCancel.setBackgroundDrawable(App.modRes.getDrawable(R.drawable.roundcorner));
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                colorSelectedListener.onCancel();
            }
        });
    }

    public Drawable makeBitmapFromColor(int Color){
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color);

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 30;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return new BitmapDrawable(output);
    }

    public void OnSelected(OnColorSelectedListener li){
        this.colorSelectedListener = li;
    }

    public void remove(){
        ((RelativeLayout) ColorPicker.this.getParent()).removeView(ColorPicker.this);
    }
}
