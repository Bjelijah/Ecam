package com.howell.webcam;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.howell.webcam.R;

public class ModifyPassword extends Activity implements OnClickListener{
	private Activities mActivities;
	private HomeKeyEventBroadCastReceiver receiver;
	private ImageButton mBack;
	private Button mOk;
	private EditText mOriginalPassword,mNewPassword,mConfirmPassword;
	private UpdatePasswordRes res;
	private SoapManager mSoapManager;
	private MyHandler handler;
	private static final int PASSWORD_DIF = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_password);
		mActivities = Activities.getInstance();
        mActivities.addActivity("ModifyPassword",ModifyPassword.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
		mSoapManager = SoapManager.getInstance();
		handler = new MyHandler();
		
		mOriginalPassword = (EditText)findViewById(R.id.et_original_password);
		mNewPassword = (EditText)findViewById(R.id.et_new_password);
		mConfirmPassword = (EditText)findViewById(R.id.et_confirm_password);
		mBack = (ImageButton)findViewById(R.id.ib_modify_password_back);
		mBack.setOnClickListener(this);
		mOk = (Button)findViewById(R.id.ib_modify_password_ok);
		mOk.setOnClickListener(this);
		
		
	}
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.removeActivity("ModifyPassword");
    	unregisterReceiver(receiver);
    }

    class MyHandler extends Handler{
    	@Override
    	public void handleMessage(Message msg) {
    		// TODO Auto-generated method stub
    		super.handleMessage(msg);
    		switch (msg.what) {
			case PASSWORD_DIF:
				MessageUtiles.postToast(getApplicationContext(), "密码不一致，请重新输入", 1000);
				mOriginalPassword.setText("");
				mNewPassword.setText("");
				mConfirmPassword.setText("");
				break;

			default:
				break;
			}
    	}
    }
    
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		final ProgressDialog pd;
		switch (view.getId()) {
		case R.id.ib_modify_password_back:
			finish();
			break;
		case R.id.ib_modify_password_ok:
			pd = new ProgressDialog(ModifyPassword.this);  
	        pd.setTitle(getResources().getString(R.string.set_new_password)+"...");   //���ñ���  
	        pd.setMessage(getResources().getString(R.string.please_wait)+"..."); //����body��Ϣ  
	        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER); //���ý������ʽ�� ����� 
			pd.show();
			new AsyncTask<Void, Void, Void>() {
				protected Void doInBackground(Void... params) {
					String originalPassword = mOriginalPassword.getText().toString();
					String newPassword = mNewPassword.getText().toString();
					String confirmPassword = mConfirmPassword.getText().toString();
					if(!newPassword.equals(confirmPassword)){
						handler.sendEmptyMessage(PASSWORD_DIF);
						return null;
					}
					System.out.println("originalPassword:"+originalPassword+" newPassword:"+newPassword);
					try{
						LoginResponse loginRes = mSoapManager.getLoginResponse();
						UpdatePasswordReq req = new UpdatePasswordReq(loginRes.getAccount(),loginRes.getLoginSession(),DecodeUtils.getEncodedPassword(originalPassword),DecodeUtils.getEncodedPassword(confirmPassword));
						res = mSoapManager.getUpdatePasswordRes(req);
						System.out.println(res.getResult());
					}catch (Exception e) {
						// TODO: handle exception
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					try{
						pd.dismiss();
						if(res.getResult().equals("OK")){
							MessageUtiles.postToast(ModifyPassword.this.getApplicationContext(), "密码设置成功", 1000);
							ModifyPassword.this.finish();
						}else if(res.getResult().equals("PasswordFormat")){
							MessageUtiles.postToast(ModifyPassword.this.getApplicationContext(), "密码格式不正确，请重新输入", 1000);
						}else if( res.getResult().equals("Authencation")){
							MessageUtiles.postToast(ModifyPassword.this.getApplicationContext(), "密码不正确，请重新输入", 1000);
						}
					}catch (Exception e) {
						// TODO: handle exception
					}
				}
			}.execute();
			
			break;
		default:
			break;
		}
	}
}
