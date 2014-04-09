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
import android.util.FloatMath;
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
	private static NodeDetails dev;
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
	
//	public static boolean isQuit;
	private static int nowFrames;
	private static int lastSecondFrames;
	
	private static Timer mTimer;
	private static boolean progressHasStop;
	
	public  AudioManager audiomanage;  
	private int maxVolume ;  
	
	private static long correctedStartTime;
	private static long correctedEndTime;
	private static int stopTrackingTouchProgress;
	
	boolean bPause ;
	boolean isAnimationStart;
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
	//public static native void setCatchPictureFlag(long handle,String path,int length);
	
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
		if (intent.getSerializableExtra("arg") instanceof NodeDetails) {
            dev = (NodeDetails) intent.getSerializableExtra("arg");
//           client = new Client(dev);
            playback = false;
		} else if (intent.getSerializableExtra("arg") instanceof VODRecord) {
            mRecord = (VODRecord) intent.getSerializableExtra("arg");
            dev = (NodeDetails) intent.getSerializableExtra("nodeDetails");
//            client = new Client(dev);
            playback = true;
        }
		
        mSoapManger = SoapManager.getInstance();
        LoginResponse res = mSoapManger.getLoginResponse();
        account = res.getAccount();
        loginSession = res.getLoginSession();
        devID = dev.getDevID();
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
		isAnimationStart = false;
		correctedStartTime = -1;
		correctedEndTime = -1;
		stopTrackingTouchProgress = 0;
		
		//锟斤拷取锟斤拷锟斤拷锟侥硷拷锟斤拷锟斤拷图锟斤拷锟斤拷息
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
		//锟叫讹拷锟借备锟斤拷锟斤拷SD锟斤拷
		if(dev.iseStoreFlag()){
			mVedioList.setEnabled(true);
			mVedioList.setImageResource(R.drawable.vedio_list);
		}else{
			mVedioList.setEnabled(false);
			mVedioList.setImageResource(R.drawable.vedio_list_enable_false);
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
				if(client.setCatchPictureFlag(client.getHandle(),path,path.length()) == 1)
					MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.save_picture),2000);
			}
	    });
	    System.out.println("audio init");
	    audioInit();
	    audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE); 
	    maxVolume = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  //锟斤拷取系统锟斤拷锟斤拷锟斤拷锟� 
	    System.out.println("maxVolume:"+maxVolume);
//	    int currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC);  //锟斤拷取锟斤拷前值  
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
				//锟芥储锟斤拷锟斤拷图锟斤拷锟斤拷息
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
        //锟斤拷锟斤拷预锟斤拷 锟截凤拷 锟斤拷同锟斤拷锟斤拷
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
				Log.e("----------->>>", "onStopTrackingTouch progress:"+progress);
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
				stopTrackingTouchProgress = progress;
				mPlayerHandler.sendEmptyMessage(REPLAYSEEKBAR);
				//锟斤拷锟斤拷锟斤拷停锟斤拷
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
	
	//锟斤拷取SD锟斤拷锟斤拷锟斤拷锟斤拷
    public long getSDAllSize(){  
        //取锟斤拷SD锟斤拷锟侥硷拷路锟斤拷  
        File path = Environment.getExternalStorageDirectory();   
        StatFs sf = new StatFs(path.getPath());   
        //锟斤拷取锟斤拷锟斤拷锟斤拷菘锟侥达拷小(Byte)  
        long blockSize = sf.getBlockSize();   
        //锟斤拷取锟斤拷锟斤拷锟斤拷菘锟斤拷锟� 
        long allBlocks = sf.getBlockCount();  
        //锟斤拷锟斤拷SD锟斤拷锟斤拷小  
        //return allBlocks * blockSize; //锟斤拷位Byte  
        //return (allBlocks * blockSize)/1024; //锟斤拷位KB  
        return (allBlocks * blockSize)/1024/1024; //锟斤拷位MB  
    }    
    
    //锟斤拷取SD锟斤拷剩锟斤拷锟斤拷锟斤拷
    public long getSDFreeSize(){  
        //取锟斤拷SD锟斤拷锟侥硷拷路锟斤拷  
        File path = Environment.getExternalStorageDirectory();   
        StatFs sf = new StatFs(path.getPath());   
        //锟斤拷取锟斤拷锟斤拷锟斤拷菘锟侥达拷小(Byte)  
        long blockSize = sf.getBlockSize();   
        //锟斤拷锟叫碉拷锟斤拷菘锟斤拷锟斤拷锟斤拷  
        long freeBlocks = sf.getAvailableBlocks();  
        //锟斤拷锟斤拷SD锟斤拷锟斤拷锟叫达拷小  
        //return freeBlocks * blockSize;  //锟斤拷位Byte  
        //return (freeBlocks * blockSize)/1024;   //锟斤拷位KB  
        return (freeBlocks * blockSize)/1024 /1024; //锟斤拷位MB  
    }      
    
    //锟角凤拷锟斤拷锟絊D锟斤拷
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
    	private boolean isTimeStampBreak;	//时标溢出标志位
    	private int progress,progressTemp;	//progressTemp：记录时标未溢出时的拖动条播放长度
    	private long firstBreakFrameTime;	//记录时标溢出时的第一帧数据的时标
    	
		public PlayerHandler() {
			super();
			this.isTimeStampBreak = false;
			this.progress = 0;
			this.progressTemp = 0;
			this.firstBreakFrameTime = 0;
		}

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
					System.out.println("test firstFrame:"+firstFrameTime);
					while(true){
						correctedStartTime = client.getBeg();
						correctedEndTime = client.getEnd();
						Log.e("----------->>>", "onS totoal time:"+correctedEndTime +","+ correctedStartTime);
						Log.e("----------->>>", "onS totoal time:"+(correctedEndTime - correctedStartTime));
						if(correctedStartTime != -1 && correctedEndTime != -1)
							break;
					}
					mReplaySeekBar.setMax((int)(correctedEndTime - correctedStartTime)*1000);
					System.out.println("test maxFrame:"+(int)(correctedEndTime - correctedStartTime)*1000);
				}else if(YV12Renderer.time != 0 && frameFlag > 0){
					
					endFrameTime = YV12Renderer.time;
					
					//Log.e("----------->>>", "handler msg.arg1 :"+time);
//					Log.e("----------->>>", "firstFtame time:"+firstFrameTime+",endFrame time:"+endFrameTime);
//					Log.e("----------->>>", "handler setProgress :"+(endFrameTime - firstFrameTime));
					System.out.println("test endFrameTime:"+endFrameTime);
					System.out.println("test progress:"+(int)(endFrameTime - firstFrameTime));
					if(!progressHasStop){
						mPlayerHandler.sendEmptyMessage(HIDEPROGRESSBAR);
						progressHasStop = true;
					}
					if((int)(endFrameTime - firstFrameTime) < 0 && !isTimeStampBreak ){
						isTimeStampBreak = true;
						firstBreakFrameTime = endFrameTime;
						System.out.println("test isTimeStampBreak"+isTimeStampBreak);
						progress = stopTrackingTouchProgress;
						if(progress == 0){
							progress = progressTemp;
							System.out.println("test progressTemp:"+progressTemp);
						}
					}else if((int)(endFrameTime - firstFrameTime) > 0 && isTimeStampBreak){
						isTimeStampBreak = false;
					}
					if(isTimeStampBreak){
						System.out.println("test stopTrackingTouchProgress:"+progress);
						System.out.println("test new progress:"+(int)(endFrameTime - firstBreakFrameTime));
						mReplaySeekBar.setProgress(progress + (int)(endFrameTime - firstBreakFrameTime));
					}else{
						mReplaySeekBar.setProgress((int)(endFrameTime - firstFrameTime));
						progressTemp = (int)(endFrameTime - firstFrameTime);
						
					}
					if(stopTrackingTouchProgress != 0){
						stopTrackingTouchProgress = 0;
					}
					//mReplaySeekBar.setProgress((int)(endFrameTime - firstFrameTime));
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
					//锟斤拷锟斤拷锟斤拷锟绞撅拷丝锟绞硷拷锟酵�
//					if(!playback){
//						File destDir = new File("/sdcard/eCamera/cache");
//						if (!destDir.exists()) {
//							destDir.mkdirs();
//						}
//						String path = "/sdcard/eCamera/cache/"+dev.getDevID()+".jpg";
//						YV12Renderer.setCatchPictureFlag(path,path.length());
//					}
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
				//System.out.println("SHOWSTREAMLEN");
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
				//System.out.println("frames:"+client.toString()+","+client.isQuit());
				if(!client.isQuit()){
					//System.out.println("nowFrames:"+nowFrames+"lastSecondFrames:"+lastSecondFrames);
					if(nowFrames == lastSecondFrames){
						//System.out.println("frames send message show progress");
						mPlayerHandler.sendEmptyMessage(SHOWPROGRESSBAR);
					}else{
						//System.out.println("frames send message hide progress");
						mPlayerHandler.sendEmptyMessage(HIDEPROGRESSBAR);
					}
					lastSecondFrames = nowFrames;
					if(nowFrames >= 2000000000/*1000*/){
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
			Log.i("info", "onConfigurationChanged landscape"); // 锟斤拷锟斤拷
			mSurfaceIcon.setVisibility(View.GONE);
			System.out.println("onSingleTapUp:"+mSurfaceIcon.isShown());
			isShowSurfaceIcon = false;
			mStreamLen.setVisibility(View.VISIBLE);
			//isShowSurfaceIcon = true;
		} else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			Log.i("info", "onConfigurationChanged PORTRAIT"); // 锟斤拷锟斤拷
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
			if(!playback){
				System.out.println("test catch picture");
				File destDir = new File("/sdcard/eCamera/cache");
				if (!destDir.exists()) {
					System.out.println("test 1111");
					destDir.mkdirs();
				}
				System.out.println("test2222");
				String path = "/sdcard/eCamera/cache/"+dev.getDevID()+".jpg";
				System.out.println("test: "+path);
				System.out.println("test3333");
				client.setCatchPictureFlag(client.getHandle(),path,path.length());
				System.out.println("test4444");
			}
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
        	Log.e("backCount", "press back button backCount:"+backCount);
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
		 //锟斤拷始锟斤拷 Translate锟斤拷锟斤拷  
	   // translateAnimation = new TranslateAnimation(0.1f, 100.0f,0.1f,0.1f); 
		System.out.println("Fling isAnimationStart:"+isAnimationStart);
		if(fromXDelta == 0&&toXDelta ==40&&fromYDelta ==0&&toYDelta ==0){
			System.out.println("Right");
		}else if(fromXDelta == 0&&toXDelta == -40&&fromYDelta ==0&&toYDelta ==0){
			System.out.println("Left");
		}else if(fromXDelta == 0&&toXDelta == 0&&fromYDelta ==0&&toYDelta ==-40){
			System.out.println("Up");
		}else{
			System.out.println("Down");
		}
		isAnimationStart = true;
//		System.out.println("flingAAAAAAAA");
	    translateAnimation = new TranslateAnimation(fromXDelta, toXDelta,fromYDelta,toYDelta);
//	    System.out.println("flingBBBBBBBB");
	    translateAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
//				System.out.println("Fling00000000");
				animationAim.setVisibility(View.VISIBLE);
//				System.out.println("Fling1111111");
				animationBackground.setVisibility(View.VISIBLE);
//				mAnimationLayout.setVisibility(View.VISIBLE);
//				System.out.println("Fling2222222");
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
//				System.out.println("Fling333333333");
				animationAim.setVisibility(View.GONE);
//				System.out.println("Fling44444444");
				animationBackground.setVisibility(View.GONE);
//				System.out.println("Fling5555555");
				animationAim.clearAnimation();
//				mAnimationLayout.setVisibility(View.INVISIBLE);
//				System.out.println("Fling66666666");
				isAnimationStart = false;
			}
        });
//	    System.out.println("flingCCCCCCCC");
	    //translateAnimation.setFillAfter(true);
//	    System.out.println("flingDDDDDDDD");
	    //锟斤拷始锟斤拷 Alpha锟斤拷锟斤拷  
	    //Animation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);  
	      
	    //锟斤拷锟矫讹拷锟斤拷时锟斤拷 (锟斤拷锟矫碉拷每锟斤拷锟斤拷锟斤拷)  
//	    System.out.println("flingEEEEEEE");
	    translateAnimation.setDuration(2000);  
//	    System.out.println("flingFFFFFFFF");
		animationAim.startAnimation(translateAnimation);  
//		System.out.println("flingGGGGGGGG");
		
	}
	
	Thread delay = new Thread(){
		public void run() {
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
	};
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		 // 锟斤拷锟斤拷锟斤拷停锟�  
        // e1锟斤拷锟斤拷1锟斤拷ACTION_DOWN MotionEvent   
        // e2锟斤拷锟斤拷锟揭伙拷锟紸CTION_MOVE MotionEvent   
        // velocityX锟斤拷X锟斤拷锟较碉拷锟狡讹拷锟劫度ｏ拷锟斤拷锟斤拷/锟斤拷   
        // velocityY锟斤拷Y锟斤拷锟较碉拷锟狡讹拷锟劫度ｏ拷锟斤拷锟斤拷/锟斤拷   
      
        // 锟斤拷锟斤拷锟斤拷锟斤拷 锟斤拷   
        // X锟斤拷锟斤拷锟斤拷位锟狡达拷锟斤拷FLING_MIN_DISTANCE锟斤拷锟斤拷锟狡讹拷锟劫度达拷锟斤拷FLING_MIN_VELOCITY锟斤拷锟斤拷锟斤拷/锟斤拷   
		//System.out.println("Animation flag:"+isAnimationStart);
		if(isAnimationStart || !dev.isPtzFlag() || playback){
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
        	animationStart(0,40,0,0);
        	Log.e("MyGesture", "Fling left "+"x:"+Math.abs(e1.getX() - e2.getX())+"y:"+Math.abs(e1.getY() - e2.getY()));  
        } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {   
            // Fling right   
        	Log.e("MyGesture", "Fling right "+"x:"+Math.abs(e1.getX() - e2.getX())+"y:"+Math.abs(e1.getY() - e2.getY()));   
        	direction = "Left";
        	animationStart(0, -40,0,0);
        	time = 700;
        }  else if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY) {   
            // Fling Down   
        	Log.e("MyGesture", "Fling Down "+"y:"+Math.abs(e1.getY() - e2.getY())+"x:"+Math.abs(e1.getX() - e2.getX()));   
        	direction = "Up";
        	animationStart(0, 0,0,-40);
        	time = 500;
        }   else if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY) {   
            // Fling Up   
        	Log.e("MyGesture", "Fling Up "+"y:"+Math.abs(e1.getY() - e2.getY())+"x:"+Math.abs(e1.getX() - e2.getX()));   
        	direction = "Down";
        	time = 500;
        	animationStart(0, 0,0,40);
        }   else{
        	return true;
        }
        mFlingTask = new MyFlingTask(direction,time);
        mFlingTask.execute();
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
		//锟斤拷锟斤拷
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
		//锟斤拷锟斤拷
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
//		switch (event.getAction() & MotionEvent.ACTION_MASK) {  
//		  
//        case MotionEvent.ACTION_DOWN:  
//            Log.w("FLAG", "ACTION_DOWN");
//            break;  
//        case MotionEvent.ACTION_POINTER_DOWN:  
//            Log.w("FLAG", "ACTION_POINTER_DOWN");  
//            break;  
//        case MotionEvent.ACTION_UP:  
//            Log.w("FLAG", "ACTION_UP");  
//        case MotionEvent.ACTION_POINTER_UP:  
//            Log.w("FLAG", "ACTION_POINTER_UP");  
//            break;  
//        case MotionEvent.ACTION_MOVE:  
//            Log.w("FLAG", "ACTION_MOVE");  
//            break;  
//        }  
		return mGestureDetector.onTouchEvent(event);   
	}
	
	private float spacing(MotionEvent event) {  
        float x = event.getX(0) - event.getX(1);  
        float y = event.getY(0) - event.getY(1);  
        return FloatMath.sqrt(x * x + y * y);  
    } 
	
}
