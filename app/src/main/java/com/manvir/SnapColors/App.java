package com.manvir.SnapColors;

import java.util.Random;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextWatcher;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

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

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

@SuppressWarnings("UnusedDeclaration")
public class App implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {
    static final String TAG = "SnapColors";
    static String MODULE_PATH;
    static String SnapChatPKG = "com.snapchat.android";
	static XSharedPreferences prefs;
	static Activity SnapChatContext;
	static Typeface defTypeFace;
	static boolean notFirstRun = false;
	static boolean DEBUG = false;
    public static EditText editText;
    public static Point size;
    public static XModuleResources modRes;
    Class<?> CaptionEditText;
    static RelativeLayout ly;
    public static RelativeLayout.LayoutParams param;
	
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
                final RelativeLayout SnapChatLayout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("snap_preview_relative_layout","id",SnapChatPKG));
                //LayoutParams for the "T" that shows the options when tapped.
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn","id",SnapChatPKG)).getLayoutParams());
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = new Util(SnapChatContext).px(7);
                params.rightMargin = new Util(SnapChatContext).px(110);
                //The "T" ImageButton object that shows the options when tapped.
                final ImageButton SnapColorsBtn = new ImageButton(SnapChatContext);
                SnapColorsBtn.setBackgroundColor(Color.TRANSPARENT);
                SnapColorsBtn.setImageDrawable(modRes.getDrawable(R.drawable.snapcolorsbtn));

                //Get the display params for our layout.
                Display display = SnapChatContext.getWindowManager().getDefaultDisplay();
                size = new Point();
                display.getSize(size);
                param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                param.topMargin = new Util(SnapChatContext).px(70);

                //Setup our layout here and add the views, buttons etc.
                final HorizontalScrollView f = new HorizontalScrollView(SnapChatContext);
                ly = new RelativeLayout(SnapChatContext);
                ly.setOnClickListener(null);//To prevent touches going though to the layout behind the options layout.
                ly.setPadding(70, 50, 70, 50);
                f.setVisibility(View.GONE);
                f.setBackgroundDrawable(modRes.getDrawable(R.drawable.bgviewdraw));
                f.setOverScrollMode(View.OVER_SCROLL_NEVER);
                f.addView(ly, new RelativeLayout.LayoutParams(size.x, RelativeLayout.LayoutParams.MATCH_PARENT));

                SButton btnRandomize = new SButton(SnapChatContext, R.drawable.randomize_btn, ly, 0);//Add 130 to every button
                btnRandomize.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        editText.setTextColor(Color.WHITE);
                        editText.setBackgroundColor(-1728053248);
                        return true;
                    }
                });

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

                SButton btnTextColor = new SButton(SnapChatContext, R.drawable.textcolor_btn, ly, 130);
                btnTextColor.setOnClickListener(new View.OnClickListener(){
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

                SButton btnBgColor = new SButton(SnapChatContext, R.drawable.bgcolor_btn, ly, 260);
                btnBgColor.setOnClickListener(new View.OnClickListener(){
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

                SButton btnSize = new SButton(SnapChatContext, R.drawable.size_btn, ly, 390);
                btnSize.setOnClickListener(new View.OnClickListener() {
                    boolean wasClickedAlready = false;
                    @Override
                    public void onClick(View v) {
                        Sizelayout sizelayout = new Sizelayout(SnapChatContext, editText, (int)editText.getTextSize(), f, SnapColorsBtn);
                        SnapChatLayout.addView(sizelayout, param);
                    }
                });

                SButton btnAlpha = new SButton(SnapChatContext, R.drawable.alpha_btn, ly, 520);
                btnAlpha.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText.setBackgroundColor(Color.TRANSPARENT);
                    }
                });

                SButton btnFonts = new SButton(SnapChatContext, R.drawable.font_btn, ly, 650);
                btnFonts.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.doFonts(SnapChatContext, ProgressDialog.show(SnapChatContext, "", "Loading Fonts"), new Handler(SnapChatContext.getMainLooper()), defTypeFace, SnapChatLayout, f, SnapColorsBtn);
                    }
                });

                SButton btnReset = new SButton(SnapChatContext, R.drawable.reset_btn, ly, 780);
                btnReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText.setTypeface(defTypeFace);
                        editText.setTextColor(Color.WHITE);
                        editText.setTextSize(21);
                        editText.setBackgroundColor(-1728053248);
                    }
                });

                //Add our layout to SnapChat's main layout.
                SnapChatLayout.addView(f, param);

                SnapColorsBtn.setOnClickListener(new View.OnClickListener() {
                    boolean SnapColorsBtnBool = true; //To see if the button is pressed again.
                    @Override
                    public void onClick(View v) {
                        if (SnapColorsBtnBool) {
                            f.setVisibility(View.VISIBLE);
                            SnapColorsBtnBool = false;
                        } else {
                            f.setVisibility(View.GONE);
                            SnapColorsBtnBool = true;
                        }
                    }
                });
                SnapChatLayout.addView(SnapColorsBtn, params);
            }
        });
    }


	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(SnapChatPKG))
	        return;

        /*
        *The location of the "CaptionEditText" class in the beta version of SnapChat is different then in
        *SnapChat stable, so we check to see if the class was found, if not then we are most likely using the beta version
        *of SnapChat.
        */
        try {
            //For stable versions
            CaptionEditText = XposedHelpers.findClass("com.snapchat.android.ui.SnapCaptionView.CaptionEditText", lpparam.classLoader);
        }catch (XposedHelpers.ClassNotFoundError e){
            //For beta versions
            CaptionEditText = XposedHelpers.findClass("com.snapchat.android.ui.caption.CaptionEditText", lpparam.classLoader);
        }

        //Get some settings, also get the caption box's edit object.
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
                    new Util().random(editText);
                }
                if (prefs.getBoolean("setFont", false)) {
                    final String fontsDir = SnapChatContext.getExternalFilesDir(null).getAbsolutePath();
                    Typeface face = Typeface.createFromFile(fontsDir + "/" + prefs.getString("Font", "0"));
                    editText.setTypeface(face);
                }
            }
        });

        // Hook snapchats "onCreate" method.
    	findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
			@Override
    		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				prefs.reload();//Reload prefs
                //Getting SnapChat's main activity object.
				SnapChatContext = (Activity) param.thisObject;

                Util.doDonationMsg(SnapChatContext);
    		}
    	});

        //For adding multiline support.
        try {
            XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.snapchat.android.ui.VanillaCaptionView.VanillaCaptionEditText", lpparam.classLoader), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    //For stable versions
                    XposedHelpers.callMethod(editText, "removeTextChangedListener",
                            (TextWatcher)XposedHelpers.findField(CaptionEditText, "g").get(param.thisObject));//For removing the character limit set on the caption.
                    EditText cap = (EditText) param.thisObject;
                    Util.doMultiLine(cap);
                }
            });
        }catch (XposedHelpers.ClassNotFoundError e){
            //For beta versions
            findAndHookMethod("com.snapchat.android.ui.caption.VanillaCaptionView", lpparam.classLoader, "a", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedHelpers.callMethod(editText, "removeTextChangedListener",
                            (TextWatcher)XposedHelpers.findField(CaptionEditText, "l").get(param.getResult()));//For removing the character limit set on the caption.
                    EditText cap = (EditText) param.getResult();
                    Util.doMultiLine(cap);
                }
            });
        }
    }
}