package com.howell.activity;

/**
* @author 霍之昊 
* 
* 类说明：app登录页面
*/

import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.android.howell.webcam.R;
import com.howell.utils.DecodeUtils;
import com.howell.utils.NetWorkUtils;
import com.howell.protocol.GetNATServerReq;
import com.howell.protocol.GetNATServerRes;
import com.howell.protocol.LoginRequest;
import com.howell.protocol.LoginResponse;
import com.howell.protocol.SoapManager;

public class LogoActivity extends Activity implements TagAliasCallback{
	//与平台交互协议单例
	private SoapManager mSoapManager;
	
	//是否显示开场导航标志位，存于配置文件中
	private boolean isFirstLogin;
	
	private String account;
	private String password;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo);
	    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectNetwork().build());
		
	    //推送服务初始化
	    JPushInterface.init(getApplicationContext());
		setAlias();
		if(JPushInterface.isPushStopped(getApplicationContext())) 
			JPushInterface.resumePush(getApplicationContext());
		
		//判断手机是否连接网络
		if (!NetWorkUtils.isNetworkConnected(this)) {
			LoginThread myLoginThread = new LoginThread(3);
	    	myLoginThread.start();
		}else{
			//清空存储设备信息单例对象
			DeviceManager mDeviceManager = DeviceManager.getInstance();
			mDeviceManager.clearMember();
			
			//获取平台协议单例对象
			mSoapManager = SoapManager.getInstance();
			
			//从配置文件获取开场导航界面标志位不存在则为true，获取用户名和密码如果不存在则为空字符串
		    SharedPreferences sharedPreferences = getSharedPreferences("set",Context.MODE_PRIVATE);
		    isFirstLogin = sharedPreferences.getBoolean("isFirstLogin", true);
		    account = sharedPreferences.getString("account", "");
		    password = sharedPreferences.getString("password", "");
		    
		    //如果用户以前登录过app（配置文件中用户名，密码不为空）则直接登录
		    if(!account.equals("") && !password.equals("")){
		    	LoginThread myLoginThread = new LoginThread(1);
		    	myLoginThread.start();
		    }else{
		    //如果用户以前没有登陆过app（用户名，密码为空字符串）则进入注册、登录、演示界面
		    	LoginThread myLoginThread = new LoginThread(2);
		    	myLoginThread.start();
		    }
		}
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.finish();
	}
	
	class LoginThread extends Thread{
		private int flag;
		public LoginThread(int flag) {
			// TODO Auto-generated constructor stub
			this.flag = flag;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				Thread.sleep(1 * 1000);
				//第一次进入程序加载欢迎导航界面
				if(isFirstLogin){
			    	Intent intent = new Intent(LogoActivity.this, NavigationActivity.class);
					startActivity(intent);
				}else{
					switch(flag){
					case 1:
						try{
							//登录协议实现
							String encodedPassword = DecodeUtils.getEncodedPassword(password);
							LoginRequest loginReq = new LoginRequest(account, "Common",encodedPassword, "1.0.0.1");
							LoginResponse loginRes = mSoapManager.getUserLoginRes(loginReq);
							if(loginRes.getResult().equals("OK")){
							//登录成功则进入摄像机列表界面
							    GetNATServerRes res = mSoapManager.getGetNATServerRes(new GetNATServerReq(account, loginRes.getLoginSession()));
							    Log.e("LogoActivity", res.toString());
							    Intent intent = new Intent(LogoActivity.this,CamTabActivity.class);
							    startActivity(intent);
						    }else{
						    //登录不成功则进入注册、登录、演示界面
						        Intent intent = new Intent(LogoActivity.this,RegisterOrLogin.class);
								startActivity(intent);
						    }
						}catch (Exception e) {
							// TODO: handle exception
							//若网络不好发生各种exception则进入注册、登录、演示界面
						    Intent intent = new Intent(LogoActivity.this,RegisterOrLogin.class);
						    intent.putExtra("intentFlag", 2);
						    startActivity(intent);
						}
						break;
					case 2:
						//如果用户以前没有登陆过app（用户名，密码为空字符串）则进入注册、登录、演示界面
						Intent intent = new Intent(LogoActivity.this,RegisterOrLogin.class);
						startActivity(intent);
						break;
					case 3:Intent intent2 = new Intent(LogoActivity.this,RegisterOrLogin.class);
					   intent2.putExtra("intentFlag", 1);
					   startActivity(intent2);
					default:break;
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    }
	
	//设置推送别名（老版本用ANDROID_ID作为别名）
	private void setAlias(){
		String alias = Secure.getString(getContentResolver(), Secure.ANDROID_ID);//"112233";
		JPushInterface.setAliasAndTags(getApplicationContext(), alias, null, this);
	}
	
	//Jpush推送服务器设置别名回调，返回设置结果
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
