package com.howell.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * @author 霍之昊 
 *
 * 类说明
 */
public class SDCardUtils {
	
	public static String getSDCardPath(){
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	
	public static String getBitmapCachePath(){
		return getSDCardPath() + File.separator + "eCamera" + File.separator + "notice_cache" + File.separator;
	}
	
	public static void createBitmapDir(){
		File eCameraDir = new File(getSDCardPath() + "/eCamera");
		if (!eCameraDir.exists()) {
			eCameraDir.mkdirs();
		}
		File bitmapCacheDir = new File(getSDCardPath() + "/eCamera/notice_cache");
		if (!bitmapCacheDir.exists()) {
			bitmapCacheDir.mkdirs();
		}
	}
	
	/** 
	 * 计算sdcard上的剩余空间 
	 * @return 
	 */  
	public static int freeSpaceOnSd() {  
	    StatFs stat = new StatFs(getSDCardPath());  
	    double sdFreeMB = ((double)stat.getAvailableBlocks() * (double) stat.getBlockSize()) / ( 1024 *1024 );  
	    return (int) sdFreeMB;  
	}  
	
	/** 
	 * 修改文件的最后修改时间 
	 * @param dir 
	 * @param fileName 
	 */  
	@SuppressWarnings("unused")
	private void updateFileTime(String filePath) {  
	    File file = new File(filePath);         
	    long newModifiedTime = System.currentTimeMillis();  
	    file.setLastModified(newModifiedTime);  
	}  
	
	/** 
	 *计算存储目录下的文件大小，当文件总大小大于规定的CACHE_SIZE或者sdcard剩余空间小于FREE_SD_SPACE_NEEDED_TO_CACHE的规定 
	 * 那么删除40%最近没有被使用的文件 
	 * @param dirPath 
	 * @param filename 
	 */  
//	static int CACHE_SIZE = 50;
//	static int MB = 1024 * 1024;
//	static int FREE_SD_SPACE_NEEDED_TO_CACHE = 50 ;
	private static void removeCache(String dirPath) {  
	    File dir = new File(dirPath);  
	    File[] files = dir.listFiles();  
	    if (files == null) {  
	        return;  
	    }  
	    Log.i("", "文件个数："+files.length);  
//	    int dirSize = 0;  
//	    for (int i = 0; i < files.length;i++) {  
//	        dirSize += files[i].length();  
//	    }  
//	    if (dirSize > CACHE_SIZE * MB ||FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {  
//	        int removeFactor = (int) ((0.4 *files.length) + 1);  
//	        Log.i("", "Clear some expiredcache files ");  
//	        for (int i = files.length ; i > removeFactor; i--) {  
//	            files[i].delete();               
//	        }  
//	    }  
        for (int i = files.length ; i > 0; i--) {  
            files[i].delete();               
        }  
	}  
	
	static final int neededCacheSpace = 50;
	
	public static void saveBmpToSd(Bitmap bm, String filename) {  
        if (bm == null) {  
        	Log.e("", "bm == null");
            return;  
        }  
         //判断sdcard上的空间  
        if (neededCacheSpace >freeSpaceOnSd()) {  
        	removeCache(getSDCardPath() + File.separator + "eCamera" + File.separator + "notice_cache" + File.separator);
            return;  
        }  
        File file = new File(getSDCardPath() + File.separator + "eCamera" + File.separator + "notice_cache" + File.separator + filename);  
        try {  
            file.createNewFile();  
            OutputStream outStream = new FileOutputStream(file);  
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);  
            outStream.flush();  
            outStream.close();  
        } catch (FileNotFoundException e) {  
        	Log.e("saveBmpToSd","FileNotFoundException");
        } catch (IOException e) {  
        	Log.e("saveBmpToSd","IOException");
        }  
        Log.i("saveBmpToSd","create " + filename + " success");
    }  
	
	public static boolean isBitmapExist(String filename){
		if(filename == null){
			Log.e("isBitmapExist","filename == null");
			return false;
		}
		File f = new File(getBitmapCachePath() + filename);
		Log.e("isBitmapExist",f.exists()+"");
		return f.exists();
	}
	
	 
}
