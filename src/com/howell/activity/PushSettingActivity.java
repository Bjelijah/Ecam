package com.howell.activity;


import com.android.howell.webcam.R;
import com.howell.push.MyService;
import com.howell.pushlibrary.IntentWrapper;
import com.howell.utils.AlerDialogUtils;
import com.howell.utils.ServerConfigSp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;

public class PushSettingActivity extends Activity implements OnClickListener{

	private ImageButton mBack;
	private CheckBox mPush;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.push_setting);
		mBack = (ImageButton) findViewById(R.id.ib_push_back);
		mBack.setOnClickListener(this);
		mPush = (CheckBox) findViewById(R.id.cb_push_set);
		mPush.setOnClickListener(this);
		mPush.setChecked(ServerConfigSp.loadPushOnOff(this));

	}

	private void doPushSet(){
		boolean isClick = mPush.isChecked();
		Log.i("123", "isclick="+isClick);
		//save to sp
		ServerConfigSp.savePushOnOff(this, isClick);
		//show dialog
		if (isClick) {
			IntentWrapper.whiteListMatters(this,null);
		}
		if (isClick) {
			startService(new Intent(this, MyService.class));
		}else{
			MyService.stopService();
			stopService(new Intent(this,MyService.class));
			MyService.isWorking = false;
		}

		//open stop server;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ib_push_back:
			finish();
			break;
		case R.id.cb_push_set:
			doPushSet();
			break;
		default:
			break;
		}

	}




}
