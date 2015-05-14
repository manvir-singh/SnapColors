package com.manvir.SnapColors;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.Random;

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

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class App implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {
    //Xposed
    public static XModuleResources modRes;
    //SnapColors options view
    public static RelativeLayout.LayoutParams optionsViewLayoutParams; //Layout params for the main SnapColors options view
    public static Point size;
    public static EditText SnapChatEditText;
    static Activity SnapChatContext; //Snapchats main activity
    static XSharedPreferences prefs;
    static String MODULE_PATH;
    static RelativeLayout innerOptionsLayout; //Holds all out options the buttons etc (The outer view is a scrollview)
    //Caption related
    private static boolean notFirstRun = false; //Used when getting the default typeface
    private static Typeface defTypeFace;
    //Package names
    private static String SnapChatPKG = "com.snapchat.android";
    Class<?> CaptionEditText;

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
                final RelativeLayout SnapChatLayout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("snap_preview_relative_layout", "id", SnapChatPKG));

                //LayoutParams for the "T" that shows the options when tapped.
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", SnapChatPKG)).getLayoutParams());
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, SnapChatContext.getResources().getDisplayMetrics());
                params.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, SnapChatContext.getResources().getDisplayMetrics());

                //The "T" ImageButton object that shows the options when tapped.
                final ImageButton SnapColorsBtn = new ImageButton(SnapChatContext);
                SnapColorsBtn.setBackgroundColor(Color.TRANSPARENT);
                //noinspection deprecation
                SnapColorsBtn.setImageDrawable(modRes.getDrawable(R.drawable.snapcolorsbtn));

                //Get the display params for our layout.
                Display display = SnapChatContext.getWindowManager().getDefaultDisplay();
                size = new Point();
                display.getSize(size);
                optionsViewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                optionsViewLayoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, SnapChatContext.getResources().getDisplayMetrics());

                //Setup our layout here and add the views, buttons etc.
                //optionsView is the scroll view that's going to hold our inner RelativeLayout called "innerOptionsLayout"
                final HorizontalScrollView optionsView = new HorizontalScrollView(SnapChatContext);
                innerOptionsLayout = new RelativeLayout(SnapChatContext);
                innerOptionsLayout.setOnClickListener(null);//To prevent touches going though to the layout behind the options layout.
                int LeftRightMarginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, SnapChatContext.getResources().getDisplayMetrics());
                int TopBottomMarginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13, SnapChatContext.getResources().getDisplayMetrics());
                innerOptionsLayout.setPadding(LeftRightMarginPx, TopBottomMarginPx, LeftRightMarginPx, TopBottomMarginPx);
                optionsView.setVisibility(View.GONE);
                //noinspection deprecation
                optionsView.setBackgroundDrawable(modRes.getDrawable(R.drawable.bgviewdraw));
                optionsView.setScrollbarFadingEnabled(false);
                optionsView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                optionsView.addView(innerOptionsLayout, new RelativeLayout.LayoutParams(size.x, RelativeLayout.LayoutParams.MATCH_PARENT));

                SButton btnRandomize = new SButton(SnapChatContext, R.drawable.randomize_btn, innerOptionsLayout, 0);//Add 130 to every button
                btnRandomize.setOnLongClickListener(v -> {
                    SnapChatEditText.setTextColor(Color.WHITE);
                    SnapChatEditText.setBackgroundColor(-1728053248);
                    return true;
                });
                btnRandomize.setOnClickListener(view -> {
                    Random random = new Random();
                    int colorBG = Color.argb(random.nextInt(256), random.nextInt(256), random.nextInt(256), random.nextInt(256));
                    int colorText = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                    SnapChatEditText.setBackgroundColor(colorBG);
                    SnapChatEditText.setTextColor(colorText);
                });

                SButton btnTextColor = new SButton(SnapChatContext, R.drawable.textcolor_btn, innerOptionsLayout, 130);
                btnTextColor.setOnClickListener(view -> {
                    ColorPickerDialog colorPickerDialog = new ColorPickerDialog(SnapChatContext, Color.WHITE, SnapChatEditText::setTextColor);
                    colorPickerDialog.setButton(Dialog.BUTTON_NEUTRAL, "Default", (dialog, which) -> SnapChatEditText.setTextColor(Color.WHITE));
                    colorPickerDialog.setTitle("Text Color");
                    colorPickerDialog.show();
                });

                SButton btnBgColor = new SButton(SnapChatContext, R.drawable.bgcolor_btn, innerOptionsLayout, 260);
                btnBgColor.setOnClickListener(view -> {
                    ColorPickerDialog colorPickerDialog = new ColorPickerDialog(SnapChatContext, Color.WHITE, SnapChatEditText::setBackgroundColor);
                    colorPickerDialog.setButton(Dialog.BUTTON_NEUTRAL, "Default", (dialog, which) -> SnapChatEditText.setBackgroundColor(-1728053248));
                    colorPickerDialog.setTitle("Background Color");
                    colorPickerDialog.show();
                });

                SButton btnSize = new SButton(SnapChatContext, R.drawable.size_btn, innerOptionsLayout, 390);
                btnSize.setOnClickListener(v -> {
                    Sizelayout sizelayout = new Sizelayout(SnapChatContext, SnapChatEditText, (int) SnapChatEditText.getTextSize(), optionsView, SnapColorsBtn);
                    SnapChatLayout.addView(sizelayout, optionsViewLayoutParams);
                });

                SButton btnAlpha = new SButton(SnapChatContext, R.drawable.alpha_btn, innerOptionsLayout, 520);
                btnAlpha.setOnClickListener(v -> {
                    AlphaLayout alphalayout = new AlphaLayout(SnapChatContext, SnapChatEditText, optionsView, SnapColorsBtn);
                    SnapChatLayout.addView(alphalayout, optionsViewLayoutParams);
                });

                SButton btnFonts = new SButton(SnapChatContext, R.drawable.font_btn, innerOptionsLayout, 650);
                btnFonts.setOnClickListener(v ->
                        Util.doFonts(SnapChatContext, ProgressDialog.show(SnapChatContext, "", "Loading Fonts"), new Handler(SnapChatContext.getMainLooper()), defTypeFace, SnapChatLayout, optionsView, SnapColorsBtn));

                SButton btnMultiColor = new SButton(SnapChatContext, R.drawable.multicolor_btn, innerOptionsLayout, 780);
                btnMultiColor.setOnClickListener(new View.OnClickListener() {
                    Random random = new Random();

                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < SnapChatEditText.getText().length(); i++) {
                            SpannableString ss = new SpannableString(SnapChatEditText.getText());
                            ss.setSpan(new ForegroundColorSpan(Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            SnapChatEditText.setText(ss);
                        }
                    }
                });

                SButton btnTexture = new SButton(SnapChatContext, R.drawable.texture_btn, innerOptionsLayout, 910);
                btnTexture.setOnClickListener(v -> new TextureLayout(SnapChatContext, SnapChatEditText, optionsView, SnapColorsBtn, SnapChatLayout));

                SButton btnGradient = new SButton(SnapChatContext, R.drawable.grad_btn, innerOptionsLayout, 1040);
                btnGradient.setOnClickListener(v -> {
                    GradientLayout gradientLayout = new GradientLayout(SnapChatContext, SnapChatEditText, optionsView, SnapColorsBtn);
                    SnapChatLayout.addView(gradientLayout, optionsViewLayoutParams);
                });

                SButton btnReset = new SButton(SnapChatContext, R.drawable.reset_btn, innerOptionsLayout, 1170);
                btnReset.setOnClickListener(v -> {
                    SnapChatEditText.getPaint().reset(); //Notice: resets EVERYTHING!
                    SnapChatEditText.setText(SnapChatEditText.getText().toString()); //Resets the rainbow color.
                    SnapChatEditText.setTypeface(defTypeFace);
                    SnapChatEditText.setTextColor(Color.WHITE);
                    SnapChatEditText.setTextSize(21f);
                    SnapChatEditText.setBackgroundResource(SnapChatContext.getResources().getIdentifier("camera_activity_picture_text_message_background", "color", SnapChatPKG));
                });

                SnapColorsBtn.setOnClickListener(new View.OnClickListener() {
                    boolean SnapColorsBtnBool = true;

                    @Override
                    public void onClick(View v) {
                        if (SnapColorsBtnBool) {
                            optionsView.setVisibility(View.VISIBLE);
                            SnapColorsBtnBool = false;
                        } else {
                            optionsView.setVisibility(View.GONE);
                            SnapColorsBtnBool = true;
                        }
                    }
                });

                //Add our layout to SnapChat's main layout.
                SnapChatLayout.addView(optionsView, optionsViewLayoutParams);
                SnapChatLayout.addView(SnapColorsBtn, params);
            }
        });
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(SnapChatPKG))
            return;

        // Get snapchats activity also reload our settings
        XC_MethodHook startUpHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (SnapChatContext == null) SnapChatContext = (Activity) param.thisObject;
                prefs.reload();
            }
        };
        findAndHookMethod(SnapChatPKG + ".LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, startUpHook);
        findAndHookMethod(SnapChatPKG + ".LandingPageActivity", lpparam.classLoader, "onResume", startUpHook);

        CaptionEditText = XposedHelpers.findClass(SnapChatPKG + ".ui.caption.CaptionEditText", lpparam.classLoader);
        //Get some settings, also get the caption box's edit text object.
        XposedBridge.hookAllConstructors(CaptionEditText, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws NameNotFoundException {
                prefs.reload();
                SnapChatEditText = (EditText) param.thisObject;// Get the Caption box's edit text object.

                //Check to see if the app is being opened for the first time.
                if (!notFirstRun) {
                    defTypeFace = SnapChatEditText.getTypeface();
                    notFirstRun = true;
                }
                // Get stuff from settings here
                SnapChatEditText.setTextColor(prefs.getInt("TextColor", Color.WHITE));
                SnapChatEditText.setBackgroundColor(prefs.getInt("BGColor", -1728053248));
                if (prefs.getBoolean("autoRandomize", false)) {
                    new Util().random(SnapChatEditText);
                }
                if (prefs.getBoolean("setFont", false)) {
                    @SuppressWarnings("ConstantConditions") final String fontsDir = SnapChatContext.getExternalFilesDir(null).getAbsolutePath();
                    Typeface face = Typeface.createFromFile(fontsDir + "/" + prefs.getString("Font", "0"));
                    SnapChatEditText.setTypeface(face);
                }
                if (prefs.getBoolean("shouldRainbow", false)) {
                    SnapChatEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        Random random = new Random();

                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus) {
                                for (int i = 0; i < SnapChatEditText.getText().length(); i++) {
                                    SpannableString ss = new SpannableString(SnapChatEditText.getText());
                                    ss.setSpan(new ForegroundColorSpan(Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    SnapChatEditText.setText(ss);
                                }
                            }
                        }
                    });
                }
            }
        });

        //For adding multiline support
        findAndHookConstructor(SnapChatPKG + ".ui.caption.VanillaCaptionEditText", lpparam.classLoader, Context.class, AttributeSet.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                TextWatcher textWatcher;
                try {
                    textWatcher = (TextWatcher) XposedHelpers.findField(CaptionEditText, "m").get(param.thisObject);
                } catch (ClassCastException BETA) {
                    textWatcher = (TextWatcher) XposedHelpers.findField(CaptionEditText, "p").get(param.thisObject);
                }
                XposedHelpers.callMethod(param.thisObject, "removeTextChangedListener", textWatcher);
                Util.doMultiLine(SnapChatEditText);
            }
        });
    }
}