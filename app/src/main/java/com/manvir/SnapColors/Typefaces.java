package com.manvir.SnapColors;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.util.Hashtable;

//The method "Typeface.createFromFile" has a bug this class is a workaround.
//https://code.google.com/p/android/issues/detail?id=9904
//http://stackoverflow.com/questions/12766930/native-typeface-cannot-be-made-only-for-some-people

public class Typefaces {
    private static final String TAG = "Typefaces";

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public static Typeface get(String path) {
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
