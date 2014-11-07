package com.howell.utils;


public class DeviceVersionDetect {
    private static DeviceVersionDetect sInstance = new DeviceVersionDetect();
	private OnDeviceVersionListener refreshListener;
	//private boolean isPrepared  = false;
	
    public static DeviceVersionDetect getInstance() {
        return sInstance;
    }
	
	public void setOnDeviceVersionListener(OnDeviceVersionListener refreshListener) {
		this.refreshListener = refreshListener;
		//isPrepared = true;
	}
    
	public interface OnDeviceVersionListener {
		public void onDeviceNewVersionRefresh();
	}
	
	public void onDeviceNewVersionRefresh() throws InterruptedException {
		System.out.println("onDeviceNewVersionRefresh");
		while (refreshListener == null) {
			System.out.println("wait onDeviceNewVersionRefresh sleep");
			Thread.sleep(100);
		}
		refreshListener.onDeviceNewVersionRefresh();
	}
    
}
