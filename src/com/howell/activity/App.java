package com.howell.activity;

import com.howell.push.MyService;
import com.howell.pushlibrary.DaemonEnv;

import android.app.Application;

public class App extends Application {
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		DaemonEnv.initialize(this, MyService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
	}
}
