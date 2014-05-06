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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.howell.webcam.R;
import com.howell.webcam.MyListView.OnRefreshListener;
import com.howell.webcam.player.PlayerActivity;

public class CameraList extends ListActivity {

    public static final int REQUEST_CODE = 100;
    private SoapManager mSoapManager;
    private LoginResponse mResponse;
    private MyListView listView;
    private CameraListAdapter adapter;
    private ArrayList<NodeDetails> list;
    private static final int onFirstRefresh = 1;
    private static final int postUpdateMessage = 2;
    private static final int refreshCameraList = 3;
    
    private ImageButton mAddDevice;
    
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
	        
//            getListView().setOnItemClickListener(this);
        }catch (Exception e) {
			// TODO: handle exception
        	Log.e("!!!", "null pointer exception");
//        	Intent intent = new Intent(CameraList.this,LogoActivity.class);
//        	startActivity(intent);
//        	finish();
		}
        
        mAddDevice = (ImageButton)findViewById(R.id.ib_add);
        mAddDevice.setOnClickListener(adapter.listener);
        if(mResponse.getAccount().equals("100868")){
        	mAddDevice.setVisibility(View.GONE);
        }
        
        listView = (MyListView)findViewById(android.R.id.list);
        listView.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				listView.setEnabled(false);
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try {
//							mSoapManager = SoapManager.getInstance();
//							LoginRequest loginReq = mSoapManager.getLoginRequest();
//					        mResponse = mSoapManager.getUserLoginRes(loginReq);
//					        mSoapManager.getLoginResponse().setLoginResponse(mResponse);
					        //�����豸��Ϣ
//					        mSoapManager.getQueryDeviceRes(new QueryDeviceReq(mResponse.getAccount(),mResponse.getLoginSession()));
//					        for(NodeDetails node:mSoapManager.getNodeDetails()){
//					        	System.out.println("new Device info:"+node.toString());
//					        }
//					        list = mResponse.getNodeList();
//					        sort(list);
							mSoapManager.getQueryDeviceRes(new QueryDeviceReq(mResponse.getAccount(), mResponse.getLoginSession()));
							list = mSoapManager.getNodeDetails();
					        sort(list);
//					        for(int i = 0 ; i < list.size() ; i++){
//								Device device = list.get(i);
//								int intensity = getCameraWifiIntensity(device.getDeviceID());
//								device.setIndensity(intensity);
//							}
//					        adapter.setList(list);
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
//				listView.onRefreshComplete();
				//��ȡ�豸���ã�����SoapManager��������У�
				Log.e("CameraList", "onFirstRefresh");
				mSoapManager.getQueryDeviceRes(new QueryDeviceReq(mResponse.getAccount(), mResponse.getLoginSession()));
				list = mSoapManager.getNodeDetails();
		        sort(list);
		        CamTabActivity.cameraVerThread = true;
				//��ȡ�豸WIFIǿ��
//				for(int i = 0 ; i < list.size() ; i++){
//					Device device = list.get(i);
//					int intensity = getCameraWifiIntensity(device.getDeviceID());
//					device.setIndensity(intensity);
//				}
				//��ʾ�豸�б�
				myHandler.sendEmptyMessage(refreshCameraList);
			}
		});
		
        new Thread(){
        	@Override
        	public void run() {
        		// TODO Auto-generated method stub
        		super.run();
        		//���ͻ��˰汾����
		        QueryClientVersionReq queryClientVersionReq = new QueryClientVersionReq("Android");
		        QueryClientVersionRes queryClientVersionRes = mSoapManager.getQueryClientVersionRes(queryClientVersionReq);
				System.out.println(queryClientVersionRes.toString());
				url = new String(Base64.decode(queryClientVersionRes.getDownloadAddress()));
				System.out.println("url:"+url);
				String version = getVersion();
				if(!version.equals(queryClientVersionRes.getVersion())){
					//ClientUpdateUtils.showUpdataDialog(getApplicationContext(),url);
					myHandler.sendEmptyMessage(postUpdateMessage);
				}
        		//��������
//		        SharedPreferences sharedPreferences = getSharedPreferences("set",
//		                Context.MODE_PRIVATE);
//		        boolean pushSet = sharedPreferences.getBoolean(mResponse.getAccount(), true);
//		        System.out.println(pushSet);
				
		        String UUID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		        UpdateAndroidTokenReq updateAndroidTokenReq = new UpdateAndroidTokenReq(mResponse.getAccount(), mResponse.getLoginSession()
			    		, UUID,UUID, true);
			    System.out.println(updateAndroidTokenReq.toString());
			    UpdateAndroidTokenRes res = mSoapManager.GetUpdateAndroidTokenRes(updateAndroidTokenReq);
			    Log.e("savePushParam", res.getResult());
        	}
        }.start();
    }
    
//    private int getCameraWifiIntensity(String devID ){
//    	GetWirelessNetworkReq req = new GetWirelessNetworkReq(mResponse.getAccount(), mResponse.getLoginSession(),devID);
//    	GetWirelessNetworkRes res = mSoapManager.getGetWirelessNetworkRes(req);
//    	System.out.println(res.getResult());
//    	if(res.getResult().equals("OK")){
//    		return res.getIntensity();
//    	}else if(res.getResult().equals("NotSupport")){
//    		return 50;
//    	}else if(res.getResult().equals("DeviceOffline")){
//    		return 0;
//    	}else{
//    		return 0;
//    	}
//    }
    
    private String getVersion(){
        PackageInfo pkg;
        String versionName = "";
        try {
            pkg = getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
            //String appName = pkg.applicationInfo.loadLabel(getPackageManager()).toString(); 
            versionName = pkg.versionName; 
            //System.out.println("appName:" + appName);
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
    			//setListAdapter(adapter);
//    			adapter.setList(list);
    			listView.onRefreshComplete();
    			adapter.notifyDataSetChanged();
    		}
    	}
    };
    
    private void sort(ArrayList<NodeDetails> list){
    	if(list != null){
	    	int length = list.size();
	    	//System.out.println("length:"+length);
	    	for(int i = 0 ; i < length ; i++){
	    		System.out.println(i+":"+list.get(i).toString());
	    		if(list.get(i).isOnLine()){
	    			//System.out.println(i+" isOnline:"+list.get(i).toString());
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
//        private ArrayList<Device> mList;
        private int imageWidth;
        private int imageHeight;

        public CameraListAdapter(Context context) {
            mContext = context;
            imageWidth = PhoneConfig.getPhoneWidth(getApplicationContext())/2;
            imageHeight = imageWidth * 10 / 16;
//            mList = list;
        }
        
//        public void setList(ArrayList<Device> list){
////        	this.mList = list;
//        }
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
            /*NodeDetails dev = (NodeDetails) getItem(position);
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.item, null);
            TextView name = (TextView) view.findViewById(R.id.name);
            TextView online = (TextView) view.findViewById(R.id.online);
            ImageView intensity = (ImageView)view.findViewById(R.id.iv_intensity);
            name.setText(dev.getName());
            if (dev.isOnLine()) {
                online.setTextColor(Color.GRAY);
                online.setText(R.string.online);
                view.setTag(1);
            } else {
            	if(dev.getIntensity() != 0){
            		dev.setIntensity(0);
            	}
                online.setTextColor(Color.GRAY);
                online.setText(R.string.not_online);
                view.setTag(0);
            }
            //System.out.println(dev.toString());
            int cameraIntensity = dev.getIntensity();
            if(cameraIntensity == 0){
            	intensity.setImageDrawable(getResources().getDrawable(R.drawable.wifi_0));
            }else if(cameraIntensity > 0 && cameraIntensity <= 25){
            	intensity.setImageDrawable(getResources().getDrawable(R.drawable.wifi_1));
            }else if(cameraIntensity > 25 && cameraIntensity <= 50){
            	intensity.setImageDrawable(getResources().getDrawable(R.drawable.wifi_2));
            }else if(cameraIntensity > 50 && cameraIntensity <= 75){
            	intensity.setImageDrawable(getResources().getDrawable(R.drawable.wifi_3));
            }else if(cameraIntensity > 75){
            	intensity.setImageDrawable(getResources().getDrawable(R.drawable.wifi_4));
            }else{
            	//intensity.setVisibility(View.GONE);
            }
            return view;*/
        	ViewHolder holder = null;
            if (convertView == null) {
            	LayoutInflater layoutInflater = LayoutInflater.from(mContext);
				convertView = layoutInflater.inflate(R.layout.item, null);
				holder = new ViewHolder();
				
				holder.iv = (ImageView)convertView.findViewById(R.id.iv_picture);
				//holder.iv_play_icon = (ImageView)convertView.findViewById(R.id.iv_play_icon);
				holder.playback = (ImageButton)convertView.findViewById(R.id.iv_playback);
				//holder.iv_wifi = (ImageView)convertView.findViewById(R.id.iv_wifi_idensity);
				holder.tv_wifi = (TextView)convertView.findViewById(R.id.tv_wifi_idensity);
				holder.set = (ImageButton)convertView.findViewById(R.id.iv_set);
				holder.about = (ImageButton)convertView.findViewById(R.id.iv_about);
				holder.tv = (TextView)convertView.findViewById(R.id.tv_name);
				//holder.tv_online = (TextView)convertView.findViewById(R.id.tv_online);
				holder.iv_offline = (ImageView)convertView.findViewById(R.id.iv_offline);
				
				holder.tv.setTextColor(Color.BLACK);
                convertView.setTag(holder);
                
                holder.iv.setLayoutParams(new FrameLayout.LayoutParams(imageWidth, imageHeight));
    	        //holder.iv_play_icon.setLayoutParams(new FrameLayout.LayoutParams(imageWidth, imageHeight));
		        
                holder.playback.setOnClickListener(listener);
                holder.set.setOnClickListener(listener);
                holder.about.setOnClickListener(listener);
                holder.iv.setOnClickListener(listener);
                
//                holder.playback.setOnTouchListener(listener);
//                holder.set.setOnTouchListener(listener);
//                holder.about.setOnTouchListener(listener);
//                holder.iv.setOnTouchListener(listener);
            }else{
            	holder = (ViewHolder)convertView.getTag();
            }
            
            holder.iv.setTag(position);
            holder.playback.setTag(position);
            holder.set.setTag(position);
            holder.about.setTag(position);
            //holder.tv_online.setTag(position);
            
            NodeDetails camera = list.get(position);
            
            if(!camera.iseStoreFlag()){
            	holder.playback.setImageResource(R.drawable.card_tab_playback_no_sdcard);
            }
            
            holder.tv.setText(camera.getName());
            
            if (camera.isOnLine()) {
            	if(getResources().getConfiguration().locale.getCountry().equals("CN"))
            		holder.iv_offline.setImageResource(R.drawable.card_online_image_blue);
            	if(camera.getIntensity() >= 0 && camera.getIntensity() <= 33){
            		holder.tv_wifi.setText("wifi强度:弱");
            	}else if(camera.getIntensity() > 33 && camera.getIntensity() <= 66){
            		holder.tv_wifi.setText("wifi强度:中");
            	}else {
            		holder.tv_wifi.setText("wifi强度:强");
            	}
	        }else {
	        	if(getResources().getConfiguration().locale.getCountry().equals("CN"))
	        		holder.iv_offline.setImageResource(R.drawable.card_offline_image_gray);
	        	holder.tv_wifi.setText("");
	        }
            
//            if(camera.getIntensity() == 0){
//            	holder.iv_wifi.setImageResource(R.drawable.wifi_0);
//            }else if((camera.getIntensity() > 0 && camera.getIntensity() <= 25)){
//            	holder.iv_wifi.setImageResource(R.drawable.wifi_1);
//            }else if(camera.getIntensity() > 25 && camera.getIntensity() <= 50){
//            	holder.iv_wifi.setImageResource(R.drawable.wifi_2);
//            }else if(camera.getIntensity() > 50 && camera.getIntensity() <= 75){
//            	holder.iv_wifi.setImageResource(R.drawable.wifi_3);
//            }else{
//            	holder.iv_wifi.setImageResource(R.drawable.wifi_4);
//            }
            
            //holder.iv.setImageDrawable(images.get(position));
            //String myJpgPath = "/sdcard/eCamera/20130902125951.jpg";
            BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inSampleSize = 2;
	        bm = BitmapFactory.decodeFile(list.get(position).getPicturePath(), options);
	        if(bm == null){
	        	holder.iv.setImageResource(R.drawable.card_camera_default_image);
	        }else{
	        	holder.iv.setImageBitmap(bm);
	        }
        	//}
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
					if(!list.get(Integer.valueOf(arg0.getTag().toString())).isOnLine()){
			    		MessageUtiles.postToast(getApplication(), getResources().getString(R.string.not_online_message),2000);
			    		return;
			    	}
					Intent intent = new Intent(CameraList.this,DeviceSetActivity.class);
					intent.putExtra("Device", (NodeDetails) getItem(Integer.valueOf(arg0.getTag().toString())));
					startActivity(intent);
				}else if(arg0.getId() == R.id.iv_about){
					//System.out.println("����");
					Intent intent = new Intent(CameraList.this,CameraProperty.class);
					intent.putExtra("Device", (NodeDetails) getItem(Integer.valueOf(arg0.getTag().toString())));
					startActivity(intent);
				}else if(arg0.getId() == R.id.iv_picture){
					System.out.println(getItem(Integer.valueOf(arg0.getTag().toString())).toString());
					if (!((NodeDetails)getItem(Integer.valueOf(arg0.getTag().toString()))).isOnLine()) {
			        	MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.not_online_message),1000);
			        } else {
			            Intent intent = new Intent(CameraList.this, PlayerActivity.class);
			            intent.putExtra("arg", ((NodeDetails) getItem(Integer.valueOf(arg0.getTag().toString()))));
			            startActivity(intent);
			        }
				}else if(arg0.getId() == R.id.ib_add){
					Intent intent = new Intent(CameraList.this, SetWifiOrNot.class);
		            startActivity(intent);
				}
			}
		};

    }
    
	public static class ViewHolder {
		public ImageView iv,iv_play_icon,iv_offline/*,iv_wifi*/;
		/*public LinearLayout playback,set,about,tv_online*/;
	    public ImageButton about,set,playback;
	    public TextView tv, tv_wifi;
	}

}
