package com.howell.webcam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.howell.webcam.R;

public class DeviceManageActivity extends ListActivity implements
        OnItemClickListener {

    private SoapManager mSoapManager;
    private LoginResponse mResponse;
    public static ArrayList<NodeDetails> mList;
    private int line;
    
    private Activities mActivities;
    private HomeKeyEventBroadCastReceiver receiver;
    
//    private CameraListAdapter adapter;
    private NodeDetails curDevice;
    private ImageView curRedIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_manager);
        
        mActivities = Activities.getInstance();
    	mActivities.addActivity("DeviceManageActivity",DeviceManageActivity.this);
    	receiver = new HomeKeyEventBroadCastReceiver();
 		registerReceiver(receiver, new IntentFilter(
 				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        try{
	        mSoapManager = SoapManager.getInstance();
	        mResponse = mSoapManager.getLoginResponse();

        /*if (mResponse != null) {*/
            mList = mSoapManager.getNodeDetails();
            CameraListAdapter adapter = new CameraListAdapter(this, mList);
            setListAdapter(adapter);
            getListView().setOnItemClickListener(this);
        //}
        }catch (Exception e) {
			// TODO: handle exception
        	Intent intent = new Intent(DeviceManageActivity.this,LogoActivity.class);
        	startActivity(intent);
        	finish();
		}
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    	line = (int)arg3;
    	curDevice = mList.get(line);
        curRedIcon = (ImageView)(arg1).findViewById(R.id.red_icon);
    	if(!curDevice.isOnLine()){
    		MessageUtiles.postToast(getApplication(), getResources().getString(R.string.not_online_message),2000);
    		return;
    	}
    	//MessageUtiles.postToast(getApplicationContext(), getResources().getString(R.string.load_setting), 1000);
        
//    	new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
				Intent intent = new Intent(DeviceManageActivity.this,
		                DeviceSetActivity.class);
		        intent.putExtra("Device", mList.get(line));
		        startActivity(intent);
//			}
//		}).start();
    }

    public class CameraListAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<NodeDetails> mList;
        //private Map<Integer, View> map;

        public CameraListAdapter(Context context, ArrayList<NodeDetails> list) {
            mContext = context;
            mList = list;
//            map = new HashMap<Integer, View>();
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
//        	if(map.containsKey(position)) { 
//        		return map.get(position);
//        	}
            NodeDetails dev = (NodeDetails) getItem(position);
            
            //dev.setHasUpdate(true);
            
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.device_manager_item,
                    null);
            TextView name = (TextView) view.findViewById(R.id.name);
            ImageView redIcon = (ImageView)view.findViewById(R.id.red_icon);
            name.setText(dev.getName());
            System.out.println(dev.isHasUpdate());
            if(dev.isHasUpdate()){
            	redIcon.setVisibility(View.VISIBLE);
            }else{
            	redIcon.setVisibility(View.INVISIBLE);
            }
//            map.put(position, view);
            return view;
        }
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
        Log.e("DM","onPause");
//    	for(Activity a:mActivities.getmActivityList()){
//    		a.finish();
//    	}
    }
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	Log.e("DM","onStop");
//    	adapter.map.clear();
    }
    
    @Override
    protected void onRestart() {
    	// TODO Auto-generated method stub
    	super.onRestart();
    	Log.e("DM","onRestart");
    	System.out.println();
    	if(curDevice != null && curRedIcon != null){
    		//System.out.println(mList.get(line).toString());
    		//System.out.println("�豸�Ƿ���"+curDevice.isHasUpdate());
    		if(!curDevice.isHasUpdate()) {
    			curRedIcon.setVisibility(View.INVISIBLE);
    		}else {
    			curRedIcon.setVisibility(View.VISIBLE);
    		}
    	}
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.removeActivity("DeviceManageActivity");
    	unregisterReceiver(receiver);
    }
}
