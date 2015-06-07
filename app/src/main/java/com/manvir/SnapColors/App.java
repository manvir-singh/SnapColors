package com.manvir.SnapColors;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.manvir.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Random;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findConstructorExact;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class App implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {
    //Xposed
    public static XModuleResources modRes;
    //SnapColors options view
    public static RelativeLayout.LayoutParams optionsViewLayoutParams; //Layout params for the main SnapColors options view
    public static Point size;
    public static EditText SnapChatEditText;
    static XSharedPreferences prefs;
    static String MODULE_PATH;
    private static String SnapChatPKG = "com.snapchat.android";
    //Caption related
    private static boolean notFirstRun = false; //Used when getting the default typeface
    private static Typeface defTypeFace;
    boolean imgFromGallery = false;
    //SnapChat
    private Activity SnapChatContext; //Snapchats main activity
    private Resources SnapChatResources;
    private Bitmap mSnapImage;
    private FileInputStream mSnapVideo;

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
        resparam.res.hookLayout(SnapChatPKG, "layout", "snap_image", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam layoutInflatedParam) throws Throwable {
                layoutInflatedParam.view.setBackgroundColor(Color.CYAN);
            }
        });

        resparam.res.hookLayout(SnapChatPKG, "layout", "snap_preview", new XC_LayoutInflated() {
            RelativeLayout SnapChatLayout = null;
            RelativeLayout innerOptionsLayout; //Holds all our options the buttons etc (The outer view is a scrollview)

            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                //Get Snapchats main layout.
                SnapChatLayout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("snap_preview_relative_layout", "id", SnapChatPKG));
                if (SnapChatLayout == null) //Beta support
                    SnapChatLayout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("snap_preview_decor_relative_layout", "id", SnapChatPKG));

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
                            SnapChatEditText.getText().removeSpan(SnapChatEditText.getText().getSpans(i, i + 1, ForegroundColorSpan.class));
                        }
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
        //DownloadAI
        if (lpparam.packageName.equals("com.manvir.SnapColors"))
            findAndHookMethod("com.manvir.SnapColors.Util", lpparam.classLoader, "activeVersion", XC_MethodReplacement.returnConstant(BuildConfig.VERSION_CODE));

        //SnapChat
        if (!lpparam.packageName.equals(SnapChatPKG))
            return;

        Class<?> CaptionEditText;
        // Get snapchats activity also reload our settings
        XC_MethodHook startUpHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (SnapChatContext == null) SnapChatContext = (Activity) param.thisObject;
                if (SnapChatResources == null) SnapChatResources = SnapChatContext.getResources();
                prefs.reload();

                //For opening image from gallery
                if (!param.method.getName().equals("onCreate")) return;
                Intent intent = (Intent) callMethod(SnapChatContext, "getIntent");
                if (!intent.getBooleanExtra("com.manvir.SnapColors.isSnapColors", false)) return;
                Method onActivityResult = findMethodExact(SnapChatPKG + ".LandingPageActivity", lpparam.classLoader, "onActivityResult",
                        int.class, int.class, Intent.class);
                Uri image = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
                intent.setData(image);
                try {
                    imgFromGallery = true;
                    onActivityResult.invoke(SnapChatContext, 1001, -1, intent);
                } catch (Exception theEnd) {
                    theEnd.printStackTrace();
                }
            }
        };
        findAndHookMethod(SnapChatPKG + ".LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, startUpHook);
        findAndHookMethod(SnapChatPKG + ".LandingPageActivity", lpparam.classLoader, "onResume", startUpHook);

        //For opening image from gallery
        Constructor<?> constructor;
        try {
            constructor = findConstructorExact("bdm", lpparam.classLoader, findClass("ahd", lpparam.classLoader), findClass("bdl", lpparam.classLoader));
        } catch (NoSuchMethodError beta) {
            constructor = findConstructorExact("azc", lpparam.classLoader, findClass("aem", lpparam.classLoader), findClass(SnapChatPKG + ".util.eventbus.SnapCaptureContext", lpparam.classLoader));
        }
        XposedBridge.hookMethod(constructor, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!imgFromGallery) return;
                imgFromGallery = false;
                param.args[1] = param.args[1].getClass().getEnumConstants()[2];//bdl.PHONE_GALLERY
                findField(param.args[0].getClass(), "mIsChatMedia").set(param.args[0], false);
            }
        });

        //Get some settings, also get the caption box's edit text object.
        CaptionEditText = XposedHelpers.findClass(SnapChatPKG + ".ui.caption.CaptionEditText", lpparam.classLoader);
        XposedBridge.hookAllConstructors(CaptionEditText, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws NameNotFoundException {
                prefs.reload();
                SnapChatEditText = (EditText) param.thisObject;// Get the Caption box's edit text object.
                SnapChatEditText.setOnEditorActionListener(null);

                //Check to see if the app is being opened for the first time.
                if (!notFirstRun) {
                    defTypeFace = SnapChatEditText.getTypeface();
                    notFirstRun = true;
                }
                // Get stuff from settings here
                SnapChatEditText.setTextColor(prefs.getInt("TextColor", Color.WHITE));
                SnapChatEditText.setBackgroundColor(prefs.getInt("BGColor", -1728053248));
                if (prefs.getBoolean("autoRandomize", false)) {
                    Util.random(SnapChatEditText);
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

        //Everything below this point is for saving snaps
        Method a;
        try {
            a = findMethodExact(SnapChatPKG + ".ui.SnapView", lpparam.classLoader, "a", findClass("aic", lpparam.classLoader), findClass("ahg", lpparam.classLoader), boolean.class, boolean.class);
        } catch (Throwable beta) {
            a = findMethodExact(SnapChatPKG + ".ui.SnapView", lpparam.classLoader, "a", findClass(SnapChatPKG + ".model.ReceivedSnap", lpparam.classLoader), findClass("aeo", lpparam.classLoader), boolean.class, boolean.class);
        }
        XposedBridge.hookMethod(a, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!prefs.getBoolean("shouldSaveSnaps", true)) return;
                //noinspection ResultOfMethodCallIgnored,ConstantConditions
                new File(prefs.getString("saveLocation", Util.SDCARD_SNAPCOLORS)).mkdirs();
                String mSender = (String) getObjectField(param.args[0], "mSender");
                if (mSender == null) { //This means its a story
                    Class<?> afr = findClass("afr", lpparam.classLoader);
                    if (((String)getObjectField(afr.cast(param.args[0]), "mId")).endsWith("BRAND_SNAP"))
                        return; //I don't think you need to save live events
                    mSender = (String) getObjectField(afr.cast(param.args[0]), "mUsername");
                }
                if (mSender == null) return;//Abort mission!
                if (mSnapImage != null) {
                    Logger.log("Saving image sent by: " + mSender);
                    new Thread(new SaveSnapTask(mSender, mSnapImage, 0)).start();
                } else if (mSnapVideo != null) {
                    Logger.log("Saving video sent by: " + mSender);
                    new Thread(new SaveSnapTask(mSender, mSnapVideo, 1)).start();
                }
                mSnapImage = null;
                mSnapVideo = null;
            }
        });

        //Get the video data from file
        findAndHookMethod(VideoView.class, "setVideoURI", Uri.class, Map.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!prefs.getBoolean("shouldSaveSnaps", true)) return;
                //We have to store the file data before snapchat deletes it
                new Thread(() -> {
                    try {
                        mSnapVideo = new FileInputStream(param.args[0].toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });

        //For saving a received image
        findAndHookMethod(ImageView.class, "updateDrawable", Drawable.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!prefs.getBoolean("shouldSaveSnaps", true)) return;
                new Thread(() -> {
                    try {
                        if (!SnapChatResources.getResourceName(((ImageView) param.thisObject).getId()).equals(SnapChatPKG + ":id/snap_image_view"))
                            return;

                        if (((BitmapDrawable) param.args[0]).getBitmap() == null) return;
                        mSnapImage = ((BitmapDrawable) param.args[0]).getBitmap();
                    } catch (NullPointerException | Resources.NotFoundException ignore) {
                        //Sometimes getResourceName is going to return null that's okay
                    }
                }).start();
            }
        });
    }
}