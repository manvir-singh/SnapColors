package com.manvir.SnapColors;
// Please don't decompile my code if you want help please ask on the thread thanks =).
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

@SuppressWarnings("UnusedDeclaration")
public class App implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {
    static final String TAG = "SnapColors";
    static String MODULE_PATH;
    static String SnapChatPKG = "com.snapchat.android";
	static XSharedPreferences prefs;
	static Activity SnapChatContext;
	static Typeface defTypeFace;
	static boolean notFirstRun = false;
	static boolean DEBUG = true;
	static Random random = new Random();
    private EditText editText;
    public static Point size;
    public static XModuleResources modRes;

    //Sets the EditText background, and the text color to a random color.
    private void random(EditText textBox) {
        int colorBG = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        int colorText = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        textBox.setBackgroundColor(colorBG);
        textBox.setTextColor(colorText);
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
        modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
        resparam.res.hookLayout(SnapChatPKG, "layout", "snap_preview", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                //Get Snapchats main layout.
                RelativeLayout layout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("snap_preview_relative_layout","id",SnapChatPKG));
                //LayoutParams for the "T" that shows the options when tapped.
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn","id",SnapChatPKG)).getLayoutParams());
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = px(7);
                params.rightMargin = px(110);
                //The "T" ImageButton object that shows the options when tapped.
                final ImageButton SnapColorsBtn = new ImageButton(SnapChatContext);
                SnapColorsBtn.setBackgroundColor(Color.TRANSPARENT);
                SnapColorsBtn.setImageDrawable(modRes.getDrawable(R.drawable.snapcolorsbtn));

                //Get the display params for our layout.
                Display display = SnapChatContext.getWindowManager().getDefaultDisplay();
                size = new Point();
                display.getSize(size);
                final RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                param.width = size.x;
                param.topMargin = px(70);

                //Setup our layout here and add the views, buttons etc.
                final RelativeLayout ly = new RelativeLayout(SnapChatContext);
                ly.setOnClickListener(null);//To prevent touches going though to the layout behind the options layout.
                ly.setBackgroundDrawable(modRes.getDrawable(R.drawable.bgviewdraw));
                ly.setVisibility(View.GONE);

                SButton btnRandomize = new SButton(SnapChatContext, R.drawable.randomize_btn, ly, 70);//Add 130 to every button
                btnRandomize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Random random = new Random();
                        int colorBG = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        int colorText = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        editText.setBackgroundColor(colorBG);
                        editText.setTextColor(colorText);
                    }
                });

                SButton btnTextColor = new SButton(SnapChatContext, R.drawable.textcolor_btn, ly, 200);
                btnTextColor.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        final ColorPicker colorPicker = new ColorPicker(SnapChatContext);
                        colorPicker.OnSelected(new OnColorSelectedListener() {
                            @Override
                            public void onCancel() {
                                editText.setTextColor(Color.WHITE);
                                editText.setTextSize(21);
                                editText.setBackgroundColor(-1728053248);
                                colorPicker.remove();
                            }

                            @Override
                            public void OnSelected(int Color) {
                                editText.setTextColor(Color);
                            }

                            @Override
                            public void OnDone() {
                                colorPicker.remove();
                            }
                        });
                        ly.addView(colorPicker);
                    }
                });

                SButton btnBgColor = new SButton(SnapChatContext, R.drawable.bgcolor_btn, ly, 330);
                btnBgColor.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {

                    }
                });

                SButton btnSize = new SButton(SnapChatContext, R.drawable.size_btn, ly, 460);
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

                SButton btnAlpha = new SButton(SnapChatContext, R.drawable.alpha_btn, ly, 590);
                btnAlpha.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText.setBackgroundColor(Color.TRANSPARENT);
                    }
                });

                SButton btnFonts = new SButton(SnapChatContext, R.drawable.font_btn, ly, 720);
                btnFonts.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.doFonts(SnapChatContext, ProgressDialog.show(SnapChatContext, "", "Loading Fonts"), new Handler(SnapChatContext.getMainLooper()), editText, defTypeFace);
                    }
                });

                SButton btnReset = new SButton(SnapChatContext, R.drawable.reset_btn, ly, 850);
                btnReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText.setTextColor(Color.WHITE);
                        editText.setTextSize(21);
                        editText.setBackgroundColor(-1728053248);
                    }
                });

                //Add our layout to SnapChat's main layout.
                layout.addView(ly, param);

                SnapColorsBtn.setOnClickListener(new View.OnClickListener() {
                    boolean SnapColorsBtnBool = true; //To see if the button is pressed again.
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
        //Get some settings, also get the caption box's edit text object.
		final Class<?> CaptionEditText = XposedHelpers.findClass("com.snapchat.android.ui.SnapCaptionView.CaptionEditText", lpparam.classLoader);
        XposedBridge.hookAllConstructors(CaptionEditText, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws NameNotFoundException {
                prefs.reload();// Reload our prefs
                editText = (EditText) param.thisObject;// Get the Caption box's edit text object.
                //Check to see if the app is being opened for the first time.
                if (!notFirstRun) {
                    defTypeFace = editText.getTypeface();
                    notFirstRun = true;
                }
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

        // For getting snapchats main context.
    	findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				prefs.reload();//Reload prefs
                //Getting SnapChat's main context object.
				SnapChatContext = (Activity) param.thisObject;
    		}
    	});
    }
}