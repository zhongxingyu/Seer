 package com.seawolfsanctuary.tmt.stats;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.seawolfsanctuary.tmt.R;
 
 public class StatsActivity extends ListActivity {
 
	private String[] names = { "Journeys by Month" };
	private String[] activities = { "stats.JourneysByMonth" };
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setListAdapter(new ArrayAdapter<String>(this,
 				R.layout.stats_activity_list, names));
 
 		ListView lv = getListView();
 		registerForContextMenu(lv);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				String className = "com.seawolfsanctuary.tmt."
 						+ activities[position];
 				try {
 					Intent intent;
 					intent = new Intent(view.getContext(), Class
 							.forName(className));
 					startActivity(intent);
 				} catch (ClassNotFoundException e) {
 					Toast.makeText(
 							getBaseContext(),
 							"Could not launch the requested activity: "
 									+ className, Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 	}
 }
