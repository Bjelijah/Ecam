package com.howell.webcam;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.howell.webcam.R;
import com.howell.cameraconctrol.CameraUtils;

public class FlashLighting extends Activity implements OnClickListener{
	private TextView tips;
	private Button mOk;
	private ImageButton mBack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flash_light);
		tips = (TextView)findViewById(R.id.tv_flash_light_success);
		mOk = (Button)findViewById(R.id.btn_flash_light_ok);
		mBack = (ImageButton)findViewById(R.id.ib_flash_light_back);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_flash_light_ok:
			CameraUtils camera = new CameraUtils();
			camera.twinkle();
			break;
		case R.id.ib_flash_light_back:
			finish();
			break;
		default:
			break;
		}
	}

}
