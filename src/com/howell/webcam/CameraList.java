package com.howell.webcam;

import java.util.ArrayList;

import org.kobjects.base64.Base64;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.howell.webcam.R;
import com.howell.webcam.MyListView.OnRefreshListener;
import com.howell.webcam.player.PlayerActivity;

public class CameraList extends ListActivity{

    private SoapManager mSoapManager;
    private LoginResponse mResponse;
    private MyListView listView;
    private CameraListAdapter adapter;
    private ArrayList<NodeDetails> list;
    private static final int onFirstRefresh = 1;
    private static final int postUpdateMessage = 2;
    private static final int refreshCameraList = 3;
    
    private ImageButton mAddDevice;
    private ImageButton mBack;
//    private TextView mTvAdd;
//    private ImageView mIvAdd;
    
    private String url;
    
    private Activities mActivities;
    private Bitmap bm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_list);
        Log.e("CameraList", "onCreate");
        try{
        	if (savedInstanceState != null) {
        		mSoapManager = (SoapManager) savedInstanceState.getSerializable("soap");
        		Log.e("", mSoapManager.toString());
    		}else{
    			mSoapManager = SoapManager.getInstance();
    			Log.e("", mSoapManager.toString());
    		}
        	
        	mActivities = Activities.getInstance();
        	mActivities.addActivity("CameraList",CameraList.this);
        	
	        mResponse = mSoapManager.getLoginResponse();
	        
	        adapter = new CameraListAdapter(this);
            setListAdapter(adapter);
	        
        }catch (Exception e) {
			// TODO: handle exception
		}
        
//        mTvAdd = (TextView)findViewById(R.id.tv_add);
//        mIvAdd = (ImageView)findViewById(R.id.iv_add);
        mAddDevice = (ImageButton)findViewById(R.id.ib_add);
        mAddDevice.setOnClickListener(adapter.listener);
//        mAddDevice.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View arg0, MotionEvent event) {
//				// TODO Auto-generated method stub
//				if(event.getAction()==MotionEvent.ACTION_DOWN){  
//					System.out.println("ACTION DOWN");
//					mTvAdd.setTextColor(getResources().getColor(R.color.gray));
//					mIvAdd.setImageResource(R.drawable.arrow_right_gray);
//	            }
//				if(event.getAction()==MotionEvent.ACTION_UP){  
//	            	System.out.println("ACTION UP");
//	            	mTvAdd.setTextColor(getResources().getColor(R.color.white)); 
//	            	mIvAdd.setImageResource(R.drawable.arrow_right);
//	            	Intent intent = new Intent(CameraList.this, SetDeviceWifi.class);
//		            startActivity(intent);
//	            }  
//				return true;
//			}
//		});
        mBack = (ImageButton)findViewById(R.id.ib_camera_list_back);
        mBack.setOnClickListener(adapter.listener);
        if(mResponse.getAccount().equals("100868")){
        	mAddDevice.setVisibility(View.GONE);
        	mBack.setVisibility(View.VISIBLE);
        }
        
        listView = (MyListView)findViewById(android.R.id.list);
        listView.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				listView.setEnabled(false);
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try {
							mSoapManager.getQueryDeviceRes(new QueryDeviceReq(mResponse.getAccount(), mResponse.getLoginSession()));
							list = mSoapManager.getNodeDetails();
					        sort(list);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						try{
							adapter.notifyDataSetChanged();
							listView.onRefreshComplete();
							listView.setEnabled(true);
						}catch (Exception e) {
							// TODO: handle exception
						}
					}

				}.execute();
			}

			@Override
			public void onFirstRefresh() {
				// TODO Auto-generated method stub
				Log.e("CameraList", "onFirstRefresh");
				mSoapManager.getQueryDeviceRes(new QueryDeviceReq(mResponse.getAccount(), mResponse.getLoginSession()));
				list = mSoapManager.getNodeDetails();
		        sort(list);
		        CamTabActivity.cameraVerThread = true;
				myHandler.sendEmptyMessage(refreshCameraList);
			}
		});
		
        new Thread(){
        	@Override
        	public void run() {
        		// TODO Auto-generated method stub
        		super.run();
		        QueryClientVersionReq queryClientVersionReq = new QueryClientVersionReq("Android");
		        QueryClientVersionRes queryClientVersionRes = mSoapManager.getQueryClientVersionRes(queryClientVersionReq);
				System.out.println(queryClientVersionRes.toString());
				url = new String(Base64.decode(queryClientVersionRes.getDownloadAddress()));
				System.out.println("url:"+url);
				String version = getVersion();
				if(!version.equals(queryClientVersionRes.getVersion())){
					myHandler.sendEmptyMessage(postUpdateMessage);
				}
				
		        String UUID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		        UpdateAndroidTokenReq updateAndroidTokenReq = new UpdateAndroidTokenReq(mResponse.getAccount(), mResponse.getLoginSession()
			    		, UUID,UUID, true);
			    System.out.println(updateAndroidTokenReq.toString());
			    UpdateAndroidTokenRes res = mSoapManager.GetUpdateAndroidTokenRes(updateAndroidTokenReq);
			    Log.e("savePushParam", res.getResult());
        	}
        }.start();
    }
    
    
    private String getVersion(){
        PackageInfo pkg;
        String versionName = "";
        try {
            pkg = getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
            versionName = pkg.versionName; 
            System.out.println("versionName:" + versionName);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        return versionName;
     }
    
    private Handler myHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		// TODO Auto-generated method stub
    		super.handleMessage(msg);
    		if(msg.what == onFirstRefresh){
    			listView.onRefreshComplete();
    		}
    		if(msg.what == postUpdateMessage){
    			try{
    				ClientUpdateUtils.showUpdataDialog(CameraList.this,url);
    			}catch(Exception e){
    				
    			}
    		}
    		if(msg.what == refreshCameraList){
    			listView.onRefreshComplete();
    			adapter.notifyDataSetChanged();
    		}
    	}
    };
    
    private void sort(ArrayList<NodeDetails> list){
    	if(list != null){
	    	int length = list.size();
	    	for(int i = 0 ; i < length ; i++){
	    		System.out.println(i+":"+list.get(i).toString());
	    		if(list.get(i).isOnLine()){
	    			list.add(0, list.get(i));
	    			list.remove(i+1);
	    		}else{
	    			//System.out.println(i);
	    		}
	    	}
    	}
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.e("CameraList", "onSaveInstanceState");
        savedInstanceState.putSerializable("soap", mSoapManager);
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	Log.e("CameraList", "onPause()");
    }
    
    @Override
    protected void onRestart() {
    	// TODO Auto-generated method stub
    	super.onRestart();
    	Log.e("CameraList", "onRestart()");
    	adapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	Log.e("CameraList", "onDestroy()");
    	mActivities.removeActivity("CameraList");
    	if((bm!=null)&&(!bm.isRecycled())){
	    	bm.recycle();
	    	bm = null;
    	}
    }
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	Log.e("CameraList", "onStop()");
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.e("CameraList", "onResume()");
    }

    public class CameraListAdapter extends BaseAdapter {

        private Context mContext;
        private int imageWidth;
        private int imageHeight;

        public CameraListAdapter(Context context) {
            mContext = context;
            imageWidth = PhoneConfig.getPhoneWidth(getApplicationContext())/2;
            imageHeight = imageWidth * 10 / 16;
        }
        
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list == null ? 0 : list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return list == null ? null : list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
        	System.out.println("getView:"+position);
        	ViewHolder holder = null;
            if (convertView == null) {
            	LayoutInflater layoutInflater = LayoutInflater.from(mContext);
				convertView = layoutInflater.inflate(R.layout.item, null);
				holder = new ViewHolder();
				
				holder.iv = (ImageView)convertView.findViewById(R.id.iv_picture);
				holder.playback = (ImageButton)convertView.findViewById(R.id.iv_playback);
				holder.iv_wifi = (ImageView)convertView.findViewById(R.id.iv_wifi_idensity);
				holder.set = (ImageButton)convertView.findViewById(R.id.iv_set);
				holder.about = (ImageButton)convertView.findViewById(R.id.iv_about);
				holder.tv = (TextView)convertView.findViewById(R.id.tv_name);
				holder.iv_offline = (ImageView)convertView.findViewById(R.id.iv_offline);
				holder.iv_badge = (ImageView)convertView.findViewById(R.id.iv_badge);
				holder.mPlay = (LinearLayout)convertView.findViewById(R.id.to_play);
				
				holder.tv.setTextColor(Color.BLACK);
                convertView.setTag(holder);
                
                holder.iv.setLayoutParams(new FrameLayout.LayoutParams(imageWidth, imageHeight));
		        
                holder.playback.setOnClickListener(listener);
                holder.set.setOnClickListener(listener);
                holder.about.setOnClickListener(listener);
                holder.mPlay.setOnClickListener(listener);
                
            }else{
            	holder = (ViewHolder)convertView.getTag();
            }
            
            holder.mPlay.setTag(position);
            holder.playback.setTag(position);
            holder.set.setTag(position);
            holder.about.setTag(position);
            
            NodeDetails camera = list.get(position);
            
            if(!camera.iseStoreFlag()){
            	holder.playback.setImageResource(R.drawable.card_tab_playback_no_sdcard);
            }
            
            if(camera.getSharingFlag() == 1){
            	holder.tv.setText(camera.getName()+"-分享");
            }else{
            	holder.tv.setText(camera.getName());
            }
            
            if (camera.isOnLine()) {
            	if(getResources().getConfiguration().locale.getCountry().equals("CN"))
            		holder.iv_offline.setImageResource(R.drawable.card_online_image_blue);
//            	if(camera.getIntensity() >= 0 && camera.getIntensity() <= 33){
//            		holder.tv_wifi.setText("wifi强度:弱");
//            	}else if(camera.getIntensity() > 33 && camera.getIntensity() <= 66){
//            		holder.tv_wifi.setText("wifi强度:中");
//            	}else {
//            		holder.tv_wifi.setText("wifi强度:强");
//            	}
            	if(camera.getIntensity() == 0){
                	holder.iv_wifi.setImageResource(R.drawable.wifi_0);
                }else if((camera.getIntensity() > 0 && camera.getIntensity() <= 25)){
                	holder.iv_wifi.setImageResource(R.drawable.wifi_1);
                }else if(camera.getIntensity() > 25 && camera.getIntensity() <= 50){
                	holder.iv_wifi.setImageResource(R.drawable.wifi_2);
                }else if(camera.getIntensity() > 50 && camera.getIntensity() <= 75){
                	holder.iv_wifi.setImageResource(R.drawable.wifi_3);
                }else{
                	holder.iv_wifi.setImageResource(R.drawable.wifi_4);
                }
	        }else {
	        	if(getResources().getConfiguration().locale.getCountry().equals("CN"))
	        		holder.iv_offline.setImageResource(R.drawable.card_offline_image_gray);
	        	holder.iv_wifi.setImageResource(R.drawable.wifi_0);
//	        	holder.tv_wifi.setText("");
	        }
            
            if(camera.isHasUpdate()){
            	holder.iv_badge.setVisibility(View.VISIBLE);
            }else{
            	holder.iv_badge.setVisibility(View.GONE);
            }
            
            
            
            BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inSampleSize = 2;
	        bm = BitmapFactory.decodeFile(list.get(position).getPicturePath(), options);
	        if(bm == null){
	        	holder.iv.setImageResource(R.drawable.card_camera_default_image);
	        }else{
	        	holder.iv.setImageBitmap(bm);
	        }
			return convertView;
        }

        private OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(arg0.getId() == R.id.iv_playback){
					if(!list.get(Integer.valueOf(arg0.getTag().toString())).isOnLine()){
			    		MessageUtiles.postToast(getApplication(), getResources().getString(R.string.not_online_message),2000);
			    		return;
			    	}
					if(!list.get(Integer.valueOf(arg0.getTag().toString())).iseStoreFlag()){
			    		MessageUtiles.postToast(getApplication(), getResources().getString(R.string.no_estore),2000);
			    		return;
			    	}
					System.out.println("tag:"+arg0.getTag().toString());
					System.out.println(((NodeDetails) getItem(Integer.valueOf(arg0.getTag().toString()))).getName());
					Intent intent = new Intent(CameraList.this, VideoList.class);
		            intent.putExtra("Device", ((NodeDetails) getItem(Integer.valueOf(arg0.getTag().toString()))));
		            startActivity(intent);
				}else if(arg0.getId() == R.id.iv_set){
					//System.out.println("����");
//					if(!list.get(Integer.valueOf(arg0.getTag().toString())).isOnLine()){
//			    		MessageUtiles.postToast(getApplication(), getResources().getString(R.string.not_online_message),2000);
//			    		return;
//			    	}
					Intent intent = new Intent(CameraList.this,DeviceSetActivity.class);
					intent.putExtra("Device", (NodeDetails) getItem(Integer.valueOf(arg0.getTag().toString())));
					startActivity(intent);
				}else if(arg0.getId() == R.id.iv_about){
					Intent intent = new Intent(CameraList.this,CameraProperty.class);
					intent.putExtra("Device", (NodeDetails) getItem(Integer.valueOf(arg0.getTag().toString())));
					startActivity(intent);
				}
//				else if(arg0.getId() == R.id.iv_picture){
//					System.out.println(getItem(Integer.valueOf(arg0.getTag().toString())).toString());
//					if (!((NodeDetails)getItem(Integer.valueOf(arg0.getTag().toString()))).isOnLine()) {
//			        	MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.not_online_message),1000);
//			        } else {
//			            Intent intent = new Intent(CameraList.this, PlayerActivity.class);
//			            intent.putExtra("arg", ((NodeDetails) getItem(Integer.valueOf(arg0.getTag().toString()))));
//			            startActivity(intent);
//			        }
//				}
				else if(arg0.getId() == R.id.ib_add){
					Intent intent = new Intent(CameraList.this, SetDeviceWifi.class);
		            startActivity(intent);
				}else if(arg0.getId() == R.id.to_play){
					System.out.println(getItem(Integer.valueOf(arg0.getTag().toString())).toString());
					if (!((NodeDetails)getItem(Integer.valueOf(arg0.getTag().toString()))).isOnLine()) {
			        	MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.not_online_message),1000);
			        } else {
			            Intent intent = new Intent(CameraList.this, PlayerActivity.class);
			            intent.putExtra("arg", ((NodeDetails) getItem(Integer.valueOf(arg0.getTag().toString()))));
			            startActivity(intent);
			        }
				}else if(arg0.getId() == R.id.ib_camera_list_back){
					finish();
				}
			}
		};

    }
    
	public static class ViewHolder {
		public ImageView iv,iv_play_icon,iv_offline/*,iv_wifi*/,iv_badge,iv_wifi;
	    public ImageButton about,set,playback;
	    public TextView tv;
	    public LinearLayout mPlay;
	}

}
