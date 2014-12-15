package com.manvir.SnapColors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class DonationDialog extends AlertDialog {
    public DonationDialog(final Context context) {
        super(context);
        setTitle("SnapColors");
        setMessage("Please consider donating to show your appreciation for the hard work ive put into developing this module. Your donation is much appreciated, like mega ∞ appreciated. :)");
        setButton(Dialog.BUTTON_NEGATIVE, "Get outta my face!", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
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