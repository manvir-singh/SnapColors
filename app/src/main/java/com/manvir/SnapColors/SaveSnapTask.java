package com.manvir.SnapColors;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.manvir.logger.Logger;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveSnapTask implements Runnable {
    private final String mSender;
    private final Object mSnap;
    private final int mMediaType;

    public SaveSnapTask(String mSender, Object mSnap, int mMediaType) {
        this.mSender = mSender;
        this.mSnap = mSnap;
        this.mMediaType = mMediaType;
    }

    @Override
    public void run() {
        @SuppressLint("SimpleDateFormat")
        String date = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
        switch (mMediaType) {
            case 0: //Image
                Util.saveBitmap((Bitmap) mSnap, new File(Util.SDCARD_SNAPCOLORS + "/" + mSender, mSender + "_" + date + ".png"));
                break;
            case 1: //Video
            case 2: //Video no sound
                FileInputStream videoData = (FileInputStream) mSnap;
                try {
                    FileUtils.copyInputStreamToFile(videoData, new File(Util.SDCARD_SNAPCOLORS + "/" + mSender, mSender + "_" + date + ".mp4"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                Logger.log("Unknown snap type");
                break;
        }
    }
}
