package com.howell.webcam;

import com.android.howell.webcam.test.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;


public class CameraProperty extends Activity {
	private TextView mDeviceName,mDeviceId,mWifiIntensity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_property);
		Intent intent = getIntent();
		NodeDetails dev = (NodeDetails) intent.getSerializableExtra("Device");
		mDeviceName = (TextView)findViewById(R.id.device_name);
		mDeviceId = (TextView)findViewById(R.id.device_id);
		if(dev != null){
			mDeviceName.setText(dev.getName());
			mDeviceId.setText(dev.getDevID());
		}
		mWifiIntensity = (TextView)findViewById(R.id.wifi_intensity);
		mWifiIntensity.setText(dev.getIntensity()+"%");
	}
}
