package com.howell.webcam;

import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.android.howell.webcam.R;

public class LogoActivity extends Activity implements TagAliasCallback{
//	private MyHandler handler;
	private static final int LOGIN = 1; 
	private static final int NEXT = 2;
	private static final int UNCONNECT = 3;
	private SoapManager mSoapManager;
//	private static LogoActivity mContext;
	private String account;
	private String password;
	
//	public static boolean isForeground = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo);
	    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
    	.detectNetwork() // ��������滻ΪdetectAll() �Ͱ����˴��̶�д������I/O
    	.build());
	    //���ͷ����ʼ��
		JPushInterface.init(getApplicationContext());
		//�������ͱ���
		setAlias();
		if(JPushInterface.isPushStopped(getApplicationContext())) 
			JPushInterface.resumePush(getApplicationContext());
		//System.out.println(JPushInterface.isPushStopped(getApplicationContext()));
//	    handler = new MyHandler();
	    //intentFlag 1:δ�������� 2:���Ӵ��� 
		if (!isNetworkConnected()) {
			MyPlayer myPlayer = new MyPlayer(3);
	    	myPlayer.start();
		}else{
			//����ʼʱ�ѵ���DeviceManager�������map���
			DeviceManager mDeviceManager = DeviceManager.getInstance();
			mDeviceManager.clearMember();
			
			mSoapManager = SoapManager.getInstance();
		    SharedPreferences sharedPreferences = getSharedPreferences("set",
		                Context.MODE_PRIVATE);
		    account = sharedPreferences.getString("account", "");
		    password = sharedPreferences.getString("password", "");
		    
		    if(!account.equals("") && !password.equals("")){
		    	MyPlayer myPlayer = new MyPlayer(1);
		    	myPlayer.start();
		    }else{
		    	MyPlayer myPlayer = new MyPlayer(2);
		    	myPlayer.start();
		    }
		}
	}
	
	/**
	 *����Alias
	 */
	private void setAlias(){
		String alias = Secure.getString(getContentResolver(), Secure.ANDROID_ID);//"112233";
		
		//����JPush API����Alias
		JPushInterface.setAliasAndTags(getApplicationContext(), alias, null, this);
	}
	
	@Override
	protected void onResume() {
//		isForeground = true;
		super.onResume();
	}


	@Override
	protected void onPause() {
//		isForeground = false;
		super.onPause();
	}


	@Override
	protected void onDestroy() {
//		unregisterReceiver(mMessageReceiver);
		super.onDestroy();
	}
	
/*
	//for receive customer msg from jpush server
	private MessageReceiver mMessageReceiver;
	public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
	public static final String KEY_TITLE = "title";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_EXTRAS = "extras";
	
	public void registerMessageReceiver() {
		mMessageReceiver = new MessageReceiver();
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(MESSAGE_RECEIVED_ACTION);
		registerReceiver(mMessageReceiver, filter);
	}

	public class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
              String messge = intent.getStringExtra(KEY_MESSAGE);
              String extras = intent.getStringExtra(KEY_EXTRAS);
              StringBuilder showMsg = new StringBuilder();
              showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
              if (!ExampleUtil.isEmpty(extras)) {
            	  showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
              }
//              setCostomMsg(showMsg.toString());
			}
		}
	}
	*/
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.finish();
	}
	
    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }
        return true;
    }

//	class MyHandler extends Handler{
//		@Override
//		public void handleMessage(Message msg) {
//			// TODO Auto-generated method stub
//			super.handleMessage(msg);
//			if (msg.what == LOGIN) {
//			    
//			    
//			}
//			if (msg.what == NEXT){
//				
//			}
//			if (msg.what == UNCONNECT){
//				
//			}
//		}
//	}
//	
	class MyPlayer extends Thread{
		private int flag;
		public MyPlayer(int flag) {
			// TODO Auto-generated constructor stub
			this.flag = flag;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				System.out.println("logoactivity11111111111");
				Thread.sleep(1 * 1000);
				System.out.println("logoactivity22222222222");
				switch(flag){
				case 1:try{
						    String encodedPassword = DecodeUtils.getEncodedPassword(password);
						    LoginRequest loginReq = new LoginRequest(account, "Common",
						                     encodedPassword, "1.0.0.1");
						    LoginResponse loginRes = mSoapManager.getUserLoginRes(loginReq);
						    if(loginRes.getResult().equals("OK")){
						    	GetNATServerRes res = mSoapManager.getGetNATServerRes(new GetNATServerReq(account, loginRes.getLoginSession()));
						    	Log.e("LogoActivity", res.toString());
						        Intent intent = new Intent(LogoActivity.this,CamTabActivity.class);
						        startActivity(intent);
					        }else if(loginRes.getResult().equals("PasswordFormat")){
					        	Intent intent = new Intent(LogoActivity.this,MainActivity.class);
								startActivity(intent);
					        }else{
					        	Intent intent = new Intent(LogoActivity.this,MainActivity.class);
								startActivity(intent);
					        }
					        
					        /*if (loginRes == null) {
					            MessageUtiles.postNewUIDialog(getApplicationContext(), getResources().getString(R.string.message), getResources().getString(R.string.ok),1);
					            return;
					        }*/
					    }catch (Exception e) {
								// TODO: handle exception
					    	Intent intent = new Intent(LogoActivity.this,MainActivity.class);
							intent.putExtra("intentFlag", 2);
							startActivity(intent);
		//			    	MessageUtiles.postNewUIDialog(LogoActivity.getContext(), LogoActivity.getContext().getString(R.string.login_error), LogoActivity.getContext().getString(R.string.ok), 1);
						}
						break;
				case 2:Intent intent = new Intent(LogoActivity.this,MainActivity.class);
					   startActivity(intent);
					   break;
				case 3:Intent intent2 = new Intent(LogoActivity.this,MainActivity.class);
					   intent2.putExtra("intentFlag", 1);
					   startActivity(intent2);
					   break;
				default:break;
				}
					
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
	@Override
	public void gotResult(int code, String alias, Set<String> tags) {
		// TODO Auto-generated method stub
		/*
		String logs ;
		switch (code) {
		case 0:
			logs = "Set tag and alias success, alias = " + alias + "; tags = " + tags;
			Log.i("", logs);
			break;
		
		default:
			logs = "Failed with errorCode = " + code + " alias = " + alias + "; tags = " + tags;
			Log.e("", logs);
		}
		ExampleUtil.showToast(logs, getApplicationContext());
		*/
	}
}
