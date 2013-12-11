package com.howell.webcam;

import java.util.ArrayList;

import org.kobjects.base64.Base64;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.howell.webcam.R;
import com.howell.webcam.MyListView.OnRefreshListener;
import com.howell.webcam.player.PlayerActivity;

public class CameraList extends ListActivity implements OnItemClickListener {

    public static final int REQUEST_CODE = 100;
    private SoapManager mSoapManager;
    private LoginResponse mResponse;
    private MyListView listView;
    private CameraListAdapter adapter;
    private ArrayList<Device> list;
    private static final int onFirstRefresh = 1;
    private static final int postUpdateMessage = 2;
    private static final int refreshCameraList = 3;
    
    private String url;
    
    private Activities mActivities;

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
        	mActivities.getmActivityList().add(CameraList.this);
	       
	        mResponse = mSoapManager.getLoginResponse();
	        
	        adapter = new CameraListAdapter(this, list);
            setListAdapter(adapter);
	        
        /*if (mResponse != null) {*/
            list = mResponse.getNodeList();
            sort(list);
            
//            setListAdapter(adapter);
            getListView().setOnItemClickListener(this);
//        }
        }catch (Exception e) {
			// TODO: handle exception
        	Log.e("!!!", "null pointer exception");
//        	Intent intent = new Intent(CameraList.this,LogoActivity.class);
//        	startActivity(intent);
//        	finish();
		}
        listView = (MyListView)findViewById(android.R.id.list);
        listView.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				listView.setEnabled(false);
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try {
							mSoapManager = SoapManager.getInstance();
							LoginRequest loginReq = mSoapManager.getLoginRequest();
					        mResponse = mSoapManager.getUserLoginRes(loginReq);
					        mSoapManager.getLoginResponse().setLoginResponse(mResponse);
					        //更新设备信息
//					        mSoapManager.getQueryDeviceRes(new QueryDeviceReq(mResponse.getAccount(),mResponse.getLoginSession()));
//					        for(NodeDetails node:mSoapManager.getNodeDetails()){
//					        	System.out.println("new Device info:"+node.toString());
//					        }
					        list = mResponse.getNodeList();
					        sort(list);
					        adapter.setList(list);
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
				//获取设备设置（存于SoapManager单例对象中）
				Log.e("CameraList", "onFirstRefresh");
				mSoapManager.getQueryDeviceRes(new QueryDeviceReq(mResponse.getAccount(), mResponse.getLoginSession()));
				
				//显示设备列表
				myHandler.sendEmptyMessage(refreshCameraList);
				myHandler.sendEmptyMessage(onFirstRefresh);
			}
		});
		
        new Thread(){
        	@Override
        	public void run() {
        		// TODO Auto-generated method stub
        		super.run();
        		//检查客户端版本更新
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
        		//推送设置
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
//        //推送设置
//        SharedPreferences sharedPreferences = getSharedPreferences("set",
//                Context.MODE_PRIVATE);
//        boolean pushSet = sharedPreferences.getBoolean(mResponse.getAccount(), true);
//        System.out.println(pushSet);
//        String UUID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
//        UpdateAndroidTokenReq updateAndroidTokenReq = new UpdateAndroidTokenReq(mResponse.getAccount(), mResponse.getLoginSession()
//	    		, UUID,UUID, pushSet);
//	    System.out.println(updateAndroidTokenReq.toString());
//	    UpdateAndroidTokenRes res = mSoapManager.GetUpdateAndroidTokenRes(updateAndroidTokenReq);
//	    Log.e("savePushParam", res.getResult());
//	    
//	    //检查客户端版本更新
//        QueryClientVersionReq queryClientVersionReq = new QueryClientVersionReq("Android");
//        QueryClientVersionRes queryClientVersionRes = mSoapManager.getQueryClientVersionRes(queryClientVersionReq);
//		System.out.println(queryClientVersionRes.toString());
//		String url = new String(Base64.decode(queryClientVersionRes.getDownloadAddress()));
//		System.out.println("url:"+url);
//		String version = getVersion();
//		if(!version.equals(queryClientVersionRes.getVersion())){
//			ClientUpdateUtils.showUpdataDialog(this,url);
//		}
		
    }
    
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
    			adapter.setList(list);
    			adapter.notifyDataSetChanged();
    		}
    	}
    };
    
    private void sort(ArrayList<Device> list){
    	if(list != null){
	    	int length = list.size();
	    	System.out.println("length:"+length);
	    	for(int i = 0 ; i < length ; i++){
	    		System.out.println(i+":"+list.get(i).toString());
	    		if(list.get(i).isOnLine()){
	    			System.out.println(i+" isOnline:"+list.get(i).toString());
	    			list.add(0, list.get(i));
	    			list.remove(i+1);
	    		}else{
	    			System.out.println(i);
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
    
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState){
//        super.onRestoreInstanceState(savedInstanceState);
//        Log.e("CameraList", "onRestoreInstanceState");
//        mSoapManager = (SoapManager) savedInstanceState.getSerializable("temp");
//    }
    
    
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
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	Log.e("CameraList", "onDestroy()");
    	mActivities.getmActivityList().remove(CameraList.this);
    	mActivities.toString();
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
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        menu.add(R.string.settings);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, Settings.class);
        startActivityForResult(intent, REQUEST_CODE);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }*/

    public class CameraListAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<Device> mList;

        public CameraListAdapter(Context context, ArrayList<Device> list) {
            mContext = context;
            mList = list;
        }
        
        public void setList(ArrayList<Device> list){
        	this.mList = list;
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mList == null ? 0 : mList.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            Device dev = (Device) getItem(position);
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.item, null);
            TextView name = (TextView) view.findViewById(R.id.name);
            TextView online = (TextView) view.findViewById(R.id.online);
            name.setText(dev.getName());
            if (dev.isOnLine()) {
                online.setTextColor(Color.GRAY);
                online.setText(R.string.online);
                view.setTag(1);
            } else {
                online.setTextColor(Color.GRAY);
                online.setText(R.string.not_online);
                view.setTag(0);
            }
            return view;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Integer tag = (Integer) arg1.getTag();
        if (tag == 0) {
        	MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.not_online_message),1000);
        } else if (tag == 1) {
            Intent intent = new Intent(CameraList.this, PlayerActivity.class);
            intent.putExtra("arg", mResponse.getNodeList().get((int) arg3));
            startActivity(intent);
        }
    }
    
}
