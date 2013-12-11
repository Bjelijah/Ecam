package com.howell.webcam;

public class VMDParamReq {
	private String account_;
	private String login_session_;
	private String dev_id_;
	private int channel_no_;
	
	public VMDParamReq(String account, String login_session, String dev_id, int channel_no) {
		// TODO Auto-generated constructor stub
		account_=account;
		login_session_=login_session;
		dev_id_ = dev_id;
		channel_no_=channel_no;
	}
	
	public String getAccount() {
		return account_;
	}
	public String getLoginSession() {
		return login_session_;
	}
	public String getDevID() {
		return dev_id_;
	}
	public int getChannelNo() {
		return channel_no_;
	}
}
