package com.howell.activity;

import java.util.ArrayList;

import android.app.TabActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

import com.android.howell.webcam.R;
import com.howell.broadcastreceiver.HomeKeyEventBroadCastReceiver;
//import com.howell.ehlib.MyListView.OnRefreshListener;
import com.howell.entityclass.NodeDetails;
//import com.howell.utils.UpdateCameraUtils;
import com.howell.protocol.LoginResponse;
import com.howell.protocol.SoapManager;

@SuppressWarnings("deprecation")
public class CamTabActivity extends TabActivity implements
        OnCheckedChangeListener {

    private TabHost mHost;
    private RadioGroup mGroup;
    private RadioButton mCameraList,mLocalFiles,mSettings;
    
    private Activities mActivities;
    private HomeKeyEventBroadCastReceiver receiver;
    
    private SoapManager mSoapManager;
    //static int updateNum;
    
    static boolean cameraVerThread;
    
    //private static BadgeView badge;
    ArrayList<NodeDetails> list;
    LoginResponse mResponse;
//    private static final int TOGGLEON = 1;
//    private static final int TOGGLEOFF = 2;
    private static boolean hasToggled;
    
//    private DeviceVersionDetect detect;
    /*static Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		// TODO Auto-generated method stub
    		super.handleMessage(msg);
    		if(msg.what == TOGGLEON){
    			badge.toggle(1);
    		}
    		if(msg.what == TOGGLEOFF){
    			badge.toggle(0);
    		}
    	}
    };*/
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cam_tab);
        Log.e("CamTabActivity", "onCreate");
        //updateNum = 0;
        hasToggled = false;
        cameraVerThread = false;
        mActivities = Activities.getInstance();
        mActivities.addActivity("CamTabActivity",CamTabActivity.this);
        
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        mGroup = (RadioGroup) findViewById(R.id.radio_group);
        mGroup.setOnCheckedChangeListener(this);
        mCameraList = (RadioButton)findViewById(R.id.rb_camera_list);
        mLocalFiles = (RadioButton)findViewById(R.id.rb_local_files);
        mSettings = (RadioButton)findViewById(R.id.rb_settings);

        mHost = getTabHost();
        mHost.addTab(mHost
                .newTabSpec("cameralist")
                .setIndicator(getResources().getString(R.string.camera_list),
                        getResources().getDrawable(R.drawable.camera))
                .setContent(new Intent(this, CameraList.class)));
        mHost.addTab(mHost
                .newTabSpec("localfiles")
                .setIndicator(getResources().getString(R.string.local_files),
                        getResources().getDrawable(R.drawable.tab_camera_selector))
                .setContent(new Intent(this, LocalFilesActivity.class)));

        mHost.addTab(mHost
                .newTabSpec("settings")
                .setIndicator(getResources().getString(R.string.settings),
                        getResources().getDrawable(R.drawable.setting))
                .setContent(new Intent(this, Settings.class)));
        mHost.setCurrentTab(0);  
        
       /* badge = new BadgeView(this, mGroup);*/
        
        mSoapManager = SoapManager.getInstance();
        mResponse = mSoapManager.getLoginResponse();
        
//        detect = DeviceVersionDetect.getInstance();
        
        list = mSoapManager.getNodeDetails();
        
        /* new Thread (){
        	@Override
        	public void run() {
        		// TODO Auto-generated method stub
        		super.run();
        		while(true){
        			if(cameraVerThread == true){
        				break;
        			}
        		}
        		try{
	        		for(NodeDetails d:list){
	                	System.out.println("aaaaaa");
	                	GetDevVerReq getDevVerReq = new GetDevVerReq(mResponse.getAccount(),mResponse.getLoginSession(),d.getDevID());
	                	GetDevVerRes res = mSoapManager.getGetDevVerRes(getDevVerReq);
	                	Log.e("GetDevVerRes", res.toString());
	                	if(d.isOnLine() && UpdateCameraUtils.needToUpdate(res.getCurDevVer(), res.getNewDevVer())){
	                		System.out.println(res.getCurDevVer()+","+res.getNewDevVer());
	                		//updateNum++;
	                		d.setHasUpdate(true);
//	                		if(updateNum == 1){
//	                        	//badge.setText(String.valueOf(updateNum));
//	                    		handler.sendEmptyMessage(TOGGLEON);
//	                        }
	                		//return;
	                	}
	                	//CameraList.adapter.notifyDataSetChanged();
	                }
	        		
	        		detect.onDeviceNewVersionRefresh();
        		}catch(Exception e){
                	System.out.println("getDevVerReq crash");
                }
        		//Log.e("updateNum", updateNum+"");
//                if(updateNum > 0){
//                	//badge.setText(String.valueOf(updateNum));
//            		handler.sendEmptyMessage(TOGGLEON);
//                }
        		
        	}
        }.start();*/
        
    }
    
	@Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO Auto-generated method stub
        switch (checkedId) {
        case R.id.rb_camera_list:
            mHost.setCurrentTabByTag("cameralist");
            mCameraList.setTextColor(getResources().getColor(R.color.blue));
            mLocalFiles.setTextColor(getResources().getColor(R.color.light_gray));
            mSettings.setTextColor(getResources().getColor(R.color.light_gray));
            break;
        case R.id.rb_local_files:
            mHost.setCurrentTabByTag("localfiles");
            mLocalFiles.setTextColor(getResources().getColor(R.color.blue));
            mCameraList.setTextColor(getResources().getColor(R.color.light_gray));
            mSettings.setTextColor(getResources().getColor(R.color.light_gray));
            break;
        case R.id.rb_settings:
            mHost.setCurrentTabByTag("settings");
            mSettings.setTextColor(getResources().getColor(R.color.blue));
            mLocalFiles.setTextColor(getResources().getColor(R.color.light_gray));
            mCameraList.setTextColor(getResources().getColor(R.color.light_gray));
            break;
        default:
            break;
        }
    }
	
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	Log.e("CamTab","onPause");
//    	for(Activity a:mActivities.getmActivityList()){
//    		a.finish();
//    	}
    }
    
    @Override
    protected void onRestart() {
    	// TODO Auto-generated method stub
    	super.onRestart();
    	Log.e("CamTab","onRestart:"+hasToggled);
    	/*if(updateNum == 0){
    		if(!hasToggled){
    			System.out.println("toggle");
    			handler.sendEmptyMessage(TOGGLEOFF);
    			hasToggled = true;
    		}
    	}*/
    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	Log.e("CamTab","onResume");
    }
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	Log.e("CamTab", "onStop");
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	Log.e("CamTab", "onDestroy");
    	super.onDestroy();
    	mActivities.removeActivity("CamTabActivity");
    	unregisterReceiver(receiver);
    }
}
