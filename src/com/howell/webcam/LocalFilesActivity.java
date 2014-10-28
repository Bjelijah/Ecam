package com.howell.webcam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
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
	private Bitmap bitmapReference;
	private static final int SHOWPICTURE = 1;
	private ShowPictureHandler handler/*,handler2,handler3*/;
    private Activities mActivities;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_files);
    	mActivities = Activities.getInstance();
    	mActivities.addActivity("LocalFilesActivity",LocalFilesActivity.this);
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
		handler = new ShowPictureHandler();
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
		if((bitmapReference!=null)&&(!bitmapReference.isRecycled())){
			bitmapReference.recycle();
			bitmapReference = null;
    	}
		handler = null;
		mList = null;
		if(adapter.maps != null){
			adapter.maps.clear();
			adapter.maps = null;
		}
		mActivities.removeActivity("LocalFilesActivity");
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
	
	private void deleteImage(File file){
		if (file.exists()) { // 判断文件是否存在
			if (file.isFile()) { // 判断是否是文件
				file.delete(); // delete()方法 你应该知道 是删除的意思;
			}
		} 
	}
	
    // decode这个图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的  
    private Bitmap decodeFile(File f) {  
        try {  
            // decode image size  
            BitmapFactory.Options o = new BitmapFactory.Options();  
            o.inJustDecodeBounds = true;  
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);  
  
            // Find the correct scale value. It should be the power of 2.  
            //final int REQUIRED_SIZE = 70;  
            int REQUIRED_WIDTH_SIZE = PhoneConfig.getPhoneWidth(this) / 3;
            int REQUIRED_HEIGHT_SIZE = REQUIRED_WIDTH_SIZE * 3 / 4;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;  
            int scale = 1;  
            while (true) {  
                if (width_tmp / 2 < REQUIRED_WIDTH_SIZE  
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
	
	class ShowPictureHandler extends Handler{
		private int position;
		private ImageView iv;
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOWPICTURE:
				 synchronized(this){
					position = msg.arg1;
					iv = (ImageView)msg.obj;
					bm = decodeFile(new File(mList.get(position)));
		            iv.setImageBitmap(bm);
		            adapter.maps.put(position, new SoftReference<Bitmap>(bm));
		            System.out.println("position:"+position);
				 }
				break;

			default:
				break;
			}
		}
	}
	
	public class MyAdapter extends BaseAdapter {
		private Context mContext;
		private Map<Integer,SoftReference<Bitmap>> maps;
		
		public MyAdapter(Context mContext) {
			super();
			this.mContext = mContext;
			this.maps = new HashMap<Integer,SoftReference<Bitmap>>();
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
				
//				holder.iv1.setOnLongClickListener(longClickListener);
//				holder.iv2.setOnLongClickListener(longClickListener);
//				holder.iv3.setOnLongClickListener(longClickListener);
				
				convertView.setTag(holder);
            }else{
            	holder = (ViewHolder)convertView.getTag();
            	
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
            
            holder.iv1.setTag(firstImagePostion);
            if(!maps.containsKey(firstImagePostion)){
            	Message msg = new Message();
        		msg.what = SHOWPICTURE;
        		msg.obj = holder.iv1;
        		msg.arg1 = firstImagePostion;
        		handler.sendMessage(msg);
            }else{
            	SoftReference<Bitmap> reference = maps.get(firstImagePostion);  
                bitmapReference = reference.get();  
                if(bitmapReference != null)
                	holder.iv1.setImageBitmap(bitmapReference);
                else{
                	Message msg = new Message();
            		msg.what = SHOWPICTURE;
            		msg.obj = holder.iv1;
            		msg.arg1 = firstImagePostion;
                	handler.sendMessage(msg);
                }
            }
            System.out.println(mList.get(firstImagePostion)+","+firstImagePostion);
            
	        if(secondImagePositon == mList.size()){
	        	holder.iv2.setVisibility(View.GONE);
	        	holder.iv3.setVisibility(View.GONE);
            	return convertView;
            }else{
            	holder.iv2.setVisibility(View.VISIBLE);
            	holder.iv3.setVisibility(View.VISIBLE);
            }
	        
	        holder.iv2.setTag(secondImagePositon);
	        if(!maps.containsKey(secondImagePositon)){
	        	Message msg = new Message();
        		msg.what = SHOWPICTURE;
        		msg.obj = holder.iv2;
        		msg.arg1 = secondImagePositon;
            	handler.sendMessage(msg);
            }else{
            	SoftReference<Bitmap> reference = maps.get(secondImagePositon);  
                bitmapReference = reference.get();  
                if(bitmapReference != null)
                	holder.iv2.setImageBitmap(bitmapReference);
                else{
                	Message msg = new Message();
            		msg.what = SHOWPICTURE;
            		msg.obj = holder.iv2;
            		msg.arg1 = secondImagePositon;
                	handler.sendMessage(msg);
                }
            }
	        
	        System.out.println(mList.get(secondImagePositon)+","+secondImagePositon);
            if(thirdImagePositon == mList.size()){
            	holder.iv3.setVisibility(View.GONE);
            	return convertView;
            }else{
            	holder.iv3.setVisibility(View.VISIBLE);
            }
            holder.iv3.setTag(thirdImagePositon);
            if(!maps.containsKey(thirdImagePositon)){
            	Message msg = new Message();
        		msg.what = SHOWPICTURE;
        		msg.obj = holder.iv3;
        		msg.arg1 = thirdImagePositon;
            	handler.sendMessage(msg);
            }else{
            	SoftReference<Bitmap> reference = maps.get(thirdImagePositon);  
                bitmapReference = reference.get();  
                if(bitmapReference != null)
                	holder.iv3.setImageBitmap(bitmapReference);
                else{
                	Message msg = new Message();
            		msg.what = SHOWPICTURE;
            		msg.obj = holder.iv3;
            		msg.arg1 = thirdImagePositon;
                	handler.sendMessage(msg);
                }
            }
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
			Intent intent = new Intent(LocalFilesActivity.this, BigImages.class);
			intent.putExtra("position", Integer.valueOf(view.getTag().toString()));
			intent.putStringArrayListExtra("arrayList", mList);
	        startActivity(intent);  
	        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);  
		}
		
	};
	
//	private OnLongClickListener longClickListener = new OnLongClickListener() {
//
//		@Override
//		public boolean onLongClick(View view) {
//			// TODO Auto-generated method stub
//			System.out.println("picture:"+mList.get(Integer.valueOf(view.getTag().toString())));
//			deleteImage(new File(mList.get(Integer.valueOf(view.getTag().toString()))));
//			mList.remove(mList.get(Integer.valueOf(view.getTag().toString())));
//			adapter.notifyDataSetChanged();
//			return false;
//		}
//	};
	
}
