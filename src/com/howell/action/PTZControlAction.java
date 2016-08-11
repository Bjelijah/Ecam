package com.howell.action;

import com.howell.protocol.LensControlReq;
import com.howell.protocol.PtzControlReq;
import com.howell.protocol.PtzControlRes;
import com.howell.protocol.SoapManager;

import android.os.AsyncTask;
import android.util.Log;

/**
 * 
 * @author cbj
 * @category
 *	ptz control class task
 *  just  tele and wide; {@link PlayerActivity}:MyFlingTask for PTZ move
 */
public class PTZControlAction {
	private static PTZControlAction mInstance = null;
	public static PTZControlAction getInstance(){
		if(mInstance == null){
			mInstance = new PTZControlAction();
		}
		return mInstance;
	}
	private PtzInfo info = null;


	public void setPtzInfo(SoapManager soapManager ,String account,String session,String devId,int channel){
		if (info != null) {
			return;
		}
		info = new PtzInfo();
		info.setAccount(account).setLoginSession(session).setDevID(devId).setChannelNo(channel).setSoapManager(soapManager);
	}
	

	public  void zoomTeleStart(){
		new AsyncTask<Void, Integer, Void>(){

			@Override
			protected Void doInBackground(Void... arg0) {
				if (null==info) {
					throw new NullPointerException();
				}
				// TODO Auto-generated method stub
				LensControlReq req = new LensControlReq(info.getAccount(),info.getLoginSession(),info.getDevID(),info.getChannelNo(),"ZoomTele");
				info.getSoapManager().getLensControlRes(req);
				return null;
			}
		}.execute();
	}
	
	public void zoomTeleStop(){
		new AsyncTask<Void, Integer, Void>(){

			@Override
			protected Void doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				if (null==info) {
					throw new NullPointerException();
				}
				LensControlReq req = new LensControlReq(info.getAccount(),info.getLoginSession(),info.getDevID(),info.getChannelNo(),"Stop");
				info.getSoapManager().getLensControlRes(req);
				return null;
			}
			
		}.execute();
	}
	
	
	public void zoomWideStart(){
		new AsyncTask<Void, Integer, Void>(){

			@Override
			protected Void doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				if ( null==info) {
					throw new NullPointerException();
				}
				LensControlReq req = new LensControlReq(info.getAccount(),info.getLoginSession(),info.getDevID(),info.getChannelNo(),"ZoomWide");
				info.getSoapManager().getLensControlRes(req);

				return null;
			}
			
		}.execute();
	}

	public void zoomWideStop(){
		new AsyncTask<Void, Integer, Void>(){
			@Override
			protected Void doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				if(null == info){
					throw new NullPointerException();
				}
				LensControlReq req = new LensControlReq(info.getAccount(),info.getLoginSession(),info.getDevID(),info.getChannelNo(),"Stop");
				info.getSoapManager().getLensControlRes(req);
				return null;
			}
		}.execute();
	}
	
	
	

	public class PtzInfo{
		SoapManager soapManager;
		String account;
		String loginSession;
		String devID;
		int channelNo;
		public SoapManager getSoapManager() {
			return soapManager;
		}
		public PtzInfo setSoapManager(SoapManager soapManager) {
			this.soapManager = soapManager;
			return this;
		}
		public String getAccount() {
			return account;
		}
		public PtzInfo setAccount(String account) {
			this.account = account;
			return this;
		}
		public String getLoginSession() {
			return loginSession;
		}
		public PtzInfo setLoginSession(String loginSession) {
			this.loginSession = loginSession;
			return this;
		}
		public String getDevID() {
			return devID;
		}
		public PtzInfo setDevID(String devID) {
			this.devID = devID;
			return this;
		}
		public int getChannelNo() {
			return channelNo;
		}
		public PtzInfo setChannelNo(int channelNo) {
			this.channelNo = channelNo;
			return this;
		}
		
	}
	
}
