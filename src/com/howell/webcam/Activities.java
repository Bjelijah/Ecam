package com.howell.webcam;

import java.util.ArrayList;

import android.app.Activity;

public class Activities {
	
    public static ArrayList<Activity> mActivityList = new ArrayList<Activity>();
    
    private static Activities sInstance = new Activities();

    public static Activities getInstance() {
        return sInstance;
    }

	public ArrayList<Activity> getmActivityList() {
		return mActivityList;
	}

	@Override
	public String toString() {
		for(Activity a:mActivityList){
			System.out.println(a.getLocalClassName());
		}
		return null;
	}

//	public static void setmActivityList(ArrayList<Activity> mActivityList) {
//		Activitys.mActivityList = mActivityList;
//	}
    
}
