package com.howell.action;

/**
 * Created by Administrator on 2017/7/11.
 */


import android.Manifest;
import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 闪光灯管理工具类
 *
 * http://www.360doc.com/content/14/0308/15/3700464_358779548.shtml
 *
 * @author linzhiyong
 * @time 2016年11月17日07:47:03
 * @email wflinzhiyong@163.com
 *
 * @desc * 如果配合拍照使用, 则无需调用init()方法, 直接使用turnLightOnCamera(Camera c) turnLightOffCamera(Camera c)
 *       * 如果只作为手电筒使用, 则需要初始化init()方法, 使用turnOn() turnOff()
 */
public class FlashLightManager {

    private static final String TAG = FlashLightManager.class.getName();

    /** 上下文对象 */
    private Context context;

    /** 是否已经开启闪光灯 */
    private boolean isOpenFlash = false;

    /** Camera相机硬件操作类 */
    private Camera camera = null;

    /** Camera2相机硬件操作类 */
    private CameraManager manager = null;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession = null;
    private CaptureRequest request = null;
    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private String cameraId = null;
    private boolean isSupportFlashCamera2 = false;
    private TimerTask task;
    private FlashLightManager() {
    }


    private Handler mHander =null;

    public FlashLightManager(Context context) {
        this.context = context;
    }

    /**
     * 初始化相机
     */
    public void init(Handler handler) {
        this.manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (isLOLLIPOP()) {
            initCamera2();
        } else {
            camera = Camera.open();

        }
        mHander = handler;
    }

    public void deInit(){
        if (isLOLLIPOP()){

        }else{
            if (camera!=null){
                camera.stopPreview();
                camera.release();
                camera = null;
            }

        }

    }

    public void twinkle(){
        task = new TimerTask();
        task.execute();
    }

    public void stopTwinkle(){


        if (task!=null){
            task.cancel(true);
            task = null;
        }
        try {
            turnOff();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * 开启闪光灯
     */

    public void turnOn() throws CameraAccessException {
        if (!isSupportFlash()) {
            showToast("设备不支持闪光灯！");
            return;
        }
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            showToast("应用未开启访问相机权限！");
//            return;
//        }
        if (isOpenFlash) {
            return;
        }

        if (isLOLLIPOP()) {
            Log.i("123","camera2 open");
            openFlash();
        } else {
            turnLightOnCamera(camera);
        }
    }

    /**
     * 关闭闪光灯
     */
    public void turnOff() throws CameraAccessException {
        if (!isSupportFlash()) {
            showToast("设备不支持闪光灯！");
            return;
        }
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            showToast("应用未开启访问相机权限！");
//            return;
//        }
        if (!isOpenFlash) {
            return;
        }
        if (isLOLLIPOP()) {
            Log.i("123","camera2 close");
            closeFlash();
        } else {
            turnLightOffCamera(camera);
        }
        isOpenFlash = false;
    }


    private void openFlash() throws CameraAccessException {
    	if(cameraDevice==null)return;
        CaptureRequest.Builder builder =  cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
        builder.addTarget(surface);
        request = builder.build();
        Log.i("123","open flash  FLASH_MODE_TORCH ");
        if (captureSession!=null)
        captureSession.setRepeatingRequest(request,null,null);
        isOpenFlash = true;
    }

    private void closeFlash() throws CameraAccessException {
    	if (cameraDevice==null) return;
        CaptureRequest.Builder builder =  cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        builder.addTarget(surface);
        request = builder.build();
        Log.i("123","close flash  FLASH_MODE_OFF ");
        if (captureSession!=null)
        captureSession.setRepeatingRequest(request,null,null);
        isOpenFlash = false;
    }




    /**
     * 判断设备是否支持闪光灯
     *
     * @return boolean
     */
    public boolean isSupportFlash() {
        if (isLOLLIPOP()) { // 判断当前Android系统版本是否 >= 21, 分别处理
            return isSupportFlashCamera2;
        } else {
            PackageManager pm = context.getPackageManager();
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            for (FeatureInfo f : features) {
                // 判断设备是否支持闪光灯
                if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                    return true;
                }
            }
            // 判断是否支持闪光灯,方式二
            // Camera.Parameters parameters = camera.getParameters();
            // if (parameters == null) {
            // return false;
            // }
            // List<String> flashModes = parameters.getSupportedFlashModes();
            // if (flashModes == null) {
            // return false;
            // }
        }
        return false;
    }

    /**
     * 是否已经开启闪光灯
     *
     * @return
     */
    public boolean isTurnOnFlash() {
        return isOpenFlash;
    }

    /**
     * 判断Android系统版本是否 >= LOLLIPOP(API21)
     *
     * @return boolean
     */
    private boolean isLOLLIPOP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 通过设置Camera打开闪光灯
     *
     * @param mCamera
     */
    public void turnLightOnCamera(Camera mCamera) {
        mCamera.startPreview();
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> flashModes = parameters.getSupportedFlashModes();
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
            // 开启闪光灯
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            }
        }
        isOpenFlash = true;
    }

    /**
     * 通过设置Camera关闭闪光灯
     *
     * @param mCamera
     */
    public void turnLightOffCamera(Camera mCamera) {
//        mCamera.stopPreview();
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> flashModes = parameters.getSupportedFlashModes();
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
            // 关闭闪光灯
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
            }
        }
        isOpenFlash = false;
    }

    /**
     * 初始化Camera2
     */
    private void initCamera2() {
        new Object() {
            private void _initCamera2() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        for (String _cameraId : manager.getCameraIdList()) {
                            CameraCharacteristics characteristics = manager.getCameraCharacteristics(_cameraId);
                            // 过滤掉前置摄像头
                            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                                continue;
                            }
                            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                            if (map == null) {
                                continue;
                            }
                            cameraId = _cameraId;
                            // 判断设备是否支持闪光灯
                            isSupportFlashCamera2 = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("初始化失败：" + e.getMessage());
                    }
                }
            }
        }._initCamera2();
    }

    /**
     * createCaptureSession
     */


    private void createCaptureSession(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {

                public void onConfigured(CameraCaptureSession arg0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        captureSession = arg0;
                    }
                }
                public void onConfigureFailed(CameraCaptureSession arg0) {
                }
            };
            surfaceTexture = new SurfaceTexture(0, false);
            surfaceTexture.setDefaultBufferSize(1280, 720);
            surface = new Surface(surfaceTexture);
            ArrayList localArrayList = new ArrayList(1);
            localArrayList.add(surface);
            try {
                cameraDevice.createCaptureSession(localArrayList, stateCallback, null);
            } catch (Exception e) {
                e.printStackTrace();
                showToast("开启失败：" + e.getMessage());
            }
        }

    }


    private void showToast(String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }

    private void openCamera() throws CameraAccessException {

        manager.openCamera(cameraId, new StateCallback() {
            @Override
            public void onOpened( CameraDevice camera) {
                cameraDevice = camera;
                createCaptureSession();
            }

            @Override
            public void onDisconnected( CameraDevice camera) {

            }

            @Override
            public void onError( CameraDevice camera, int error) {

            }
        },mHander);
    }

    private void closeCamera(){
        if (cameraDevice != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraDevice.close();
        }
    }


    class TimerTask extends AsyncTask<Object, Object, Void> {

        @Override
        protected Void doInBackground(Object... params) {
//            int i = 0;
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }


            while (true){//true
                if (isCancelled()){
                    return null;
                }

                try {
                    Log.i("123","f  turnLight on");
                    turnOn();
                    Log.i("123","--------------------");
                    Thread.sleep(500);
                    turnOff();
                    Thread.sleep(500);
                    Log.i("123","c  turnLight off");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
//                i++;
            }
//            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            closeCamera();
        }
    }





}