package com.manvir.SnapColors;

import android.app.Activity;
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
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Settings extends PreferenceFragment {
    public SharedPreferences prefs;
    String fontsDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + MainActivity.SnapChatPKG + "/files";
    private CheckBoxPreference TextColor;
    private CheckBoxPreference BGColor;
    private CheckBoxPreference setFont;
    private CheckBoxPreference autoRandomize;
    private CheckBoxPreference shouldRainbow;
    private CheckBoxPreference checkForVer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        //noinspection Annotator,deprecation
        prefs = getActivity().getSharedPreferences("settings", Context.MODE_WORLD_READABLE);
        if (BuildConfig.DEBUG) getActivity().setTitle("SnapColors: Dev");

        //Find all preferences
        TextColor = (CheckBoxPreference) getPreferenceManager().findPreference("TextColor");
        BGColor = (CheckBoxPreference) getPreferenceManager().findPreference("BGColor");
        setFont = (CheckBoxPreference) getPreferenceManager().findPreference("setFont");
        autoRandomize = (CheckBoxPreference) getPreferenceManager().findPreference("autoRandomize");
        shouldRainbow = (CheckBoxPreference) getPreferenceManager().findPreference("shouldRainbow");
        Preference minTimerInt = getPreferenceManager().findPreference("minTimerInt");
        Preference importFont = getPreferenceManager().findPreference("importFont");
        Preference clearAllImportedFonts = getPreferenceManager().findPreference("clearAllImportedFonts");
        checkForVer = (CheckBoxPreference) getPreferenceManager().findPreference("checkForVer");
        Preference donate = getPreferenceManager().findPreference("donate");

        //Startup stuff
        prefs.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> update());
        update();

        //Listeners
        minTimerInt.setOnPreferenceClickListener(preference -> {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Time in Seconds");
            EditText timerMin = new EditText(getActivity());
            timerMin.setGravity(Gravity.CENTER_HORIZONTAL);
            timerMin.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(timerMin);
            builder.setPositiveButton("Done", (dialog, which) -> {
                if (timerMin.getText().toString().equals("") || timerMin.getText().toString().equals("0")) {
                    Toast.makeText(getActivity(), "Must be greater then 0", Toast.LENGTH_LONG).show();
                    return;
                }
                prefs.edit().putInt("minTimerInt", Integer.valueOf(timerMin.getText().toString())).apply();
                dialog.dismiss();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(dialog -> imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0));
            alertDialog.setOnDismissListener(dialog -> imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0));
            alertDialog.show();
            return true;
        });
        donate.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(MainActivity.SnapColorsPKG, MainActivity.SnapColorsPKG + ".DonateActivity"));
            getActivity().startActivity(intent);
            return true;
        });
        checkForVer.setOnPreferenceChangeListener((preference, newValue) -> {
            if (!checkForVer.isChecked()) {
                prefs.edit().putBoolean("checkForVer", true).apply();
            } else {
                prefs.edit().putBoolean("checkForVer", false).apply();
            }
            return true;
        });
        clearAllImportedFonts.setOnPreferenceClickListener(preference -> {
            Util.DeleteRecursive(new File(fontsDir));
            Toast.makeText(getActivity(), "Successful", Toast.LENGTH_LONG).show();
            return true;
        });
        shouldRainbow.setOnPreferenceChangeListener((preference, newValue) -> {
            if (!shouldRainbow.isChecked()) {
                prefs.edit().putBoolean("shouldRainbow", true).apply();
            } else {
                prefs.edit().putBoolean("shouldRainbow", false).apply();
            }
            return true;
        });
        autoRandomize.setOnPreferenceChangeListener((preference, newValue) -> {
            if (!autoRandomize.isChecked()) {
                TextColor.setEnabled(false);
                BGColor.setEnabled(false);
                prefs.edit().putBoolean("autoRandomize", true).apply();
            } else {
                prefs.edit().putBoolean("autoRandomize", false).apply();
                TextColor.setEnabled(true);
                BGColor.setEnabled(true);
            }
            return true;
        });
        setFont.setOnPreferenceChangeListener((preference, newValue) -> {
            if (!setFont.isChecked()) {
                try {
                    Resources res = getActivity().getPackageManager().getResourcesForApplication("com.manvir.snapcolorsfonts");
                    prefs.edit().putBoolean("setFont", true).apply();
                    copyAssets(res);

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_singlechoice);

                    File file[] = new File(fontsDir).listFiles();
                    for (File aFile : file) {
                        arrayAdapter.add(aFile.getName());
                    }

                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                    builderSingle.setIcon(R.drawable.ic_launcher);
                    builderSingle.setTitle("Select A Font:");
                    builderSingle.setNegativeButton("Cancel", (dialog, which) -> {
                        prefs.edit().putBoolean("setFont", false).apply();
                        setFont.setChecked(false);
                        System.gc(); //We need to run the GC, if we don't thetypefaces stay in memory.
                    });

                    builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
                        String strName = arrayAdapter.getItem(which).replace(".TTF", ".ttf");
                        Typeface face = Typeface.createFromFile(fontsDir + "/" + strName);
                        prefs.edit().putString("Font", strName).apply();
                        prefs.edit().putBoolean("setFont", true).apply();
                        System.gc(); //We need to run the GC, if we don't thetypefaces stay in memory.
                    });
                    builderSingle.show();
                } catch (PackageManager.NameNotFoundException e) {
                    final AlertDialog.Builder al = new AlertDialog.Builder(getActivity());
                    al.setCancelable(false);
                    al.setTitle("SnapColors");
                    al.setMessage("You need to download fonts, they are not included. (Note no icon will be added)");
                    al.setNegativeButton("Download & Install", (dialog, which) -> {
                        Util.downloadFontsApk(getActivity());
                    });
                    al.setPositiveButton("Cancel", (dialog, which) -> {
                        prefs.edit().putBoolean("setFont", false).apply();
                        setFont.setChecked(false);
                        dialog.dismiss();
                    });
                    al.setNeutralButton("Why", (dialog, which) -> {
                        String whyText = "The reason why fonts are not included with the tweak are:\n1. People may not have the space for fonts on there phone.\n2. Its easier for me to manage.\n3. You can move the apk to your SDCARD with out moving the tweak to the SDCARD.\n4. This way I can have different font packs with different sizes.";
                        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle("SnapColors");
                        alertDialog.setMessage(whyText);
                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", (dialog1, which1) -> {
                            prefs.edit().putBoolean("setFont", false).apply();
                            setFont.setChecked(false);
                            alertDialog.dismiss();
                        });
                        alertDialog.show();
                    });
                    al.show();
                }
            } else {
                prefs.edit().putBoolean("setFont", false).apply();
            }
            return true;
        });
        importFont.setOnPreferenceClickListener(preference -> {
            Intent i = new Intent(getActivity(), FilePickerActivity.class);
            i.setType("font/opentype");
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
            startActivityForResult(i, 0);
            return true;
        });
        BGColor.setOnPreferenceChangeListener((preference, newValue) -> {
            if (!BGColor.isChecked()) {
                autoRandomize.setEnabled(false);
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog(getActivity(), Color.WHITE, color -> prefs.edit().putInt("BGColor", color).apply());
                colorPickerDialog.setButton(Dialog.BUTTON_NEUTRAL, "Default", (dialog, which) -> {
                    prefs.edit().putInt("BGColor", -1728053248).apply();
                    BGColor.setChecked(false);
                });
                colorPickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
                    prefs.edit().putInt("BGColor", -1728053248).apply();
                    BGColor.setChecked(false);
                    autoRandomize.setEnabled(true);
                });
                colorPickerDialog.setTitle("Background Color");
                colorPickerDialog.show();
            } else {
                prefs.edit().putInt("BGColor", -1728053248).apply();
                autoRandomize.setEnabled(true);
            }
            return true;
        });
        TextColor.setOnPreferenceChangeListener((preference, newValue) -> {
            if (!TextColor.isChecked()) {
                autoRandomize.setEnabled(false);
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog(getActivity(), Color.WHITE, color -> prefs.edit().putInt("TextColor", color).apply());
                colorPickerDialog.setButton(Dialog.BUTTON_NEUTRAL, "Default", (dialog, which) -> {
                    prefs.edit().putInt("TextColor", Color.WHITE).apply();
                    TextColor.setChecked(false);
                });
                colorPickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
                    prefs.edit().putInt("TextColor", Color.WHITE).apply();
                    TextColor.setChecked(false);
                    autoRandomize.setEnabled(true);
                });
                colorPickerDialog.setTitle("Text Color");
                colorPickerDialog.show();
            } else {
                prefs.edit().putInt("TextColor", Color.WHITE).apply();
                autoRandomize.setEnabled(true);
            }
            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            try {
                File ttfFile = new File(Uri.decode(data.getDataString()).replace("file://", ""));
                FileUtils.copyFile(ttfFile, new File(fontsDir + "/" + ttfFile.getName()));
                Toast.makeText(getActivity(), "Import successful.", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Import failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void update() {
        if (prefs.getBoolean("checkForVer", true)) {
            checkForVer.setChecked(true);
        }
        if (TextColor.isChecked() || BGColor.isChecked()) {
            autoRandomize.setEnabled(false);
        }
        if (autoRandomize.isChecked()) {
            TextColor.setEnabled(false);
            BGColor.setEnabled(false);
        }
        if (Util.activeVersion() != BuildConfig.VERSION_CODE && !Util.isNegative(Util.activeVersion())) {
            Toast.makeText(getActivity(), "Warning: Did you forget to reboot/soft reboot?!", Toast.LENGTH_LONG).show();
        } else if (Util.isNegative(Util.activeVersion())) {
            Toast.makeText(getActivity(), "Warning: Module disabled in xposed!", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("UnusedAssignment")
    public void copyAssets(Resources res) {
        if (!new File(fontsDir).exists()) {
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
        if (files == null) return;
        for (String filename : files) {
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
            } catch (IOException e) {
                Log.e("SnapColors", "Failed to copy asset file: " + filename, e);
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
