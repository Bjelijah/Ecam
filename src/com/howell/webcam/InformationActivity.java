package com.howell.webcam;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import com.android.howell.webcam.R;

public class InformationActivity extends Activity {
    private SoapManager mSoapManager;
    private AccountResponse mResponse;

    private TextView mAccountName;
    private TextView mEmail;
    private TextView mName;
    private TextView mMobileTel;
    private Activities mActivities;
    private HomeKeyEventBroadCastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);
        mActivities = Activities.getInstance();
        mActivities.getmActivityList().add(InformationActivity.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        mAccountName = (TextView) findViewById(R.id.account_name);
        mEmail = (TextView) findViewById(R.id.email);
        mName = (TextView) findViewById(R.id.name);
        mMobileTel = (TextView) findViewById(R.id.mobiletel);
        
        try{
	        mSoapManager = SoapManager.getInstance();
	        LoginResponse loginResponse = mSoapManager.getLoginResponse();
	        if (loginResponse.getResult().toString().equals("OK")) {
	            String account = loginResponse.getAccount().toString();
	            String loginSession = loginResponse.getLoginSession().toString();
	            AccountRequest request = new AccountRequest(account, loginSession);
	            mResponse = mSoapManager.getAccountRes(request);
	            mAccountName.setText(mResponse.getAccount());
	            mEmail.setText(mResponse.getEmail());
	            mName.setText(mResponse.getUsername());
	            mMobileTel.setText(mResponse.getMobileTel());
	        }
        }catch (Exception e) {
			// TODO: handle exception
        	Intent intent = new Intent(InformationActivity.this,LogoActivity.class);
        	startActivity(intent);
        	finish();
		}
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
//    	for(Activity a:mActivities.getmActivityList()){
//    		a.finish();
//    	}
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.getmActivityList().remove(InformationActivity.this);
    	unregisterReceiver(receiver);
    }
}
