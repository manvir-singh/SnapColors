package com.manvir.SnapColors;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
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
import android.util.AttributeSet;
import android.util.Log;
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

import com.esotericsoftware.kryo.Kryo;
import com.manvir.logger.Logger;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
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
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class App implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {

    public static XModuleResources modRes;
    public static Point size;
    public static RelativeLayout.LayoutParams optionsViewLayoutParams; //Layout params for the main SnapColors options view
    //Caption related
    public static EditText editTextAbstract;
    //Groups related
    public static ArrayList<String> users; //Holds the usernames of all the users friends. People the user can send snaps to
    static ArrayList<Object> friendsObjects;
    //Xposed
    static String MODULE_PATH;
    static XSharedPreferences prefs;
    //Misc
    static Activity SnapChatContext; //Snapchats main context
    static boolean notFirstRun = false;
    //Package names
    static String SnapChatPKG = "com.snapchat.android";
    //SnapColors options view
    static RelativeLayout innerOptionsLayout; //Holds all out options the buttons etc (The outer view is a scrollview)
    static Typeface defTypeFace;
    static View SendToBottomPanelView;
    private static Object SendToFragmentThisObject; //Class object of SendToFragment
    EditText editText;
    Class<?> CaptionEditText;
    private List<String> groupsList; //This is a list of all the groups that get added to the listview
    private XSharedPreferences groupsPref;
    boolean isEveryone;
    boolean isGroup;

    public static void unCheckUsers(String[] usersToUnSelect) {
        LinkedHashSet<Object> l;
        try {
            //noinspection unchecked
            l = (LinkedHashSet<Object>) getObjectField(SendToFragmentThisObject, "l");
        } catch (Exception e) {
            //noinspection unchecked
            l = (LinkedHashSet<Object>) getObjectField(SendToFragmentThisObject, "m");
        }
        for (Object friendObject : friendsObjects) {
            for (String s : usersToUnSelect) {
                if (getObjectField(friendObject, "mUsername").equals(s)) {
                    l.remove(friendObject);
                }
            }
        }
        doBottomSendToPanel();
    }

    public static void checkUsers(String[] usersToSelect) {
        if (SendToFragmentThisObject == null) {
            Logger.log("SendToFragmentThisObject is nullllllllllllllllllll");
        }
        LinkedHashSet<Object> l;
        try {
            //noinspection unchecked
            l = (LinkedHashSet<Object>) getObjectField(SendToFragmentThisObject, "l");
        } catch (Exception e) {
            //noinspection unchecked
            l = (LinkedHashSet<Object>) getObjectField(SendToFragmentThisObject, "m");
        }
        for (String user : usersToSelect) {
            try {
                for (Object friendObject : friendsObjects) {
                    if (getObjectField(friendObject, "mUsername").equals(user)) {
                        l.add(friendObject);
                    }
                }
            } catch (NoSuchFieldError ignore) {
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
            //In the beta version calling the above methods scrolls the listview to the top so we scroll it back down
            ListView usersListView = null;
            try {
                usersListView = (ListView) getObjectField(SendToFragmentThisObject, "j");
            } catch (ClassCastException err) {
                try {
                    usersListView = (ListView) getObjectField(SendToFragmentThisObject, "k");
                } catch (ClassCastException ignore) {
                }
            }

            if (usersListView != null) {
                usersListView.setSelection(usersListView.getAdapter().getCount() - 1);
            }
        }
    }

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

                RelativeLayout SendToActivityLayout = (RelativeLayout) layoutInflatedParam.view;

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

                backArrow.setOnTouchListener((v, event) -> {
                    if (event.getAction() != MotionEvent.ACTION_UP) {
                        editGroups.setVisibility(View.VISIBLE);
                    }
                    return false;
                });
                searchButton.setOnTouchListener((v, event) -> {
                    if (event.getAction() != MotionEvent.ACTION_UP) {
                        editGroups.setVisibility(View.INVISIBLE);
                    }
                    return false;
                });
                editGroups.setOnClickListener(v -> {
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra("users", users);
                    intent.setComponent(new ComponentName("com.manvir.SnapColors", "com.manvir.SnapColors.EditGroups"));
                    SnapChatContext.startActivityForResult(intent, 10);
                });
                topPanle.addView(editGroups, editGroupsParams);

                groupsPref.reload();
            }
        });

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
                    editTextAbstract.setTextColor(Color.WHITE);
                    editTextAbstract.setBackgroundColor(-1728053248);
                    return true;
                });
                btnRandomize.setOnClickListener(view -> {
                    Random random = new Random();
                    int colorBG = Color.argb(random.nextInt(256), random.nextInt(256), random.nextInt(256), random.nextInt(256));
                    int colorText = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                    editTextAbstract.setBackgroundColor(colorBG);
                    editTextAbstract.setTextColor(colorText);
                });

                SButton btnTextColor = new SButton(SnapChatContext, R.drawable.textcolor_btn, innerOptionsLayout, 130);
                btnTextColor.setOnClickListener(view -> {
                    ColorPickerDialog colorPickerDialog = new ColorPickerDialog(SnapChatContext, Color.WHITE, editTextAbstract::setTextColor);
                    colorPickerDialog.setButton(Dialog.BUTTON_NEUTRAL, "Default", (dialog, which) -> editTextAbstract.setTextColor(Color.WHITE));
                    colorPickerDialog.setTitle("Text Color");
                    colorPickerDialog.show();
                });

                SButton btnBgColor = new SButton(SnapChatContext, R.drawable.bgcolor_btn, innerOptionsLayout, 260);
                btnBgColor.setOnClickListener(view -> {
                    ColorPickerDialog colorPickerDialog = new ColorPickerDialog(SnapChatContext, Color.WHITE, editTextAbstract::setBackgroundColor);
                    colorPickerDialog.setButton(Dialog.BUTTON_NEUTRAL, "Default", (dialog, which) -> editTextAbstract.setBackgroundColor(-1728053248));
                    colorPickerDialog.setTitle("Background Color");
                    colorPickerDialog.show();
                });

                SButton btnSize = new SButton(SnapChatContext, R.drawable.size_btn, innerOptionsLayout, 390);
                btnSize.setOnClickListener(v -> {
                    Sizelayout sizelayout = new Sizelayout(SnapChatContext, editTextAbstract, (int) editTextAbstract.getTextSize(), optionsView, SnapColorsBtn);
                    SnapChatLayout.addView(sizelayout, optionsViewLayoutParams);
                });

                SButton btnAlpha = new SButton(SnapChatContext, R.drawable.alpha_btn, innerOptionsLayout, 520);
                btnAlpha.setOnClickListener(v -> {
                    AlphaLayout alphalayout = new AlphaLayout(SnapChatContext, editTextAbstract, optionsView, SnapColorsBtn);
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
                        for (int i = 0; i < editTextAbstract.getText().length(); i++) {
                            SpannableString ss = new SpannableString(editTextAbstract.getText());
                            ss.setSpan(new ForegroundColorSpan(Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            editTextAbstract.setText(ss);
                        }
                    }
                });

                SButton btnTexture = new SButton(SnapChatContext, R.drawable.texture_btn, innerOptionsLayout, 910);
                btnTexture.setOnClickListener(v -> new TextureLayout(SnapChatContext, editTextAbstract, optionsView, SnapColorsBtn, SnapChatLayout));

                SButton btnGradient = new SButton(SnapChatContext, R.drawable.grad_btn, innerOptionsLayout, 1040);
                btnGradient.setOnClickListener(v -> {
                    GradientLayout gradientLayout = new GradientLayout(SnapChatContext, editTextAbstract, optionsView, SnapColorsBtn);
                    SnapChatLayout.addView(gradientLayout, optionsViewLayoutParams);
                });

                SButton btnReset = new SButton(SnapChatContext, R.drawable.reset_btn, innerOptionsLayout, 1170);
                btnReset.setOnClickListener(v -> {
                    editTextAbstract.getPaint().reset(); //Notice: resets EVERYTHING!
                    editTextAbstract.setText(editTextAbstract.getText().toString()); //Resets the rainbow color.
                    editTextAbstract.setTypeface(defTypeFace);
                    editTextAbstract.setTextColor(Color.WHITE);
                    editTextAbstract.setTextSize(21f);
                    editTextAbstract.setBackgroundResource(SnapChatContext.getResources().getIdentifier("camera_activity_picture_text_message_background", "color", SnapChatPKG));
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

        findAndHookMethod("com.snapchat.android.ui.SendToBottomPanelView", lpparam.classLoader, "setText", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    isEveryone = ((String) param.args[0]).contains("Everyone, ");
                    for (String groupName : groupsList) {
                        isGroup = ((String) param.args[0]).contains(groupName);
                    }

                    String arg = ((String) param.args[0]).replace("Everyone, ", "");

                    for (String groupName : groupsList) {
                        if (arg.contains(groupName)) {
                            arg = arg.replace(groupName + ", ", "");
                        }
                    }

                    param.args[0] = arg;
                } catch (NullPointerException ignore){}
            }
        });

        try {
            findAndHookMethod("com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration", lpparam.classLoader, "a",
                    findClass("android.support.v7.widget.RecyclerView", lpparam.classLoader), int.class, new XC_MethodHook() {
                        boolean flag = false;

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!prefs.getBoolean("shouldGroups", true))
                                return;

                            TextView stickyHeader = (TextView) param.getResult();
                            if (flag && stickyHeader.getText().toString().equals("RECENTS")) {
                                stickyHeader.setText("GROUPS");
                                flag = false;
                            } else if (stickyHeader.getText().toString().equals("RECENTS")) {
                                flag = true;
                            } else if (!flag && stickyHeader.getText().toString().equals("GROUPS")) {
                                stickyHeader.setText("RECENTS");
                                flag = true;
                            }
                        }
                    });
        } catch (XposedHelpers.ClassNotFoundError ignore) {
//            //TODO Add header "GROUPS"
//            findAndHookMethod("com.emilsjolander.components.stickylistheaders.AdapterWrapper", lpparam.classLoader, "getHeaderView", int.class, View.class, ViewGroup.class, new XC_MethodHook() {
//                boolean flag = false;
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    if (!prefs.getBoolean("shouldGroups", true))
//                        return;
//
//                    TextView stickyHeader = (TextView) param.getResult();
//                    if (flag && stickyHeader.getText().toString().equals("RECENTS")) {
//                        stickyHeader.setText("GROUPS");
//                        flag = false;
//                    } else if (stickyHeader.getText().toString().equals("RECENTS")) {
//                        flag = true;
//                    } else if (!flag && stickyHeader.getText().toString().equals("GROUPS")) {
//                        stickyHeader.setText("RECENTS");
//                        flag = true;
//                    }
//                }
//            });
        }

        findAndHookMethod("com.snapchat.android.fragments.sendto.SendToFragment", lpparam.classLoader, "a", findClass("com.snapchat.android.model.Friend", lpparam.classLoader), boolean.class, new XC_MethodHook() {
            ArrayList<String> groupsChecked = new ArrayList<>();

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!prefs.getBoolean("shouldGroups", true))
                    return;
                Object friend = param.args[0];

                String mUsername = (String) findField(friend.getClass(), "mUsername").get(friend);
                if (!mUsername.contains(",")) //If the username field contains commas its a group
                    return;

                String GroupName = (String) findField(friend.getClass(), "mDisplayName").get(friend);
                if (!groupsChecked.contains(GroupName)) {
                    groupsChecked.add(GroupName);
                    checkUsers(mUsername.split(","));
                } else {
                    groupsChecked.remove(GroupName);
                    unCheckUsers(mUsername.split(","));
                }
            }
        });

        XposedBridge.hookAllConstructors(findClass("com.snapchat.android.fragments.sendto.SendToAdapter", lpparam.classLoader), new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!prefs.getBoolean("shouldGroups", true))
                    return;

                List<Object> j = (List<Object>) getObjectField(param.thisObject, "j");
                List<Object> i = (List<Object>) getObjectField(param.thisObject, "i");
                Kryo cloner = new Kryo();

                Object everyOne = cloner.copy(j.get(2));
                findField(everyOne.getClass(), "mUsername").set(everyOne, StringUtils.join(users, ','));
                findField(everyOne.getClass(), "mDisplayName").set(everyOne, "Everyone");
                j.add(everyOne);
                i.add(everyOne);

                for (Map.Entry<String, ?> entry : groupsPref.getAll().entrySet()) {
                    String groupName = entry.getKey();
                    String users = entry.getValue().toString();

                    Object friend = cloner.copy(j.get(2));
                    findField(friend.getClass(), "mUsername").set(friend, users);
                    findField(friend.getClass(), "mDisplayName").set(friend, groupName);
                    j.add(friend);
                    i.add(friend);

                    if (groupsList == null) groupsList = new ArrayList<>();
                    groupsList.add(groupName);
                }
            }
        });

        findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onActivityResult", int.class, int.class, Intent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (((Integer) param.args[0]) == 10) { //10 is our activity
                    Logger.log("Refreshing groups");
                    groupsPref.reload(); //Reload the groups from file

                    param.setResult(null); //Same thing as return; in a method I think
                }
            }
        });

        // Hook snapchats "onCreate" method.
        findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                prefs.reload();
                SnapChatContext = (Activity) param.thisObject;

                //Util.doDonationMsg(SnapChatContext);
            }
        });

        findAndHookMethod("com.snapchat.android.api.SendSnapTask", lpparam.classLoader, "b", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Logger.log("Sending");
                Bundle bundle = (Bundle) param.getResult();

                try {
                    String recipients = "[\"" + bundle.getString("recipients").split(",\",\"")[1];
                    bundle.putString("recipients", recipients);
                } catch (ArrayIndexOutOfBoundsException ignore) {
                    //Only
                    if (isEveryone || isGroup) {
                        StringBuilder recipientsTemp = new StringBuilder("[\"");
                        String recipients[] = bundle.getString("recipients").split(",\"");
                        for (int i = 0; i < recipients.length; i++) {
                            if (i == 0) continue;
                            recipientsTemp.append(",\""+recipients[i]);
                        }
                        bundle.putString("recipients", "[\""+recipientsTemp.toString().substring(4));
                        if (bundle.getString("recipients").substring(0, 3).equals("[\",")) {
                            bundle.putString("recipients", "["+bundle.getString("recipients").substring(3));
                        }
                        isEveryone = false;
                        isGroup = false;
                    }
                }

                Logger.log(bundle.getString("recipients"));
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
        } catch (XposedHelpers.ClassNotFoundError e) {
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
                    @SuppressWarnings("ConstantConditions") final String fontsDir = SnapChatContext.getExternalFilesDir(null).getAbsolutePath();
                    Typeface face = Typeface.createFromFile(fontsDir + "/" + prefs.getString("Font", "0"));
                    editTextAbstract.setTypeface(face);
                }
                if (prefs.getBoolean("shouldRainbow", false)) {
                    editTextAbstract.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        Random random = new Random();

                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus) {
                                for (int i = 0; i < editTextAbstract.getText().length(); i++) {
                                    SpannableString ss = new SpannableString(editTextAbstract.getText());
                                    ss.setSpan(new ForegroundColorSpan(Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            findAndHookMethod("com.snapchat.android.ui.caption.VanillaCaptionView", lpparam.classLoader, "a", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedHelpers.callMethod(param.getResult(), "removeTextChangedListener",
                            (TextWatcher) XposedHelpers.findField(CaptionEditText, "m").get(param.getResult()));//For removing the character limit set on the caption.

                    final EditText cap = (EditText) param.getResult();
                    editText = cap;

                    Util.doMultiLine(cap);
                }
            });
        } catch (NoSuchMethodError ignore) {
            findAndHookConstructor("com.snapchat.android.ui.caption.VanillaCaptionEditText", lpparam.classLoader, Context.class, AttributeSet.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedHelpers.callMethod(param.thisObject, "removeTextChangedListener",
                            (TextWatcher) XposedHelpers.findField(CaptionEditText, "m").get(param.thisObject));//For removing the character limit set on the caption.

                    final EditText cap = (EditText) param.thisObject;
                    editText = cap;

                    Util.doMultiLine(cap);
                }
            });
        }

        //For getting the users contacts aka friends
        XposedBridge.hookAllConstructors(findClass("com.snapchat.android.fragments.sendto.SendToAdapter", lpparam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List friendsList;
                //Beta class check
                try {
                    friendsList = (List) findField(param.thisObject.getClass(), "d").get(param.thisObject);//Stable snapchat
                } catch (ClassCastException e) {
                    friendsList = (List) findField(param.thisObject.getClass(), "j").get(param.thisObject);//Beta snapchat
                }

                for (Object friendRaw : friendsList) {
                    if (!friendRaw.toString().contains("com.snapchat.android.model.MyPostToStory")) {
                        if (!friendRaw.toString().contains("com.snapchat.android.sendto.SeeMoreRecentsItem")) {
                            if (!friendRaw.toString().equals("lk")) {
                                if (friendsObjects == null) friendsObjects = new ArrayList<>();
                                friendsObjects.add(friendRaw);

                                Logger.log(friendRaw.toString());

                                String[] tempFriend = friendRaw.toString().replace("]", "").split("mUsername=");
                                if (tempFriend.length <= 1) {
                                    continue;
                                }
                                String friend = tempFriend[1];

                                //Check to see if the user has already been added to the list, also don't add the user teamsnapchat
                                if (users == null) users = new ArrayList<>();
                                if (!friend.equals("teamsnapchat")) {
                                    if (!users.contains(friend)) {
                                        //Logger.log("Adding user to users list: " + friend);
                                        users.add(friend);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        Method SendToFragmentMethod;
        try {
            SendToFragmentMethod = XposedHelpers.findMethodExact("com.snapchat.android.fragments.sendto.SendToFragment", lpparam.classLoader, "l");
        }catch (NoSuchMethodError ignore){
            SendToFragmentMethod = XposedHelpers.findMethodExact("com.snapchat.android.fragments.sendto.SendToFragment", lpparam.classLoader, "k");
        }

        //For checking users in the listview
        XposedBridge.hookMethod(SendToFragmentMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                SendToFragmentThisObject = param.thisObject;
            }
        });
    }
}