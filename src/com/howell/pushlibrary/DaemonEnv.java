package com.howell.pushlibrary;

import android.content.Context;


public final class DaemonEnv {

    private DaemonEnv() {}

    public static final int DEFAULT_WAKE_UP_INTERVAL = 6 * 60 * 1000;
    private static final int MINIMAL_WAKE_UP_INTERVAL = 3 * 60 * 1000;
    public static boolean mShouldWakeUp = false;
    static Context sApp;
    static Class<? extends AbsWorkService> sServiceClass;
    private static int sWakeUpInterval = DEFAULT_WAKE_UP_INTERVAL;
    static boolean sInitialized;

    /**
     * @param app Application Context.
     * @param wakeUpInterval 定时唤醒的时间间隔(ms).
     */
    public static void initialize( Context app,  Class<? extends AbsWorkService> serviceClass,  Integer wakeUpInterval) {
        sApp = app;
        sServiceClass = serviceClass;
        if (wakeUpInterval != null) sWakeUpInterval = wakeUpInterval;
        sInitialized = true;
    }

    static int getWakeUpInterval() {
        return Math.max(sWakeUpInterval, MINIMAL_WAKE_UP_INTERVAL);
    }
}
