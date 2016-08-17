package com.howell.activity;

import com.howell.adapter.MyPagerAdapter;

import android.content.Context;
import android.content.DialogInterface.OnShowListener;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PlayFunViewPage extends ViewPager {

	public PlayFunViewPage(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public PlayFunViewPage(Context context,AttributeSet attrs){
		super(context, attrs);
	}
	
	
	private float startX;
	private float endX;
	
	private View mBottomView = null;
	

	
	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		if (arg0.getAction() == MotionEvent.ACTION_DOWN && mBottomView!=null) {
			if (arg0.getY() > mBottomView.getTop()) {
			    Log.e("123", "gety > view.top 不处理    view page touch y="+arg0.getY()+" bottom view top="+mBottomView.getTop());
				return false;//不处理    让 监听优先级低的（onclickedlistener）和下面的控件处理
			}
		}
		return super.onTouchEvent(arg0);
	}
	
	
	
	public void setBottomView(View v){
		this.mBottomView = v;
	}

	
	public void updataAllView(){//FIXME low effect
		MyPagerAdapter adapter = (MyPagerAdapter) getAdapter();
		adapter.notifyDataSetChanged();
	}
	
	private void fun(){
		MyPagerAdapter adapter = (MyPagerAdapter) getAdapter();
		int item = getCurrentItem();
		Fragment  fragment = adapter.getItem(item);
		if (fragment instanceof PlayFunFragment) {
			((PlayFunFragment) fragment).checkSoundImage();
		}
	}
}
