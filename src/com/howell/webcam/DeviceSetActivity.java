package com.howell.webcam;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.howell.webcam.R;

public class DeviceSetActivity extends Activity implements
        OnSeekBarChangeListener {
	
    private TextView mTvDeviceName,mCameraUpdateStatus;
    private static SoapManager mSoapManager;
    private SeekBar mSeekBar_reso, mSeekBar_quality;
    private static LoginResponse mLoginResponse;
    private String[] mFrameSizeValues;
    private CodingParamRes mCodingParamRes;
    private TextView reso_text_,quality_text_;
    private CheckBox vmd_checkbox_,video_checkbox,power_led_checkbox;
    private VMDParamRes vmd_res_;
    private static int backCount;
    public static NodeDetails dev;
    private boolean isCrashed;
    // row is reoslution, col is quality, map to bitrate, unit is kbps
    private static int[][] reso_bitrate_map_ = {{96,128,196},{128,256,384},{1024,1536,2048}};
    private static String[] VMD_DEFAULT_GRIDS = {
    	"00000000000",
    	"00000000000",
    	"00011111000",
    	"00011111000",
    	"00011111000",
    	"00011111000",
    	"00011111000",
    	"00000000000",
    	"00000000000",
    };
    private static String[] VMD_ZERO_GRIDS = {
    	"00000000000",
    	"00000000000",
    	"00000000000",
    	"00000000000",
    	"00000000000",
    	"00000000000",
    	"00000000000",
    	"00000000000",
    	"00000000000",
    };
    
    private Activities mActivities;
    private HomeKeyEventBroadCastReceiver receiver;
    private ProgressDialog pd;
    private Button mUpdateButton;
//    private TextView tv_font;
    private LinearLayout ll_alarm_push;
    private CheckBox cb_alarm_notice;
    private TextView mCameraVersion;
    
    private static final int CRASH = 1;
    private static final int ALARMPUSHOFF = 2;
    private int gainedReso,gainedQuality;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deviceset);
        
        mActivities = Activities.getInstance();
    	mActivities.getmActivityList().add(DeviceSetActivity.this);
    	receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
        backCount = 0;
        gainedReso = -1;
        gainedQuality = -1;
        mSoapManager = SoapManager.getInstance();

        mTvDeviceName = (TextView) findViewById(R.id.tv_device_name);
        mSeekBar_reso = (SeekBar) findViewById(R.id.seekBar1);
        mSeekBar_reso.setMax(2);
        reso_text_ = (TextView) findViewById(R.id.resolutoin_str);
        mSeekBar_quality = (SeekBar) findViewById(R.id.seekBar2);
        mSeekBar_quality.setMax(2);
        quality_text_ = (TextView) findViewById(R.id.quality_str);
        vmd_checkbox_ = (CheckBox)findViewById(R.id.vmd_enable);
        video_checkbox = (CheckBox)findViewById(R.id.turn_over);
        power_led_checkbox = (CheckBox)findViewById(R.id.power_led);
        mCameraUpdateStatus = (TextView)findViewById(R.id.camera_update_status);
        mUpdateButton = (Button)findViewById(R.id.setting_update_button);
        //ll_load = (LinearLayout)findViewById(R.id.ll_load);
        ll_alarm_push = (LinearLayout)findViewById(R.id.ll_alarm_push);
        cb_alarm_notice = (CheckBox)findViewById(R.id.alarm_notice);
        mCameraVersion = (TextView)findViewById(R.id.tv_camera_version);
//        tv_font = (TextView)findViewById(R.id.tv_font);
        
        mSeekBar_reso.setOnSeekBarChangeListener(this);
        mSeekBar_quality.setOnSeekBarChangeListener(this);

        Intent intent = getIntent();
        dev = (NodeDetails) intent.getSerializableExtra("Device");
        mTvDeviceName.setText(dev.getName());

        mLoginResponse = mSoapManager.getLoginResponse();
        
//        Typeface fontFace = Typeface.createFromAsset(getAssets(),
//                "fonts/new_font.ttf");
//        
//        tv_font.setTypeface(fontFace);
        mUpdateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!dev.isHasUpdate())return;
				AlerDialogUtils.postDialog(DeviceSetActivity.this);
			}
		});
        
        //图像翻转
        video_checkbox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				pd = new ProgressDialog(DeviceSetActivity.this);  
		        pd.setTitle(getResources().getString(R.string.save_set)+"...");   //设置标题  
		        pd.setMessage(getResources().getString(R.string.please_wait)+"..."); //设置body信息  
		        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER); //设置进度条样式是 横向的 
				pd.show();
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try{
							saveVideoParam();
						}catch (Exception e) {
							// TODO: handle exception
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						try{
							pd.dismiss();
						}catch (Exception e) {
							// TODO: handle exception
						}
					}
				}.execute();
			}
		});
        
        //电源指示灯
        power_led_checkbox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				pd = new ProgressDialog(DeviceSetActivity.this);  
		        pd.setTitle(getResources().getString(R.string.save_set)+"...");   //设置标题  
		        pd.setMessage(getResources().getString(R.string.please_wait)+"..."); //设置body信息  
		        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER); //设置进度条样式是 横向的 
				pd.show();
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try{
							savePowerLedParam();
						}catch (Exception e) {
							// TODO: handle exception
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						try{
							pd.dismiss();
						}catch (Exception e) {
							// TODO: handle exception
						}
					}
				}.execute();
			}
		});
        
        //移动侦测
        vmd_checkbox_.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//ll_load.setVisibility(View.VISIBLE);
				pd = new ProgressDialog(DeviceSetActivity.this);  
		        pd.setTitle(getResources().getString(R.string.save_set)+"...");   //设置标题  
		        pd.setMessage(getResources().getString(R.string.please_wait)+"..."); //设置body信息  
		        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER); //设置进度条样式是 横向的 
				pd.show();
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try{
							saveVMDParam();
						}catch (Exception e) {
							// TODO: handle exception
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						//ll_load.setVisibility(View.GONE);
						pd.dismiss();
						if(vmd_checkbox_.isChecked()){
							ll_alarm_push.setVisibility(View.VISIBLE);
						}else{
							ll_alarm_push.setVisibility(View.GONE);
						}
					}
				}.execute();
			}
		});
        
        //报警推送设置
        cb_alarm_notice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				pd = new ProgressDialog(DeviceSetActivity.this);  
		        pd.setTitle(getResources().getString(R.string.save_set)+"...");   //设置标题  
		        pd.setMessage(getResources().getString(R.string.please_wait)+"..."); //设置body信息  
		        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER); //设置进度条样式是 横向的 
				pd.show();
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try{
							boolean alarmPush = cb_alarm_notice.isChecked();
							saveAlarmPushParam(alarmPush);
						}catch (Exception e) {
							// TODO: handle exception
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						try{
							pd.dismiss();
						}catch (Exception e) {
							// TODO: handle exception
						}
					}
				}.execute();
			}
		});
        
        //获取远程设备设置
        pd = new ProgressDialog(DeviceSetActivity.this);  
        pd.setTitle(getResources().getString(R.string.gain_set)+"...");   //设置标题  
        pd.setMessage(getResources().getString(R.string.please_wait)+"..."); //设置body信息  
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER); //设置进度条样式是 横向的 
		pd.show();
		
		new AsyncTask<Void, Void, Void>() {
			int bitrate = 0;
	        String framesize=null;
	        int rotationDegree = 0;
	        int reso_idx=-1;
	        GetAuxiliaryRes getAuxiliaryRes = null;
	        GetDevVerRes res = null;
	        QueryDeviceRes queryDeviceRes = null;
			protected Void doInBackground(Void... params) {
		        System.out.println("111111111111111");
				 try{
				        if (mLoginResponse.getResult().toString().equals("OK")) {
				            String account = mLoginResponse.getAccount().toString();
				            String loginSession = mLoginResponse.getLoginSession().toString();

				            CodingParamReq req = new CodingParamReq(account, loginSession,
				                    dev.getDevID(), dev.getChannelNo(), "Sub");

				            mCodingParamRes = mSoapManager.getCodingParamRes(req);
				            framesize = mCodingParamRes.getFrameSize();
				            Log.v("dev","frame size is "+framesize);
				            bitrate = Integer.parseInt(mCodingParamRes.getBitRate());
				            Log.v("dev","image qualit is "+bitrate);
				            
				            VMDParamReq vmd_req = new VMDParamReq(account,loginSession,dev.getDevID(),dev.getChannelNo());
				            vmd_res_ = mSoapManager.getVMDParam(vmd_req);
				            Log.v("dev", "vmd enable: "+vmd_res_.getEnabled());
				            //如果移动侦测开启 检测是否推送
				            if(vmd_res_.getEnabled()){
				            	QueryDeviceReq queryDeviceReq = new QueryDeviceReq(mLoginResponse.getAccount(),mLoginResponse.getLoginSession(),dev.getDevID());
				            	queryDeviceRes = new QueryDeviceRes();
				            	queryDeviceRes = mSoapManager.getQueryDeviceRes(queryDeviceReq);
				            	System.out.println("is Push?"+queryDeviceRes.toString());
				            }
				        }
				        
				        System.out.println("2222222222222222");
				        mFrameSizeValues = getResources().getStringArray(R.array.FrameSize);
				        
				        for (int i=0; i<mFrameSizeValues.length; ++i) {
				        	if (mFrameSizeValues[i].equals(framesize)) {
				        		reso_idx = i;
				        		break;
				        	}
				        }
				        //获取电源指示灯
				        GetAuxiliaryReq getAuxiliaryReq = new GetAuxiliaryReq(mLoginResponse.getAccount(),mLoginResponse.getLoginSession(),dev.getDevID(),"SignalLamp");
				        getAuxiliaryRes = mSoapManager.getGetAuxiliaryRes(getAuxiliaryReq);
				        System.out.println("getAuxiliaryRes"+getAuxiliaryRes.getResult());
				        
				        System.out.println("333333333333333");
				        //获取视频翻转信息
				        GetVideoParamReq getVideoParamReq = new GetVideoParamReq(mLoginResponse.getAccount(),mLoginResponse.getLoginSession(),dev.getDevID(), dev.getChannelNo());
				    	rotationDegree = mSoapManager.getGetVideoParamRes(getVideoParamReq).getRotationDegree();
				    	System.out.println("rotationDegree"+rotationDegree);
				    	
				    	 System.out.println("4444444444444444444");
				        //获取设备版本信息
				    	GetDevVerReq getDevVerReq = new GetDevVerReq(mLoginResponse.getAccount(),mLoginResponse.getLoginSession(),dev.getDevID());
				    	res = mSoapManager.getGetDevVerRes(getDevVerReq);
				    	Log.e("GetDevVerRes", res.toString());
				    	
				        }catch (Exception e) {
							// TODO: handle exception
				        	System.out.println("crash!!!!!!");
				        	isCrashed = true;
				        	pd.dismiss();
				        	handler.sendEmptyMessage(CRASH);
						}
				 System.out.println("55555555555555");
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				try{
					System.out.println("re11111111111");
					 if (reso_idx>=0) {
						 //保存获取的分辨率值
						 gainedReso = reso_idx;
				        	mSeekBar_reso.setProgress(reso_idx);
				        	refreshResolutionText(reso_idx);
				        	
				        	for (int i=0; i<reso_bitrate_map_[reso_idx].length; ++i) {
				        		if (reso_bitrate_map_[reso_idx][i]>=bitrate) {
				        			//保存获取的画质
				        			gainedQuality = i;
				        			mSeekBar_quality.setProgress(i);
				        			refreshImageQualityText(i);
				        			break;
				        		}
				        	}
				        }
				        else {
				        	//TODO error, not found resolution
				        }
					 
					vmd_checkbox_.setChecked(vmd_res_.getEnabled());
					if(vmd_checkbox_.isChecked()){
						ll_alarm_push.setVisibility(View.VISIBLE);
						if(queryDeviceRes.getAndroidPushSubscribedFlag() == 0){
							cb_alarm_notice.setChecked(false);
						}else if(queryDeviceRes.getAndroidPushSubscribedFlag() == 1){
							cb_alarm_notice.setChecked(true);
						}
					}else{
						ll_alarm_push.setVisibility(View.GONE);
						cb_alarm_notice.setChecked(false);
					}
					
					System.out.println("re222222222222");
					if(getAuxiliaryRes.getResult().equals("OK")){
						if(getAuxiliaryRes.getAuxiliaryState().equals("Inactive")){
							power_led_checkbox.setChecked(false);
						}else if(getAuxiliaryRes.getAuxiliaryState().equals("Active")){
							power_led_checkbox.setChecked(true);
						}
					}
					System.out.println("re333333333333");
					if(rotationDegree == 0){
			    		video_checkbox.setChecked(false);
			    	}else if(rotationDegree == 180){
			    		video_checkbox.setChecked(true);
			    	}
					System.out.println("re4444444444444");
					mCameraVersion.setText(getResources().getString(R.string.camera_version_title)+"(V"+res.getCurDevVer()+")");
					if(res.getCurDevVer().equals(res.getNewDevVer())){
			    		mCameraUpdateStatus.setText(getResources().getString(R.string.camera_old_version1)+res.getCurDevVer()+getResources().getString(R.string.camera_old_version2));
			    		mUpdateButton.setVisibility(View.INVISIBLE);
			    	}else{
			    		mCameraUpdateStatus.setText(getResources().getString(R.string.camera_new_version)+res.getNewDevVer()/*+getResources().getString(R.string.client_new_version)*/);
			    		mUpdateButton.setVisibility(View.VISIBLE);
			    	}
					System.out.println("re5555555555555");
				}catch (Exception e) {
					// TODO: handle exception
					System.out.println("exception");
				}
				pd.dismiss();
			}
		}.execute();

    }
    
    Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		// TODO Auto-generated method stub
    		super.handleMessage(msg);
    		if(msg.what == CRASH){
    			MessageUtiles.postNewUIDialog(DeviceSetActivity.this, "网络信号弱，获取配置失败", "OK", 0);
    		}
    		if(msg.what == ALARMPUSHOFF){
    			cb_alarm_notice.setChecked(false);
    		}
    	}
    };
    
    public static void cameraUpdate(){
    	Log.e("", "cameraUpdate");
    	UpgradeDevVerReq req = new UpgradeDevVerReq(mLoginResponse.getAccount(),mLoginResponse.getLoginSession(),dev.getDevID());
    	UpgradeDevVerRes res = mSoapManager.getUpgradeDevVerRes(req);
    	Log.e("cameraUpdate", res.getResult());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        // TODO Auto-generated method stub
    	if (seekBar == mSeekBar_reso) {
    		refreshResolutionText(progress);
    	}
    	else {
    		refreshImageQualityText(progress);
    	}
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    	/*
        if (seekBar == mSeekBar1) {
            if (mLoginResponse.getResult().toString().equals("OK")) {
                mParamRes.setFrameSize(mFrameSizeValues[seekBar.getProgress()]);
                mSoapManager.setCodingParamFrameSize(mParamRes);
            }
        } else if (seekBar == mSeekBar2) {
            if (mLoginResponse.getResult().toString().equals("OK")) {
                mParamRes
                        .setImageQuality(String.valueOf(seekBar.getProgress()));
                mSoapManager.setCodingParamImageQuality(mParamRes);
            }
        }
        */
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	//MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.save_set),2000);
        	//if (backCount == 0) {
//                MyTask mTask = new MyTask();
//                mTask.execute();
        	if(isCrashed){
        		finish();
        		return false;
        	}
        	int reso_idx = mSeekBar_reso.getProgress();
        	int qual_idx = mSeekBar_quality.getProgress();
        	//如果没设置直接退出
        	if(gainedReso == reso_idx && gainedQuality == qual_idx){
        		finish();
        		return false;
        	}
        		pd = new ProgressDialog(DeviceSetActivity.this);  
		        pd.setTitle(getResources().getString(R.string.save_set)+"...");   //设置标题  
		        pd.setMessage(getResources().getString(R.string.please_wait)+"..."); //设置body信息  
		        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER); //设置进度条样式是 横向的 
				pd.show();
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try{
						saveEncodingParam();
			        	//saveVMDParam();
			        	//saveVideoParam();
						}catch (Exception e) {
							// TODO: handle exception
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						try{
							pd.dismiss();
							finish();
						}catch (Exception e) {
							// TODO: handle exception
						}
					}
				}.execute();
           // }//else{
//            	MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.save_set),2000);
//            }
        	 //backCount++;
        }
        return false;
    }
    
    private void refreshResolutionText(int idx) {
    	String[] s = getResources().getStringArray(R.array.ResolutionText);
    	if (idx<s.length) {
    		reso_text_.setText(s[idx]);
    	}
    }
    
    private void refreshImageQualityText(int idx) {
    	String[] s = getResources().getStringArray(R.array.ImageQualityText);
    	if (idx<s.length) {
    		quality_text_.setText(s[idx]);
    	}
    }

    //保存图像质量设置
    private boolean saveEncodingParam() {
    	int reso_idx = mSeekBar_reso.getProgress();
    	//TODO  hd720p设置的是主码流，先不处理
    	if (reso_idx==2) {
    		return true;
    	}
    	int qual_idx = mSeekBar_quality.getProgress();
    	
    	//如果没设置直接退出
    	if(gainedReso == reso_idx && gainedQuality == qual_idx){
    		return false;
    	}
    	
    	int bitrate = reso_bitrate_map_[reso_idx][qual_idx];
    	Log.v("dev","save bitrate: "+bitrate);
    	String[] s = getResources().getStringArray(R.array.FrameSize);
    	mCodingParamRes.setFrameSize(s[reso_idx]);
    	mCodingParamRes.setBitRate(String.valueOf(bitrate));
    	mSoapManager.setCodingParam(mCodingParamRes);
    	return true;
    }
    
    //保存移动侦测设置
    private boolean saveVMDParam() {
    	boolean use_vmd = vmd_checkbox_.isChecked();
    	vmd_res_.setEnabled(use_vmd);
    	vmd_res_.setSensitivity(40);
    	if (use_vmd) {
    		VMDGrid grids = new VMDGrid(VMD_DEFAULT_GRIDS);
    		vmd_res_.setGrids(grids);
    	}else{
    		VMDGrid grids = new VMDGrid(VMD_ZERO_GRIDS);
    		vmd_res_.setGrids(grids);
    		//关闭警报推送
			handler.sendEmptyMessage(ALARMPUSHOFF);
			saveAlarmPushParam(false);
    	}
    	mSoapManager.setVMDParam(vmd_res_);
    	return true;
    }
    
    //保存图像翻转设置
    private boolean saveVideoParam(){
    	System.out.println(mLoginResponse.getAccount()+","+mLoginResponse.getLoginSession()+","+dev.getDevID()+","+dev.getChannelNo());
    	boolean isTurnOver = video_checkbox.isChecked();
    	SetVideoParamReq req_set = null;
    	SetVideoParamRes res = null;
    	if(isTurnOver){
    		req_set = new SetVideoParamReq(mLoginResponse.getAccount(),mLoginResponse.getLoginSession(),dev.getDevID(), dev.getChannelNo(),180);
			res = mSoapManager.getSetVideoParamRes(req_set);
    	}else{
    		req_set = new SetVideoParamReq(mLoginResponse.getAccount(),mLoginResponse.getLoginSession(),dev.getDevID(), dev.getChannelNo(),0);
			res = mSoapManager.getSetVideoParamRes(req_set);
    	}
    	System.out.println("turn over:"+res.getResult());
    	return true;
    }
    
    //保存电源指示灯设置
    private boolean savePowerLedParam(){
    	System.out.println(mLoginResponse.getAccount()+","+mLoginResponse.getLoginSession()+","+dev.getDevID()+","+dev.getChannelNo());
    	boolean powerLed = power_led_checkbox.isChecked();
    	SetAuxiliaryReq req_set = null;
    	SetAuxiliaryRes res = null;
    	if(powerLed){
    		req_set = new SetAuxiliaryReq(mLoginResponse.getAccount(),mLoginResponse.getLoginSession(),dev.getDevID(), "SignalLamp","Active");
    		res = mSoapManager.getSetAuxiliaryRes(req_set);
    	}else{
    		req_set = new SetAuxiliaryReq(mLoginResponse.getAccount(),mLoginResponse.getLoginSession(),dev.getDevID(), "SignalLamp","Inactive");
    		res = mSoapManager.getSetAuxiliaryRes(req_set);
    	}
    	System.out.println("power led:"+res.getResult());
    	return true;
    }
    
  //保存警报推送设置
    private boolean saveAlarmPushParam(boolean alarmPush){
    	//boolean alarmPush = cb_alarm_notice.isChecked();
    	SubscribeAndroidPushReq req = null;
    	SubscribeAndroidPushRes res = null;
    	if(alarmPush){
    		req = new SubscribeAndroidPushReq(mLoginResponse.getAccount(),mLoginResponse.getLoginSession(),0x01,dev.getDevID(), dev.getChannelNo());
    		res = mSoapManager.getSubscribeAndroidPushRes(req);
    	}else{
    		req = new SubscribeAndroidPushReq(mLoginResponse.getAccount(),mLoginResponse.getLoginSession(),0x00,dev.getDevID(),dev.getChannelNo());
    		res = mSoapManager.getSubscribeAndroidPushRes(req);
    	}
    	System.out.println("alarm push:"+req.toString());
    	System.out.println("alarm push:"+res.getResult());
    	return true;
    }
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    }
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.getmActivityList().remove(DeviceSetActivity.this);
    	mActivities.toString();
    	unregisterReceiver(receiver);
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
//    	for(Activity a:mActivities.getmActivityList()){
//    		a.finish();
//    	}
    }
}
