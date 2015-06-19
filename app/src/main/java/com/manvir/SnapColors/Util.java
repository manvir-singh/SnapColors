package com.manvir.SnapColors;

import android.app.AlertDialog;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class Util {
    public static String SDCARD_SNAPCOLORS = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SnapColors";
    private static Context mContext;

    /**
     * Allows the {@link android.widget.EditText} to have multiple lines
     *
     * @param editText The EditText to enable multiline on
     */
    public static void doMultiLine(EditText editText) {
        editText.setSingleLine(false);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
    }

    /**
     * Checks to see if the user has the fonts apk installed, if it is copy the fonts(.ttf) to snapchats data directory.
     *
     * @param con            Snapchats {@link android.content.Context}.
     * @param pro            The {@link android.app.ProgressDialog} to show while copying the .ttf's or when the view is done loading.
     * @param handler        Used to run stuff on the uithread.
     * @param defTypeFace    The captions default {@link com.manvir.SnapColors.Typefaces}.
     * @param SnapChatLayout Snapchats main layout to witch we add the preview view to.
     * @param f              SnapColors outer view.
     * @param SnapColorsBtn  SnapColors main options panel button(The colored "T").
     */
    public static void doFonts(final Context con, final ProgressDialog pro, final Handler handler, final Typeface defTypeFace, final RelativeLayout SnapChatLayout, final HorizontalScrollView f, final ImageButton SnapColorsBtn) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Resources res = con.getPackageManager().getResourcesForApplication("com.manvir.snapcolorsfonts");
                    Util.copyAssets(res, con);
                    handler.post(() -> {
                        SnapChatLayout.addView(new FontsListView(con, defTypeFace, f, SnapColorsBtn), App.optionsViewLayoutParams);
                        pro.dismiss();
                    });
                } catch (PackageManager.NameNotFoundException e) {
                    handler.post(() -> {
                        final AlertDialog.Builder al = new AlertDialog.Builder(con);
                        al.setTitle("SnapColors");
                        al.setMessage("You need to download fonts, they are not included. To download just tap \"Download & Install\". (Note no icon will be added)");
                        al.setNegativeButton("Download & Install", (dialog, which) -> {
                            Util.downloadFontsApk(con);
                        });
                        al.setNeutralButton("Why", (dialog, which) -> {
                            String whyText = "The reason why fonts are not included with the tweak are:\n1. People may not have the space for fonts on there phone.\n2. Its easier for me to manage.\n3. You can move the apk to your SDCARD with out moving the tweak to the SDCARD.\n4. This way I can have different font packs with different sizes.";
                            final AlertDialog alertDialog = new AlertDialog.Builder(con).create();
                            alertDialog.setTitle("SnapColors");
                            alertDialog.setMessage(whyText);
                            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", (dialog1, which1) -> alertDialog.dismiss());
                            alertDialog.show();
                        });
                        al.show();
                        pro.dismiss();
                    });
                }
            }
        }.start();
    }

    /**
     * Deletes a directory regardless of whats in it.
     *
     * @param fileOrDirectory The directory or file you would like to delete.
     * @return true if the file was deleted, false otherwise.
     */
    public static boolean DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        return fileOrDirectory.delete();
    }

    /**
     * Used to determine if a reboot needs to be done after updating the module
     */
    public static int activeVersion() {
        return -1;
    }

    /**
     * Returns true if the passed int is a negative value, returns false otherwise.
     */
    public static boolean isNegative(int i) {
        return i != 0 && i >> 31 != 0;
    }

    /**
     * Sets the EditText background, and the text color to a random color.
     *
     * @param textsBox The {@link android.widget.EditText} to set a random background color and text color on.
     */
    public static void random(EditText textsBox) {
        Random random = new Random();
        int colorBG = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        int colorText = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        textsBox.setBackgroundColor(colorBG);
        textsBox.setTextColor(colorText);
    }

    /**
     * Used by {@link #doFonts(android.content.Context, android.app.ProgressDialog, android.os.Handler, android.graphics.Typeface, android.widget.RelativeLayout, android.widget.HorizontalScrollView, android.widget.ImageButton) doFonts}
     * to copy the all assets(fonts) to sdcard/Android/data/com.snapchat.android/files
     *
     * @param res The {@link android.content.res.Resources} object of the application to extract assets from.
     */
    @SuppressWarnings("UnusedAssignment")
    public static void copyAssets(Resources res, Context con) {
        AssetManager assetManager = res.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files == null) {
            Logger.log("Error: copyAssets");
            return;
        }
        for (String filename : files) {
            InputStream in;
            OutputStream out;
            try {
                in = assetManager.open(filename);
                File outFile = new File(con.getExternalFilesDir(null), filename);
                out = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (IOException e) {
                Log.e("SnapColors", "Failed to copy asset file: " + filename, e);
            }
        }
    }

    /**
     * Downloads the fonts apk
     */
    public static void downloadFontsApk(Context con) {
        mContext = con;
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "SnapColorsFonts.apk";
        String url = "http://forum.xda-developers.com/devdb/project/dl/?id=5916&task=get";
        new DownloadFileAsync().execute(url, path);
    }

    /**
     * Downloads a file from the given url then writes it to the given path.
     */
    public static class DownloadFileAsync extends AsyncTask<String, String, String> {
        ProgressDialog pro;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pro = ProgressDialog.show(mContext, "", "Downloading");
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
                OutputStream output = new FileOutputStream(aurl[1]);

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
                install.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "SnapColorsFonts.apk")),
                        "application/vnd.android.package-archive");
                mContext.startActivity(install);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
