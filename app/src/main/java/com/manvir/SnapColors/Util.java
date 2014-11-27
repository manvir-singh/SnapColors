package com.manvir.SnapColors;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import de.robv.android.xposed.XposedBridge;

public class Util {
    public Context con;
    public static int SHOW = 1;
    public static int HIDE = 1;
    public Util(Context con){
        this.con = con;
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
                File outFile = new File(con.getExternalFilesDir(null), filename);
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
                            final AlertDialog.Builder al = new AlertDialog.Builder(con);
                            final TextView message = new TextView(con);
                            final SpannableString s = new SpannableString("You need to download fonts, they are not included. To download just tap \"Download & Install\"(Note no icon will be added)");
                            Linkify.addLinks(s, Linkify.WEB_URLS);
                            message.setText(s);
                            message.setMovementMethod(LinkMovementMethod.getInstance());
                            al.setTitle("SnapColors");
                            al.setView(message);
                            al.setNegativeButton("Download & Install", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Util(con).downlaodFontsApk();
                                }
                            });
                            al.setNeutralButton("Why", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String whyText = "The reason why fonts are not included with the tweak are simple.\n1. People may not have the space for fonts on there phone.\n2. Its easier for me to manage.\n3. You can move the apk to your SDCARD with out moving the tweak to the SDCARD.\n4. This way I can have different font packs with different sizes.";
                                    final AlertDialog alertDialog = new AlertDialog.Builder(con).create();
                                    alertDialog.setTitle("SnapColors");
                                    alertDialog.setMessage(whyText);
                                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            alertDialog.dismiss();
                                        }
                                    });
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

    public void downlaodFontsApk(){
        String url = "http://forum.xda-developers.com/devdb/project/dl/?id=5916&task=get";
        new DownloadFileAsync().execute(url);
    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {
        ProgressDialog pro;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pro = ProgressDialog.show(con, "", "Downloading Fonts Apk");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pro.dismiss();
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;
            try {
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lenghtOfFile = conexion.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"SnapColorsFonts.apk");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                Intent install = new Intent(Intent.ACTION_VIEW);
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                install.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"SnapColorsFonts.apk")),
                        "application/vnd.android.package-archive");
                con.startActivity(install);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
