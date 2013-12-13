package com.howell.webcam.player;


import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Timer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.howell.webcam.R;
import com.howell.invite.Client;
import com.howell.webcam.Device;
import com.howell.webcam.FileUtils;
import com.howell.webcam.LoginResponse;
import com.howell.webcam.MessageUtiles;
import com.howell.webcam.NodeDetails;
import com.howell.webcam.PhoneConfig;
import com.howell.webcam.PtzControlReq;
import com.howell.webcam.PtzControlRes;
import com.howell.webcam.SoapManager;
import com.howell.webcam.VODRecord;
import com.howell.webcam.VideoList;

public class PlayerActivity extends Activity implements Callback, OnTouchListener, OnGestureListener {
	
	public static Client client;
	private static PlayerActivity mPlayer;
	private Thread inviteThread;
	private static boolean playback=false;
	private Device dev;
	private boolean mPausing=false;
	private GLSurfaceView mGlView;
	private VODRecord mRecord;
	private AudioTrack mAudioTrack;
	private byte[] mAudioData;
	private int mAudioDataLength;
	private static int backCount;
	private static long startTime,endTime;
	private boolean isAudioOpen;
	private boolean isShowSurfaceIcon;
	public static boolean stopSendMessage;
	private static long firstFrameTime,endFrameTime;
    private static int frameFlag;
    private static int streamLenFlag;
    private static int streamLen;
    
//    private static Timer timer;
	
	private LinearLayout mSurfaceIcon;
    private static MySeekBar mReplaySeekBar;
    private static ProgressBar mWaitProgressBar;
    private static PlayerHandler mPlayerHandler;
    private static ImageButton mVedioList;
	private ImageButton mSound;
	private ImageButton mCatchPicture;
    private static TextView mStreamLen;
    private ImageButton mPause;
	
	public static final Integer REPLAYSEEKBAR = 0x0001;
	public static final Integer STOPPROGRESSBAR = 0x0002;
	public static final Integer SHOWPROGRESSBAR = 0x0003;
	public static final Integer HIDEPROGRESSBAR = 0x0004;
	public static final Integer TIMEOUT = 0x0005;
	public static final Integer POSTERROR = 0x0006;
	public static final Integer SHOWSTREAMLEN = 0x0007;
	public static final Integer SETVEDIOLISTENABLE = 0x0008;
	public static final Integer SHOW_NO_STREAM_ARRIVE_PROGRESS = 0x0009;
	public static final Integer HIDE_HAS_STREAM_ARRIVE_PROGRESS = 0x0010;
	public static final Integer DETECT_IF_NO_STREAM_ARRIVE = 0x0011;
	
	private SoapManager mSoapManger;
	private String account,loginSession,devID;
	private int channelNo;
	private MyFlingTask mFlingTask;
	private GestureDetector mGestureDetector;
	
	private Animation translateAnimation;
	private ImageView animationAim,animationBackground;
//	private FrameLayout mAnimationLayout;
	
//	private MyInviteTask task;
	private boolean inviteRet;
	private NodeDetails nodeDetail;
	
//	public static boolean isQuit;
	private static int nowFrames;
	private static int lastSecondFrames;
	
	private static Timer mTimer;
	private static boolean progressHasStop;
	
	public  AudioManager audiomanage;  
	private int maxVolume ;  
	
	private static long correctedStartTime;
	private static long correctedEndTime;
	
	boolean bPause ;
//	private FrameLayout mLayout;
	public PlayerActivity() {   
        mGestureDetector = new GestureDetector(this);   
    } 
	
	static {
        System.loadLibrary("hwplay");
        System.loadLibrary("player_jni");
    }
	
	public native void nativeAudioInit();
	public static native void nativeAudioStop();
//	public native void nativeAudioDeinit();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Log.e("main","activity on create");
//		mGlView = new GLSurfaceView(this);
		setContentView(R.layout.glsurface);
		mGlView = (GLSurfaceView)findViewById(R.id.glsurface_view);
		System.out.println("mGlView:"+mGlView.toString());
		mGlView.setEGLContextClientVersion(2);
		mGlView.setRenderer(new YV12Renderer(this,mGlView));
		mGlView.getHolder().addCallback((Callback) this);
		mGlView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
//		mLayout = (FrameLayout)findViewById(R.id.glsurface);
		mGlView.setOnTouchListener(this);   
		mGlView.setFocusable(true);   
		mGlView.setClickable(true);   
		mGlView.setLongClickable(true);   
        mGestureDetector.setIsLongpressEnabled(true);  
        
        Intent intent = getIntent();
		if (intent.getSerializableExtra("arg") instanceof Device) {
            dev = (Device) intent.getSerializableExtra("arg");
//           client = new Client(dev);
            playback = false;
		} else if (intent.getSerializableExtra("arg") instanceof VODRecord) {
            mRecord = (VODRecord) intent.getSerializableExtra("arg");
            dev = mRecord.getDevice();
//            client = new Client(dev);
            playback = true;
        }
		
        mSoapManger = SoapManager.getInstance();
        LoginResponse res = mSoapManger.getLoginResponse();
        account = res.getAccount();
        loginSession = res.getLoginSession();
        devID = dev.getDeviceID();
        channelNo =	dev.getChannelNo();
        
		backCount = 0;
		isAudioOpen = true;
		frameFlag = 0;
		stopSendMessage = false;
		YV12Renderer.time = 0;
		mPlayer = this;
		streamLenFlag = 0;
		streamLen = 0;
		isShowSurfaceIcon = true;
		client = null;
		nowFrames = 0;
		lastSecondFrames = 0;
		progressHasStop = false;
		bPause = true;
		correctedStartTime = -1;
		correctedEndTime = -1;
		
		//获取配置文件声音图标信息
		SharedPreferences sharedPreferences = getSharedPreferences("set",
                Context.MODE_PRIVATE);
        boolean soundMode = sharedPreferences.getBoolean("sound_mode", true);
        System.out.println("soundMode:"+soundMode);
		if (mRecord != null) {
            try {
                SimpleDateFormat foo = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss");
                foo.setTimeZone(TimeZone.getTimeZone("UTC"));
                startTime = foo.parse(mRecord.getStartTime()).getTime()/1000;
                endTime = foo.parse(mRecord.getEndTime()).getTime()/1000;
//                mSurface.setTime(startTime / 1000, endTime / 1000);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
		
		mVedioList = (ImageButton) findViewById(R.id.vedio_list);
		//判断设备有无SD卡
		ArrayList<NodeDetails> node = mSoapManger.getNodeDetails();
		if(node != null){
			for(int i = 0 ; i < node.size() ; i++ ){
				if(node.get(i).getDevID().equals(dev.getDeviceID())){
					nodeDetail = node.get(i);
					if(nodeDetail.iseStoreFlag()){
						mVedioList.setEnabled(true);
						mVedioList.setImageResource(R.drawable.vedio_list);
					}else{
						mVedioList.setEnabled(false);
						mVedioList.setImageResource(R.drawable.vedio_list_enable_false);
					}
					break;
				}
			}
		}
	    mVedioList.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            // TODO Auto-generated method stub
	        	//isQuit = true;
	        	if(null != client)
	        		client.setQuit(true);
	        	quitDisplay();
	            Log.e("", "00000000");
	            Intent intent = new Intent(PlayerActivity.this, VideoList.class);
	            intent.putExtra("Device", dev);
	            startActivity(intent);
	        }
	    });
	        
	    mCatchPicture = (ImageButton)findViewById(R.id.catch_picture);
	    mCatchPicture.setOnClickListener(new OnClickListener() {
				
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!existSDCard()){
					MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.no_sdcard),2000);
					return;
				}
				File destDir = new File("/sdcard/eCamera");
				if (!destDir.exists()) {
					destDir.mkdirs();
				}
				String path = "/sdcard/eCamera/"+FileUtils.getFileName()+".jpg";
				YV12Renderer.setCatchPictureFlag(path,path.length());
				MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.save_picture),2000);
			}
	    });
	    System.out.println("audio init");
	    audioInit();
	    audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE); 
	    maxVolume = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  //获取系统最大音量  
	    System.out.println("maxVolume:"+maxVolume);
//	    int currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值  
	    mSound = (ImageButton)findViewById(R.id.sound);
	    if(soundMode){
	    	System.out.println("soundMode:"+soundMode);
	    	isAudioOpen = true;
			mSound.setImageDrawable(getResources().getDrawable(R.drawable.sound));
	    }
        else {
        	System.out.println("soundMode:"+soundMode);
        	audioPause();
        }
	    mSound.setOnClickListener(new OnClickListener() {
				
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.e("sdl--->", "mSound.setOnClickListener");
				if(isAudioOpen){
					audioPause();
					
				}else {
					audioPlay();
//					mSound.setImageDrawable(getResources().getDrawable(R.drawable.sound));
				}
				//存储声音图标信息
				SharedPreferences sharedPreferences = getSharedPreferences(
		                "set", Context.MODE_PRIVATE);
		        Editor editor = sharedPreferences.edit();
		        editor.putBoolean("sound_mode", isAudioOpen);
		        editor.commit();
					
			}
	    });
	    
	    mPause = (ImageButton)findViewById(R.id.ib_pause);
	    mPause.setOnClickListener(new OnClickListener() {
	    	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(playback){
					if(bPause){
						client.playbackPause(client.getHandle(), true);
						bPause = false;
						mPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
					}
					else{
						client.playbackPause(client.getHandle(), false);
						bPause = true;
						mPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
					}
				}
			}
		});
	    
        mReplaySeekBar = (MySeekBar)findViewById(R.id.replaySeekBar);
        //mReplaySeekBar.setVisibility(View.GONE);
        //设置预览 回放 不同界面
        if(playback){
			mReplaySeekBar.setVisibility(View.VISIBLE);
			mPause.setVisibility(View.VISIBLE);
			mVedioList.setVisibility(View.GONE);
		}else{
			mReplaySeekBar.setVisibility(View.GONE);
			mPause.setVisibility(View.GONE);
			mVedioList.setVisibility(View.VISIBLE);
		}
        mSurfaceIcon = (LinearLayout)findViewById(R.id.surface_icons);
        System.out.println("activity start progress Bar");
        mWaitProgressBar = (ProgressBar)findViewById(R.id.waitProgressBar);
        mPlayerHandler = new PlayerHandler();
        if(playback){
        	Log.e("----------->>>", "onS totoal time:"+endTime +","+ startTime);
        	//mReplaySeekBar.setMax((int)(endTime - startTime)*1000);
//        	System.out.println("setMax:"+(int)(endTime - startTime)*1000);
        	//Log.e("---------->>>>", "setMax:"+(int)(endTime - startTime)*1000);
        	//mReplaySeekBar.setProgress(0);
        	mVedioList.setEnabled(false);
        	Log.e("---------->>>>", "frames send message");
        	mPlayerHandler.sendEmptyMessage(REPLAYSEEKBAR);
        }
        Log.e("----------->>>", "send stopprogress message!!!!!!!!!!");
        mPlayerHandler.sendEmptyMessage(STOPPROGRESSBAR);
        
        mReplaySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				int progress = mReplaySeekBar.getProgress();
//				Log.e("----------->>>", "onStopTrackingTouch progress:"+progress);
//				Log.e("---------->>>>", "onS startTime:"+startTime+"onS progress:"+progress+"onS endTime:"+endTime);
				long replayStartTime = correctedStartTime + (long)progress/1000;
				if(replayStartTime < startTime){
					replayStartTime = startTime;
				}
				Log.e("---------->>>>", "onS startTime:"+replayStartTime+"onS endTime:"+endTime);
				client.Replay(replayStartTime, endTime);
				Log.e("---------->>>>", "replay end");
				stopSendMessage = false;
				progressHasStop = false;
				mPlayerHandler.sendEmptyMessage(REPLAYSEEKBAR);
				//设置暂停键
				client.playbackPause(client.getHandle(), false);
				bPause = true;
				mPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				int progress = mReplaySeekBar.getProgress();
				Log.e("----------->>>", "onStartTrackingTouch progress:"+progress);
				mReplaySeekBar.setSeekBarText(translateTime(progress));
				mPlayerHandler.sendEmptyMessage(SHOWPROGRESSBAR);
				stopSendMessage = true;
//				PlayerActivity.nativeAudioStop();
//	            YV12Renderer.nativeThreadStop();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if(fromUser){
					mReplaySeekBar.setSeekBarText(translateTime(progress));
				}
			}
		});
 
        mStreamLen = (TextView)findViewById(R.id.tv_stream_len);
        animationAim = (ImageView)findViewById(R.id.animation_aim);
        animationBackground = (ImageView)findViewById(R.id.animation_back);
//        mAnimationLayout = (FrameLayout)findViewById(R.id.animation_layout);
        
		InviteThread thread = new InviteThread();
		thread.start();
//		task = new MyInviteTask();
//		task.execute();
	}
	
	//获取SD卡总容量
    public long getSDAllSize(){  
        //取得SD卡文件路径  
        File path = Environment.getExternalStorageDirectory();   
        StatFs sf = new StatFs(path.getPath());   
        //获取单个数据块的大小(Byte)  
        long blockSize = sf.getBlockSize();   
        //获取所有数据块数  
        long allBlocks = sf.getBlockCount();  
        //返回SD卡大小  
        //return allBlocks * blockSize; //单位Byte  
        //return (allBlocks * blockSize)/1024; //单位KB  
        return (allBlocks * blockSize)/1024/1024; //单位MB  
    }    
    
    //获取SD卡剩余容量
    public long getSDFreeSize(){  
        //取得SD卡文件路径  
        File path = Environment.getExternalStorageDirectory();   
        StatFs sf = new StatFs(path.getPath());   
        //获取单个数据块的大小(Byte)  
        long blockSize = sf.getBlockSize();   
        //空闲的数据块的数量  
        long freeBlocks = sf.getAvailableBlocks();  
        //返回SD卡空闲大小  
        //return freeBlocks * blockSize;  //单位Byte  
        //return (freeBlocks * blockSize)/1024;   //单位KB  
        return (freeBlocks * blockSize)/1024 /1024; //单位MB  
    }      
    
    //是否存在SD卡
    private boolean existSDCard() {  
    	if (android.os.Environment.getExternalStorageState().equals(  
    		android.os.Environment.MEDIA_MOUNTED)) {  
        	return true;  
        } else  
        	return false;  
    }  
	
	private String translateTime(int progress){
		SimpleDateFormat foo = new SimpleDateFormat("HH:mm:ss");
		foo.setTimeZone(TimeZone.getDefault());
        String text = foo.format(correctedStartTime*1000 + progress);
        return text;
	}
	
    public static Context getContext() {
        return mPlayer;
    }
    
    public static PlayerHandler getHandler(){
    	return mPlayerHandler;
    }
    
    private int getFrames(){
    	return nowFrames;
    }
    
    public static void addFrames(){
    	nowFrames += 1;
    } 

	private void audioInit() {
		// TODO Auto-generated method stub
		int buffer_size = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size*8, AudioTrack.MODE_STREAM);
		mAudioData = new byte[buffer_size*8];
		
		nativeAudioInit();
		
		//Log.d("play","audio buffer size"+buffer_size);
		mAudioTrack.play();
	}
	
	private void audioPause(){
//		mAudioTrack.flush();
//		mAudioTrack.pause();
		audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, 0 , 0);
		isAudioOpen = false;
		mSound.setImageDrawable(getResources().getDrawable(R.drawable.no_sound));
	}
	
	private void audioPlay(){
		//mAudioTrack.play();
		audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume/2 , 0);
		isAudioOpen = true;
		mSound.setImageDrawable(getResources().getDrawable(R.drawable.sound));
	}
	
	private void audioStop(){
		mAudioTrack.flush();
		mAudioTrack.stop();
	}
	
	private void audioRelease(){
		System.out.println(mAudioTrack.toString());
		mAudioTrack.release();
	}
	
	class InviteThread extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			client = new Client(dev);
			System.out.println("start invite live");
			if (playback) {
				Log.e("---------->>>>", "1111111111111111111");
				System.out.println("startTime:"+startTime+"endTime:"+endTime);
				inviteRet = PlayerActivity.client.InvitePlayback(startTime, endTime);
		    } else {
		        Log.e("---------->>>>", "2222222222222222222");
		        inviteRet = PlayerActivity.client.InviteLive(1);
		    }
			System.out.println("finish invite live");
//			System.out.println("estoreflag:"+client.getQueryDeviceRes().iseStoreFlag());
//			if(client.getQueryDeviceRes().iseStoreFlag()) mPlayerHandler.sendEmptyMessage(SETVEDIOLISTENABLE);
		}
	}
	
	public static void showStreamLen(int streamLen){
		Message msg = new Message();
		msg.what = SHOWSTREAMLEN;
		msg.obj = streamLen;
		mPlayerHandler.sendMessage(msg);
	}
	
    public static class PlayerHandler extends Handler{
    	
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == REPLAYSEEKBAR) {
				//-------------------------------------------------
				if(stopSendMessage){
					return;
				}
////				mPlayerHandler.sendEmptyMessage(BITSCHANGE);
//				Log.e("----------->>>", "native time:"+YV12Renderer.time);
				if(YV12Renderer.time != 0 && frameFlag == 0){
					firstFrameTime = YV12Renderer.time;
					frameFlag++;
					
					while(true){
						correctedStartTime = client.getBeg();
						correctedEndTime = client.getEnd();
						Log.e("----------->>>", "onS totoal time:"+correctedEndTime +","+ correctedStartTime);
						Log.e("----------->>>", "onS totoal time:"+(correctedEndTime - correctedStartTime));
						if(correctedStartTime != -1 && correctedEndTime != -1)
							break;
					}
					mReplaySeekBar.setMax((int)(correctedEndTime - correctedStartTime)*1000);
				}else if(YV12Renderer.time != 0 && frameFlag > 0){
					
					endFrameTime = YV12Renderer.time;
					//Log.e("----------->>>", "handler msg.arg1 :"+time);
//					Log.e("----------->>>", "firstFtame time:"+firstFrameTime+",endFrame time:"+endFrameTime);
//					Log.e("----------->>>", "handler setProgress :"+(endFrameTime - firstFrameTime));
					
					if(!progressHasStop){
						mPlayerHandler.sendEmptyMessage(HIDEPROGRESSBAR);
						progressHasStop = true;
					}
					mReplaySeekBar.setProgress((int)(endFrameTime - firstFrameTime));
				}
				mPlayerHandler.sendEmptyMessageDelayed(REPLAYSEEKBAR,100);
			}
			if (msg.what == STOPPROGRESSBAR) {
				System.out.println("frames: "+stopSendMessage);
				if(stopSendMessage){
					return;
				}
//				Log.e("----------->>>", "STOPPROGRESSBAR native time:"+YV12Renderer.time);
				if(YV12Renderer.time == 0){
					mPlayerHandler.sendEmptyMessageDelayed(STOPPROGRESSBAR,100);
				}else{
					mWaitProgressBar.setVisibility(View.GONE);
					System.out.println("frames: send message DETECT_IF_NO_STREAM_ARRIVE");
					mPlayerHandler.sendEmptyMessage(DETECT_IF_NO_STREAM_ARRIVE);
				}
			}
			if(msg.what == SHOWPROGRESSBAR){
				if(!mWaitProgressBar.isShown()){
					System.out.println("frames progress visible");
					mWaitProgressBar.setVisibility(View.VISIBLE);
				}
			}
			if(msg.what == HIDEPROGRESSBAR){
				if(mWaitProgressBar.isShown()){
					System.out.println("frames progress gone");
					mWaitProgressBar.setVisibility(View.GONE);
				}
			}
			if(msg.what == TIMEOUT){
				if(!stopSendMessage && YV12Renderer.time == 0){
					MessageUtiles.postNewUIDialog(PlayerActivity.getContext(), PlayerActivity.getContext().getString(R.string.link_timeout), PlayerActivity.getContext().getString(R.string.ok),0);
				}
			}
			if (msg.what == POSTERROR) {
				MessageUtiles.postNewUIDialog(PlayerActivity.getContext(), PlayerActivity.getContext().getString(R.string.link_error), PlayerActivity.getContext().getString(R.string.ok), 1);
			}
			if (msg.what == SHOWSTREAMLEN) {
				System.out.println("SHOWSTREAMLEN");
				int msg_boj = Integer.valueOf(msg.obj.toString());
				if(mStreamLen != null){
					streamLenFlag++;
					if(streamLenFlag % 10 == 0){
						streamLen += msg_boj;
						mStreamLen.setText(streamLen/2 + " Kbit/s");
						
//						if(streamLen <= 40){
//							mPlayerHandler.sendEmptyMessage(SHOW_NO_STREAM_ARRIVE_PROGRESS);
//						}else{
//							mPlayerHandler.sendEmptyMessage(HIDE_HAS_STREAM_ARRIVE_PROGRESS);
//						}
						
						streamLen = 0;
					}else{
						streamLen += Integer.valueOf(msg.obj.toString());
					}
				}
			}
			if(msg.what == SETVEDIOLISTENABLE){
				mVedioList.setEnabled(true);
				mVedioList.setImageResource(R.drawable.vedio_list);
			}
//			if(msg.what == SHOW_NO_STREAM_ARRIVE_PROGRESS){
//				if(!isQuit){
//					if(!mWaitProgressBar.isShown())
//						mWaitProgressBar.setVisibility(View.VISIBLE);
//				}
//			}
//			if(msg.what == HIDE_HAS_STREAM_ARRIVE_PROGRESS){
//				if(!isQuit){
//					if(mWaitProgressBar.isShown())
//						mWaitProgressBar.setVisibility(View.GONE);
//				}
//			}
			if(msg.what == DETECT_IF_NO_STREAM_ARRIVE){
				if(stopSendMessage){
					return;
				}
				System.out.println("frames:"+client.toString()+","+client.isQuit());
				if(!client.isQuit()){
					System.out.println("nowFrames:"+nowFrames+"lastSecondFrames:"+lastSecondFrames);
					if(nowFrames == lastSecondFrames){
						System.out.println("frames send message show progress");
						mPlayerHandler.sendEmptyMessage(SHOWPROGRESSBAR);
					}else{
						System.out.println("frames send message hide progress");
						mPlayerHandler.sendEmptyMessage(HIDEPROGRESSBAR);
					}
					lastSecondFrames = nowFrames;
					if(nowFrames >= /*2000000000*/1000){
						nowFrames = 0;
						lastSecondFrames = 0;
					}
					mPlayerHandler.sendEmptyMessageDelayed(DETECT_IF_NO_STREAM_ARRIVE, 1000);
				}
			}
		}
	}

	@Override
	 public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.e("main","config change");
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			Log.i("info", "onConfigurationChanged landscape"); // 横屏
			mSurfaceIcon.setVisibility(View.GONE);
			System.out.println("onSingleTapUp:"+mSurfaceIcon.isShown());
			isShowSurfaceIcon = false;
			mStreamLen.setVisibility(View.VISIBLE);
			//isShowSurfaceIcon = true;
		} else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			Log.i("info", "onConfigurationChanged PORTRAIT"); // 竖屏
			mSurfaceIcon.setVisibility(View.VISIBLE);
			isShowSurfaceIcon = true;
		}
	}
	
	@Override
	protected void onPause() {
		Log.e("PA", "onPause");
		quitDisplay();
		mPausing = true;
		this.mGlView.onPause();
		super.onPause();
//		finish();
	}

	@Override
	protected void onDestroy() {
		Log.e("PA", "onDestroy");
		super.onDestroy();
		System.runFinalization();
	}

	@Override
	protected void onResume() {
		Log.e("PA", "onResume");
		mPausing = false;
		mGlView.onResume();
		super.onResume();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//surfaceCreated = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}

	public void audioWrite() {
//		Log.d("audio","audio data len: "+mAudioDataLength);
//		for (int i=0; i<10; i++) {
//			Log.d("audio","data "+i+" is "+mAudioData[i]);
//		}
		mAudioTrack.write(mAudioData,0,mAudioDataLength);
	}
	
	public class MyQuitTask extends AsyncTask<Void, Integer, Void> {
		private Client client;
		public MyQuitTask(Client client){
			this.client = client;
		}
        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            System.out.println("call doInBackground");
            System.out.println("----------------stop1");
            System.out.println("----------------quit1");
			
			System.out.println("----------------quit2");
				
			System.out.println("----------------quit3");
			if(client != null && client.getHandle() != -1){
				System.out.println("isStartFinish:"+client.isStartFinish()+","+client.toString());
            	while(true){
            		if(client.isStartFinish()){
		        		System.out.println("free handle");
						client.freeHandle(client.getHandle());
						break;
            		}
            	}
			}
			System.out.println("release audio");
			audioRelease();
            if(client != null)
	            client.bye(client.getAccount(),client.getLoginSession(),client.getDevID(),client.getChannelNo(),client.getStreamType(),client.getDevID());	
	        System.out.println("finish activity");
            return null;
        }
        @Override  
        protected void onPostExecute(Void result) {  
            super.onPostExecute(result);  
            //if (!isFinishing()) {  
                //try {  
                //} catch (Exception e) {  
                //}  
            //}  
        }  
    }
	
	private void quitDisplay(){
		if (backCount == 0) {
			stopSendMessage = true;
			System.out.println("aaaaaaaaaa");
			while(true){
				if(client != null){
					System.out.println("thread set true:"+client.isQuit());
					client.setQuit(true);
					System.out.println("bbbbbbbb");
	//				client.setQuickQuit(-1);
					client.joinThread(client.getHandle());
					break;
				}
			}
			System.out.println("stop audio");
			audioStop();
			YV12Renderer.nativeDeinit();
			finish();
			MyQuitTask mTask = new MyQuitTask(client);
            mTask.execute();
        }
        System.out.println(backCount);
        backCount++;
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	Log.e("backCount", "backCount:"+backCount);
        	//isQuit = true;
        	if(null != client)
        		client.setQuit(true);
        	quitDisplay();
            //
        }
        return false;
    }

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public class MyFlingTask extends AsyncTask<Void, Integer, Void> {
		private String direction;
		private int time;
		public MyFlingTask(String direction,int time) {
			// TODO Auto-generated constructor stub
			this.direction = direction;
			this.time = time;
		}
        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            System.out.println("call doInBackground");
            Log.e("start direction", direction);
            PtzControlReq req = new PtzControlReq(account,loginSession,devID,channelNo,direction);
        	PtzControlRes ptzRes = mSoapManger.GetPtzControlRes(req);
            Log.e("start Res", ptzRes.getResult());
            //startStopPtzThread();
            try {
            	Thread.sleep(time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            req = new PtzControlReq(account,loginSession,devID,channelNo,"Stop");
            ptzRes = mSoapManger.GetPtzControlRes(req);
            Log.e("stop Res", ptzRes.getResult());
            return null;
        }
    }

	private void animationStart(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta){
		 //初始化 Translate动画  
	   // translateAnimation = new TranslateAnimation(0.1f, 100.0f,0.1f,0.1f);  
		System.out.println("flingAAAAAAAA");
	    translateAnimation = new TranslateAnimation(fromXDelta, toXDelta,fromYDelta,toYDelta);
	    System.out.println("flingBBBBBBBB");
	    translateAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
				System.out.println("Fling00000000");
				animationAim.setVisibility(View.VISIBLE);
				System.out.println("Fling1111111");
				animationBackground.setVisibility(View.VISIBLE);
//				mAnimationLayout.setVisibility(View.VISIBLE);
				System.out.println("Fling2222222");
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
				System.out.println("Fling333333333");
				animationAim.setVisibility(View.GONE);
				System.out.println("Fling44444444");
				animationBackground.setVisibility(View.GONE);
				System.out.println("Fling5555555");
				animationAim.clearAnimation();
//				mAnimationLayout.setVisibility(View.INVISIBLE);
				System.out.println("Fling66666666");
			}
        });
	    System.out.println("flingCCCCCCCC");
	    //translateAnimation.setFillAfter(true);
	    System.out.println("flingDDDDDDDD");
	    //初始化 Alpha动画  
	    //Animation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);  
	      
	    //设置动画时间 (作用到每个动画)  
	    System.out.println("flingEEEEEEE");
	    translateAnimation.setDuration(2000);  
	    System.out.println("flingFFFFFFFF");
		animationAim.startAnimation(translateAnimation);  
		System.out.println("fling"+translateAnimation.willChangeBounds());
		System.out.println("flingGGGGGGGG");
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		 // 参数解释：   
        // e1：第1个ACTION_DOWN MotionEvent   
        // e2：最后一个ACTION_MOVE MotionEvent   
        // velocityX：X轴上的移动速度，像素/秒   
        // velocityY：Y轴上的移动速度，像素/秒   
      
        // 触发条件 ：   
        // X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒   
		
		if(!nodeDetail.isPtzFlag() || playback){
			System.out.println("is not PTZ");
			return false;
		}
		
        String direction = "Stop";
        int time = 0;
        
        if(mFlingTask != null){
        	Log.e("start status", mFlingTask.getStatus().toString());
	        if( mFlingTask.getStatus() != AsyncTask.Status.FINISHED){
	        	Log.e("return", mFlingTask.getStatus().toString());
	        	Log.e("return", "return");
	        	return true;
	        }
        }
        final int FLING_MIN_DISTANCE = 100, FLING_MIN_VELOCITY = 200;   
        if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {   
            // Fling left   
        	direction = "Right";
        	time = 700;
        	System.out.println("fling111111111");
        	animationStart(0.1f, -100.0f,0.1f,0.1f);
        	Log.e("MyGesture", "Fling left "+"x:"+Math.abs(e1.getX() - e2.getX())+"y:"+Math.abs(e1.getY() - e2.getY()));  
        } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {   
            // Fling right   
        	Log.e("MyGesture", "Fling right "+"x:"+Math.abs(e1.getX() - e2.getX())+"y:"+Math.abs(e1.getY() - e2.getY()));   
        	direction = "Left";
        	System.out.println("fling222222222");
        	animationStart(0.1f, 100.0f,0.1f,0.1f);
        	time = 700;
        }  else if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY) {   
            // Fling Down   
        	Log.e("MyGesture", "Fling Down "+"y:"+Math.abs(e1.getY() - e2.getY())+"x:"+Math.abs(e1.getX() - e2.getX()));   
        	direction = "Up";
        	animationStart(0.1f, 0.1f,0.1f,100.0f);
        	time = 500;
        }   else if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY) {   
            // Fling Up   
        	Log.e("MyGesture", "Fling Up "+"y:"+Math.abs(e1.getY() - e2.getY())+"x:"+Math.abs(e1.getX() - e2.getX()));   
        	direction = "Down";
        	time = 500;
        	animationStart(0.1f, 0.1f,0.1f,-100.0f);
        }   else{
        	return true;
        }
        mFlingTask = new MyFlingTask(direction,time);
        mFlingTask.execute();
        Log.e("mFlingTask", mFlingTask.getStatus().toString());
        return true;   
	}
	
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.e("MyGesture", "onSingleTapUp");  
		System.out.println("playback:"+playback);
		//横屏
		if(PhoneConfig.getPhoneHeight(this) < PhoneConfig.getPhoneWidth(this)){
			System.out.println("onSingleTapUp000:"+isShowSurfaceIcon);
			if(isShowSurfaceIcon){
				System.out.println("onSingleTapUp111:"+isShowSurfaceIcon);
				mSurfaceIcon.setVisibility(View.GONE);
				isShowSurfaceIcon = false;
			}else{
				System.out.println("onSingleTapUp222:"+isShowSurfaceIcon);
				mSurfaceIcon.setVisibility(View.VISIBLE);
				System.out.println("onSingleTapUp:"+mSurfaceIcon.isShown());
				isShowSurfaceIcon = true;
			}
		}
		//竖屏
//		else{
//			mSurfaceIcon.setVisibility(View.VISIBLE);
//			isShowSurfaceIcon = true;
//		}
		if(playback){
			mReplaySeekBar.setVisibility(View.VISIBLE);
		}else{
			mReplaySeekBar.setVisibility(View.GONE);
		}
//		if(isShowSurfaceIcon){
//			mSurfaceIcon.setVisibility(View.GONE);
//			isShowSurfaceIcon = false;
//		}else{
//			mSurfaceIcon.setVisibility(View.VISIBLE);
//			isShowSurfaceIcon = true;
//		}
//	    Toast.makeText(this, "onSingleTapUp", Toast.LENGTH_SHORT).show();   
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return mGestureDetector.onTouchEvent(event);   
	}
	
}
