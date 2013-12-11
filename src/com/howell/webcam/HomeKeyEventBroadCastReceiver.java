package com.howell.webcam;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {
	private Activities mActivities;
	static final String SYSTEM_REASON = "reason";
	static final String SYSTEM_HOME_KEY = "homekey";// home key
	static final String SYSTEM_RECENT_APPS = "recentapps";// long home key

	@Override
	public void onReceive(Context context, Intent intent) {
		mActivities = Activities.getInstance();
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
			String reason = intent.getStringExtra(SYSTEM_REASON);
			if (reason != null) {
				if (reason.equals(SYSTEM_HOME_KEY)) {
					// home key澶勭悊鐐�
					Log.e("homekey", "home 按下");
					for(Activity a:mActivities.getmActivityList()){
						System.out.println("haha");
			    		a.finish();
			    	}
//					Toast.makeText(BaseActivity.this, "Home閿鐐瑰嚮", Toast.LENGTH_SHORT).show();
				} else if (reason.equals(SYSTEM_RECENT_APPS)) {
					// long homekey澶勭悊鐐�
					 Log.e("homekey", "长按");
//					 Toast.makeText(BaseActivity.this, "Home閿暱鎸�", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
}