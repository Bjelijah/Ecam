package com.howell.webcam;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.howell.webcam.R;

public class LocalFilesActivity extends Activity {
	private ListView listview;
	private LinearLayout background;
	private ArrayList<String> mList ;
	private int imageWidth;
	private int imageHeight;
	private LinearLayout.LayoutParams lp;
	private File f;
	private MyAdapter adapter;
	private Bitmap bm;
	private static final int SHOWPICTURE = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_files);
		background = (LinearLayout)findViewById(R.id.lf_local_file);
		imageWidth = PhoneConfig.getPhoneWidth(getApplicationContext())/3;
		imageHeight = imageWidth * 3 / 4;
		f = new File("/sdcard/eCamera");
		lp = new LinearLayout.LayoutParams(imageWidth, imageHeight);
		lp.setMargins(0, 0, 0, 10);
		listview = (ListView)findViewById(R.id.lv_localfiles);
		mList = new ArrayList<String>();
		getFileName(f);
		if(mList.size() != 0){
			background.setBackgroundColor(getResources().getColor(R.color.bg));
		}else{
			background.setBackgroundResource(R.drawable.local_file_bg);
		}
		adapter = new MyAdapter(this);
		listview.setAdapter(adapter);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if((bm!=null)&&(!bm.isRecycled())){
	    	bm.recycle();
	    	bm = null;
    	}
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		System.out.println("Local Files onRestart");
		getFileName(f);
		if(mList.size() != 0){
			background.setBackgroundColor(getResources().getColor(R.color.bg));
		}else{
			background.setBackgroundResource(R.drawable.local_file_bg);
		}
		adapter.notifyDataSetChanged();
	}
	
	public ArrayList<String> getFileName(File file){
		File[] fileArray = file.listFiles();
		for (File f : fileArray) {
			System.out.println(f.getPath());
			if(f.isFile() && !mList.contains(f.getPath())){
				mList.add(f.getPath());
			}
		}
		return mList;
	}
	
	class ShowPictureHandler extends Handler{
		private int position;
		private ImageView iv;
		
		public ShowPictureHandler(int position, ImageView iv) {
			super();
			this.position = position;
			this.iv = iv;
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOWPICTURE:
				bm = BitmapFactory.decodeFile(mList.get(position));
	            iv.setImageBitmap(bm);
				break;

			default:
				break;
			}
		}
	}
	
	public class MyAdapter extends BaseAdapter{
		private Context mContext;
		
		public MyAdapter(Context mContext) {
			super();
			this.mContext = mContext;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if(mList == null){
				return 0;
			}
			if(mList.size()%3 == 0){
				return mList.size() / 3;
			}else{
				return mList.size() / 3 + 1;
			}
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			 return mList == null ? null : mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			System.out.println("getView");
			int firstImagePostion = position * 2 + position ;
			int secondImagePositon = position * 2 + position + 1;
			int thirdImagePositon = position * 2 + position + 2;
			ViewHolder holder = null;
            if (convertView == null) {
            	LayoutInflater layoutInflater = LayoutInflater.from(mContext);
				convertView = layoutInflater.inflate(R.layout.localfile_item, null);
				holder = new ViewHolder();
				
				holder.iv1 = (ImageView)convertView.findViewById(R.id.imageView1);
				holder.iv2 = (ImageView)convertView.findViewById(R.id.imageView2);
				holder.iv3 = (ImageView)convertView.findViewById(R.id.imageView3);
				
				holder.iv1.setLayoutParams(lp);
				holder.iv2.setLayoutParams(lp);
				holder.iv3.setLayoutParams(lp);
				
				holder.iv1.setOnClickListener(listener);
				holder.iv2.setOnClickListener(listener);
				holder.iv3.setOnClickListener(listener);
				
				convertView.setTag(holder);
            }else{
            	holder = (ViewHolder)convertView.getTag();
            	
            	//holder.iv1.setImageResource(R.drawable.images_cache_bg);
				//holder.iv2.setImageResource(R.drawable.images_cache_bg);
				//holder.iv3.setImageResource(R.drawable.images_cache_bg);
            }
            
            if(firstImagePostion == mList.size()){
            	holder.iv1.setVisibility(View.GONE);
            	holder.iv2.setVisibility(View.GONE);
	        	holder.iv3.setVisibility(View.GONE);
            	return convertView;
            }else{
            	holder.iv1.setVisibility(View.VISIBLE);
            	holder.iv2.setVisibility(View.VISIBLE);
            	holder.iv3.setVisibility(View.VISIBLE);
            }
            //Bitmap bm = BitmapFactory.decodeFile(mList.get(firstImagePostion));
            //holder.iv1.setImageBitmap(bm);
            
            holder.iv1.setTag(firstImagePostion);
            ShowPictureHandler handler = new ShowPictureHandler(firstImagePostion,holder.iv1);
            handler.sendEmptyMessage(SHOWPICTURE);
            System.out.println(mList.get(firstImagePostion)+","+firstImagePostion);
	        if(secondImagePositon == mList.size()){
	        	holder.iv2.setVisibility(View.GONE);
	        	holder.iv3.setVisibility(View.GONE);
            	return convertView;
            }else{
            	holder.iv2.setVisibility(View.VISIBLE);
            	holder.iv3.setVisibility(View.VISIBLE);
            }
	        
	        //holder.iv2.setLayoutParams(lp);
	        holder.iv2.setTag(secondImagePositon);
	        ShowPictureHandler handler2 = new ShowPictureHandler(secondImagePositon,holder.iv2);
            handler2.sendEmptyMessage(SHOWPICTURE);
	        System.out.println(mList.get(secondImagePositon)+","+secondImagePositon);
            if(thirdImagePositon == mList.size()){
            	holder.iv3.setVisibility(View.GONE);
            	return convertView;
            }else{
            	holder.iv3.setVisibility(View.VISIBLE);
            }
            //bm = BitmapFactory.decodeFile(mList.get(thirdImagePositon));
	        //holder.iv3.setImageBitmap(bm);
            //holder.iv3.setLayoutParams(lp);
            holder.iv3.setTag(thirdImagePositon);
            ShowPictureHandler handler3 = new ShowPictureHandler(thirdImagePositon,holder.iv3);
            handler3.sendEmptyMessage(SHOWPICTURE);
            System.out.println(mList.get(thirdImagePositon)+","+thirdImagePositon);
			return convertView;
		}
	}
	
	public static class ViewHolder {
		public ImageView iv1,iv2,iv3;
	}
	
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			System.out.println(view.getTag());
			//�Ŵ���С��ת  
			Intent intent = new Intent(LocalFilesActivity.this, BigImages.class);
			intent.putExtra("position", Integer.valueOf(view.getTag().toString()));
			intent.putStringArrayListExtra("arrayList", mList);
	        startActivity(intent);  
	        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);  
		}
		
	};
	
}
