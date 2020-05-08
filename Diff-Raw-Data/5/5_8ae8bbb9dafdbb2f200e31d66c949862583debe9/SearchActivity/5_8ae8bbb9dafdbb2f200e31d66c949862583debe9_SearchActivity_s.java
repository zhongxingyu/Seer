 package com.codepath.gridimagesearch;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.Toast;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class SearchActivity extends Activity {
 	EditText etQuery;
 	GridView gvResults;
 	Button btnSearch;
 	ArrayList<ImageResult> imageResults = new ArrayList<ImageResult>();
 	ImageResultArrayAdapter imageAdapter;
 	static final int UPDATE_OPTIONS_REQUEST = 0;
 	OptionSet options = null;
 	int imageStart;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_search);
         setupViews();
         imageAdapter = new ImageResultArrayAdapter(this, imageResults);
         gvResults.setAdapter(imageAdapter);
         btnSearch.setEnabled(false);
         addListeners();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.search, menu);
         return true;
     }
     
     private void setupViews() {
         etQuery = (EditText) findViewById(R.id.etQuery);
         gvResults = (GridView) findViewById(R.id.gvResults);
         btnSearch = (Button) findViewById(R.id.btnSearch);
     }
     
     private void addListeners() {
         etQuery.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {				
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {				
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				if (etQuery.getText().toString().trim().isEmpty()) {
 					btnSearch.setEnabled(false);
 				} else {
 					btnSearch.setEnabled(true);
 				}
 			}
 		});
         
         gvResults.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View parent, int position,
 					long rowId) {
 				Intent i = new Intent(getApplicationContext(), ImageDisplayActivity.class);
 				ImageResult imageResult = imageResults.get(position);
 				i.putExtra("result", imageResult);
 				startActivity(i);
 			}
         	
 		});
         
         gvResults.setOnScrollListener(new EndlessScrollListener() {
 			
 			@Override
 			public void onLoadMore(int page, int totalItemsCount) {
 				getImageResults(false);
 			}
 		});
     }
     
     public void onImageSearch(View v) {
     	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
     	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
     	
     	getImageResults(true);
     }
     
     private void getImageResults(final boolean firstPage) {
     	String query = etQuery.getText().toString();
     	AsyncHttpClient client = new AsyncHttpClient();
     	
     	if (firstPage){
     		imageStart = 0;
         	Toast.makeText(this, "Searching for " + query, Toast.LENGTH_SHORT).show();
     	} else {
    		imageStart += 15;
     	}
     	
     	String optionsQuery = "";
     	if (options != null) {
     		optionsQuery += "&imgsz=" + options.getSize();
     		optionsQuery += "&imgcolor=" + options.getColor();
     		optionsQuery += "&imgtype=" + options.getType();
     		optionsQuery += "&as_sitesearch=" + options.getFilter();
     	}
     	
    	client.get("https://ajax.googleapis.com/ajax/services/search/images?rsz=15&" + "start=" + imageStart + "&v=1.0&q=" + Uri.encode(query) + optionsQuery,
     			new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONObject response) {
 				JSONArray imageJsonResults = null;
 				try {
 					imageJsonResults = response.getJSONObject("responseData").getJSONArray("results");
 					
 					if (firstPage){
 						imageResults.clear();
 					}
 					
 					imageAdapter.addAll(ImageResult.fromJSONArray(imageJsonResults));
 					// TODO: remove logger after done working on this project
 					Log.d("DEBUG", imageResults.toString());
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
     	});
     }
     
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.action_settings:
 				Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
 				i.putExtra("options", options);
 				startActivityForResult(i, UPDATE_OPTIONS_REQUEST);
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == UPDATE_OPTIONS_REQUEST && resultCode == RESULT_OK) {
 			options = (OptionSet) data.getExtras().getSerializable("options");
 		}
 	}
 }
