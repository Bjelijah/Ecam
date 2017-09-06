package com.howell.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ServerConfigSp {
    private static final String SP_NAME = "server_set";
    public static void savePushOnOff(Context context,boolean isOnOff){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("push_on_off",isOnOff);
        editor.commit();
    }
    public static boolean loadPushOnOff(Context context){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
        return sp.getBoolean("push_on_off",true);
    }
}
