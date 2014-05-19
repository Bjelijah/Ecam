package com.howell.webcam;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.android.howell.webcam.R;

public class RegisterOrLogin extends Activity implements OnClickListener{
	private TextView mRegister,mLogin,mTest;
    private SoapManager mSoapManager;
    private Activities mActivities;
    private HomeKeyEventBroadCastReceiver receiver;
	private Dialog waitDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_or_login);
    	mActivities = Activities.getInstance();
    	mActivities.addActivity("RegisterOrLogin",RegisterOrLogin.this);
		mSoapManager = SoapManager.getInstance();
		
		receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
		mRegister = (TextView)findViewById(R.id.btn_register);
		mLogin = (TextView)findViewById(R.id.btn_login);
		mTest = (TextView)findViewById(R.id.btn_test);
		
		TextPaint tp = mRegister.getPaint();
        tp.setFakeBoldText(true);
        
        tp = mLogin.getPaint();
        tp.setFakeBoldText(true);
        
        tp = mTest.getPaint();
        tp.setFakeBoldText(true);
		
		mRegister.setOnClickListener(this);
		mLogin.setOnClickListener(this);
		mTest.setOnClickListener(this);
	}
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_register:
			Intent intent = new Intent(RegisterOrLogin.this,Register.class);
			startActivity(intent);
			break;
			
		case R.id.btn_login:
			intent = new Intent(RegisterOrLogin.this,MainActivity.class);
			startActivity(intent);
			break;
			
		case R.id.btn_test:
			waitDialog = MessageUtiles.postNewUIDialog(RegisterOrLogin.this);
			waitDialog.show();
			new AsyncTask<Void, Integer, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub
					String encodedPassword = DecodeUtils.getEncodedPassword("100868");
		            LoginRequest loginReq = new LoginRequest("100868", "Common",encodedPassword, "1.0.0.1");
		            LoginResponse loginRes = mSoapManager.getUserLoginRes(loginReq);
		            if (loginRes.getResult().toString().equals("OK")) {
		                GetNATServerRes res = mSoapManager.getGetNATServerRes(new GetNATServerReq("100868", loginRes.getLoginSession()));
		                Log.e("Register ", res.toString());
		                Intent intent = new Intent(RegisterOrLogin.this,CameraList.class);
			            startActivity(intent);
		            }
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub
					super.onPostExecute(result);
					waitDialog.dismiss();
				}
				
			}.execute();
			break;
			
		default:
			break;
		}
	}
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	Log.e("CameraList", "onDestroy()");
	    mActivities.removeActivity("RegisterOrLogin");
	    mActivities.toString();
    	unregisterReceiver(receiver);
    }
}
