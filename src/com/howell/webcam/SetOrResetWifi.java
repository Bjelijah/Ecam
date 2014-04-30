package com.howell.webcam;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.howell.webcam.R;

public class SetOrResetWifi extends Activity implements OnClickListener{
	private FrameLayout setWifi,resetWifi;
	private ImageButton mBack;
	private TextView greenLightTips,redLightTips;
	private Activities mActivities;
	private HomeKeyEventBroadCastReceiver receiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_wifi_or_reset_wifi);
		mActivities = Activities.getInstance();
        mActivities.addActivity("SetOrResetWifi",SetOrResetWifi.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
		setWifi = (FrameLayout)findViewById(R.id.fl_set_wifi);
		resetWifi = (FrameLayout)findViewById(R.id.fl_reset_wifi);
		mBack = (ImageButton)findViewById(R.id.ib_reset_wifi_back);
		greenLightTips = (TextView)findViewById(R.id.tv_green_light_tips);
		redLightTips = (TextView)findViewById(R.id.tv_red_light_tips);
		
		TextPaint tp = greenLightTips.getPaint();
        tp.setFakeBoldText(true);
        
        tp = redLightTips.getPaint();
        tp.setFakeBoldText(true);
        
		setWifi.setOnClickListener(this);
		resetWifi.setOnClickListener(this);
		mBack.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fl_set_wifi:
			Intent intent = new Intent(SetOrResetWifi.this,SetDeviceWifi.class);
			startActivity(intent);
			break;
		case R.id.fl_reset_wifi:
			intent = new Intent(SetOrResetWifi.this,FlashLighting.class);
			startActivity(intent);
			break;
		case R.id.ib_reset_wifi_back:
			finish();
			break;
		default:
			break;
		}
	}
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.removeActivity("SetOrResetWifi");
    	unregisterReceiver(receiver);
    }
}
