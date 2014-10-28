package com.howell.webcam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.android.howell.webcam.R;

import uk.co.senab.photoview.PhotoView;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;


public class BigImages extends Activity implements OnClickListener{
	private LinearLayout ll;
	//private GestureDetector mGestureDetector;
	private int position;
	private ArrayList<String> mList;
	
	private ImageButton mShare,mBack;
	private FrameLayout title;
	
	private HackyViewPager viewPager;
	
	private boolean isShown;
	
	private SamplePagerAdapter adapter;
	private Activities mActivities;
	private HomeKeyEventBroadCastReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.big_images);
		
		mActivities = Activities.getInstance();
        mActivities.addActivity("BigImages",BigImages.this);
        receiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(receiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        
        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        System.out.println("position:"+position);
        mList = intent.getStringArrayListExtra("arrayList");
        
        isShown = true;
        
        mShare = (ImageButton)findViewById(R.id.ib_share);
        mBack = (ImageButton)findViewById(R.id.ib_bigimage_back);
        title = (FrameLayout)findViewById(R.id.fl_title);
        ll = (LinearLayout)findViewById(R.id.ll_big_image);
        mShare.setOnClickListener(this);
        mBack.setOnClickListener(this);
        title.setOnClickListener(this);
        
        viewPager = (HackyViewPager) findViewById(R.id.viewPager);
        try{
        	adapter = new SamplePagerAdapter();
        }catch(OutOfMemoryError e){
        	System.out.println("OutOfMemory");
        }
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		for(Bitmap bm:adapter.sDrawables){
//			if((bm!=null)&&(!bm.isRecycled())){
//		    	bm.recycle();
//		    	bm = null;
//	    	}
//		}
    	mActivities.removeActivity("BigImages");
    	unregisterReceiver(receiver);
	}
	
	private Bitmap decodeFile(File f) {  
        try {  
            // decode image size  
            BitmapFactory.Options o = new BitmapFactory.Options();  
            o.inJustDecodeBounds = true;  
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);  
  
            // Find the correct scale value. It should be the power of 2.  
            final int REQUIRED_WIDTH_SIZE = PhoneConfig.getPhoneWidth(this);  
            final int REQUIRED_HEIGHT_SIZE = REQUIRED_WIDTH_SIZE * 3 / 4;  
            int width_tmp = o.outWidth, height_tmp = o.outHeight;  
            int scale = 1;  
            while (true) {  
                if (width_tmp / 2 <   REQUIRED_WIDTH_SIZE
                        || height_tmp / 2 < REQUIRED_HEIGHT_SIZE)  
                    break;  
                width_tmp /= 2;  
                height_tmp /= 2;  
                scale *= 2;  
            }  
  
            // decode with inSampleSize  
            BitmapFactory.Options o2 = new BitmapFactory.Options();  
            o2.inSampleSize = scale;  
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);  
        } catch (FileNotFoundException e) {  
        }  
        return null;  
    }  
	
	class SamplePagerAdapter extends PagerAdapter {

		//private Bitmap [] sDrawables = new Bitmap[mList.size()];
		
		public SamplePagerAdapter() {
			super();
//			for(int i = 0 ; i < mList.size() ; i++){
//				sDrawables[i] = decodeFile(new File(mList.get(i)));
//			}
		}

		@Override
		public int getCount() {
			return /*sDrawables.length*/mList.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			System.out.println("instatiateItem position:"+position);
			PhotoView photoView = new PhotoView(container.getContext());
			photoView.setImageBitmap(/*sDrawables[position]*/decodeFile(new File(mList.get(position))));

			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);

			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		System.out.println("onRestart");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ib_share:
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//			Uri screenshotUri = Uri.parse("file:///sdcard/eCamera/20130902125951.jpg");
			Uri screenshotUri = Uri.parse("file://"+mList.get(position));
			sharingIntent.setType("image/jpeg");
			sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
			startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_pic)));
			break;
		case R.id.ib_bigimage_back:
			BigImages.this.finish();
			break;
		case R.id.viewPager:
			if(isShown){
				System.out.println("1111111");
				title.setVisibility(View.INVISIBLE);
				isShown = false;
			}else{
				System.out.println("22222222");
				title.setVisibility(View.VISIBLE);
				isShown = true;
			}
			break;
		default:
			break;
		}
	}
}
