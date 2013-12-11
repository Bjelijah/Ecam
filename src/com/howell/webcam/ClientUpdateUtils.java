package com.howell.webcam;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.android.howell.webcam.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ClientUpdateUtils {
	private static final int DOWN_ERROR = 1;
    /*  
     *   
     * 弹出对话框通知用户更新程序   
     *   
     * 弹出对话框的步骤：  
     *  1.创建alertDialog的builder.    
     *  2.要给builder设置属性, 对话框的内容,样式,按钮  
     *  3.通过builder 创建一个对话框  
     *  4.对话框show()出来    
     */    
    protected static void showUpdataDialog(final Context context,final String httpUrl) {    
        AlertDialog.Builder builer = new Builder(context) ;   
        builer.setIcon(R.drawable.expander_ic_minimized);
        builer.setTitle(context.getResources().getString(R.string.update_dialog_title));   
        builer.setMessage(context.getResources().getString(R.string.update_dialog_message));    
        //当点确定按钮时从服务器上下载 新的apk 然后安装      
        builer.setPositiveButton(context.getResources().getString(R.string.ok), new OnClickListener() {    
        public void onClick(DialogInterface dialog, int which) {    
                Log.i("","下载apk,更新");    
                downLoadApk(context,httpUrl);    
            }       
        });    
        //当点取消按钮时进行登录     
        builer.setNegativeButton(context.getResources().getString(R.string.cancel), new OnClickListener() {    
            public void onClick(DialogInterface dialog, int which) {    
                // TODO Auto-generated method stub     
                //LoginMain();    
            }    
        });    
        AlertDialog dialog = builer.create();    
        dialog.show();    
    }   
    
    /*  
     * 从服务器中下载APK  
     */    
    protected static void downLoadApk(final Context context,final String httpUrl) {    
        final ProgressDialog pd;    //进度条对话框     
        pd = new  ProgressDialog(context);    
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);    
        pd.setMessage(context.getResources().getString(R.string.download_dialog_message));    
        pd.show();    
        new Thread(){    
            @Override    
            public void run() {    
                try {    
                    File file = getFileFromServer(httpUrl, pd);    
                    //sleep(3000);    
                    installApk(file,context);    
                    pd.dismiss(); //结束掉进度条对话框     
                } catch (Exception e) {    
                    Message msg = new Message();    
                    msg.what = DOWN_ERROR;
                    msg.obj = context;
                    handler.sendMessage(msg);    
                    e.printStackTrace();    
                }    
            }}.start();    
    }  
    
    public static Handler handler = new Handler(){        
        @Override    
        public void handleMessage(Message msg) {    
            // TODO Auto-generated method stub     
            super.handleMessage(msg);    
            switch (msg.what) {    
//            case UPDATA_CLIENT:    
//                 //对话框通知用户升级程序      
//                 showUpdataDialog();    
//                 break;    
//            case GET_UNDATAINFO_ERROR:    
//                    //服务器超时      
//                    Toast.makeText(getApplicationContext(), "获取服务器更新信息失败", 1).show();    
//                    LoginMain();    
//                    break;      
            case DOWN_ERROR:    
                    //下载apk失败     
            		Context context = (Context)msg.obj;
                    Toast.makeText((Context)msg.obj, context.getResources().getString(R.string.download_dialog_fail), 1).show();    
                    break;      
            }    
        }    
    };   
    
    //安装apk      
    protected static void installApk(File file,Context context) {    
        Intent intent = new Intent();    
        //执行动作     
        intent.setAction(Intent.ACTION_VIEW);    
        //执行的数据类型     
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");//编者按：此处Android应为android，否则造成安装不了      
        context.startActivity(intent);    
    }    
    
	public static File getFileFromServer(String httpUrl, ProgressDialog pd) throws Exception{     
		//如果相等的话表示当前的sdcard挂载在手机上并且是可用的      
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){     
			URL url = new URL(httpUrl);     
			HttpURLConnection conn =  (HttpURLConnection) url.openConnection();     
			conn.setConnectTimeout(5000);     
			//获取到文件的大小       
			pd.setMax(conn.getContentLength());     
			InputStream is = conn.getInputStream();     
			File file = new File(Environment.getExternalStorageDirectory(), "ecamera.apk");     
			FileOutputStream fos = new FileOutputStream(file);     
			BufferedInputStream bis = new BufferedInputStream(is);     
			byte[] buffer = new byte[1024];     
			int len ;     
			int total=0;     
			while((len =bis.read(buffer))!=-1){     
				fos.write(buffer, 0, len);     
				total+= len;     
				//获取当前下载量      
				pd.setProgress(total);     
			}     
			fos.close();     
			bis.close();     
			is.close();     
			return file;     
		}     
		else{     
			return null;     
		}     
	}    
	
}
