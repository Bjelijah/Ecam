package com.howell.webcam;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
//import android.view.View.OnClickListener;

import com.android.howell.webcam.R;

public class AlerDialogUtils {
	public static void postDialog(Context context){
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
		    			System.out.println(DeviceSetActivity.dev.toString());
		    			DeviceSetActivity.dev.setHasUpdate(false);
//		    			for(NodeDetails d:DeviceManageActivity.mList){
//		    	    		if(d.getName().equals(DeviceSetActivity.dev.getName())){
//		    	    			d.setHasUpdate(DeviceSetActivity.dev.isHasUpdate());
//		    	    			break;
//		    	    		}
//		    	    	}
		    			System.out.println(DeviceSetActivity.dev.toString());
		    			DeviceSetActivity.cameraUpdate();
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
}
