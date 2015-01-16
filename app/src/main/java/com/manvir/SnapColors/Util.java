package com.manvir.SnapColors;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.manvir.logger.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class Util {
    public Context con;
    static Logger logger = App.logger;

    public Util(Context con){
        this.con = con;
    }
    public Util(){}

    public static void doMultiLine(EditText editText){
        editText.setSingleLine(false);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
    }

    //For converting px's to dpi
    public int px(float dips){
        float DP = con.getResources().getDisplayMetrics().density;
        return Math.round(dips * DP);
    }

    //Sets the EditText background, and the text color to a random color.
    public void random(EditText textsBox) {
        Random random = new Random();
        int colorBG = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        int colorText = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        textsBox.setBackgroundColor(colorBG);
        textsBox.setTextColor(colorText);
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

    public static void doFonts(final Context con, final ProgressDialog pro, final Handler handler, final Typeface defTypeFace, final RelativeLayout SnapChatLayout, final HorizontalScrollView f, final ImageButton SnapColorsBtn){
        new Thread(){
            @Override
            public void run() {
                try {
                    Resources res = con.getPackageManager().getResourcesForApplication("com.manvir.snapcolorsfonts");
                    new Util(con).copyAssets(res);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            SnapChatLayout.addView(new FontsListView(con, defTypeFace, f, SnapColorsBtn), App.param);
                            pro.dismiss();
                        }
                    });
                } catch (PackageManager.NameNotFoundException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder al = new AlertDialog.Builder(con);
                            al.setTitle("SnapColors");
                            al.setMessage("You need to download fonts, they are not included. To download just tap \"Download & Install\". (Note no icon will be added)");
                            al.setNegativeButton("Download & Install", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Util(con).downlaodFontsApk();
                                }
                            });
                            al.setNeutralButton("Why", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String whyText = "The reason why fonts are not included with the tweak are:\n1. People may not have the space for fonts on there phone.\n2. Its easier for me to manage.\n3. You can move the apk to your SDCARD with out moving the tweak to the SDCARD.\n4. This way I can have different font packs with different sizes.";
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
            pro = ProgressDialog.show(con, "", "Downloading");
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

    public static void doDonationMsg(Context con){
        logger.log("Doing donation stuff");
        try {
            logger.log("Checking to see if the user has donated");
            con.createPackageContext("com.manvir.snapcolorsdonation", 0);
            logger.log("User has donated don't do anything");
        } catch (PackageManager.NameNotFoundException e) {
            logger.log("User hasn't donated show the msg");
            try {
                int SnapColorsVersionCode = con.getPackageManager().getPackageInfo("com.manvir.SnapColors", 0).versionCode;
                File versionFile = new File(con.getCacheDir().getAbsolutePath()+"/version");

                if(!versionFile.exists())
                    versionFile.createNewFile();

                if(getStringFromFile(versionFile.getAbsolutePath()).equals("")){
                    new DonationDialog(con).show();
                    PrintWriter writer = new PrintWriter(versionFile);
                    writer.write(String.valueOf(SnapColorsVersionCode));
                    writer.close();
                }else if(Integer.parseInt(getStringFromFile(versionFile.getAbsolutePath())) != SnapColorsVersionCode){
                    new DonationDialog(con).show();
                    PrintWriter writer = new PrintWriter(versionFile);
                    writer.write(String.valueOf(SnapColorsVersionCode));
                    writer.close();
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    private static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        fin.close();
        return sb.toString();
    }

    public static void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }

    public Bitmap getBorderPreview(String borderName){
        Bitmap imgthumBitmap=null;
        try
        {
            final int THUMBNAIL_SIZE = 64;
            imgthumBitmap = BitmapFactory.decodeStream(App.modRes.openRawResource(App.modRes.getIdentifier(borderName, "drawable", "com.manvir.SnapColors")));
            imgthumBitmap = Bitmap.createScaledBitmap(imgthumBitmap,
                    THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);
            ByteArrayOutputStream bytearroutstream = new ByteArrayOutputStream();
            imgthumBitmap.compress(Bitmap.CompressFormat.JPEG, 100,bytearroutstream);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return imgthumBitmap;
    }

    //Downscales the border bitmap and returns it as a drawable.
    public static BitmapDrawable getBorder(String name, Context con){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap map = BitmapFactory.decodeStream(App.modRes.openRawResource(App.modRes.getIdentifier(name, "drawable", "com.manvir.SnapColors")), null, options);
        int originalHeight = options.outHeight;
        int originalWidth = options.outWidth;
        // Calculate your sampleSize based on the requiredWidth and originalWidth
        // For e.g you want the width to stay consistent at 500dp
        float requiredWidth = (con.getResources().getDisplayMetrics().widthPixels/con.getResources().getDisplayMetrics().density) * con.getResources().getDisplayMetrics().density;
        float sampleSize = originalWidth / requiredWidth;
        // If the original image is smaller than required, don't sample
        if(sampleSize < 1) { sampleSize = 1; }
        options.inSampleSize = (int)sampleSize;
        options.inPurgeable = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeStream(App.modRes.openRawResource(App.modRes.getIdentifier(name, "drawable", "com.manvir.SnapColors")), null, options);
        return new BitmapDrawable(bitmap);
    }
}
