 package org.fruct.oss.tourme;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.widget.ArrayAdapter;
 import android.widget.Toast;
 
 public class MapActivity extends FragmentActivity implements
 		ActionBar.OnNavigationListener {
 
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * current dropdown position.
 	 */
 	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 	private MapController mapController;
 	private MapView mapView;
 	private ArrayList<Integer> selectedCategories;
 	private Context cont;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 		
 		cont = this;
 
 		// Set up the action bar to show a dropdown list.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowTitleEnabled(false);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 		// Show the Up button in the action bar.
 		// actionBar.setDisplayHomeAsUpEnabled(true);
 
 		// Set up the dropdown list navigation in the action bar.
 		actionBar.setListNavigationCallbacks(
 		// Specify a SpinnerAdapter to populate the dropdown list.
 				new ArrayAdapter<String>(getActionBarThemedContextCompat(),
 						android.R.layout.simple_list_item_1,
 						android.R.id.text1, new String[] {
 								getString(R.string.actionbar_main),
 								getString(R.string.actionbar_map),
 								getString(R.string.actionbar_nearby),
 								getString(R.string.actionbar_favour),
 								getString(R.string.actionbar_log) }), this);
 
 		actionBar.setSelectedNavigationItem(1);
 
 		this.initMap();
 		
 		
 	}
 
 	private WebView myWebView;
 
 	private void initMap() {
 		String url = Environment.getExternalStorageDirectory()
 				+ "/osmdroid/Mapnik";
 		print(url);
 		myWebView = (WebView) findViewById(R.id.mapview);
 		myWebView.getSettings().setJavaScriptEnabled(true);
 		myWebView.loadUrl("file:///android_asset/map.html");
 
 		myWebView.addJavascriptInterface(this, "Android");
 		// url = "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
 		print(url);
 		myWebView.loadUrl("javascript:setUrl(" + url + ")");
 
 		/*
 		 * MapView mapView = new MapView(this, 256); //constructor
 		 * 
 		 * mapView.setClickable(true);
 		 * 
 		 * mapView.setBuiltInZoomControls(true);
 		 * 
 		 * setContentView(mapView); //displaying the MapView
 		 * 
 		 * mapView.getController().setZoom(12); //set initial zoom-level,
 		 * depends on your need
 		 * 
 		 * mapView.getController().setCenter(new GeoPoint(61.800322,34.320819));
 		 * //FIXME This point is in Enschede, Netherlands. You should select a
 		 * point in your map or get it from user's location.
 		 * 
 		 * if (getSharedPreferences(ConstantsAndTools.SHARED_PREFERENCES,
 		 * 0).getBoolean(ConstantsAndTools.ONLINE_MODE, false))
 		 * mapView.setUseDataConnection(true); //keeps the mapView from loading
 		 * online tiles using network connection. else
 		 * mapView.setUseDataConnection(false);
 		 */
 	}
 
 	public void print(String str) {
 		System.out.println(str);
 	}
 
 	/**
 	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that
 	 * simply returns the {@link android.app.Activity} if
 	 * <code>getThemedContext</code> is unavailable.
 	 */
 	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 	private Context getActionBarThemedContextCompat() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			return getActionBar().getThemedContext();
 		} else {
 			return this;
 		}
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		// Restore the previously serialized current dropdown position.
 		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
 			getActionBar().setSelectedNavigationItem(
 					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// Serialize the current dropdown position.
 		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
 				.getSelectedNavigationIndex());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_map, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		case R.id.map_menu_filter:
 			// Show filter dialog
 			PointsCategoriesDialog dialog = new PointsCategoriesDialog();
 			dialog.show(getFragmentManager(), ConstantsAndTools.TAG);
 			break;
 		case R.id.map_menu_nearby:
 			Toast.makeText(cont,  "Jusst a test", Toast.LENGTH_SHORT).show();
 			String Urlik = "http://api.wikilocation.org/articles?lat="+
 					61.78333 + "&lng=" + 34.33333 + "&limit=20&radius=3000&locale=ru&format=json";
			FindWikiArticle dwn = new FindWikiArticle();
 			dwn.execute(Urlik);
 			
			// FIXME
 			//Intent intent = new Intent(this, NearbyActivity.class);
 			//startActivity(intent);
 			break;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int position, long id) {
 		Intent intent = null;
 
 		// I know, it's kinda bicycle, but I dunno how to do better
 		switch (position) {
 		// Goto Main
 		case (0):
 			intent = new Intent(this, MainActivity.class);
 			break;
 		// Goto Map
 		case (1):
 			// intent = new Intent (this, MapActivity.class);
 			break;
 		// Goto Nearby
 		case (2):
 			intent = new Intent(this, NearbyActivity.class);
 			break;
 		// Goto Favourites
 		case (3):
 			intent = new Intent(this, FavourActivity.class);
 			break;
 		// Goto Travel Log
 		case (4):
 			intent = new Intent(this, TravellogActivity.class);
 			break;
 		default:
 			Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_LONG)
 					.show();
 			break;
 		}
 
 		if (intent != null) {
 			startActivity(intent);
 			finish();
 		}
 
 		return true;
 	}
 
 	/**
 	 * Categories chooser
 	 */
 	/*
 	 * public class PointsCategoriesDialog extends DialogFragment {
 	 * 
 	 * @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
 	 * AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 	 * builder.setTitle(R.string.map_points_categories_title)
 	 * .setItems(R.array.map_points_categories, new
 	 * DialogInterface.OnClickListener() { public void onClick(DialogInterface
 	 * dialog, int which) { // TODO: do something // 'which' is the selected
 	 * item number } }); return builder.create(); } }
 	 */
 
 	/**
 	 * Show privacy settings dialog
 	 */
 
 	@SuppressLint("ValidFragment")
 	public class PointsCategoriesDialog extends DialogFragment {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			selectedCategories = new ArrayList<Integer>(); // Where we track the
 															// selected items
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			// Set the dialog title
 			builder.setTitle(R.string.map_points_categories_title)
 
 					// TODO: get selected items by default or from
 					// SharedPreferences
 					// boolean[] selectedCategoriesByDefault;
 
 					// Specify the list array, the items to be selected by
 					// default (null for none),
 					// and the listener through which to receive callbacks when
 					// items are selected
 					.setMultiChoiceItems(R.array.map_points_categories, null,
 							new DialogInterface.OnMultiChoiceClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which, boolean isChecked) {
 									if (isChecked) {
 										// If the user checked the item, add it
 										// to the selected items
 										selectedCategories.add(which);
 									} else if (selectedCategories
 											.contains(which)) {
 										// Else, if the item is already in the
 										// array, remove it
 										selectedCategories.remove(Integer
 												.valueOf(which));
 									}
 								}
 							})
 
 					// Set the action buttons
 					.setPositiveButton(R.string.ok,
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int id) {
 									// User clicked OK, so save the
 									// mSelectedItems results somewhere
 									// or return them to the component that
 									// opened the dialog
 									// TODO: do something
 									// System.out.print("asdasda");
 									// myWebView.loadUrl("javascript:setZoom(1)");
 									String[] testArray = getResources()
 											.getStringArray(
 													R.array.map_points_categories);
 									System.out.print(testArray);
 								}
 							})
 					.setNegativeButton(R.string.cancel,
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int id) {
 									return;
 								}
 							});
 
 			return builder.create();
 		}
 	}
 
	class FindWikiArticle extends AsyncTask<String, Integer, String> {
 		
 		HttpResponse response;
 		String jsonString = null;
 
 		@Override
 		protected String doInBackground(String... sUrl) {
 			try {
 				String url = sUrl[0];
 
 				StringBuilder builder = new StringBuilder();
 		        HttpClient client = new DefaultHttpClient();
 		        HttpGet httpGet = new HttpGet(url);
 		        HttpResponse response = client.execute(httpGet);
 		        StatusLine statusLine = response.getStatusLine();
                 int statusCode = statusLine.getStatusCode();
                 if (statusCode == 200) {
                     HttpEntity entity = response.getEntity();
                     InputStream content = entity.getContent();
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(content));
                     String line;
                     while ((line = reader.readLine()) != null) {
                         builder.append(line);
                     }
                     jsonString = builder.toString();
                 } else {
                     Log.e("Err", "Failed to download file");
                 }				
 				
 			} catch (Exception e) {
 				Log.e("ERROR downloading file", e.getMessage());
 				// Show Toast message on UI thread
 				
 				 runOnUiThread(new Runnable() { public void run() {
 				 Toast.makeText(cont, "Klingon: Fyah REerf NEtwfk Err", Toast.LENGTH_SHORT).show();
 			}
 		});
 				 
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onCancelled() {
 			Log.i("DWNLD", "cancelled");
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			Log.i("DWNLD", "preExec");
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			super.onProgressUpdate(progress);
 			Log.i("DWNLD", "ProgressUpd");
 			// TODO Set progress
 		}
 
 		@Override
 		protected void onPostExecute(String str) {
 			super.onPostExecute("OK");
 			
 			JSONObject json;
 			
 			String articleUrl = null;
 			String articleTitle = null;
 			try {
 				json = new JSONObject(jsonString);
 				JSONArray elems = json.getJSONArray("articles");
 				JSONObject link = elems.getJSONObject(0);
 				
 				articleUrl = link.get("mobileurl").toString();
 				articleTitle = link.get("title").toString();
 				Log.e("url", articleUrl);
 				Log.e("title", articleTitle);
 			} catch (JSONException e) {	}
 			
 			if (articleUrl != null) {
 				// Open ArticleActivity for an article
 				Intent myInt = new Intent(cont, ArticleActivity.class);
 				myInt.putExtra(ConstantsAndTools.ARTICLE_ID, articleUrl);
 				myInt.putExtra(ConstantsAndTools.ARTICLE_TITLE, articleTitle);
 				startActivity(myInt);
 			}	
 
 		}
 	}
 
 }
