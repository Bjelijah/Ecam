package com.howell.activity;

import java.io.File;

import com.android.howell.webcam.R;
import com.google.zxing.oned.rss.FinderPattern;
import com.howell.utils.FileUtils;
import com.howell.utils.MessageUtiles;
import com.howell.utils.SDCardUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PlayFunFragment extends Fragment implements OnClickListener {
	public static final int MSG_UPDATE_SOUND_STATE = 0x00;
	LinearLayout mFun1,mFun2;
	Context context;
	IPlayFun fun;
	View view;
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		
		try {
			fun = (IPlayFun)activity;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new ClassCastException(activity.toString()+ "must implement IPlayFun");
		}
		
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.play_fun2_fragment, null);
		mFun1 = (LinearLayout)view.findViewById(R.id.play_fun2_sound);
		mFun1.setOnClickListener(this);
		mFun2 = (LinearLayout) view.findViewById(R.id.play_fun2_photo);
		mFun2.setOnClickListener(this);
		
		checkSoundImage();
	
		context = container.getContext();
		return view;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub\
		Log.e("123", "frgmengt on resume");
		checkSoundImage();
		super.onResume();
	}


	
	
	
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		Log.e("123", "frgmengt on start");
		super.onStart();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.play_fun2_photo:
			photoFun();
			break;
		case R.id.play_fun2_sound:
			soundFun();
			checkSoundImage();
			break;
		default:
			break;
		}
	}
	
	
	
	private void photoFun(){
		if(!SDCardUtils.existSDCard()){
			MessageUtiles.postToast(context, getResources().getString(R.string.no_sdcard),2000);
			return;
		}
		File destDir = new File("/sdcard/eCamera");
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		String path = "/sdcard/eCamera/"+FileUtils.getFileName()+".jpg";
		if(PlayerActivity.client.setCatchPictureFlag(PlayerActivity.client.getHandle(),path,path.length()) == 1)
			MessageUtiles.postToast(context, getResources().getString(R.string.save_picture),2000);
	}
	
	private void soundFun(){
		fun.clickSound();
	}
	
	public void checkSoundImage(){
		ImageView iv = (ImageView) view.findViewById(R.id.play_fun2_sound_iv);
		if (fun.getSoundState()) {
			iv.setImageDrawable(getResources().getDrawable(R.drawable.img_sound));
		}else{
			iv.setImageDrawable(getResources().getDrawable(R.drawable.img_no_sound));
		}
	}

	


}
