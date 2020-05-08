 package org.sdu.view.taskshow;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.sdu.dao.UserDao;
 import org.sdu.dbaction.Action;
 import org.sdu.dbaction.TaskAction;
 import org.sdu.gis.R;
 import org.sdu.pojo.Task;
 import org.sdujq.map.PullToRefreshListView;
 import org.sdujq.map.PullToRefreshListView.OnLoadMoreListener;
 import org.sdujq.map.PullToRefreshListView.OnRefreshListener;
 
 import android.R.integer;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.DataSetObserver;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class TaskShowMhActivity extends Activity {
 
 	PullToRefreshListView lv;
 	public static TaskShowMhActivity currentActivity;
 	TaskShowAdapter adapter;
 	public Handler h = new Handler() {
 		@Override
 		public void handleMessage(Message m) {
 			refreshData();
 		}
 	};
 
 	public void refreshData() {
 		adapter=new TaskShowAdapter();
 		this.lv.setAdapter(adapter);
 	}
 
 	@Override
 	public void onCreate(Bundle b) {
 		super.onCreate(b);
 		setContentView(R.layout.taskshowmh);
 		lv = (PullToRefreshListView) findViewById(R.id.listView1);
 		currentActivity = this;
 		adapter=new TaskShowAdapter();
 		lv.setAdapter(adapter);
 		lv.setOnRefreshListener(new OnRefreshListener() {
 			@Override
 			public void onRefresh() {
 				new GetDataTask().execute();
 			}
 		});
 		lv.setOnLoadMoreListener(new OnLoadMoreListener() {
 			
 			@Override
 			public void onLoadMore() {
 				adapter.loadMore();
 			}
 		});
 	}
 
 	class TaskShowAdapter extends BaseAdapter {
 		int limit = 6;
 		int offset = 0;
 		Drawable d1, d2, d3;
 		List<Task> data;
 		List<Integer> idList;
 
 		public TaskShowAdapter() {
 			TaskAction tAction = new TaskAction(TaskShowMhActivity.this);
 			try {
 				idList =  tAction.getTaskIds(Action.currentUser.getId() + "");
				data = tAction.getList(idList.subList(offset,Math.min(limit, idList.size())));
 			} catch (Exception e) {
 				data = new ArrayList<Task>();
 			}
 			d1 = getResources().getDrawable(R.drawable.state_1);
 			d2 = getResources().getDrawable(R.drawable.state_2);
 			d3 = getResources().getDrawable(R.drawable.state_3);
 		}
 
 		public void loadMore(){
 			if(limit>idList.size()){
 				return;
 			}
 			limit+=6;
 			TaskAction tAction = new TaskAction(TaskShowMhActivity.this);
 			try {
 				idList =  tAction.getTaskIds(Action.currentUser.getId() + "");
 				data = tAction.getList(idList.subList(offset,Math.min(limit, idList.size())));
 				for(Task i:data){
 					Log.e("qq", ""+i.getId());
 				}
 			} catch (Exception e) {
 				data = new ArrayList<Task>();
 			}
 			notifyDataSetChanged();
 		}
 		@Override
 		public int getCount() {
 			return data.size();
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			return data.get(arg0);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			/*if (convertView != null&&(((Integer)convertView.getTag())==data.size())) {
 				return convertView;
 			}*/
 			View v = View.inflate(TaskShowMhActivity.this,
 					R.layout.task_show_item, null);
 			TextView tv1 = (TextView) v.findViewById(R.id.cake_name);
 			TextView tv2 = (TextView) v.findViewById(R.id.cake_price);
 			TextView tv3 = (TextView) v.findViewById(R.id.cake_brand);
 			TextView tv4 = (TextView) v.findViewById(R.id.cake_timestamp);
 			ImageView icon = (ImageView) v.findViewById(R.id.task_icon);
 			final Task t = data.get(position);
 			if (t.getState().equals("3")) {
 				icon.setImageDrawable(d3);
 			} else if (t.getState().equals("2")) {
 				icon.setImageDrawable(d2);
 			} else if (t.getState().equals("1")) {
 				icon.setImageDrawable(d1);
 			}
 			tv1.setText(":" + t.getId());
 			tv2.setText(":" + t.getTaskType());
 			tv3.setText("·:" + t.getRoadName());
 			String userName = "";
 			try {
 				userName = (new UserDao(TaskShowMhActivity.this)).get(
 						t.getCreatePersonId()).getName();
 			} catch (Exception e) {
 				userName = "";
 			}
 			tv4.setText("Ա:" + userName + " ʱ:" + t.getRealseTime());
 			v.setTag(data.size());
 			v.setOnClickListener(new OnClickListener() {
 			
 				@Override
 				public void onClick(View v) {
 					Intent it = new Intent();
 					it.setClass(TaskShowMhActivity.this, TaskDetialShow.class);
 					it.putExtra("one", t.getId() + "");
 					TaskShowMhActivity.this.startActivity(it);
 				}
 			});
 			return v;
 		}
 		@Override
 		public void unregisterDataSetObserver(DataSetObserver observer) {
 			if (observer != null) {
 				super.unregisterDataSetObserver(observer);
 			}
 		}
 	}
 
 	private class GetDataTask extends AsyncTask<Void, Void, List<Task>> {
 
 		@Override
 		protected List<Task> doInBackground(Void... params) {
 			List<Task> lst = new ArrayList<Task>();
 			try {
 				Thread.sleep(500);
 				h.sendEmptyMessage(0);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			return lst;
 		}
 
 		@Override
 		protected void onPostExecute(List<Task> data) {
 			lv.onRefreshComplete();
 
 			super.onPostExecute(data);
 		}
 	}
 }
