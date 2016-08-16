package com.howell.adapter;

import com.howell.activity.PlayFunFragment;
import com.howell.activity.PtzFunFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class MyPagerAdapter extends FragmentStatePagerAdapter {
	
	Fragment fragment;
	public MyPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public Fragment getItem(int arg0) {
		
		int newPos;
		if (arg0>=0) {
			newPos = (arg0+200)%2;
		}else{
			newPos = (-arg0)%2;
		}
		switch (newPos) {
		case 0:
			fragment = new PtzFunFragment();
			break;
		case 1:
			fragment = new PlayFunFragment();
			break;
	    default:
	    	return null;
		}
		return fragment;
	}
	

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		super.destroyItem(container, position, object);
	}
	
	@Override
	public int getCount() {
		return Integer.MAX_VALUE;
	}	
	
	@Override
	public int getItemPosition(Object object) {
		// TODO Auto-generated method stub
		return POSITION_NONE;
	}
	
	public Fragment getCurFragment(){
		return fragment;
	}
}
