package com.sonycsl.Kadecot.plugin.gotapi;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Fumiaki on 15/06/26.
 */
public class AccessTokenPreference {

    private AccessTokenPreference(){}

    public static void setAccessToken(Context context, String accessToken) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.got_api_preferences_file_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(context.getString(R.string.access_token_preference_key), accessToken);
        editor.apply();
    }

    public static String getAccessToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.got_api_preferences_file_name), Context.MODE_PRIVATE);
        String accessToken = sp.getString(context.getString(R.string.access_token_preference_key), null);
        return accessToken;
    }
}
