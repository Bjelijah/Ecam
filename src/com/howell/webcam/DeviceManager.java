package com.howell.webcam;

import java.util.HashMap;
import java.util.Map;

public class DeviceManager {
	private static DeviceManager sInstance = new DeviceManager();
	private Map<String, Device> map = new HashMap<String, Device>();
	public static DeviceManager getInstance() {
	    return sInstance;
	}
	 
	public void addMember(Device device){
		map.put(device.getDeviceID(), device);
	}
	
	public void clearMember(){
		map.clear();
	}

	public Map<String, Device> getMap() {
		return map;
	}

}
