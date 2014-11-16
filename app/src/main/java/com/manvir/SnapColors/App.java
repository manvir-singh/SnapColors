package com.manvir.SnapColors;
// Please do'nt decompile my code if you want help please ask on the thread thanks =).
/*
 * Fixed crash when trying to change fonts.
 *
*/
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.io.File;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import org.apache.commons.io.FileUtils;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class App implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources{
    static final String TAG = "SnapColors";
    static String MODULE_PATH;
    static String SnapChatPKG = "com.snapchat.android";
	static XSharedPreferences prefs;
	static Activity SnapChatContext;
	static Typeface defTypeFace;
	static boolean notFirstRun = false;
	static boolean DEBUG = true;
	static Random random = new Random();
    EditText editText;

    private void random(EditText textBox) {
        int colorBG = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        int colorText = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        textBox.setBackgroundColor(colorBG);
        textBox.setTextColor(colorText);
    }
	
	public void log(String text){
		if(DEBUG){
			XposedBridge.log(TAG+": "+text);
		}
	}

    //For converting px's to dpi
    private int px(float dips)
    {
        float DP = SnapChatContext.getResources().getDisplayMetrics().density;
        return Math.round(dips * DP);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        prefs = new XSharedPreferences("com.manvir.SnapColors", "settings");
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(SnapChatPKG))
            return;

        final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
        resparam.res.hookLayout(SnapChatPKG, "layout", "snap_preview", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                final RelativeLayout layout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("snap_preview_relative_layout","id",SnapChatPKG));
                final ImageButton SnapColorsBtn = new ImageButton(SnapChatContext);
                SnapColorsBtn.setBackgroundColor(Color.TRANSPARENT);

                final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn","id",SnapChatPKG)).getLayoutParams());

                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

                params.topMargin = px(7);
                params.rightMargin = px(110);

                SnapColorsBtn.setImageDrawable(modRes.getDrawable(R.drawable.snapcolorsbtn));

                //Get the display params for our layout.
                Display display = SnapChatContext.getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                param.width = size.x;
                param.topMargin = px(70);
                //end of get display params.

                //Setup our layout here and add the views, buttons etc.
                final RelativeLayout ly = new RelativeLayout(SnapChatContext);
                ly.setBackgroundDrawable(modRes.getDrawable(R.drawable.bgviewdraw));
                ly.setVisibility(View.GONE);

                //**Init -randomizeBtn- button and add to view.
                RelativeLayout.LayoutParams randomizeBtnParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                randomizeBtnParams.leftMargin = 70; //plus 130 for every button
                randomizeBtnParams.topMargin = 50;
                randomizeBtnParams.width = 100;
                randomizeBtnParams.height = 100;
                ImageButton randomizeBtn = new ImageButton(SnapChatContext);
                randomizeBtn.setBackgroundDrawable(modRes.getDrawable(R.drawable.roundcorner));
                randomizeBtn.setImageDrawable(modRes.getDrawable(R.drawable.randomize_btn));
                randomizeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Random random = new Random();
                        int colorBG = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        int colorText = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        editText.setBackgroundColor(colorBG);
                        editText.setTextColor(colorText);
                    }
                });
                ly.addView(randomizeBtn, randomizeBtnParams);
                //**End

                //**Init -textColorbtn- button and add to view.
                RelativeLayout.LayoutParams textColorbtnParmas = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                textColorbtnParmas.leftMargin = 200;
                textColorbtnParmas.topMargin = 50;
                textColorbtnParmas.width = 100;
                textColorbtnParmas.height = 100;
                ImageButton textColorbtn = new ImageButton(SnapChatContext);
                textColorbtn.setBackgroundDrawable(modRes.getDrawable(R.drawable.roundcorner));
                textColorbtn.setImageDrawable(modRes.getDrawable(R.drawable.textcolor_btn));
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
                ly.addView(textColorbtn, textColorbtnParmas);
                //**End

                //**Init -bgColorbtn- button and add to view.
                RelativeLayout.LayoutParams bgColorbtnParmas = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                bgColorbtnParmas.leftMargin = 330;
                bgColorbtnParmas.topMargin = 50;
                bgColorbtnParmas.width = 100;
                bgColorbtnParmas.height = 100;
                ImageButton bgColorbtn = new ImageButton(SnapChatContext);
                bgColorbtn.setBackgroundDrawable(modRes.getDrawable(R.drawable.roundcorner));
                bgColorbtn.setImageDrawable(modRes.getDrawable(R.drawable.bgcolor_btn));
                bgColorbtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
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
                ly.addView(bgColorbtn, bgColorbtnParmas);
                //**End

                //**Init -btnSize- button and add to view.
                RelativeLayout.LayoutParams btnSizeParmas = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                btnSizeParmas.leftMargin = 460;
                btnSizeParmas.topMargin = 50;
                btnSizeParmas.width = 100;
                btnSizeParmas.height = 100;
                ImageButton btnSize = new ImageButton(SnapChatContext);
                btnSize.setBackgroundDrawable(modRes.getDrawable(R.drawable.roundcorner));
                btnSize.setImageDrawable(modRes.getDrawable(R.drawable.size_btn));
                btnSize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(SnapChatContext);
                        SeekBar seek = new SeekBar(SnapChatContext);
                        seek.setMax(300);
                        seek.setProgress((int) editText.getTextSize());
                        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                                editText.setTextSize(arg1);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar arg0) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar arg0) {
                            }
                        });
                        alert.setPositiveButton("Done", null);
                        alert.setView(seek);
                        alert.show();
                    }
                });
                ly.addView(btnSize, btnSizeParmas);
                //**End

                //**Init -btnAlpha- button and add to view.
                RelativeLayout.LayoutParams btnAlphaParmas = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                btnAlphaParmas.leftMargin = 590;
                btnAlphaParmas.topMargin = 50;
                btnAlphaParmas.width = 100;
                btnAlphaParmas.height = 100;
                ImageButton btnAlpha = new ImageButton(SnapChatContext);
                btnAlpha.setBackgroundDrawable(modRes.getDrawable(R.drawable.roundcorner));
                btnAlpha.setImageDrawable(modRes.getDrawable(R.drawable.alpha_btn));
                btnAlpha.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText.setBackgroundColor(Color.TRANSPARENT);
                    }
                });
                ly.addView(btnAlpha, btnAlphaParmas);
                //**End

                //**Init -btnReset- button and add to view.
                RelativeLayout.LayoutParams btnResetParmas = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                btnResetParmas.addRule(RelativeLayout.CENTER_HORIZONTAL);
                btnResetParmas.topMargin = 750;
                btnResetParmas.width = 100;
                btnResetParmas.height = 100;
                ImageButton btnReset = new ImageButton(SnapChatContext);
                btnReset.setBackgroundDrawable(modRes.getDrawable(R.drawable.roundcorner));
                btnReset.setImageDrawable(modRes.getDrawable(R.drawable.alpha_btn));
                btnReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText.setTextColor(Color.WHITE);
                        editText.setTextSize(21);
                        editText.setBackgroundColor(-1728053248);
                    }
                });
                ly.addView(btnReset, btnResetParmas);
                //**End

                //Add our layout to SnapChat's main layout.
                layout.addView(ly, param);
                //End of setting up our views.

                SnapColorsBtn.setOnClickListener(new View.OnClickListener() {
                    boolean SnapColorsBtnBool = true; //To see if the button is pressed agian
                    @Override
                    public void onClick(View v) {
                        if (SnapColorsBtnBool) {
                            ly.setVisibility(View.VISIBLE);
                            SnapColorsBtnBool = false;
                        } else {
                            ly.setVisibility(View.GONE);
                            SnapColorsBtnBool = true;
                        }
                    }
                });

                layout.addView(SnapColorsBtn, params);
            }
        });
    }


	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(SnapChatPKG))
	        return;
        ///// Find the caption box
		final Class<?> CaptionEditText = XposedHelpers.findClass("com.snapchat.android.ui.SnapCaptionView.CaptionEditText", lpparam.classLoader);
        XposedBridge.hookAllConstructors(CaptionEditText, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws NameNotFoundException {
                prefs.reload();
                editText = (EditText) param.thisObject;
                //final GestureDetector gestureDetector = new GestureDetector(SnapChatContext, new GestureDec(SnapChatContext, editText, defTypeFace));
                if (!notFirstRun) {
                    defTypeFace = editText.getTypeface();
                    notFirstRun = true;
                }

//                editText.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View arg0, MotionEvent arg1) {
//                        return gestureDetector.onTouchEvent(arg1);
//                    }
//                });

                // Get stuff from settings here
                editText.setTextColor(prefs.getInt("TextColor", Color.WHITE));
                editText.setBackgroundColor(prefs.getInt("BGColor", -1728053248));
                if (prefs.getBoolean("autoRandomize", false)) {
                    random(editText);
                }
                if (prefs.getBoolean("setFont", false)) {
                    final String fontsDir = SnapChatContext.getExternalFilesDir(null).getAbsolutePath();
                    Typeface face = Typeface.createFromFile(fontsDir + "/" + prefs.getString("Font", "0"));
                    editText.setTypeface(face);
                }
            }
        });

        // For showing the donation msg, and for getting snapchats main context
    	findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				prefs.reload();
                //Getting SnapChat's main context.
				SnapChatContext = (Activity) param.thisObject;
                File SnapColorsVer = new File(SnapChatContext.getExternalFilesDir(null).getAbsolutePath()+"/snapcolors");
                if(SnapColorsVer.createNewFile()){
                    FileUtils.writeStringToFile(SnapColorsVer, SnapChatContext.getPackageManager().getPackageInfo("com.manvir.SnapColors", 0).versionName);
                }else if(!FileUtils.readFileToString(SnapColorsVer).contentEquals(SnapChatContext.getPackageManager().getPackageInfo("com.manvir.SnapColors", 0).versionName)){
                    FileUtils.writeStringToFile(SnapColorsVer, SnapChatContext.getPackageManager().getPackageInfo("com.manvir.SnapColors", 0).versionName);
                }
    		}
    	});
    }
}