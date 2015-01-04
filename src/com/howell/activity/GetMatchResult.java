package com.howell.activity;

import com.android.howell.webcam.R;
import com.howell.broadcastreceiver.HomeKeyEventBroadCastReceiver;
import com.howell.protocol.GetDeviceMatchingResultReq;
import com.howell.protocol.GetDeviceMatchingResultRes;
import com.howell.protocol.SoapManager;
import com.howell.protocol.UpdateChannelNameReq;
import com.howell.protocol.UpdateChannelNameRes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;


public class GetMatchResult extends Activity implements OnClickListener{
	private ProgressBar mSeekBar;
	private SoapManager mSoapManager;
	private TimerTask task;
	private GetResultTask getResultTask;
	private TextView mTips;
	private ImageButton mBack;
	
	private String device_name;
	
	private Activities mActivities;
	private HomeKeyEventBroadCastReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_match_result);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Intent intent = getIntent();
		device_name = intent.getStringExtra("device_name");
		if(device_name.equals("")){
			device_name = "我的e看";
		}
		mActivities = Activities.getInstance();
        mActivities.addActivity("GetMatchResult",GetMatchResult.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		//等待时间 60秒
		int progress = 60;
		mSeekBar = (ProgressBar)findViewById(R.id.sb_get_match_result);
		mSeekBar.setMax(progress);
		
		mBack = (ImageButton)findViewById(R.id.ib_get_match_result_back);
		mBack.setOnClickListener(this);
		
		mTips = (TextView)findViewById(R.id.tv_get_match_result_tip);
		mSoapManager = SoapManager.getInstance();
		task = new TimerTask(progress);
		task.execute();
		getResultTask = new GetResultTask(progress);
		getResultTask.execute();
		
	}
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.removeActivity("GetMatchResult");
    	unregisterReceiver(receiver);
    }
	
    //计时器 progressbar每秒前进一格
	class TimerTask extends AsyncTask<Void, Integer, Void> {
		private int progress;
		private int nowProgress;
		public TimerTask(int progress) {
			// TODO Auto-generated constructor stub
			this.progress = progress;
			this.nowProgress = 0;
		}
        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
        	System.out.println("TimerTask doInBackground");
            while(nowProgress <= progress){
            	if (isCancelled()) break;

            	try {
            		System.out.println("TimerTask progress:"+progress);
            		mSeekBar.setProgress(nowProgress);
            		
					Thread.sleep(1000);
					nowProgress ++;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
        	// TODO Auto-generated method stub
        	super.onPostExecute(result);
        	System.out.println("TimerTask onPostExecute");
        	System.out.println("OVER :"+task.getStatus());
        	if(getResultTask != null)
        		getResultTask.cancel(true);
        	mSeekBar.setVisibility(View.GONE);
        	mTips.setText(getResources().getString(R.string.match_activity_fail_tips));
        	Dialog alertDialog = new AlertDialog.Builder(GetMatchResult.this).   
		            setTitle(getResources().getString(R.string.match_activity_fail_dialog_title)).   
		            setMessage(getResources().getString(R.string.match_activity_fail_dialog_message)).   
		            setIcon(R.drawable.expander_ic_minimized).   
		            setPositiveButton(getResources().getString(R.string.match_activity_fail_dialog_yes_btn), new DialogInterface.OnClickListener() {   

		                @Override   
		                public void onClick(DialogInterface dialog, int which) {   
		                    // TODO Auto-generated method stub    
		                	finish();
		                	if(mActivities.getmActivityList().containsKey("FlashLighting")){
								mActivities.getmActivityList().get("FlashLighting").finish();
							}
		                	if(mActivities.getmActivityList().containsKey("SetDeviceWifi")){
								mActivities.getmActivityList().get("SetDeviceWifi").finish();
							}
		                	if(mActivities.getmActivityList().containsKey("SendWifi")){
								mActivities.getmActivityList().get("SendWifi").finish();
							}
		                }   
		            }).   
		    create();   
			alertDialog.show(); 
        }
    }
	
	//获取匹配摄像机结果
	class GetResultTask extends AsyncTask<Void, Integer, Void> {
		private GetDeviceMatchingResultRes getDeviceMatchingResultRes ;
		private UpdateChannelNameRes updateChannelNameRes ;
		private int progress;
		
		public GetResultTask(int progress) {
			super();
			this.progress = progress;
		}

		private void queryResult(){
			GetDeviceMatchingResultReq req = new GetDeviceMatchingResultReq(mSoapManager.getLoginResponse().getAccount(),mSoapManager.getLoginResponse().getLoginSession(),mSoapManager.getmGetDeviceMatchingCodeRes().getMatchingCode());
			getDeviceMatchingResultRes = mSoapManager.getGetDeviceMatchingResultRes(req);
            System.out.println("GetResult:"+getDeviceMatchingResultRes.getResult());
		}
		
		private void chanegName(){
			UpdateChannelNameReq req = new UpdateChannelNameReq(mSoapManager.getLoginResponse().getAccount(),mSoapManager.getLoginResponse().getLoginSession(),getDeviceMatchingResultRes.getDevID(),0,device_name);
			updateChannelNameRes = mSoapManager.getUpdateChannelNameRes(req);
            System.out.println("UpdateChannelName Result:"+updateChannelNameRes.getResult());
		}
		
        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
        	System.out.println("GetResultTask doinbackground");
        	queryResult();
        	while(!getDeviceMatchingResultRes.getResult().equals("OK")){
        		if (isCancelled()) break;
        		try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		queryResult();
        	}
        	chanegName();
//        	while(true){
//        	try {
//        		if (isCancelled()) break;
//        		System.out.println("GetResultTask");
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        	}
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
        	// TODO Auto-generated method stub
        	super.onPostExecute(result);
        	System.out.println("GetResultTask onPostExecute");
        	System.out.println(getDeviceMatchingResultRes.getResult());
        	if(task != null)
        		task.cancel(true);
        	System.out.println("GetResultTask progress:"+progress);
        	mSeekBar.setProgress(progress);
        	Dialog alertDialog = new AlertDialog.Builder(GetMatchResult.this).   
		            setTitle(getResources().getString(R.string.match_activity_success_dialog_title)).   
		            setMessage(getResources().getString(R.string.match_activity_success_dialog_message)).   
		            setIcon(R.drawable.expander_ic_minimized).   
		            setPositiveButton(getResources().getString(R.string.match_activity_success_dialog_yes_btn), new DialogInterface.OnClickListener() {   

		                @Override   
		                public void onClick(DialogInterface dialog, int which) {   
		                    // TODO Auto-generated method stub    
		                	finish();
		                	if(mActivities.getmActivityList().containsKey("SendWifi")){
								mActivities.getmActivityList().get("SendWifi").finish();
							}
		                	if(mActivities.getmActivityList().containsKey("FlashLighting")){
								mActivities.getmActivityList().get("FlashLighting").finish();
							}
		                	if(mActivities.getmActivityList().containsKey("SetDeviceWifi")){
								mActivities.getmActivityList().get("SetDeviceWifi").finish();
							}
		                	if(mActivities.getmActivityList().containsKey("CamTabActivity")){
		                		mActivities.getmActivityList().get("CamTabActivity").finish();
							}
		                	Intent intent = new Intent(GetMatchResult.this,CamTabActivity.class);
		                	startActivity(intent);
		                }   
		            }).   
		    create();   
			alertDialog.show(); 
        	//mSeekBar.setVisibility(View.GONE);
        	//Intent intent = new Intent(GetMatchResult.this,ChangeDeviceName.class);
        	//intent.putExtra("devid", getDeviceMatchingResultRes.getDevID());
        	//startActivity(intent);
        	//mTips.setText("添加成功");
        }
    }

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if(getResultTask != null && !getResultTask.getStatus().equals("FINISHED")){
				getResultTask.cancel(true);
			}
			if(task != null && !task.getStatus().equals("FINISHED")){
				task.cancel(true);
			}
			finish();
        }
        return false;
    }
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ib_get_match_result_back:
			if(getResultTask != null && !getResultTask.getStatus().equals("FINISHED")){
				getResultTask.cancel(true);
			}
			if(task != null && !task.getStatus().equals("FINISHED")){
				task.cancel(true);
			}
			finish();
			break;

		default:
			break;
		}
	}

}
