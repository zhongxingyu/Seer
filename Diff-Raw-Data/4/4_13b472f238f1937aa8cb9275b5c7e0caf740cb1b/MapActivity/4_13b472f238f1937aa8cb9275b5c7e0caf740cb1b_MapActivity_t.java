 package com.aluen.tracerecorder;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Gravity;
 import android.view.ViewGroup;
 import android.webkit.ConsoleMessage;
 import android.webkit.JsResult;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 import android.widget.Toast;
 
 import com.aluen.tracerecoder.R;
 import com.aluen.tracerecorder.util.GeoPoint;
 import com.aluen.tracerecorder.util.LinePath;
 import com.aluen.tracerecorder.util.Record;
 import com.aluen.tracerecorder.util.SinglePoint;
 import com.aluen.tracerecorder.util.Utility;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 @SuppressLint("SetJavaScriptEnabled")
 public class MapActivity extends FragmentActivity implements
 		OnMarkerClickListener {
 
 	private String dirName;
 	private WebView webView;
 	private PopupWindow popupWindow;
 	private LinearLayout llWrapper;
 	private GoogleMap mMap;
 	// hmNameToPoint is a hashmap that record the file name to a singlepoint
 	// object,which contains the html used in the webview
 	private HashMap<String, SinglePoint> hmNameToPoint = new HashMap<String, SinglePoint>();
 	// Handle to SharedPreferences for this app
 	private SharedPreferences mPrefs;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 
 		// Open Shared Preferences
 		mPrefs = getSharedPreferences(Utility.SHARED_PREFERENCES,
 				Context.MODE_PRIVATE);
 
 		setUpMapIfNeeded();
 		setupWebView();
 	}
 
 	/**
 	 * Called when you click the marker,and then will display the info.
 	 * 
 	 * @param html
 	 */
 	@SuppressWarnings("deprecation")
 	private void popupWindow(String html) {
 		String data = "<script>window.isMobile = true;</script>" + html;
 		String baseUrl = "file://" + dirName + "/gallery.html";
 		webView.loadDataWithBaseURL(baseUrl, data, "text/html", "UTF-8",
 				"file://" + dirName + "/");
 
 		int height = (int) (llWrapper.getHeight() * 0.6);
 		popupWindow = new PopupWindow(webView,
 				ViewGroup.LayoutParams.MATCH_PARENT, height);
 		popupWindow.setFocusable(true);
 		popupWindow.setOutsideTouchable(true);
 		popupWindow.setBackgroundDrawable(new BitmapDrawable());
 		int x = -popupWindow.getWidth() / 2;
 		popupWindow.showAtLocation(llWrapper, Gravity.CENTER, x, 0);
 	}
 
 	private void setupWebView() {
 		webView = new WebView(this);
 		webView.getSettings().setJavaScriptEnabled(true);
 		webView.setWebChromeClient(new WebChromeClient() {
 			@Override
 			public boolean onJsAlert(WebView view, String url, String message,
 					JsResult result) {
 				// TODO Auto-generated method stub
 				return super.onJsAlert(view, url, message, result);
 			}
 
 			@Override
 			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
 				// TODO Auto-generated method stub
 				return super.onConsoleMessage(consoleMessage);
 			}
 
 		});
 		webView.setWebViewClient(new WebViewClient() {
 			@Override
 			public void onPageStarted(WebView view, String url, Bitmap favicon) {
 				// TODO Auto-generated method stub
 				super.onPageStarted(view, url, favicon);
 			}
 
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				// TODO Auto-generated method stub
 				super.onPageFinished(view, url);
 			}
 		});
 		llWrapper = (LinearLayout) findViewById(R.id.llWrapper);
 	}
 
 	@Override
 	protected void onResume() {
 		setTitle(R.string.map_title);
 		super.onResume();
 		setUpMapIfNeeded();
 	}
 
 	private void setUpMapIfNeeded() {
 		// Do a null check to confirm that we have not already instantiated the
 		// map.
 		if (mMap == null) {
 			// Try to obtain the map from the SupportMapFragment.
 			mMap = ((SupportMapFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.map)).getMap();
 			// Check if we were successful in obtaining the map.
 			if (mMap != null) {
 				setUpMap();
 			}
 		}
 	}
 
 	/**
 	 * Draw the line path and Marker that you have record in the travel. The
 	 * PolylineOptions will be a preference in the future.
 	 */
 	private void setUpMap() {
 		dirName = mPrefs.getString("PATH", "n/a");
 		// add MarkerClickListener
 		mMap.setOnMarkerClickListener(this);
 		Marker marker;
 
 		// Here will be changed in the future, sharedPreference will be ok.
 		PolylineOptions poLinePath = new PolylineOptions().width(5)
 				.color(Color.BLUE).geodesic(true);
 		Record record = new Record(dirName, getApplicationContext(),
 				getAssets());
 		ArrayList<SinglePoint> path = record.getList();
 
 		for (int i = 0; i < path.size(); i++) {
 			SinglePoint sp = (SinglePoint) path.get(i);
 			marker = mMap.addMarker(new MarkerOptions().position(sp
 					.getLocation().getTransformedLatLng()));
 			hmNameToPoint.put(marker.getId(), sp);
 		}
 
 		LinePath lp = record.getPath();
 		ArrayList<GeoPoint> points = lp.getPoints();
 		if (0 != points.size()) {
 			for (int j = 0; j < points.size(); ++j) {
 				poLinePath.add(points.get(j).getTransformedLatLng());
 			}
 			mMap.addPolyline(poLinePath);
 			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0)
 					.getTransformedLatLng(), 15));
 		} else {
 			Toast.makeText(this, R.string.no_data, Toast.LENGTH_LONG).show();
 			this.finish();
 		}
 	}
 
 	/**
 	 * Called when a marker be clicked , and the will appear a popup windows
 	 * That will display the content of this point(such as pic ,description,and
 	 * time)
 	 * 
 	 * @param Marker
 	 *            that will be used to get the marker id.
 	 * @see com.google.android.gms.maps.GoogleMap.OnMarkerClickListener#onMarkerClick
 	 *      (com.google.android.gms.maps.model.Marker)
 	 * @return true will be always
 	 */
 	@Override
 	public boolean onMarkerClick(final Marker marker) {
 		// TODO Auto-generated method stub
 		SinglePoint sp = hmNameToPoint.get(marker.getId());
 		popupWindow(sp.getHtml());
 		return true;
 	}
 
 }
