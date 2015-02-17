package com.manvir.SnapColors;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GradientLayout extends LinearLayout{
    int topGradColor;
    int bottomGradColor;
    public GradientLayout(final Context context, final EditText editText, final HorizontalScrollView f, final ImageButton SnapColorsBtn) {
        super(context);
        f.setVisibility(View.GONE);
        SnapColorsBtn.setClickable(false);
        setClickable(true);
        setBackgroundDrawable(App.modRes.getDrawable(R.drawable.bgviewdraw));
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.addView(inflater.inflate(App.modRes.getLayout(R.layout.gradient_layout), null));

        final Button btnDone = (Button) findViewById(R.id.done);
        btnDone.setEnabled(false);

        final TextView topGrad = (TextView) findViewById(R.id.topGrad);
        topGrad.setText("Top Gradient (Tap to change)");
        topGrad.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog(context, Color.WHITE, new ColorPickerDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        topGradColor = color;
                        topGrad.setBackgroundColor(color);
                        btnDone.setEnabled(true);
                    }
                });
                colorPickerDialog.setTitle("Top Gradient");
                colorPickerDialog.show();
            }
        });


        final TextView bottomGrad = (TextView) findViewById(R.id.bottomGrad);
        bottomGrad.setText("Bottom Gradient (Tap to change)");
        bottomGrad.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog(context, Color.WHITE, new ColorPickerDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        bottomGradColor = color;
                        bottomGrad.setBackgroundColor(color);
                        btnDone.setEnabled(true);
                    }
                });
                colorPickerDialog.setTitle("Bottom Gradient");
                colorPickerDialog.show();
            }
        });

        btnDone.getLayoutParams().width = (App.size.x/2)-40;
        btnDone.setBackgroundDrawable(App.modRes.getDrawable(R.drawable.roundcorner));
        btnDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                applyGrad(editText, topGradColor, bottomGradColor);
                ((RelativeLayout) GradientLayout.this.getParent()).removeView(GradientLayout.this);
                f.setVisibility(View.VISIBLE);
                SnapColorsBtn.setClickable(true);
            }
        });

        Button btnCancel = (Button)findViewById(R.id.cancel);
        btnCancel.getLayoutParams().width = (App.size.x/2)-40;
        btnCancel.setBackgroundDrawable(App.modRes.getDrawable(R.drawable.roundcorner));
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RelativeLayout) GradientLayout.this.getParent()).removeView(GradientLayout.this);
                f.setVisibility(View.VISIBLE);
                SnapColorsBtn.setClickable(true);
            }
        });
    }

    public void applyGrad(EditText editText, int topColor, int bottomColor){
        Shader textShader = new LinearGradient(0, 0, 0, 100, new int[]{topColor, bottomColor}, new float[]{0, 1}, Shader.TileMode.CLAMP);
        editText.getPaint().setShader(textShader);
        editText.setText(editText.getText());
    }
}
