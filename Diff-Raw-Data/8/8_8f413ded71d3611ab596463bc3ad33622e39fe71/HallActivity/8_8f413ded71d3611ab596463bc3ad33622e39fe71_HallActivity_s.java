 package com.robertkcheung.laundrytime;
 
 import java.io.IOException;
 import java.util.Calendar;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.format.DateUtils;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.handmark.pulltorefresh.library.PullToRefreshBase;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
 import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
 
 
 public class HallActivity extends Activity {
 	public static int hallNum;
 	public Hall param1;
 	public int wminsLeft;
 	public int dminsLeft;
 	int totalw;
 	int totald;
 	String code;
 	PullToRefreshScrollView mPullRefreshScrollView;
 	ScrollView mScrollView;
 	SharedPreferences sp;
 
 @Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.reloadButton) {
 			try {
 				//new LoadHallInfo().execute();
 				mPullRefreshScrollView.setRefreshing(true);
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 		} else if (item.getItemId() == R.id.changePrefButton) {
 			try {
 				startActivity(new Intent(HallActivity.this,
 						SchoolActivity.class));
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 		}
 		else if (item.getItemId() == R.id.changePrefClearAlarm) {
 			try {
 				Editor e = sp.edit();
 				e.putBoolean("NotificationDSet", false);
 				e.putBoolean("NotificationWSet", false);
 				e.commit();
 				Toast t = Toast.makeText(getApplicationContext(),
 						"All pending laundry machine alarms cleared", Toast.LENGTH_LONG);
 				t.show();
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 		}
 
 		return true;
 	}
 
 @Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.main);
 		 sp = getSharedPreferences("schoolPref",
 				MODE_WORLD_READABLE);
 
 		 
 		LinearLayout washerLayout = (LinearLayout) findViewById(R.id.washerLayout);
 		
 		boolean isWNotifSet = sp.getBoolean("NotificationWSet", false);
 		if(System.currentTimeMillis()-sp.getLong("lastnotif", System.currentTimeMillis()+3700000)>3600000){
 			Editor e = sp.edit();
 			e.putBoolean("NotificationWSet", false);
 			e.putBoolean("NotificationDSet", false);
 			e.commit();
 		}
 		
 		TextView btnw = (TextView) findViewById(R.id.btnWasher);
 		
 		if(isWNotifSet){
 		btnw.setClickable(false);
 		btnw.setText("NOTIFICATION SET");
 		btnw.setBackgroundColor(getResources().getColor(
 				R.color.sudsdarkgray));
 		}
 		boolean isDNotifSet = sp.getBoolean("NotificationDSet", false);
 		TextView btnd = (TextView) findViewById(R.id.btnWasher);
 		
 		if(isDNotifSet){
 		btnd.setClickable(false);
 		btnd.setText("NOTIFICATION SET");
 		btnd.setBackgroundColor(getResources().getColor(
 				R.color.sudsdarkgray));
 		}
 
 		mPullRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.scrollV);
 		mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
 
 			@Override
 			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
 				String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
 				DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
 
 				// Update the LastUpdatedLabel
 				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
 				new GetDataTask().execute();
 			}
 		});
 
 		mScrollView = mPullRefreshScrollView.getRefreshableView();
 		
 		
 
 		hallNum = sp.getInt("myHall", -1);
 		code = sp.getString("code", "");
 		new LoadHallInfo().execute();
 		// Bundle bundle = this.getIntent().getExtras();
 		// Hall param1 = (Hall) bundle.getSerializable("param1");
 
 	}
 
 	@Override
 	public void onRestart(){
 		super.onResume();
 		boolean isWNotifSet = sp.getBoolean("NotificationWSet", false);
 		if(System.currentTimeMillis()-sp.getLong("lastnotif", System.currentTimeMillis()+3700000)>3600000){
 			Editor e = sp.edit();
 			e.putBoolean("NotificationWSet", false);
 			e.putBoolean("NotificationDSet", false);
 			e.commit();
 		}
 		
 		TextView btnw = (TextView) findViewById(R.id.btnWasher);
 		
 		if(isWNotifSet){
 		btnw.setClickable(false);
 		btnw.setText("NOTIFICATION SET");
 		btnw.setBackgroundColor(getResources().getColor(
 				R.color.sudsdarkgray));
 		}
 		boolean isDNotifSet = sp.getBoolean("NotificationDSet", false);
 		TextView btnd = (TextView) findViewById(R.id.btnWasher);
 		
 		if(isDNotifSet){
 		btnd.setClickable(false);
 		btnd.setText("NOTIFICATION SET");
 		btnd.setBackgroundColor(getResources().getColor(
 				R.color.sudsdarkgray));
 		}
 		mPullRefreshScrollView.setRefreshing(true);
 	}
 
 	private class GetDataTask extends AsyncTask<Void, Void, Void> {
 	
 		protected Void doInBackground(Void... ars) {
 			try {
 				HttpClient client = new DefaultHttpClient();
 				HttpGet get = new HttpGet(
 						"http://robertkcheung.com/laundrytime/laundry.php?request=GetHallList&code="
 								+ code);
 
 				ResponseHandler<String> responseHandler = new BasicResponseHandler();
 				JSONObject jsonob = new JSONObject(client.execute(get,
 						responseHandler));
 				// JSONArray ja = new JSONArray(client.execute(get,
 				// responseHandler));
 				JSONArray ja = jsonob.getJSONArray("halls");
 
 				JSONObject jo = (JSONObject) ja.get(hallNum);
 				param1 = new Hall(jo.getString("hall_number"),
 						jo.getString("title"),
 						jo.getString("washers_available"),
 						jo.getString("dryers_available"),
 						jo.getString("washers_in_use"),
 						jo.getString("dryers_in_use"));
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 
 			return null;
 		}
 
 
 		protected void onPostExecute(Void result) {
 			finisheverything();
 			mPullRefreshScrollView.onRefreshComplete();
 			boolean isWNotifSet = sp.getBoolean("NotificationWSet", false);
 			if(System.currentTimeMillis()-sp.getLong("lastnotif", System.currentTimeMillis()+3700000)>3600000){
 				Editor e = sp.edit();
 				e.putBoolean("NotificationWSet", false);
 				e.putBoolean("NotificationDSet", false);
 				e.commit();
 			}
 			
 			TextView btnw = (TextView) findViewById(R.id.btnWasher);
 			
 			if(isWNotifSet){
 			btnw.setClickable(false);
 			btnw.setText("NOTIFICATION SET");
 			btnw.setBackgroundColor(getResources().getColor(
 					R.color.sudsdarkgray));
 			}
 			boolean isDNotifSet = sp.getBoolean("NotificationDSet", false);
 			TextView btnd = (TextView) findViewById(R.id.btnWasher);
 			
 			if(isWNotifSet){
 			btnd.setClickable(false);
 			btnd.setText("NOTIFICATION SET");
 			btnd.setBackgroundColor(getResources().getColor(
 					R.color.sudsdarkgray));
 			}
 			super.onPostExecute(result);
 		}
 	}
 
 	private class LoadHallInfo extends AsyncTask<Void, Hall, Void> {
 
 		private final ProgressDialog pd = new ProgressDialog(HallActivity.this);
 
 		// can use UI thread here
 		protected void onPreExecute() {
 			this.pd.setMessage(" Loading Hall Info...");
 			this.pd.show();
 		}
 
 		protected Void doInBackground(Void... ars) {
 			try {
 				HttpClient client = new DefaultHttpClient();
 				HttpGet get = new HttpGet(
 						"http://robertkcheung.com/laundrytime/laundry.php?request=GetHallList&code="
 								+ code);
 
 				ResponseHandler<String> responseHandler = new BasicResponseHandler();
 				JSONObject jsonob = new JSONObject(client.execute(get,
 						responseHandler));
 				// JSONArray ja = new JSONArray(client.execute(get,
 				// responseHandler));
 				JSONArray ja = jsonob.getJSONArray("halls");
 
 				JSONObject jo = (JSONObject) ja.get(hallNum);
 				param1 = new Hall(jo.getString("hall_number"),
 						jo.getString("title"),
 						jo.getString("washers_available"),
 						jo.getString("dryers_available"),
 						jo.getString("washers_in_use"),
 						jo.getString("dryers_in_use"));
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 
 			return null;
 		}
 
 		protected void onProgressUpdate(Hall... progress) {
 			// ((ArrayAdapter<Hall>)getListAdapter()).add(progress[0]);
 		}
 
 		protected void onPostExecute(Void result) {
 			pd.dismiss();
 			finisheverything();
 			String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
 					DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
 			mPullRefreshScrollView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
 
 		}
 	}
 
 	private class getWashTimes extends AsyncTask<Void, Void, Integer> {
 		
 		private final ProgressDialog pd = new ProgressDialog(HallActivity.this);
 
 		// can use UI thread here
 		protected void onPreExecute() {
 			this.pd.setMessage("Setting Alarm");
 			this.pd.show();
 		}
 
 		protected Integer doInBackground(Void... params) {
 			try {
 				HttpClient client = new DefaultHttpClient();
 				HttpGet get = new HttpGet(
 						"http://robertkcheung.com/laundrytime/laundry.php?request=GetMachinesByHall&hall_number="
 								+ hallNum + "&code=" + code);
 
 				ResponseHandler<String> responseHandler = new BasicResponseHandler();
 				JSONObject jsonob = new JSONObject(client.execute(get,
 						responseHandler));
 				JSONArray ja = jsonob.getJSONArray("machines");
 				wminsLeft = 120;
 				int ta = totalw;
 				for (int i = 0; i < ta; i++) {
 					JSONObject jo = (JSONObject) ja.get(i);
 					int time;
 					try{
 						if (jo.getString("status").startsWith("In Use")) {
 							time = Integer.parseInt(jo.getString("time_remaining")
 									.replace(" min", ""));
 							wminsLeft = time < wminsLeft ? time : wminsLeft;
 						}
 					}catch(Exception e){
 						Toast t = Toast.makeText(getApplicationContext(),
 								"Error getting remaining time", Toast.LENGTH_LONG);
 						t.show();
 						return -1;
 					}
 				}
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 			return wminsLeft;
 		}
 
 		protected void onPostExecute(Integer result) {
 
 			// ParsePush pp = new ParsePush();
 			// pp.setChannel("myTestDev");
 			// pp.setMessage(dminsLeft + "until a machine opens");
 			// pp.sendInBackground();
 			pd.dismiss();
 			if (wminsLeft == 120 || result == -1) {
 				Toast errortoast = Toast
 						.makeText(HallActivity.this.getApplicationContext(),
 								"ETA currently not available for this hall",
 								Toast.LENGTH_LONG);
 				errortoast.show();
 			} else {
 				Calendar cal = Calendar.getInstance();
 				cal.setTime(Calendar.getInstance().getTime());
 				cal.add(Calendar.MINUTE, wminsLeft);
 
 				Intent intent = new Intent(HallActivity.this,
 						AlarmReciever.class);
 				intent.putExtra("note",
 						"Washer Available!");
 				intent.putExtra("title", "SUDS");
 				PendingIntent pendingIntent = PendingIntent.getBroadcast(
 						HallActivity.this.getApplicationContext(), 1253,
 						intent, PendingIntent.FLAG_UPDATE_CURRENT
 								| Intent.FILL_IN_DATA);
 				AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 				alarmManager.set(AlarmManager.RTC_WAKEUP,
 						cal.getTimeInMillis(), pendingIntent);
 				Toast t = Toast.makeText(getApplicationContext(),
 						"Notification set for " + wminsLeft
 								+ " minutes from now", Toast.LENGTH_LONG);
 				t.show();
 				TextView btn = (TextView) findViewById(R.id.btnWasher);
 				btn.setClickable(false);
 				btn.setText("NOTIFICATION SET");
 				btn.setBackgroundColor(getResources().getColor(
 						R.color.sudsdarkgray));
 				Editor e = sp.edit();
 				e.putBoolean("NotificationWSet", true);
 				e.putLong("lastnotif", System.currentTimeMillis());
 				e.commit();
 			}
 		}
 
 	}
 
 	private class getDryTimes extends AsyncTask<Void, Void, Integer> {
 		private final ProgressDialog pd = new ProgressDialog(HallActivity.this);
 
 		// can use UI thread here
 		protected void onPreExecute() {
 			this.pd.setMessage("Setting Alarm");
 			this.pd.show();
 		}
 		protected Integer doInBackground(Void... params) {
 			try {
 				HttpClient client = new DefaultHttpClient();
 				HttpGet get = new HttpGet(
 						"http://robertkcheung.com/laundrytime/laundry.php?request=GetMachinesByHall&hall_number="
 								+ hallNum + "&code=" + code);
 
 				ResponseHandler<String> responseHandler = new BasicResponseHandler();
 				JSONObject jsonob = new JSONObject(client.execute(get,
 						responseHandler));
 				JSONArray ja = jsonob.getJSONArray("machines");
 				dminsLeft = 120;
 				int ta = totalw + totald;
 				for (int i = totalw; i < ta; i++) {
 					JSONObject jo = (JSONObject) ja.get(i);
 					int time;
 					
 					if (jo.getString("status").startsWith("In Use")) {
 						time = Integer.parseInt(jo.getString("time_remaining")
 								.replace(" min", ""));
 						dminsLeft = time < dminsLeft ? time : dminsLeft;
 					}
 					
 				}
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 				Toast t = Toast.makeText(getApplicationContext(),
 						"Error getting remaining time", Toast.LENGTH_LONG);
 				t.show();
 				return -1;
 			}
 			return dminsLeft;
 		}
 
 		protected void onPostExecute(Integer result) {
 
 			// ParsePush pp = new ParsePush();
 			// pp.setChannel("myTestDev");
 			// pp.setMessage(dminsLeft + "until a machine opens");
 			// pp.sendInBackground();
 			pd.dismiss();
 			if (dminsLeft == 120 || result==-1) {
 				Toast errortoast = Toast
 						.makeText(HallActivity.this.getApplicationContext(),
 								"ETA currently not available",
 								Toast.LENGTH_LONG);
 				errortoast.show();
 			} else {
 				Calendar cal = Calendar.getInstance();
 				cal.setTime(Calendar.getInstance().getTime());
 				cal.add(Calendar.MINUTE, dminsLeft);
 
 				Intent intent = new Intent(HallActivity.this,
 						AlarmReciever.class);
 				intent.putExtra("note",
 						"Dryer Available!");
 				intent.putExtra("title", "SUDS");
 				PendingIntent pendingIntent = PendingIntent.getBroadcast(
 						HallActivity.this.getApplicationContext(), 1253,
 						intent, PendingIntent.FLAG_UPDATE_CURRENT
 								| Intent.FILL_IN_DATA);
 				AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 				alarmManager.set(AlarmManager.RTC_WAKEUP,
 						cal.getTimeInMillis(), pendingIntent);
 				Toast t = Toast.makeText(getApplicationContext(),
 						"Notification set for " + dminsLeft
 								+ " minutes from now", Toast.LENGTH_LONG);
 				t.show();
 				TextView btn = (TextView) findViewById(R.id.btnDryer);
 				btn.setClickable(false);
 				btn.setText("NOTIFICATION SET");
 				btn.setBackgroundColor(getResources().getColor(
 						R.color.sudsdarkgray));
 				Editor e = sp.edit();
 				e.putBoolean("NotificationDSet", true);
 				e.putLong("lastnotif", System.currentTimeMillis());
 				e.commit();
 			}
 		}
 
 	}
 
 	void finisheverything() {
 
 		try {
 			int wavailable = Integer.parseInt(param1.washers_available);
 			int winuse = Integer.parseInt(param1.washers_in_use);
 			totalw = wavailable + winuse;
 			int davailable = Integer.parseInt(param1.dryers_available);
 			int dinuse = Integer.parseInt(param1.dryers_in_use);
 			totald = davailable + dinuse;
 
 
 			TextView hallname = (TextView) findViewById(R.id.textViewHall);
 			TextView nwashers = (TextView) findViewById(R.id.nWash);
 			TextView dwashers = (TextView) findViewById(R.id.dWash);
 
 			TextView ndryers = (TextView) findViewById(R.id.nDryer);
 			TextView ddryers = (TextView) findViewById(R.id.dDryer);
 
 			// TextView time = (TextView) findViewById(R.id.timeRemaining);
 			// TextView time2 = (TextView) findViewById(R.id.timeRemaining2);			
 			hallname.setText(param1.title.toUpperCase());
 			Editor e = sp.edit();
 			e.putString("hallName", param1.title);
 			e.commit();
 
 			if (davailable == 0) {
 				TextView btn = (TextView) findViewById(R.id.btnDryer);
 				btn.setClickable(true);
 				btn.setBackgroundDrawable(getResources().getDrawable(
 						R.drawable.no_more_machines_btn));
 				btn.setText("Notify Me When Available".toUpperCase());
 				btn.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 					public void onClick(View v) {
 						new getDryTimes().execute();
 					}
 				});
 
 			}
 			if (wavailable == 0) {
 				TextView btn = (TextView) findViewById(R.id.btnWasher);
 				btn.setClickable(true);
 				btn.setBackgroundDrawable(getResources().getDrawable(
 						R.drawable.no_more_machines_btn));
 				btn.setText("Notify Me When Available".toUpperCase());
 				btn.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 					public void onClick(View v) {
 						new getWashTimes().execute();
 					}
 				});
 			}
 			ndryers.setText(Integer.toString(davailable));
 			nwashers.setText(Integer.toString(wavailable));
 			ddryers.setText(Integer.toString(totald));
 			dwashers.setText(Integer.toString(totalw));
 
 		} catch (NullPointerException npe) {
 
 		}
 	}
 	
 	public void onWasherClick(View v){
 		Intent i = new Intent(HallActivity.this, DetailActivity.class);
 		i.putExtra("isWasher", true);
 		startActivityForResult(i, 500);
 		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
 	}
 	public void onDryerClick(View v){
 		Intent i = new Intent(HallActivity.this, DetailActivity.class);
 		i.putExtra("isWasher", false);
 		startActivityForResult(i, 500);
 		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
 	}
 	
 	
 
 	
 }
