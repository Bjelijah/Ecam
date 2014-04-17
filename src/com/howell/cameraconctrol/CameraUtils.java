package com.howell.cameraconctrol;

import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;

public class CameraUtils {
	private Camera camera;
	
	private void openCamera(){
		try{
			camera = Camera.open();
		}
		catch(RuntimeException e){
			camera = Camera.open(Camera.getNumberOfCameras()-1);
			System.out.println("open()方法有问题");
		}
	}
	
	private void closeCamera(){
		camera.stopPreview();
		camera.release();
		camera = null;
	}
	
	public void twinkle(){
		myThread.start();	
	}
	
	private Thread myThread = new Thread(){
		private int num;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			//打开摄像机
			openCamera();
			//闪烁 500ms亮 500ms灭
			for(num = 1;num <= 4 ;num++){
				System.out.println("次数："+num);
				turnLightOn(camera);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				turnLightOff(camera);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//关闭摄像机
			closeCamera();
		}
	};
	
	/**
	 * 通过设置Camera打开闪光灯
	 * @param mCamera
	 */
	private void turnLightOn(Camera mCamera) {
		if (mCamera == null) {
			return;
		}
		Parameters parameters = mCamera.getParameters();
		if (parameters == null) {
			return;
		}
		List<String> flashModes = parameters.getSupportedFlashModes();
		// Check if camera flash exists
		if (flashModes == null) {
			// Use the screen as a flashlight (next best thing)
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
