package com.howell.utils;

import com.android.howell.webcam.R;
import com.howell.entityclass.NodeDetails;
import com.howell.protocol.SoapManager;
import com.howell.protocol.UpgradeDevVerReq;
import com.howell.protocol.UpgradeDevVerRes;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
//import android.view.View.OnClickListener;
import android.util.Log;


public class AlerDialogUtils {
	public static void postDialog(Context context,final NodeDetails dev){
		AlertDialog.Builder builer = new Builder(context) ;   
	    builer.setIcon(R.drawable.expander_ic_minimized);
	    builer.setTitle(context.getResources().getString(R.string.camera_version_title));   
	    builer.setMessage(context.getResources().getString(R.string.camera_update_notice));    
	    builer.setPositiveButton(context.getResources().getString(R.string.ok), new OnClickListener() {    
		    public void onClick(DialogInterface dialog, int which) {  
		    	new Thread(){
		    		@Override
		    		public void run() {
		    			// TODO Auto-generated method stub
		    			super.run();
		    			//CamTabActivity.updateNum -- ;
		    			System.out.println(dev.toString());
		    			//DeviceSetActivity.dev.setHasUpdate(false);
		    			for(NodeDetails d:SoapManager.getInstance().getNodeDetails()){
		    	    		if(d.getName().equals(dev.getName())){
		    	    			d.setHasUpdate(false);
		    	    			break;
		    	    		}
		    	    	}
		    			System.out.println(dev.toString());
		    			//DeviceSetActivity.cameraUpdate();
		    			UpgradeDevVerReq req = new UpgradeDevVerReq(SoapManager.getInstance().getLoginResponse().getAccount(),SoapManager.getInstance().getLoginResponse().getLoginSession(),dev.getDevID());
		    	    	UpgradeDevVerRes res = SoapManager.getInstance().getUpgradeDevVerRes(req);
		    	    	Log.e("cameraUpdate", res.getResult());
		    		}
		    	}.start();
		    }
		});    
	    builer.setNegativeButton(context.getResources().getString(R.string.cancel), new OnClickListener() {    
	        public void onClick(DialogInterface dialog, int which) {    
	            // TODO Auto-generated method stub     
	        }    
	    });    
	    AlertDialog dialog = builer.create();    
	    dialog.show();    
	}

	public static void postDialogMsg(Context context,String title,String msg,OnClickListener ob){
		Builder builder = new Builder(context);
		builder.setTitle(title).setMessage(msg)
				.setPositiveButton(context.getResources().getString(R.string.ok),ob)
				.create()
				.show();
	}
	
	public static void postDialogMsg(Context context,String title,String msg,OnClickListener positiveBtn,OnClickListener negativeBtn){
		Builder builder = new Builder(context);
		builder.setTitle(title).setMessage(msg)
				.setPositiveButton(context.getResources().getString(R.string.ok),positiveBtn)
				.setNegativeButton(context.getResources().getString(R.string.cancel),negativeBtn)
				.create()
				.show();
	}
	
	public static void postDialogMsg(Context context,String title,String msg,String pos,String neg,OnClickListener posBtn,OnClickListener negBtn){
		Builder builder = new Builder(context);
		builder.setTitle(title).setMessage(msg)
				.setPositiveButton(pos,posBtn)
				.setNegativeButton(neg,negBtn)
				.create()
				.show();
	}

}
