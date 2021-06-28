package com.github.shwuup.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.github.shwuup.BuildConfig;



import com.github.shwuup.R;

import timber.log.Timber;

public final class SharedPref {
    public static void writeString(Context context, String key, String value) {
        SharedPreferences sharedPref = context.getSharedPreferences(String.format("%s_%s", BuildConfig.PREFIX, context.getString(R.string.preference_file_key)), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
        Timber.d("Writing string: %s under key %s", value, key);
    }

    public static String readString(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(String.format("%s_%s", BuildConfig.PREFIX, context.getString(R.string.preference_file_key)), Context.MODE_PRIVATE);
        return sharedPref.getString(key, "not found");
    }
}
