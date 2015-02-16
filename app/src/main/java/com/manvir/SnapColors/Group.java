package com.manvir.SnapColors;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;

public class Group {
    private final boolean isAllGroup;
    private final String[] users;
    private RelativeLayout te;
    private CheckBox checkbox;

    public Group(Context context, ListView listView, String GroupName, final String[] users, boolean isAllGroup) {
        this.isAllGroup = isAllGroup;
        this.users = users;
        te = (RelativeLayout) View.inflate(context, context.getResources().getIdentifier("send_to_item", "layout", "com.snapchat.android"), null);
        if (isAllGroup) {
            te.setTag("isAllGroup");
        } else {
            te.setTag("group");
        }

        TextView userName = (TextView) te.findViewById(context.getResources().getIdentifier("name", "id", "com.snapchat.android"));
        userName.setSingleLine(false);
        userName.setHorizontallyScrolling(false);
        Spanned text = Html.fromHtml("<span>"+GroupName+" <small>("+ Arrays.toString(users).replace("[", "").replace("]", "") +")</small></span>");
        userName.setText(text);

        checkbox = (CheckBox) te.findViewById(context.getResources().getIdentifier("checkbox", "id", "com.snapchat.android"));
        checkbox.setVisibility(View.VISIBLE);

        te.findViewById(context.getResources().getIdentifier("add_friend_checkbox", "id", "com.snapchat.android")).setVisibility(View.GONE);

        listView.addFooterView(te);

        View grayDivider = new View(context);
        grayDivider.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics())));
        grayDivider.setBackgroundDrawable(context.getResources().getDrawable(context.getResources().getIdentifier("list_divider", "drawable", App.SnapChatPKG)));
        te.addView(grayDivider);

        View.OnClickListener onClickListenerUserName = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkbox.isChecked()){
                    checkbox.setChecked(true);
                    onChecked();
                }else {
                    onUnChecked();
                    checkbox.setChecked(false);
                }
            }
        };
        View.OnClickListener onClickListenerCheckBox = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkbox.isChecked()){
                    onChecked();
                }else {
                    onUnChecked();
                }
            }
        };

        userName.setOnClickListener(onClickListenerUserName);
        checkbox.setOnClickListener(onClickListenerCheckBox);
    }

    private void onUnChecked() {
        App.recipients = null;
        te.setTag("");
        enableGroups();

        if (isAllGroup) { //If the group is "everyone"
            App.unCheckAll();
        } else {
            //Deselects the users
            App.unCheckUsers(users);
        }
    }

    private void onChecked(){
        if(te.getAlpha() == 0.5f){
            checkbox.setEnabled(false);
            checkbox.setClickable(false);
            checkbox.setChecked(false);
        }else {
            checkbox.setClickable(true);
            checkbox.setChecked(true);
            checkbox.setEnabled(true);
            te.setTag("selected");
            disableGroups();

            if (isAllGroup) { //If the group is "everyone"
                App.checkAll();
            } else {
                //Selects the users
                App.checkUsers(users);
            }
        }
    }

    //Disables every group unless that group has a tag of "selected"
    private void disableGroups() {
        for (Group group : App.groupsList) {
            if (!group.getView().getTag().equals("selected")) {
                group.getView().setAlpha(0.5f);
            }
        }
    }

    private void enableGroups() {
        for (Group group : App.groupsList) {
            group.getView().setAlpha(1f);
        }
    }

    public RelativeLayout getView(){
        return this.te;
    }

}
