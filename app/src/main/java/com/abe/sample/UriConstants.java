package com.abe.sample;

import android.net.Uri;

public class UriConstants {

    public static Uri getApplicationPackage() {
        return Uri.parse("package:" + BuildConfig.APPLICATION_ID);
    }
}
