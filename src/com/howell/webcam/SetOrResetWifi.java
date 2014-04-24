package com.howell.webcam;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.android.howell.webcam.R;

public class SetOrResetWifi extends Activity implements OnClickListener{
	private FrameLayout setWifi,resetWifi;
	private ImageButton mBack;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_wifi_or_reset_wifi);
		setWifi = (FrameLayout)findViewById(R.id.fl_set_wifi);
		resetWifi = (FrameLayout)findViewById(R.id.fl_reset_wifi);
		mBack = (ImageButton)findViewById(R.id.ib_reset_wifi_back);
		
		setWifi.setOnClickListener(this);
		resetWifi.setOnClickListener(this);
		mBack.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fl_set_wifi:
			
			break;
		case R.id.fl_reset_wifi:
			
			break;
		case R.id.ib_reset_wifi_back:
			finish();
			break;
		default:
			break;
		}
	}
}
