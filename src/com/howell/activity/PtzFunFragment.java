package com.howell.activity;

import com.android.howell.webcam.R;
import com.google.zxing.oned.rss.FinderPattern;
import com.howell.action.PTZControlAction;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class PtzFunFragment extends Fragment implements OnTouchListener{

	private LinearLayout mWide,mTele;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.play_fun1_fragment, null);
		mWide = (LinearLayout)view.findViewById(R.id.play_fragment_wide);
		mWide.setOnTouchListener(this);
		mTele = (LinearLayout)view.findViewById(R.id.play_fragment_tele);
		mTele.setOnTouchListener(this);
		
	
		
		return view;
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.play_fragment_wide:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				
				PTZControlAction.getInstance().zoomTeleStart();
			}else if(event.getAction() == MotionEvent.ACTION_UP){
				
				PTZControlAction.getInstance().zoomTeleStop();
			}
			
			
			break;
		case R.id.play_fragment_tele:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {// 反
				PTZControlAction.getInstance().zoomWideStart();
			}else if(event.getAction() == MotionEvent.ACTION_UP){
				PTZControlAction.getInstance().zoomWideStop();
			}
			
			break;
		default:
			break;
		}
		
		
		
		return false;
	}


	
	
	
	
	
}
