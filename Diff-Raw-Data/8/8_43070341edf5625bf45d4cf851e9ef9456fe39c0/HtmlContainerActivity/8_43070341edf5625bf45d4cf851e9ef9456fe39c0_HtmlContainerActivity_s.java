 /**
  * (c) 2011-2012 Sense Tecnic Systems Inc.   All rights reserved.
  */
 
 package com.sensetecnic.container;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 import com.slidingmenu.lib.SlidingMenu;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 
 import br.ufscar.dc.thingbroker.interfaces.ThingBrokerRequestListener;
 import br.ufscar.dc.thingbroker.model.Event;
 import br.ufscar.dc.thingbroker.services.EventService;
 import br.ufscar.dc.thingbroker.services.impl.EventServiceImpl;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Looper;
 import android.provider.MediaStore;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.GeolocationPermissions;
 import android.webkit.JsResult;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.LinearLayout.LayoutParams;
 
 public class HtmlContainerActivity extends SlidingFragmentActivity {
 
 	private WebView webView;
 	private String callbackUrl;
 	private String nfc_upload;
 	private String currentUrl;
 	private int accelerometerPeriod = 1000;
 	private boolean accelerometer_enabled = false;
 	private JSONArray appList;
 	private String displayID = "1";
 	
 	private ProgressBar pbLoading;
 
 	public ProgressDialog pd; 
 	public static final int MEDIA_TYPE_IMAGE = 1;
 	
 	// Constant codes for the ThingBroker event recognition
 	public static final int STANDARD_MESSAGE = 1;
 	
 	// For uploading accelerometer data periodically
 	AccelerometerUploader accelerometerUploader;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		final String OVERRIDE_PREFIX = getString(R.string.uri_prefix);
		initAppList();
 		
 		// Initialise Sensor management information 
         accelerometerUploader = new AccelerometerUploader(getString(R.string.upload_url), this);
 		
 		setContentView(R.layout.html_challenge);
 		
 		initAppList();
 		
 		SlidingMenu menu = getSlidingMenu();
         menu.setMode(SlidingMenu.LEFT);
         menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
         menu.setShadowWidthRes(R.dimen.shadow_width);
         menu.setShadowDrawable(R.drawable.shadow);
         menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
         menu.setFadeDegree(0.35f);
 		
 		pbLoading = (ProgressBar)findViewById(R.id.pbLoading);
 		webView = (WebView) findViewById(R.id.webview);
 		webView.getSettings().setJavaScriptEnabled(true);
 		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
 		webView.setWebChromeClient(new ContainerWebChromeClient());
 		
 		// TODO see if this hack is avoidable
 		final Activity activity = this;
 		webView.setWebViewClient(new WebViewClient() {
 			@Override
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				URLParser parser = new URLParser(url);
 				if (parser.isSpecialURL()) {
 					String uploadURL = getUploadURL(parser.getThingID(), displayID);
 					if (parser.getDevice() == URLParser.DEVICE_ACCELEROMTER) {
 						if (accelerometerUploader != null) {
 							accelerometerUploader.stop();
 						}
 						accelerometerUploader = new AccelerometerUploader(uploadURL, activity);
 					}
 					switch(parser.getMethod()) {
 						case URLParser.METHOD_START_ACCELEROMETER:
 							accelerometerUploader.start();
 							accelerometer_enabled = true;
 							return true;
 						case URLParser.METHOD_STOP_ACCELEROMETER:
 							accelerometerUploader.stop();
 							accelerometer_enabled = false;
 							return true;
 						case URLParser.METHOD_INCREASE_ACCELEROMETER_INTERVAL:
 							accelerometerPeriod *= 2;
 							accelerometerUploader.setPeriod(accelerometerPeriod);
 							System.out.println("accelPeriod = " + accelerometerPeriod);
 							return true;
 						case URLParser.METHOD_DECREASE_ACCELEROMETER_INTERVAL:
 							accelerometerPeriod /= 2;
 							accelerometerUploader.setPeriod(accelerometerPeriod);
 							System.out.println("accelPeriod = " + accelerometerPeriod);
 							return true;
 						case URLParser.METHOD_START_CAMERA:
 						case URLParser.METHOD_GALLERY_OPEN:
 							Intent intent = new Intent(HtmlContainerActivity.this, PhotoUploader.class);
 							String source = (parser.getMethod() == URLParser.METHOD_GALLERY_OPEN) ?
 											"gallery" : "camera";
 							intent.putExtra("source", source);
 							intent.putExtra("uploadURL", uploadURL);
 							startActivityForResult(intent, 1);
 							return true;
 					}
 				}
 				if (url.startsWith(OVERRIDE_PREFIX)) {
 					System.out.println("Starting the override Prefix");
 					Intent intent = new Intent(HtmlContainerActivity.this, HtmlCallbackActivity.class);
 					intent.setData(Uri.parse(url));
 					startActivityForResult(intent, 1);
 					return true;
 				}
 				
 				System.out.println("Ordinary URL: " + url);
 				// ordinary link
 				return false;
 			}
 		});
 
 		String url = getIntent().getStringExtra("url");
 		
 		currentUrl = url;
 
 		if (url != null) {
 			if (url.startsWith(OVERRIDE_PREFIX)) {
 				Intent intent = new Intent(HtmlContainerActivity.this, HtmlCallbackActivity.class);
 				intent.setData(Uri.parse(url));
 				startActivity(intent);
 				// do we want to load a webpage from this scan?  if not, we're done.
 				if (!url.contains("ret="))
 					finish();
 			}
 			else {
 				System.out.println("Webview loading: " + url);
 				webView.loadUrl(url);
 			}
 		} else {
 			// load default url
 			System.out.println("Webview loading default url");
 			webView.loadUrl(getString(R.string.default_url));
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.html_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.refresh:
 			webView.reload();
 			return true;
 		case R.id.gotourl:
 			typeUrl();
 			return true;
 		case R.id.camera:
 			Intent intent = new Intent(HtmlContainerActivity.this, PhotoUploader.class);
 			intent.putExtra("source", "gallery");
 			intent.putExtra("uploadURL", getUploadURL("123", displayID));
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	protected void onSaveinstanceState(Bundle outState) {
 		webView.saveState(outState);
 	}
 	
 	private void typeUrl() {
 		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		final EditText input = new EditText(this);
 		final LinearLayout layout = new LinearLayout(this);
 		final TextView instructions = new TextView(this);
 		input.setText(getString(R.string.default_url));
 		input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
 		layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		input.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));
 		instructions.setText("Enter URL:");
 		instructions.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0f));
 		layout.setOrientation(LinearLayout.VERTICAL);
 		layout.addView(instructions);
 		layout.addView(input);
 		alert.setView(layout);
 		alert.setPositiveButton("Go", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String value = input.getText().toString().trim();
 				webView.loadUrl(value);
 			}
 		});
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				dialog.cancel();
 			}
 		});
 		alert.show();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		if (accelerometer_enabled) {
 			accelerometerUploader.start();
 		}
 		
 		// load callback URL from preferences, if it exists, and whether the user has requested a quit.  
 		// These preferences will only exist if, while we were suspended, our sub-launched self wrote the preferences 
 		// because it wants us to refresh the page or quit.
 		SharedPreferences settings = getSharedPreferences("container_prefs", 0);
 		callbackUrl = settings.getString("callbackUrl", "");
 		
 		nfc_upload = settings.getString("nfc_retrieved_message", "");
 		System.out.println("nfc_upload : " + nfc_upload);
 		
 		// now clear the preferences again so that we don't refresh ourselves/quit if suspended by some other
 		// activity
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("callbackUrl", "");
 		editor.putString("nfc_retrieved_message", "");
 		editor.putBoolean("quitChallenge", false);
 		editor.commit();
 
 		EventService service = new EventServiceImpl(getString(R.string.thing_broker_server),getString(R.string.thing_broker_port) , null, false);
 		Event event = new Event();
 		event.setThingId(getString(R.string.thing_broker_thing_name));
 		Map<String,String> gameInfo = new HashMap<String, String>();
 
 		if (callbackUrl != null && !callbackUrl.equals("")) {
 			if (callbackUrl.equals(HtmlCallbackActivity.ABORT_CODE)) {
 				if (currentUrl == null || currentUrl.startsWith(getString(R.string.default_url))) 
 					finish();
 			}
 			else {
 				System.out.println("Webview loading: " + callbackUrl);
 				webView.loadUrl(callbackUrl);
 				currentUrl = callbackUrl;
 			}
 		} else if (nfc_upload != null && !nfc_upload.equals("")) {
 			//webView.loadUrl("javascript:gotNFC('"+nfc_upload+"');");
 			
 			gameInfo.put("type", "nfcResult");
 			gameInfo.put("value", nfc_upload);
 			event.setInfo(gameInfo);
 			//service.postEvent(STANDARD_MESSAGE, this, event, true);
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (webView.canGoBack()) {
 			webView.goBack();
 		} else {
 			accelerometerUploader.stop();
 			finish();
 		}
 	}
 
     @Override
     protected void onStop() {
     	accelerometerUploader.stop();
         super.onStop();
     }
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		EventService service = new EventServiceImpl(getString(R.string.thing_broker_server),getString(R.string.thing_broker_port) , null, false);
 		Event event = new Event();
 		event.setThingId(getString(R.string.thing_broker_thing_name));
 		Map<String,String> gameInfo = new HashMap<String, String>();
 
 		if (resultCode == RESULT_OK) {
 			IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
 			if (scanResult != null) {
 				String content = scanResult.getContents();
 				displayID = content.substring(content.lastIndexOf('/') + 1, content.length());
 				System.out.println("DisplayID = " + displayID);
 			} else if (data.getStringExtra("method").equals("nfc")) {
 				nfc_upload = data.getStringExtra("uploadResult");
 				System.out.println("NFC upload result: " + nfc_upload);
 				if (nfc_upload != null && !nfc_upload.equals("")) {
 					//webView.loadUrl("javascript:gotNFC('"+nfc_upload+"');");
 				}
 			} else if (data.getStringExtra("method").equals("upload")) {
 				String result = data.getStringExtra("uploadResult");
 				System.out.println("Upload restul: " + result);
 				try {
 					JSONObject json = (JSONObject) new JSONTokener(result).nextValue();
 					Object jsonData = json.get("data");
 					String id = (String)((JSONArray)jsonData).get(0);
 					gameInfo.put("type", "uploadResult");
 					gameInfo.put("value", id);
 					String src = 
 						"http://" + getString(R.string.thing_broker_server) + ":" 
 						+ getString(R.string.thing_broker_port) + "/thingbroker/events/event/content/" + id
 						+ "?mustAttach=false";
 					gameInfo.put("append", "<img src='" + src + "'>");
 					event.setInfo(gameInfo);
 					//service.postEvent(STANDARD_MESSAGE, this, event, true);
 				} catch (JSONException e) {}
 			}
 		}
 	}
 
 	/**
 	 * Custom web chrome client so that we can grant geolocation privileges automatically and display alerts if they come up
 	 * 
 	 * @author tom
 	 *
 	 */
 	class ContainerWebChromeClient extends WebChromeClient {
 		@Override
 		public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
 			callback.invoke(origin, true, false);
 		}
 
 		@Override
 		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
 			new AlertDialog.Builder(view.getContext()).setMessage(message).setCancelable(true).show();
 			result.confirm();
 			return true;
 		}
 
 		@Override
 		public void onProgressChanged(WebView view, int progress) {
 			if (progress < 100 && pbLoading.getVisibility() == ProgressBar.GONE) {
 				pbLoading.setVisibility(ProgressBar.VISIBLE);				
 			}
 			pbLoading.setProgress(progress);
 			if (progress == 100) {
 				pbLoading.setVisibility(ProgressBar.GONE);
 			}
 		}
 	}
 	
 	private void initAppList() {
 		try {
 			DefaultHttpClient httpclient = new DefaultHttpClient();
 			HttpGet getRequest = new HttpGet(getString(R.string.api_url));
 			HttpResponse response = httpclient.execute(getRequest);
 			System.out.println(response.getStatusLine());
 			if (response.getEntity() != null) {
 				ByteArrayOutputStream out = new ByteArrayOutputStream();
 				response.getEntity().writeTo(out);
 				out.close();
 				appList = (JSONArray) new JSONTokener(out.toString()).nextValue();
 				
 				String[] items = new String[appList.length() + 1];
 				for (int i = 0; i < appList.length(); i++) {
 					items[i] = appList.getJSONObject(i).getString("name");
 				}
 				
 				// Last item is for scanning the QR code of the display
 				items[items.length - 1] = "** Scan Display QR **";
 				
				setBehindContentView(R.layout.menu_frame);
 				getSupportFragmentManager()
 					.beginTransaction()
 					.replace(R.id.menu_frame, new MenuFragment(items))
 					.commit();
 			}
 		} catch (Exception e) {
 			e.printStackTrace(System.err);
 		}		
 	}
 	
 	private String getUploadURL(String thingID, String displayID) {
 		// Follow this format: http://kimberly.magic.ubc.ca:8080/thingbroker/things/1231/events
 		String server = getString(R.string.thing_broker_server);
 		String port = getString(R.string.thing_broker_port);
 		String baseURL = "http://" + server + ":" + port + "/thingbroker";
 		return baseURL + "/things/" + thingID + displayID + "/events";
 	}
 
 	public void onMenuItemSelected(int index) {
 		toggle();
 		// If this is for scanning the QR code of a display
 		if (index == appList.length()) {
 			IntentIntegrator integrator = new IntentIntegrator(HtmlContainerActivity.this);
 			integrator.initiateScan();
 		} else {
 			try {
 				String url = appList.getJSONObject(index).getString("mobile_url");
 				if (url == null || url.equals("")) {
 					url = appList.getJSONObject(index).getString("url");
 				}
 				System.out.println("URL: " + url);
 				webView.loadUrl(url);
 			} catch (Exception e) {
 				e.printStackTrace(System.err);
 			}
 		}
 	}
 }
