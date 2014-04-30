package com.howell.webcam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

public class SetDeviceWifi extends Activity implements OnClickListener{
	private WifiAdmin mWifiAdmin;
	
	private EditText wifi_ssid,wifi_password;
	//private Button btnSend,btnSendFinish;
	private Button mOk;
	private ImageButton mBack;
	
	private Activities mActivities;
	private HomeKeyEventBroadCastReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_device_wifi);
		mActivities = Activities.getInstance();
        mActivities.addActivity("SetDeviceWifi",SetDeviceWifi.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		mWifiAdmin = new WifiAdmin(this);
		System.out.println(mWifiAdmin.getWifiSSID());
		wifi_ssid = (EditText)findViewById(R.id.et_wifi);
		wifi_password = (EditText)findViewById(R.id.et_wifi_password);
		//btnSend = (Button)findViewById(R.id.btn_send);
		//btnSendFinish = (Button)findViewById(R.id.btn_send_finish);
		mOk = (Button)findViewById(R.id.ib_set_device_ok);
		mBack = (ImageButton)findViewById(R.id.ib_set_device_wifi_back);
		wifi_ssid.setText(removeMarks(mWifiAdmin.getWifiSSID()));
	    
	    //btnSend.setOnClickListener(this);
	    mBack.setOnClickListener(this);
	    mOk.setOnClickListener(this);
	    //btnSendFinish.setOnClickListener(this);
	}
	
	private String removeMarks(String SSID){
		if(SSID.startsWith("\"") && SSID.endsWith("\"")){
			SSID = SSID.substring(1, SSID.length()-1);
		}
		return SSID;
	}
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.removeActivity("SetDeviceWifi");
    	unregisterReceiver(receiver);
    }
    
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		/*case R.id.btn_send:
			
			send();
			break;
			
		case R.id.btn_send_finish:
			Dialog alertDialog = new AlertDialog.Builder(this).   
            setTitle("完成").   
            setMessage("Wifi设置已完成，您要继续添加设备吗？").   
            setIcon(R.drawable.expander_ic_minimized).   
            setPositiveButton("确定", new DialogInterface.OnClickListener() {   

                @Override   
                public void onClick(DialogInterface dialog, int which) {   
                    // TODO Auto-generated method stub    
                	Intent intent = new Intent(SetDeviceWifi.this,AddCamera.class);
                	startActivity(intent);
                	finish();
                	
                }   
            }).   
            setNegativeButton("取消", new DialogInterface.OnClickListener() {   

                @Override   
                public void onClick(DialogInterface dialog, int which) {   
                    // TODO Auto-generated method stub    
                	
                	//Intent intent = new Intent(SetDeviceWifi.this,CameraList.class);
                	//startActivity(intent);
                	finish();
                	mActivities.getmActivityList().get("SetOrResetWifi").finish();
                	mActivities.getmActivityList().get("SetWifiOrAddDevice").finish();
                }   
            }).   
            create();   
			alertDialog.show();   
			break;*/
		case R.id.ib_set_device_ok:
			String message = "W:"+wifi_ssid.getText().toString()+"|"+wifi_password.getText().toString();
			Intent intent = new Intent(SetDeviceWifi.this,SendWifi.class);
			intent.putExtra("wifi_message", message);
        	startActivity(intent);
			break;
		case R.id.ib_set_device_wifi_back:
			finish();
			break;

		default:
			break;
		}
	}

}
