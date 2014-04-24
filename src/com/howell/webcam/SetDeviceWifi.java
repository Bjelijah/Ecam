package com.howell.webcam;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.howell.webcam.R;
import com.howell.wificontrol.WifiAdmin;
import com.xququ.OfflineSDK.XQuquerService;
import com.xququ.OfflineSDK.XQuquerService.XQuquerListener;

public class SetDeviceWifi extends Activity implements OnClickListener, XQuquerListener{
	private WifiAdmin mWifiAdmin;
	
	private EditText wifi_ssid,wifi_password;
	private XQuquerService xququerService;
	
	private Button btnSend;
	private ImageButton mBack;
	public  AudioManager audiomanage;  
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_device_wifi);
		mWifiAdmin = new WifiAdmin(this);
		System.out.println(mWifiAdmin.getWifiSSID());
		wifi_ssid = (EditText)findViewById(R.id.et_wifi);
		wifi_password = (EditText)findViewById(R.id.et_wifi_password);
		btnSend = (Button)findViewById(R.id.btn_send);
		mBack = (ImageButton)findViewById(R.id.ib_set_device_wifi_back);
		wifi_ssid.setText(removeMarks(mWifiAdmin.getWifiSSID()));
		audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE); 
	    int maxVolume = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  
	    audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume - 1 , 0);
	    
	    xququerService = XQuquerService.getInstance();
	    btnSend.setOnClickListener(this);
	    mBack.setOnClickListener(this);
	}
	
	private String removeMarks(String SSID){
		if(SSID.startsWith("\"") && SSID.endsWith("\"")){
			SSID = SSID.substring(1, SSID.length()-1);
		}
		return SSID;
	}
	
	@Override
	protected void onStart()
	{
		Log.i("", "onStart");
		super.onStart();
		xququerService.start(this);		
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		xququerService.stop();		
		Log.i("", "onStop");
	}

	@Override
	public void onRecv(byte[] data) {
		// TODO Auto-generated method stub
		String message = new String(data);
		//MessageUtiles.postToast(this, "onRecv:"+message, 2000);
		Log.i("", "onRecv:"+message);
	}

	@Override
	public void onSend() {
		// TODO Auto-generated method stub
		Log.i("", "onSend");
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_send:
			
			send();
			break;
			
		case R.id.ib_set_device_wifi_back:
			finish();
			break;

		default:
			break;
		}
	}
	
	private void send()
	{
		String message = "W:"+wifi_ssid.getText().toString()+";"+wifi_password.getText().toString();
		System.out.println(message);
		byte[] data = message.getBytes();
		if(data.length>0) xququerService.sendData(data, 0.5f);  //0.0 ~ 1.0
	}
}
