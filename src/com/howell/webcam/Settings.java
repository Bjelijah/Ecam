package com.howell.webcam;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import cn.jpush.android.api.JPushInterface;

import com.android.howell.webcam.R;
import com.wyy.twodimcode.CaptureActivity;

public class Settings extends Activity implements OnClickListener {
    private SoapManager mSoapManager;
    //private AccountResponse mResponse;

    private View mAccount;
    private View mSysMessage;
    //private View mQRcode;
   // private View mPushAlarm;

    private Button mButton;
    private Button mExit;
    private Button mCancel;

    //private TextView mInformationTextView;
    private ImageView mRedIcon;
    private Dialog mDialog;
    
    private Activities mActivities;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        Log.e("Settings", "onCreate");
        mActivities = Activities.getInstance();
    	mActivities.getmActivityList().add(Settings.this);
    	
        mAccount = findViewById(R.id.account);
        mSysMessage = findViewById(R.id.sys_message);
        //mQRcode = findViewById(R.id.qr_code);
        //mPushAlarm = findViewById(R.id.fl_push_alarm);

        mButton = (Button) findViewById(R.id.exit);
       // mInformationTextView = (TextView) findViewById(R.id.information_text);
        mRedIcon = (ImageView)findViewById(R.id.setting_red_icon);
        
        mAccount.setOnClickListener(this);
        mSysMessage.setOnClickListener(this);
        //mQRcode.setOnClickListener(this);
        //mPushAlarm.setOnClickListener(this);
        mButton.setOnClickListener(this);
        
        try{
	        mSoapManager = SoapManager.getInstance();
	        LoginResponse loginResponse = mSoapManager.getLoginResponse();
	        if (loginResponse.getResult().toString().equals("OK")) {
	            String account = loginResponse.getAccount().toString();
	            //String loginSession = loginResponse.getLoginSession().toString();
	            //AccountRequest request = new AccountRequest(account, loginSession);
	            //mResponse = mSoapManager.getAccountRes(request);
	            //mInformationTextView.setText(account);
	        }
        }catch (Exception e) {
			// TODO: handle exception
        	Intent intent = new Intent(Settings.this,LogoActivity.class);
        	startActivity(intent);
        	finish();
		}
        Log.e("setting", CamTabActivity.updateNum+"");
//        if(CamTabActivity.updateNum > 0) mRedIcon.setVisibility(View.VISIBLE);
//        else mRedIcon.setVisibility(View.INVISIBLE);
        showIcon();
    }
    
    private void showIcon(){
   	 	if(CamTabActivity.updateNum > 0) mRedIcon.setVisibility(View.VISIBLE);
        else mRedIcon.setVisibility(View.INVISIBLE);
    }
    
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        Log.e("",id+"");
        switch (id) {
        /*case R.id.qr_code:
        	Intent it = new Intent(Settings.this, CaptureActivity.class);
			startActivityForResult(it, 1);
        	break;*/
        case R.id.account:
            Intent intent = new Intent(this, InformationActivity.class);
            startActivity(intent);
            break;
//        case R.id.fl_push_alarm:
//            intent = new Intent(this, PushAlarmActivity.class);
//            startActivity(intent);
//            break;
        case R.id.sys_message:
            //intent = new Intent(this, DeviceManageActivity.class);
            //startActivity(intent);
            break;
//        case R.id.dialog:
//            showDialog();
//            break;
        case R.id.exit:
        	SharedPreferences sharedPreferences = getSharedPreferences("set",
                    Context.MODE_PRIVATE);
        	Editor editor = sharedPreferences.edit();
            editor.putString("account", "");
            editor.putString("password", "");
            editor.commit();
            System.out.println(JPushInterface.isPushStopped(getApplicationContext()));
            if(!JPushInterface.isPushStopped(getApplicationContext()))
            	JPushInterface.stopPush(getApplicationContext());
            System.out.println(JPushInterface.isPushStopped(getApplicationContext()));
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            break;
        case R.id.cancel:
            mDialog.dismiss();
            break;
        default:
            break;
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case 1:
			if(data != null){
				String result = data.getStringExtra("result");
				System.out.println("result:"+result);
				if(result != null){
					Uri uri = Uri.parse(result);  
					Intent it = new Intent(Intent.ACTION_VIEW, uri);  
					startActivity(it);
				}
			}
			break;

		default:
			break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
    
    private void showDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog, null);

        mExit = (Button) view.findViewById(R.id.exit);
        mCancel = (Button) view.findViewById(R.id.cancel);
        mExit.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        mDialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        mDialog.setContentView(view, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        Window window = mDialog.getWindow();
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams lp = window.getAttributes();

        Point size = new Point();
//        getWindowManager().getDefaultDisplay().getSize(size);
        lp.x = 0;
        lp.y = size.y;

        mDialog.onWindowAttributesChanged(lp);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
        Log.e("Setting","onPause");
        showIcon();
//        if(CamTabActivity.updateNum > 0) mRedIcon.setVisibility(View.VISIBLE);
//        else mRedIcon.setVisibility(View.INVISIBLE);
//    	for(Activity a:mActivities.getmActivityList()){
//    		a.finish();
//    	}
    }
    
    @Override
    protected void onRestart() {
    	// TODO Auto-generated method stub
    	super.onRestart();
    	if(CamTabActivity.updateNum == 0){
    		mRedIcon.setVisibility(View.INVISIBLE);
    	}
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.getmActivityList().remove(Settings.this);
    }
}
