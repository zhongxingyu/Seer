 /*
  *   Copyright 2013 Candace Bain
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package com.candacebain.lakemendotabuoy;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Xml;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	/**
 	 * The source of all our data
 	 */
 	private static final String dataQueryURL = "http://metobs.ssec.wisc.edu/app/mendota/buoy/data/xml?symbols=t:rh:td:spd:dir:gust:wt_0.0:wt_1.0:wt_5.0:wt_10.0:wt_15.0:wt_20.0:do_ppm:do_sat:chlor:pc";
 
 	/**
 	 * The data identifiers from the metobs XML
 	 */
 	private static final String AIR_TEMP = "AIR_TEMP";
 	private static final String REL_HUM = "REL_HUM";
 
 	private static final String WIND_SPEED = "WIND_SPEED_2.0";
 	private static final String WIND_DIRECTION = "WIND_DIRECTION_2.0";
 	private static final String WIND_GUST = "WIND_GUST";
 	private static final String TIMESTAMP = "timestamp";
 
 	private static final String WATER_TEMP_0 = "WATER_TEMP_0.0";
 	private static final String WATER_TEMP_1 = "WATER_TEMP_1.0";
 	private static final String WATER_TEMP_5 = "WATER_TEMP_5.0";
 	private static final String WATER_TEMP_10 = "WATER_TEMP_10.0";
 	private static final String WATER_TEMP_15 = "WATER_TEMP_15.0";
 	private static final String WATER_TEMP_20 = "WATER_TEMP_20.0";
 
 	private static final String DEWPOINT_CALC = "DEWPOINT_CALC";
 	private static final String DO_SAT = "DO_SAT";
 	private static final String DO_PPM = "DO_PPM";
 	private static final String CHLOROPHYLL = "CHLOROPHYLL_0.4";
 	private static final String PHYCOCYANIN = "PHYCOCYANIN_0.4";
 
 	/**
 	 * Used to know when we've returned from the settings view
 	 */
 	private static final int SETTINGS_RESPONSE = 1;
 
 	/**
 	 * Format double values for display
 	 */
 	private static final DecimalFormat decimalFormat = new DecimalFormat(
 			"##0.0");
 
 	/**
 	 * Parse and format date values
 	 */
 	private static final SimpleDateFormat inputDateFormat = new SimpleDateFormat(
 			"yyyy-MM-dd HH:mm:ss", Locale.US);
 	private static final SimpleDateFormat outputDateFormat = new SimpleDateFormat(
 			"MMM dd, yyyy h:mm a", Locale.US);
 
 	/**
 	 * Wind directions text array
 	 */
 	private String[] windDirections = null;
 
 	/**
 	 * Views we might want to interact with
 	 */
 	private TextView windDirection;
 	private TextView windSpeed;
 	private TextView windGust;
 	private TextView airTemperature;
 	private TextView humidity;
 
 	private TextView surfaceTemperature;
 	private TextView oneMeterTemperature;
 	private TextView fiveMeterTemperature;
 	private TextView tenMeterTemperature;
 	private TextView fifteenMeterTemperature;
 	private TextView twentyMeterTemperature;
 
 	private TextView oneMeterCaption;
 	private TextView fiveMeterCaption;
 	private TextView tenMeterCaption;
 	private TextView fifteenMeterCaption;
 	private TextView twentyMeterCaption;
 
 	private TextView dewPoint;
 	private TextView dissolvedOxygen;
 	private TextView dissolvedOxygenSaturation;
 	private TextView chlorophyll;
 	private TextView phycocyanin;
 
 	private TextView additionalDataCaption;
 	private View additionalDataLine;
 	private TableLayout additionalDataTable;
 
 	private TextView updatedAt;
 	private ProgressBar updateProgressBar;
 
 	private Timer updateTimer;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreate()
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Get values from resources
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 		if (windDirections == null) {
 			windDirections = getResources().getStringArray(
 					R.array.windDirections);
 		}
 		setContentView(R.layout.activity_main);
 
 		// Set up our views
 		initializeViews();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onPause()
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		// Stop updating our data
 		stopUpdateTimer();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// Start updating our data
 		startUpdateTimer();
 	}
 
 	/**
 	 * Set up our options menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/**
 	 * Respond to options actions
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			startActivityForResult(new Intent(this, SettingsActivity.class),
 					SETTINGS_RESPONSE);
 			return true;
 		case R.id.action_about:
 			displayAboutDialog();
 			return true;
 		case R.id.action_update:
 			updateData();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * If the user has just returned from the settings menu, update our
 	 * measurement units and restart the update timer
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		switch (requestCode) {
 		case SETTINGS_RESPONSE:
 			setWaterDepthCaptions();
 			setAdditionalDataVisibility();
 			startUpdateTimer();
 			break;
 		}
 	}
 
 	/**
 	 * Find the views we want to interact with while the application is running
 	 */
 	private void initializeViews() {
 		inputDateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
 		windDirection = (TextView) findViewById(R.id.wind_direction);
 		windSpeed = (TextView) findViewById(R.id.wind_speed);
 		windGust = (TextView) findViewById(R.id.wind_gust);
 		airTemperature = (TextView) findViewById(R.id.air_temperature);
 		humidity = (TextView) findViewById(R.id.humidity);
 
 		surfaceTemperature = (TextView) findViewById(R.id.surface_temperature);
 		oneMeterTemperature = (TextView) findViewById(R.id.one_meter_temperature);
 		fiveMeterTemperature = (TextView) findViewById(R.id.five_meter_temperature);
 		tenMeterTemperature = (TextView) findViewById(R.id.ten_meter_temperature);
 		fifteenMeterTemperature = (TextView) findViewById(R.id.fifteen_meter_temperature);
 		twentyMeterTemperature = (TextView) findViewById(R.id.twenty_meter_temperature);
 
 		oneMeterCaption = (TextView) findViewById(R.id.one_meter_temperature_caption);
 		fiveMeterCaption = (TextView) findViewById(R.id.five_meter_temperature_caption);
 		tenMeterCaption = (TextView) findViewById(R.id.ten_meter_temperature_caption);
 		fifteenMeterCaption = (TextView) findViewById(R.id.fifteen_meter_temperature_caption);
 		twentyMeterCaption = (TextView) findViewById(R.id.twenty_meter_temperature_caption);
 
 		dewPoint = (TextView) findViewById(R.id.dew_point);
 		dissolvedOxygen = (TextView) findViewById(R.id.dissolved_oxygen);
 		dissolvedOxygenSaturation = (TextView) findViewById(R.id.dissolved_oxygen_saturation);
 		chlorophyll = (TextView) findViewById(R.id.chlorophyll);
 		phycocyanin = (TextView) findViewById(R.id.phycocyanin);
 
 		additionalDataCaption = (TextView) findViewById(R.id.additional_data_caption);
 		additionalDataLine = (View) findViewById(R.id.additional_data_line);
 		additionalDataTable = (TableLayout) findViewById(R.id.additional_data_table);
 
 		updatedAt = (TextView) findViewById(R.id.updated_at);
 		updateProgressBar = (ProgressBar) findViewById(R.id.update_progress_bar);
 
 		setWaterDepthCaptions();
 		setAdditionalDataVisibility();
 	}
 
 	/**
 	 * Changes the water depth captions based on the user's preferred distance
 	 * units
 	 */
 	private void setWaterDepthCaptions() {
 		String distanceUnits = PreferenceManager.getDefaultSharedPreferences(
 				this).getString(SettingsActivity.KEY_PREF_DISTANCE_UNITS, "");
 		if (distanceUnits.equals(getString(R.string.pref_distanceUnits_feet))) {
 			oneMeterCaption
 					.setText(getString(R.string.one_meter_temperature_caption_in_feet));
 			fiveMeterCaption
 					.setText(getString(R.string.five_meter_temperature_caption_in_feet));
 			tenMeterCaption
 					.setText(getString(R.string.ten_meter_temperature_caption_in_feet));
 			fifteenMeterCaption
 					.setText(getString(R.string.fifteen_meter_temperature_caption_in_feet));
 			twentyMeterCaption
 					.setText(getString(R.string.twenty_meter_temperature_caption_in_feet));
 		} else if (distanceUnits
 				.equals(getString(R.string.pref_distanceUnits_meters))) {
 			oneMeterCaption
 					.setText(getString(R.string.one_meter_temperature_caption));
 			fiveMeterCaption
 					.setText(getString(R.string.five_meter_temperature_caption));
 			tenMeterCaption
 					.setText(getString(R.string.ten_meter_temperature_caption));
 			fifteenMeterCaption
 					.setText(getString(R.string.fifteen_meter_temperature_caption));
 			twentyMeterCaption
 					.setText(getString(R.string.twenty_meter_temperature_caption));
 		}
 	}
 
 	/**
 	 * Changes the visibility of the additional data based on the user's
 	 * preferences
 	 */
 	private void setAdditionalDataVisibility() {
 		boolean showAdditionalData = PreferenceManager
 				.getDefaultSharedPreferences(this).getBoolean(
 						SettingsActivity.KEY_PREF_DISPLAY_ADDITIONAL_DATA,
 						false);
 		if (showAdditionalData) {
 			additionalDataCaption.setVisibility(android.view.View.VISIBLE);
 			additionalDataLine.setVisibility(android.view.View.VISIBLE);
 			additionalDataTable.setVisibility(android.view.View.VISIBLE);
 		} else {
 			additionalDataCaption.setVisibility(android.view.View.GONE);
 			additionalDataLine.setVisibility(android.view.View.GONE);
 			additionalDataTable.setVisibility(android.view.View.GONE);
 		}
 	}
 
 	/**
 	 * Sets up a timer to update the data on the schedule preferred by the user
 	 */
 	private void startUpdateTimer() {
 
 		stopUpdateTimer();
 		updateTimer = new Timer();
 
 		int updateInterval = Integer.parseInt(PreferenceManager
 				.getDefaultSharedPreferences(this).getString(
 						SettingsActivity.KEY_PREF_UPDATE_INTERVAL, ""));
 		if (updateInterval > 0) {
 			// Update data now and at the interval specified by the user
 			updateTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							updateData();
 						}
 					});
 				}
 			}, 0, updateInterval);
 		} else {
 			updateData();
 		}
 	}
 
 	/**
 	 * Stop updating when we're no longer focused on this view
 	 */
 	private void stopUpdateTimer() {
 		if (updateTimer != null) {
 			updateTimer.cancel();
 			updateTimer = null;
 		}
 	}
 
 	/**
 	 * Format the wind direction value
 	 * 
 	 * @param windDirectionInDegrees
 	 * @return
 	 */
 	String formatWindDirection(double windDirectionInDegrees) {
 		if (windDirectionInDegrees <= 0 || windDirectionInDegrees > 360) {
 			return getString(R.string.unknown_value);
 		}
 
 		// deal with special north case
 		if ((windDirectionInDegrees >= 348.75 && windDirectionInDegrees <= 360)
 				|| windDirectionInDegrees > 0 && windDirectionInDegrees < 11.25)
 			return getString(R.string.wind_direction_N);
 
 		double i = 11.25; // 1/2 degree increment between the directions
 		for (int j = 0; j < windDirections.length; j++) {
 			if (windDirectionInDegrees >= i
 					&& windDirectionInDegrees < (i + 22.5)) {
 				return windDirections[j];
 			}
 			i += 22.5;
 		}
 
 		// shouldn't ever get here
 		return getString(R.string.unknown_value);
 	}
 
 	/**
 	 * Format the temperature value according to the user's preferences,
 	 * converting units if necessary
 	 * 
 	 * @param windDirectionInDegrees
 	 * @return
 	 */
 	private String formatTemperature(double value) {
 		// What units should we use to display temperature?
 		String temperatureUnits = PreferenceManager
 				.getDefaultSharedPreferences(this).getString(
 						SettingsActivity.KEY_PREF_TEMPERATURE_UNITS, "");
 		boolean isTemperatureFahrenheit = temperatureUnits
 				.equals(getString(R.string.pref_temperatureUnits_fahrenheit));
 
 		String temperatureUnitSuffix = getString(R.string.celsius_suffix);
 		if (isTemperatureFahrenheit) {
 			temperatureUnitSuffix = getString(R.string.fahrenheit_suffix);
 		}
 
 		double useValue = value;
 		if (isTemperatureFahrenheit) {
 			useValue = ConversionUtil.celsiusToFahrenheit(value);
 		}
 
 		return (decimalFormat.format(useValue) + temperatureUnitSuffix);
 	}
 
 	/**
 	 * Format the wind speed according to the user's preferences, converting
 	 * units if necessary
 	 * 
 	 * @param value
 	 * @return
 	 */
 	private String formatWindSpeed(double value) {
 		// What units should we use to display wind speed?
 		String windSpeedUnits = PreferenceManager.getDefaultSharedPreferences(
 				this).getString(SettingsActivity.KEY_PREF_WIND_SPEED_UNITS, "");
 		boolean isWindSpeedMph = windSpeedUnits
 				.equals(getString(R.string.pref_windSpeedUnits_milesPerHour));
 		boolean isWindSpeedKnots = windSpeedUnits
 				.equals(getString(R.string.pref_windSpeedUnits_knots));
 
 		// Set up the suffixes for the units
 		String windSpeedUnitSuffix = " "
 				+ getString(R.string.meters_per_second_suffix);
 		if (isWindSpeedMph) {
 			windSpeedUnitSuffix = " "
 					+ getString(R.string.miles_per_hour_suffix);
 		} else if (isWindSpeedKnots) {
 			windSpeedUnitSuffix = " " + getString(R.string.knots_suffix);
 		}
 
 		double useValue = value;
 		if (isWindSpeedMph) {
 			useValue = ConversionUtil.metersPerSecondToMilesPerHour(value);
 		} else if (isWindSpeedKnots) {
 			useValue = ConversionUtil.metersPerSecondToKnots(value);
 		}
 
 		return (decimalFormat.format(useValue) + windSpeedUnitSuffix);
 	}
 
 	/**
 	 * Format the humidity value for display
 	 * 
 	 * @param value
 	 * @return
 	 */
 	private String formatHumidity(double value) {
 		String humidityUnitSuffix = getString(R.string.percent_suffix);
 		return (decimalFormat.format(value) + " " + humidityUnitSuffix);
 	}
 
 	/**
 	 * Display the data returned from the service
 	 * 
 	 * @param result
 	 */
 	private void displayData(Map<String, Double> result) {
 		if (result.containsKey(WIND_DIRECTION)) {
 			windDirection.setText(formatWindDirection(result
 					.get(WIND_DIRECTION)));
 		}
 		if (result.containsKey(WIND_SPEED)) {
 			windSpeed.setText(formatWindSpeed(result.get(WIND_SPEED)));
 		}
 		if (result.containsKey(WIND_GUST)) {
 			windGust.setText(formatWindSpeed(result.get(WIND_GUST)));
 		}
 		if (result.containsKey(AIR_TEMP)) {
 			airTemperature.setText(formatTemperature(result.get(AIR_TEMP)));
 		}
 		if (result.containsKey(REL_HUM)) {
 			humidity.setText(formatHumidity(result.get(REL_HUM)));
 		}
 		if (result.containsKey(WATER_TEMP_0)) {
 			surfaceTemperature.setText(formatTemperature(result
 					.get(WATER_TEMP_0)));
 		}
 		if (result.containsKey(WATER_TEMP_1)) {
 			oneMeterTemperature.setText(formatTemperature(result
 					.get(WATER_TEMP_1)));
 		}
 		if (result.containsKey(WATER_TEMP_5)) {
 			fiveMeterTemperature.setText(formatTemperature(result
 					.get(WATER_TEMP_5)));
 		}
 		if (result.containsKey(WATER_TEMP_10)) {
 			tenMeterTemperature.setText(formatTemperature(result
 					.get(WATER_TEMP_10)));
 		}
 		if (result.containsKey(WATER_TEMP_15)) {
 			fifteenMeterTemperature.setText(formatTemperature(result
 					.get(WATER_TEMP_15)));
 		}
 		if (result.containsKey(WATER_TEMP_20)) {
 			twentyMeterTemperature.setText(formatTemperature(result
 					.get(WATER_TEMP_20)));
 		}
 		if (result.containsKey(DEWPOINT_CALC)) {
 			dewPoint.setText(formatTemperature(result.get(DEWPOINT_CALC)));
 		}
 		if (result.containsKey(DO_SAT)) {
 			dissolvedOxygen.setText(decimalFormat.format(result.get(DO_SAT)));
 		}
 		if (result.containsKey(DO_PPM)) {
 			dissolvedOxygenSaturation.setText(decimalFormat.format(result
 					.get(DO_PPM)));
 		}
 		if (result.containsKey(CHLOROPHYLL)) {
 			chlorophyll.setText(decimalFormat.format(result.get(CHLOROPHYLL)));
 		}
 		if (result.containsKey(PHYCOCYANIN)) {
 			phycocyanin.setText(decimalFormat.format(result.get(PHYCOCYANIN)));
 		}
 		if (result.containsKey(TIMESTAMP)) {
 			Date dateTime = new Date(Math.round(result.get(TIMESTAMP)));
 			updatedAt.setText(outputDateFormat.format(dateTime));
 		}
 	}
 
 	/**
 	 * Something went wrong, tell the user what
 	 * 
 	 * @param message
 	 */
 	private void displayError(String message) {
 		Context context = getApplicationContext();
 		int duration = Toast.LENGTH_LONG;
 		Toast toast = Toast.makeText(context, message, duration);
 		toast.show();
 	}
 
 	/**
 	 * Display dialog with information about the application
 	 */
 	private void displayAboutDialog() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		// Get the layout inflater
 		LayoutInflater inflater = getLayoutInflater();
 
 		// Inflate and set the layout for the dialog
 		// Pass null as the parent view because its going in the dialog layout
 		builder.setView(inflater.inflate(R.layout.about, null));
 		builder.setPositiveButton(R.string.ok,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// Just close the dialog
 					}
 				});
 
 		Dialog dialog = builder.create();
 		dialog.show();
 	}
 
 	/**
 	 * Connect to the web service, download and parse the data
 	 */
 	private void updateData() {
 		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			updateProgressBar.setVisibility(android.view.View.VISIBLE);
 			new DownloadDataTask().execute();
 		} else {
 			displayError(getString(R.string.error_cannot_show_data) + "\n"
 					+ getString(R.string.error_no_network_connection));
 		}
 	}
 
 	/**
 	 * Uses AsyncTask to download the data away from the main thread.
 	 */
 	private class DownloadDataTask extends
 			AsyncTask<Void, Void, Map<String, Double>> {
 
 		Throwable throwable = null;
 
 		@Override
 		protected Map<String, Double> doInBackground(Void... params) {
 			try {
 				return downloadData();
 			} catch (IOException e) {
 				throwable = e;
 				return null;
 			} catch (ParseException e) {
 				throwable = e;
 				return null;
 			} catch (XmlPullParserException e) {
 				throwable = e;
 				return null;
 			}
 		}
 
 		/**
 		 * onPostExecute displays the results of the AsyncTask.
 		 */
 		@Override
 		protected void onPostExecute(Map<String, Double> result) {
 			if (throwable == null) {
 				displayData(result);
 			} else {
 				displayError(getString(R.string.error_cannot_show_data) + "\n"
 						+ throwable.getMessage());
 			}
 			updateProgressBar.setVisibility(android.view.View.INVISIBLE);
 		}
 	}
 
 	/**
 	 * Connects to the metobs web service, downloads data in XML, returns the
 	 * most recent values in a string key -> double value map
 	 * 
 	 * @return
 	 * @throws IOException
 	 * @throws ParseException
 	 * @throws XmlPullParserException
 	 */
 	private Map<String, Double> downloadData() throws IOException,
 			ParseException, XmlPullParserException {
 		InputStream is = null;
 		Map<String, Double> entries = new HashMap<String, Double>();
 		try {
 			URL url = new URL(dataQueryURL);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setReadTimeout(10000 /* milliseconds */);
 			conn.setConnectTimeout(15000 /* milliseconds */);
 			conn.setRequestMethod("GET");
 			conn.setDoInput(true);
 			// Starts the query
 			conn.connect();
 			is = conn.getInputStream();
 
 			XmlPullParser parser = Xml.newPullParser();
 			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
 			parser.setInput(is, null);
 			parser.nextTag();
 
 			String dataType = null;
 
 			parser.require(XmlPullParser.START_TAG, null, "metobs");
 			while (parser.next() != XmlPullParser.END_DOCUMENT) {
 
 				switch (parser.getEventType()) {
 				case XmlPullParser.START_TAG:
 					String name = parser.getName();
 					if (name.equals("data")) {
 						dataType = parser.getAttributeValue(null, "symbol");
 					} else if (name.equals("timestamp")) {
 						dataType = name;
 					}
 					break;
 				case XmlPullParser.TEXT:
 					if (dataType != null) {
 						String text = parser.getText();
 						String[] values = text.split(",");
 						if (dataType.equals("timestamp")) {
 							// Store timestamp value
 							Date dateTime = inputDateFormat
 									.parse(values[values.length - 1]);
 							entries.put(dataType, (double) dateTime.getTime());
 						} else {
							// Store most recent double value
							entries.put(dataType, Double
									.parseDouble(values[values.length - 1]));
 						}
 					}
 					break;
 				case XmlPullParser.END_TAG:
 					dataType = null;
 					break;
 				}
 			}
 		} finally {
 			// Makes sure that the InputStream is closed after the app is
 			// finished using it.
 			if (is != null) {
 				is.close();
 			}
 		}
 		return entries;
 	}
 }
