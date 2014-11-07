package com.howell.utils;

import android.content.Context;
import android.view.WindowManager;

/**
 * @author huozhihao
 * 
 * 用于获取手机屏幕长度和宽度的工具类
 */

public class PhoneConfig {
	private static WindowManager wm;
	
	public static int getPhoneWidth(Context context){
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();//��Ļ���
		return width;
	}
	
	public static int getPhoneHeight(Context context){
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int height = wm.getDefaultDisplay().getHeight();//��Ļ���
		return height;
	}
	
}
