package com.howell.webcam.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.howell.webcam.R;
import com.howell.broadcastreceiver.HomeKeyEventBroadCastReceiver;
import com.howell.webcam.LoginResponse;
import com.howell.webcam.SoapManager;

public class InformationActivity extends Activity implements OnClickListener{
    private SoapManager mSoapManager;
    //private AccountResponse mResponse;

    private TextView mAccountName;
    private ImageButton mBack;
    private FrameLayout mChangePhoneNum,mChangePassword,mChangeMail;
    private Activities mActivities;
    private HomeKeyEventBroadCastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);
        mActivities = Activities.getInstance();
        mActivities.addActivity("InformationActivity",InformationActivity.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        mAccountName = (TextView) findViewById(R.id.account_name);
        mBack = (ImageButton)findViewById(R.id.ib_information_back);
        mChangePhoneNum = (FrameLayout)findViewById(R.id.fl_change_phone_num);
        mChangePassword = (FrameLayout)findViewById(R.id.fl_change_password);
        mChangeMail = (FrameLayout)findViewById(R.id.fl_change_mail);
        mChangePassword.setOnClickListener(this);
        mChangePhoneNum.setOnClickListener(this);
        mChangeMail.setOnClickListener(this);
        mBack.setOnClickListener(this);
        
        try{
	        mSoapManager = SoapManager.getInstance();
	        LoginResponse loginResponse = mSoapManager.getLoginResponse();
	        if (loginResponse.getResult().toString().equals("OK")) {
	            String account = loginResponse.getAccount().toString();
	            mAccountName.setText(account);
	        }
        }catch (Exception e) {
			// TODO: handle exception
		}
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.removeActivity("InformationActivity");
    	unregisterReceiver(receiver);
    }

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (view.getId()) {
		case R.id.fl_change_phone_num:
			intent = new Intent(InformationActivity.this,ModifyPhoneNum.class);
        	startActivity(intent);
			break;
		case R.id.fl_change_password:
			intent = new Intent(InformationActivity.this,ModifyPassword.class);
        	startActivity(intent);
			break;
		case R.id.ib_information_back:
			finish();
			break;
		default:
			break;
		}
	}
}
