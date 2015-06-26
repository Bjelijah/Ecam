package com.howell.activity;


import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.howell.webcam.R;
import com.howell.broadcastreceiver.HomeKeyEventBroadCastReceiver;
import com.howell.protocol.AddDeviceReq;
import com.howell.protocol.AddDeviceRes;
import com.howell.protocol.SoapManager;
import com.howell.protocol.UpdateChannelNameReq;
import com.howell.protocol.UpdateChannelNameRes;
import com.howell.utils.MessageUtiles;

public class ChangeDeviceName extends Activity implements OnClickListener{
	private ImageButton mOk;
	private EditText mName;
	private Dialog waitDialog;
	private SoapManager mSoapManager;
	private String result;
	private String devId,devKey;
	
	private Activities mActivities;
	private HomeKeyEventBroadCastReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_device_name);
		mSoapManager = SoapManager.getInstance();
		Intent intent = getIntent();
		result = intent.getStringExtra("result");
		try {
			parseJsonString(new JSONObject(result));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mActivities = Activities.getInstance();
        mActivities.addActivity("ChangeDeviceName",ChangeDeviceName.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
		mOk = (ImageButton)findViewById(R.id.btn_cheng_device_name_ok);
		mName = (EditText)findViewById(R.id.et_change_device_name);
		
		mOk.setOnClickListener(this);
		
	}
	
	private void parseJsonString(JSONObject obj) throws JSONException{
//		devId = obj.getString("id");
//		devKey = obj.getString("key");
		//-------------test--------------
		devId = "123124314151";
		devKey = "123124314151";
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
				UpdateChannelNameRes updateChannelNameRes = null;
				AddDeviceRes addDeviceRes = null;
				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub
					String name = mName.getText().toString();
					String account = mSoapManager.getLoginResponse().getAccount();
					String session = mSoapManager.getLoginResponse().getLoginSession();
					UpdateChannelNameReq updateChannelNameReq = new UpdateChannelNameReq(account,session,devId,0,name);
					updateChannelNameRes = mSoapManager.getUpdateChannelNameRes(updateChannelNameReq);
					System.out.println(updateChannelNameRes.getResult());
					
					if(updateChannelNameRes != null && updateChannelNameRes.getResult().equals("OK")){
						AddDeviceReq addDeviceReq = new AddDeviceReq(account, session, devId, devKey, name, true);
						addDeviceRes = mSoapManager.getAddDeviceRes(addDeviceReq);
						System.out.println(addDeviceRes.getResult());
					}
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub
					super.onPostExecute(result);
					waitDialog.dismiss();
					if(addDeviceRes != null && addDeviceRes.getResult().equals("OK")){
						MessageUtiles.postToast(ChangeDeviceName.this, getResources().getString(R.string.match_activity_success_dialog_message), 1000);
						finish();
//						if(mActivities.getmActivityList().containsKey("GetMatchResult")){
//							mActivities.getmActivityList().get("GetMatchResult").finish();
//						}
//	                	if(mActivities.getmActivityList().containsKey("FlashLighting")){
//							mActivities.getmActivityList().get("FlashLighting").finish();
//						}
//	                	if(mActivities.getmActivityList().containsKey("SetDeviceWifi")){
//							mActivities.getmActivityList().get("SetDeviceWifi").finish();
//						}
//	                	if(mActivities.getmActivityList().containsKey("SendWifi")){
//							mActivities.getmActivityList().get("SendWifi").finish();
//						}
						mActivities.getmActivityList().get("CamTabActivity").finish();
						Intent intent = new Intent(ChangeDeviceName.this,CamTabActivity.class);
						startActivity(intent);
					}else{
						MessageUtiles.postToast(ChangeDeviceName.this, getResources().getString(R.string.match_activity_fail_tips), 1000);
					
					}
				}
				
			}.execute();
			break;

		default:
			break;
		}
	}
}
