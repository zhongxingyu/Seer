 package com.example.glucoseapp;
 
 
 import java.util.Calendar;
 
 import org.apache.http.entity.StringEntity;
 import org.json.JSONObject;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class AdjustMealActivity extends Activity {
 	private String ServingSizeGrams;
 	private FoodItem food_item;
 	private Calendar calendar;
 	private Context context;
 	private String g_load;
 	private String carb_p_serv;
 	private ProgressDialog pDialog;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_adjust_meal);
 		
 		calendar = Calendar.getInstance();
 		context = this.getApplicationContext();
 		
 		food_item = (FoodItem) getIntent().getParcelableExtra("FOOD_ITEM");
 		ServingSizeGrams = food_item.getServingSizeGrams();
 		g_load = food_item.getGlycemicLoad();	
 		carb_p_serv = food_item.getAvailCarbServing();
 		
 		
 		String user_num_serving = food_item.getAdjustMealServing();
 		
 		TextView servingSize = (TextView) findViewById(R.id.selected_serving_size);
 		
		servingSize.setText("Serving Size: " + ServingSizeGrams + " Grams");
 		
 		TextView user_serving = (TextView) findViewById(R.id.user_num_servings);
 		
 		user_serving.setText("inputted servings: " + user_num_serving);
 		
 		final EditText adjust_user_serving = (EditText) findViewById(R.id.adjust_num_servings);
 		
		adjust_user_serving.setHint( user_num_serving);
 		
 		final Button button = (Button) findViewById(R.id.submit_adjusted_serving);
 
 
 		button.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				final String num_servings;
 				num_servings = adjust_user_serving.getText().toString();
 				food_item.setAdjustMealServing(num_servings);
 
 				try {
 					AsyncHttpClient client = new AsyncHttpClient();
 					JSONObject jsonParams = new JSONObject();
 					final String timestamp = String.valueOf(calendar.getTimeInMillis());
 					jsonParams.put("carb_p_serv", carb_p_serv);
 					jsonParams.put("num_serv", num_servings);
 					jsonParams.put("g_load", g_load);
 					jsonParams.put("p_type", "0");
 					jsonParams.put("bw", "70");
 
 					StringEntity entity = new StringEntity(jsonParams.toString());
 
 
 					client.put(context,"http://198.61.177.186:8080/virgil/data/glucoseapp/menu/1/"+timestamp+"/",entity,null,new AsyncHttpResponseHandler() {
 						@Override
 						public void onSuccess(String response) {
 							Log.d("POST:","Success HTTP PUT to POST ColumnFamily");
 							System.out.println("Success HTTP PUT to POST ColumnFamily");
 							pDialog = new ProgressDialog(AdjustMealActivity.this);
 							pDialog.setMessage("Computing Graph...");
 							pDialog.show();
 
 							AsyncHttpClient putClient = new AsyncHttpClient();
 
 							putClient.get("http://198.61.177.186:5000/user/1/"+timestamp, new AsyncHttpResponseHandler() {
 								@Override
 								public void onSuccess(String response) {
 									Log.d("GET:","Success GET from Flask");
 									System.out.println("Success GET from Flask");
 									pDialog.dismiss();
 									Intent i = new Intent(context, GraphActivity.class);
 									i.putExtra("FOOD_ITEM", food_item);
 									startActivity(i);
 									finish();
 								}
 							});
 
 						}
 					});
 
 				} catch (Exception e) {
 					System.out.println("Failed HTTP PUT");
 				} 
 			}
 		});
 	}
 	
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.adjust_meal, menu);
 		return true;
 	}
 
 }
