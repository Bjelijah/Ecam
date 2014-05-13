package com.howell.webcam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.howell.webcam.R;

public class GetMatchResult extends Activity implements OnClickListener{
	private ProgressBar mSeekBar;
	private SoapManager mSoapManager;
	private TimerTask task;
	private GetResultTask getResultTask;
	private TextView mTips;
	private ImageButton mBack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_match_result);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
            while(nowProgress <= progress){
            	if (isCancelled()) break;

            	try {
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
        	System.out.println("OVER :"+task.getStatus());
        	if(getResultTask != null)
        		getResultTask.cancel(true);
        	mSeekBar.setVisibility(View.GONE);
        	mTips.setText("添加失败，网络不稳定，请检查网络");
        	Dialog alertDialog = new AlertDialog.Builder(GetMatchResult.this).   
		            setTitle("错误").   
		            setMessage("添加失败，网络不稳定，请检查网络").   
		            setIcon(R.drawable.expander_ic_minimized).   
		            setPositiveButton("确定", new DialogInterface.OnClickListener() {   

		                @Override   
		                public void onClick(DialogInterface dialog, int which) {   
		                    // TODO Auto-generated method stub    
		                	
		                }   
		            }).   
		    create();   
			alertDialog.show(); 
        }
    }
	
	class GetResultTask extends AsyncTask<Void, Integer, Void> {
		private GetDeviceMatchingResultRes res ;
		private int progress;
		
		public GetResultTask(int progress) {
			super();
			this.progress = progress;
		}

		private void queryResult(){
			GetDeviceMatchingResultReq req = new GetDeviceMatchingResultReq(mSoapManager.getLoginResponse().getAccount(),mSoapManager.getLoginResponse().getLoginSession(),mSoapManager.getmGetDeviceMatchingCodeRes().getMatchingCode());
            res = mSoapManager.getGetDeviceMatchingResultRes(req);
            System.out.println("GetResult:"+res.getResult());
		}
		
        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
        	queryResult();
        	while(res.getResult() != "OK"){
        		if (isCancelled()) break;
        		try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		queryResult();
        	}
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
        	System.out.println(res.getResult());
        	if(task != null)
        		task.cancel(true);
        	mSeekBar.setProgress(progress);
        	mSeekBar.setVisibility(View.GONE);
        	mTips.setText("添加成功");
        }
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
