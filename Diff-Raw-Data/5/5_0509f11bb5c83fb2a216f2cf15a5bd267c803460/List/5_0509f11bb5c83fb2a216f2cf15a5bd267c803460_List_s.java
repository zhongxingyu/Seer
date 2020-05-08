 package com.pilot51.lclock;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class List extends Activity {
 	protected Common common = newCommon();
 	String TAG;
 	int src;
 	TextView txtTimer;
 	ListView lv;
 	SimpleAdapter adapter;
 	String[] from;
 	int[] to;
 	HashMap<String, Object> launchMap = new HashMap<String, Object>();
 	ArrayList<HashMap<String, Object>> launchMaps = new ArrayList<HashMap<String, Object>>();
 	CountDownTimer timer;
 
 	protected Common newCommon() {
 		return new Common();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		TAG = getString(R.string.app_name);
 
 		src = getIntent().getIntExtra("source", 0);
 
 		requestWindowFeature(Window.FEATURE_PROGRESS);
 		setContentView(R.layout.list);
 
 		from = new String[] { "mission", "vehicle", "location", "date", "time", "description" };
 		to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6 };
 
 		getFeed();
 		adapter = new SimpleAdapter(this, launchMaps, R.layout.grid, from, to);
 		createList();
 
 		long launchTime = eventTime(launchMap);
 		txtTimer.setVisibility(TextView.VISIBLE);
 		String mission = null;
 		if (src == 1) mission = ((String)launchMap.get("mission")).replaceAll("</a>|^[0-9a-zA-Z \\-]+\\(|\\)$", "");
 		else if (src == 2) mission = (String)launchMap.get("vehicle");
 		if (launchTime > 0) {
 			timer = new CDTimer(launchTime - System.currentTimeMillis(), 1000, this, txtTimer, launchTime, "Next mission: " + mission).start();
 		} else txtTimer.setVisibility(TextView.GONE);
 
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			@SuppressWarnings("unchecked")
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				launchMap = (HashMap<String, Object>) lv.getItemAtPosition(position);
 				if (timer != null)
 					timer.cancel();
 				long launchTime = eventTime(launchMap);
 				txtTimer.setVisibility(TextView.VISIBLE);
 				String mission = null;
 				if (src == 1)
 					mission = ((String)launchMap.get("mission")).replaceAll("</a>|^[0-9a-zA-Z \\-]+\\(|\\)$", "");
 				else if (src == 2)
 					mission = (String)launchMap.get("vehicle");
 				if (launchTime > 0)
 					timer = new CDTimer(launchTime - System.currentTimeMillis(), 1000, List.this, txtTimer, launchTime, mission).start();
 				else
 					txtTimer.setText(mission + "\nError parsing launch time.");
 			}
 		});
 
 		// Main.progress.dismiss();
 		/*
 		 * if (timr != null) { // Log.d(TAG, "timr != null"); timr = common.new
 		 * CDTimer(common.eventTime(map) - System.currentTimeMillis(), 1000,
 		 * this, txttime, String.valueOf(grouptmp.get("name"))).start(); }
 		 */
 	}
 
 	void createList() {
 		lv = (ListView) findViewById(R.id.list);
 		common.ad(this);
 		txtTimer = (TextView) findViewById(R.id.txtTime);
 		txtTimer.setVisibility(TextView.GONE);
 		TextView header1 = (TextView) findViewById(R.id.header1);
 		if(src == 2) header1.setText("Payload");
 		registerForContextMenu(lv);
 		lv.setAdapter(adapter);
 		// ll = new LinearLayout(this);
 		// mWebView = new WebView(this);
 	}
 
 	long eventTime(HashMap<String, Object> map) {
 		Calendar cal = Calendar.getInstance();
 		try {
 			String year = (String)map.get("year");
 			String date = ((String)map.get("day")).replaceAll("\\?|/[0-9]+|\\.", "").replaceFirst("Sept", "Sep");
 			String time = ((String)map.get("time")).replaceAll("^[A-Za-z]* |\\-[0-9]{4}(:[0-9]{2})?| \\([0-9a-zA-Z:; \\-]*\\)| (–|\\-|and|to)[0-9ap: ]+(m|[0-9])| */ *[0-9a-zA-Z: \\-]+$", "");
			if (src == 1 & time.contentEquals("**")) {
 				time = "0:00 am GMT";
 			} else if (src == 2 & time.contentEquals("TBD")) {
 				time = "0000 GMT";
 			}
 			if (date.matches("[A-Za-z]+")) {
 //				Log.d(TAG, "passes accuracy missing day check");
 				date = date + " 1";
 			}
 			Log.d(TAG, time + " " + date + " " + year);
 			if (src == 1) {
 				if (time.matches("[0-9]{1,2}:[0-9]{2}:[0-9]{2} [ap]m [A-Z]+")) {
 //					Log.d(TAG, "passes accuracy to second check");
 					cal.setTime(new SimpleDateFormat("h:mm:ss a z MMM d yyyy").parse(time + " " + date + " " + year));
 				} else cal.setTime(new SimpleDateFormat("h:mm a z MMM d yyyy").parse(time + " " + date + " " + year));
 			} else if (src == 2) {
 				if (time.matches("[0-9]{4}:[0-9]{2} [A-Z]+")) {
 //					Log.d(TAG, "passes accuracy to second check");
 					cal.setTime(new SimpleDateFormat("HHmm:ss z MMM d yyyy").parse(time + " " + date + " " + year));
 				} else cal.setTime(new SimpleDateFormat("HHmm z MMM d yyyy").parse(time + " " + date + " " + year));
 				Calendar cal2 = Calendar.getInstance();
 				cal2.set(Calendar.MONTH, cal2.get(Calendar.MONTH) - 1);
 				if (cal.before(cal2)) cal.add(Calendar.YEAR, 1);
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "Error parsing event date!");
 			e.printStackTrace();
 			cal.setTimeInMillis(0);
 		}
 		return cal.getTimeInMillis();
 	}
 
 	void getFeed() {
 		String data = null;
 		if (src == 1)
 			data = downloadFile("http://www.nasa.gov/missions/highlights/schedule.html");
 		else if (src == 2)
 			data = downloadFile("http://spaceflightnow.com/tracking/index.html");
 		if (data == null) {
 			// readArray();
 			if (launchMaps.isEmpty()) {
 				// Finish List activity and show error if cache cannot be loaded
 				finish();
 				Toast.makeText(this, "Error: No data received and no cache.", Toast.LENGTH_LONG).show();
 			} else {
 				// Tell user situation if cache successfully loaded
 				Toast.makeText(this, "No data received, loaded from cache.", Toast.LENGTH_LONG).show();
 			}
 		} else {
 			try {
 				if (src == 1)
 					parseNASA(data);
 				else if (src == 2)
 					parseSfn(data);
 				// Save cache if new data downloaded
 				// saveArray();
 			} catch (Exception e) {
 				// readArray();
 				if (launchMaps.isEmpty()) {
 					// Finish List activity and show error if cache cannot be loaded
 					finish();
 					Toast.makeText(this, "Error parsing received data,\nno cache to fall back to.", Toast.LENGTH_LONG).show();
 				} else {
 					// Tell user situation if cache successfully loaded
 					Toast.makeText(this, "Error parsing received data,\nloaded from cache.", Toast.LENGTH_LONG).show();
 				}
 				Toast.makeText(this, "Please contact developer if error persists and webpage loads normally.", Toast.LENGTH_LONG).show();
 				e.printStackTrace();
 			}
 		}
 		if (!launchMaps.isEmpty()) {
 			// Toast.makeText(this,
 			// "Touch & hold an event in the list for more stuff.",
 			// Toast.LENGTH_LONG).show();
 		}
 	}
 
 	String downloadFile(String url) {
 		InputStream input = null;
 		String strdata = null;
 		StringBuffer strbuff = new StringBuffer();
 		int count;
 		try {
 			URL url2 = new URL(url);
 			// download the file
 			input = new BufferedInputStream(url2.openStream());
 			byte data[] = new byte[1024];
 			long total = 0;
 			while ((count = input.read(data)) != -1) {
 				total += count;
 				strbuff.append(new String(data, 0, count));
 			}
 			strdata = strbuff.toString();
 			strdata = strdata.replaceAll("\r\n", "\n");
 			input.close();
 		} catch (Exception e) {
 			Log.e(TAG, "Error: Failure downloading file\n" + e.getMessage());
 		}
 		// Log.d(TAG, strdata);
 		return strdata;
 	}
 
 	void parseNASA(String data) {
 		data = data.replaceAll("<[aA] [^>]*?>|</[aA]>|<font[^>]*?>|</font>|</?b>|\n|\t", "");
 		int tmp;
 		String year = null;
 		for (int i = 0; data.contains("Description:"); i++) {
 			HashMap<String, Object> map = new HashMap<String, Object>();
 			
 			// Isolate event from the rest of the HTML
 			String data2 = data.substring(data.indexOf("Date:"), data.indexOf("<br /><br />", data.indexOf("Description:")) + 12);
 			
 			// Year
 			tmp = data.indexOf("<center> 20");
 			if (tmp != -1 & tmp < data.indexOf("Date:")) {
 				data = data.substring(tmp + 9, data.length());
 				year = data.substring(0, data.indexOf(" "));
 			}
 			map.put("year", year);
 
 			// Date
 			data2 = data2.substring(data2.indexOf("Date:") + 6, data2.length());
 			map.put("day", data2.substring(0, data2.indexOf("<")).replaceAll("[\\*\\+]*", "").trim());
 			map.put("date", map.get("day") + ", " + year);
 
 			// Mission
 			data2 = data2.substring(data2.indexOf("Mission:") + 9, data2.length());
 			map.put("mission", data2.substring(0, data2.indexOf("<br")).trim());
 
 			// Vehicle
 			data2 = data2.substring(data2.indexOf("Vehicle:") + 9, data2.length());
 			map.put("vehicle", data2.substring(0, data2.indexOf("<br")).trim());
 
 			// Location
 			data2 = data2.substring(data2.indexOf("Site:") + 6, data2.length());
 			map.put("location", data2.substring(0, data2.indexOf("<br")).trim());
 
 			// Time
 			/*
 			if (data2.indexOf("Time:") != -1 & data2.indexOf("Window:") != -1) {
 				if (data2.indexOf("Time:") < data2.indexOf("Window:")) tmp = data2.indexOf("Time:") + 11;
 				else tmp = data2.indexOf("Window:") + 13;
 			} else */
 			if (data2.contains("Time:"))
 				tmp = data2.indexOf("Time:") + 5;
 			else if (data2.contains("Window:"))
 				tmp = data2.indexOf("Window:") + 7;
 			else if (data2.contains("Times:"))
 				tmp = data2.indexOf("Times:") + 6;
 			data2 = data2.substring(tmp, data2.length());
 			map.put("time", data2.substring(0, data2.indexOf("<br")).replaceAll("[\\.\\*\\+]*", "").replaceAll(" {2,}", " ").trim());
 
 			// Description
 			data2 = data2.substring(data2.indexOf("Description:") + 17, data2.length());
 			map.put("description", data2.substring(0, data2.indexOf("<br")).trim());
 			
 			/*
 			// Calendar
 			Calendar cal = Calendar.getInstance();
 			cal.setTimeInMillis(eventTime(map));
 			map.put("calendar", cal);
 			*/
 
 			launchMaps.add(map);
 			if (i == 0)
 				launchMap = map;
 			data = data.substring(data.indexOf("<br /><br />", data.indexOf("Description:")) + 12, data.length());
 		}
 	}
 
 	void parseSfn(String data) {
 		data = data.replaceAll("<![ \n\t]*(--([^-]|[\n]|-[^-])*--[ \n\t]*)>|<FONT[^>]*?>|</[\nFONT]{4,5}>|</?B>|<[aA]\\s[^>]*?>|</[aA]>", "");
 		int tmp = 0;
 		int year = Calendar.getInstance().get(Calendar.YEAR);
 		for (int i = 0; data.contains("CC0000"); i++) {
 			HashMap<String, Object> map = new HashMap<String, Object>();
 			
 			// Isolate event from the rest of the HTML
 			String data2 = data.substring(data.indexOf("CC0000") + 8, data.indexOf("6\"></TD"));
 			data = data.substring(data.indexOf("6\"></TD") + 8, data.length());
 
 			// Date
 			map.put("day", data2.substring(0, data2.indexOf("<")).replaceAll("\n", " ").trim());
 			map.put("year", Integer.toString(year));
 			map.put("date", map.get("day")/* + ", " + map.get("year")*/);
 
 			// Vehicle
 			data2 = data2.substring(data2.indexOf(">&nbsp;") + 7, data2.length());
 			map.put("vehicle", data2.substring(0, data2.indexOf("&nbsp")).replaceAll("\n", " ").trim());
 			
 			// Payload
 			data2 = data2.substring(data2.indexOf("&#149;") + 18, data2.length());
 			map.put("mission", data2.substring(0, data2.indexOf("</TD")).replaceAll("\n|<BR>", " ").trim());
 
 			// Time
 			/*
 			if (data2.indexOf("time:") != -1 & data2.indexOf("window:") != -1) {
 				if (data2.indexOf("time:") < data2.indexOf("window:")) tmp = data2.indexOf("time:") + 5;
 				else tmp = data2.indexOf("window:") + 7;
 			} else */
 			if (data2.indexOf("time:") != -1)
 				tmp = data2.indexOf("time:") + 5;
 			else if (data2.indexOf("window:") != -1)
 				tmp = data2.indexOf("window:") + 7;
 			else if (data2.indexOf("times:") != -1)
 				tmp = data2.indexOf("times:") + 6;
 			data2 = data2.substring(tmp, data2.length());
 			map.put("time", data2.substring(0, data2.indexOf("<")).replaceAll("\\.", "").replaceAll("\n", " ").trim());
 
 			// Location
 			data2 = data2.substring(data2.indexOf("site:") + 5, data2.length());
 			map.put("location", data2.substring(0, data2.indexOf("<")).replaceAll("\n", " ").trim());
 
 			// Description
 			data2 = data2.substring(data2.indexOf("><BR>") + 5, data2.length());
 			map.put("description", data2.substring(0, data2.indexOf("</TD")).replaceAll("\n", " ").trim());
 
 			/*
 			// Calendar
 			Calendar cal = Calendar.getInstance();
 			cal.setTimeInMillis(eventTime(map));
 			map.put("calendar", cal);
 			*/
 			
 			launchMaps.add(map);
 			if (i == 0)
 				launchMap = map;
 		}
 	}
 
 	/*	@Override
 		public Object onRetainNonConfigurationInstance() {
 			ArrayList<Object> tmpData = new ArrayList<Object>();
 			tmpData.add(fillMaps);
 			tmpData.add(grouptmp);
 			if (timr != null) {
 				timr.cancel();
 			}
 			tmpData.add(timr);
 			return tmpData;
 		}*/
 
 	/*	@SuppressWarnings("unchecked")
 		private void loadData() {
 			ArrayList<Object> tmpdata = (ArrayList<Object>)getLastNonConfigurationInstance();
 			// The activity is starting for the first time, load the data
 			if (tmpdata == null) {
 				getFeed();
 			} else {
 				// The activity was destroyed/created automatically, reload the data from the previous activity
 				fillMaps = (ArrayList<HashMap<String, Object>>) tmpdata.get(0);
 				grouptmp = (HashMap<String, Object>)tmpdata.get(1);
 				timr = (CountDownTimer)tmpdata.get(2);
 			}
 		}*/
 
 	/*	@Override
 		public boolean onKeyDown(int keyCode, KeyEvent event) {
 			if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 				// If WebView is shown
 				if (ll.isShown()) {
 					// If there is a page before current in history
 					if (mWebView.canGoBack()) {
 						// Go to previous page
 						mWebView.goBack();
 					}
 					// Return to list
 					else {
 						ll.removeAllViews();
 						mWebView.destroy();
 						setContentView(R.layout.list_pass);
 						createList();
 					}
 				}
 				// Close activity
 				else {
 					finish();
 					clock.cancel();
 					if (timr != null) {
 						timr.cancel();
 					}
 				}
 			}
 			return false;
 		}*/
 	@SuppressWarnings("unchecked")
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater =	getMenuInflater();
 		inflater.inflate(R.menu.context_menu, menu);
 		launchMap = (HashMap<String, Object>)lv.getItemAtPosition(((AdapterContextMenuInfo)menuInfo).position);
 	}
 	@SuppressWarnings("unchecked")
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		launchMap = (HashMap<String, Object>)lv.getItemAtPosition(info.position);
 		switch (item.getItemId()) {
 			case R.id.ctxtMap:
 				String location = (String)launchMap.get("location");
 				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
 				Uri.parse("geo:0,0?q=" + location));
 				startActivity(intent);
 				return true;
 /*			case R.id.ctxtEventDet:
 				if (eventType == 1) {
 					common.browser(this, this, mWebView, ll, "http://heavens-above.com/" + grouptmp.get("passurl"));
 				} else if (eventType == 2) {
 					common.browser(this, this, mWebView, ll, "http://heavens-above.com/" + grouptmp.get("flareurl"));
 				} return true;
 			case R.id.ctxtSatDet:
 				common.browser(this, this, mWebView, ll, "http://heavens-above.com/" + grouptmp.get("saturl"));
 				return true;
 			case R.id.ctxtCD:
 				if (timer != null) {
 					timer.cancel();
 				}
 				long launchTime = eventTime(launchMap);
 				txtTimer.setVisibility(TextView.VISIBLE);
 				if(launchTime > 0) {
 					timer = new CDTimer(launchTime - System.currentTimeMillis(), 1000, this, txtTimer, launchTime, launchMap.get("mission")).start();
 				} else {
 					txtTimer.setText(launchMap.get("mission") + "\nError parsing launch time.");
 				}
 				return true;
 */
 			default:
 					return super.onContextItemSelected(item);
 		}
 	}
 	class CDTimer extends CountDownTimer {
 		int cddd, cdhh, cdmm, cdss;
 		long launchTime;
 		String info;
 		TextView txttime;
 
 		CDTimer(long millisInFuture, long countDownInterval, Context context, TextView txttime, long launchTime, String info) {
 			super(millisInFuture, countDownInterval);
 			this.info = info;
 			this.launchTime = launchTime;
 			this.txttime = txttime;
 		}
 
 		@Override
 		public void onFinish() {
			txttime.setText(info + " Launch!");
 		}
 
 		@Override
 		public void onTick(long millisUntilFinished) {
 			cddd = (int) (millisUntilFinished / 1000 / 24 / 60 / 60);
 			cdhh = (int) ((millisUntilFinished - cddd * 1000 * 24 * 60 * 60) / 1000 / 60 / 60);
 			cdmm = (int) ((millisUntilFinished - cddd * 1000 * 24 * 60 * 60 - cdhh * 1000 * 60 * 60) / 1000 / 60);
 			cdss = (int) ((millisUntilFinished - cddd * 1000 * 24 * 60 * 60 - cdhh * 1000 * 60 * 60 - cdmm * 1000 * 60) / 1000);
 			txttime.setText(info + "\n" + new SimpleDateFormat("yyyy-MM-dd h:mm:ss a zzz").format(launchTime) + "\nCountdown: " + cddd + "d "
 					+ new SimpleDateFormat("HH:mm:ss").format(new Date(0, 0, 0, cdhh, cdmm, cdss)));
 		}
 	}
 }
