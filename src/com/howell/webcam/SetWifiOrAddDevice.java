package com.howell.webcam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.android.howell.webcam.R;

public class SetWifiOrAddDevice extends Activity implements OnClickListener{
	private FrameLayout mSetWifi,mAddDevice;
	private ImageButton mBack;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_device_step_select);
		mSetWifi = (FrameLayout)findViewById(R.id.fl_set_wifi);
		mAddDevice = (FrameLayout)findViewById(R.id.fl_add_device);
		
		mBack = (ImageButton)findViewById(R.id.ib_add_device_back);
		
		mSetWifi.setOnClickListener(this);
		mAddDevice.setOnClickListener(this);
		mBack.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.fl_set_wifi:
			Intent intent = new Intent(SetWifiOrAddDevice.this,SetOrResetWifi.class);
			startActivity(intent);
			break;

		case R.id.fl_add_device:
			intent = new Intent(SetWifiOrAddDevice.this,AddCamera.class);
			startActivity(intent);
			break;
			
		case R.id.ib_add_device_back:
			finish();
			break;
		default:
			break;
		}
	}
}
