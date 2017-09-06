package com.howell.activity;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.howell.webcam.R;
import com.howell.broadcastreceiver.HomeKeyEventBroadCastReceiver;
import com.howell.utils.DecodeUtils;
import com.howell.utils.MessageUtiles;
import com.howell.utils.PhoneConfig;
import com.howell.utils.ServerConfigSp;
import com.howell.protocol.GetNATServerReq;
import com.howell.protocol.GetNATServerRes;
import com.howell.protocol.LoginRequest;
import com.howell.protocol.LoginResponse;
import com.howell.protocol.QueryAndroidTokenReq;
import com.howell.protocol.QueryAndroidTokenRes;
import com.howell.protocol.SoapManager;
import com.howell.protocol.UpdateAndroidTokenReq;
import com.howell.protocol.UpdateAndroidTokenRes;
import com.howell.push.MyService;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText mUserName;
    private EditText mPassWord;
    private Button mButton;
    private SoapManager mSoapManager;

    public ProgressDialog mLoadingDialog;
    
    private static final int POSTPASSWORDERROR = 1;
    private static final int POSTNULLINFO = 2;
    private static final int POSTTOAST = 3;
    private static final int POSTLINKERROR = 4;
    private static final int POSTACCOUNTERROR = 5;
    
    private MessageHandler handler;
    
    private static MainActivity mActivity;
    
    private int intentFlag;
    
    private Activities mActivities;
    private HomeKeyEventBroadCastReceiver receiver;
    
//    private ResizeLayout layout;
    
    private ImageButton mBack;
	private Dialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mActivities = Activities.getInstance();
        mActivities.addActivity("MainActivity",MainActivity.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
        mActivity = this;
        mSoapManager = SoapManager.getInstance();

        mUserName = (EditText) findViewById(R.id.username);
        mPassWord = (EditText) findViewById(R.id.password);
        mButton = (Button) findViewById(R.id.ok);
        
        mBack = (ImageButton)findViewById(R.id.ib_login_back);
        
        SharedPreferences sharedPreferences = getSharedPreferences("set",Context.MODE_PRIVATE);
        String account = sharedPreferences.getString("account", "");
        String password = sharedPreferences.getString("password", "");

        mUserName.setText(account);
        mPassWord.setText(password);
        
        handler = new MessageHandler();
        mButton.setOnClickListener(this);
        mBack.setOnClickListener(this);
        Intent intent = getIntent();
        intentFlag = intent.getIntExtra("intentFlag", 0);
        if(intentFlag == 1){
        	MessageUtiles.postAlertDialog(this, getResources().getString(R.string.login_fail), getResources().getString(R.string.message), R.drawable.expander_ic_minimized
        			, null, getResources().getString(R.string.ok), null, null);
//        	MessageUtiles.postNewUIDialog(this, getResources().getString(R.string.message), getResources().getString(R.string.ok), 1);
        }else if(intentFlag == 2){
        	MessageUtiles.postAlertDialog(this, getResources().getString(R.string.login_fail), getResources().getString(R.string.login_error), R.drawable.expander_ic_minimized
        			, null, getResources().getString(R.string.ok), null, null);
//        	MessageUtiles.postNewUIDialog(this, getResources().getString(R.string.login_error), getResources().getString(R.string.ok), 1);
        }
        
        /*layout = (ResizeLayout) findViewById(R.id.layout);   
		layout.setOnResizeListener(new ResizeLayout.OnResizeListener() {   
		       
			public void OnResize(int w, int h, int oldw, int oldh) { 
				if(oldh == 0){
					return;
				}
				if(oldh > h){
					if(oldh - h < 100)return;
					layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.backgroundclear1));
				}else if(oldh < h){
					if(h - oldh < 100)return;
					layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.backgroundclear));
				}else{
					return;
				}
			}   
		});   */
        
    }
    

    private static MainActivity getContext(){
    	return mActivity;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    	switch (v.getId()) {
		case R.id.ib_login_back:
			finish();
			break;
		case R.id.ok:
			final String account = mUserName.getText().toString().trim();
	        final String password = mPassWord.getText().toString().trim();
			if (TextUtils.isEmpty(account) && TextUtils.isEmpty(password)) {
				MessageUtiles.postAlertDialog(this, getResources().getString(R.string.login_fail), getResources().getString(R.string.verification), R.drawable.expander_ic_minimized
						, null, getResources().getString(R.string.ok), null, null);
//				MessageUtiles.postNewUIDialog2(MainActivity.getContext(), MainActivity.getContext().getString(R.string.verification), MainActivity.getContext().getString(R.string.ok), 1);
	        	return;
	        }
	        waitDialog = MessageUtiles.postWaitingDialog(MainActivity.this);
			waitDialog.show();
			new AsyncTask<Void, Integer, Void>() {
				LoginResponse loginRes;
				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub
					try{
						String encodedPassword = DecodeUtils.getEncodedPassword(password);
				        LoginRequest loginReq = new LoginRequest(account, "Common",encodedPassword, "1.0.0.1");
				        loginRes = mSoapManager.getUserLoginRes(loginReq);
				        Log.e("loginRes",loginRes.getResult().toString());
			         }catch (Exception e) {
						// TODO: handle exception
			        	handler.sendEmptyMessage(POSTLINKERROR);
			         }
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub
					super.onPostExecute(result);
					waitDialog.dismiss();
					if(loginRes == null){
						return;
					}
					if (loginRes.getResult().toString().equals("OK")) {
	                     SharedPreferences sharedPreferences = getSharedPreferences(
	                             "set", Context.MODE_PRIVATE);
	                     Editor editor = sharedPreferences.edit();
	                     editor.putString("account", account);
	                     editor.putString("password", password);
	                     editor.commit();
	                     GetNATServerRes res = mSoapManager.getGetNATServerRes(new GetNATServerReq(account, loginRes.getLoginSession()));
	                     Log.e("MainActivity", res.toString());
	                     //
	                    
	                 	androidToken(account,loginRes.getLoginSession(),PhoneConfig.getIMEI(MainActivity.this));
						startPushService(); 
	                     
	                     Intent intent = new Intent(MainActivity.this,CamTabActivity.class);
	                     startActivity(intent);
	                     finish();
	                     mActivities.getmActivityList().get("RegisterOrLogin").finish();
		            }else if(loginRes.getResult().toString().equals("AccountNotExist")){
		            	MessageUtiles.postAlertDialog(MainActivity.this, getResources().getString(R.string.login_fail), getResources().getString(R.string.account_error), R.drawable.expander_ic_minimized
								, null, getResources().getString(R.string.ok), null, null);
//		            	 MessageUtiles.postNewUIDialog2(MainActivity.getContext(), MainActivity.getContext().getString(R.string.account_error), MainActivity.getContext().getString(R.string.ok), 1);
		            }else if(loginRes.getResult().toString().equals("Authencation")){
		            	MessageUtiles.postAlertDialog(MainActivity.this, getResources().getString(R.string.login_fail), getResources().getString(R.string.password_error), R.drawable.expander_ic_minimized
								, null, getResources().getString(R.string.ok), null, null);
//		            	 MessageUtiles.postNewUIDialog2(MainActivity.getContext(), MainActivity.getContext().getString(R.string.password_error), MainActivity.getContext().getString(R.string.ok), 1);
		            }else{
		            	MessageUtiles.postAlertDialog(MainActivity.this, getResources().getString(R.string.login_fail), getResources().getString(R.string.login_error), R.drawable.expander_ic_minimized
								, null, getResources().getString(R.string.ok), null, null);
//		            	 MessageUtiles.postNewUIDialog2(MainActivity.getContext(), MainActivity.getContext().getString(R.string.login_error), MainActivity.getContext().getString(R.string.ok), 1);
		            }
				}
				
			}.execute();
			break;
		default:
			break;
		}
	        
    }
    
    private boolean androidToken(String account,String session,String uuid){
        QueryAndroidTokenRes res = mSoapManager.GetQueryAndroidTokenRes(new QueryAndroidTokenReq(account, session,uuid));
        Log.i("123","QueryAndroidTokenRes="+res.toString());
        if (res.getResult().equalsIgnoreCase("ok")){
            return true;
        }
        //regist
        UpdateAndroidTokenRes tokenRes = mSoapManager.GetUpdateAndroidTokenRes(new UpdateAndroidTokenReq(account,session,uuid,uuid,true));
        Log.i("123","UpdateAndroidTokenRes="+tokenRes.toString());
        if (!tokenRes.getResult().equalsIgnoreCase("ok"))   return false;
        return true;
    }
	  
	   private void startPushService(){
         boolean isPush = ServerConfigSp.loadPushOnOff(this);
         if (isPush){
             this.startService(new Intent(this, MyService.class));
         }
     }
    
    public static class MessageHandler extends Handler{
    	
 		@Override
 		public void handleMessage(Message msg) {
 			// TODO Auto-generated method stub
 			super.handleMessage(msg);
 			if (msg.what == POSTPASSWORDERROR) {
 				MessageUtiles.postAlertDialog(MainActivity.getContext(), MainActivity.getContext().getString(R.string.login_fail), MainActivity.getContext().getString(R.string.password_error), R.drawable.expander_ic_minimized
						, null, MainActivity.getContext().getString(R.string.ok), null, null);
// 				MessageUtiles.postNewUIDialog2(MainActivity.getContext(), MainActivity.getContext().getString(R.string.password_error), MainActivity.getContext().getString(R.string.ok), 1);
 			}
 			if (msg.what == POSTACCOUNTERROR) {
 				MessageUtiles.postAlertDialog(MainActivity.getContext(), MainActivity.getContext().getString(R.string.login_fail), MainActivity.getContext().getString(R.string.account_error), R.drawable.expander_ic_minimized
						, null, MainActivity.getContext().getString(R.string.ok), null, null);
// 				MessageUtiles.postNewUIDialog2(MainActivity.getContext(), MainActivity.getContext().getString(R.string.account_error), MainActivity.getContext().getString(R.string.ok), 1);
 			}
 			if (msg.what == POSTLINKERROR) {
 				MessageUtiles.postAlertDialog(MainActivity.getContext(), MainActivity.getContext().getString(R.string.login_fail), MainActivity.getContext().getString(R.string.login_error), R.drawable.expander_ic_minimized
						, null, MainActivity.getContext().getString(R.string.ok), null, null);
// 				MessageUtiles.postNewUIDialog2(MainActivity.getContext(), MainActivity.getContext().getString(R.string.login_error), MainActivity.getContext().getString(R.string.ok), 1);
 			}
 			if (msg.what == POSTNULLINFO) {
 				MessageUtiles.postAlertDialog(MainActivity.getContext(), MainActivity.getContext().getString(R.string.login_fail), MainActivity.getContext().getString(R.string.verification), R.drawable.expander_ic_minimized
						, null, MainActivity.getContext().getString(R.string.ok), null, null);
// 				MessageUtiles.postNewUIDialog2(MainActivity.getContext(), MainActivity.getContext().getString(R.string.verification), MainActivity.getContext().getString(R.string.ok), 1);
 			}
 			if(msg.what == POSTTOAST){
 				MessageUtiles.postToast(MainActivity.getContext(), MainActivity.getContext().getString(R.string.loading), 1000);
 			}
 		}
 	}

    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.removeActivity("MainActivity");
    	unregisterReceiver(receiver);
    }
}
