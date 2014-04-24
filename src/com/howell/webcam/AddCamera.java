package com.howell.webcam;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.howell.webcam.R;
import com.wyy.twodimcode.CaptureActivity;

public class AddCamera extends Activity implements OnClickListener{
	private ImageButton mBack;
	private Button ok,scan,search;
	private EditText devId,devKey;
	
	private Dialog waitDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_camera);
		mBack = (ImageButton)findViewById(R.id.ib_add_camera_back);
		ok = (Button)findViewById(R.id.btn_add_camera_ok);
		scan = (Button)findViewById(R.id.btn_add_camera_scan);
		search = (Button)findViewById(R.id.btn_add_camera_search);
		devId = (EditText)findViewById(R.id.et_device_id);
		devKey = (EditText)findViewById(R.id.et_device_key);
		
		mBack.setOnClickListener(this);
		ok.setOnClickListener(this);
		scan.setOnClickListener(this);
		search.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ib_add_camera_back:
			finish();
			break;
		case R.id.btn_add_camera_ok:
			waitDialog = MessageUtiles.postNewUIDialog(AddCamera.this);
			waitDialog.show();
			new AsyncTask<Void, Integer, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub
					super.onPostExecute(result);
					waitDialog.dismiss();
				}
				
			}.execute();
			break;
		case R.id.btn_add_camera_scan:
			Intent it = new Intent(AddCamera.this, CaptureActivity.class);
			startActivityForResult(it, 1);
			break;
		case R.id.btn_add_camera_search:
			
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
				if(result != null)
					//tv.setText(result);
					System.out.println(result);
				Uri uri = Uri.parse(result);  
				Intent it = new Intent(Intent.ACTION_VIEW, uri);  
				startActivity(it);
			}
			break;

		default:
			break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
}
