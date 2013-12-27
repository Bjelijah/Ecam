package com.howell.webcam;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.android.howell.webcam.R;

public class LocalFilesActivity extends Activity {
	private ArrayList<File> mFileList;
	private TableLayout table;
	private int imageWidth;
	private int imageHeight;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_files);
		imageWidth = PhoneConfig.getPhoneWidth(getApplicationContext())/3;
		imageHeight = imageWidth * 3 / 4;
		mFileList = new ArrayList<File>();
		table = (TableLayout)findViewById(R.id.table_for_local_files);
		File f = new File("/sdcard/eCamera");
		getFile(f);
System.out.println("pictures:"+mFileList.size());
		LinearLayout.LayoutParams lp = new TableRow.LayoutParams(imageWidth, imageHeight);
		lp.setMargins(0, 0, 0, 10);
		int j ;
		for(int i = 0 ; i < mFileList.size() ; i++){
			System.out.println(f.getPath()+"/"+mFileList.get(i).getName());
			TableRow row = new TableRow(LocalFilesActivity.this);
			row.setGravity(Gravity.CENTER);
			for(j = i ; j < i+3 ; j++){
				if(j == mFileList.size()){
		        	break;
		        }
				ImageView imageView = new ImageView(LocalFilesActivity.this);
	//			imageView.setLayoutParams(imgvwDimens);
				
				BitmapFactory.Options options = new BitmapFactory.Options();
		        options.inSampleSize = 2;
		        Bitmap bm = BitmapFactory.decodeFile(f.getPath()+"/"+mFileList.get(j).getName(), options);
		        // SET SCALETYPE
	//	        imageView.setScaleType(ScaleType.FIT_XY);
		        imageView.setImageBitmap(bm);
		        
		        imageView.setLayoutParams(lp);
				row.addView(imageView);
			}
			i = j - 1;
			table.addView(row);
		}
	}
	
	public ArrayList<File> getFile(File file){
		File[] fileArray = file.listFiles();
		for (File f : fileArray) {
			System.out.println(f.getName());
			if(f.isFile()){
				mFileList.add(f);
			}
		}
		return mFileList;
	}
}
