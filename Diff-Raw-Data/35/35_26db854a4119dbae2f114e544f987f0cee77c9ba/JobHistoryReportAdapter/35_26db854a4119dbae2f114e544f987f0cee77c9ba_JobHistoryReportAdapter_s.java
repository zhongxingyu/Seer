 package com.skybot.adapters;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.skybot.activities.JobHistoryReportActivity;
 import com.skybot.activities.R;
 import com.skybot.connection.connection.BaseNetworkManager;
 import com.skybot.connection.connection.helper.RequestCreator;
 import com.skybot.util.Constants;
 
 public class JobHistoryReportAdapter extends BaseAdapter {
 
 	private JobHistoryReportActivity activity;
 	public ArrayList<HashMap<String, String>> data;
 	private static LayoutInflater inflater = null;
 	HashMap m = new HashMap();
 
 	public JobHistoryReportAdapter(JobHistoryReportActivity a,
 			ArrayList<HashMap<String, String>> d) {
 		activity = a;
 		data = d;
 		inflater = (LayoutInflater) activity
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 	}
 
 	public int getCount() {
 		return data.size();
 	}
 
 	public Object getItem(int position) {
 		return position;
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View vi = convertView;
 		if (convertView == null)
 			vi = inflater.inflate(R.layout.job_history_report_layout, null);
 
 		TextView file_name = (TextView) vi.findViewById(R.id.title);
 		TextView id = (TextView) vi.findViewById(R.id.report_id);
 		TextView servercopiedtime = (TextView) vi
 				.findViewById(R.id.servercopiedtime);
 		ImageView image = (ImageView) vi.findViewById(R.id.list_image);
 
 		try {
 			if (data != null && !data.isEmpty()) {
 
 				m = data.get(position);
 
 				if (m.get("status").toString().equals("Finished")) {
 					image.setImageResource(R.drawable.blank_badge_green);
 				} /*
 				 * else if (m.get("status").toString().equals("S")) {
 				 * image.setImageResource(R.drawable.blank_badge_orange); }
 				 */
 
 				/*
 				 * else if (m.get("status").toString().equals("Failed")) {
 				 * image.setImageResource(R.drawable.blank_badge_red); }
 				 */
 
 				if (m.get("file_name") != null
 						&& m.get("copied_server_time_utc") != null) {
 
 					id.setText("ID: " + m.get("id").toString());
 					file_name.setText(m.get("file_name").toString());
 					servercopiedtime.setText("Server copied Time: "
 							+ m.get("copied_server_time_utc").toString());
 				}
 
 			}
 		} catch (Exception e) {
 			Log.e("Exception occured", e.toString());
 }
 			vi.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
					String pdfurl = "http://192.168.0.5:8008/skybot-scheduler/user_files/"
 							+ m.get("id") + "?download=true";
 
 					//Intent intent = new Intent(Intent.ACTION_VIEW);
 					//intent.setDataAndType(Uri.parse(pdfurl), "text/html");
 					//ViewTracker.getInstance().getCurrentContext()
 					//		.startActivity(intent);
 					// JobsHistoryReportAdapter.this.onClickAction("sadasdas");
 					
 					RequestCreator reqCreator = new RequestCreator();
 					
 					BaseNetworkManager man = new BaseNetworkManager();
 					man.constructConnectionAndHitFile("Report is downloaded", "Starting report download", pdfurl, activity, Constants.JOBHISTORYREPORT_VIEW, "");
 					
 					activity.showProgressDialog();
 				}
 
 			});
 
 		
 		return vi;
 	}
 
 }
