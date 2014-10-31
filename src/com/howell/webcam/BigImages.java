package com.howell.webcam;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.howell.webcam.R;


public class BigImages extends Activity implements OnClickListener,OnPageChangeListener,OnViewTapListener{
//	private FrameLayout ll;
	//private GestureDetector mGestureDetector;
	private int position;
	private ArrayList<String> mList;
	
	private ImageButton mShare,mBack,mDelete;
	private FrameLayout title,bottom;
	private TextView mImagePosition;
	
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
        bottom = (FrameLayout)findViewById(R.id.fl_bottom);
        mDelete = (ImageButton)findViewById(R.id.ib_delete);
//        ll = (FrameLayout)findViewById(R.id.ll_big_image);
//        ll.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mBack.setOnClickListener(this);
        title.setOnClickListener(this);
        mDelete.setOnClickListener(this);
        
        mImagePosition = (TextView)findViewById(R.id.tv_bigimage_position);
        mImagePosition.setText((position+1) + "/" + mList.size());
        
        viewPager = (HackyViewPager) findViewById(R.id.viewPager);
        try{
        	adapter = new SamplePagerAdapter();
        }catch(OutOfMemoryError e){
        	System.out.println("OutOfMemory");
        }
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.setOnPageChangeListener(this);
//        viewPager.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
    	mActivities.removeActivity("BigImages");
    	unregisterReceiver(receiver);
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
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return POSITION_NONE;
		}
		
		@Override
		public int getCount() {
			return /*sDrawables.length*/mList.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			System.out.println("instatiateItem position:"+position);
			PhotoView photoView = new PhotoView(container.getContext());
			int requiredWidthSize = PhoneConfig.getPhoneWidth(BigImages.this);
			photoView.setImageBitmap(/*sDrawables[position]*/ScaleImageUtils.decodeFile(requiredWidthSize,requiredWidthSize * 3 / 4,new File(mList.get(position))));
			photoView.setOnViewTapListener(BigImages.this);
			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			photoView.setTag(position);
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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ib_share:
		{
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//			Uri screenshotUri = Uri.parse("file:///sdcard/eCamera/20130902125951.jpg");
			Uri screenshotUri = Uri.parse("file://"+mList.get(position));
			sharingIntent.setType("image/jpeg");
			sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
			startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_pic)));
			break;
		}
		case R.id.ib_bigimage_back:
			BigImages.this.finish();
			break;
//		case R.id.viewPager:
//		{
//			System.out.println("test");
//			if(isShown){
//				System.out.println("1111111");
//				title.setVisibility(View.INVISIBLE);
//				bottom.setVisibility(View.INVISIBLE);
//				isShown = false;
//			}else{
//				System.out.println("22222222");
//				title.setVisibility(View.VISIBLE);
//				bottom.setVisibility(View.VISIBLE);
//				isShown = true;
//			}
//			break;
//		}
		case R.id.ib_delete:
		{
			Dialog alertDialog = new AlertDialog.Builder(BigImages.this).   
    	            setTitle("删除").   
    	            setMessage("删除这张照片？").   
    	            setIcon(R.drawable.expander_ic_minimized).   
    	            setPositiveButton("确定", new DialogInterface.OnClickListener() {   
    	                @Override   
    	                public void onClick(DialogInterface dialog, int which) {   
    	                    // TODO Auto-generated method stub  
//    	                	SharedPreferences sharedPreferences = getSharedPreferences("set", Context.MODE_PRIVATE);
//    	                    Editor editor = sharedPreferences.edit();
//    	                    editor.putBoolean("isServiceStart", false);
//    	                    editor.commit();
    	                	FileUtils.deleteImage(new File(mList.get(position)));
    	                	mList.remove(position);
    	                	mImagePosition.setText((position+1) + "/" + mList.size());
    	                	adapter.notifyDataSetChanged();
    	                	//finish();
    	                }   
    	            }).   
    	            setNegativeButton("取消", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							
						}
					}).
    	    create();   
    		alertDialog.show();   
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		System.out.println("onPageScrollStateChanged:"+arg0);
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		System.out.println("onPageScrolled:"+arg0+","+arg1+","+arg2);
	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		this.position = position;
		mImagePosition.setText((position+1) + "/" + mList.size());
	}

	@Override
	public void onViewTap(View view, float x, float y) {
		// TODO Auto-generated method stub
//		System.out.println("onViewTap");
		if(isShown){
//			System.out.println("1111111");
			title.setVisibility(View.INVISIBLE);
			bottom.setVisibility(View.INVISIBLE);
			isShown = false;
		}else{
//			System.out.println("22222222");
			title.setVisibility(View.VISIBLE);
			bottom.setVisibility(View.VISIBLE);
			isShown = true;
		}
	}
}
