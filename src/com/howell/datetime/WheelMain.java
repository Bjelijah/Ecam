package com.howell.datetime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.view.View;

import com.android.howell.webcam.R;
import com.howell.webcam.TimeTransform;


public class WheelMain {

	private View view;
	private String country;
	private WheelView wv_year;
	private WheelView wv_month;
	private WheelView wv_day;
	private int wv_hours;
	private int wv_mins;
	private int wv_seconds;
	public int screenheight;
	private static int START_YEAR = 1990, END_YEAR = 2100;

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public static int getSTART_YEAR() {
		return START_YEAR;
	}

	public static void setSTART_YEAR(int sTART_YEAR) {
		START_YEAR = sTART_YEAR;
	}

	public static int getEND_YEAR() {
		return END_YEAR;
	}

	public static void setEND_YEAR(int eND_YEAR) {
		END_YEAR = eND_YEAR;
	}

	public WheelMain(View view,String country) {
		super();
		this.country = country;
		this.view = view;
		setView(view);
	}

	/**
	 * @Description: TODO 弹出日期时间选择�?
	 */
	public void initDateTimePicker(int year ,int month ,int day) {
//		int year = calendar.get(Calendar.YEAR);
//		int month = calendar.get(Calendar.MONTH);
//		int day = calendar.get(Calendar.DATE);

		// 添加大小月月份并将其转换为list,方便之后的判�?
		String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
		String[] months_little = { "4", "6", "9", "11" };

		final List<String> list_big = Arrays.asList(months_big);
		final List<String> list_little = Arrays.asList(months_little);

		// �?
		wv_year = (WheelView) view.findViewById(R.id.year);
		wv_year.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));// 设置"�?的显示数�?
		wv_year.setCyclic(true);// 可循环滚�?
		if(country.equals("CN"))
			wv_year.setLabel("��");// 添加文字
		wv_year.setCurrentItem(year - START_YEAR);// 初始化时显示的数�?

		// �?
		wv_month = (WheelView) view.findViewById(R.id.month);
		wv_month.setAdapter(new NumericWheelAdapter(1, 12));
		wv_month.setCyclic(true);
		if(country.equals("CN"))
			wv_month.setLabel("��");
		wv_month.setCurrentItem(month);

		// �?
		wv_day = (WheelView) view.findViewById(R.id.day);
		wv_day.setCyclic(true);
		// 判断大小月及是否闰年,用来确定"�?的数�?
		if (list_big.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 31));
		} else if (list_little.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 30));
		} else {
			// 闰年
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				wv_day.setAdapter(new NumericWheelAdapter(1, 29));
			else
				wv_day.setAdapter(new NumericWheelAdapter(1, 28));
		}
		if(country.equals("CN"))
			wv_day.setLabel("��");
		wv_day.setCurrentItem(day - 1);

		// 添加"�?监听
		OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int year_num = newValue + START_YEAR;
				// 判断大小月及是否闰年,用来确定"�?的数�?
				if (list_big
						.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(wv_month
						.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if ((year_num % 4 == 0 && year_num % 100 != 0)
							|| year_num % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		// 添加"�?监听
		OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int month_num = newValue + 1;
				// 判断大小月及是否闰年,用来确定"�?的数�?
				if (list_big.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if (((wv_year.getCurrentItem() + START_YEAR) % 4 == 0 && (wv_year
							.getCurrentItem() + START_YEAR) % 100 != 0)
							|| (wv_year.getCurrentItem() + START_YEAR) % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		wv_year.addChangingListener(wheelListener_year);
		wv_month.addChangingListener(wheelListener_month);

		// 根据屏幕密度来指定�?择器字体的大�?不同屏幕可能不同)
		int textSize = 0;
		textSize = (screenheight / 100) * 4;
		wv_day.TEXT_SIZE = textSize;
		wv_month.TEXT_SIZE = textSize;
		wv_year.TEXT_SIZE = textSize;

	}
	
	public String getEndTime() {
		String sb = new String();
		int year = (wv_year.getCurrentItem() + START_YEAR);
		int month = wv_month.getCurrentItem() + 1;
		int day = wv_day.getCurrentItem() + 1;
		String strYear = "",strMonth = "",strDay = "";
		if( year < 10 ){
			strYear = "0" + year;
		}else{
			strYear = "" + year;
		}
		if( month < 10 ){
			strMonth = "0" + month;
		}else{
			strMonth = "" + month;
		}
		if( day < 10 ){
			strDay = "0" + day;
		}else{
			strDay = "" + day;
		}
		sb=strYear+"-"
				+strMonth+"-"
				+strDay+"T"
				+"23:59:59";
				/*.append(String.valueOf(wv_hours)).append(":")
				.append(String.valueOf(wv_mins)).append(":")
				.append(String.valueOf(wv_seconds));*/
		
		java.util.Date date = null;
		SimpleDateFormat foo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		foo.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//Сд��mm��ʾ���Ƿ���  
        try {
			date=sdf.parse(sb);
			System.out.println(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        String dateTime = foo.format(date);
        System.out.println(dateTime);
		
		return dateTime;
	}
	
	public String getStartTime(String endTime) {
		Date date = TimeTransform.StringToDate(endTime);
		String dateTime = TimeTransform.reduceTenDays(date);
		System.out.println(dateTime);
		return dateTime;
	}
}
