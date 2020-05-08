 package com.smerty.wunder;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.AsyncTask.Status;
 import android.text.InputFilter;
 import android.text.Spanned;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Wunder extends Activity implements LocationListener {
 
 	public static final String PREFS_NAME = "WunderPrefs";
 	private LocationManager locationManager;
 	private String bestProvider;
 	private static final String[] S = { "Out of Service",
 		"Temporarily Unavailable", "Available" };
 	private String selectedID = "";
 	
 	ScrollView sv;
 	TableLayout table;
 	
 	private AsyncTask<Wunder, Integer, Integer> updatetask;
 	public ProgressDialog progressDialog;
 
 	WeatherReport conds;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// might be a memory leak on rotation since onCreate will be called again
 		sv = new ScrollView(this);
 		table = new TableLayout(this);
 
 		table.setShrinkAllColumns(true);
 		
 		
 
 		if (this.updatetask == null) {
 			Log.d("startDownloading", "task was null, calling execute");
 			this.updatetask = new UpdateFeedTask().execute(this);
 		} else {
 			Status s = this.updatetask.getStatus();
 			if (s == Status.FINISHED) {
 				Log
 						.d("updatetask",
 								"task wasn't null, status finished, calling execute");
 				this.updatetask = new UpdateFeedTask().execute(this);
 			}
 		}
 		//allTogether(tableLayout, this);
 		
 		sv.addView(table);
 		setContentView(sv);
 	}
 	
 	private class UpdateFeedTask extends AsyncTask<Wunder, Integer, Integer> {
 
 		Wunder that;
 
 		protected Integer doInBackground(Wunder... thats) {
 
 			if (that == null) {
 				this.that = thats[0];
 			}
 
 			publishProgress(0);
 			
 			try {
 				that.conds = that.getWeather();
 			} catch (Exception e) {
 				that.conds = null;
 			}
 			
 
 
 			publishProgress(100);
 
 			return 0;
 		}
 
 		protected void onProgressUpdate(Integer... progress) {
 			Log.d("onProgressUpdate", progress[0].toString());
 			if (progress[0] == 0) {
 				that.progressDialog = ProgressDialog.show(that, "Wunder",
 						"Downloading Weather XML Feed", true, false);
 			}
 			if (progress[0] == 100) {
 				that.progressDialog.dismiss();
 			}
 
 		}
 
 		protected void onPostExecute(Integer result) {
 			//Log.d("onPostExecute", that.getApplicationInfo().packageName);
 			that.allTogether();
 			
 		}
 	}
 	
 	
 	
 	public void allTogether() {
 		
 		
 		Wunder that = this;
 		
 		
 		
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		boolean usemetric = settings.getBoolean("useMetric", false);
 
 		TableRow row = new TableRow(that);
 		TextView text = new TextView(that);
 		if (conds != null) {
 			text.setText(conds.neighborhood);
 		} else {
 			text.setText("No Weather To Display");
 		}
 		text.setTextSize(24);
 		row.setPadding(3, 3, 3, 3);
 		row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 		row.addView(text);
 		table.addView(row);
 
 		row = new TableRow(that);
 		row.setPadding(3, 1, 3, 1);
 		row.setBackgroundColor(Color.argb(200, 80, 80, 80));
 		table.addView(row);
 
 		if (conds != null) {
 
 			if (conds.temperature != null && conds.temperature.length() > 0) {
 				row = new TableRow(that);
 				text = new TextView(that);
 				if (usemetric) {
 					conds.temperature = String.valueOf(Math.floor((Double.parseDouble(conds.temperature)-32)*5/9*10)/10);
 					text.setText("Temperature: \t\t\t\t\t" + conds.temperature
 							+ "C");
 				}
 				else {
 					text.setText("Temperature: \t\t\t\t\t" + conds.temperature
 							+ "F");
 				}
 				text.setTextSize(18);
 				row.setPadding(3, 3, 3, 3);
 				row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 				row.addView(text);
 				table.addView(row);
 			}
 
 			if (conds.dewpoint != null && conds.dewpoint.length() > 0) {
 				row = new TableRow(that);
 				text = new TextView(that);
 				if (usemetric) {
 					conds.dewpoint = String.valueOf(Math.floor((Double.parseDouble(conds.dewpoint)-32)*5/9*10)/10);
 					text.setText("Dewpoint: \t\t\t\t\t\t" + conds.dewpoint + "C");
 				}
 				else {
 					text.setText("Dewpoint: \t\t\t\t\t\t" + conds.dewpoint + "F");
 				}
 				text.setTextSize(18);
 				row.setPadding(3, 3, 3, 3);
 				row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 				row.addView(text);
 				table.addView(row);
 			}
 
 			if (conds.humidity != null && conds.humidity.length() > 0) {
 				row = new TableRow(that);
 				text = new TextView(that);
 				text.setText("Relative Humidity: \t\t\t" + conds.humidity
 								+ "%");
 				text.setTextSize(18);
 				row.setPadding(3, 3, 3, 3);
 				row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 				row.addView(text);
 				table.addView(row);
 			}
 
 			if (conds.winddirection != null && conds.winddirection.length() > 0
 					&& conds.windspeed != null && conds.windspeed.length() > 0
 					&& Double.valueOf(conds.windspeed) > 0) {
 				row = new TableRow(that);
 				text = new TextView(that);
 				text.setText("Wind Direction: \t\t\t\t" + conds.winddirection);
 				text.setTextSize(18);
 				row.setPadding(3, 3, 3, 3);
 				row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 				row.addView(text);
 				table.addView(row);
 			}
 
 			if (conds.windspeed != null && conds.windspeed.length() > 0) {
 				row = new TableRow(that);
 				text = new TextView(that);
 				if (Double.valueOf(conds.windspeed) > 0) {
 					// text.setText("Wind: \t\t" + conds.winddirection + " at "+
 					// conds.windspeed + " mph");
 				}
 				if (usemetric) {
 					conds.windspeed = String.valueOf(Math.floor((Double.parseDouble(conds.windspeed))*1.609344*10)/10);
 					text.setText("Wind Speed: \t\t\t\t\t" + conds.windspeed
 							+ " km/h");
 				}
 				else {
 					text.setText("Wind Speed: \t\t\t\t\t" + conds.windspeed
 							+ " mph");
 				}
 				// }
 				text.setTextSize(18);
 				row.setPadding(3, 3, 3, 3);
 				row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 				row.addView(text);
 				table.addView(row);
 			}
 
 			if (conds.pressure != null && conds.pressure.length() > 0) {
 				row = new TableRow(that);
 				text = new TextView(that);
 				if (usemetric) {
 					conds.pressure = String.valueOf(Math.floor((Double.parseDouble(conds.pressure))*33.86389*10)/10);
 					text.setText("Barometric Pressure: \t\t" + conds.pressure
 							+ " mb");
 				}
 				else {
 					text.setText("Barometric Pressure: \t\t" + conds.pressure
 							+ " in");
 				}
 				text.setTextSize(18);
 				row.setPadding(3, 3, 3, 3);
 				row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 				row.addView(text);
 				table.addView(row);
 			}
 
 			if (conds.solarradiation != null
 					&& conds.solarradiation.length() > 0) {
 				row = new TableRow(that);
 				text = new TextView(that);
 				text.setText("Solar Radiation: \t\t\t\t" + conds.solarradiation
 						+ " w/m^2");
 				text.setTextSize(18);
 				row.setPadding(3, 3, 3, 3);
 				row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 				row.addView(text);
 				table.addView(row);
 			}
 
 			if (conds.uv != null && conds.uv.length() > 0) {
 				row = new TableRow(that);
 				text = new TextView(that);
 				text.setText("UV Index: \t\t\t\t\t\t" + conds.uv);
 				text.setTextSize(18);
 				row.setPadding(3, 3, 3, 3);
 				row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 				row.addView(text);
 				table.addView(row);
 			}
 
 			if (conds.precipitation1hr != null
 					&& conds.precipitation1hr.length() > 0
 					&& Double.valueOf(conds.precipitation1hr) > 0) {
 				row = new TableRow(that);
 				text = new TextView(that);
 				if (usemetric) {
 					conds.precipitation1hr = String.valueOf(Math.floor((Double.parseDouble(conds.precipitation1hr))*25.4*10)/10);
 					text.setText("Precipitation (hour): \t\t"
 							+ conds.precipitation1hr + " mm");
 				}
 				else {
 					text.setText("Precipitation (hour): \t\t"
 							+ conds.precipitation1hr + " in");
 				}
 				text.setTextSize(18);
 				row.setPadding(3, 3, 3, 3);
 				row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 				row.addView(text);
 				table.addView(row);
 			}
 
 			if (conds.precipitationtoday != null
 					&& conds.precipitationtoday.length() > 0
 					&& Double.valueOf(conds.precipitationtoday) > 0) {
 				row = new TableRow(that);
 				text = new TextView(that);
 				if (usemetric) {
 					conds.precipitationtoday = String.valueOf(Math.floor((Double.parseDouble(conds.precipitationtoday))*25.4*10)/10);
 					text.setText("Precipitation (today): \t\t"
 							+ conds.precipitationtoday + " mm");
 				}
 				else {
 					text.setText("Precipitation (today): \t\t"
 							+ conds.precipitationtoday + " in");
 				}
 				text.setTextSize(18);
 				row.setPadding(3, 3, 3, 3);
 				row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 				row.addView(text);
 				table.addView(row);
 			}
 
 			row = new TableRow(that);
 			row.setPadding(3, 3, 3, 3);
 			row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 			table.addView(row);
 
 			row = new TableRow(that);
 			row.setPadding(3, 1, 3, 1);
 			row.setBackgroundColor(Color.argb(200, 80, 80, 80));
 			table.addView(row);
 
 			row = new TableRow(that);
 			text = new TextView(that);
 			text.setText(conds.updated);
 			text.setTextSize(14);
 			text.setGravity(2);
 			row.setPadding(3, 3, 3, 3);
 			row.addView(text);
 			table.addView(row);
 
 			row = new TableRow(that);
 			text = new TextView(that);
 			text.setText(R.string.weathersource);
 			text.setTextSize(14);
 			text.setGravity(2);
 			row.setPadding(3, 3, 3, 3);
 			row.addView(text);
 			table.addView(row);
 
 		} else {
 
 			row = new TableRow(that);
 			text = new TextView(that);
 			text.setText("Data not available...");
 			text.setTextSize(14);
 			text.setGravity(1);
 			row.setPadding(3, 3, 3, 3);
 			row.setBackgroundColor(Color.argb(200, 51, 51, 51));
 			row.addView(text);
 			table.addView(row);
 		}
 
 	}
 
 	public static final int MENU_ABOUT = 10;
 	public static final int MENU_QUIT = 11;
 	public static final int MENU_REFRESH = 12;
 	public static final int MENU_SETTINGS = 13;
 	public static final int MENU_GEO = 14;
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MENU_ABOUT, 0, "About");
 		// menu.add(0, MENU_REFRESH, 0, "Refresh");
 		menu.add(0, MENU_GEO, 0, "Nearby PWS");
 		menu.add(0, MENU_SETTINGS, 0, "Settings");
 		menu.add(0, MENU_QUIT, 0, "Quit");
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case MENU_ABOUT:
 			Toast.makeText(getBaseContext(), "Developed by Smerty Software", Toast.LENGTH_LONG).show();
 			return true;
 		case MENU_GEO:
 			geoHelper();
 			return true;
 		case MENU_REFRESH:
 			// this.onCreate(null);
 			return true;
 		case MENU_SETTINGS:
 
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 			alert.setTitle("PWS Station ID");
 
 			final EditText input = new EditText(this);
 
 			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 			String pwsid = settings.getString("stationID", "KCALIVER14");
 			boolean useMetric = settings.getBoolean("useMetric", false);
 
 			input.setSingleLine(true);
 
 			InputFilter filter = new InputFilter() {
 				public CharSequence filter(CharSequence source, int start,
 						int end, Spanned dest, int dstart, int dend) {
 					for (int i = start; i < end; i++) {
 						if (!Character.isLetterOrDigit(source.charAt(i))) {
 							return "";
 						}
 					}
 					return null;
 				}
 			};
 			
 			input.setText(pwsid);
 			input.setFilters(new InputFilter[]{filter});
 			
 			final LinearLayout alertLayout = new LinearLayout(this);
 			
 			alertLayout.setOrientation(1);
 			
 			
 			final LinearLayout metricCheckLayout = new LinearLayout(this);
 			metricCheckLayout.setOrientation(0);
 			
 			
 			final CheckBox metricCheck = new CheckBox(this);
 			metricCheck.setChecked(useMetric);
 			
 			metricCheckLayout.addView(metricCheck);
             final TextView metricText = new TextView(this);			
 			
             metricText.setText("Use Metric");
             metricText.setTextSize(14);
             metricText.setGravity(2);
 			
             metricCheckLayout.addView(metricText);
             
 			alertLayout.addView(input);
 			alertLayout.addView(metricCheckLayout);
 			
 			alert.setView(alertLayout);
 			
 			final Wunder that = this;
 
 			
 			alert.setPositiveButton("Set",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 							String value = input.getText().toString().trim()
 									.toUpperCase();
 
 							SharedPreferences settings = getSharedPreferences(
 									PREFS_NAME, 0);
 							String pwsid = settings.getString("stationID",
 									"KCALIVER14");
 
 							SharedPreferences.Editor editor = settings.edit();
 							editor.putString("stationID", value);
 							editor.putBoolean("useMetric", metricCheck.isChecked());
 							editor.commit();
 
 							if (!pwsid.equalsIgnoreCase(value)) {
 								Toast.makeText(getBaseContext(),
 										"Changed: " + value, Toast.LENGTH_LONG)
 										.show();
 
 							}
 							
 							that.onCreate(null);
 							
 							
 						}
 					});
 
 			alert.setNegativeButton("Cancel",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 							// canceled
 						}
 					});
 			
 
 			alert.setIcon(R.drawable.icon);
 
 			alert.show();
 			
 
 			return true;
 		case MENU_QUIT:
 			finish();
 			return true;
 		}
 		return false;
 	}
 
 	public class WeatherReport {
 		public String stationID, neighborhood, updated, temperature, humidity,
 				winddirection, windspeed, pressure, dewpoint, heatindex,
 				windchill, solarradiation, uv, precipitation1hr,
 				precipitationtoday;
 
 		public WeatherReport() {
 			// do nothing
 		}
 	}
 
 	public WeatherReport getWeather() {
 
 		WeatherReport retval = new WeatherReport();
 
 		try {
 
 			HttpParams params = new BasicHttpParams();
 			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 			HttpProtocolParams.setContentCharset(params, "UTF-8");
 			HttpProtocolParams.setUseExpectContinue(params, true);
 			HttpProtocolParams.setHttpElementCharset(params, "UTF-8");
 			HttpProtocolParams.setUserAgent(params, "java");
 
 			DefaultHttpClient client = new DefaultHttpClient(params);
 
 			InputStream data = null;
 			
 			String pwsid = null;
 
 			try {
 
 				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 				pwsid = settings.getString("stationID", "KCALIVER14");
 
 				HttpGet method = new HttpGet(
 						"http://api.wunderground.com/weatherstation/WXCurrentObXML.asp?ID="
 								+ pwsid);
 				HttpResponse res = client.execute(method);
 				data = res.getEntity().getContent();
 
 			} catch (IOException e) {
 				e.printStackTrace();
 				 Toast.makeText(getBaseContext(),"Network Failure...", Toast.LENGTH_SHORT).show();
 
 				return null;
 			}
 			
 
 			Document doc = null;
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder db;
 
 			try {
 				db = dbf.newDocumentBuilder();
 				doc = db.parse(data);
 				// finish();
 			} catch (SAXParseException e) {
 				e.printStackTrace();
 				Toast.makeText(getBaseContext(), "SAXParseException, bad XML?", Toast.LENGTH_SHORT).show();
 				return null;
 				// finish();
 			} catch (SAXException e) {
 				// TODO Auto-generated catch block
 				Toast.makeText(getBaseContext(), "SAXException",
 						Toast.LENGTH_SHORT).show();
 				e.printStackTrace();
 				return null;
 				// finish();
 			} catch (ParserConfigurationException e) {
 				// TODO Auto-generated catch block
 				Toast.makeText(getBaseContext(), "ParserConfigurationException", Toast.LENGTH_SHORT).show();
 				e.printStackTrace();
 				return null;
 			}
 
 			doc.getDocumentElement().normalize();
 
 			try {
 				retval.solarradiation = doc.getElementsByTagName("solar_radiation").item(0).getChildNodes().item(0).getNodeValue();
 				retval.uv = doc.getElementsByTagName("UV").item(0).getChildNodes().item(0).getNodeValue();
 			} catch (Exception e) {
 				// do nothing
 			}
 			
 			try {
 				retval.precipitation1hr = doc.getElementsByTagName("precip_1hr_in").item(0).getChildNodes().item(0).getNodeValue();
 				retval.precipitationtoday = doc.getElementsByTagName("precip_today_in").item(0).getChildNodes().item(0).getNodeValue();
 			} catch (Exception e) {
 				// do nothing
 			}
 			
 			try {
 				if (doc.getElementsByTagName("neighborhood").item(0).hasChildNodes()) {
 					retval.neighborhood = doc.getElementsByTagName("neighborhood").item(0).getChildNodes().item(0).getNodeValue();
 				}
 				else {
 					if (doc.getElementsByTagName("city").item(0).hasChildNodes()) {
 						retval.neighborhood = doc.getElementsByTagName("city").item(0).getChildNodes().item(0).getNodeValue();
 						if (doc.getElementsByTagName("state").item(0).hasChildNodes()) {
 							retval.neighborhood += ", " + doc.getElementsByTagName("state").item(0).getChildNodes().item(0).getNodeValue();
 						}
 					}
 					else {
 						retval.neighborhood = pwsid;
 					}
 				}
 			} catch (Exception e) {
 				retval.neighborhood = pwsid;
 			}
 
 			try {
 				retval.updated = doc.getElementsByTagName("observation_time").item(0).getChildNodes().item(0).getNodeValue();
 				retval.temperature = doc.getElementsByTagName("temp_f").item(0).getChildNodes().item(0).getNodeValue();
 				retval.humidity = doc.getElementsByTagName("relative_humidity").item(0).getChildNodes().item(0).getNodeValue();
 				retval.winddirection = doc.getElementsByTagName("wind_dir").item(0).getChildNodes().item(0).getNodeValue();
 				retval.windspeed = doc.getElementsByTagName("wind_mph").item(0).getChildNodes().item(0).getNodeValue();
 				retval.dewpoint = doc.getElementsByTagName("dewpoint_f").item(0).getChildNodes().item(0).getNodeValue();
 				retval.pressure = doc.getElementsByTagName("pressure_in").item(0).getChildNodes().item(0).getNodeValue();
 			} catch (Exception e) {
 				return null;
 			}
 			
 
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 		return retval;
 	}
 	
 	
 	public void geoHelper() {
 		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		
 		List<String> providers = locationManager.getAllProviders();
 		for (String provider : providers) {
 			printProvider(provider);
 		}
 
 		Criteria criteria = new Criteria();
 		bestProvider = locationManager.getBestProvider(criteria, false);
 		Log.d("WUNDERLOC", "BEST Provider:");
 		printProvider(bestProvider);
 		
 		locationManager.requestLocationUpdates(bestProvider, 20000, 1, this);
 	} 
 	
 	/** Register for the updates when Activity is in foreground */
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	/** Stop the updates when Activity is paused */
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	public void onLocationChanged(Location location) {
 		printLocation(location);
 		
 		String tmpPWSName[] = null;
 		String tmpPWSID[] = null;
 		
 		if (location == null) {
 			// Failed
 			Toast.makeText(getBaseContext(),"Location Failure...", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		else {
 			try {
 			HttpParams params = new BasicHttpParams();
 			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 			HttpProtocolParams.setContentCharset(params, "UTF-8");
 			HttpProtocolParams.setUseExpectContinue(params, true);
 			HttpProtocolParams.setHttpElementCharset(params, "UTF-8");
 			HttpProtocolParams.setUserAgent(params, "java (wunder for android)");
 
 			DefaultHttpClient client = new DefaultHttpClient(params);
 
 			InputStream data = null;
 			
 			try {
 
 				HttpGet method = new HttpGet(
 						"http://iphone.smerty.com/wunder/wx_near_all.php?lat=" + location.getLatitude() + "&lon=" + location.getLongitude());
 				HttpResponse res = client.execute(method);
 				data = res.getEntity().getContent();
 				
 			} catch (IOException e) {
 				e.printStackTrace();
 				 Toast.makeText(getBaseContext(),"Network Failure...", Toast.LENGTH_SHORT).show();
 
 			}
 			Document doc = null;
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder db;
 
 			try {
 				db = dbf.newDocumentBuilder();
 				doc = db.parse(data);
 				// finish();
 			} catch (SAXParseException e) {
 				e.printStackTrace();
 				Toast.makeText(getBaseContext(), "SAXParseException, bad XML?", Toast.LENGTH_SHORT).show();
 				return;
 				// finish();
 			} catch (SAXException e) {
 				// TODO Auto-generated catch block
 				Toast.makeText(getBaseContext(), "SAXException",
 						Toast.LENGTH_SHORT).show();
 				e.printStackTrace();
 				return;
 				// finish();
 			} catch (ParserConfigurationException e) {
 				// TODO Auto-generated catch block
 				Toast.makeText(getBaseContext(), "ParserConfigurationException", Toast.LENGTH_SHORT).show();
 				e.printStackTrace();
 				return;
 			}
 
 			if (doc != null) {
 				doc.getDocumentElement().normalize();
 			}
 			else {
 				Toast.makeText(getBaseContext(), "doc is null", Toast.LENGTH_SHORT).show();
 				return;
 			}
 			
 			try {
 
				for (int i = 0; i < doc.getElementsByTagName("neighborhood").getLength(); i++) {
 					tmpPWSName[i] = doc.getElementsByTagName("neighborhood").item(i).getChildNodes().item(0).getNodeValue().replaceAll("\\s+", " ");
 					tmpPWSID[i] = doc.getElementsByTagName("id").item(i).getChildNodes().item(0).getNodeValue();
 				}
 
 			} catch (Exception e) {
 				// do nothing
 			}
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 			
 		}
 		locationManager.removeUpdates(this);
 		
 		AlertDialog.Builder alertPWSList = new AlertDialog.Builder(this);
 
 		alertPWSList.setTitle("Nearby PWS");
 
 		
 		final String PWSName[] = tmpPWSName;
 		final String PWSid[] = tmpPWSID;
 		
 		
 		final Wunder thatPWSList = this;
 		
 		
 		alertPWSList.setSingleChoiceItems(PWSName, -1, new DialogInterface.OnClickListener() {
 
             public void onClick(DialogInterface dialog, int item) {
 
             	thatPWSList.selectedID = PWSid[item];
 
             }
 
         });
 
 		
 		alertPWSList.setPositiveButton("Set",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog,
 							int whichButton) {
 						String value = thatPWSList.selectedID.toUpperCase();
 
 						SharedPreferences settings = getSharedPreferences(
 								PREFS_NAME, 0);
 						String pwsid = settings.getString("stationID",
 								"KCALIVER14");
 
 						SharedPreferences.Editor editor = settings.edit();
 						editor.putString("stationID", value);
 						editor.commit();
 
 						if (!pwsid.equalsIgnoreCase(value)) {
 							Toast.makeText(getBaseContext(),
 									"Changed: " + value, Toast.LENGTH_LONG)
 									.show();
 
 						}
 						
 						thatPWSList.onCreate(null);
 						
 						
 					}
 				});
 
 		alertPWSList.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog,
 							int whichButton) {
 						// Canceled.
 					}
 				});
 		
 
 		alertPWSList.setIcon(R.drawable.icon);
 
 			alertPWSList.show();
 		
 	}
 
 	public void onProviderDisabled(String provider) {
 		Log.d("WUNDERLOC", "Provider Disabled: " + provider);
 	}
 
 	public void onProviderEnabled(String provider) {
 		Log.d("WUNDERLOC", "Provider Enabled: " + provider);
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		Log.d("WUNDERLOC", "Provider Status Changed: " + provider + ", Status="
 			+ S[status] + ", Extras=" + extras);
 	}
 
 	private void printProvider(String provider) {
 		LocationProvider info = locationManager.getProvider(provider);
 		Log.d("WUNDERLOC", info.toString());
 	}
 
 	private void printLocation(Location location) {
 		if (location == null) {
 			Log.d("WUNDERLOC", "Location[unknown]");
 		}
 		else {
 			Log.d("WUNDERLOC", location.toString());
 		}
 	}
 
 	
 }
