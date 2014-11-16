package com.manvir.SnapColors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import java.io.*;
import java.util.Random;

public class GestureDec extends  GestureDetector.SimpleOnGestureListener{
    static Context SnapChatContext;
    static EditText editText;
    static Random random = new Random();
    static Typeface defTypeFace;
    public GestureDec(Context con, EditText editText, Typeface defTypeFace){
        GestureDec.SnapChatContext = con;
        GestureDec.editText = editText;
        GestureDec.defTypeFace = defTypeFace;
    }
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(SnapChatContext);
        alert.setTitle("SnapColors Options");

        LinearLayout linear = new LinearLayout(SnapChatContext);
        linear.setOrientation(LinearLayout.VERTICAL);

//        final Button btnSize = new Button(SnapChatContext);
//        btnSize.setText("Size");
//        btnSize.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder alert = new AlertDialog.Builder(SnapChatContext);
//                final SeekBar seek = new SeekBar(SnapChatContext);
//                seek.setMax(300);
//                seek.setProgress((int) editText.getTextSize());
//                seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
//                        editText.setTextSize(arg1);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar arg0) {
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar arg0) {
//                    }
//                });
//                alert.setPositiveButton("Done", null);
//                alert.setView(seek);
//                alert.show();
//            }
//        });

//        final Button btnAlpha = new Button(SnapChatContext);
//        btnAlpha.setText("Transparency");
//        btnAlpha.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LinearLayout linear = new LinearLayout(SnapChatContext);
//                linear.setOrientation(LinearLayout.VERTICAL);
//                AlertDialog.Builder alert = new AlertDialog.Builder(SnapChatContext);
//
//                final Button setInvisible = new Button(SnapChatContext);
//                setInvisible.setText("Invisible");
//                setInvisible.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        editText.setBackgroundColor(Color.TRANSPARENT);
//                    }
//                });
//                linear.addView(setInvisible);
//
//                alert.setView(linear);
//                alert.show();
//            }
//        });

//        final Button btnReset = new Button(SnapChatContext);
//        btnReset.setText("Reset");
//        btnReset.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                editText.setTextSize(21);
//                editText.setBackgroundColor(-1728053248);
//                editText.setTextColor(Color.WHITE);
//            }
//        });

        final Button randomize = new Button(SnapChatContext);
        randomize.setText("Randomize");
        randomize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                random(editText);
            }
        });

        final Button textColorbtn = new Button(SnapChatContext);
        textColorbtn.setText("Text Color");
        textColorbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog(SnapChatContext, Color.WHITE, new ColorPickerDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        editText.setTextColor(color);
                    }
                });
                colorPickerDialog.setButton( Dialog.BUTTON_NEUTRAL, "Default", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        editText.setTextColor(Color.WHITE);
                    }
                });
                colorPickerDialog.setTitle("Text Color");
                colorPickerDialog.show();
            }
        });

        final Button bgColorbtn = new Button(SnapChatContext);
        bgColorbtn.setText("Background Color");
        bgColorbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog(SnapChatContext, Color.WHITE, new ColorPickerDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        editText.setBackgroundColor(color);
                    }
                });
                colorPickerDialog.setButton( Dialog.BUTTON_NEUTRAL, "Default", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        editText.setBackgroundColor(-1728053248);
                    }
                });
                colorPickerDialog.setTitle("Background Color");
                colorPickerDialog.show();
            }
        });

        linear.addView(textColorbtn);
        linear.addView(bgColorbtn);
        //linear.addView(btnSize);
        //linear.addView(btnAlpha);
        linear.addView(randomize);
        //linear.addView(btnReset);

        alert.setView(linear);
        alert.setPositiveButton("Done", null);
        alert.setNeutralButton("Fonts", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Resources res = SnapChatContext.getPackageManager().getResourcesForApplication("com.manvir.snapcolorsfonts");
                    copyAssets(res);

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SnapChatContext, android.R.layout.select_dialog_singlechoice);
                    final String fontsDir = SnapChatContext.getExternalFilesDir(null).getAbsolutePath();
                    File file[] = new File(fontsDir).listFiles();
                    for (File aFile : file) {
                        arrayAdapter.add(aFile.getName().replace(".ttf", "").replace(".TTF", ""));
                    }

                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(SnapChatContext);
                    builderSingle.setIcon(0);
                    builderSingle.setTitle("Select A Font:");
                    builderSingle.setNeutralButton("Default", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editText.setTypeface(defTypeFace);
                        }
                    });
                    builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String strName = arrayAdapter.getItem(which);
                            Typeface face = Typeface.createFromFile(fontsDir+ "/" + strName+".ttf");
                            editText.setTypeface(face);
                        }
                    });
                    builderSingle.show();
                } catch (PackageManager.NameNotFoundException e) {
                    AlertDialog.Builder al = new AlertDialog.Builder(SnapChatContext);
                    final TextView message = new TextView(SnapChatContext);
                    final SpannableString s = new SpannableString("You need to randomize_btn fonts they are not included. Just randomize_btn and install the apk.(Note no icon will be added) Fonts apk can be downloaded from this link: http://forum.xda-developers.com/devdb/project/?id=3684#downloads");
                    Linkify.addLinks(s, Linkify.WEB_URLS);
                    message.setText(s);
                    message.setMovementMethod(LinkMovementMethod.getInstance());
                    al.setTitle("SnapColors");
                    al.setView(message);
                    al.setNegativeButton("Close", null);
                    al.setNeutralButton("Why", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String whyText = "The reason why fonts are not included with the tweak are simple.\n1. People may not have the space for fonts on there phone.\n2. Its easier for me to manage.\n3. You can move the apk to your SDCARD with out moving the tweak to the SDCARD.\n4. This way I can have different font packs with different sizes.";
                            AlertDialog alertDialog = new AlertDialog.Builder(SnapChatContext).create();
                            alertDialog.setTitle("SnapColors");
                            alertDialog.setMessage(whyText);
                            alertDialog.show();
                        }
                    });
                    al.show();
                }
            }
        });
        alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog,int id)
            {
            }
        });
        alert.show();
        return true;
    }

    private void random(EditText textBox) {
        int colorBG = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        int colorText = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        textBox.setBackgroundColor(colorBG);
        textBox.setTextColor(colorText);
    }

    public void copyAssets(Resources res){
        AssetManager assetManager = res.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(SnapChatContext.getExternalFilesDir(null), filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch(IOException e) {
                Log.e("SnapColors", "Failed to copy asset file: " + filename, e);
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
