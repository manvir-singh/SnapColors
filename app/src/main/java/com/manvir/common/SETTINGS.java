package com.manvir.common;

import android.graphics.Color;

public final class SETTINGS {
    public static final String NAME = "settings";

    public final class KEYS {
        public static final String clearAllImportedFonts = "clearAllImportedFonts";
        public static final String autoRandomize = "autoRandomize";
        public static final String shouldRainbow = "shouldRainbow";
        public static final String checkForVer = "checkForVer";
        public static final String minTimerInt = "minTimerInt";
        public static final String importFont = "importFont";
        public static final String TextColor = "TextColor";
        public static final String BGColor = "BGColor";
        public static final String setFont = "setFont";
        public static final String donate = "donate";
    }

    public final class DEFAULTS {
        public static final boolean autoRandomize = false;
        public static final boolean shouldRainbow = false;
        public static final int TextColor = Color.WHITE;
        public static final boolean checkForVer = true;
        public static final int BGColor = -1728053248;
        public static final boolean setFont = false;
        public static final int minTimerInt = 10;
    }
}
