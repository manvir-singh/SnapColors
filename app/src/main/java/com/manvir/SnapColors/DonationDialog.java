package com.manvir.SnapColors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

public class DonationDialog extends AlertDialog {
    public DonationDialog(final Context context) {
        super(context);
        setTitle("SnapColors - Donation");
        TextView messageTextView = new TextView(context);
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        messageTextView.setText("If you like my work, feel free to donate. :)\n(You can donate later in the SnapColors settings app. This message shows ever time SnapColors is updated)");
        messageTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        setView(messageTextView);
        setButton(Dialog.BUTTON_NEGATIVE, "Get outta my face!", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        setButton(Dialog.BUTTON_POSITIVE, "Hell yeah i'll donate!", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.manvir.SnapColors", "com.manvir.SnapColors.DonateActivity"));
                context.startActivity(intent);
            }
        });
    }
}