package com.howell.webcam;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText mUserName;
    private EditText mPassWord;
    private Button mButton;
    private SoapManager mSoapManager;

    public ProgressDialog mLoadingDialog;
    
    private static LoginThread thread;
    private static final int POSTPASSWORDERROR = 1;
    private static final int POSTNULLINFO = 2;
    private static final int POSTTOAST = 3;
    private static final int POSTLINKERROR = 4;
    private static final int POSTACCOUNTERROR = 5;
    private static final int THREADJOIN = 6;
    
    private MessageHandler handler;
    
    private static MainActivity mActivity;
    
    private int intentFlag;
    
    private Activities mActivities;
    private HomeKeyEventBroadCastReceiver receiver;
    
    private ResizeLayout layout;
    
    private ImageButton mBack;

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
        
        
        SharedPreferences sharedPreferences = getSharedPreferences("set",
                Context.MODE_PRIVATE);
        String account = sharedPreferences.getString("account", "");
        String password = sharedPreferences.getString("password", "");

        mUserName.setText(account);
        mPassWord.setText(password);
//        System.out.println("test password:"+password);
        
        handler = new MessageHandler();
        //thread = new LoginThread(account, password);
       /* 
        if(!account.equals("") && !password.equals("")){
        	MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.loading), 1000);
        	//enterToNextActivity(account, password);
        	if (thread == null) {
//            	Log.e("----------->>>", "inviteThread");
            	thread = new LoginThread(account,password);
            	thread.setName("LoginThread");
            	thread.start();
            }
        }*/
        mButton.setOnClickListener(this);
        mBack.setOnClickListener(this);
        Intent intent = getIntent();
        intentFlag = intent.getIntExtra("intentFlag", 0);
        if(intentFlag == 1){
        	MessageUtiles.postNewUIDialog(this, getResources().getString(R.string.message), getResources().getString(R.string.ok), 1);
        }else if(intentFlag == 2){
        	MessageUtiles.postNewUIDialog(this, getResources().getString(R.string.login_error), getResources().getString(R.string.ok), 1);
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
    

//    public void loading() {
//        mLoadingDialog = new ProgressDialog(this);
//        mLoadingDialog.setTitle(R.string.loading);
//        mLoadingDialog.setMessage(getResources().getText(R.string.please_wait));
//        mLoadingDialog.show();
//    }
//
//    public void stopLoading() {
//        if (mLoadingDialog != null) {
//            mLoadingDialog.dismiss();
//        }
//    }
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
			String account = mUserName.getText().toString().trim();
	        String password = mPassWord.getText().toString().trim();
	//        	Log.e("----------->>>", "inviteThread");
	        if(thread == null){
	        	Log.e("----------->>>", "inviteThread");
	        	thread = new LoginThread(account,password);
		        thread.setName("LoginThread");
		        thread.start();
	        }
			break;
		default:
			break;
		}
	        
    }
    
    private static void ThreadJoin(){
    	System.out.println("thread join");
    	if (thread != null) {
    		try {
    			thread.join();
    		} catch(Exception e) {
    			
    		}
    	}
    	thread = null;
    }
    
    private void enterToNextActivity(String account,String password){
    	 if (TextUtils.isEmpty(account) && TextUtils.isEmpty(password)) {
        	 handler.sendEmptyMessage(POSTNULLINFO);
         } else {
        	 try{
	             String encodedPassword = DecodeUtils.getEncodedPassword(password);
	             LoginRequest loginReq = new LoginRequest(account, "Common",
	                     encodedPassword, "1.0.0.1");
	             LoginResponse loginRes = mSoapManager.getUserLoginRes(loginReq);
	             Log.e("loginRes",loginRes.getResult().toString());
	             if (loginRes.getResult().toString().equals("OK")) {
	                     SharedPreferences sharedPreferences = getSharedPreferences(
	                             "set", Context.MODE_PRIVATE);
	                     Editor editor = sharedPreferences.edit();
	                     editor.putString("account", account);
	                     editor.putString("password", password);
	                     editor.commit();
	                     GetNATServerRes res = mSoapManager.getGetNATServerRes(new GetNATServerReq(account, loginRes.getLoginSession()));
	                     Log.e("MainActivity", res.toString());
	                     Intent intent = new Intent(MainActivity.this,CamTabActivity.class);
	                     startActivity(intent);
	                     finish();
	                     mActivities.getmActivityList().get("RegisterOrLogin").finish();
	                     handler.sendEmptyMessage(THREADJOIN);
	             }else if(loginRes.getResult().toString().equals("AccountNotExist")){
	            	 handler.sendEmptyMessage(POSTACCOUNTERROR);
	             }else if(loginRes.getResult().toString().equals("Authencation")){
	            	 handler.sendEmptyMessage(POSTPASSWORDERROR);
	             }else{
	            	 handler.sendEmptyMessage(POSTLINKERROR);
	             }
             }catch (Exception e) {
				// TODO: handle exception
            	 handler.sendEmptyMessage(POSTLINKERROR);
			}
         }
    }
    
    public static class MessageHandler extends Handler{
    	
 		@Override
 		public void handleMessage(Message msg) {
 			// TODO Auto-generated method stub
 			super.handleMessage(msg);
 			if (msg.what == POSTPASSWORDERROR) {
 				MessageUtiles.postNewUIDialog2(MainActivity.getContext(), MainActivity.getContext().getString(R.string.password_error), MainActivity.getContext().getString(R.string.ok), 1);
 				sendEmptyMessage(THREADJOIN);
 			}
 			if (msg.what == POSTACCOUNTERROR) {
 				MessageUtiles.postNewUIDialog2(MainActivity.getContext(), MainActivity.getContext().getString(R.string.account_error), MainActivity.getContext().getString(R.string.ok), 1);
 				sendEmptyMessage(THREADJOIN);
 			}
 			if (msg.what == POSTLINKERROR) {
 				MessageUtiles.postNewUIDialog2(MainActivity.getContext(), MainActivity.getContext().getString(R.string.login_error), MainActivity.getContext().getString(R.string.ok), 1);
 				sendEmptyMessage(THREADJOIN);
 			}
 			if (msg.what == POSTNULLINFO) {
 				MessageUtiles.postNewUIDialog2(MainActivity.getContext(), MainActivity.getContext().getString(R.string.verification), MainActivity.getContext().getString(R.string.ok), 1);
 				sendEmptyMessage(THREADJOIN);
 			}
 			if(msg.what == POSTTOAST){
 				MessageUtiles.postToast(MainActivity.getContext(), MainActivity.getContext().getString(R.string.loading), 1000);
 			}
 			if(msg.what == THREADJOIN){
 				ThreadJoin();
 			}
 		}
 	}

    /*private String getEncodedPassword(String password) {
        byte[] key = { 0x48, 0x4F, 0x57, 0x45, 0x4C, 0x4C, 0x4B, 0x45 };
        byte[] iv = { 0x48, 0x4F, 0x57, 0x45, 0x4C, 0x4C, 0x56, 0x49 };
        byte[] rdKey = RandomBytes.getRandombyte();
        byte[] rdIv = RandomBytes.getRandombyte();
        String DES2Password = null;
        try {
            String MD5Password = MD5.getMD5(password);
            String hexKey = HEXTranslate.getHexString(rdKey);
            String hexIv = HEXTranslate.getHexString(rdIv);
            String DES1Password = DES.CBCEncrypt(MD5Password, rdKey, rdIv);
            DES2Password = DES.CBCEncrypt(hexKey + hexIv + DES1Password, key,
                    iv);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return DES2Password;
    }*/
    
    class LoginThread extends Thread{
    	private String account;
    	private String password;
		public LoginThread(String account, String password) {
			super();
			this.account = account;
			this.password = password;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			enterToNextActivity(account,password);
		}
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
        Log.e("Setting","onPause");
//    	for(Activity a:mActivities.getmActivityList()){
//    		a.finish();
//    	}
    }
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	Log.e("Main", "onStop");
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	Log.e("Main", "onDestroy");
    	super.onDestroy();
    	mActivities.removeActivity("MainActivity");
    	unregisterReceiver(receiver);
    }
}
