package com.howell.webcam;

import java.util.ArrayList;

import android.app.TabActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

import com.android.howell.webcam.R;

@SuppressWarnings("deprecation")
public class CamTabActivity extends TabActivity implements
        OnCheckedChangeListener {

    private TabHost mHost;
    private RadioGroup mGroup;
    
    private Activities mActivities;
    private HomeKeyEventBroadCastReceiver receiver;
    
    private SoapManager mSoapManager;
    static int updateNum;
    
    private static BadgeView badge;
    ArrayList<Device> list;
    LoginResponse mResponse;
    private static final int TOGGLEON = 1;
    private static final int TOGGLEOFF = 2;
    private static boolean hasToggled;
    
    static Handler handler = new Handler(){
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
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cam_tab);
        Log.e("CamTabActivity", "onCreate");
        updateNum = 0;
        hasToggled =false;
        mActivities = Activities.getInstance();
        mActivities.getmActivityList().add(CamTabActivity.this);
        
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        mGroup = (RadioGroup) findViewById(R.id.radio_group);
        mGroup.setOnCheckedChangeListener(this);

        mHost = getTabHost();
        mHost.addTab(mHost
                .newTabSpec("cameralist")
                .setIndicator(getResources().getString(R.string.camera_list),
                        getResources().getDrawable(R.drawable.camera))
                .setContent(new Intent(this, CameraList.class)));

        mHost.addTab(mHost
                .newTabSpec("settings")
                .setIndicator(getResources().getString(R.string.settings),
                        getResources().getDrawable(R.drawable.setting))
                .setContent(new Intent(this, Settings.class)));
        mHost.setCurrentTab(0);  
        
        badge = new BadgeView(this, mGroup);
        
        mSoapManager = SoapManager.getInstance();
        mResponse = mSoapManager.getLoginResponse();
        if(null != mResponse)
        	list = mResponse.getNodeList();
        
        new Thread (){
        	@Override
        	public void run() {
        		// TODO Auto-generated method stub
        		super.run();
        		try{
        		for(Device d:list){
                	System.out.println("aaaaaa");
                	GetDevVerReq getDevVerReq = new GetDevVerReq(mResponse.getAccount(),mResponse.getLoginSession(),d.getDeviceID());
                	GetDevVerRes res = mSoapManager.getGetDevVerRes(getDevVerReq);
                	Log.e("GetDevVerRes", res.toString());
                	if(d.isOnLine() && !res.getCurDevVer().equals(res.getNewDevVer())){
                		updateNum++;
                		d.setHasUpdate(true);
                		if(updateNum == 1){
                        	//badge.setText(String.valueOf(updateNum));
                    		handler.sendEmptyMessage(TOGGLEON);
                        }
                		//return;
                	}
                }
        		}catch(Exception e){
                	System.out.println("getDevVerReq crash");
                }
//                if(updateNum > 0){
//                	//badge.setText(String.valueOf(updateNum));
//            		handler.sendEmptyMessage(TOGGLEON);
//                }
        		
        	}
        }.start();
        

    }
    
    
//    public static void setUpdateNum(int num){
//    	updateNum = num;
//    }
//    
//    public static int getUpdateNum(){
//    	return updateNum ;
//    }
    
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO Auto-generated method stub
        switch (checkedId) {
        case R.id.camera_list:
            mHost.setCurrentTabByTag("cameralist");
            break;
        case R.id.settings:
            mHost.setCurrentTabByTag("settings");
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
    	if(updateNum == 0){
    		if(!hasToggled){
    			System.out.println("toggle");
    			handler.sendEmptyMessage(TOGGLEOFF);
    			hasToggled = true;
    		}
    	}
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
    	mActivities.getmActivityList().remove(CamTabActivity.this);
    	unregisterReceiver(receiver);
    }
}
