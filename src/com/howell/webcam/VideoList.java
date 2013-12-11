package com.howell.webcam;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.howell.webcam.R;
import com.howell.datetime.JudgeDate;
import com.howell.datetime.ScreenInfo;
import com.howell.datetime.WheelMain;
import com.howell.invite.Client;
import com.howell.webcam.MyListViewWithFoot.OnRefreshListener;
import com.howell.webcam.player.PlayerActivity;

public class VideoList extends ListActivity implements OnItemClickListener {
	
    public static Client client;
    private Device dev;
//    private SoapManager mSoapManager;
//    private LoginResponse mResponse;
    private ArrayList<VODRecord> mList;
    private VideoListAdapter adapter;
    private MyListViewWithFoot mListView;
    private static final int SETADAPTER = 1;
    private static final int SETENABLE = 2;
    private ImageButton mSearch;
    private com.howell.datetime.WheelMain wheelMain;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private int year,month,day;
    private ProgressDialog pd;
    private String lastRefreshStartTime,lastRefreshEndTime;
    
    private Activities mActivities;
    private PlaybackUtils utils;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_list);
        mActivities = Activities.getInstance();
    	mActivities.getmActivityList().add(VideoList.this);
    	
    	utils = new PlaybackUtils();
        mSearch = (ImageButton)findViewById(R.id.ib_search);
        mSearch.setEnabled(false);
        mSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mList = new ArrayList<VODRecord>();
				LayoutInflater inflater=LayoutInflater.from(VideoList.this);
				String country = getResources().getConfiguration().locale.getCountry(); 
//				//able.equals("CN");
				final View timepickerview=inflater.inflate(R.layout.timepicker, null);
				ScreenInfo screenInfo = new ScreenInfo(VideoList.this);
				wheelMain = new WheelMain(timepickerview,country);
				wheelMain.screenheight = screenInfo.getHeight();
				Calendar calendar = Calendar.getInstance();
				String time = (calendar.get(Calendar.YEAR) + "-" +
	  				    (calendar.get(Calendar.MONTH) + 1 )+ "-" +
	  				    calendar.get(Calendar.DAY_OF_MONTH) + "");
				if(JudgeDate.isDate(time, "yyyy-MM-dd")){
					try {
						calendar.setTime(dateFormat.parse(time));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				year = calendar.get(Calendar.YEAR);
				month = calendar.get(Calendar.MONTH) ;
				day = calendar.get(Calendar.DAY_OF_MONTH);
				/*hour = calendar.get(Calendar.HOUR_OF_DAY);
				minute = calendar.get(Calendar.MINUTE);
				second = calendar.get(Calendar.SECOND);*/
				
				wheelMain.initDateTimePicker(year,month,day);
				
				new AlertDialog.Builder(VideoList.this)
				.setTitle(getResources().getString(R.string.select_date))
				.setView(timepickerview)
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
//						now.setText(wheelMain.getTime());
						//"yyyy-MM-dd'T'HH:mm:ss"
						pd = new ProgressDialog(VideoList.this);  
				        pd.setTitle(getResources().getString(R.string.load_data)+"...");   //锟斤拷锟矫憋拷锟斤拷  
				        pd.setMessage(getResources().getString(R.string.please_wait)+"..."); //锟斤拷锟斤拷body锟斤拷息  
				        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER); //锟斤拷锟矫斤拷锟斤拷锟斤拷锟绞斤拷锟�锟斤拷锟斤拷锟�
						pd.show();
						new AsyncTask<Void, Void, Void>() {
							protected Void doInBackground(Void... params) {
//								mSearch.setEnabled(false);
//								mList.clear();
								int round = 6;
//								adapter.map.clear();
								String endTime = wheelMain.getEndTime();
								lastRefreshEndTime = endTime;
								lastRefreshStartTime = wheelMain.getStartTime(endTime);
								
					            //----
					            for(int i = 0 ; i < round ; i++){
					            	mList = utils.getVideoList(client, lastRefreshStartTime,lastRefreshEndTime);
					            	if(mList.size() == 0){
					            		Date newStartDate = TimeTransform.StringToDate(lastRefreshStartTime);
						        		Date newEndDate = TimeTransform.StringToDate(lastRefreshEndTime);
						        		lastRefreshStartTime = TimeTransform.reduceTenDays(newStartDate);
						        		lastRefreshEndTime = TimeTransform.reduceTenDays(newEndDate);
						            }else{
						            	break;
						            }
				            	}
					            adapter.mAdapterList = mList;
//								getVideoList(wheelMain.getStartTime(endTime),endTime);
								return null;
							}

							@Override
							protected void onPostExecute(Void result) {
								try{
									myHandler.sendEmptyMessage(SETADAPTER);
									pd.dismiss();
								}catch (Exception e) {
									// TODO: handle exception
								}
							}
						}.execute();
					}
				})
				.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.show();
			}
		});
        
        getListView().setOnItemClickListener(this);
        adapter = new VideoListAdapter(this);
        mList = new ArrayList<VODRecord>();
        setListAdapter(adapter);
        mListView = (MyListViewWithFoot)findViewById(android.R.id.list);
        mListView.setEnabled(false);
        mListView.setonRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
//				
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
//						int ret = 0;
						mList = new ArrayList<VODRecord>();
						int round = 6;
						SimpleDateFormat foo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			            Date endDate = new Date();
			            Date startDate = new Date(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000);
			            lastRefreshEndTime = foo.format(endDate);
			            lastRefreshStartTime = foo.format(startDate);
			            //----
			            for(int i = 0 ; i < round ; i++){
			            	mList = utils.getVideoList(client, lastRefreshStartTime,lastRefreshEndTime);
			            	if(mList.size() == 0){
			            		Date newStartDate = TimeTransform.StringToDate(lastRefreshStartTime);
				        		Date newEndDate = TimeTransform.StringToDate(lastRefreshEndTime);
				        		lastRefreshStartTime = TimeTransform.reduceTenDays(newStartDate);
				        		lastRefreshEndTime = TimeTransform.reduceTenDays(newEndDate);
				            }else{
				            	break;
				            }
		            	}
			            adapter.mAdapterList = mList;
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						try{
							myHandler.sendEmptyMessage(SETADAPTER);
						}catch (Exception e) {
							// TODO: handle exception
						}
					}

				}.execute();
			}
			
			@Override
			public void onFirstRefresh() {
				// TODO Auto-generated method stub
				mList = new ArrayList<VODRecord>();
				int round = 6;
//				int ret = 0;
				try{
			        Intent intent = getIntent();
			        dev = (Device) intent.getSerializableExtra("Device");
			        client = new Client(dev);
//			        mSoapManager = SoapManager.getInstance();
//			        mResponse = mSoapManager.getLoginResponse();
			        Log.e("---------->>>>", "0");
		        	Log.e("---------->>>>", "0.1");
		        	
		        	//----
		        	SimpleDateFormat foo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		            Date endDate = new Date();
		            Date startDate = new Date(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000);
		            lastRefreshEndTime = foo.format(endDate);
		            lastRefreshStartTime = foo.format(startDate);
		            //----
		            for(int i = 0 ; i < round ; i++){
		            	mList = utils.getVideoList(client, lastRefreshStartTime,lastRefreshEndTime);
		            	if(mList.size() == 0){
		            		Date newStartDate = TimeTransform.StringToDate(lastRefreshStartTime);
			        		Date newEndDate = TimeTransform.StringToDate(lastRefreshEndTime);
			        		lastRefreshStartTime = TimeTransform.reduceTenDays(newStartDate);
			        		lastRefreshEndTime = TimeTransform.reduceTenDays(newEndDate);
			            }else{
			            	break;
			            }
	            	}
		            adapter.mAdapterList = mList;
		            myHandler.sendEmptyMessage(SETADAPTER);
		        }catch (Exception e) {
					// TODO: handle exception
				}
			}
			int position = 0;
			@Override
			public void onFootRefresh() {
				// TODO Auto-generated method stub
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						int round = 6;
						ArrayList<VODRecord> mTemp = utils.getMoreVideoList(client, lastRefreshStartTime, lastRefreshEndTime);
						System.out.println("foot:"+mTemp.size());
						System.out.println("foot:"+lastRefreshStartTime+","+lastRefreshEndTime);
						for(int i = 0 ; i < round ; i++){
							if(mTemp.size() == 0){
								Date newStartDate = TimeTransform.StringToDate(lastRefreshStartTime);
				        		Date newEndDate = TimeTransform.StringToDate(lastRefreshEndTime);
				        		lastRefreshStartTime = TimeTransform.reduceTenDays(newStartDate);
				        		lastRefreshEndTime = TimeTransform.reduceTenDays(newEndDate);
								mTemp = utils.getVideoList(client, lastRefreshStartTime, lastRefreshEndTime);
								System.out.println("foot111:"+mTemp.size());
								System.out.println("foot111:"+lastRefreshStartTime+","+lastRefreshEndTime);
							}
							if(mTemp.size() > 0){
								System.out.println("foot222:"+mTemp.size());
								System.out.println("foot222:"+lastRefreshStartTime+","+lastRefreshEndTime);
								mList.addAll(mTemp);
								utils.addTitleFlag(mList);
								break;
							}
						}
						adapter.mAdapterList = mList;
						position = mList.size()/2;
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						try{
							adapter.notifyDataSetChanged();
							mListView.onFootRefreshComplete();
							mListView.setSelection(position);
						}catch (Exception e) {
							// TODO: handle exception
						}
					}

				}.execute();
			}
		});
    }
	
    private Handler myHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		// TODO Auto-generated method stub
    		super.handleMessage(msg);
    		if(msg.what == SETADAPTER){
//    			setListAdapter(adapter);
    			adapter.notifyDataSetChanged();
    			mListView.onRefreshComplete();
    			mListView.setSelection(0);
    			mSearch.setEnabled(true);
    			mListView.setEnabled(true);
    		}
    		if(msg.what == SETENABLE){
//    			setListAdapter(adapter);
    			mSearch.setEnabled(false);
    			mListView.setEnabled(false);
    		}
    	}
    };
    
    public class VideoListAdapter extends BaseAdapter {

        private Context mContext;
        private VODRecord record;
        private ArrayList<VODRecord> mAdapterList;

        public VideoListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mAdapterList == null ? 0 : mAdapterList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mAdapterList == null ? null : mAdapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
//        	Log.e("---------->>>>", "getView");
//        	System.out.println("position"+position);
			record = (VODRecord) getItem(position);
            record.setDevice(dev);
            System.out.println(record.toString());
			ViewHolder holder = null;
            if (convertView == null) {
            	LayoutInflater layoutInflater = LayoutInflater.from(mContext);
				convertView = layoutInflater.inflate(R.layout.video_item, null);
				holder = new ViewHolder();
				
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
				convertView.setTag(holder);
            }else{
            	holder = (ViewHolder)convertView.getTag();
            }
            System.out.println("hasTitle?"+record.hasTitle());
            if(position == 0 || record.hasTitle()){
            	String date = record.getTimeZoneStartTime().substring(0, 10);
            	holder.alpha.setText(date);
            	holder.alpha.setVisibility(View.VISIBLE);
            }else{
            	holder.alpha.setVisibility(View.GONE);
            }
            holder.name.setText(record.getTimeZoneStartTime().substring(11) + " --> "
                    + record.getTimeZoneEndTime().substring(11));
            System.out.println("isWatched?"+record.isWatched());
            if(record.isWatched()){
            	holder.name.setTextColor(Color.GRAY);
            }else{
            	holder.name.setTextColor(Color.BLACK);
            }
            return convertView;
        }
    }
    
    public static class ViewHolder {
	    public TextView name,alpha;
	}
    
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
    	adapter.mAdapterList.get((int)arg3).setWatched(true);
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("arg", adapter.mAdapterList.get((int)arg3));
        startActivity(intent);
        TextView tvName = (TextView)(arg1).findViewById(R.id.name);
		tvName.setTextColor(Color.GRAY);
    }
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	Log.e("VIDEO LIST", "onStop");
    	super.onStop();
    	//adapter.map.clear();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	mActivities.getmActivityList().remove(VideoList.this);
    	mActivities.toString();
    }
    
    @Override
    protected void onRestart() {
    	// TODO Auto-generated method stub
    	Log.e("VIDEO LIST", "onRestart");
    	super.onRestart();
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	Log.e("VIDEO LIST", "onPause");
    	super.onPause();
//    	for(Activity a:mActivities.getmActivityList()){
//    		a.finish();
//    	}
    }
}
