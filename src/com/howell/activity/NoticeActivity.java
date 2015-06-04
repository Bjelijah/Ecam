package com.howell.activity;

import java.util.ArrayList;

import org.kobjects.base64.Base64;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.howell.webcam.R;
import com.howell.ehlib.MyListView;
import com.howell.ehlib.MyListView.OnRefreshListener;
import com.howell.protocol.GetPictureReq;
import com.howell.protocol.GetPictureRes;
import com.howell.protocol.NoticeList;
import com.howell.protocol.QueryNoticesReq;
import com.howell.protocol.QueryNoticesRes;
import com.howell.protocol.SoapManager;
/**
 * @author 霍之昊 
 *
 * 类说明
 */
public class NoticeActivity extends Activity implements OnRefreshListener{ 
	private MyListView listview;
	private ArrayList<NoticeList> list;
	private SoapManager mSoapManager;
	private NoticeAdapter adapter;
	
	private static final int ONFIRSTREFRESHDOWN = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notice_activity);
		mSoapManager = SoapManager.getInstance();
		listview = (MyListView)findViewById(R.id.notice_listview);
		listview.setonRefreshListener(this);
		adapter = new NoticeAdapter(this);
		listview.setAdapter(adapter);
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFirstRefresh() {
		// TODO Auto-generated method stub
		QueryNoticesRes res = mSoapManager.getQueryNoticesRes(new QueryNoticesReq(mSoapManager.getLoginResponse().getAccount(),mSoapManager.getLoginResponse().getLoginSession()));
		list = res.getNodeList();
		
		handler.sendEmptyMessage(ONFIRSTREFRESHDOWN);
	}
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case ONFIRSTREFRESHDOWN:
				listview.onRefreshComplete();
				adapter.notifyDataSetChanged();
				break;
			}
		}
	};
	
	class ShowImagesTask extends AsyncTask<Void, Integer, Void> {
		private ImageView iv1,iv2,iv3,iv4;
		private int position;
		private GetPictureRes res = null;
		
		public ShowImagesTask(ImageView iv1, ImageView iv2, ImageView iv3,
				ImageView iv4, int position) {
			super();
			this.iv1 = iv1;
			this.iv2 = iv2;
			this.iv3 = iv3;
			this.iv4 = iv4;
			this.position = position;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			NoticeList notice = list.get(position);
			System.out.println("pictureid:"+notice.getPictureID().get(0));
			GetPictureReq req = new GetPictureReq(mSoapManager.getLoginResponse().getAccount(),mSoapManager.getLoginResponse().getLoginSession(),notice.getPictureID().get(0));
			res = mSoapManager.getGetPictureRes(req);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(res.getResult() != null && res.getResult().equals("OK")){
				String picture = new String(Base64.decode(res.getPicture()));
				System.out.println("picture:"+picture);
			}
		}
	}
	
    public class NoticeAdapter extends BaseAdapter {

        private Context mContext;

        public NoticeAdapter(Context context) {
            mContext = context;
        }
        
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list == null ? 0 : list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return list == null ? null : list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
        	System.out.println("getView:"+position);
        	ViewHolder holder = null;
            if (convertView == null) {
            	LayoutInflater layoutInflater = LayoutInflater.from(mContext);
				convertView = layoutInflater.inflate(R.layout.notice_item, null);
				holder = new ViewHolder();
				holder.title = (TextView)convertView.findViewById(R.id.notice_item_title);
				holder.message = (TextView)convertView.findViewById(R.id.notice_item_message);
				
				holder.iv1 = (ImageView)convertView.findViewById(R.id.notice_item_imageView1);
				holder.iv2 = (ImageView)convertView.findViewById(R.id.notice_item_imageView2);
				holder.iv3 = (ImageView)convertView.findViewById(R.id.notice_item_imageView3);
				holder.iv4 = (ImageView)convertView.findViewById(R.id.notice_item_imageView4);
                convertView.setTag(holder);
                
            }else{
            	holder = (ViewHolder)convertView.getTag();
            }
            NoticeList notice = list.get(position);
            holder.title.setText(notice.getName());
            holder.message.setText(notice.getMessage());
            ShowImagesTask task = new ShowImagesTask(holder.iv1, holder.iv2, holder.iv3, holder.iv4, position);
            task.execute();
			return convertView;
        }
    }
    
	public class ViewHolder {
		public ImageView iv1,iv2,iv3,iv4;
		public TextView title,message;
	}

}
