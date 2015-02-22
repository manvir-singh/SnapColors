package com.manvir.SnapColors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Settings extends PreferenceFragment {
    public SharedPreferences prefs;
    String fontsDir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/com.snapchat.android/files";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        prefs = getActivity().getSharedPreferences("settings", Context.MODE_WORLD_READABLE);
        if(BuildConfig.DEBUG) getActivity().setTitle("SnapColors: Dev");

        //Find all preferences
        final CheckBoxPreference TextColor = (CheckBoxPreference) getPreferenceManager().findPreference("TextColor");
        final CheckBoxPreference BGColor = (CheckBoxPreference) getPreferenceManager().findPreference("BGColor");
        final CheckBoxPreference setFont = (CheckBoxPreference) getPreferenceManager().findPreference("setFont");
        final CheckBoxPreference autoRandomize = (CheckBoxPreference) getPreferenceManager().findPreference("autoRandomize");
        final CheckBoxPreference shouldRainbow = (CheckBoxPreference) getPreferenceManager().findPreference("shouldRainbow");
        final Preference importFont = getPreferenceManager().findPreference("importFont");
        final Preference clearAllImportedFonts = getPreferenceManager().findPreference("clearAllImportedFonts");
        final CheckBoxPreference shouldGroups = (CheckBoxPreference) getPreferenceManager().findPreference("shouldGroups");
        final CheckBoxPreference checkForVer = (CheckBoxPreference) getPreferenceManager().findPreference("checkForVer");
        final Preference donate = getPreferenceManager().findPreference("donate");

        //Startup stuff
        if(prefs.getBoolean("shouldGroups", true)){
            shouldGroups.setChecked(true);
        }
        if(prefs.getBoolean("checkForVer", true)){
            checkForVer.setChecked(true);
        }
        if(TextColor.isChecked() || BGColor.isChecked()){
            autoRandomize.setEnabled(false);
        }
        if(autoRandomize.isChecked()){
            TextColor.setEnabled(false);
            BGColor.setEnabled(false);
        }

        //Listeners
        donate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.manvir.SnapColors", "com.manvir.SnapColors.DonateActivity"));
                getActivity().startActivity(intent);
                return true;
            }
        });
        shouldGroups.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(!shouldGroups.isChecked()){
                    prefs.edit().putBoolean("shouldGroups", true).apply();
                }else {
                    prefs.edit().putBoolean("shouldGroups", false).apply();
                }
                return true;
            }
        });
        checkForVer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(!checkForVer.isChecked()){
                    prefs.edit().putBoolean("checkForVer", true).apply();
                }else {
                    prefs.edit().putBoolean("checkForVer", false).apply();
                }
                return true;
            }
        });
        clearAllImportedFonts.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Util.DeleteRecursive(new File(fontsDir));
                Toast.makeText(getActivity(), "Successful", Toast.LENGTH_LONG).show();
                return true;
            }
        });
        shouldRainbow.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(!shouldRainbow.isChecked()){
                    prefs.edit().putBoolean("shouldRainbow", true).apply();
                }else {
                    prefs.edit().putBoolean("shouldRainbow", false).apply();
                }
                return true;
            }
        });
        autoRandomize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(!autoRandomize.isChecked()){
                    TextColor.setEnabled(false);
                    BGColor.setEnabled(false);
                    prefs.edit().putBoolean("autoRandomize", true).apply();
                }else {
                    prefs.edit().putBoolean("autoRandomize", false).apply();
                    TextColor.setEnabled(true);
                    BGColor.setEnabled(true);
                }
                return true;
            }
        });
        setFont.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(!setFont.isChecked()){
                    try {
                        Resources res = getActivity().getPackageManager().getResourcesForApplication("com.manvir.snapcolorsfonts");
                        prefs.edit().putBoolean("setFont", true).apply();
                        copyAssets(res);

                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);

                        File file[] = new File(fontsDir).listFiles();
                        for (File aFile : file) {
                            arrayAdapter.add(aFile.getName());
                        }

                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                        builderSingle.setIcon(R.drawable.ic_launcher);
                        builderSingle.setTitle("Select A Font:");
                        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().putBoolean("setFont", false).apply();
                                setFont.setChecked(false);
                                System.gc(); //We need to run the GC, if we don't thetypefaces stay in memory.
                            }
                        });

                        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String strName = arrayAdapter.getItem(which).replace(".TTF", ".ttf");
                                Typeface face = Typeface.createFromFile(fontsDir+ "/" + strName);
                                prefs.edit().putString("Font", strName).apply();
                                prefs.edit().putBoolean("setFont", true).apply();
                                System.gc(); //We need to run the GC, if we don't thetypefaces stay in memory.
                            }
                        });
                        builderSingle.show();
                    } catch (PackageManager.NameNotFoundException e) {
                        final AlertDialog.Builder al = new AlertDialog.Builder(getActivity());
                        al.setCancelable(false);
                        al.setTitle("SnapColors");
                        al.setMessage("You need to download fonts, they are not included. (Note no icon will be added)");
                        al.setNegativeButton("Download & Install", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Util(getActivity()).downloadFontsApk();
                            }
                        });
                        al.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().putBoolean("setFont", false).apply();
                                setFont.setChecked(false);
                                dialog.dismiss();
                            }
                        });
                        al.setNeutralButton("Why", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String whyText = "The reason why fonts are not included with the tweak are:\n1. People may not have the space for fonts on there phone.\n2. Its easier for me to manage.\n3. You can move the apk to your SDCARD with out moving the tweak to the SDCARD.\n4. This way I can have different font packs with different sizes.";
                                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                                alertDialog.setTitle("SnapColors");
                                alertDialog.setMessage(whyText);
                                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        prefs.edit().putBoolean("setFont", false).apply();
                                        setFont.setChecked(false);
                                        alertDialog.dismiss();
                                    }
                                });
                                alertDialog.show();
                            }
                        });
                        al.show();
                    }
                }else {
                    prefs.edit().putBoolean("setFont", false).apply();
                }
                return true;
            }
        });
        importFont.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("font/opentype");
                startActivityForResult(intent, 0);
                return true;
            }
        });
        BGColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!BGColor.isChecked()) {
                    autoRandomize.setEnabled(false);
                    ColorPickerDialog colorPickerDialog = new ColorPickerDialog(getActivity(), Color.WHITE, new ColorPickerDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int color) {
                            prefs.edit().putInt("BGColor", color).apply();
                        }
                    });
                    colorPickerDialog.setButton(Dialog.BUTTON_NEUTRAL, "Default", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            prefs.edit().putInt("BGColor", -1728053248).apply();
                            BGColor.setChecked(false);
                        }
                    });
                    colorPickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            prefs.edit().putInt("BGColor", -1728053248).apply();
                            BGColor.setChecked(false);
                            autoRandomize.setEnabled(true);
                        }
                    });
                    colorPickerDialog.setTitle("Background Color");
                    colorPickerDialog.show();
                } else {
                    prefs.edit().putInt("BGColor", -1728053248).apply();
                    autoRandomize.setEnabled(true);
                }
                return true;
            }
        });
        TextColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!TextColor.isChecked()) {
                    autoRandomize.setEnabled(false);
                    ColorPickerDialog colorPickerDialog = new ColorPickerDialog(getActivity(), Color.WHITE, new ColorPickerDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int color) {
                            prefs.edit().putInt("TextColor", color).apply();
                        }
                    });
                    colorPickerDialog.setButton(Dialog.BUTTON_NEUTRAL, "Default", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            prefs.edit().putInt("TextColor", Color.WHITE).apply();
                            TextColor.setChecked(false);
                        }
                    });
                    colorPickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            prefs.edit().putInt("TextColor", Color.WHITE).apply();
                            TextColor.setChecked(false);
                            autoRandomize.setEnabled(true);
                        }
                    });
                    colorPickerDialog.setTitle("Text Color");
                    colorPickerDialog.show();
                } else {
                    prefs.edit().putInt("TextColor", Color.WHITE).apply();
                    autoRandomize.setEnabled(true);
                }
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != 0 && data != null){
            try {
                File ttfFile = new File(Uri.decode(data.getDataString()).split(":/")[1]);//Todo Fix the import font bug. Fixed it temporally though.
                FileUtils.copyFile(ttfFile, new File(fontsDir+"/"+ttfFile.getName()));
                Toast.makeText(getActivity(), "Import successful.", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Import failed! Something went wrong =0", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void copyAssets(Resources res){
        if(!new File(fontsDir).exists()){
            if (!new File(fontsDir).mkdirs())
                throw new RuntimeException("SnapColors was unable to create fontsDir.");
        }
        AssetManager assetManager = res.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for(String filename : files) {
            try {
                InputStream in;
                OutputStream out;
                in = assetManager.open(filename);
                File outFile = new File(new File(fontsDir), filename);
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
