package com.howell.activity;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.android.howell.webcam.R;
import com.howell.broadcastreceiver.HomeKeyEventBroadCastReceiver;
import com.howell.utils.MessageUtiles;
import com.howell.protocol.SoapManager;
import com.howell.protocol.UpdateChannelNameReq;
import com.howell.protocol.UpdateChannelNameRes;

public class ChangeDeviceName extends Activity implements OnClickListener{
	private Button mOk;
	private EditText mName;
	private Dialog waitDialog;
	private SoapManager mSoapManager;
	private String devId;
	
	private Activities mActivities;
	private HomeKeyEventBroadCastReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_device_name);
		mSoapManager = SoapManager.getInstance();
		Intent intent = getIntent();
		devId = intent.getStringExtra("devid");
		
		mActivities = Activities.getInstance();
        mActivities.addActivity("ChangeDeviceName",ChangeDeviceName.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
		mOk = (Button)findViewById(R.id.btn_cheng_device_name_ok);
		mName = (EditText)findViewById(R.id.et_change_device_name);
		
		mOk.setOnClickListener(this);
		
	}

    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.removeActivity("ChangeDeviceName");
    	unregisterReceiver(receiver);
    }
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_cheng_device_name_ok:
			waitDialog = MessageUtiles.postWaitingDialog(ChangeDeviceName.this);
			waitDialog.show();
			new AsyncTask<Void, Integer, Void>() {
				UpdateChannelNameRes res = null;
				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub
					String name = mName.getText().toString();
					UpdateChannelNameReq req = new UpdateChannelNameReq(mSoapManager.getLoginResponse().getAccount(),mSoapManager.getLoginResponse().getLoginSession(),devId,0,name);
					res = mSoapManager.getUpdateChannelNameRes(req);
					System.out.println(res.getResult());
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub
					super.onPostExecute(result);
					waitDialog.dismiss();
					if(res != null && res.getResult().equals("OK")){
						MessageUtiles.postToast(ChangeDeviceName.this, getResources().getString(R.string.change_devicename_activity_success), 1000);
						finish();
						if(mActivities.getmActivityList().containsKey("GetMatchResult")){
							mActivities.getmActivityList().get("GetMatchResult").finish();
						}
	                	if(mActivities.getmActivityList().containsKey("FlashLighting")){
							mActivities.getmActivityList().get("FlashLighting").finish();
						}
	                	if(mActivities.getmActivityList().containsKey("SetDeviceWifi")){
							mActivities.getmActivityList().get("SetDeviceWifi").finish();
						}
	                	if(mActivities.getmActivityList().containsKey("SendWifi")){
							mActivities.getmActivityList().get("SendWifi").finish();
						}
						mActivities.getmActivityList().get("CamTabActivity").finish();
						Intent intent = new Intent(ChangeDeviceName.this,CamTabActivity.class);
						startActivity(intent);
					}else{
						MessageUtiles.postToast(ChangeDeviceName.this, getResources().getString(R.string.change_devicename_activity_fail), 1000);
					}
				}
				
			}.execute();
			break;

		default:
			break;
		}
	}
}
