package com.howell.push;


import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import com.android.howell.webcam.R;
import com.howell.activity.LogoActivity;
import com.howell.push.WSRes.AlarmLinkRes;
import com.howell.pushlibrary.AbsWorkService;
import com.howell.pushlibrary.DaemonEnv;
import com.howell.utils.NetWorkUtils;
import com.howell.utils.PhoneConfig;
import com.howell.utils.ServerConfigSp;
import com.howell.utils.ThreadUtil;
import com.howell.websocket.autobahn.WebSocketException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;


/**
 * Created by Administrator on 2017/6/8.
 */

public class MyService extends AbsWorkService implements WebSocketManager.IMessage {
	WebSocketManager mgr = WebSocketManager.getInstance();
	boolean mWsIsOpen = false;
	int mCseq = 0;
	Handler mHandler = new Handler();

	Runnable heartRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				mgr.alarmAlive(getCseq(),0,0,0,false);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mHandler.postDelayed(this,60*1000);
		}
	};

	private static boolean isAliveHeart = false;
	public static boolean sShouldStopService=false;
	public static boolean isWorking = false;
	public static String TAG = MyService.class.getName();

	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private int notificationId=0;
	private int heartNo = 0;
	public static void stopService(){
		Log.i("547","myservice stop service");
		DaemonEnv.mShouldWakeUp = false;
		sShouldStopService = true;
		cancelJobAlarmSub();
	}


	@Override
	public IBinder onBind(Intent intent) {
		Log.e("547",TAG+":onBind1");
		return null;
	}

	@Override
	public Boolean shouldStopService(Intent intent, int flags, int startId) {
		return sShouldStopService;
	}

	@Override
	public Boolean shouldStopService() {
		return sShouldStopService;
	}

	@Override
	public void startWork(Intent intent, int flags, int startId) {
		isWorking = true;
		Log.e("547",TAG+":start work");
		//        myFun();
		link();
	}

	@Override
	public void stopWork(Intent intent, int flags, int startId) {
		isWorking = false;
		Log.e("547",TAG+":stop work");
		//TODO do work
		        unLink();

	}

	@Override
	public void stopWork() {
		isWorking = false;
		Log.e("547",TAG+":stop work");
		//TODO stop work
		        unLink();
	}

	@Override
	public Boolean isWorkRunning(Intent intent, int flags, int startId) {
		Log.i("547","isWorking="+isWorking+"    ws connect="+mgr.isOpen());
		return isWorking&&mgr.isOpen();
	}


	@Override
	public IBinder onBind(Intent intent, Void alwaysNull) {
		Log.e("547",TAG+":onBind2");
		return null;
	}

	@Override
	public void onServiceKilled(Intent rootIntent) {
		isWorking = false;
		Log.e("547",TAG+":onServiceKilled  reborn in "+DaemonEnv.DEFAULT_WAKE_UP_INTERVAL+" ms");
		        unLink();
	}

	@Override
	protected int onStart(Intent intent, int flags, int startId) {
		Log.i("123","MyService   onStart");
		return super.onStart(intent, flags, startId);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.e("547","on start");
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		isWorking = false;
		unLink();
		Log.e("547","my service on destroy reborn in "+DaemonEnv.DEFAULT_WAKE_UP_INTERVAL+" ms");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("547", TAG + ":on start command");
		DaemonEnv.mShouldWakeUp = true;
		sShouldStopService = false;
		initNotifcation();
		return super.onStartCommand(intent, flags, startId);
	}

	private long num = 0;
	private void myFun(){
		new Thread(){
			@Override
			public void run() {
				super.run();
				Log.i("123","shouldWakeUp="+DaemonEnv.mShouldWakeUp);
				while(DaemonEnv.mShouldWakeUp) {
					try {
						sleep(2000);
						num++;
						Log.e("547", "i am alive!!  num="+num);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}


	private int getCseq(){
		return mCseq++;
	}

	private void link(){

		String ip = "www.haoweis.com";
		Log.i("547","my server link  ~~~~~~~~~~~~~~~~~~~~link ip="+ip);
		try {
			mgr.registMessage(this).initURL(ip);
		} catch (WebSocketException e) {
			e.printStackTrace();
		}
	}
	private void unLink(){
		Log.i("547","we unLink");
		stopHeart();
		mgr.unregistMessage(this);
		mgr.deInit();
	}

	private void sendLink(){
		ThreadUtil.cachedThreadStart(new Runnable() {
			@Override
			public void run() {
				Log.i("547","sendlink");
				String imei = PhoneConfig.getIMEI(MyService.this);
				try {
					mgr.alarmLink(getCseq(), null, imei);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (Exception e){
					e.printStackTrace();
				}

			}
		});
	}

	private void sendHeart() {
		ThreadUtil.cachedThreadStart(new Runnable() {
			@Override
			public void run() {
				try {

					mgr.alarmAlive(getCseq(),0,0,0,false);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void sendPushAfk(final int cseq){
		ThreadUtil.cachedThreadStart(new Runnable() {
			@Override
			public void run() {
				try {
					mgr.pushRes(cseq);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

	}



	private void startHeart(long delaySec){
		if (!isAliveHeart) {

			ThreadUtil.scheduledSingleThreadStart(new Runnable() {
				@Override
				public void run() {
					try {
						Log.e("547","start alarmAlive  in  scheduledSingleThreadStart  runnable ");
						mgr.alarmAlive(getCseq(),0,0,0,false);
						
						heartNo ++;
						if (heartNo>=3) {
							Log.e("547", "no heart we unlink websocket");
							unLink();
							heartNo = 0;
						}
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			},delaySec,delaySec, TimeUnit.SECONDS);
			isAliveHeart = true;
		}
	}

	private void stopHeart(){
		Log.e("547","stop heart");
		ThreadUtil.scheduledSingleThreadShutDown();
		isAliveHeart = false;
	}


	private void initNotifcation(){
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private int getNotificationId(){
		notificationId++;
		if(notificationId>10){
			notificationId = 0;
		}
		return notificationId;
	}


	private void showNotification(String content){

		Notification.Builder nb = new Notification.Builder(this);
		nb.setTicker("报警");
		try {
			String str[] = content.split(",");
			String title = str[0];
			String text = str[1];
			nb.setContentTitle(title);
			nb.setContentText(text);
		}catch (Exception e){
			nb.setContentTitle(content);
		}
		nb.setSmallIcon(R.drawable.logo);
		nb.setWhen(System.currentTimeMillis());
		nb.setAutoCancel(true);
		nb.setDefaults(Notification.DEFAULT_SOUND);
		PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,LogoActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
		nb.setContentIntent(pendingIntent);
		mNotificationManager.notify(getNotificationId(),nb.build());

	}



	@Override
	public void onWebSocketOpen() {
		Log.i("547","myService   on websocket open   we send  link~~~~~~~~~");
		mWsIsOpen = true;
		sendLink();
	}

	@Override
	public void onWebSocketClose() {
		Log.e("547","myService  on web socket close");
		//判断网络    恢复正常是由 daemon 拉起 startwork
		stopHeart();
		if(!NetWorkUtils.isNetworkConnected(this)){Log.e("547","网络连接失败  不重连");return;}
		if (isWorking) {
			link();
		}
	}

	@Override
	public void onGetMessage(WSRes res) {
		Log.i("547","on get message res="+res.toString());
		switch (res.getType()){
		case ALARM_LINK:
			WSRes.AlarmLinkRes alarmLinkRes = (AlarmLinkRes) res.getResultObject();
			Log.i("547", "alarm link res = "+alarmLinkRes.getResult());
			if (alarmLinkRes.getResult()==0) {
				sendHeart();
			}else{
				unLink();
			}
			break;
		case ALARM_ALIVE:
			heartNo = 0;
			WSRes.AlarmAliveRes aRes = (WSRes.AlarmAliveRes) res.getResultObject();
			startHeart(aRes.getHeartbeatinterval());
			break;
		case ALARM_EVENT:
			//                WSRes.AlarmEvent event = (WSRes.AlarmEvent) res.getResultObject();
			//                Log.i("547","ALARM_EVENT="+res.toString());
			break;
		case ALARM_NOTICE:
			break;
		case PUSH_MESSAGE:
			//会送应答
			WSRes.PushMessage ps = (WSRes.PushMessage) res.getResultObject();
			sendPushAfk(ps.getCseq());

			String content = new String(Base64.decode(ps.getContent(),0));
			Log.i("547","content="+content);
			//直接notficiation
			showNotification(content);

			break;

		default:
			break;
		}
	}

	@Override
	public void onError(int error) {
		Log.e("547","on error="+error);
	}





}
