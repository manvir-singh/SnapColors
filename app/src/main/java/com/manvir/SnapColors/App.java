package com.manvir.SnapColors;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
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
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manvir.common.PACKAGES;
import com.manvir.common.SETTINGS;
import com.manvir.common.Util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
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
    //Caption related
    private static boolean notFirstRun = false; //Used when getting the default typeface
    private static Typeface defTypeFace;
    boolean imgFromGallery = false;
    //SnapChat
    private Activity SnapChatContext; //Snapchats main activity
    private Resources SnapChatResources;
    private ClassLoader CLSnapChat;
    private Map<String, String> friendsList;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        prefs = new XSharedPreferences(PACKAGES.SNAPCOLORS, SETTINGS.NAME);
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(PACKAGES.SNAPCHAT))
            return;

        modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
        resparam.res.hookLayout(PACKAGES.SNAPCHAT, "layout", "snap_preview", new XC_LayoutInflated() {
            RelativeLayout SnapChatLayout = null;
            RelativeLayout innerOptionsLayout; //Holds all our options the buttons etc (The outer view is a scrollview)

            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                if (prefs.getBoolean(SETTINGS.KEYS.hideT, SETTINGS.DEFAULTS.hideT)) return;
                new Thread(() -> {
                    //Get Snapchats main layout.
                    SnapChatLayout = (RelativeLayout) liparam.view.findViewById(SnapChatResources.getIdentifier("snap_preview_relative_layout", "id", PACKAGES.SNAPCHAT));
                    if (SnapChatLayout == null) //Beta support
                        SnapChatLayout = (RelativeLayout) liparam.view.findViewById(SnapChatResources.getIdentifier("snap_preview_decor_relative_layout", "id", PACKAGES.SNAPCHAT));

                    //LayoutParams for the "T" that shows the options when tapped.
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(liparam.view.findViewById(SnapChatResources.getIdentifier("toggle_emoji_btn", "id", PACKAGES.SNAPCHAT)).getLayoutParams());
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    params.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, SnapChatResources.getDisplayMetrics());
                    params.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 190, SnapChatResources.getDisplayMetrics());

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
                        //noinspection ResourceType
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
                        //noinspection ResourceType
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
                            Util.doFonts(ProgressDialog.show(SnapChatContext, "", "Loading Fonts"), new Handler(SnapChatContext.getMainLooper()), defTypeFace, SnapChatLayout, optionsView, SnapColorsBtn));

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
                        SnapChatEditText.setBackgroundResource(SnapChatResources.getIdentifier("camera_activity_picture_text_message_background", "color", PACKAGES.SNAPCHAT));
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
                    SnapChatContext.runOnUiThread(() -> {
                        SnapChatLayout.addView(optionsView, optionsViewLayoutParams);
                        SnapChatLayout.addView(SnapColorsBtn, params);
                    });
                }).start();
            }
        });
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        //DownloadAI
        if (lpparam.packageName.equals(PACKAGES.SNAPCOLORS))
            findAndHookMethod("com.manvir.common.Util", lpparam.classLoader, "activeVersion", XC_MethodReplacement.returnConstant(BuildConfig.VERSION_CODE));

        //SnapChat
        if (!lpparam.packageName.equals(PACKAGES.SNAPCHAT))
            return;

        if (CLSnapChat == null) CLSnapChat = lpparam.classLoader;
        Class<?> CaptionEditText;
        // Get snapchats activity also reload our settings
        XC_MethodHook startUpHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (SnapChatContext == null) SnapChatContext = (Activity) param.thisObject;
                if (SnapChatResources == null) SnapChatResources = SnapChatContext.getResources();
                Util.setContext(SnapChatContext);
                prefs.reload();

                //For opening image from gallery
                if (!param.method.getName().equals("onCreate")) return;
                Intent intent = (Intent) callMethod(SnapChatContext, "getIntent");
                if (!intent.getBooleanExtra("com.manvir.SnapColors.isSnapColors", false)) return;
                if (intent.getBooleanExtra("com.manvir.SnapColors.isImage", true)) {
                    Method onActivityResult = findMethodExact(PACKAGES.SNAPCHAT + ".LandingPageActivity", CLSnapChat, "onActivityResult",
                            int.class, int.class, Intent.class);
                    Uri image = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
                    intent.setData(image);
                    imgFromGallery = true;
                    new Thread(() -> {
                        SnapChatContext.runOnUiThread(() -> {
                            try {
                                onActivityResult.invoke(SnapChatContext, 1001, -1, intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }).start();
                } else {
                    throw new Exception("Video sharing not supported yet");
//                    Uri video = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
//                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//                    retriever.setDataSource(Util.getRealPathFromURI(video));
//                    Bitmap bmp = retriever.getFrameAtTime();
//                    Object ajm_a;
//                    try {
//                        ajm_a = findConstructorExact("akh.a", CLSnapChat).newInstance(); //Search String TAG = "SnapVideobryo";
//                    } catch (Error beta) {
//                        ajm_a = findConstructorExact("alr.a", CLSnapChat).newInstance(); //Search String TAG = "SnapVideobryo";
//                    }
//                    findField(ajm_a.getClass(), "mSnapType").set(ajm_a, findClass(PACKAGES.SNAPCHAT + ".model.Mediabryo.SnapType", CLSnapChat).getEnumConstants()[0]);
//                    findField(ajm_a.getClass(), "mHeight").set(ajm_a, bmp.getHeight());
//                    findField(ajm_a.getClass(), "mWidth").set(ajm_a, bmp.getWidth());
//                    findField(ajm_a.getClass(), "mVideoUri").set(ajm_a, video);
//                    Class SnapCaptureContext = findClass(PACKAGES.SNAPCHAT + ".util.eventbus.SnapCaptureContext", CLSnapChat);
//                    Object ayt;
//                    try {
//                        ayt = findConstructorExact("bem", CLSnapChat, findClass("ajk", CLSnapChat), SnapCaptureContext).newInstance(callMethod(ajm_a, "c"), SnapCaptureContext.getEnumConstants()[2]);
//                    } catch (NoSuchMethodError beta) {
//                        ayt = findConstructorExact("bfy", CLSnapChat, findClass("ake", CLSnapChat), SnapCaptureContext).newInstance(callMethod(ajm_a, "c"), SnapCaptureContext.getEnumConstants()[2]);
//                    }
//                    callMethod(SnapChatContext, "onSnapCapturedEvent", ayt);
                }
            }
        };
        findAndHookMethod(PACKAGES.SNAPCHAT + ".LandingPageActivity", CLSnapChat, "onCreate", Bundle.class, startUpHook);
        findAndHookMethod(PACKAGES.SNAPCHAT + ".LandingPageActivity", CLSnapChat, "onResume", startUpHook);

        //For opening image from gallery
        findAndHookConstructor("bhv", CLSnapChat, findClass("alp", CLSnapChat), findClass(PACKAGES.SNAPCHAT + ".util.eventbus.SnapCaptureContext", CLSnapChat), View.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!imgFromGallery) return;
                imgFromGallery = false;
                Object SnapType = findClass(PACKAGES.SNAPCHAT + ".model.Mediabryo.SnapType", CLSnapChat).getEnumConstants()[0];
                findField(param.args[0].getClass(), "mSnapType").set(param.args[0], SnapType);
            }
        });

        //Get some settings, also get the caption box's edit text object.
        CaptionEditText = XposedHelpers.findClass(PACKAGES.SNAPCHAT + ".ui.caption.CaptionEditText", CLSnapChat);
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
                SnapChatEditText.setTextColor(prefs.getInt(SETTINGS.KEYS.TextColor, SETTINGS.DEFAULTS.TextColor));
                if (prefs.getInt(SETTINGS.KEYS.BGColor, SETTINGS.DEFAULTS.BGColor) != SETTINGS.DEFAULTS.BGColor) {
                    SnapChatEditText.setBackgroundColor(prefs.getInt(SETTINGS.KEYS.BGColor, SETTINGS.DEFAULTS.BGColor));
                }
                if (prefs.getBoolean(SETTINGS.KEYS.autoRandomize, SETTINGS.DEFAULTS.autoRandomize)) {
                    Util.random(SnapChatEditText);
                }
                if (prefs.getBoolean(SETTINGS.KEYS.setFont, SETTINGS.DEFAULTS.setFont)) {
                    @SuppressWarnings("ConstantConditions") final String fontsDir = SnapChatContext.getExternalFilesDir(null).getAbsolutePath();
                    Typeface face = Typeface.createFromFile(fontsDir + "/" + prefs.getString("Font", "0"));
                    SnapChatEditText.setTypeface(face);
                }
                if (prefs.getBoolean(SETTINGS.KEYS.shouldRainbow, SETTINGS.DEFAULTS.shouldRainbow)) {
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
        findAndHookConstructor(PACKAGES.SNAPCHAT + ".ui.caption.VanillaCaptionEditText", CLSnapChat, Context.class, AttributeSet.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                TextWatcher textWatcher;
                try {
                    textWatcher = (TextWatcher) XposedHelpers.findField(CaptionEditText, "m").get(param.thisObject);
                } catch (ClassCastException BETA) {
                    textWatcher = (TextWatcher) XposedHelpers.findField(CaptionEditText, "p").get(param.thisObject);
                }
                XposedHelpers.callMethod(param.thisObject, "removeTextChangedListener", textWatcher);
                SnapChatEditText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                SnapChatEditText.setSingleLine(false);
            }
        });

        //For locking snaps to show for 10 seconds regardless of what the sender set the view time for (extends Snap)
        findAndHookConstructor("amj", CLSnapChat, String.class, long.class, long.class, long.class, int.class, boolean.class, findClass(PACKAGES.SNAPCHAT + ".model.Snap.ClientSnapStatus", CLSnapChat), String.class, double.class, String.class, boolean.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if ((double) getObjectField(param.thisObject, "mCanonicalDisplayTime") == 0.0)
                    return;
                findField(param.thisObject.getClass(), "mCanonicalDisplayTime").set(param.thisObject, (double) prefs.getInt(SETTINGS.KEYS.minTimerInt, SETTINGS.DEFAULTS.minTimerInt));
            }
        });

        //For disabling screenshot detection
        findAndHookMethod(PACKAGES.SNAPCHAT + ".model.Snap", CLSnapChat, "ap", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!prefs.getBoolean(SETTINGS.KEYS.screenshotDetection, SETTINGS.DEFAULTS.screenshotDetection))
                    return;
                param.setResult(false);
            }
        });

        //For blocking stories so they dont show up in the Stories feed
        findAndHookConstructor(PACKAGES.SNAPCHAT + ".model.Friend", CLSnapChat, String.class, String.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (friendsList == null) friendsList = new HashMap<>();
                String mUsername = (String) getObjectField(param.thisObject, "mUsername");
                String mDisplayName = (String) getObjectField(param.thisObject, "mDisplayName");
                friendsList.put(mUsername, mDisplayName);
            }
        });
        findAndHookMethod(PACKAGES.SNAPCHAT + ".fragments.stories.StoriesAdapter", CLSnapChat, "getView", int.class, View.class, ViewGroup.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ViewGroup listItemView = (ViewGroup) param.getResult();
                String mDisplayName;
                try {
                    mDisplayName = ((TextView) listItemView.findViewById(SnapChatResources.getIdentifier("name", "id", PACKAGES.SNAPCHAT))).getText().toString();
                } catch (NullPointerException ignore) {
                    return;
                }
                String mUsername = "";
                for (Map.Entry<String, String> friend : friendsList.entrySet()) {
                    if (friend.getValue().equals(mDisplayName)) {
                        mUsername = friend.getKey();
                    }
                }
                ArrayList<String> whiteList = new ArrayList<>(
                        prefs.getStringSet(SETTINGS.KEYS.blockStoriesFromList, SETTINGS.DEFAULTS.blockStoriesFromList));
                for (String user : whiteList) {
                    if (mDisplayName.contains(user)) {
                        param.setResult(new View(SnapChatContext));
                    } else if (mUsername.equals(user)) {
                        param.setResult(new View(SnapChatContext));
                    }
                }
            }
        });
    }
}