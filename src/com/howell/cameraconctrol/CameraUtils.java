package com.howell.cameraconctrol;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class CameraUtils {
	private Camera camera;
	private Timer timer;
	
	
	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	private void openCamera(){
		try{
			camera = Camera.open();
			camera.startPreview();
		}
		catch(RuntimeException e){
			camera = Camera.open(Camera.getNumberOfCameras()-1);
			System.out.println("open()方法有问题");
		}
	}
	
	public void stopTwinkle(){
		if(timer != null){
			timer.cancel();
			closeCamera();
		}
	}
	
	private void closeCamera(){
		if(camera != null){
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}
	
	public void twinkle(final TextView tips){
		//myThread.start();	
//		int num;
		//打开摄像机
		openCamera();
		//闪烁 500ms亮 500ms灭
//		for(num = 1;num <= 4 ;num++){
//			System.out.println("次数："+num);
//			try {
//				turnLightOn(camera);
//				Thread.sleep(500);
//				turnLightOff(camera);
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		timer = new Timer();//实例化Timer类
		timer.schedule(new TimerTask(){
			int num = 0;
			public void run(){
				System.out.println(num);
				/*if(num == 8){
					//this.cancel();
					//closeCamera();
					
					Message msg = new Message();
					msg.what = 1;
					msg.obj = tips;
					handler.sendMessage(msg);
//					tips.setVisibility(View.VISIBLE);
					//return;
				}*/
				if(num % 2 == 0){
					turnLightOn(camera);
					System.out.println("亮");
				}else{
					turnLightOff(camera);
					System.out.println("灭");
				}
				num++;
			}
		},0,500);
		//关闭摄像机
		
	}
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				TextView tv = (TextView) msg.obj;
				tv.setVisibility(View.VISIBLE);
				break;

			default:
				break;
			}
		}
	};
	
	/**
	 * 通过设置Camera打开闪光灯
	 * @param mCamera
	 */
	private void turnLightOn(Camera mCamera) {
		if (mCamera == null) {
			System.out.println("camera == null");
			return;
		}
		Parameters parameters = mCamera.getParameters();
		if (parameters == null) {
			System.out.println("parameters == null");
			return;
		}
		List<String> flashModes = parameters.getSupportedFlashModes();
		// Check if camera flash exists
		if (flashModes == null) {
			// Use the screen as a flashlight (next best thing)
			System.out.println("flashModes == null");
			return;
		}
		String flashMode = parameters.getFlashMode();
		Log.i("", "Flash mode: " + flashMode);
		Log.i("", "Flash modes: " + flashModes);
		if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
			// Turn on the flash
			System.out.println("turn on");
			if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {
				System.out.println("turn on 2");
				parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
				mCamera.setParameters(parameters);
			} else {
			}
		}
	}
	/**
	 * 通过设置Camera关闭闪光灯
	 * @param mCamera
	 */
	private void turnLightOff(Camera mCamera) {
		if (mCamera == null) {
			return;
		}
		Parameters parameters = mCamera.getParameters();
		if (parameters == null) {
			return;
		}
		List<String> flashModes = parameters.getSupportedFlashModes();
		String flashMode = parameters.getFlashMode();
		// Check if camera flash exists
		if (flashModes == null) {
			return;
		}
		Log.i("", "Flash mode: " + flashMode);
		Log.i("", "Flash modes: " + flashModes);
		if (!Parameters.FLASH_MODE_OFF.equals(flashMode)) {
			// Turn off the flash
			if (flashModes.contains(Parameters.FLASH_MODE_OFF)) {
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				mCamera.setParameters(parameters);
			} else {
				Log.e("", "FLASH_MODE_OFF not supported");
			}
		}
	}
}
