package com.manvir.SnapColors;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manvir.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class App implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {

    //Xposed
    static String MODULE_PATH;
    static XSharedPreferences prefs;
    public static XModuleResources modRes;

    //Misc
    static Activity SnapChatContext; //Snapchats main context
    static boolean notFirstRun = false;
    public static Point size;

    //Package names
    static String SnapChatPKG = "com.snapchat.android";

    //SnapColors options view
    static RelativeLayout innerOptionsLayout; //Holds all out options the buttons etc (The outer view is a scrollview)
    public static RelativeLayout.LayoutParams optionsViewLayoutParams; //Layout params for the main SnapColors options view

    //Caption related
    public static EditText editTextAbstract;
    EditText editText;
    static Typeface defTypeFace;
    Class<?> CaptionEditText;

    //Groups related
    public static List<View> usersList = new ArrayList<>(); //This is a list of all users that get added to the listview
    public static List<Group> groupsList = new ArrayList<>(); //This is a list of all the groups that get added to the listview
    public static ArrayList<String> users = new ArrayList<>(); //Holds the usernames of all the users friends. People the user can send snaps to
    static ArrayList<Object> friendsObjects = new ArrayList<>();
    private static Object SendToFragmentThisObject; //Class object of SendToFragment
    private XSharedPreferences groupsPref;
    public static String[] recipients;
    View SendToBottomPanelView;
    private ListView listView;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        prefs = new XSharedPreferences("com.manvir.SnapColors", "settings");
        groupsPref = new XSharedPreferences("com.manvir.SnapColors", "groups");
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(SnapChatPKG))
            return;
        modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);

        //For adding groups
        resparam.res.hookLayout(SnapChatPKG, "layout", "send_to", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(final LayoutInflatedParam layoutInflatedParam) throws Throwable {
                if (!prefs.getBoolean("shouldGroups", true))
                    return;

                final RelativeLayout SendToActivityLayout = (RelativeLayout)layoutInflatedParam.view;
                listView = (ListView) layoutInflatedParam.view.findViewById(layoutInflatedParam.res.getIdentifier("send_to_list", "id", SnapChatPKG));
                SendToBottomPanelView = SendToActivityLayout.findViewById(layoutInflatedParam.res.getIdentifier("bottom_panel", "id", SnapChatPKG));

                //Add edit groups button to the action bar
                final RelativeLayout topPanle = (RelativeLayout) SendToActivityLayout.findViewById(layoutInflatedParam.res.getIdentifier("action_bar", "id", SnapChatPKG));
                RelativeLayout backArrow = (RelativeLayout) SendToActivityLayout.findViewById(layoutInflatedParam.res.getIdentifier("send_to_back_button_area", "id", SnapChatPKG));
                ImageView searchButton = (ImageView) SendToActivityLayout.findViewById(layoutInflatedParam.res.getIdentifier("send_to_action_bar_search_button", "id", SnapChatPKG));
                final RelativeLayout.LayoutParams editGroupsParams = new RelativeLayout.LayoutParams(
                        SendToActivityLayout.findViewById(layoutInflatedParam.res.getIdentifier("send_to_action_bar_search_button", "id", SnapChatPKG)).getLayoutParams());
                editGroupsParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                editGroupsParams.addRule(RelativeLayout.LEFT_OF, layoutInflatedParam.res.getIdentifier("send_to_action_bar_search_button", "id", SnapChatPKG));
                editGroupsParams.rightMargin = 0;
                final ImageView editGroups = new ImageView(SnapChatContext);
                editGroups.setBackgroundDrawable(modRes.getDrawable(R.drawable.group_btn));

                backArrow.setOnTouchListener(new View.OnTouchListener() { //For showing our action bar button
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() != MotionEvent.ACTION_UP) {
                            editGroups.setVisibility(View.VISIBLE);
                        }
                        return false;
                    }
                });
                searchButton.setOnTouchListener(new View.OnTouchListener() { //For hiding our action bar button
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() != MotionEvent.ACTION_UP) {
                            editGroups.setVisibility(View.INVISIBLE);
                        }
                        return false;
                    }
                });
                editGroups.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putStringArrayListExtra("users", users);
                        intent.setComponent(new ComponentName("com.manvir.SnapColors", "com.manvir.SnapColors.EditGroups"));
                        SnapChatContext.startActivityForResult(intent, 10);
                    }
                });
                topPanle.addView(editGroups, editGroupsParams);

                RelativeLayout divider = (RelativeLayout)View.inflate(SnapChatContext, SnapChatContext.getResources().getIdentifier("blue_list_section_header", "layout", "com.snapchat.android"), null);
                ((TextView)divider.findViewById(SnapChatContext.getResources().getIdentifier("text", "id", App.SnapChatPKG))).setText("GROUPS");
                listView.addFooterView(divider);

                groupsList.add(new Group(SnapChatContext, listView, "Everyone", new String[]{"∞"}, true));

                groupsPref.reload();
                for(Map.Entry<String, ?> entry : groupsPref.getAll().entrySet()){
                    groupsList.add(new Group(SnapChatContext, listView, entry.getKey(), entry.getValue().toString().split(","), false));
                }
            }
        });

        resparam.res.hookLayout(SnapChatPKG, "layout", "snap_preview", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                //Get Snapchats main layout.
                final RelativeLayout SnapChatLayout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("snap_preview_relative_layout", "id",SnapChatPKG));

                //LayoutParams for the "T" that shows the options when tapped.
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn","id",SnapChatPKG)).getLayoutParams());
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, SnapChatContext.getResources().getDisplayMetrics());
                params.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, SnapChatContext.getResources().getDisplayMetrics());

                //The "T" ImageButton object that shows the options when tapped.
                final ImageButton SnapColorsBtn = new ImageButton(SnapChatContext);
                SnapColorsBtn.setBackgroundColor(Color.TRANSPARENT);
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
                optionsView.setBackgroundDrawable(modRes.getDrawable(R.drawable.bgviewdraw));
                optionsView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                optionsView.addView(innerOptionsLayout, new RelativeLayout.LayoutParams(size.x, RelativeLayout.LayoutParams.MATCH_PARENT));

                SButton btnRandomize = new SButton(SnapChatContext, R.drawable.randomize_btn, innerOptionsLayout, 0);//Add 130 to every button
                btnRandomize.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        editTextAbstract.setTextColor(Color.WHITE);
                        editTextAbstract.setBackgroundColor(-1728053248);
                        return true;
                    }
                });
                btnRandomize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Random random = new Random();
                        int colorBG = Color.argb(random.nextInt(256), random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        int colorText = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        editTextAbstract.setBackgroundColor(colorBG);
                        editTextAbstract.setTextColor(colorText);
                    }
                });

                SButton btnTextColor = new SButton(SnapChatContext, R.drawable.textcolor_btn, innerOptionsLayout, 130);
                btnTextColor.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(SnapChatContext, Color.WHITE, new ColorPickerDialog.OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int color) {
                                editTextAbstract.setTextColor(color);
                            }
                        });
                        colorPickerDialog.setButton( Dialog.BUTTON_NEUTRAL, "Default", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                editTextAbstract.setTextColor(Color.WHITE);
                            }
                        });
                        colorPickerDialog.setTitle("Text Color");
                        colorPickerDialog.show();
                    }
                });

                SButton btnBgColor = new SButton(SnapChatContext, R.drawable.bgcolor_btn, innerOptionsLayout, 260);
                btnBgColor.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(SnapChatContext, Color.WHITE, new ColorPickerDialog.OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int color) {
                                editTextAbstract.setBackgroundColor(color);
                            }
                        });
                        colorPickerDialog.setButton( Dialog.BUTTON_NEUTRAL, "Default", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                editTextAbstract.setBackgroundColor(-1728053248);
                            }
                        });
                        colorPickerDialog.setTitle("Background Color");
                        colorPickerDialog.show();
                    }
                });

                SButton btnSize = new SButton(SnapChatContext, R.drawable.size_btn, innerOptionsLayout, 390);
                btnSize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Sizelayout sizelayout = new Sizelayout(SnapChatContext, editTextAbstract, (int) editTextAbstract.getTextSize(), optionsView, SnapColorsBtn);
                        SnapChatLayout.addView(sizelayout, optionsViewLayoutParams);
                    }
                });

                SButton btnAlpha = new SButton(SnapChatContext, R.drawable.alpha_btn, innerOptionsLayout, 520);
                btnAlpha.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlphaLayout alphalayout = new AlphaLayout(SnapChatContext, editTextAbstract, optionsView, SnapColorsBtn);
                        SnapChatLayout.addView(alphalayout, optionsViewLayoutParams);
                    }
                });

                SButton btnFonts = new SButton(SnapChatContext, R.drawable.font_btn, innerOptionsLayout, 650);
                btnFonts.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.doFonts(SnapChatContext, ProgressDialog.show(SnapChatContext, "", "Loading Fonts"), new Handler(SnapChatContext.getMainLooper()), defTypeFace, SnapChatLayout, optionsView, SnapColorsBtn);
                    }
                });

                SButton btnMultiColor = new SButton(SnapChatContext, R.drawable.multicolor_btn, innerOptionsLayout, 780);
                btnMultiColor.setOnClickListener(new View.OnClickListener() {
                    Random random = new Random();
                    @Override
                    public void onClick(View v) {
                        for(int i=0; i< editTextAbstract.getText().length(); i++){
                            SpannableString ss = new SpannableString(editTextAbstract.getText());
                            ss.setSpan(new ForegroundColorSpan(Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))), i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            editTextAbstract.setText(ss);
                        }
                    }
                });

                SButton btnTexture = new SButton(SnapChatContext, R.drawable.texture_btn, innerOptionsLayout, 910);
                btnTexture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new TextureLayout(SnapChatContext, editTextAbstract, optionsView, SnapColorsBtn, SnapChatLayout);
                    }
                });

                SButton btnGradient = new SButton(SnapChatContext, R.drawable.grad_btn, innerOptionsLayout, 1040);
                btnGradient.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GradientLayout gradientLayout = new GradientLayout(SnapChatContext, editTextAbstract, optionsView, SnapColorsBtn);
                        SnapChatLayout.addView(gradientLayout, optionsViewLayoutParams);
                    }
                });

                SButton btnReset = new SButton(SnapChatContext, R.drawable.reset_btn, innerOptionsLayout, 1170);
                btnReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editTextAbstract.getPaint().reset(); //Notice: resets EVERYTHING!
                        editTextAbstract.setText(editTextAbstract.getText().toString()); //Resets the rainbow color.
                        editTextAbstract.setTypeface(defTypeFace);
                        editTextAbstract.setTextColor(Color.WHITE);
                        editTextAbstract.setTextSize(21f);
                        editTextAbstract.setBackgroundResource(SnapChatContext.getResources().getIdentifier("camera_activity_picture_text_message_background", "color", SnapChatPKG));
                    }
                });

                //Add our layout to SnapChat's main layout.
                SnapChatLayout.addView(optionsView, optionsViewLayoutParams);

                SnapColorsBtn.setOnClickListener(new View.OnClickListener() {
                    boolean SnapColorsBtnBool = true; //To see if the button is pressed again.
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

                SnapChatLayout.addView(SnapColorsBtn, params);
            }
        });
    }


	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(SnapChatPKG))
	        return;

        findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onActivityResult", int.class, int.class, Intent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (((Integer) param.args[0]) == 10) { //10 is our activity
                    Logger.log("Refreshing groups");
                    groupsPref.reload(); //Reload the groups from file
                    for (Group group : groupsList) {
                        listView.removeFooterView(group.getView());
                    }

                    groupsList.add(new Group(SnapChatContext, listView, "Everyone", new String[]{"∞"}, true));
                    for(Map.Entry<String, ?> entry : groupsPref.getAll().entrySet()){
                        groupsList.add(new Group(SnapChatContext, listView, entry.getKey(), entry.getValue().toString().split(","), false));
                    }
                }
                param.setResult(null);
            }
        });

        // Hook snapchats "onCreate" method.
        findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                prefs.reload();
                SnapChatContext = (Activity) param.thisObject;

                new DonationDialog(SnapChatContext).show();
                Util.doDonationMsg(SnapChatContext);
            }
        });

        findAndHookMethod("com.snapchat.android.api.SendSnapTask", lpparam.classLoader, "b", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Bundle bundle = (Bundle) param.getResult();

                if (recipients != null) { //"recipients" is only going to be null when the user is not sending to a group
                    for (int index =0; index < recipients.length; index++) {
                        recipients[index] = recipients[index].replace(recipients[index], "\""+recipients[index]+"\"");
                    }
                    bundle.putString("recipients", Arrays.toString(recipients));
                }

                Logger.log(bundle.getString("username"));
                Logger.log(bundle.getString("media_id"));
                Logger.log(bundle.getString("recipients"));
                Logger.log(bundle.getString("time"));
            }
        });

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

        //Get some settings, also get the caption box's edit text object.
        XposedBridge.hookAllConstructors(CaptionEditText, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws NameNotFoundException {
                prefs.reload();
                editTextAbstract = (EditText) param.thisObject;// Get the Caption box's edit text object.

                //Check to see if the app is being opened for the first time.
                if (!notFirstRun) {
                    defTypeFace = editTextAbstract.getTypeface();
                    notFirstRun = true;
                }
                // Get stuff from settings here
                editTextAbstract.setTextColor(prefs.getInt("TextColor", Color.WHITE));
                editTextAbstract.setBackgroundColor(prefs.getInt("BGColor", -1728053248));
                if (prefs.getBoolean("autoRandomize", false)) {
                    new Util().random(editTextAbstract);
                }
                if (prefs.getBoolean("setFont", false)) {
                    final String fontsDir = SnapChatContext.getExternalFilesDir(null).getAbsolutePath();
                    Typeface face = Typeface.createFromFile(fontsDir + "/" + prefs.getString("Font", "0"));
                    editTextAbstract.setTypeface(face);
                }
                if(prefs.getBoolean("shouldRainbow", false)) {
                    editTextAbstract.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        Random random = new Random();
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if(!hasFocus) {
                                for(int i=0; i< editTextAbstract.getText().length(); i++){
                                    SpannableString ss = new SpannableString(editTextAbstract.getText());
                                    ss.setSpan(new ForegroundColorSpan(Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))), i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    editTextAbstract.setText(ss);
                                }
                            }
                        }
                    });
                }
            }
        });

        //For adding multiline support.
        try {
            //For versions below 8.0.0 stable
            XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.snapchat.android.ui.VanillaCaptionView.VanillaCaptionEditText", lpparam.classLoader), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedHelpers.callMethod(editTextAbstract, "removeTextChangedListener",
                            (TextWatcher)XposedHelpers.findField(CaptionEditText, "g").get(param.thisObject));//For removing the character limit set on the caption.
                    EditText cap = (EditText) param.thisObject;
                    Util.doMultiLine(cap);
                }
            });
        }catch (XposedHelpers.ClassNotFoundError e){
            //For versions above 8.1.1 beta
            findAndHookMethod("com.snapchat.android.ui.caption.VanillaCaptionView", lpparam.classLoader, "a", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedHelpers.callMethod(editTextAbstract, "removeTextChangedListener",
                            (TextWatcher)XposedHelpers.findField(CaptionEditText, "l").get(param.getResult()));//For removing the character limit set on the caption.

                    final EditText cap = (EditText) param.getResult();
                    editText = cap;

                    Util.doMultiLine(cap);
                }
            });
        }

        findAndHookMethod("com.emilsjolander.components.stickylistheaders.WrapperView", lpparam.classLoader, "onLayout", boolean.class, int.class, int.class, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!prefs.getBoolean("shouldGroups", true))
                    return;

                final View sendToItem = (View)findField(param.thisObject.getClass(), "mItem").get(param.thisObject);
                if(sendToItem.getId() != View.NO_ID)
                    return;

                usersList.add(sendToItem);
            }
        });

        //For getting the users contacts aka friends
        XposedBridge.hookAllConstructors(findClass("com.snapchat.android.fragments.sendto.SendToAdapter", lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //List friendsList = (List) findField(param.thisObject.getClass(), "e").get(param.thisObject); // Also stores friends
                List friendsList = (List) findField(param.thisObject.getClass(), "d").get(param.thisObject);

                for (Object friendRaw : friendsList) {
                    if (!friendRaw.toString().contains("com.snapchat.android.model.MyPostToStory")) {
                        friendsObjects.add(friendRaw);

                        String[] tempFriend = friendRaw.toString().replace("]", "").split("mUsername=");
                        String friend = tempFriend[1];

                        //Check to see if the user has already been added to the list, also don't add the user teamsnapchat
                        if (!friend.equals("teamsnapchat")) {
                            if (!users.contains(friend)) {
                                Logger.log("Adding user to users list: "+friend);
                                users.add(friend);
                            }
                        }
                    }
                }
            }
        });

        //For checking users in the listview
        findAndHookMethod("com.snapchat.android.fragments.sendto.SendToFragment", lpparam.classLoader, "h", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                SendToFragmentThisObject = param.thisObject;
            }
        });
    }

    public static void unCheckAll() {
        //noinspection unchecked
        LinkedHashSet<Object> l = (LinkedHashSet<Object>) getObjectField(SendToFragmentThisObject, "l");
        l.clear();
        doBottomSendToPanel();
    }

    public static void unCheckUsers(String[] usersToUnSelect) {
        //noinspection unchecked
        LinkedHashSet<Object> l = (LinkedHashSet<Object>) getObjectField(SendToFragmentThisObject, "l");
        for (Object friendObject : friendsObjects) {
            for (String s : usersToUnSelect) {
                if (getObjectField(friendObject, "mUsername").equals(s)) {
                    l.remove(friendObject);
                }
            }
        }
        doBottomSendToPanel();
    }

    public static void checkAll() {
        //noinspection unchecked
        LinkedHashSet<Object> l = (LinkedHashSet<Object>) getObjectField(SendToFragmentThisObject, "l");
        for (Object friendObject : friendsObjects) {
            l.add(friendObject);
        }
        doBottomSendToPanel();
    }

    public static void checkUsers(String[] usersToSelect) {
        //noinspection unchecked
        LinkedHashSet<Object> l = (LinkedHashSet<Object>) getObjectField(SendToFragmentThisObject, "l");
        for (String user : usersToSelect) {
            for (Object friendObject : friendsObjects) {
                if (getObjectField(friendObject, "mUsername").equals(user)) {
                    l.add(friendObject);
                }
            }
        }
        doBottomSendToPanel();
    }

    //Hides or shows the bottom panel and populates the user names
    private static void doBottomSendToPanel() {
        try {
            callMethod(SendToFragmentThisObject, "e");
            callMethod(SendToFragmentThisObject, "g");
        } catch (NoSuchMethodError e) {
            //For beta versions
            callMethod(SendToFragmentThisObject, "b");
            callMethod(SendToFragmentThisObject, "i");
            //In the beta version calling the above methods scrolls the listview to the top so we scroll it back down
            ListView usersListView = (ListView) getObjectField(SendToFragmentThisObject, "j");
            usersListView.setSelection(usersListView.getAdapter().getCount() - 1);
        }
    }
}