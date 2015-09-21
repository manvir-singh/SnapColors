package com.manvir.common;

import android.graphics.Color;

import java.util.HashSet;
import java.util.Set;

public final class SETTINGS {
    public static final String NAME = "settings";

    public static final class DEFAULTS {
        public static final Set<String> blockStoriesFromList = new HashSet<>();
        public static final boolean screenshotDetection = true;
        public static final boolean resizeVideoAlert = true;
        public static final boolean disableDiscover = false;
        public static final boolean autoRandomize = false;
        public static final boolean shouldRainbow = false;
        public static final int TextColor = Color.WHITE;
        public static final boolean disableLive = false;
        public static final boolean checkForVer = true;
        public static final int BGColor = -1728053248;
        public static final boolean setFont = false;
        public static final boolean hideT = false;
        public static final int minTimerInt = 10;
    }

    public final class KEYS {
        public static final String clearAllImportedFonts = "clearAllImportedFonts";
        public static final String blockStoriesFromList = "blockStoriesFromList";
        public static final String screenshotDetection = "screenshotDetection";
        public static final String resizeVideoAlert = "resizeVideoAlert";
        public static final String disableDiscover = "disableDiscover";
        public static final String autoRandomize = "autoRandomize";
        public static final String shouldRainbow = "shouldRainbow";
        public static final String checkForVer = "checkForVer";
        public static final String minTimerInt = "minTimerInt";
        public static final String disableLive = "disableLive";
        public static final String importFont = "importFont";
        public static final String TextColor = "TextColor";
        public static final String BGColor = "BGColor";
        public static final String setFont = "setFont";
        public static final String donate = "donate";
        public static final String hideT = "hideT";
    }
}
