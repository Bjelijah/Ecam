package com.howell.webcam;

//
//import java.util.TimerTask;
//
//import android.content.Context;
//import android.content.pm.ApplicationInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.PackageManager.NameNotFoundException;
//import android.net.TrafficStats;
//import android.os.Message;
//
public class NetWorkUtils {
//	private Context context;
//	private long new_KB,old_KB;
//	
//	public NetWorkUtils(Context context) {
//		super();
//		this.context = context;
//	}
//
//	public long getUidRxBytes(){ //获取总的接受字节数，包含Mobile和WiFi等
//		PackageManager pm = context.getPackageManager();
//		ApplicationInfo ai = null;
//		try {
//			ai = pm.getApplicationInfo("com.android.howell.webcam", PackageManager.GET_ACTIVITIES);
//		} catch (NameNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		return TrafficStats.getUidRxBytes(ai.uid)==TrafficStats.UNSUPPORTED?0:(TrafficStats.getTotalRxBytes()/1024);
//	}
//	
//	TimerTask task = new TimerTask() {
//        
//        @Override
//        public void run() {
//                new_KB = getUidRxBytes() - old_KB;
//                old_KB=getUidRxBytes();
//            System.out.println("++++++++++++++++++++++++++++"+s_KB);
//            Message msg = new Message();
//                msg.what = 0;
//                msg.obj =new_KB;
//                handler.sendMessage(msg);
//        }
//    };  
//    private Handler handler = new Handler(){
//      @Override
//      public void handleMessage(Message msg) {
//                              switch(msg.what){
//                              case 0 :
//                   s_KB =(Long) msg.obj;
//                   System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+s_KB);
//                   progressBar.setMessage("视频加载中("+s_KB+"K/S)...");
//                              break;
//                              }
//                      }
//              };
}
