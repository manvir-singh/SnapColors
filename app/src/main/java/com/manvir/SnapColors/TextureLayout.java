package com.manvir.SnapColors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Shader;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.manvir.common.PACKAGES;

import java.lang.reflect.Field;

public class TextureLayout extends RelativeLayout {
    public TextureLayout(final Activity context, final EditText editText, final HorizontalScrollView f, final ImageButton SnapColorsBtn, final RelativeLayout SnapChatLayout) {
        super(context);
        final ProgressDialog progressDialog = ProgressDialog.show(context, "", "Loading Textures");

        setClickable(true);
        SnapColorsBtn.setClickable(false);
        setBackgroundDrawable(App.modRes.getDrawable(R.drawable.bgviewdraw));
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.addView(inflater.inflate(App.modRes.getLayout(R.layout.textures_layout), null));

        ScrollView scrollView = (ScrollView) findViewById(R.id.ScrollViewFontsList);
        scrollView.setLayoutParams(new LayoutParams(App.size.x, LayoutParams.WRAP_CONTENT));

        Button btnCancel = (Button) findViewById(R.id.cancel);
        btnCancel.setBackgroundDrawable(App.modRes.getDrawable(R.drawable.roundcorner));
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.getPaint().reset();
                editText.setTextColor(Color.WHITE);
                editText.setTextSize(21);
                editText.setBackgroundColor(-1728053248);

                ((RelativeLayout) TextureLayout.this.getParent()).removeView(TextureLayout.this);
                f.setVisibility(View.VISIBLE);
                SnapColorsBtn.setClickable(true);
                System.gc();
            }
        });

        new Thread() {
            @Override
            public void run() {
                Field[] textures = R.raw.class.getFields();
                for (int i = 0; i < textures.length; i++) {
                    final TexturePreview v = new TexturePreview(context);
                    v.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    v.setTextSize(40);
                    v.setText("Preview");

                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 8;

                    v.setTextureID(textures[i].getName());
                    Bitmap bitmap = BitmapFactory.decodeStream(App.modRes.openRawResource(App.modRes.getIdentifier(textures[i].getName(), "raw", PACKAGES.SNAPCOLORS)), null, options);
                    final Shader shader = new BitmapShader(bitmap,
                            Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                    v.getPaint().setShader(shader);
                    v.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View vv) {
                            Bitmap bitmapp = BitmapFactory.decodeStream(App.modRes.openRawResource(App.modRes.getIdentifier(((TexturePreview) vv).getTextureID(), "raw", PACKAGES.SNAPCOLORS)));
                            Shader shaderr = new BitmapShader(bitmapp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                            editText.getPaint().setShader(shaderr);
                            editText.setText(editText.getText()); //Forces the view to redraw
                            ((RelativeLayout) TextureLayout.this.getParent()).removeView(TextureLayout.this);
                            f.setVisibility(View.VISIBLE);
                            SnapColorsBtn.setClickable(true);
                            System.gc();
                        }
                    });
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((LinearLayout) findViewById(R.id.texturesMainLayout)).addView(v);
                        }
                    });
                }
                progressDialog.dismiss();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        f.setVisibility(View.GONE);
                        SnapChatLayout.addView(TextureLayout.this, App.optionsViewLayoutParams);
                    }
                });
            }
        }.start();

    }

    private class TexturePreview extends TextView {
        String name;

        public TexturePreview(Context context) {
            super(context);
        }

        public String getTextureID() {
            return this.name;
        }

        public void setTextureID(String name) {
            this.name = name;
        }
    }
}
