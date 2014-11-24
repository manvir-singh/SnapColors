package com.manvir.SnapColors;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Layout;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.robv.android.xposed.XposedBridge;

public class Util {
    Context SnapChatContext;
    public static int SHOW = 1;
    public static int HIDE = 1;
    public Util(Context con){
        this.SnapChatContext = con;
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

    public static void doFonts(final Context con, final ProgressDialog pro, final Handler handler, final EditText editText, final Typeface defTypeFace){
        new Thread(){
            @Override
            public void run() {
                try {
                    Resources res = con.getPackageManager().getResourcesForApplication("com.manvir.snapcolorsfonts");
                    new Util(con).copyAssets(res);

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(con, android.R.layout.select_dialog_singlechoice);
                    final String fontsDir = con.getExternalFilesDir(null).getAbsolutePath();
                    File file[] = new File(fontsDir).listFiles();
                    for (File aFile : file) {
                        arrayAdapter.add(aFile.getName().replace(".ttf", "").replace(".TTF", ""));
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builderSingle = new AlertDialog.Builder(con);
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
                            pro.dismiss();
                        }
                    });
                } catch (PackageManager.NameNotFoundException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder al = new AlertDialog.Builder(con);
                            final TextView message = new TextView(con);
                            final SpannableString s = new SpannableString("You need to download fonts, they are not included. Just download and install the apk.(Note no icon will be added) Fonts apk can be downloaded from this link: http://forum.xda-developers.com/devdb/project/?id=3684#downloads");
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
                                    AlertDialog alertDialog = new AlertDialog.Builder(con).create();
                                    alertDialog.setTitle("SnapColors");
                                    alertDialog.setMessage(whyText);
                                    alertDialog.show();
                                }
                            });
                            al.show();
                            pro.dismiss();
                        }
                    });
                }
            }
        }.start();
    }
}
