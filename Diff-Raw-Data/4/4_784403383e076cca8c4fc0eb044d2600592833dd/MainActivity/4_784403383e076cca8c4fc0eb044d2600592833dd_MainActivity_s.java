 /*
  * 
  */
 package com.example.java1week2_4;
 
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.example.lib.WebStuff;
 import com.loopj.android.image.SmartImageView;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 /**
  * The Class MainActivity.
  */
 public class MainActivity extends Activity {
 
 	// Global variables
 	Context _context;
 	Boolean _connected = false;
 	JSONObject _json;
 	EditText _searchField;
 	TextView results;
 	Spinner iSpin;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Set Content View
 		setContentView(R.layout.main);
 
 		// Set defaults for variables
 		_searchField = (EditText) findViewById(R.id.searchField);
 		results = (TextView) findViewById(R.id.resultsText);
 		iSpin = (Spinner) findViewById(R.id.infoSpinner);
 		_context = this;
 
 		// Detect Network connection
 		_connected = WebStuff.getConnectionStatus(_context);
 		if (_connected) {
 			Log.i("NETWORK CONNECTION", WebStuff.getConnectionType(_context));
 		} else {
 			// If no connection load last viewed movie details
 
 			// Warning Display
 			TextView warningText = new TextView(this);
 			warningText.setText("No connection. Viewing last searched movie.");
 			warningText.setGravity(Gravity.CENTER);
 			warningText.setTextSize(30);
 			// _appLayout.addView(warningText);
 
 			// Load last JSON file
 			Object stored = FileStuff.readObjectFile(_context, "Movie", true);
 			if (stored != null) {
 				String temp = (String) stored;
 				try {
 					_json = new JSONObject(temp);
 					Log.i("JSON", _json.toString());
 
 				} catch (JSONException e) {
 					Log.e("SAVED FILE", "CANT MAKE JSON");
 				}
 			}
 
 			// Set Search Text
 			try {
 				_searchField.setText(_json.getString("title").toString());
 			} catch (JSONException e) {
 				Log.e("SAVED FILE", "CANT LOAD MOVIE TITLE");
 			}
 		}
 
 		// Search Handler
 		Button searchButton = (Button) findViewById(R.id.searchButton);
 		searchButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Log.i("CLICK HANDLER", _searchField.getText().toString());
 				// Send text field entry to URL builder and request API
 				getQuote(_searchField.getText().toString());
 				// Set spinner back to default
 				iSpin.setSelection(0);
 				// Close keyboard
 				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 				inputManager.hideSoftInputFromWindow(getCurrentFocus()
 						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 			}
 		});
 
 		// Create Info Spinner Display
 		ArrayAdapter<CharSequence> listAdapter = ArrayAdapter
 				.createFromResource(_context, R.array.detailsArray,
 						android.R.layout.simple_spinner_item);
 		listAdapter
 				.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
 		iSpin.setAdapter(listAdapter);
 
 		// Info Spinner Handler
 		iSpin.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View v, int pos,
 					long id) {
 				Log.i("DETAIL SELECTED", parent.getItemAtPosition(pos)
 						.toString());
 				// Check to see if JSON has been created yet
 				if (_json != null) {
 					try {
 						// Set selected value
 						String selected = new String(parent.getItemAtPosition(
 								pos).toString());
 						Log.i("DETAIL SELECTED", selected);
 						// If not at default value fetch results
 						if (!selected.matches(getString(R.string.see_details))) {
 							// Change from nice formatting to API formatting
 							// Title
 							if (selected.matches(getString(R.string.title))) {
 								results.setText("Movie Title:\n \t"
 										+ _json.getString(
 												getString(R.string.titleAPI))
 												.toString());
 								// Year
 							} else if (selected
 									.matches(getString(R.string.year))) {
 								results.setText("Year Released:\n \t"
 										+ _json.getString(
 												getString(R.string.yearAPI))
 												.toString());
 								// Rating
 							} else if (selected
 									.matches(getString(R.string.mpaa_rating))) {
 								results.setText("MPAA Rating:\n \t"
 										+ _json.getString(
 												getString(R.string.mpaa_ratingAPI))
 												.toString());
 								// Runtime
 							} else if (selected
 									.matches(getString(R.string.runtime))) {
 								results.setText("Movie Runtime:\n \t"
 										+ _json.getString(
 												getString(R.string.runtimeAPI))
 												.toString() + " minutes");
 								// Critics
 							} else if (selected
 									.matches(getString(R.string.critics_consensus))) {
 								results.setText("Critics Consensus:\n \t"
 										+ _json.getString(
 												getString(R.string.critics_consensusAPI))
 												.toString());
 								/*
								 * Movie Poster I, for the life of me can't
 								 * figure out why this isn't working. Please
 								 * help.
 								 */
 							} else if (selected
 									.matches(getString(R.string.thumbnail))) {
 								results.setText("Poster:\n \t");
 								String link = new String(_json.getJSONObject(
 										"posters").getString(
 										getString(R.string.thumbnailAPI)
 												.toString()));
 								SmartImageView iView = new SmartImageView(_context);
 								try {
 									iView.setImageUrl(link);
 									LinearLayout ll = (LinearLayout) findViewById(R.layout.main);
 									ll.addView(iView);
 								} catch (Exception e) {
 									Log.e("IMAGE", "NO IMAGE");
 								}
 							}
 						}
 					} catch (JSONException e) {
 						Log.e("RESULTS ERROR", "NO RESULTS");
 						results.setText("No info found. Please double check that the movie title was entered correctly");
 					}
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {
 
 			}
 		});
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	// Fetch Movie poster
 	Bitmap drawable_from_url(String url) throws java.net.MalformedURLException,
 			java.io.IOException {
 		Bitmap bit;
 
 		HttpURLConnection connection = (HttpURLConnection) new URL(url)
 				.openConnection();
 
 		connection.connect();
 		InputStream input = connection.getInputStream();
 
 		bit = BitmapFactory.decodeStream(input);
 		return bit;
 	}
 
 	// Build URL
 	private void getQuote(String movieName) {
 		String baseURL = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=zns9a4f2hfm94ju3unu4axzk&q=";
 		String qs;
 		try {
 			qs = URLEncoder.encode(movieName, "UTF-8");
 		} catch (Exception e) {
 			Log.e("BAD URL", "ENCODING PROBLEM");
 			qs = "";
 		}
 		// Send URl to Async Task
 		URL finalURL;
 		try {
 			finalURL = new URL(baseURL + qs + "&page_limit=1");
 			QuoteRequest qr = new QuoteRequest();
 			qr.execute(finalURL);
 		} catch (MalformedURLException e) {
 			Log.e("BAD URL", "MALFORMED URL");
 			finalURL = null;
 		}
 	}
 
 	// Send URL to web stuff to pull data
 	private class QuoteRequest extends AsyncTask<URL, Void, String> {
 		@Override
 		protected String doInBackground(URL... urls) {
 			String response = "";
 			for (URL url : urls) {
 				response = WebStuff.getURLStringResponse(url);
 			}
 			return response;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			try {
 				// Set data
 				Log.i("JSON", result);
 				JSONObject resultJSON = new JSONObject(result);
 				try {
 					/*
 					 * JSON Array is Movies. object 0 is the first result. get
 					 * string provides the detail Object.
 					 */
 
 					if (resultJSON.getString("total").toString().compareTo("0") == 0) {
 						Toast toast = Toast.makeText(_context,
 								"No movie by that name exists",
 								Toast.LENGTH_LONG);
 						toast.show();
 					} else {
 						_json = resultJSON.getJSONArray("movies")
 								.getJSONObject(0);
 						// Store object in external memory
 						FileStuff.storeObjectFile(_context, "Movie",
 								_json.toString(), true);
 						results.setText("");
 					}
 				} catch (JSONException e) {
 					Log.e("JSON", "JSON OBJECT EXCEPTION");
 					results.setText("No info found. Please double check that the movie title was entered correctly");
 				}
 			} catch (JSONException e) {
 				Log.e("JSON", "JSON OBJECT EXCEPTION");
 				results.setText("No info found. Please double check that the movie title was entered correctly");
 			}
 		}
 	}
 
 }
