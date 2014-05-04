package com.howell.webcam;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.howell.webcam.R;
import com.howell.cameraconctrol.CameraUtils;

public class FlashLighting extends Activity implements OnClickListener{
	private TextView tips,btnTips;
	private ImageButton mBack,mFlashLight;
	private Activities mActivities;
	private HomeKeyEventBroadCastReceiver receiver;
	private CameraUtils c;
	private boolean isBtnClicked;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flash_light);
		isBtnClicked = false;
		mActivities = Activities.getInstance();
        mActivities.addActivity("FlashLighting",FlashLighting.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
		c = new CameraUtils();
		
		tips = (TextView)findViewById(R.id.tv_flash_light_success);
		mBack = (ImageButton)findViewById(R.id.ib_flash_light_back);
		mFlashLight = (ImageButton)findViewById(R.id.ib_flash_light);
		btnTips = (TextView)findViewById(R.id.tv_flash_light);
		//mFinish = (Button)findViewById(R.id.btn_flash_light_finish);
		
		mBack.setOnClickListener(this);
		mFlashLight.setOnClickListener(this);
		//mFinish.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		/*case R.id.btn_flash_light_ok:
			if(!isBtnClicked){
				camera.twinkle(tips);
				tips.setVisibility(View.VISIBLE);
				mOk.setText("完成");
				isBtnClicked = true;
			}else{
				camera.stopTwinkle();
				Intent intent = new Intent(FlashLighting.this,SetDeviceWifi.class);
				startActivity(intent);
				finish();
			}
			break;*/
		/*case R.id.btn_flash_light_finish:
			Intent intent = new Intent(FlashLighting.this,SetDeviceWifi.class);
			startActivity(intent);
			finish();
			break;*/
		case R.id.ib_flash_light:
			if(!isBtnClicked){
				c.twinkle();
				tips.setVisibility(View.VISIBLE);
				isBtnClicked = true;
				mFlashLight.setImageDrawable(getResources().getDrawable(R.drawable.send_wifi_finish_btn_selector));
				btnTips.setText("完成");
			}else{
				c.stopTwinkle();
				Intent intent = new Intent(FlashLighting.this,SetDeviceWifi.class);
				startActivity(intent);
				finish();
			}
			break;
			
		case R.id.ib_flash_light_back:
			if(c.getCamera() != null){
				c.stopTwinkle();
			}
			finish();
			break;
		default:
			break;
		}
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if(c.getCamera() != null){
				c.stopTwinkle();
			}
			finish();
        }
        return false;
    }
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.removeActivity("FlashLighting");
    	unregisterReceiver(receiver);
    }

}
