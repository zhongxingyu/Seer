 package com.inc.im.serptracker.adapters;
 
 import java.util.ArrayList;
 import com.inc.im.serptracker.R;
 import com.inc.im.serptracker.data.Keyword;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * 
  * Custom adapter for main activity result list (added to include up/down arrow
  * and different colors for result numbers)
  * 
  */
 
 public class MainActivityListAdapter extends BaseAdapter {
 
 	private Context con;
 	private ArrayList<Keyword> input;
 
 	public MainActivityListAdapter(Context con, ArrayList<Keyword> input) {
 		this.con = con;
 		this.input = input;
 	}
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return input.size();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		// TODO Auto-generated method stub
 		return arg0;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		// inflate from layout
 		// bind values
 		View v = convertView;
 
 		LayoutInflater inf = (LayoutInflater) con
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		v = inf.inflate(R.layout.main_activity_listview_item, null);
 
 		Keyword k = input.get(position);
 
 		TextView tvKeyword = (TextView) v.findViewById(R.id.textView1);
 		TextView tvRank = (TextView) v.findViewById(R.id.textView2);
 		TextView tvRankOld = (TextView) v.findViewById(R.id.textView3);
 
 		String keyword = k.keyword;
 		String newRank = Integer.toString(k.newRank);
 		String oldRank = Integer.toString(k.oldRank);
 		int rankChange = k.oldRank - k.newRank;
 
 		tvRankOld.setTextColor(R.color.dred);
 
 		ImageView iv = (ImageView) v.findViewById(R.id.imageView1);
 
 		// debugg
 		// k.newRank = new Random().nextInt(100);
 		// k.oldRank = new Random().nextInt(100);
 		// Log.i("MY", k.keyword + " newRank:" + k.newRank + " oldRank:"
 		// + k.oldRank);
 
 		// k.oldRank != 0 is for when comes from edit page or just entered
 		// keyword first run
 		if (k.newRank > k.oldRank && k.oldRank != 0) {
 			// Log.d("MY", "iv.setBackgroundResource");
 			iv.setBackgroundResource(R.drawable.down);
 			tvRankOld.setVisibility(View.VISIBLE);
 			tvRankOld.setTextColor(con.getResources().getColor(R.color.dred));
 		} else if (k.newRank < k.oldRank) {
 
 			// Log.d("MY", "iv.setBackgroundResource");
 			iv.setBackgroundResource(R.drawable.up);
 			tvRankOld.setVisibility(View.VISIBLE);
 			tvRankOld.setTextColor(con.getResources().getColor(R.color.green));
 		} else {
 
 			// Log.d("MY", "iv.setVisibility(View.GONE)");
 			iv.setVisibility(View.GONE);
 			tvRankOld.setVisibility(View.GONE);
 		}
 
 		tvKeyword.setText(keyword);
 		tvRank.setText(newRank);
 		tvRankOld.setText(Math.abs(rankChange) + "");
 
 		// special cases
 		// 0 - empty field in DB - new
 		// -1 - not ranked in top 100
 		// -2 - error getting data
 
 		// #SPECIAL CASE #1
 		if (k.newRank == 0) {
 			if (k.oldRank == 0) {
 
 				// just added
 				tvRank.setVisibility(View.GONE);
 				// Log.d("MY", "iv.setVisibility(View.GONE)");
 				iv.setVisibility(View.GONE);
 				tvRankOld.setVisibility(View.GONE);
 			} else {
 				// has been used before (has oldrank) and started from
 				// mainscreen first time
 				tvRank.setText(oldRank);
 				// Log.d("MY", "iv.setVisibility(View.GONE)");
 				iv.setVisibility(View.GONE);
 				tvRankOld.setVisibility(View.GONE);
 
 				if (k.oldRank == -1) {
 					// Log.d("MY", "iv.setVisibility(View.GONE)");
 					iv.setVisibility(View.GONE);
 					tvRankOld.setVisibility(View.GONE);
 					tvRank.setText("-");
 				}
 
 				if (k.oldRank == -2) {
 					// hide image + old rank
 					// Log.d("MY", "iv.setVisibility(View.GONE)");
 					iv.setVisibility(View.GONE);
 					tvRankOld.setVisibility(View.GONE);
 
 					// display error msg
					tvRank.setText(con.getString(R.string.error));
 					tvRank.setTextColor(con.getResources().getColor(
 							R.color.dred));
 				}
 			}
 
 		}
 
 		// #SPECIAL CASE #2
 		// replace -1 with "-"
 		if (k.newRank == -1) {
 			// Log.d("MY", "iv.setVisibility(View.GONE)");
 			iv.setVisibility(View.GONE);
 			tvRankOld.setVisibility(View.GONE);
 			tvRank.setText("-");
 		}
 
 		// #SPECIAL CASE #3
 		// replace -2 with "error"
 		if (k.newRank == -2) {
 			// hide image + old rank
 			// Log.d("MY", "iv.setVisibility(View.GONE)");
 			iv.setVisibility(View.GONE);
 			tvRankOld.setVisibility(View.GONE);
 
 			// display error msg
 			tvRank.setText("error");
 			tvRank.setTextColor(con.getResources().getColor(R.color.dred));
 		}
 
 		return v;
 	}
 }
