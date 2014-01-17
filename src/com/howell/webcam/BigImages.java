package com.howell.webcam;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.android.howell.webcam.R;

public class BigImages extends Activity implements OnTouchListener, OnGestureListener{
	private ImageView iv1,iv2;
	private ViewFlipper mViewFlipper;
	private LinearLayout ll;
	private GestureDetector mGestureDetector;
	private int position;
	private ArrayList<String> mList;
	
	private int imageId;
	private Bitmap bm;
	private ImageButton mShare;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.big_images);
		mGestureDetector = new GestureDetector(this);   
		mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);
		ll = (LinearLayout)findViewById(R.id.ll);
		iv1 = (ImageView)findViewById(R.id.iv1);
		iv2 = (ImageView)findViewById(R.id.iv2);
		
		ll.setOnTouchListener(this);   
		ll.setFocusable(true);   
		ll.setClickable(true);   
		ll.setLongClickable(true);   
        mGestureDetector.setIsLongpressEnabled(true); 
        
        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        System.out.println("position:"+position);
        mList = intent.getStringArrayListExtra("arrayList");
        
        bm = BitmapFactory.decodeFile(mList.get(position));
        iv1.setImageBitmap(bm);
        
        imageId = R.id.iv1;
        mShare = (ImageButton)findViewById(R.id.ib_share);
        mShare.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//				Uri screenshotUri = Uri.parse("file:///sdcard/eCamera/20130902125951.jpg");
				Uri screenshotUri = Uri.parse("file://"+mList.get(position));
				sharingIntent.setType("image/jpeg");
				sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
				startActivity(Intent.createChooser(sharingIntent, "·ÖÏíÍ¼Æ¬"));
			}
		});
//        for(Integer i:mList){
//        	System.out.println(i);
//        }
		
//		Bitmap bitmap = (Bitmap)getIntent().getParcelableExtra("bitmap");
//		System.out.println(bitmap.toString());
//		iv = (ImageView)findViewById(R.id.iv);
//		iv.setImageBitmap(bitmap);
//		
//		iv.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				OtherActivity.this.finish();
//				overridePendingTransition(R.anim.zoomin, R.anim.zoomout);  
//			}
//		});
	}
	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		 Log.e("onFling", "Fling");
		final int FLING_MIN_DISTANCE = 100, FLING_MIN_VELOCITY = 200;   
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {   
	        // Fling left   
			mViewFlipper.setInAnimation(this, R.anim.push_left_in);
			mViewFlipper.setOutAnimation(this, R.anim.push_left_out);
			if(position == mList.size() - 1){
				//position = -1;
				return true;
			}
			System.out.println("position:"+position);
			bm = BitmapFactory.decodeFile(mList.get(position + 1));
	    	if(imageId == R.id.iv1){
//	    		iv2.setImageDrawable(getResources().getDrawable(mList.get(position + 1)));
	    		iv2.setImageBitmap(bm);
		    	mViewFlipper.showNext();
				imageId = R.id.iv2;
			}else{
//				iv1.setImageDrawable(getResources().getDrawable(mList.get(position + 1)));
				iv1.setImageBitmap(bm);
				mViewFlipper.showNext();
				imageId = R.id.iv1;
			}
	    	position ++;
	        Log.e("MyGesture", "Fling left "+"x:"+Math.abs(e1.getX() - e2.getX())+"y:"+Math.abs(e1.getY() - e2.getY()));  
	    } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {   
	        // Fling right   
	    	mViewFlipper.setInAnimation(this, R.anim.push_right_in);
			mViewFlipper.setOutAnimation(this, R.anim.push_right_out);
	    	if(position == 0){
				//position = mList.size() ;
				return true;
			}
	    	System.out.println("position:"+position);
	    	bm = BitmapFactory.decodeFile(mList.get(position - 1));
			if(imageId == R.id.iv1){
//				iv2.setImageDrawable(getResources().getDrawable(mList.get(position - 1)));
				iv2.setImageBitmap(bm);
				mViewFlipper.showPrevious();
				imageId = R.id.iv2;
			}else{
//				iv1.setImageDrawable(getResources().getDrawable(mList.get(position - 1)));
				iv1.setImageBitmap(bm);
				mViewFlipper.showPrevious();
				imageId = R.id.iv1;
			}
			position--;
	        Log.e("MyGesture", "Fling right "+"x:"+Math.abs(e1.getX() - e2.getX())+"y:"+Math.abs(e1.getY() - e2.getY()));   
	    } else if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY) {   
	        // Fling Down   
	        Log.e("MyGesture", "Fling Down "+"y:"+Math.abs(e1.getY() - e2.getY())+"x:"+Math.abs(e1.getX() - e2.getX()));   
	    } else if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY) {   
	        // Fling Up   
	        Log.e("MyGesture", "Fling Up "+"y:"+Math.abs(e1.getY() - e2.getY())+"x:"+Math.abs(e1.getX() - e2.getX()));   
	    } else{
	        return true;
	    }
		return true;
	}
	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		Log.e("onLongPress", "onLongPress");
	}
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		Log.e("onShowPress", "onShowPress");
	}
	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		Log.e("onSingleTapUp", "onSingleTapUp");
		BigImages.this.finish();
		overridePendingTransition(R.anim.zoomin, R.anim.zoomout);  
		return true;
	}
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		// TODO Auto-generated method stub
		Log.e("onTouch", "onTouch");
		return mGestureDetector.onTouchEvent(event);   
	}

}
