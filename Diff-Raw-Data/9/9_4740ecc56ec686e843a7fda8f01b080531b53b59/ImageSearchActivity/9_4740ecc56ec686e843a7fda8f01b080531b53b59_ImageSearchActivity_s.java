 package com.medha.imagesearch;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.Toast;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class ImageSearchActivity extends Activity {
 	EditText etSearch;
 	Button bSearch;
 	GridView gvImages;
 
 	ArrayList<ImageResult> imageResults = new ArrayList<ImageResult>();
 	ImageResultArrayAdapter imageAdapter;
 	private static int REQUEST_CODE = 1;
 	private static int RESULT_OK = 1;
 	private String searchQuery;
 	private String imgsz;
 	private String imgcolor;
 	private String imgtype;
 	private String as_sitesearch;
 	private int imgcolorPos;
 	private int imgszPos;
 	private int imgtypePos;
 	private static String BASE_ADDRESS = "https://ajax.googleapis.com/ajax/services/search/images?";
 	int start = 0; 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_image_search);
 		setupViews();
 		imageAdapter = new ImageResultArrayAdapter(this, imageResults);
 		gvImages.setAdapter(imageAdapter);
 		gvImages.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View parent,
 					int position, long id) {
 				Intent intent = new Intent(getApplicationContext(),
 						ImageDisplayActivity.class);
 				ImageResult imageResult = imageResults.get(position);
 				intent.putExtra("result", imageResult);
 				startActivity(intent);
 			}
 
 		});
 		
 		gvImages.setOnScrollListener(new EndlessScrollListener() {
 			
 			@Override
 			public void onLoadMore(int page, int totalItemsCount) {
 				
 				loadMore(totalItemsCount);
 				
 			}
 		});
 	}
 	
 	public void loadMore(int totalItemsCount) {
		start = totalItemsCount-8;
 		String fullUrl;
 		if (imgsz != null && imgcolor!= null && imgtype!= null  && as_sitesearch!= null) {
 		
 		 fullUrl = BASE_ADDRESS + "rsz=8" + "&start=" +start + "&v=1.0"
 				+ "&imgsz=" + imgsz + "&imgcolor=" + imgcolor + "&imgtype="
 				+ imgtype + "&as_sitesearch=" + as_sitesearch + "&q="
 				+ Uri.encode(searchQuery);
 		} else if ( imgsz != null && imgcolor!= null && imgtype!= null && as_sitesearch== null) {
 			 fullUrl = BASE_ADDRESS + "rsz=8" + "&start=" + start + "&v=1.0"
 					+ "&imgsz=" + imgsz + "&imgcolor=" + imgcolor + "&imgtype="
 					+ imgtype + "&q=" + Uri.encode(searchQuery);
 		} else {
 			 fullUrl = BASE_ADDRESS + "rsz=8" + "&start=" + start + "&v=1.0" + "&q=" + Uri.encode(searchQuery);
 		}
 
 		AsyncHttpClient client = new AsyncHttpClient();
//		if (start == 0) {
//		loadImages(client, fullUrl, true);
//		} else {
 			loadImages(client, fullUrl, false);
//		}
		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.image_search, menu);
 		return true;
 	}
 
 	public void setupViews() {
 		etSearch = (EditText) findViewById(R.id.etSearch);
 		bSearch = (Button) findViewById(R.id.bSearch);
 		gvImages = (GridView) findViewById(R.id.gvImages);
 	}
 
 	public void onImageSearch(View v) {
 		searchQuery = etSearch.getText().toString();
 		if (searchQuery != null) {
 			Toast toast = Toast.makeText(this, "Searching for " + searchQuery,
 					Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 300);
 			toast.show();
 			
 			String fullUrl;
 			if (imgsz != null && imgcolor!= null && imgtype!= null  && as_sitesearch!= null) {
 			
 			 fullUrl = BASE_ADDRESS + "rsz=8" + "&start=0" + "&v=1.0"
 					+ "&imgsz=" + imgsz + "&imgcolor=" + imgcolor + "&imgtype="
 					+ imgtype + "&as_sitesearch=" + as_sitesearch + "&q="
 					+ Uri.encode(searchQuery);
 			} else if ( imgsz != null && imgcolor!= null && imgtype!= null && as_sitesearch== null) {
 				 fullUrl = BASE_ADDRESS + "rsz=8" + "&start=0" + "&v=1.0"
 						+ "&imgsz=" + imgsz + "&imgcolor=" + imgcolor + "&imgtype="
 						+ imgtype + "&q=" + Uri.encode(searchQuery);
 			} else {
 				 fullUrl = BASE_ADDRESS + "rsz=8" + "&start=0" + "&v=1.0" + "&q=" + Uri.encode(searchQuery);
 			}
 
 			AsyncHttpClient client = new AsyncHttpClient();
 			loadImages(client, fullUrl, true);
 		} else {
 			Toast toast = Toast.makeText(this, "Please enter a search query!", Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 300);
 			toast.show();
 		}
 
 	}
 
 	public void onSettingsAction(MenuItem mi) {
 		Log.d("INFO", "Setting Clicked. Launching Settings Activity");
 		Intent i = new Intent(this, SettingsActivity.class);
 		i.putExtra("imgsz", imgsz);
 		i.putExtra("imgcolor", imgcolor);
 		i.putExtra("imgtype", imgtype);
 		i.putExtra("as_sitesearch", as_sitesearch);
 		
 		 i.putExtra("imgszPos", imgszPos);
 		  i.putExtra("imgcolorPos",imgcolorPos);
 		  i.putExtra("imgtypePos", imgtypePos);
 		this.startActivityForResult(i, REQUEST_CODE);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_OK && searchQuery != null) {
 			imgsz = data.getExtras().getString("imgsz");
 			imgcolor = data.getExtras().getString("imgcolor");
 			imgtype = data.getExtras().getString("imgtype");
 			as_sitesearch = data.getExtras().getString("as_sitesearch");
 			
 			imgszPos = data.getExtras().getInt("imgszPos");
 			imgcolorPos =  data.getExtras().getInt("imgcolorPos");
 			imgtypePos =  data.getExtras().getInt("imgtypePos");
 
 			String fullUrl = BASE_ADDRESS + "rsz=8" + "&start=0" + "&v=1.0"
 					+ "&imgsz=" + imgsz + "&imgcolor=" + imgcolor + "&imgtype="
 					+ imgtype + "&as_sitesearch=" + as_sitesearch + "&q="
 					+ Uri.encode(searchQuery);
 
 			AsyncHttpClient client = new AsyncHttpClient();
 			loadImages(client, fullUrl, true);
 		}
 	}
 
 	private void loadImages(AsyncHttpClient client, String fullUrl, final boolean clear) {
 		client.get(fullUrl, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONObject response) {
 				JSONArray imageJsonResults = null;
 				try {
 					imageJsonResults = response.getJSONObject("responseData")
 							.getJSONArray("results");
 					if (clear) {
 						imageResults.clear();
 					}
 					imageAdapter.addAll(ImageResult
 							.fromJSONArray(imageJsonResults));
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 }
