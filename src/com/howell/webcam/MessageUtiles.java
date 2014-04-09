package com.howell.webcam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.howell.webcam.R;

public class MessageUtiles {
//	private static Context context;
//	public ToastUtiles(Context context){
//		this.context = context;
//	}
	public static void postToast(Context context,String message,int time){
//		Toast toast= Toast.makeText(context, message, 1000);
//		toast.setGravity(Gravity.CENTER, 0, 0);
//		toast.show();
		Toast.makeText(context, message, time).show();
	}
	
//	public static void postAlerDialog(Context context,String message){
//		new AlertDialog.Builder(context)   
////        .setTitle("�û�����������")   
//        .setMessage(message)                 
//        .setPositiveButton("ȷ��", null)   
//        .show();  
//	}
	
	public static Dialog postNewUIDialog(Context context){
		final Dialog lDialog = new Dialog(context,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
//       lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		lDialog.setContentView(R.layout.wait_dialog);
//       ((TextView) lDialog.findViewById(R.id.dialog_title)).setText(pTitle);
		//lDialog.show();
		return lDialog;
	}
	
	public static void postNewUIDialog2(Context context,String message,String buttonName,final int flag){
		 final Dialog lDialog = new Dialog(context,android.R.style.Theme_Translucent_NoTitleBar);
//         lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
         lDialog.setContentView(R.layout.dialog_view);
//         ((TextView) lDialog.findViewById(R.id.dialog_title)).setText(pTitle);
         ((TextView) lDialog.findViewById(R.id.dialog_message)).setText(message);
         ((Button) lDialog.findViewById(R.id.ok)).setText(buttonName);
         ((Button) lDialog.findViewById(R.id.ok))
                 .setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         // write your code to do things after users clicks OK
                    	 if(flag == 0){
                    		 
                    	 }
                         lDialog.dismiss();
                     }
                 });
          lDialog.show();
	}
	public static void postNewUIDialog(Context context,String message,String buttonName,final int flag){
		 final Dialog lDialog = new Dialog(context,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
//        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setContentView(R.layout.dialog_view);
//        ((TextView) lDialog.findViewById(R.id.dialog_title)).setText(pTitle);
        ((TextView) lDialog.findViewById(R.id.dialog_message)).setText(message);
        ((Button) lDialog.findViewById(R.id.ok)).setText(buttonName);
        ((Button) lDialog.findViewById(R.id.ok))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // write your code to do things after users clicks OK
                   	 if(flag == 0){
                   		 
                   	 }
                        lDialog.dismiss();
                    }
                });
         lDialog.show();
	}
	
	public static void postUpdateDialog(Context context,String message,String buttonName1,String buttonName2 ){
		 final Dialog lDialog = new Dialog(context,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
//       lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
       lDialog.setContentView(R.layout.update_dialog);
//       ((TextView) lDialog.findViewById(R.id.dialog_title)).setText(pTitle);
       ((TextView) lDialog.findViewById(R.id.dialog_message)).setText(message);
       ((Button) lDialog.findViewById(R.id.ok)).setText(buttonName1);
       ((Button) lDialog.findViewById(R.id.ok))
               .setOnClickListener(new OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       // write your code to do things after users clicks OK
                       lDialog.dismiss();
                   }
               });
       ((Button) lDialog.findViewById(R.id.cancel)).setText(buttonName2);
       ((Button) lDialog.findViewById(R.id.cancel))
               .setOnClickListener(new OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       // write your code to do things after users clicks OK
                       lDialog.dismiss();
                   }
               });
        lDialog.show();
	}
}
