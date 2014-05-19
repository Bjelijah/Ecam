package com.howell.webcam;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.android.howell.webcam.test.R;
import com.howell.wificontrol.WifiAdmin;

public class SetDeviceWifi extends Activity implements OnClickListener{
	private WifiAdmin mWifiAdmin;
	
	private EditText wifi_password,device_name;
	//private Button btnSend,btnSendFinish;
	private Button mOk;
	private ImageButton mBack;
	
	private Activities mActivities;
	private HomeKeyEventBroadCastReceiver receiver;
	
	private Spinner wifi_ssid;  
    private String[] Member;  
    private ArrayAdapter<String> myAdapter;  
    
    private SoapManager mSoapManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_device_wifi);
		mActivities = Activities.getInstance();
        mActivities.addActivity("SetDeviceWifi",SetDeviceWifi.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		mSoapManager = SoapManager.getInstance();
		mWifiAdmin = new WifiAdmin(this);
		System.out.println(mWifiAdmin.getWifiSSID());
		//wifi_ssid = (EditText)findViewById(R.id.et_wifi);
		wifi_password = (EditText)findViewById(R.id.et_wifi_password);
		device_name  = (EditText)findViewById(R.id.et_device_name);
		//btnSend = (Button)findViewById(R.id.btn_send);
		//btnSendFinish = (Button)findViewById(R.id.btn_send_finish);
		mOk = (Button)findViewById(R.id.ib_set_device_ok);
		mBack = (ImageButton)findViewById(R.id.ib_set_device_wifi_back);
		//wifi_ssid.setText(removeMarks(mWifiAdmin.getWifiSSID()));
		
		ArrayList<String> list = mWifiAdmin.getSSIDResultList();
		Member = new String[list.size()];
		list.toArray(Member);
		wifi_ssid = (Spinner)findViewById(R.id.spinner_wifi);  
        myAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,Member);  
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
        wifi_ssid.setAdapter(myAdapter);  
        wifi_ssid.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){  
            @Override  
            public void onItemSelected(AdapterView<?> arg0, View arg1,  
                    int arg2, long arg3) {  
                //Toast.makeText(getApplicationContext(),   
                //        "你选择了："+Member[arg2], 0).show();  
                //arg0.setVisibility(View.VISIBLE);  
            }  
            @Override  
            public void onNothingSelected(AdapterView<?> arg0) { 
            	
            }             
        });  
	    
	    //btnSend.setOnClickListener(this);
	    mBack.setOnClickListener(this);
	    mOk.setOnClickListener(this);
	    
	    SendMatchCodeTask task = new SendMatchCodeTask();
		task.execute();
	    //btnSendFinish.setOnClickListener(this);
	}
	
	public class SendMatchCodeTask extends AsyncTask<Void, Integer, Void> {
		GetDeviceMatchingCodeRes res;
        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            System.out.println("call doInBackground");
            GetDeviceMatchingCodeReq req = new GetDeviceMatchingCodeReq(mSoapManager.getLoginResponse().getAccount(),mSoapManager.getLoginResponse().getLoginSession());
            res = mSoapManager.getGetDeviceMatchingCodeRes(req);
            
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
        	// TODO Auto-generated method stub
        	super.onPostExecute(result);
        	System.out.println(res.getResult()+","+res.getMatchingCode());
        }
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
			//String code = SoapManager.getInstance().getmGetDeviceMatchingCodeRes().getMatchingCode();
			//String message = "Wo:"+wifi_ssid.getSelectedItem().toString()+"|"+wifi_password.getText().toString()+"|"+code;
			//System.out.println("message:"+message);
			Intent intent = new Intent(SetDeviceWifi.this,FlashLighting.class);
			intent.putExtra("wifi_ssid", wifi_ssid.getSelectedItem().toString());
			intent.putExtra("wifi_password", wifi_password.getText().toString());
			intent.putExtra("device_name", device_name.getText().toString());
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
