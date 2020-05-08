 package com.example.android_etherboard;
 
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Picture;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements SensorEventListener {
 
 	private static final float X_MAX_ANGLE = 45.0f;
 	private static final float Y_MAX_ANGLE = 45.0f;
 	private static final int PIXEL_FUDGE = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		loadSensor();
 
 		loadUIReferences();
 		loadWebView();
 	}
 
 	private static Sensor getFirstOfType(int type, SensorManager mSensorManager) {
 		List<Sensor> sensors = mSensorManager.getSensorList(type);
 		if (sensors.isEmpty()) {
 			System.out.println("There is no sensor of type " + type + " :(");
 			return null;
 		} else {
 			Sensor s = sensors.get(0);
 			System.out.println("Yeah, there is a sensor! (" + sensors.size()
 					+ " to be exact).  We're gonna go with " + s.getName());
 			return s;
 		}
 	}
 
 	private Sensor accelerometer;
 
 	float[] adjusted = new float[3];
 	float[] gravity = null;
 
 	TextView sensorText;
 	TextView infoText;
 
 	long lastTimestamp = 0;
 	float[] linear_acceleration = new float[3];
 
 	private SensorManager mSensorManager;
 	double pos = 0;
 	int times = 0;
 
 	private WebView webView;
 
 	private float calibrationX;
 
 	private float calibrationY;
 
 	private float[] currentReadings;
 
 	private int viewHeight;
 
 	private int viewWidth;
 	private Object recordedUrlPreference;
 
 	private String format(float val) {
 		return String.format("%06.2f", val);
 	}
 
 	private void loadUIReferences() {
 		if (sensorText == null) {
 			sensorText = (TextView) findViewById(R.id.sensor);
 			infoText = (TextView) findViewById(R.id.pageInfo);
 			webView = (WebView) findViewById(R.id.web_engine);
 		}
 	}
 
 	private float length(float[] gravity2) {
 		float ss = 0;
 		for (float f : gravity2) {
 			ss += f * f;
 		}
 		return (float) Math.sqrt(ss);
 	}
 
 	private void loadSensor() {
 		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		accelerometer = getFirstOfType(Sensor.TYPE_ORIENTATION, mSensorManager);
 	}
 
 	@SuppressWarnings("deprecation")
 	@SuppressLint("SetJavaScriptEnabled")
 	private void loadWebView() {
 		WebSettings settings = webView.getSettings();
 		settings.setJavaScriptEnabled(true);
 		settings.setBuiltInZoomControls(true);
 		settings.setPluginsEnabled(true);
 
 		webView.addJavascriptInterface(new WebSocketFactory(webView), "WebSocketFactory");
 		webView.setWebChromeClient(new WebChromeClient());
 
 		webView.setWebViewClient(new WebViewClient() {
 			@Override
 			public void onReceivedError(WebView view, int errorCode,
 					String description, String failingUrl) {
 				// Handle the error
 			}
 
 			@Override
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				view.loadUrl(url);
 				return true;
 			}
 
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				super.onPageFinished(view, url);
 				getSize(view);
 			}
 
 			@Override
 			public void onScaleChanged(WebView view, float oldScale,
 					float newScale) {
 				super.onScaleChanged(view, oldScale, newScale);
 				getSize(view);
 			}
 		});
 
 		setUrl();
 	}
 
 	private void setUrl() {
 		webView.clearView();
 		String url = getUrlPreference();
 		Toast.makeText(getApplicationContext(), "Loading " + url, Toast.LENGTH_LONG).show();
 		recordedUrlPreference = url;
 		webView.loadUrl(url);
 	}
 
 	private String getUrlPreference() {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		String url = preferences.getString(SettingsActivity.CONFIG_URL_KEY, SettingsActivity.CONFIG_DEFAULT_URL);
 		return url;
 	}
 
 	private void getSize(WebView view) {
 		if (view != null) {
 			float scale = view.getScale();
 
 			Picture picture = view.capturePicture();
 			if (picture != null) {
				viewHeight = (int)(picture.getHeight() * scale) + PIXEL_FUDGE;
				viewWidth = (int)(picture.getWidth() * scale) + PIXEL_FUDGE;
 				infoText.setText("[" + viewWidth + " x " + viewHeight + "] (" + scale + ")");
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mSensorManager.unregisterListener(this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		recalibrate();
 		loadUIReferences();
 		mSensorManager.registerListener(this, accelerometer,
 				SensorManager.SENSOR_DELAY_NORMAL);
 		
 		if(urlHasBeenChanged()) {
 			setUrl();
 		}
 	}
 
 	private boolean urlHasBeenChanged() {
 		String currentUrlPreference = getUrlPreference();
 		boolean changed = !currentUrlPreference.equals(recordedUrlPreference);
 		return changed;
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		currentReadings = event.values;
 		float rawCompass = event.values[0];
 		float rawPitch = event.values[1];
 
 		float xPercent = xCalibrate(rawCompass);
 		float yPercent = yCalibrate(rawPitch);
 
 		adjustWebView(xPercent, yPercent);
 	}
 
 	private void adjustWebView(float xPercent, float yPercent) {
 		if (webView != null) {
 			int moveToX = (int) (viewWidth * xPercent);
 			int moveToY = (int) (viewHeight * yPercent);
 
 			if (sensorText != null) {
 				sensorText.setText("moving to (" + moveToX + "," + moveToY + ")");
 			}
 
 			webView.scrollTo(moveToX, moveToY);
 		}
 	}
 
 	private float xCalibrate(float rawCompass) {
 		// zero is leftmost corner. rawCompass goes up when rotated to the
 		// right.
 		// zero the compass and allow a span of 60 degrees to span the whole
 		// board.
 		return bound(normalize360(rawCompass - calibrationX), 0, X_MAX_ANGLE)
 				/ X_MAX_ANGLE;
 	}
 
 	private float bound(float value, float lowerLimit, float upperLimit) {
 		return Math.max(lowerLimit, Math.min(upperLimit, value));
 	}
 
 	private float yCalibrate(float rawPitch) {
 		// zero is desired at topmost corner. rawPitch goes up when the phone is
 		// tilted down (it is a higher y value in the webView)
 		// allow a span of 60 degrees to span the whole board.
 		return bound(normalize360(rawPitch - calibrationY), 0, Y_MAX_ANGLE)
 				/ Y_MAX_ANGLE;
 	}
 
 	private float normalize360(float f) {
 		while (f < -180) {
 			f += 360;
 		}
 		while (f > 180) {
 			f -= 360;
 		}
 		return f;
 	}
 
 	private void recalibrate() {
 		if (currentReadings != null) {
 			calibrationX = currentReadings[0];
 			calibrationY = currentReadings[1];
 			System.out.println("current readings is "
 					+ vectorToString(currentReadings));
 		}
 
 		getSize(webView);
 
 		if (webView != null) {
 			webView.scrollTo(0, 0);
 		}
 	}
 
 	private String vectorToString(float[] vector) {
 		return "(" + format(vector[0]) + "," + format(vector[1]) + ","
 				+ format(vector[2]) + ") -> " + format(length(vector));
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_recalibrate:
 				recalibrate();
 				Toast.makeText(getApplicationContext(), "Top Left is now here.", Toast.LENGTH_SHORT).show();
 				break;
 			case R.id.menu_settings:
 				startActivity(new Intent(this.getApplicationContext(), SettingsActivity.class));
 				break;
 			default:
 				Toast.makeText(getApplicationContext(), "what?", Toast.LENGTH_SHORT).show();
 		}
 
 		return true;
 	}
 	
 }
