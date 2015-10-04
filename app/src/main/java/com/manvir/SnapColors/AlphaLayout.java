package com.manvir.SnapColors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

public class AlphaLayout extends LinearLayout implements SeekBar.OnSeekBarChangeListener {
    private final EditText editText;

    public AlphaLayout(Context context, final EditText editText, final HorizontalScrollView f, final ImageButton SnapColorsBtn) {
        super(context);
        this.editText = editText;
        f.setVisibility(View.GONE);
        SnapColorsBtn.setClickable(false);
        setClickable(true);
        setBackgroundDrawable(App.modRes.getDrawable(R.drawable.bgviewdraw));
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.addView(inflater.inflate(App.modRes.getLayout(R.layout.size_layout), null));
        SeekBar seekBarSize = (SeekBar) findViewById(R.id.seekBarSize);
        seekBarSize.setOnSeekBarChangeListener(this);
        seekBarSize.setMax(255);// So people don't go crazy with the Alpha.
        seekBarSize.setProgress(255);

        Button btnDone = (Button) findViewById(R.id.done);
        btnDone.getLayoutParams().width = (App.size.x / 2) - 40;
        btnDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RelativeLayout) AlphaLayout.this.getParent()).removeView(AlphaLayout.this);
                f.setVisibility(View.VISIBLE);
                SnapColorsBtn.setClickable(true);
            }
        });

        Button btnCancel = (Button) findViewById(R.id.cancel);
        btnCancel.getLayoutParams().width = (App.size.x / 2) - 40;
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.getBackground().setAlpha(255);
                ((RelativeLayout) AlphaLayout.this.getParent()).removeView(AlphaLayout.this);
                f.setVisibility(View.VISIBLE);
                SnapColorsBtn.setClickable(true);
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        editText.getBackground().setAlpha(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
