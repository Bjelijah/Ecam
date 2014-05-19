package com.howell.webcam;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.howell.webcam.test.R;

public class Register extends Activity implements OnClickListener{
	private ImageButton mBack;
	private Button mRegister;
	private EditText mUserName,mEmail,mPassword,mPasswordAgain;
	private SoapManager mSoapManager;
	private CheckBox showPassword;
	private Dialog waitDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		mSoapManager = SoapManager.getInstance();
		mBack = (ImageButton)findViewById(R.id.ib_register_back);
		mRegister = (Button)findViewById(R.id.btn_register_ok);
		mUserName = (EditText)findViewById(R.id.et_register_account);
		mEmail = (EditText)findViewById(R.id.et_register_email);
		mPassword = (EditText)findViewById(R.id.et_register_password);
		mPasswordAgain = (EditText)findViewById(R.id.et_register_password_confirm);
		showPassword = (CheckBox)findViewById(R.id.cb_register_show_password);
		showPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					mPasswordAgain.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				}else{
					mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					mPasswordAgain.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				}
				
			}
		});
		
		mBack.setOnClickListener(this);
		mRegister.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.ib_register_back:
			finish();
			break;
			
		case R.id.btn_register_ok:
			//finish();
			final String password = mPassword.getText().toString();
			final String passwordAgain = mPasswordAgain.getText().toString();
			if(mUserName.getText().toString().equals("") || mPassword.getText().toString().equals("")
					|| mPasswordAgain.getText().toString().equals("")){
				MessageUtiles.postToast(Register.this, "帐号，密码不能为空", 1000);
				return;
			}
			if(!password.equals(passwordAgain)){
				MessageUtiles.postToast(Register.this, "两次密码不一致", 1000);
				return ;
			}
			waitDialog = MessageUtiles.postNewUIDialog(Register.this);
			waitDialog.show();
			new AsyncTask<Void, Integer, Void>() {
				CreateAccountRes res = null;
				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub
					String account = mUserName.getText().toString();
					String encodedPassword = DecodeUtils.getEncodedPassword(password);
					String email = mEmail.getText().toString();
					CreateAccountReq req = new CreateAccountReq(account,encodedPassword,email);
					res = mSoapManager.getCreateAccountRes(req);
					System.out.println(res.getResult());
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub
					super.onPostExecute(result);
					waitDialog.dismiss();
					if(res != null && res.getResult().equals("OK")){
						System.out.println("注册成功！");
						MessageUtiles.postToast(Register.this, "注册成功", 1000);
					}else if(res != null && res.getResult().equals("AccountExist")){
						System.out.println("注册失败！");
						MessageUtiles.postToast(Register.this, "注册失败，账户已存在", 1000);
					}else if(res != null && res.getResult().equals("EmailExist")){
						System.out.println("注册失败！");
						MessageUtiles.postToast(Register.this, "注册失败，邮箱已注册", 1000);
					}else {
						System.out.println("注册失败！");
						MessageUtiles.postToast(Register.this, "注册失败", 1000);
					}
				}
				
			}.execute();
			break;

		default:
			break;
		}
	}
}
