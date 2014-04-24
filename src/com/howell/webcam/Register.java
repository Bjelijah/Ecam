package com.howell.webcam;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.android.howell.webcam.R;

public class Register extends Activity implements OnClickListener{
	private ImageButton mBack;
	private Button mRegister;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		mBack = (ImageButton)findViewById(R.id.ib_register_back);
		mRegister = (Button)findViewById(R.id.btn_register_ok);
		
		mBack.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.ib_register_back:
			finish();
			break;
			
		case R.id.btn_register_ok:
			finish();
			break;

		default:
			break;
		}
	}
}
