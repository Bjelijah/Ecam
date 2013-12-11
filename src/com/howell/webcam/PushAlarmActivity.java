package com.howell.webcam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.CheckBox;

import com.android.howell.webcam.R;


public class PushAlarmActivity extends Activity {
	 private CheckBox push_checkbox;
	 private SoapManager mSoapManager;
	 private LoginResponse mLoginResponse;
	 private int backCount;
	 
	 private Activities mActivities;
	 private HomeKeyEventBroadCastReceiver receiver;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.push_alarm);
		mActivities = Activities.getInstance();
        mActivities.getmActivityList().add(PushAlarmActivity.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
		mSoapManager = SoapManager.getInstance();
		mLoginResponse = mSoapManager.getLoginResponse();
		push_checkbox = (CheckBox)findViewById(R.id.push_enable);
		backCount = 0 ; 
		
		SharedPreferences sharedPreferences = getSharedPreferences("set",
                Context.MODE_PRIVATE);
        boolean pushSet = sharedPreferences.getBoolean(mLoginResponse.getAccount(), true);
        System.out.println(pushSet);
//		QueryAndroidTokenReq req= new QueryAndroidTokenReq(mLoginResponse.getAccount()
//        		, mLoginResponse.getLoginSession(),Secure.getString(getContentResolver(), Secure.ANDROID_ID));
//        QueryAndroidTokenRes res = mSoapManager.GetQueryAndroidTokenRes(req);
//        Log.e("", res.toString());
        push_checkbox.setChecked(pushSet/*res.isAPNs()*/);
	}
	
    private boolean savePushParam(){
    	String UUID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
    	System.out.println(UUID);
    	boolean isPushAlarm = push_checkbox.isChecked();
    	System.out.println(isPushAlarm);
    	SharedPreferences sharedPreferences = getSharedPreferences(
                "set", Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(mLoginResponse.getAccount(), isPushAlarm);
        editor.commit();
	    UpdateAndroidTokenReq req = new UpdateAndroidTokenReq(mLoginResponse.getAccount(), mLoginResponse.getLoginSession()
	    		, UUID,UUID, isPushAlarm);
	    System.out.println(req.toString());
	    UpdateAndroidTokenRes res = mSoapManager.GetUpdateAndroidTokenRes(req);
	    Log.e("savePushParam", res.getResult());
    	return true;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.save_set),2000);
        	if (backCount == 0) {
                MyTask mTask = new MyTask();
                mTask.execute();
            }else{
            	MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.save_set),2000);
            }
        	 backCount++;
        }
        return false;
    }
    
    private class MyTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            System.out.println("call doInBackground");
            savePushParam();
            finish();
            return null;
        }
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.getmActivityList().remove(PushAlarmActivity.this);
    	unregisterReceiver(receiver);
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
//    	for(Activity a:mActivities.getmActivityList()){
//    		a.finish();
//    	}
    }
}
