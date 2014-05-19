package com.howell.webcam;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.howell.webcam.test.R;
import com.howell.cameraconctrol.CameraUtils;

public class FlashLighting extends Activity implements OnClickListener{
	private TextView /*tips,*/btnTips;
	private ImageButton mBack,mFlashLight;
	//private ImageView mBackground;
	private LinearLayout mSucceedTips;
	private Activities mActivities;
	private HomeKeyEventBroadCastReceiver receiver;
	private CameraUtils c;
	private boolean isBtnClicked;
	private String wifi_ssid,wifi_password,device_name;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flash_light);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		isBtnClicked = false;
		mActivities = Activities.getInstance();
        mActivities.addActivity("FlashLighting",FlashLighting.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
		Intent intent = getIntent();
		wifi_ssid = intent.getStringExtra("wifi_ssid");
		wifi_password = intent.getStringExtra("wifi_password");
		device_name = intent.getStringExtra("device_name");
		
		c = new CameraUtils();
		
		//tips = (TextView)findViewById(R.id.tv_flash_light_success);
		mBack = (ImageButton)findViewById(R.id.ib_flash_light_back);
		mFlashLight = (ImageButton)findViewById(R.id.ib_flash_light);
		btnTips = (TextView)findViewById(R.id.tv_flash_light);
		mSucceedTips = (LinearLayout)findViewById(R.id.ll_flash_light_success);
		//mBackground = (ImageView)findViewById(R.id.iv_flash_background2);
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
//				tips.setVisibility(View.VISIBLE);
				isBtnClicked = true;
				mFlashLight.setImageDrawable(getResources().getDrawable(R.drawable.ok_btn_red_selector));
				btnTips.setText("变红了，点这里");
				btnTips.setTextColor(getResources().getColor(R.color.red));
				mSucceedTips.setVisibility(View.VISIBLE);
			}else{
				c.stopTwinkle();
				Intent intent = new Intent(FlashLighting.this,SendWifi.class);
				intent.putExtra("wifi_ssid", wifi_ssid);
				intent.putExtra("wifi_password", wifi_password);
				intent.putExtra("device_name", device_name);
				startActivity(intent);
				//finish();
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
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onRestart();
		isBtnClicked = false;
		mFlashLight.setImageDrawable(getResources().getDrawable(R.drawable.flash_light_btn_selecor));
		btnTips.setText("闪一闪");
		btnTips.setTextColor(getResources().getColor(R.color.btn_blue_color));
		mSucceedTips.setVisibility(View.GONE);
	}
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.removeActivity("FlashLighting");
    	unregisterReceiver(receiver);
    }

}
