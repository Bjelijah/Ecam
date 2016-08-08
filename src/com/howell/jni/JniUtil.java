package com.howell.jni;

import com.howell.entityclass.StreamReqContext;

public class JniUtil {

	
	static{
		  System.loadLibrary("hwplay");
	      System.loadLibrary("player_jni");
	}
	
	
	/*
	 *yuv native
	 */
	public static native void nativeInit(Object callbackObj);
	public static native void nativeDeinit();
	public static native void nativeRenderY();
	public static native void nativeRenderU();
	public static native void nativeRenderV();
	public static native void nativeOnSurfaceCreated();
	
	
	
	
	/*
	 * audio native
	 */
	public static native void nativeAudioInit(Object callbackObj);
	public static native void nativeAudioStop();
	public static native void nativeAudioDeinit();
	
	
	
	
	/*
	 * streamreq native
	 */
	public static native void nativePlaybackPause(long handle,boolean bPause);
	public static native int nativeGetStreamCount(long handle);
	public static native void nativeJoinThread(long handle);
    public static native long nativeCreateHandle(Object callbackObj,String account, int is_palyback);
    public static native String nativePrepareSDP(long handle,StreamReqContext streamReqContext);
    public static native int nativeHandleRemoteSDP(long handle,StreamReqContext streamReqContext, String dialog_id,String remote_sdp);
    public static native int nativeStart(long handle, StreamReqContext streamReqContext,int timeout_ms);
    public static native void nativeFreeHandle(long handle);
    public static native void nativePrepareReplay(int isPlayBack,long handle);
    public static native int nativeGetMethod(long handle);
    public static native int nativeGetSdpTime(long handle);
    public static native int nativeGetBegSdpTime(long handle);
    public static native int nativeGetEndSdpTime(long handle);
    public static native int nativeSetCatchPictureFlag(long handle,String path,int length);
    
   /*
    * talk native
    */
    public static native int nativeSetAudioData(long handle,byte[] buf ,int len);
	
}
