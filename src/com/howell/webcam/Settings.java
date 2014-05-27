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

    private View mAccount;
    private View mSysMessage;

    private Button mButton;
    private Button mExit;
    private Button mCancel;

    private Dialog mDialog;
    
    private Activities mActivities;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        Log.e("Settings", "onCreate");
        mActivities = Activities.getInstance();
    	mActivities.addActivity("Settings",Settings.this);
    	
        mAccount = findViewById(R.id.account);
        mSysMessage = findViewById(R.id.sys_message);

        mButton = (Button) findViewById(R.id.exit);
        
        mAccount.setOnClickListener(this);
        mSysMessage.setOnClickListener(this);
        mButton.setOnClickListener(this);
        
        try{
	        mSoapManager = SoapManager.getInstance();
	        LoginResponse loginResponse = mSoapManager.getLoginResponse();
	        if (loginResponse.getResult().toString().equals("OK")) {
	            String account = loginResponse.getAccount().toString();
	        }
        }catch (Exception e) {
			// TODO: handle exception
        	Intent intent = new Intent(Settings.this,LogoActivity.class);
        	startActivity(intent);
        	finish();
		}
    }
    
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        Log.e("",id+"");
        switch (id) {
        case R.id.account:
            Intent intent = new Intent(this, InformationActivity.class);
            startActivity(intent);
            break;
        case R.id.sys_message:
            break;
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
            intent = new Intent(this, RegisterOrLogin.class);
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
    }
    
    @Override
    protected void onRestart() {
    	// TODO Auto-generated method stub
    	super.onRestart();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.removeActivity("Settings");
    }
}
