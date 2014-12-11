package com.manvir.SnapColors;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.util.Hashtable;

public class Typefaces {
    private static final String TAG = "Typefaces";

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public static Typeface get(Context c, String path) {
        synchronized (cache) {
            if (!cache.containsKey(path)) {
                try {
                    Typeface t = Typeface.createFromFile(path);
                    cache.put(path, t);
                } catch (Exception e) {
                    Log.e(TAG, "Could not get typeface '" + path
                            + "' because " + e.getMessage());
                    return null;
                }
            }
            return cache.get(path);
        }
    }
}
