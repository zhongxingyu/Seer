 package com.app.getconnected.activities;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.app.getconnected.R;
 
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TransportResultActivity extends BaseActivity {
 
 	private int page = 0;
 	private JSONArray itineraries;
 	TableLayout table = (TableLayout)findViewById(R.id.transport_result_table);	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_transport_result);
 		initLayout(R.string.title_activity_transport_result, true, true, true,
 				false);
 		String json = getIntent().getExtras().getString("json");
		JSONObject jObject;
 		try {
 			jObject = new JSONObject(json);
 			itineraries = jObject.getJSONArray("itineraries");
 		} catch (JSONException e) {
 			Toast.makeText(this, "Something went wrong =(", Toast.LENGTH_LONG).show();
 			return;
 		}
 		
 	}
 
 	private void initTable() {
 		JSONObject itinerariy = null;
 		try {	
 			for (int i = (page * 5); i < itineraries.length() && i < (page * 5 + 5); i++) {
 				itinerariy = itineraries.getJSONObject(i);
 				TableRow row = (TableRow)findViewById(R.id.transport_result_row);
 				setTextViews(row, itinerariy);
 				table.addView(row);
 			}
 		} catch (JSONException e) {
 			Toast.makeText(this, "Something went wrong =(", Toast.LENGTH_LONG).show();
 			return;
 		}
 	}
 	
 	private void setTextViews(TableRow row, JSONObject itinerariy) throws JSONException {
 		TextView departure = (TextView)row.findViewById(R.id.transport_result_text_departure);
 		TextView duration = (TextView)row.findViewById(R.id.transport_result_text_duration);
 		TextView arival = (TextView)row.findViewById(R.id.transport_result_text_arival);
 		TextView transfers = (TextView)row.findViewById(R.id.transport_result_text_transfers);
 		departure.setText(itinerariy.getString("duration"));
 		duration.setText(itinerariy.getString("startTime"));
 		arival.setText(itinerariy.getString("endTime"));
 		transfers.setText(itinerariy.getInt("transfers"));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.transport_result, menu);
 		return true;
 	}
 
 	private void nextPage() {
 		page++;
 		table.removeAllViews();
 		this.initTable();
 		
 		
 		if((page * 5) >= itineraries.length()){
 			Button nextButton = (Button) findViewById(R.id.transport_results_next);
 			nextButton.setVisibility(View.INVISIBLE);
 		}
 		
 		
 		// set next page
 	}
 
 	private void prefPage() {
 		page--;
 		if (page <= 0) {
 			Button prefButton = (Button) findViewById(R.id.transport_results_pref);
 			prefButton.setVisibility(View.INVISIBLE);
 			return;
 		}
 
 		// set previous page
 	}
 
 }
